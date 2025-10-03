package com.example.demoPlay.repository

import com.example.demoPlay.entity.CosmeticsInventory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CosmeticsInventoryRepository : JpaRepository<CosmeticsInventory, Long> {

    // Buscar todos los cosméticos que posee un usuario
    fun findAllByUserId(userId: Long): List<CosmeticsInventory>

    // Encontrar el cosmético activo de un tipo específico (ej: el 'Fondo' activo)
    // Nota: 'Cosmetic_Type' usa el nombre de la propiedad en la Entidad ProfileCosmetic, conectada por la relación 'cosmetic'.
    fun findByUserIdAndCosmetic_TypeAndIsActive(userId: Long, type: String, isActive: Boolean = true): Optional<CosmeticsInventory>
}