package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "response_log")
class ResponseLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    var session: GameSession = GameSession()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question = Question()

    @Column(name = "selected_option_id")
    var selectedOptionId: Long? = null

    var isCorrect: Boolean = false
    var pointsGained: Int = 0
    var responseTimeMs: Int? = null
    var advantageUsed: Boolean = false
}