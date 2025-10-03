package com.example.demoPlay.repository

import com.example.demoPlay.entity.AdvantagePurchase
import org.springframework.data.jpa.repository.JpaRepository

interface AdvantagePurchaseRepository : JpaRepository<AdvantagePurchase, Long> {

    // Buscar todas las ventajas que el usuario ha comprado pero aún no ha usado
    fun findAllByUserIdAndIsUsed(userId: Long, isUsed: Boolean = false): List<AdvantagePurchase>
}