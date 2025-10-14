package com.example.demoPlay.repository

import com.example.demoPlay.entity.ResponseOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ResponseOptionRepository : JpaRepository<ResponseOption, Long> {

    // 💡 MÉTODO AÑADIDO: Necesario para que GameService.kt obtenga TODAS las opciones.
    fun findByQuestionId(questionId: Long): List<ResponseOption>

    // Útil para buscar la opción correcta
    fun findByQuestionIdAndIsCorrect(questionId: Long, isCorrect: Boolean = true): Optional<ResponseOption>
}