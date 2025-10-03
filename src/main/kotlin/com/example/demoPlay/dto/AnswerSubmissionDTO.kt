package com.example.demoPlay.dto

data class AnswerSubmissionDTO(
    val selectedOptionId: Long,
    val responseTimeMs: Int,
    val advantageUsed: Boolean
)
