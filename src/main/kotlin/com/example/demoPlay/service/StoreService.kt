package com.example.demoPlay.service

import com.example.demoPlay.entity.*
import com.example.demoPlay.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoreService(
    private val userRepository: UserRepository,
    private val userPointsRepository: UserPointsRepository,
    private val cosmeticRepository: ProfileCosmeticRepository,
    private val inventoryRepository: CosmeticsInventoryRepository,
    private val advantageRepository: AdvantageRepository,
    private val purchaseRepository: AdvantagePurchaseRepository
) {
    @Transactional(rollbackFor = [IllegalArgumentException::class]) // Si hay fallo en la lógica de puntos, revierte
    fun buyCosmetic(userId: Long, cosmeticId: Long): CosmeticsInventory {
        val cosmetic = cosmeticRepository.findById(cosmeticId).orElseThrow { NoSuchElementException("Cosmético no encontrado.") }
        val userPoints = userPointsRepository.findByUserId(userId).orElseThrow { NoSuchElementException("Usuario no encontrado.") }
        val user = userRepository.findById(userId).orElseThrow()

        if (userPoints.totalPoints < cosmetic.pointCost) {
            throw IllegalArgumentException("Puntos insuficientes. Costo: ${cosmetic.pointCost}, Saldo: ${userPoints.totalPoints}")
        }

        // 1. Restar puntos (CRUCIAL)
        userPoints.totalPoints -= cosmetic.pointCost
        userPointsRepository.save(userPoints)

        // 2. Añadir al inventario
        val newInventoryItem = CosmeticsInventory().apply {
            this.user = user
            this.cosmetic = cosmetic
            this.isActive = false // El usuario lo activará después
        }
        return inventoryRepository.save(newInventoryItem)
    }

    @Transactional
    fun activateCosmetic(userId: Long, inventoryItemId: Long): CosmeticsInventory {
        val itemToActivate = inventoryRepository.findById(inventoryItemId)
            .orElseThrow { NoSuchElementException("Ítem de inventario no encontrado.") }

        // 1. Validación de propiedad
        if (itemToActivate.user.id != userId) {
            throw SecurityException("Acceso denegado.")
        }

        val cosmeticType = itemToActivate.cosmetic.type

        // 2. Desactivar ítems ACTIVO previos del mismo tipo (ej: desactiva el Marco anterior)
        inventoryRepository
            .findByUserIdAndCosmetic_TypeAndIsActive(userId, cosmeticType, true)
            .ifPresent { currentActiveItem ->
                if (currentActiveItem.id != itemToActivate.id) {
                    currentActiveItem.isActive = false
                    inventoryRepository.save(currentActiveItem)
                }
            }

        // 3. Activar el ítem solicitado
        itemToActivate.isActive = true
        return inventoryRepository.save(itemToActivate)
    }

    @Transactional(rollbackFor = [IllegalArgumentException::class])
    fun buyAdvantage(userId: Long, advantageId: Long): AdvantagePurchase {
        val advantage = advantageRepository.findById(advantageId).orElseThrow { NoSuchElementException("Ventaja no encontrada.") }
        val userPoints = userPointsRepository.findByUserId(userId).orElseThrow { NoSuchElementException("Usuario no encontrado.") }
        val user = userRepository.findById(userId).orElseThrow()

        if (userPoints.totalPoints < advantage.pointCost) {
            throw IllegalArgumentException("Puntos insuficientes.")
        }

        // 1. Restar puntos
        userPoints.totalPoints -= advantage.pointCost
        userPointsRepository.save(userPoints)

        // 2. Registrar la compra
        val purchase = AdvantagePurchase().apply {
            this.user = user
            this.advantage = advantage
            this.isUsed = false
        }
        return purchaseRepository.save(purchase)
    }

    // Método para marcar una ventaja como usada (llamado desde GameService o Controller)
    @Transactional
    fun markAdvantageAsUsed(purchaseId: Long): AdvantagePurchase {
        val purchase = purchaseRepository.findById(purchaseId).orElseThrow { NoSuchElementException("Compra de ventaja no encontrada.") }
        purchase.isUsed = true
        return purchaseRepository.save(purchase)
    }
}