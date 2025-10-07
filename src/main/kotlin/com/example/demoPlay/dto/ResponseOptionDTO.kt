package com.example.demoPlay.dto

data class ResponseOptionDTO(
    val optionText: String,
    val isCorrect: Boolean
    // No necesita el ID aquí si es solo para creación/edición
)
