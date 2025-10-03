package com.example.demoPlay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "advantage_purchases")
class AdvantagePurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User = User()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advantage_id")
    var advantage: Advantage = Advantage()

    var purchaseDate: LocalDateTime = LocalDateTime.now()
    var isUsed: Boolean = false
}