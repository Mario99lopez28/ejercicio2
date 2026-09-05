package com.mariolopez.daybyday.ui.screens.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TaskFormViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _existingTask = MutableStateFlow<TaskEntity?>(null)
    val existingTask: StateFlow<TaskEntity?> = _existingTask

    fun loadTask(taskId: Long) {
        if (taskId <= 0) return
        viewModelScope.launch {
            _existingTask.value = repository.getTask(taskId)
        }
    }

    fun save(
        taskId: Long,
        title: String,
        description: String?,
        date: LocalDate,
        time: LocalTime,
        durationMinutes: Int,
        reminderMinutesBefore: Int,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (taskId > 0) {
                repository.updateTaskDetails(taskId, title, description, date, time, durationMinutes, reminderMinutesBefore)
            } else {
                repository.createTask(title, description, date, time, durationMinutes, reminderMinutesBefore)
            }
            onSaved()
        }
    }

    companion object {
        fun factory(repository: TaskRepository) = viewModelFactory {
            initializer { TaskFormViewModel(repository) }
        }
    }
}
