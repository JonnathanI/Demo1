package com.example.demoPlay.service

import com.example.demoPlay.entity.*
import com.example.demoPlay.repository.*
import com.example.demoPlay.dto.HintResponseDTO // 💡 Necesario para la función purchaseAndGenerateHint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random
import org.slf4j.LoggerFactory

@Service
class GameService(
    private val userRepository: UserRepository,
    private val questionRepository: QuestionRepository,
    private val responseOptionRepository: ResponseOptionRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val responseLogRepository: ResponseLogRepository,
    private val userPointsRepository: UserPointsRepository,
    private val userService: UserService // 💡 Necesario para manejar la suma y resta de puntos
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    // El costo de la pista se define en UserService, pero se usa aquí como referencia si no se inyecta directamente
    private val HINT_COST = 50

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

        // 3. Actualizar puntos del usuario usando UserService
        if (pointsGained > 0) {
            // ✅ Usa la función de UserService para sumar puntos
            userService.addPoints(session.user.id!!, pointsGained)
        }

        // 4. Actualizar total de puntos de la sesión
        session.pointsEarned += pointsGained
        gameSessionRepository.save(session)

        logger.info("Session Total Points updated to: {}", session.pointsEarned)

        return savedLog
    }

    // ==========================================================
    // 🛑 FUNCIÓN AÑADIDA: COMPRA Y GENERACIÓN DE PISTA
    // ==========================================================

    /**
     * Procesa la compra de una pista, resta puntos (vía UserService) y genera el contenido dinámico.
     */
    @Transactional
    fun purchaseAndGenerateHint(userId: Long, questionId: Long): HintResponseDTO {
        // 1. Restar los puntos (Llama a UserService para la lógica de débito y validación)
        // Esto lanzará IllegalArgumentException si los puntos son insuficientes
        val newPoints = userService.purchaseHint(userId)

        // 2. Obtener la pregunta y las opciones
        val question = questionRepository.findById(questionId)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada con ID: $questionId") }

        // Asumiendo que existe findByQuestionId en ResponseOptionRepository
        val allOptions = responseOptionRepository.findByQuestionId(questionId)
        val correctOption = allOptions.firstOrNull { it.isCorrect }
            ?: throw NoSuchElementException("Opción correcta no encontrada para la pregunta ID: $questionId")

        // 3. Generar la pista (Lógica dinámica, simplificada y aleatoria)
        val correctText = correctOption.optionText.lowercase()
        val hintText = when (Random.nextInt(3)) {
            0 -> "La respuesta tiene **${correctText.length}** letras."
            1 -> "Comienza con la letra **'${correctText.first().uppercaseChar()}'**."
            else -> "Es la opción **${getOptionPosition(allOptions, correctOption)}**."
        }

        logger.info("Pista generada para user ${userId} (Costo: $HINT_COST): ${hintText}")

        // 4. Devolver la pista y el nuevo saldo
        return HintResponseDTO(
            hintText = hintText,
            newPoints = newPoints
        )
    }

    private fun getOptionPosition(options: List<ResponseOption>, correctOption: ResponseOption): String {
        // Asume que las opciones se cargan en el orden correcto de presentación.
        val index = options.indexOfFirst { it.id == correctOption.id }

        return when (index) {
            0 -> "superior (primera)."
            1 -> "central (segunda)."
            2 -> "inferior (tercera)."
            else -> "desconocida"
        }
    }

    @Transactional
    fun finishSession(sessionId: Long): GameSession {
        val session = gameSessionRepository.findById(sessionId).orElseThrow { NoSuchElementException("Sesión de juego no encontrada.") }
        session.endTime = LocalDateTime.now()

        return gameSessionRepository.save(session)
    }
}