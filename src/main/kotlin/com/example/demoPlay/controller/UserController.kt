package com.example.demoPlay.controller

import com.example.demoPlay.dto.* import com.example.demoPlay.entity.User
import com.example.demoPlay.service.UserService
import com.example.demoPlay.service.EmailService
import com.example.demoPlay.service.GameService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.Authentication // 💡 Añadido para el endpoint de puntos
import java.lang.IllegalArgumentException
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val emailService: EmailService,
    private val gameService: GameService
) {

    // ==========================================================
    // --- GESTIÓN DE USUARIOS (ADMIN) ---
    // ==========================================================

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.findAllUsers()
        return ResponseEntity.ok(users)
    }

    @PutMapping("/{userId}/admin-update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun adminUpdateUser(@PathVariable userId: Long, @RequestBody updatedData: UserUpdateDTO): ResponseEntity<User> {
        // Llama al servicio con los campos de rol y nivel del DTO
        val user = userService.updateAdminUser(userId, updatedData.role, updatedData.currentLevel)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }


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

    // 💡 NUEVO ENDPOINT: Login con Google
    @PostMapping("/login/google")
    fun loginWithGoogle(@RequestBody tokenRequest: Map<String, String>): ResponseEntity<UserLoginResponseDTO> {
        val googleToken = tokenRequest["token"]
            ?: return ResponseEntity.badRequest().build()

        return try {
            val responseDTO = userService.loginOrCreateUserByGoogleToken(googleToken)
            ResponseEntity.ok(responseDTO)
        } catch (e: IllegalArgumentException) {
            // Token inválido o expirado
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            // Otros errores del servidor
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ==========================================================
    // --- RECUPERACIÓN DE CONTRASEÑA ---
    // ==========================================================

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: Map<String, String>): ResponseEntity<*> {
        val email = request["email"]
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "El email es requerido."))

        return try {
            userService.forgotPassword(email)
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
    // --- OBTENER PERFIL COMPLETO ---
    // ==========================================================

    @GetMapping("/{userId}/profile/full")
    // 💡 Mejorada la comparación de IDs en el PreAuthorize para ser más robusta con el String del principal.
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN') and #userId.toString() == authentication.principal.toString()")
    fun getFullUserProfile(@PathVariable userId: Long): ResponseEntity<*> {
        return try {
            val profile = userService.getUserProfile(userId)
            ResponseEntity.ok(profile)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Error al obtener el perfil: ${e.message}"))
        }
    }

    // ==========================================================
    // --- OTROS ENDPOINTS (EXISTENTES) ---
    // ==========================================================

    @PostMapping("/{userId}/buy-hint")
    fun buyHint(@PathVariable userId: Long, @RequestBody request: Map<String, Long>): ResponseEntity<*> {
        val questionId = request["questionId"]
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "questionId es requerido."))

        return try {
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
    // Se utiliza un Map para la actualización de perfil de usuario normal (nombre/email)
    fun updateProfile(@PathVariable userId: Long, @RequestBody updatedData: Map<String, String>): ResponseEntity<User> {
        val newFullName = updatedData["fullName"]
        val newEmail = updatedData["email"]
        val user = userService.updateProfile(userId, newFullName, newEmail)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/points") // 🛑 CORRECCIÓN CLAVE: Eliminamos {userId} de la ruta.
    // Usamos PreAuthorize y obtenemos el ID del token autenticado.
    @PreAuthorize("isAuthenticated()")
    fun getUserPoints(authentication: Authentication): ResponseEntity<Int> {
        // Obtenemos el ID (que viene como String en el principal del token mock) y lo convertimos a Long.
        val userId = authentication.principal.toString().toLong()

        val points = userService.getUserPoints(userId)
        return ResponseEntity.ok(points)
    }
}
