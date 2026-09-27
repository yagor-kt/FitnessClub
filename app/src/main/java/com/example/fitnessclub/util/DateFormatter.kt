package com.example.fitnessclub.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val locale = Locale("ru")

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy", locale).format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", locale).format(Date(timestamp))
}