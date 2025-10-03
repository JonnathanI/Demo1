package com.example.demoPlay.entity // <-- Mantenemos tu paquete original

import jakarta.persistence.*

/**
 * Entidad para almacenar los puntos de un usuario.
 * Nota: Implementa Bloqueo Optimista (@Version) para prevenir concurrencia.
 */
@Entity
@Table(name = "user_points")
class UserPoints {

    // Identificador y clave foránea de User
    @Id
    @Column(name = "user_id")
    var userId: Long? = null

    // Asumimos que User también está en com.example.demoPlay.entity
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Indica que la PK de esta tabla es la FK de la tabla User
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(name = "total_points", nullable = false)
    var totalPoints: Int = 0

    // *** SOLUCIÓN AL StaleObjectStateException ***
    // Se añade el campo @Version para que Hibernate gestione el bloqueo optimista.
    @Version
    @Column(name = "version_number")
    var version: Int? = null
}
