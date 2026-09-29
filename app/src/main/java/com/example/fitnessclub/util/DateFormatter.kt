package com.example.fitnessclub.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val locale = Locale("ru")
    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy", locale).format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", locale).format(Date(timestamp))

    fun relativeTime(timestamp: Long): String {
        val target = Instant.ofEpochMilli(timestamp).atZone(zoneId)
        val now = Instant.now().atZone(zoneId)
        val dayDifference = ChronoUnit.DAYS.between(now.toLocalDate(), target.toLocalDate())
        val time = SimpleDateFormat("HH:mm", locale).format(Date(timestamp))

        return when (dayDifference) {
            0L -> "Сегодня в $time"
            1L -> "Завтра в $time"
            -1L -> "Вчера в $time"
            in 2L..Long.MAX_VALUE -> "Через $dayDifference ${daysWord(dayDifference)}"
            else -> "${-dayDifference} ${daysWord(-dayDifference)} назад"
        }
    }

    fun monthStart(): Long =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun greetingByTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
    }

    private fun daysWord(days: Long): String = when {
        days % 100 in 11L..14L -> "дней"
        days % 10 == 1L -> "день"
        days % 10 in 2L..4L -> "дня"
        else -> "дней"
    }
}