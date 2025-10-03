package com.example.demoPlay.repository

import com.example.demoPlay.entity.GameSession
import org.springframework.data.jpa.repository.JpaRepository

interface GameSessionRepository : JpaRepository<GameSession, Long> {
    // Ejemplo de una función que podrías añadir después:
    fun findAllByUserId(userId: Long): List<GameSession>
}