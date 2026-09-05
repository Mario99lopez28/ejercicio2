package com.mariolopez.daybyday.data.model

/**
 * Minutos de anticipación con los que se dispara la notificación, relativos a la hora de la tarea.
 */
enum class ReminderOption(val minutesBefore: Int, val label: String) {
    AT_TIME(0, "A la hora programada"),
    MIN_5(5, "5 minutos antes"),
    MIN_15(15, "15 minutos antes"),
    MIN_30(30, "30 minutos antes"),
    NONE(-1, "Sin recordatorio");

    companion object {
        fun fromMinutes(minutes: Int): ReminderOption =
            entries.firstOrNull { it.minutesBefore == minutes } ?: AT_TIME
    }
}
