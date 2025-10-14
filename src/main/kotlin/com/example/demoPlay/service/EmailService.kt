package com.example.demoPlay.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    /**
     * Envía un correo de confirmación de registro al usuario.
     * * @param toEmail La dirección de correo del destinatario.
     * @param username El nombre de usuario que se registró.
     */
    fun sendRegistrationConfirmation(toEmail: String, username: String) {
        val message = SimpleMailMessage()

        // 💡 Configura el correo de origen (debe ser el mismo que configuraste en application.yml)
        message.setFrom("tu_correo_de_envio@gmail.com")
        message.setTo(toEmail)
        message.setSubject("¡Bienvenido a English Game! Registro Exitoso 🚀")

        // Contenido del Correo
        val content = """
            ¡Hola $username!
            
            Confirmamos que te has registrado exitosamente a nuestra aplicación English Game con el usuario: $username.
            
            Tu cuenta está lista para que comiences a jugar, aprender inglés y subir de nivel.
            
            ¡Gracias por unirte a la comunidad!
            
            El equipo de English Game.
        """.trimIndent()

        message.setText(content)

        try {
            mailSender.send(message)
            println("Correo de confirmación de registro enviado a: $toEmail")
        } catch (e: Exception) {
            // Es importante capturar errores aquí para que un fallo en el envío del correo no detenga el registro del usuario.
            System.err.println("Error al enviar el correo de registro a $toEmail: ${e.message}")
        }
    }
}
