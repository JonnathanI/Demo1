package com.example.demoPlay.controller

import com.example.demoPlay.dto.AnswerSubmissionDTO
import com.example.demoPlay.entity.GameSession
import com.example.demoPlay.entity.Question
import com.example.demoPlay.entity.ResponseLog
import com.example.demoPlay.service.GameService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize // 💡 Nueva importación necesaria

@RestController
@RequestMapping("/api/game")
class GameController(private val gameService: GameService) {

    /**
     * Inicia una nueva sesión de juego para un usuario.
     * Mapeo: POST /api/game/start?userId={id}&difficulty={level}&gameType={type}
     */
    @PostMapping("/start")
    // 🛑 CORRECCIÓN CLAVE: Permite iniciar el juego a usuarios o administradores.
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    fun startSession(
        @RequestParam userId: Long,
        @RequestParam difficulty: String, // Ej: "Normal"
        @RequestParam gameType: String // Ej: "Aprender" o "Prueba"
    ): ResponseEntity<GameSession> {
        val session = gameService.startSession(userId, difficulty, gameType)

        // Retorna 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(session)
    }

    /**
     * Obtiene un conjunto de preguntas para el cliente.
     * Mapeo: GET /api/game/questions?difficulty={level}
     */
    @GetMapping("/questions")
    // 💡 Asegura que el endpoint esté protegido.
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    fun getQuestions(@RequestParam difficulty: String): ResponseEntity<List<Question>> {
        val questions = gameService.getQuestions(difficulty)
        // Retorna 200 OK
        return ResponseEntity.ok(questions)
    }

    /**
     * Registra la respuesta del usuario y actualiza los puntos de forma atómica.
     * Mapeo: POST /api/game/{sessionId}/answer/{questionId}
     */
    @PostMapping("/{sessionId}/answer/{questionId}")
    // 💡 Asegura que el endpoint esté protegido.
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
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
        // Retorna 200 OK
        return ResponseEntity.ok(log)
    }

    /**
     * Finaliza la sesión de juego.
     * Mapeo: POST /api/game/{sessionId}/finish
     */
    @PostMapping("/{sessionId}/finish")
    // 💡 Asegura que el endpoint esté protegido.
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    fun finishSession(@PathVariable sessionId: Long): ResponseEntity<GameSession> {
        val session = gameService.finishSession(sessionId)
        // Retorna 200 OK
        return ResponseEntity.ok(session)
    }
}