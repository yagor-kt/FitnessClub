package com.example.fitnessclub.util

import androidx.compose.ui.graphics.Color

object WorkoutType {
    fun colorForType(type: String): Color =
        when (type.trim().lowercase()) {
            "йога" -> Color(0xFF4CAF50)
            "кроссфит" -> Color(0xFFFF9800)
            "пилатес" -> Color(0xFF2196F3)
            "стретчинг" -> Color(0xFF7C4DFF)
            "кардио" -> Color(0xFFFF5252)
            else -> Color(0xFF808080)
        }
}