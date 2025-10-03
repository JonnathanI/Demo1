package com.example.demoPlay.controller

import com.example.demoPlay.dto.AnswerSubmissionDTO
import com.example.demoPlay.entity.GameSession
import com.example.demoPlay.entity.Question
import com.example.demoPlay.entity.ResponseLog
import com.example.demoPlay.service.GameService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/game")
class GameController(private val gameService: GameService) {

    /**
     * Inicia una nueva sesión de juego para un usuario.
     */
    @PostMapping("/start")
    fun startSession(
        @RequestParam userId: Long,
        @RequestParam difficulty: String, // Ej: "Normal"
        @RequestParam gameType: String // Ej: "Aprender" o "Prueba"
    ): ResponseEntity<GameSession> {
        val session = gameService.startSession(userId, difficulty, gameType)

        // El estado HTTP.CREATED (201) requiere el import de HttpStatus
        return ResponseEntity.status(HttpStatus.CREATED).body(session)
    }

    /**
     * Obtiene un conjunto de preguntas para el cliente.
     */
    @GetMapping("/questions")
    fun getQuestions(@RequestParam difficulty: String): ResponseEntity<List<Question>> {
        val questions = gameService.getQuestions(difficulty)
        return ResponseEntity.ok(questions) // Estado HTTP 200 (OK)
    }

    /**
     * Registra la respuesta del usuario y actualiza los puntos de forma atómica.
     */
    @PostMapping("/{sessionId}/answer/{questionId}")
    fun submitAnswer(
        @PathVariable sessionId: Long,
        @PathVariable questionId: Long,
        @RequestBody submission: AnswerSubmissionDTO
    ): ResponseEntity<ResponseLog> {

        val log = gameService.processAnswer(
            sessionId = sessionId,
            questionId = questionId,
            selectedOptionId = submission.selectedOptionId,
            responseTimeMs = submission.responseTimeMs,
            advantageUsed = submission.advantageUsed
        )
        return ResponseEntity.ok(log) // Estado HTTP 200 (OK)
    }

    /**
     * Finaliza la sesión de juego.
     */
    @PostMapping("/{sessionId}/finish")
    fun finishSession(@PathVariable sessionId: Long): ResponseEntity<GameSession> {
        val session = gameService.finishSession(sessionId)
        return ResponseEntity.ok(session) // Estado HTTP 200 (OK)
    }
}