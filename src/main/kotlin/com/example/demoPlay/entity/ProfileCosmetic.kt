package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "profile_cosmetics")
class ProfileCosmetic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var name: String = ""
    var type: String = "" // 'Fondo', 'Marco', 'Avatar'
    var pointCost: Int = 0
    var resourceUrl: String = ""
}