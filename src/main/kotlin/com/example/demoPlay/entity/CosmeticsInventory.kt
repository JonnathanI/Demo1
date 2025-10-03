package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cosmetics_inventory")
class CosmeticsInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User = User()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosmetic_id")
    var cosmetic: ProfileCosmetic = ProfileCosmetic()

    var acquisitionDate: LocalDateTime = LocalDateTime.now()
    var isActive: Boolean = false
}