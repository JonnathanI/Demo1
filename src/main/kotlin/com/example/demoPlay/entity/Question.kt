package com.example.demoPlay.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "questions")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler") // 👈 CORRECCIÓN: Ignora el proxy de Hibernate
class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var questionText: String = "",
    var difficultyLevel: String = "",
    var pointsAwarded: Int = 0, // ✅ CRUCIAL para la lógica del puntaje
    var category: String = "",
    @Column(length = 1024, nullable = true)
    var mediaUrl: String? = null
) {
    @OneToMany(
        mappedBy = "question",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    // ⚠️ RECOMENDACIÓN: Añadir @JsonIgnore para prevenir ciclos infinitos si la pregunta se serializa a través de ResponseLog
    @JsonIgnoreProperties("question")
    var responseOptions: MutableList<ResponseOption> = mutableListOf()
}