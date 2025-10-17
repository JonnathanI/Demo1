package com.example.demoPlay.dto

import java.time.LocalDateTime

// DTO para representar una sola respuesta/sesión de juego
data class UserGameStatDTO(
    val questionText: String,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val answeredAt: LocalDateTime
)

// DTO principal para la vista del perfil
data class UserProfileDTO(
    // Datos del Usuario
    val userId: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val currentLevel: String,
    val totalPoints: Int,
    val role: String,

    // Estadísticas de Juego
    val totalQuestionsAnswered: Int,
    val correctAnswersCount: Int,
    val correctPercentage: Double,

    // Historial detallado
    val gameHistory: List<UserGameStatDTO>
)