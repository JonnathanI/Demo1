package com.example.demoPlay.dto

data class UserRegistrationDTO(
    val username: String,
    val email: String,
    val password: String, // Contraseña en texto plano antes de ser hasheada en el Service
    val fullName: String? = null
)