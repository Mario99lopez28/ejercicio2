package com.mariolopez.daybyday.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateTimeUtils {
    private val spanish = Locale("es", "ES")

    fun dayHeaderLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "HOY"
            today.minusDays(1) -> "AYER"
            today.plusDays(1) -> "MAÑANA"
            else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, spanish).uppercase(spanish)
        }
    }

    fun shortDateLabel(date: LocalDate): String {
        val day = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.SHORT, spanish).uppercase(spanish).removeSuffix(".")
        return "$day $month"
    }

    fun fullDateLabel(date: LocalDate): String {
        val day = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.FULL, spanish)
        return "$day de $month"
    }

    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}
