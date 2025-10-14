package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // El token real que se envía por correo (un UUID o código aleatorio)
    @Column(nullable = false, unique = true)
    var token: String = ""

    // Enlace al usuario que solicitó el reseteo
    @OneToOne(targetEntity = User::class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    var expiryDate: LocalDateTime = LocalDateTime.now()
}