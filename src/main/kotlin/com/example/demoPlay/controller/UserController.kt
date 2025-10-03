package com.example.demoPlay.controller

import com.example.demoPlay.dto.UserRegistrationDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    /**
     * Endpoint para registrar un nuevo usuario.
     * Recibe un DTO y devuelve el usuario creado.
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody registrationDTO: UserRegistrationDTO): ResponseEntity<User> {
        // Mapear DTO a Entity (solo con los campos necesarios para el registro)
        val newUser = User().apply {
            this.username = registrationDTO.username
            this.email = registrationDTO.email
            this.passwordHash = registrationDTO.password // El servicio se encarga de hashearla
            this.fullName = registrationDTO.fullName
        }

        // El servicio maneja el hash de la contraseña y la creación de UserPoints
        val savedUser = userService.register(newUser)

        return ResponseEntity(savedUser, HttpStatus.CREATED)
    }

    /**
     * Endpoint para actualizar el perfil del usuario.
     * En una aplicación real, se usaría la autenticación JWT para obtener el ID del usuario,
     * no un @PathVariable.
     */
    @PutMapping("/{userId}/profile")
    fun updateProfile(@PathVariable userId: Long, @RequestBody updatedData: User): ResponseEntity<User> {
        // Solo enviamos al servicio los datos que pueden ser actualizados
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