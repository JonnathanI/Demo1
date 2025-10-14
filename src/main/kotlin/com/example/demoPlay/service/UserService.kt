package com.example.demoPlay.service

import com.example.demoPlay.dto.LoginRequestDTO
import com.example.demoPlay.dto.UserLoginResponseDTO
import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.dto.HintResponseDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.entity.UserPoints
import com.example.demoPlay.repository.UserPointsRepository
import com.example.demoPlay.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointsRepository: UserPointsRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val ADMIN_SECRET_CODE = "SUPERCLAVE2025"
    private val HINT_COST = 50 // 💡 Costo de una pista en puntos

    // ==========================================================
    // FUNCIÓN DE REGISTRO
    // ==========================================================
    @Transactional
    fun register(dto: UserRegistrationDTO): User {
        // 1. Verificación de existencia (por username o email)
        if (userRepository.findByUsername(dto.username).isPresent || userRepository.findByEmail(dto.email).isPresent) {
            throw IllegalArgumentException("El nombre de usuario o email ya está en uso.")
        }

        // 2. Determinación del rol
        val roleToAssign = if (dto.adminCode.equals(ADMIN_SECRET_CODE, ignoreCase = false)) {
            "ADMIN"
        } else {
            "USER"
        }

        // 3. Crear la entidad User y hashear la contraseña
        val newUser = User().apply {
            username = dto.username
            email = dto.email
            passwordHash = passwordEncoder.encode(dto.password)
            fullName = dto.fullName
            registrationDate = LocalDateTime.now()
            currentLevel = "Principiante"
            role = roleToAssign
        }

        // 4. Guardar el usuario
        val savedUser = userRepository.save(newUser)

        // 5. Inicializar los puntos a 0
        val userPoints = UserPoints().apply {
            this.userId = savedUser.id
            this.user = savedUser
            this.totalPoints = 0
        }
        userPointsRepository.save(userPoints)

        return savedUser
    }

    // ==========================================================
    // FUNCIÓN DE LOGIN
    // ==========================================================
    @Transactional(readOnly = true)
    fun login(dto: LoginRequestDTO): UserLoginResponseDTO {
        // 1. Encontrar al usuario por nombre de usuario O email
        val user = userRepository.findByUsername(dto.usernameOrEmail)
            .orElseGet {
                userRepository.findByEmail(dto.usernameOrEmail)
                    .orElseThrow { UsernameNotFoundException("Usuario o email incorrecto.") }
            }

        // 2. Verificar la contraseña
        if (!passwordEncoder.matches(dto.password, user.passwordHash)) {
            throw IllegalArgumentException("Contraseña incorrecta.")
        }

        // 3. Generar el token MOCK
        val mockToken = "MOCK_TOKEN_${user.id}_${user.role}"

        // 4. Devolver el DTO de respuesta
        return UserLoginResponseDTO(
            id = user.id!!,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            role = user.role,
            currentLevel = user.currentLevel,
            registrationDate = user.registrationDate.toString(),
            token = mockToken
        )
    }

    // ==========================================================
    // 💡 FUNCIÓN PARA COMPRAR PISTAS (NUEVA)
    // ==========================================================
    @Transactional
    fun purchaseHint(userId: Long, hintText: String): HintResponseDTO {
        // 1. Obtener los puntos del usuario
        val userPoints = userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("No se encontraron puntos para el usuario con ID: $userId") }

        val currentPoints = userPoints.totalPoints

        // 2. Verificar si tiene suficientes puntos
        if (currentPoints < HINT_COST) {
            throw IllegalArgumentException("Puntos insuficientes. La pista cuesta $HINT_COST puntos.")
        }

        // 3. Descontar los puntos y guardar
        userPoints.totalPoints = currentPoints - HINT_COST
        userPointsRepository.save(userPoints)

        // 4. Devolver la respuesta
        return HintResponseDTO(
            hintText = hintText,
            newPoints = userPoints.totalPoints
        )
    }

    // ==========================================================
    // RESTO DE FUNCIONES
    // ==========================================================

    @Transactional
    fun updateProfile(id: Long, updatedData: User): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }

        user.fullName = updatedData.fullName
        user.email = updatedData.email

        return userRepository.save(user)
    }

    fun getUserPoints(userId: Long): Int {
        return userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Puntos no encontrados para el usuario ID: $userId") }
            .totalPoints
    }

    fun findAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun findUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $userId") }
    }

    @Transactional
    fun updateAdminUser(userId: Long, role: String, currentLevel: String): User {
        val user = findUserById(userId)

        if (role.isBlank()) {
            throw IllegalArgumentException("El rol no puede estar vacío.")
        }

        user.role = role
        user.currentLevel = currentLevel

        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        userRepository.deleteById(userId)
    }
}