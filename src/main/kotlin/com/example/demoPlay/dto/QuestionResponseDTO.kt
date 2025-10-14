package com.example.demoPlay.dto

import com.example.demoPlay.entity.Question
import com.example.demoPlay.entity.ResponseOption // Asegúrate de tener este import

data class QuestionResponseDTO(
    val id: Long,
    val questionText: String,
    val difficultyLevel: String,
    val pointsAwarded: Int,
    val category: String,
    val mediaUrl: String? = null,

    // ✅ CORRECCIÓN CLAVE: Usar el DTO que incluye el ID
    val options: List<ResponseOptionGameDTO>,

    val correctAnswer: String
) {
    companion object {
        fun fromEntity(question: Question): QuestionResponseDTO {
            val correctOption = question.responseOptions.find { it.isCorrect }
                ?: throw IllegalStateException("Pregunta ID ${question.id} no tiene una opción correcta definida.")

            return QuestionResponseDTO(
                id = question.id,
                questionText = question.questionText,
                difficultyLevel = question.difficultyLevel,
                pointsAwarded = question.pointsAwarded,
                category = question.category,
                mediaUrl = question.mediaUrl,

                // ✅ MAPEAMOS AL DTO CON ID
                options = question.responseOptions.map { optionEntity ->
                    ResponseOptionGameDTO(
                        id = optionEntity.id!!, // Asegúrate de que el ID de la entidad ResponseOption no es nulo
                        optionText = optionEntity.optionText
                    )
                },
                correctAnswer = correctOption.optionText
            )
        }
    }
}