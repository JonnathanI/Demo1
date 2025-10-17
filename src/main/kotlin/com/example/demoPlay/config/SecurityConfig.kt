package com.example.demoPlay.config

import com.example.demoPlay.security.JwtMockFilter
import com.example.demoPlay.security.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val userDetailsService: UserDetailsServiceImpl,
    private val jwtMockFilter: JwtMockFilter
) {

    // --- Beans de Configuración Esenciales ---

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.authenticationProvider(authenticationProvider())
        return authenticationManagerBuilder.build()
    }

    // --- Configuración de CORS ---
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173", "http://127.0.0.1:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }


    // --- Cadena de Filtros de Seguridad ---
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 1. 🔑 RUTAS PÚBLICAS: Registro, Login, Google Login y Recuperación
                    .requestMatchers(
                        "/api/users/login",
                        "/api/users/register",
                        // 💡 NUEVO: Añadido el endpoint de Google
                        "/api/users/login/google",
                        "/api/users/forgot-password",
                        "/api/users/reset-password"
                    ).permitAll()

                    // 2. RUTAS DE PERFIL Y PUNTOS (USERS & ADMIN)
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/users/{userId}/profile",
                        // 🛑 CORRECCIÓN APLICADA: Añadida la ruta de perfil completo
                        "/api/users/{userId}/profile/full",
                        "/api/users/points"
                    ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                    // 3. RUTAS DE JUEGO GENERALES (USERS & ADMIN)
                    // ✅ CORRECCIÓN CLAVE: Permitir que los USUARIOS accedan a las preguntas del juego.
                    .requestMatchers(HttpMethod.GET, "/api/questions/game").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                    .requestMatchers("/api/game/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/users/{userId}/buy-hint").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")


                    // 4. RUTAS DE SÓLO ADMINISTRADOR
                    .requestMatchers(
                        "/api/users/all",
                        "/api/users/{userId}/admin-update",
                        "/api/users/{userId}", // DELETE
                        // Las demás rutas bajo /api/questions/ (CRUD) se mantienen solo para ADMIN
                        "/api/questions/**"
                    ).hasAuthority("ROLE_ADMIN")

                    // 5. FALLBACK
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtMockFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
