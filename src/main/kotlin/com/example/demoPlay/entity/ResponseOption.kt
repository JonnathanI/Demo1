package com.example.demoPlay.entity
// ... otras importaciones
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "response_options")
class ResponseOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var optionText: String = ""

    @Column(nullable = false)
    var isCorrect: Boolean = false

    // La relación @ManyToOne es la que causa el ciclo.
    // Añadimos @JsonIgnore para que no se serialice cuando se serializa una Question que contiene ResponseOption.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: Question? = null // Usamos nullable ya que en el service se asume que no es nulo, pero la definición de la clase base puede ser así.
}
