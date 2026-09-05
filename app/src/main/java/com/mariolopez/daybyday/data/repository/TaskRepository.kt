package com.mariolopez.daybyday.data.repository

import android.content.Context
import com.mariolopez.daybyday.data.local.AppDatabase
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.local.entity.TaskHistoryEntity
import com.mariolopez.daybyday.data.model.HistoryEventType
import com.mariolopez.daybyday.data.model.PostponeOption
import com.mariolopez.daybyday.data.model.TaskStatus
import com.mariolopez.daybyday.notification.AlarmScheduler
import com.mariolopez.daybyday.notification.NotificationHelper
import com.mariolopez.daybyday.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class TaskRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val historyDao = db.taskHistoryDao()

    fun observeTasksForDate(date: LocalDate): Flow<List<TaskEntity>> = taskDao.observeTasksForDate(date)

    fun observeDatesWithTasks(start: LocalDate, end: LocalDate): Flow<List<LocalDate>> =
        taskDao.observeDatesWithTasks(start, end)

    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)

    fun observeHistoryForTask(taskId: Long): Flow<List<TaskHistoryEntity>> = historyDao.observeHistoryForTask(taskId)

    fun observeAllHistory(): Flow<List<TaskHistoryEntity>> = historyDao.observeAllHistory()

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun createTask(
        title: String,
        description: String?,
        date: LocalDate,
        time: java.time.LocalTime,
        durationMinutes: Int,
        reminderMinutesBefore: Int
    ): Long {
        val now = System.currentTimeMillis()
        val task = TaskEntity(
            title = title,
            description = description,
            currentDate = date,
            currentTime = time,
            durationMinutes = durationMinutes,
            status = TaskStatus.PENDING,
            reminderMinutesBefore = reminderMinutesBefore,
            originalDate = date,
            originalTime = time,
            createdAt = now
        )
        val id = taskDao.insert(task)
        val saved = task.copy(id = id)
        AlarmScheduler.schedule(context, saved)
        historyDao.insert(
            TaskHistoryEntity(
                taskId = id,
                taskTitle = title,
                timestamp = now,
                eventType = HistoryEventType.CREATED,
                message = "Tarea programada para ${DateTimeUtils.fullDateLabel(date)} — ${time.format(DateTimeUtils.timeFormatter)}."
            )
        )
        return id
    }

    suspend fun updateTaskDetails(
        taskId: Long,
        title: String,
        description: String?,
        date: LocalDate,
        time: java.time.LocalTime,
        durationMinutes: Int,
        reminderMinutesBefore: Int
    ) {
        val existing = taskDao.getById(taskId) ?: return
        val moved = existing.currentDate != date || existing.currentTime != time
        val updated = existing.copy(
            title = title,
            description = description,
            currentDate = date,
            currentTime = time,
            durationMinutes = durationMinutes,
            reminderMinutesBefore = reminderMinutesBefore
        )
        taskDao.update(updated)
        AlarmScheduler.schedule(context, updated)
        historyDao.insert(
            TaskHistoryEntity(
                taskId = taskId,
                taskTitle = title,
                timestamp = System.currentTimeMillis(),
                eventType = HistoryEventType.EDITED,
                message = if (moved) {
                    "Editada y reprogramada para ${DateTimeUtils.fullDateLabel(date)} — ${time.format(DateTimeUtils.timeFormatter)}."
                } else {
                    "Datos de la tarea editados."
                }
            )
        )
    }

    suspend fun markDone(taskId: Long) {
        val task = taskDao.getById(taskId) ?: return
        val now = System.currentTimeMillis()
        taskDao.update(task.copy(status = TaskStatus.DONE, completedAt = now))
        AlarmScheduler.cancel(context, taskId)
        NotificationHelper.dismiss(context, taskId)
        historyDao.insert(
            TaskHistoryEntity(
                taskId = taskId,
                taskTitle = task.title,
                timestamp = now,
                eventType = HistoryEventType.COMPLETED,
                message = "Realizada."
            )
        )
    }

    suspend fun reopen(taskId: Long) {
        val task = taskDao.getById(taskId) ?: return
        taskDao.update(task.copy(status = TaskStatus.PENDING, completedAt = null))
        AlarmScheduler.schedule(context, task.copy(status = TaskStatus.PENDING, completedAt = null))
        historyDao.insert(
            TaskHistoryEntity(
                taskId = taskId,
                taskTitle = task.title,
                timestamp = System.currentTimeMillis(),
                eventType = HistoryEventType.REOPENED,
                message = "Marcada nuevamente como pendiente."
            )
        )
    }

    suspend fun postpone(taskId: Long, option: PostponeOption) {
        val task = taskDao.getById(taskId) ?: return
        val fromDateTime = LocalDateTime.of(task.currentDate, task.currentTime)
        val toDateTime = option.resolve(fromDateTime)

        val updated = task.copy(
            currentDate = toDateTime.toLocalDate(),
            currentTime = toDateTime.toLocalTime(),
            postponeCount = task.postponeCount + 1,
            status = TaskStatus.PENDING
        )
        taskDao.update(updated)
        AlarmScheduler.schedule(context, updated)
        NotificationHelper.dismiss(context, taskId)

        historyDao.insert(
            TaskHistoryEntity(
                taskId = taskId,
                taskTitle = task.title,
                timestamp = System.currentTimeMillis(),
                eventType = HistoryEventType.POSTPONED,
                message = "Pospuesta → ${DateTimeUtils.fullDateLabel(toDateTime.toLocalDate())} ${toDateTime.toLocalTime().format(DateTimeUtils.timeFormatter)}.",
                fromDateTime = fromDateTime,
                toDateTime = toDateTime
            )
        )
    }

    suspend fun deleteTask(taskId: Long) {
        val task = taskDao.getById(taskId) ?: return
        AlarmScheduler.cancel(context, taskId)
        NotificationHelper.dismiss(context, taskId)
        historyDao.insert(
            TaskHistoryEntity(
                taskId = taskId,
                taskTitle = task.title,
                timestamp = System.currentTimeMillis(),
                eventType = HistoryEventType.DELETED,
                message = "Tarea eliminada."
            )
        )
        taskDao.delete(task)
    }

    suspend fun rescheduleAllPendingAlarms() {
        taskDao.getAllPending().forEach { AlarmScheduler.schedule(context, it) }
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
