package com.example.demoPlay.service

import com.example.demoPlay.entity.*
import com.example.demoPlay.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random
import org.slf4j.LoggerFactory // <-- NEW: Import for Logging

@Service
class GameService(
    private val userRepository: UserRepository,
    private val questionRepository: QuestionRepository,
    private val responseOptionRepository: ResponseOptionRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val responseLogRepository: ResponseLogRepository,
    private val userPointsRepository: UserPointsRepository
) {
    // <-- NEW: Initialize Logger
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun startSession(userId: Long, difficulty: String, gameType: String): GameSession {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("Usuario no encontrado.") }

        val session = GameSession().apply {
            this.user = user
            this.difficultyLevel = difficulty
            this.gameType = gameType
            this.startTime = LocalDateTime.now()
        }
        return gameSessionRepository.save(session)
    }

    // NOTE: This method should ideally be replaced by getGameQuestions (QuestionService)
    // to return the DTO, but we keep it here for service completeness.
    fun getQuestions(difficulty: String, count: Int = 10): List<Question> {
        val allQuestions = questionRepository.findByDifficultyLevel(difficulty)
        return allQuestions.shuffled(Random).take(count)
    }

    @Transactional
    fun processAnswer(sessionId: Long, questionId: Long, selectedOptionId: Long, responseTimeMs: Int, advantageUsed: Boolean): ResponseLog {
        val session = gameSessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Sesión de juego no encontrada.") }

        // 1. Verificar la respuesta y obtener puntos
        val selectedOption = responseOptionRepository.findById(selectedOptionId).orElseThrow { IllegalArgumentException("Opción no válida.") }
        val question = selectedOption.question!!

        if (question.id != questionId) {
            throw IllegalArgumentException("La opción seleccionada no pertenece a la pregunta ID: $questionId.")
        }

        val isCorrect = selectedOption.isCorrect
        val pointsGained = if (isCorrect) question.pointsAwarded else 0

        // <-- DIAGNOSTIC LOGGING
        logger.info("Processing answer for Session ID: {}, Question ID: {}. Correct: {}, Points Gained: {}",
            sessionId, questionId, isCorrect, pointsGained)

        // 2. Registrar el Log de la Respuesta
        val log = ResponseLog().apply {
            this.session = session
            this.question = question
            this.selectedOptionId = selectedOptionId
            this.isCorrect = isCorrect
            this.pointsGained = pointsGained
            this.responseTimeMs = responseTimeMs
            this.advantageUsed = advantageUsed
        }
        val savedLog = responseLogRepository.save(log)

        // 3. Actualizar puntos del usuario (Transacción atómica)
        if (pointsGained > 0) {
            val userPoints = userPointsRepository.findByUserId(session.user.id).orElseThrow()

            // <-- DIAGNOSTIC LOGGING: Check BEFORE update
            logger.info("UserPoints BEFORE update (User ID {}): TotalPoints={}, Version={}",
                session.user.id, userPoints.totalPoints, userPoints.version)

            // ✅ CRITICAL UPDATE
            userPoints.totalPoints += pointsGained

            userPointsRepository.save(userPoints)

            // <-- DIAGNOSTIC LOGGING: Check AFTER update/save
            logger.info("UserPoints AFTER update (User ID {}): New TotalPoints={}, New Version={}",
                session.user.id, userPoints.totalPoints, userPoints.version)
        }

        // 4. Actualizar total de puntos de la sesión
        session.pointsEarned += pointsGained
        gameSessionRepository.save(session)

        logger.info("Session Total Points updated to: {}", session.pointsEarned)

        return savedLog
    }

    @Transactional
    fun finishSession(sessionId: Long): GameSession {
        val session = gameSessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Sesión de juego no encontrada.") }
        session.endTime = LocalDateTime.now()

        return gameSessionRepository.save(session)
    }
}