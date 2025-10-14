package com.example.demoPlay.repository

import com.example.demoPlay.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {

    fun findByToken(token: String): Optional<PasswordResetToken>

    // 🛑 CRÍTICO: Usar @Modifying y @Transactional para eliminar correctamente
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    fun deleteByUserId(userId: Long)

    // También puedes definirla solo por el nombre si Hibernate puede inferir el DELETE
    // Si la anterior con @Query falla, usa esta (pero la de arriba es más explícita):
    // fun deleteByUserId(userId: Long)
}