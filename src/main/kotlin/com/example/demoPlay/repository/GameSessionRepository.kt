package com.example.demoPlay.repository

import com.example.demoPlay.entity.GameSession
import org.springframework.data.jpa.repository.JpaRepository

interface GameSessionRepository : JpaRepository<GameSession, Long> {
    // 💡 Método necesario para buscar las sesiones por el ID del usuario
    fun findAllByUserId(userId: Long): List<GameSession>

    // Si tu entidad GameSession tiene un campo 'answeredAt' y quieres ordenar:
    fun findAllByUserIdOrderByAnsweredAtDesc(userId: Long): List<GameSession>
}