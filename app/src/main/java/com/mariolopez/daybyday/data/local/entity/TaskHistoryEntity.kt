package com.mariolopez.daybyday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mariolopez.daybyday.data.model.HistoryEventType
import java.time.LocalDateTime

@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long,
    val taskTitle: String,
    val timestamp: Long,
    val eventType: HistoryEventType,
    val message: String,
    val fromDateTime: LocalDateTime? = null,
    val toDateTime: LocalDateTime? = null
)
