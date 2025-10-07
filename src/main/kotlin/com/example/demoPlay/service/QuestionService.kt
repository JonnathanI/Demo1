package com.example.demoPlay.service

import com.example.demoPlay.dto.QuestionCreationDTO
import com.example.demoPlay.entity.Question
import com.example.demoPlay.entity.ResponseOption
import com.example.demoPlay.repository.QuestionRepository
import com.example.demoPlay.repository.ResponseOptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val responseOptionRepository: ResponseOptionRepository
) {
    // ==========================================================
    // --- LÓGICA DEL JUEGO ---
    // ==========================================================

    fun getQuestions(difficulty: String, count: Int = 10): List<Question> {
        val allQuestions = questionRepository.findByDifficultyLevel(difficulty)
        return allQuestions.shuffled(Random).take(count)
    }

    // ==========================================================
    // --- LÓGICA DE ADMINISTRACIÓN (CRUD) ---
    // ==========================================================

    // 1. CREAR una nueva pregunta (usando el DTO)
    @Transactional
    fun createQuestion(dto: QuestionCreationDTO): Question {
        // Validar que al menos una opción sea correcta
        if (!dto.options.any { it.isCorrect }) {
            throw IllegalArgumentException("Debe haber al menos una opción de respuesta correcta.")
        }

        // Crear la entidad Question
        val question = Question(
            questionText = dto.questionText,
            difficultyLevel = dto.difficultyLevel,
            pointsAwarded = dto.pointsAwarded,
            category = dto.category
        )

        // CORRECCIÓN: Usar ResponseOption() y el bloque apply {}
        val options = dto.options.map { optionDto ->
            ResponseOption().apply {
                this.optionText = optionDto.optionText
                this.isCorrect = optionDto.isCorrect
                this.question = question // Asignar la referencia a la Question
            }
        }.toMutableList()

        question.responseOptions = options
        return questionRepository.save(question)
    }

    // 2. LISTAR todas las preguntas
    fun findAllQuestions(): List<Question> {
        return questionRepository.findAll()
    }

    // 3. ACTUALIZAR una pregunta existente
    @Transactional
    fun updateQuestion(id: Long, dto: QuestionCreationDTO): Question {
        val existingQuestion = questionRepository.findById(id)
            .orElseThrow { NoSuchElementException("Pregunta no encontrada con ID: $id") }

        // Actualizar campos principales
        existingQuestion.questionText = dto.questionText
        existingQuestion.difficultyLevel = dto.difficultyLevel
        existingQuestion.pointsAwarded = dto.pointsAwarded
        existingQuestion.category = dto.category

        // Reemplazar todas las opciones
        existingQuestion.responseOptions.clear()

        // CORRECCIÓN: Usar ResponseOption() y el bloque apply {}
        val newOptions = dto.options.map { optionDto ->
            ResponseOption().apply {
                this.optionText = optionDto.optionText
                this.isCorrect = optionDto.isCorrect
                this.question = existingQuestion // Asignar la referencia a la Question existente
            }
        }.toMutableList()

        existingQuestion.responseOptions.addAll(newOptions)

        return questionRepository.save(existingQuestion)
    }

    // 4. ELIMINAR una pregunta
    fun deleteQuestion(id: Long) {
        questionRepository.deleteById(id)
    }
}
