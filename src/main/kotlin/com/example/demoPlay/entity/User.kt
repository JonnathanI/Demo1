package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var username: String = ""
    var email: String = ""
    var passwordHash: String = ""

    var fullName: String? = null
    var currentLevel: String = "Principiante"
    var registrationDate: LocalDateTime = LocalDateTime.now()
}