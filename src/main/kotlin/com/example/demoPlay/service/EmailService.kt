package com.example.demoPlay.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.SimpleMailMessage // 💡 Necesario para crear el mensaje de texto
import java.lang.RuntimeException

@Service
class EmailService(
    // Inyecta el MailSender configurado por Spring Boot (usando application.properties)
    private val mailSender: JavaMailSender,
    // Obtiene el email del remitente configurado en application.properties
    @Value("\${spring.mail.username}") private val fromEmail: String
) {
    // 💡 La URL debe coincidir con la de tu frontend.
    private val FRONTEND_BASE_URL = "http://localhost:5173"

    // ==========================================================
    // --- ENVÍO DE CORREO DE REGISTRO ---
    // ==========================================================
    fun sendRegistrationConfirmation(toEmail: String, username: String) {
        val subject = "Bienvenido a English Game"
        val body = """
            Hola $username,
            
            ¡Gracias por registrarte en English Game! Ya puedes iniciar sesión y empezar a aprender.
            
            Saludos,
            El equipo de English Game
        """.trimIndent()

        sendEmail(toEmail, subject, body)
    }

    // ==========================================================
    // --- ENVÍO DE CORREO DE RESTABLECIMIENTO ---
    // ==========================================================
    fun sendPasswordResetEmail(toEmail: String, token: String) {
        val subject = "Recuperación de Contraseña para English Game"

        // Enlace que el usuario debe hacer clic para ir al formulario de nueva contraseña
        val resetUrl = "$FRONTEND_BASE_URL/reset-password?token=$token"

        val body = """
            Hola,
            
            Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.
            
            Para continuar, haz clic en el siguiente enlace. Este enlace caducará en 1 hora:
            $resetUrl
            
            Si no solicitaste este cambio, por favor ignora este correo.
            
            Saludos,
            El equipo de English Game
        """.trimIndent()

        sendEmail(toEmail, subject, body)
    }

    // ==========================================================
    // --- FUNCIÓN DE ENVÍO REAL (IMPLEMENTACIÓN CORREGIDA) ---
    // ==========================================================
    private fun sendEmail(toEmail: String, subject: String, body: String) {
        val message = SimpleMailMessage()

        message.setFrom(fromEmail)
        message.setTo(toEmail)
        message.setSubject(subject)
        message.setText(body)

        try {
            mailSender.send(message)
            println("=====================================================")
            println("EMAIL REAL ENVIADO EXITOSAMENTE a: $toEmail")
            println("SUBJECT: $subject")
            println("=====================================================")
        } catch (e: Exception) {
            // Manejo de errores de envío (ej: error de autenticación, conexión fallida)
            println("=====================================================")
            println("❌ ERROR AL ENVIAR CORREO a $toEmail. Causa: ${e.message}")
            println("=====================================================")
            // Opcional: Relanzar una excepción para que sea manejada por el controlador.
            throw RuntimeException("Fallo al enviar el correo a $toEmail.", e)
        }
    }
}