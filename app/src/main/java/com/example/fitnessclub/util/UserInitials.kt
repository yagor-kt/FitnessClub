package com.example.fitnessclub.util

import androidx.compose.ui.graphics.Color

object UserInitials {
    private val palette = listOf(
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFF7C4DFF),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFF009688),
        Color(0xFF03A9F4),
        Color(0xFF8BC34A)
    )

    fun initials(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return words
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
    }

    fun colorFromEmail(email: String): Color {
        val index = Math.floorMod(email.lowercase().hashCode(), palette.size)
        return palette[index]
    }
}