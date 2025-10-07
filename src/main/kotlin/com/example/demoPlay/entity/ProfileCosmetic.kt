package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "profile_cosmetics")
class ProfileCosmetic(
    // Mover las propiedades al constructor primario para permitir la sintaxis de DTO
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0, // Debe ser 'var' para ser mutable

    var name: String = "",
    var type: String = "Avatar", // 'Fondo', 'Marco', 'Avatar'
    var pointCost: Int = 0,
    var resourceUrl: String = ""
)
// El cuerpo de la clase (las llaves) queda vacío o se puede omitir si no tiene lógica adicional