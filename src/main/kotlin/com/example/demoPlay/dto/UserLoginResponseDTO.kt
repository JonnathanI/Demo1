package com.example.demoPlay.dto

/**
 * DTO utilizado para devolver los datos del usuario tras el login exitoso.
 * Incluye el token JWT (simulado) para que el frontend pueda guardarlo.
 */
data class UserLoginResponseDTO(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String, // 'ADMIN' o 'USER'
    val currentLevel: String,
    val registrationDate: String,
    val token: String // El token Mock
)
