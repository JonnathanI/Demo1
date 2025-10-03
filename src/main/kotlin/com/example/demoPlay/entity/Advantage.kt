package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "advantages")
class Advantage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var name: String = ""
    var description: String = ""
    var pointCost: Int = 0
    var effect: String = "" // 'Extra_Time_10s', 'Hint_Remove_Option'
}