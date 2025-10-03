package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "response_options")
class ResponseOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question = Question()

    var optionText: String = ""
    var isCorrect: Boolean = false
}