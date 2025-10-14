package com.example.demoPlay.controller

import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.dto.LoginRequestDTO
import com.example.demoPlay.dto.UserLoginResponseDTO
import com.example.demoPlay.dto.HintResponseDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.service.UserService
import com.example.demoPlay.service.EmailService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.userdetails.UsernameNotFoundException

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService
) {

    /**
     * Endpoint para registrar un nuevo usuario y enviar un correo de confirmación.
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<User> {
        val savedUser = userService.register(registrationDTO)

        // ENVÍO DE CORREO
        emailService.sendRegistrationConfirmation(
            toEmail = savedUser.email,
            username = savedUser.username
        )

        return ResponseEntity(savedUser, HttpStatus.CREATED)
    }

    // ==========================================================
    // ENDPOINT DE LOGIN
    // ==========================================================
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
    // 💡 ENDPOINT PARA COMPRAR PISTA (NUEVO)
    // ==========================================================
    /**
     * Endpoint para que un usuario compre una pista usando sus puntos.
     */
    @PostMapping("/{userId}/buy-hint")
    fun buyHint(@PathVariable userId: Long, @RequestBody request: Map<String, Long>): ResponseEntity<*> {

        val questionId = request["questionId"]

        if (questionId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "questionId es requerido."))
        }

        // 🚨 Pista Hardcodeada: Se usaría un servicio de juego para esto
        val generatedHint = "La respuesta es la opción del centro. (Pista para pregunta $questionId)"

        return try {
            // Ejecutar la transacción de compra en el servicio
            val response: HintResponseDTO = userService.purchaseHint(userId, generatedHint)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            // Puntos insuficientes (BAD REQUEST)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            // Usuario/Puntos no encontrados (NOT FOUND)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            // Error interno del servidor
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Error interno del servidor al comprar la pista."))
        }
    }

    // ==========================================================
    // RESTO DE ENDPOINTS
    // ==========================================================

    /**
     * Endpoint para actualizar el perfil del usuario.
     */
    @PutMapping("/{userId}/profile")
    fun updateProfile(@PathVariable userId: Long, @RequestBody updatedData: User): ResponseEntity<User> {
        val user = userService.updateProfile(userId, updatedData)
        return ResponseEntity.ok(user)
    }

    /**
     * Endpoint para obtener el saldo de puntos de un usuario.
     */
    @GetMapping("/{userId}/points")
    fun getUserPoints(@PathVariable userId: Long): ResponseEntity<Int> {
        val points = userService.getUserPoints(userId)
        return ResponseEntity.ok(points)
    }
}