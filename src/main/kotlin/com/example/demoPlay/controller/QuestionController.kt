package com.example.demoPlay.controller

import com.example.demoPlay.dto.QuestionCreationDTO
import com.example.demoPlay.dto.QuestionResponseDTO // <-- Importar el nuevo DTO
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
    // ==========================================================

    /**
     * POST: Crea una nueva pregunta. Solo accesible por 'ROLE_ADMIN'.
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
     * GET: Lista todas las preguntas (para el panel de administración). Solo accesible por 'ROLE_ADMIN'.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    fun getAllQuestions(): ResponseEntity<List<Question>> {
        val questions = questionService.findAllQuestions()
        return ResponseEntity.ok(questions)
    }

    /**
     * PUT: Actualiza una pregunta existente por ID. Solo accesible por 'ROLE_ADMIN'.
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
     * DELETE: Elimina una pregunta por ID. Solo accesible por 'ROLE_ADMIN'.
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
    // ENDPOINTS DE JUEGO
    // ==========================================================

    /**
     * GET: Obtiene un set de preguntas aleatorias para iniciar un juego.
     * Accesible por cualquier usuario autenticado.
     * URL: GET /api/questions/game?difficulty=Dificil&count=10
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/game")
    // ✅ CORREGIDO: Tipo de retorno List<QuestionResponseDTO>
    fun getGameQuestions(
        @RequestParam difficulty: String,
        @RequestParam(defaultValue = "10") count: Int
    ): ResponseEntity<List<QuestionResponseDTO>> {
        // Llama al servicio que devuelve el DTO mapeado correctamente.
        val questions = questionService.getGameQuestions(difficulty, count)
        return ResponseEntity.ok(questions)
    }
}