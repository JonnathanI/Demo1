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
 *
 * NOTA: Esta versión incluye validaciones más estrictas para asegurar que
 * Spring Security reciba un objeto de autenticación válido o un error claro.
 */
@Component
class JwtMockFilter : OncePerRequestFilter() {

    // Constantes para el token
    private val TOKEN_PREFIX = "Bearer "
    private val MOCK_TOKEN_START = "MOCK_TOKEN"
    private val EXPECTED_PARTS = 4 // e.g., MOCK_TOKEN_1_ADMIN

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            val token = authorizationHeader.substring(TOKEN_PREFIX.length).trim()

            if (token.startsWith(MOCK_TOKEN_START)) {

                // Intentar procesar el token.
                try {
                    // Dividir el token: ["MOCK", "TOKEN", "1", "ADMIN"]
                    val parts = token.split("_")

                    if (parts.size == EXPECTED_PARTS) {
                        val userId = parts[2]
                        val baseRole = parts[3].uppercase()
                        val fullRole = "ROLE_$baseRole"

                        // --- DEBUG ---
                        println(">>> DEBUG MOCK: Token Válido. Asignando ID: $userId con Rol: $fullRole")
                        // --- END DEBUG ---

                        // Crear la autoridad y el objeto de autenticación
                        val authorities = listOf(SimpleGrantedAuthority(fullRole))
                        val authentication = UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                        )

                        // Establecer la autenticación en el contexto de seguridad (¡ÉXITO!)
                        SecurityContextHolder.getContext().authentication = authentication
                    } else {
                        // Si el formato es incorrecto pero empieza con MOCK_TOKEN
                        println(">>> DEBUG MOCK: Formato de token MOCK incorrecto: $token. Esperado ${EXPECTED_PARTS} partes.")
                        // NOTA: No hacemos nada aquí, la petición será denegada por 403.
                    }
                } catch (e: Exception) {
                    // Si algo falla al procesar el token
                    println(">>> DEBUG MOCK: Error al procesar token MOCK: $token. Error: ${e.message}")
                }
            } else {
                // NOTA: Si el token no es MOCK_TOKEN, pero está presente (simulando token real),
                // aquí iría la lógica de validación de JWT real. Como es MOCK, lo ignoramos.
            }
        }

        // Continúa la cadena de filtros. Si SecurityContextHolder.getContext().authentication
        // no fue establecido arriba, la petición se denegará más adelante en las reglas de seguridad.
        filterChain.doFilter(request, response)
    }
}
