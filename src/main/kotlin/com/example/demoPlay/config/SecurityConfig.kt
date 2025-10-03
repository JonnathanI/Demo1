package com.example.demoPlay.config

import com.example.demoPlay.security.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    // Inyectamos nuestro servicio que sabe cómo cargar usuarios de la DB
    private val userDetailsService: UserDetailsServiceImpl
) {

    // 1. BEAN para el hasheo de contraseñas (PasswordEncoder)
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // 2. BEAN para el AuthenticationManager
    // Es el corazón del proceso de login y es inyectado en el AuthController
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    // 3. BEAN que define cómo autenticar (comparar contraseña y cargar usuario)
    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        // Le decimos a Spring dónde buscar el usuario
        authProvider.setUserDetailsService(userDetailsService)
        // Le decimos a Spring cómo verificar la contraseña
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    // 4. CADENA DE FILTROS DE SEGURIDAD (Reglas de Acceso a Endpoints)
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .authorizeHttpRequests { auth ->
                auth
                    // Rutas públicas: Registro y Login deben ser accesibles
                    .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                    .requestMatchers("/api/game/questions").permitAll()

                    // Requerir autenticación para CUALQUIER otra ruta
                    .anyRequest().authenticated()
            }

            // Usamos httpBasic por ahora. (Debería ser reemplazado por JWT/Bearer Token después)
            .httpBasic { }

        return http.build()
    }
}
