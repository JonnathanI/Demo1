package com.example.demoPlay.repository

import com.example.demoPlay.entity.ResponseLog
import org.springframework.data.jpa.repository.JpaRepository

interface ResponseLogRepository : JpaRepository<ResponseLog, Long> {
    // Ejemplo de una función que podrías añadir después:
    fun findAllBySessionId(sessionId: Long): List<ResponseLog>
}