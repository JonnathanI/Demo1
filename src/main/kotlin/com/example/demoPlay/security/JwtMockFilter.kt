package com.example.demoPlay.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro de Seguridad MOCK para simular la autenticación JWT
 * usando tokens simples como "MOCK_TOKEN_1_ADMIN" o "MOCK_TOKEN_2_USER".
 */
@Component
class JwtMockFilter : OncePerRequestFilter() {

    // Constantes para el token
    private val TOKEN_PREFIX = "Bearer "
    private val MOCK_TOKEN_START = "MOCK_TOKEN"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            val token = authorizationHeader.substring(TOKEN_PREFIX.length).trim() // Asegura limpieza

            if (token.startsWith(MOCK_TOKEN_START)) {

                // Dividir el token: ["MOCK", "TOKEN", "1", "ADMIN"]
                val parts = token.split("_")

                // CRÍTICO: El token debe tener 4 partes
                if (parts.size == 4) {
                    val userId = parts[2] // ID del usuario (ej: "1")
                    val baseRole = parts[3].uppercase() // Rol (ej: "ADMIN")

                    // Crear el rol completo de Spring Security: ROLE_ADMIN
                    val fullRole = "ROLE_$baseRole"

                    // --- DEBUG ---
                    // Imprime en tu consola de Kotlin para verificar el rol asignado
                    println(">>> DEBUG MOCK: Token: $token -> Asignando ID: $userId con Rol: $fullRole")
                    // --- END DEBUG ---

                    // Crear la autoridad de Spring Security
                    val authorities = listOf(SimpleGrantedAuthority(fullRole))

                    // Crear el objeto de autenticación
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId, // Principal (el ID del usuario)
                        null,   // Credentials
                        authorities
                    )

                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    println(">>> DEBUG MOCK: Formato de token inválido: $token")
                }
            }
        }

        // Continúa la cadena de filtros
        filterChain.doFilter(request, response)
    }
}
