package com.example.demoPlay.service

import com.example.demoPlay.entity.*
import com.example.demoPlay.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class GameService(
    private val userRepository: UserRepository,
    private val questionRepository: QuestionRepository,
    private val responseOptionRepository: ResponseOptionRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val responseLogRepository: ResponseLogRepository,
    private val userPointsRepository: UserPointsRepository
) {
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

    fun getQuestions(difficulty: String, count: Int = 10): List<Question> {
        val allQuestions = questionRepository.findByDifficultyLevel(difficulty)

        // Selecciona preguntas al azar (la función 'shuffled()' es propia de Kotlin)
        return allQuestions.shuffled(Random).take(count)
    }

    @Transactional
    fun processAnswer(sessionId: Long, questionId: Long, selectedOptionId: Long, responseTimeMs: Int, advantageUsed: Boolean): ResponseLog {
        val session = gameSessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Sesión de juego no encontrada.") }

        // 1. Verificar la respuesta y obtener puntos
        val selectedOption = responseOptionRepository.findById(selectedOptionId).orElseThrow { IllegalArgumentException("Opción no válida.") }

        // Accedemos a la pregunta no nula de la opción seleccionada.
        // Asumimos que la relación Question en ResponseOption es @ManyToOne y no debe ser nula.
        val question = selectedOption.question!!

        // Verificación de seguridad: la opción debe pertenecer a la pregunta
        // CORRECCIÓN 1: Usar 'question.id' en lugar de 'selectedOption.question.id'
        if (question.id != questionId) {
            throw IllegalArgumentException("La opción seleccionada no pertenece a la pregunta ID: $questionId.")
        }

        val isCorrect = selectedOption.isCorrect
        // CORRECCIÓN 2: Usar 'question.pointsAwarded'
        val pointsGained = if (isCorrect) question.pointsAwarded else 0

        // 2. Registrar el Log de la Respuesta
        val log = ResponseLog().apply {
            this.session = session
            // CORRECCIÓN 3: Pasar el objeto Question no nulo
            this.question = question
            this.selectedOptionId = selectedOptionId
            this.isCorrect = isCorrect
            this.pointsGained = pointsGained
            this.responseTimeMs = responseTimeMs
            this.advantageUsed = advantageUsed
        }
        val savedLog = responseLogRepository.save(log)

        // 3. Actualizar puntos del usuario (Transacción atómica)
        // Se usa orElseThrow() sin argumentos, lo cual es válido si el resultado es conocido (el usuario siempre debe tener un registro de puntos)
        val userPoints = userPointsRepository.findByUserId(session.user.id).orElseThrow()
        userPoints.totalPoints += pointsGained
        userPointsRepository.save(userPoints)

        // 4. Actualizar total de puntos de la sesión
        session.pointsEarned += pointsGained
        gameSessionRepository.save(session)

        return savedLog
    }

    @Transactional
    fun finishSession(sessionId: Long): GameSession {
        val session = gameSessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Sesión de juego no encontrada.") }
        session.endTime = LocalDateTime.now()

        // Se asume que los puntos ya fueron actualizados por 'processAnswer'
        return gameSessionRepository.save(session)
    }
}
