package com.example.demoPlay.dto

data class HintResponseDTO(
    val hintText: String,     // El contenido de la pista
    val newPoints: Int        // Los puntos restantes del usuario
)