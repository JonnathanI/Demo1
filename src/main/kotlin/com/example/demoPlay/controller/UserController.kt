package com.example.demoPlay.controller

import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.dto.LoginRequestDTO
import com.example.demoPlay.dto.UserLoginResponseDTO
import com.example.demoPlay.dto.HintResponseDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.service.UserService
import com.example.demoPlay.service.EmailService
import com.example.demoPlay.service.GameService // 💡 Necesario para buyHint
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.userdetails.UsernameNotFoundException

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    private val gameService: GameService // 💡 INYECCIÓN necesaria para la lógica de compra de pista
) {

    // ==========================================================
    // --- REGISTRO Y LOGIN ---
    // ==========================================================
    @PostMapping("/register")
    fun registerUser(@RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<User> {
        val savedUser = userService.register(registrationDTO)
        emailService.sendRegistrationConfirmation(
            toEmail = savedUser.email,
            username = savedUser.username
        )
        return ResponseEntity(savedUser, HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody loginDTO: LoginRequestDTO): ResponseEntity<UserLoginResponseDTO> {
        return try {
            val responseDTO = userService.login(loginDTO)
            ResponseEntity.ok(responseDTO)
        } catch (e: UsernameNotFoundException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    // ==========================================================
    // --- RECUPERACIÓN DE CONTRASEÑA --- (NUEVOS ENDPOINTS)
    // ==========================================================

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: Map<String, String>): ResponseEntity<*> {
        val email = request["email"]
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "El email es requerido."))

        return try {
            userService.forgotPassword(email)
            // Siempre se devuelve un mensaje genérico por seguridad, incluso si el email no existe.
            ResponseEntity.ok(mapOf("message" to "Si el email está registrado, se ha enviado un enlace para restablecer la contraseña."))
        } catch (e: NoSuchElementException) {
            ResponseEntity.ok(mapOf("message" to "Si el email está registrado, se ha enviado un enlace para restablecer la contraseña."))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Error interno del servidor."))
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: Map<String, String>): ResponseEntity<*> {
        val token = request["token"]
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "El token es requerido."))
        val newPassword = request["newPassword"]
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "La nueva contraseña es requerida."))

        return try {
            userService.resetPassword(token, newPassword)
            ResponseEntity.ok(mapOf("message" to "Contraseña restablecida exitosamente."))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Error interno del servidor al restablecer la contraseña."))
        }
    }

    // ==========================================================
    // --- OTROS ENDPOINTS ---
    // ==========================================================

    // 💡 ENDPOINT COMPRA PISTA (CORREGIDO para usar GameService)
    @PostMapping("/{userId}/buy-hint")
    fun buyHint(@PathVariable userId: Long, @RequestBody request: Map<String, Long>): ResponseEntity<*> {
        val questionId = request["questionId"]
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "questionId es requerido."))

        return try {
            // Llama a la función completa en GameService que gestiona la transacción y la pista
            val response: HintResponseDTO = gameService.purchaseAndGenerateHint(userId, questionId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Error interno del servidor al comprar la pista."))
        }
    }

    @PutMapping("/{userId}/profile")
    fun updateProfile(@PathVariable userId: Long, @RequestBody updatedData: User): ResponseEntity<User> {
        val user = userService.updateProfile(userId, updatedData)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/{userId}/points")
    fun getUserPoints(@PathVariable userId: Long): ResponseEntity<Int> {
        val points = userService.getUserPoints(userId)
        return ResponseEntity.ok(points)
    }
}