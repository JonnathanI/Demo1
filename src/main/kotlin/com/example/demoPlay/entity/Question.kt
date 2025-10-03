package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "questions")
class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var questionText: String = ""
    var difficultyLevel: String = ""
    var pointsAwarded: Int = 0
    var category: String = ""

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var options: MutableList<ResponseOption> = mutableListOf()
}