package com.mariolopez.daybyday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mariolopez.daybyday.data.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String?,
    /** Fecha y hora en las que la tarea aparece actualmente en la agenda (cambia al posponer). */
    val currentDate: LocalDate,
    val currentTime: LocalTime,
    val durationMinutes: Int,
    val status: TaskStatus,
    val reminderMinutesBefore: Int,
    /** Fecha y hora con las que la tarea fue creada originalmente; nunca cambia. */
    val originalDate: LocalDate,
    val originalTime: LocalTime,
    val createdAt: Long,
    val completedAt: Long? = null,
    val postponeCount: Int = 0
)
