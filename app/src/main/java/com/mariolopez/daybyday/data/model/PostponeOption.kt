package com.mariolopez.daybyday.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

sealed class PostponeOption(val label: String) {
    data object Min15 : PostponeOption("15 minutos")
    data object Min30 : PostponeOption("30 minutos")
    data object Hour1 : PostponeOption("1 hora")
    data object ThisEvening : PostponeOption("Esta tarde")
    data object Tomorrow : PostponeOption("Mañana")
    data class Custom(val dateTime: LocalDateTime) : PostponeOption("Elegir fecha y hora")

    fun resolve(from: LocalDateTime): LocalDateTime = when (this) {
        is Min15 -> from.plusMinutes(15)
        is Min30 -> from.plusMinutes(30)
        is Hour1 -> from.plusHours(1)
        is ThisEvening -> {
            val evening = from.toLocalDate().atTime(19, 0)
            if (evening.isAfter(from)) evening else from.toLocalDate().plusDays(1).atTime(19, 0)
        }
        is Tomorrow -> LocalDateTime.of(from.toLocalDate().plusDays(1), from.toLocalTime())
        is Custom -> dateTime
    }

    companion object {
        fun quickOptions(): List<PostponeOption> = listOf(Min15, Min30, Hour1, ThisEvening, Tomorrow)
    }
}

fun LocalDate.combine(time: LocalTime): LocalDateTime = LocalDateTime.of(this, time)
