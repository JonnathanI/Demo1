package com.example.demoPlay.service

import com.example.demoPlay.entity.User
import com.example.demoPlay.entity.UserPoints
import com.example.demoPlay.repository.UserPointsRepository
import com.example.demoPlay.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointsRepository: UserPointsRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun register(user: User): User {
        // 1. Verificar si el usuario o email ya existen
        if (userRepository.findByUsername(user.username).isPresent || userRepository.findByEmail(user.email).isPresent) {
            throw IllegalArgumentException("El nombre de usuario o email ya está en uso.")
        }

        // 2. Hashear la contraseña antes de guardar
        val hashedPassword = passwordEncoder.encode(user.passwordHash)
        user.passwordHash = hashedPassword // Usamos 'var' para actualizar

        // 3. Guardar el usuario
        val savedUser = userRepository.save(user)

        // 4. Inicializar los puntos a 0 (Transacción atómica)
        val userPoints = UserPoints().apply {
            this.userId = savedUser.id
            this.user = savedUser
            this.totalPoints = 0
        }
        userPointsRepository.save(userPoints)

        return savedUser
    }

    @Transactional
    fun updateProfile(id: Long, updatedData: User): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }

        // Actualizar solo los campos permitidos
        user.fullName = updatedData.fullName
        user.email = updatedData.email // Considerar la validación de unicidad si se cambia el email

        return userRepository.save(user)
    }

    fun getUserPoints(userId: Long): Int {
        return userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Puntos no encontrados para el usuario ID: $userId") }
            .totalPoints
    }
}