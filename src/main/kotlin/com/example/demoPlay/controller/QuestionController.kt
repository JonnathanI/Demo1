package com.example.demoPlay.controller

import com.example.demoPlay.dto.QuestionCreationDTO
import com.example.demoPlay.entity.Question
import com.example.demoPlay.service.QuestionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")
class QuestionController(private val questionService: QuestionService) {

    // ==========================================================
    // ENDPOINTS DE ADMINISTRACIÓN (CRUD)
    // Protegidos con @PreAuthorize para el rol ROLE_ADMIN
    // ==========================================================

    /**
     * POST: Crea una nueva pregunta.
     * Solo accesible por usuarios con rol 'ROLE_ADMIN'.
     * URL: POST /api/questions
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    fun createQuestion(@RequestBody dto: QuestionCreationDTO): ResponseEntity<Question> {
        try {
            val newQuestion = questionService.createQuestion(dto)
            return ResponseEntity(newQuestion, HttpStatus.CREATED)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }

    /**
     * GET: Lista todas las preguntas (para el panel de administración).
     * Solo accesible por usuarios con rol 'ROLE_ADMIN'.
     * URL: GET /api/questions
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    fun getAllQuestions(): ResponseEntity<List<Question>> {
        val questions = questionService.findAllQuestions()
        return ResponseEntity.ok(questions)
    }

    /**
     * PUT: Actualiza una pregunta existente por ID.
     * Solo accesible por usuarios con rol 'ROLE_ADMIN'.
     * URL: PUT /api/questions/{id}
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    fun updateQuestion(@PathVariable id: Long, @RequestBody dto: QuestionCreationDTO): ResponseEntity<Question> {
        return try {
            val updatedQuestion = questionService.updateQuestion(id, dto)
            ResponseEntity.ok(updatedQuestion)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * DELETE: Elimina una pregunta por ID.
     * Solo accesible por usuarios con rol 'ROLE_ADMIN'.
     * URL: DELETE /api/questions/{id}
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            questionService.deleteQuestion(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    // ==========================================================
    // ENDPOINTS DE JUEGO (ACCESIBLES POR CUALQUIER USUARIO LOGUEADO)
    // ==========================================================

    /**
     * GET: Obtiene un set de preguntas aleatorias para iniciar un juego.
     * Accesible por cualquier usuario autenticado (rol USER o ADMIN).
     * URL: GET /api/questions/game?difficulty=Dificil&count=10
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/game")
    fun getGameQuestions(
        @RequestParam difficulty: String,
        @RequestParam(defaultValue = "10") count: Int
    ): ResponseEntity<List<Question>> {
        val questions = questionService.getQuestions(difficulty, count)
        return ResponseEntity.ok(questions)
    }
}