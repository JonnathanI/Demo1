package com.example.demoPlay.repository

import com.example.demoPlay.entity.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    // Para obtener preguntas por nivel de dificultad, útil al iniciar una sesión de juego
    fun findByDifficultyLevel(difficultyLevel: String): List<Question>
}