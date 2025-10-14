package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonIgnoreProperties // 👈 Importación requerida

@Entity
@Table(name = "users")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler") // 👈 Corrección aplicada aquí
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

    @Column(nullable = false)
    var role: String = "ADMIN"
}