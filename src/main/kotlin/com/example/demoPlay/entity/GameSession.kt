package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_sessions")
class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User = User()

    // 💡 NUEVA RELACIÓN CON LA PREGUNTA
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = true)
    var question: Question? = null

    // 🛑 CORRECCIÓN 1: Soluciona el ERROR de 'is_correct' NOT NULL.
    // Usamos 'columnDefinition' para añadir un valor por defecto (false) a las filas existentes
    // si usas la migración automática de Hibernate.
    @Column(name = "is_correct", nullable = false, columnDefinition = "boolean default false")
    var isCorrect: Boolean = false

    // CAMPO DE PUNTOS GANADOS
    var pointsEarned: Int = 0

    // Campos originales:
    var gameType: String = ""
    var difficultyLevel: String = ""
    var startTime: LocalDateTime = LocalDateTime.now()

    // 🛑 CORRECCIÓN 2: Soluciona el ERROR de 'No property 'answeredAt' found'.
    // Tu repositorio GameSessionRepository usa 'answeredAt' para ordenar el historial.
    // Cambiamos 'endTime' a 'answeredAt' para coincidir con el repositorio,
    // ya que este campo registra el final de la sesión o el momento de la respuesta.
    @Column(name = "answered_at") // Mapeamos el nombre de la columna en DB
    var answeredAt: LocalDateTime? = null
}