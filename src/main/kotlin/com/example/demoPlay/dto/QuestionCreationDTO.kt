package com.example.demoPlay.dto

data class QuestionCreationDTO(
    val questionText: String,
    val mediaUrl: String?,
    val difficultyLevel: String,
    val pointsAwarded: Int,
    val category: String,
    // La lista de opciones usa el DTO que acabas de crear
    val options: List<ResponseOptionDTO>
)
