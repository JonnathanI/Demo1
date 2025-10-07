package com.example.demoPlay.controller

import com.example.demoPlay.dto.UserUpdateDTO
import com.example.demoPlay.entity.User
import com.example.demoPlay.service.QuestionService
import com.example.demoPlay.service.StoreService
import com.example.demoPlay.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val questionService: QuestionService,
    private val storeService: StoreService,
    private val userService: UserService // <--- Inyectar UserService
) {
    // ... (Endpoints de Preguntas y Tienda ya existentes)

    // ==========================================================
    // --- Endpoints de Gestión de Usuarios ---
    // ==========================================================

    // 1. LISTAR Usuarios (READ)
    @GetMapping("/users")
    fun listAllUsers(): ResponseEntity<List<User>> {
        val users = userService.findAllUsers()
        // IMPORTANTE: No devuelvas el 'passwordHash' en producción real
        return ResponseEntity.ok(users)
    }

    // 2. ACTUALIZAR Usuario (UPDATE)
    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody dto: UserUpdateDTO): ResponseEntity<User> {
        return try {
            val updatedUser = userService.updateAdminUser(id, dto.role, dto.currentLevel)
            ResponseEntity.ok(updatedUser)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(null)
        }
    }

    // 3. ELIMINAR Usuario (DELETE)
    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}