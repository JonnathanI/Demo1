package com.example.demoPlay.dto

data class CosmeticDTO(
    val name: String,
    val type: String, // Ej: "Frame", "Background"
    val pointCost: Int,
    val resourceUrl: String
)
