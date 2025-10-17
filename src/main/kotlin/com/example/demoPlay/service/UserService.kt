package com.example.demoPlay.service

// 💡 IMPORTS NECESARIOS
import com.example.demoPlay.dto.* // Asume que incluye UserProfileDTO, UserGameStatDTO, etc.
import com.example.demoPlay.entity.User
import com.example.demoPlay.entity.UserPoints
import com.example.demoPlay.entity.PasswordResetToken
import com.example.demoPlay.repository.UserPointsRepository
import com.example.demoPlay.repository.UserRepository
import com.example.demoPlay.repository.PasswordResetTokenRepository
import com.example.demoPlay.repository.GameSessionRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import java.util.Locale // Necesario para formato si se usa, pero no en la corrección final

// IMPORTS PARA EL LOGIN CON GOOGLE
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import org.springframework.beans.factory.annotation.Value
// FIN IMPORTS AÑADIDOS

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPointsRepository: UserPointsRepository,
    private val tokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val gameSessionRepository: GameSessionRepository,
    // PROPIEDAD INYECTADA DESDE application.yml
    @Value("\${google.client.id}")
    private val googleClientId: String
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
    // --- RECUPERACIÓN DE CONTRASEÑA ---
    // ==========================================================

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    @Transactional
    fun forgotPassword(email: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("No existe un usuario registrado con el email: $email") }

        tokenRepository.deleteByUserId(user.id!!)

        val tokenString = generateToken()
        val expiryDate = LocalDateTime.now().plusHours(1)

        val tokenEntity = PasswordResetToken().apply {
            this.token = tokenString
            this.user = user
            this.expiryDate = expiryDate
        }
        tokenRepository.save(tokenEntity)

        emailService.sendPasswordResetEmail(user.email, tokenString)
    }

    @Transactional
    fun resetPassword(tokenString: String, newPassword: String) {
        val tokenEntity = tokenRepository.findByToken(tokenString)
            .orElseThrow { IllegalArgumentException("Token de restablecimiento inválido.") }

        if (tokenEntity.expiryDate.isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity)
            throw IllegalArgumentException("El enlace de restablecimiento ha expirado.")
        }

        val user = tokenEntity.user ?: throw NoSuchElementException("Usuario asociado al token no encontrado.")

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        tokenRepository.delete(tokenEntity)
    }


    // ==========================================================
    // --- REGISTRO, LOGIN Y GESTIÓN DE PERFIL/ADMIN ---
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

    // ==========================================================
    // --- LOGIN/REGISTRO CON GOOGLE (EXISTENTE) ---
    // ==========================================================

    @Transactional
    fun loginOrCreateUserByGoogleToken(token: String): UserLoginResponseDTO {
        // ... (código existente) ...
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(listOf(googleClientId))
            .build()

        val idToken = verifier.verify(token)
            ?: throw IllegalArgumentException("Token de Google inválido o expirado.")

        val payload = idToken.payload
        val email = payload.email
        val fullName = payload["name"] as String?
        val username = email.substringBefore('@')

        // 2. Buscar usuario por email
        val user = userRepository.findByEmail(email).orElseGet {
            // 3. Si no existe, registrar un nuevo usuario
            val newUser = User().apply {
                this.username = username
                this.email = email
                // Contraseña dummy, autenticación por Google
                this.passwordHash = passwordEncoder.encode(UUID.randomUUID().toString())
                this.fullName = fullName ?: username
                this.registrationDate = LocalDateTime.now()
                this.currentLevel = "Principiante"
                this.role = "USER"
            }
            val savedUser = userRepository.save(newUser)

            // Inicializar puntos
            val userPoints = UserPoints().apply {
                this.userId = savedUser.id
                this.user = savedUser
                this.totalPoints = 0
            }
            userPointsRepository.save(userPoints)
            savedUser
        }

        // 4. Generar respuesta (mock token)
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

    // ==========================================================
    // --- OBTENER PERFIL COMPLETO (FINAL Y CORREGIDO) ---
    // ==========================================================

    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserProfileDTO {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $userId") }

        val userPoints = userPointsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Puntos no encontrados para el usuario ID: $userId") }

        // 1. Obtener Historial de Juego
        val gameSessions = gameSessionRepository.findAllByUserId(userId).filter { it.answeredAt != null }

        // 2. Mapear y calcular estadísticas
        val historyDTOs = gameSessions.map { session ->
            UserGameStatDTO(
                // Se asume que Question tiene questionText.
                questionText = session.question?.questionText ?: session.gameType,
                isCorrect = session.isCorrect,
                pointsEarned = session.pointsEarned,
                answeredAt = session.answeredAt!!
            )
        }.sortedByDescending { it.answeredAt }

        val totalAnswered = historyDTOs.size
        val correctCount = historyDTOs.count { it.isCorrect }
        val correctPercent = if (totalAnswered > 0) (correctCount.toDouble() / totalAnswered) * 100 else 0.0

        // 3. Devolver el DTO consolidado
        return UserProfileDTO(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            currentLevel = user.currentLevel,
            totalPoints = userPoints.totalPoints,
            role = user.role,

            totalQuestionsAnswered = totalAnswered,
            correctAnswersCount = correctCount,
            // 🛑 CORRECCIÓN: Devolver el Double directamente para evitar el error "For input string: '0,00'".
            // La serialización a JSON usará el formato estándar (punto decimal).
            correctPercentage = correctPercent,

            gameHistory = historyDTOs
        )
    }

    // ==========================================================
    // --- GESTIÓN DE PERFIL/ADMIN (EXISTENTE) ---
    // ==========================================================

    @Transactional
    // Función para actualizar perfil de usuario (nombre y email)
    fun updateProfile(id: Long, newFullName: String?, newEmail: String?): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }

        newFullName?.let { user.fullName = it }
        newEmail?.let { user.email = it }

        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    // Función para listar todos los usuarios (Admin)
    fun findAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional
    // Función para actualizar rol y nivel (Admin)
    fun updateAdminUser(id: Long, newRole: String?, newLevel: String?): User {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuario no encontrado con ID: $id") }

        newRole?.let { user.role = it }
        newLevel?.let { user.currentLevel = it } // Se asume que newLevel aquí es String

        return userRepository.save(user)
    }

    @Transactional
    // Función para eliminar usuario y sus dependencias
    fun deleteUser(userId: Long) {
        // Eliminar dependencias (puntos, tokens, etc.)
        val userPoints = userPointsRepository.findByUserId(userId)
        if (userPoints.isPresent) {
            userPointsRepository.delete(userPoints.get())
        }
        tokenRepository.deleteByUserId(userId)

        // Eliminar usuario principal
        userRepository.deleteById(userId)
    }
}