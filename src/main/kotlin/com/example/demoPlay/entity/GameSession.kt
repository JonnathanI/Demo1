package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "game_sessions")
class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User = User()

    var gameType: String = ""
    var difficultyLevel: String = ""
    var pointsEarned: Int = 0
    var startTime: LocalDateTime = LocalDateTime.now()
    var endTime: LocalDateTime? = null
}