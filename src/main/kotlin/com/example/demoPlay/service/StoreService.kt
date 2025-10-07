package com.example.demoPlay.service

import com.example.demoPlay.dto.AdvantageDTO
import com.example.demoPlay.dto.CosmeticDTO
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
    // ==========================================================
    // --- LÓGICA DE TIENDA PARA USUARIOS (YA EXISTENTE) ---
    // ==========================================================

    @Transactional(rollbackFor = [IllegalArgumentException::class])
    fun buyCosmetic(userId: Long, cosmeticId: Long): CosmeticsInventory {
        val cosmetic = cosmeticRepository.findById(cosmeticId).orElseThrow { NoSuchElementException("Cosmético no encontrado.") }
        val userPoints = userPointsRepository.findByUserId(userId).orElseThrow { NoSuchElementException("Usuario no encontrado.") }
        val user = userRepository.findById(userId).orElseThrow()

        if (userPoints.totalPoints < cosmetic.pointCost) {
            throw IllegalArgumentException("Puntos insuficientes. Costo: ${cosmetic.pointCost}, Saldo: ${userPoints.totalPoints}")
        }

        // 1. Restar puntos
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

    @Transactional
    fun markAdvantageAsUsed(purchaseId: Long): AdvantagePurchase {
        val purchase = purchaseRepository.findById(purchaseId).orElseThrow { NoSuchElementException("Compra de ventaja no encontrada.") }
        purchase.isUsed = true
        return purchaseRepository.save(purchase)
    }


    // ==========================================================
    // --- LÓGICA DE ADMINISTRACIÓN (CRUD) AÑADIDA ---
    // ==========================================================

    // --- Ventajas (Advantage) CRUD ---

    fun createAdvantage(dto: AdvantageDTO): Advantage {
        val advantage = Advantage(
            name = dto.name,
            description = dto.description,
            pointCost = dto.pointCost,
            effect = dto.effect
        )
        return advantageRepository.save(advantage)
    }

    fun findAllAdvantages(): List<Advantage> = advantageRepository.findAll()

    fun updateAdvantage(id: Long, dto: AdvantageDTO): Advantage {
        val existing = advantageRepository.findById(id).orElseThrow { NoSuchElementException("Ventaja no encontrada con ID: $id") }
        existing.name = dto.name
        existing.description = dto.description
        existing.pointCost = dto.pointCost
        existing.effect = dto.effect
        return advantageRepository.save(existing)
    }

    fun deleteAdvantage(id: Long) {
        // Podrías añadir lógica de validación aquí si fuera necesario (ej: no eliminar si ya fue comprada)
        advantageRepository.deleteById(id)
    }

    // --- Cosméticos (ProfileCosmetic) CRUD ---

    fun createCosmetic(dto: CosmeticDTO): ProfileCosmetic {
        val cosmetic = ProfileCosmetic(
            name = dto.name,
            type = dto.type,
            pointCost = dto.pointCost,
            resourceUrl = dto.resourceUrl
        )
        return cosmeticRepository.save(cosmetic)
    }

    fun findAllCosmetics(): List<ProfileCosmetic> = cosmeticRepository.findAll()

    fun updateCosmetic(id: Long, dto: CosmeticDTO): ProfileCosmetic {
        val existing = cosmeticRepository.findById(id).orElseThrow { NoSuchElementException("Cosmético no encontrado con ID: $id") }
        existing.name = dto.name
        existing.type = dto.type
        existing.pointCost = dto.pointCost
        existing.resourceUrl = dto.resourceUrl
        return cosmeticRepository.save(existing)
    }

    fun deleteCosmetic(id: Long) {
        // Podrías añadir lógica de validación aquí (ej: si está activo en el inventario de alguien)
        cosmeticRepository.deleteById(id)
    }
}