package com.mariolopez.daybyday.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.model.PostponeOption
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(private val repository: TaskRepository) : ViewModel() {

    fun tasksForDate(date: LocalDate): Flow<List<TaskEntity>> = repository.observeTasksForDate(date)

    fun toggleDone(task: TaskEntity) {
        viewModelScope.launch {
            if (task.status == com.mariolopez.daybyday.data.model.TaskStatus.DONE) {
                repository.reopen(task.id)
            } else {
                repository.markDone(task.id)
            }
        }
    }

    fun postpone(taskId: Long, option: PostponeOption) {
        viewModelScope.launch { repository.postpone(taskId, option) }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch { repository.deleteTask(taskId) }
    }

    companion object {
        fun factory(repository: TaskRepository) = viewModelFactory {
            initializer { TodayViewModel(repository) }
        }
    }
}
