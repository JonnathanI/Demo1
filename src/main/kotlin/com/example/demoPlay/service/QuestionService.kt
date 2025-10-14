package com.example.demoPlay.service

import com.example.demoPlay.dto.QuestionCreationDTO
import com.example.demoPlay.dto.QuestionResponseDTO // <-- Importar el nuevo DTO
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

    /**
     * Obtiene un conjunto de preguntas al azar para el juego y las mapea a un DTO.
     * Esto asegura que el JSON enviado al Frontend tenga el formato correcto (incluyendo 'options' como List<String>).
     */
    // ✅ CORREGIDO: Devuelve QuestionResponseDTO
    @Transactional(readOnly = true)
    fun getGameQuestions(difficulty: String, count: Int = 10): List<QuestionResponseDTO> {
        val allQuestions = questionRepository.findByDifficultyLevel(difficulty)

        // Selecciona preguntas al azar, las toma según el límite y las mapea al DTO
        return allQuestions
            .shuffled(Random)
            .take(count)
            .map { QuestionResponseDTO.fromEntity(it) } // <-- Mapeo CRÍTICO para el Frontend
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
            category = dto.category,
            mediaUrl = dto.mediaUrl
        )

        // Crear y asignar ResponseOptions
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
        existingQuestion.mediaUrl = dto.mediaUrl

        // Reemplazar todas las opciones
        existingQuestion.responseOptions.clear()

        // Crear y agregar nuevas ResponseOptions
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