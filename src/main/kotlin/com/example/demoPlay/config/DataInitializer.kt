package com.example.demoPlay.config

import com.example.demoPlay.entity.User
import com.example.demoPlay.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostConstruct
    fun initAdminUser() {
        // CAMBIADO: Usaremos 'sysadmin' para forzar la creación de un nuevo usuario limpio
        val adminUsername = "sysadmin"

        // 1. Verifica si el nuevo usuario administrador existe
        if (userRepository.findByUsername(adminUsername) == null) {
            println("Creando usuario SYS ADMIN inicial...")

            val adminUser = User().apply {
                username = adminUsername
                email = "sysadmin@juego.com" // Email del administrador
                passwordHash = passwordEncoder.encode("adminpass") // Contraseña correcta
                fullName = "System Administrator"
                role = "ADMIN" // Rol correcto para el frontend
            }

            userRepository.save(adminUser)
            println("Usuario SYS ADMIN creado con éxito. Username: $adminUsername")
        } else {
            println("El usuario SYS ADMIN ya existe.")
        }
    }
}
