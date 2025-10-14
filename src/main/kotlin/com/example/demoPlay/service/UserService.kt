package com.example.demoPlay.service

import com.example.demoPlay.dto.LoginRequestDTO
import com.example.demoPlay.dto.UserLoginResponseDTO
import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.entity.UserPoints
import com.example.demoPlay.entity.PasswordResetToken // 💡 IMPORTANTE
import com.example.demoPlay.repository.UserPointsRepository
import com.example.demoPlay.repository.UserRepository
import com.example.demoPlay.repository.PasswordResetTokenRepository // 💡 IMPORTANTE
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointsRepository: UserPointsRepository,
    private val tokenRepository: PasswordResetTokenRepository, // 💡 INYECCIÓN NUEVA
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService // 💡 Asumiendo que está inyectado
) {

    private val ADMIN_SECRET_CODE = "SUPERCLAVE2025"
    private val HINT_COST = 50

    // ==========================================================
    // --- LÓGICA DE GESTIÓN DE PUNTOS ---
    // ==========================================================

    @Transactional
    fun purchaseHint(userId: Long): Int {
        val userPoints = userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("No se encontraron puntos para el usuario con ID: $userId") }

        val currentPoints = userPoints.totalPoints

        if (currentPoints < HINT_COST) {
            throw IllegalArgumentException("Puntos insuficientes. La pista cuesta $HINT_COST puntos.")
        }

        userPoints.totalPoints = currentPoints - HINT_COST
        userPointsRepository.save(userPoints)

        return userPoints.totalPoints
    }

    @Transactional
    fun addPoints(userId: Long, points: Int): Int {
        val userPoints = userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("No se encontraron puntos para el usuario con ID: $userId") }

        userPoints.totalPoints += points
        userPointsRepository.save(userPoints)

        return userPoints.totalPoints
    }

    fun getUserPoints(userId: Long): Int {
        return userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Puntos no encontrados para el usuario ID: $userId") }
            .totalPoints
    }

    // ==========================================================
    // --- RECUPERACIÓN DE CONTRASEÑA --- (NUEVAS FUNCIONES)
    // ==========================================================

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Busca el usuario, genera un token de recuperación y lo envía por email.
     */
    @Transactional
    fun forgotPassword(email: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("No existe un usuario registrado con el email: $email") }

        // Limpia cualquier token anterior para este usuario
        tokenRepository.deleteByUserId(user.id!!)

        // Crea el nuevo token (válido por 1 hora)
        val tokenString = generateToken()
        val expiryDate = LocalDateTime.now().plusHours(1)

        val tokenEntity = PasswordResetToken().apply {
            this.token = tokenString
            this.user = user
            this.expiryDate = expiryDate
        }
        tokenRepository.save(tokenEntity)

        // Envía el correo con el token
        emailService.sendPasswordResetEmail(user.email, tokenString)
    }

    /**
     * Valida el token, verifica la expiración y restablece la contraseña.
     */
    @Transactional
    fun resetPassword(tokenString: String, newPassword: String) {
        val tokenEntity = tokenRepository.findByToken(tokenString)
            .orElseThrow { IllegalArgumentException("Token de restablecimiento inválido.") }

        if (tokenEntity.expiryDate.isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity) // Limpieza
            throw IllegalArgumentException("El enlace de restablecimiento ha expirado.")
        }

        val user = tokenEntity.user ?: throw NoSuchElementException("Usuario asociado al token no encontrado.")

        // Actualiza y guarda la nueva contraseña encriptada
        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        // Elimina el token para evitar reuso
        tokenRepository.delete(tokenEntity)
    }


    // ==========================================================
    // --- REGISTRO, LOGIN Y GESTIÓN DE ADMINS ---
    // ==========================================================

    @Transactional
    fun register(dto: UserRegistrationDTO): User {
        val newUser = User().apply {
            username = dto.username
            email = dto.email
            passwordHash = passwordEncoder.encode(dto.password)
            fullName = dto.fullName
            registrationDate = LocalDateTime.now()
            currentLevel = "Principiante"
            role = if (dto.adminCode.equals(ADMIN_SECRET_CODE, ignoreCase = false)) "ADMIN" else "USER"
        }
        val savedUser = userRepository.save(newUser)

        val userPoints = UserPoints().apply {
            this.userId = savedUser.id
            this.user = savedUser
            this.totalPoints = 0
        }
        userPointsRepository.save(userPoints)

        return savedUser
    }

    @Transactional(readOnly = true)
    fun login(dto: LoginRequestDTO): UserLoginResponseDTO {
        val user = userRepository.findByUsername(dto.usernameOrEmail)
            .orElseGet {
                userRepository.findByEmail(dto.usernameOrEmail)
                    .orElseThrow { UsernameNotFoundException("Usuario o email incorrecto.") }
            }
        if (!passwordEncoder.matches(dto.password, user.passwordHash)) {
            throw IllegalArgumentException("Contraseña incorrecta.")
        }
        val mockToken = "MOCK_TOKEN_${user.id}_${user.role}"
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

    @Transactional
    fun updateProfile(id: Long, updatedData: User): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }
        user.fullName = updatedData.fullName
        user.email = updatedData.email
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun findAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional
    fun updateAdminUser(id: Long, newRole: String?, newLevel: String?): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }

        newRole?.let { user.role = it }
        newLevel?.let { user.currentLevel = it }

        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        // 1. Eliminar las dependencias del usuario (necesario para la clave foránea)

        // a) Eliminar los puntos del usuario (dependencia confirmada: user_points)
        val userPoints = userPointsRepository.findByUserId(userId)
        if (userPoints.isPresent) {
            userPointsRepository.delete(userPoints.get())
        }

        // b) Si tuvieras otras dependencias (ej: PasswordResetToken, GameSession), eliminarlas aquí.
        tokenRepository.deleteByUserId(userId) // Eliminar tokens de reseteo del usuario

        // 2. Eliminar el registro del usuario principal
        userRepository.deleteById(userId)
    }
}