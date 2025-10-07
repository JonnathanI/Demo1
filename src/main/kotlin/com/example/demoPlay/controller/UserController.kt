package com.example.demoPlay.controller

import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.dto.LoginRequestDTO
import com.example.demoPlay.dto.UserLoginResponseDTO // <-- Importación necesaria
import com.example.demoPlay.entity.User
import com.example.demoPlay.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.userdetails.UsernameNotFoundException

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    /**
     * Endpoint para registrar un nuevo usuario.
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<User> {
        val savedUser = userService.register(registrationDTO)
        return ResponseEntity(savedUser, HttpStatus.CREATED)
    }

    // ==========================================================
    // ENDPOINT DE LOGIN (CORREGIDO)
    // ==========================================================
    /**
     * Endpoint para iniciar sesión.
     * Recibe un DTO con credenciales y devuelve el DTO de respuesta completo (incluye el token).
     *
     * CORRECCIÓN CLAVE: Se pasa el DTO completo al servicio, resolviendo el error de "Too many arguments".
     * Se devuelve el DTO tipado UserLoginResponseDTO en lugar de un Map.
     */
    @PostMapping("/login")
    fun loginUser(@RequestBody loginDTO: LoginRequestDTO): ResponseEntity<UserLoginResponseDTO> {
        return try {
            // 1. LLAMADA CORREGIDA: Pasar el DTO completo al servicio
            val responseDTO = userService.login(loginDTO)

            // 2. Respuesta tipada
            ResponseEntity.ok(responseDTO)
        } catch (e: UsernameNotFoundException) {
            // Se puede devolver un DTO de error, pero por simplicidad usamos UNAUTHORIZED
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
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
