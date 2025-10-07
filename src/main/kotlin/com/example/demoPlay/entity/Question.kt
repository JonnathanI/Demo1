package com.example.demoPlay.entity

import jakarta.persistence.*

@Entity
@Table(name = "questions")
class Question(
    // 1. Mover las propiedades al constructor primario
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var questionText: String = "",
    var difficultyLevel: String = "",
    var pointsAwarded: Int = 0,
    var category: String = "",
    // NUEVO CAMPO: URL para la imagen o el audio. Puede ser null.
    @Column(length = 1024, nullable = true)
var mediaUrl: String? = null
) {
    // 2. Usar 'responseOptions' y añadir 'orphanRemoval = true' para facilitar la edición (UPDATE)
    @OneToMany(
        mappedBy = "question",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true // Crucial para la función updateQuestion
    )
    var responseOptions: MutableList<ResponseOption> = mutableListOf() // ¡Nombre corregido a responseOptions!
}