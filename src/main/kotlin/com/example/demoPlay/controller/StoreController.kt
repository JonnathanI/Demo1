package com.example.demoPlay.controller

import com.example.demoPlay.entity.CosmeticsInventory
import com.example.demoPlay.entity.AdvantagePurchase
import com.example.demoPlay.service.StoreService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/store")
class StoreController(private val storeService: StoreService) {

    /**
     * Endpoint para comprar un cosmético.
     */
    @PostMapping("/buy/cosmetic")
    fun buyCosmetic(
        @RequestParam userId: Long,
        @RequestParam cosmeticId: Long
    ): ResponseEntity<CosmeticsInventory> {
        val item = storeService.buyCosmetic(userId, cosmeticId)
        return ResponseEntity.status(HttpStatus.CREATED).body(item)
    }

    /**
     * Endpoint para activar un cosmético del inventario del usuario.
     * Maneja la lógica de desactivar el ítem previo del mismo tipo.
     */
    @PutMapping("/activate/cosmetic/{inventoryItemId}")
    fun activateCosmetic(
        @PathVariable inventoryItemId: Long,
        @RequestParam userId: Long // Se usa para la validación de propiedad
    ): ResponseEntity<CosmeticsInventory> {
        val item = storeService.activateCosmetic(userId, inventoryItemId)
        return ResponseEntity.ok(item)
    }

    /**
     * Endpoint para comprar una ventaja (ej: tiempo extra, pista).
     */
    @PostMapping("/buy/advantage")
    fun buyAdvantage(
        @RequestParam userId: Long,
        @RequestParam advantageId: Long
    ): ResponseEntity<AdvantagePurchase> {
        val purchase = storeService.buyAdvantage(userId, advantageId)
        return ResponseEntity.status(HttpStatus.CREATED).body(purchase)
    }
}