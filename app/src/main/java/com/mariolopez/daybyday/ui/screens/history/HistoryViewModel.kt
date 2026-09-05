package com.mariolopez.daybyday.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mariolopez.daybyday.data.local.entity.TaskHistoryEntity
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(private val repository: TaskRepository) : ViewModel() {

    fun allHistory(): Flow<List<TaskHistoryEntity>> = repository.observeAllHistory()

    fun historyForTask(taskId: Long): Flow<List<TaskHistoryEntity>> = repository.observeHistoryForTask(taskId)

    suspend fun getTask(taskId: Long) = repository.getTask(taskId)

    companion object {
        fun factory(repository: TaskRepository) = viewModelFactory {
            initializer { HistoryViewModel(repository) }
        }
    }
}
