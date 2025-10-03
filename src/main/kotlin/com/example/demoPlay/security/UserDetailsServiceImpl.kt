package com.example.demoPlay.security

import com.example.demoPlay.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $username") }

        // Mapea tu entidad User a la clase User de Spring Security
        // Usamos el username, el passwordHash (que ya está hasheado)
        // y una lista vacía de roles (asumiendo que no usas roles por ahora).
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.passwordHash, // Contraseña ya hasheada
            emptyList() // Colección de authorities/roles
        )
    }
}