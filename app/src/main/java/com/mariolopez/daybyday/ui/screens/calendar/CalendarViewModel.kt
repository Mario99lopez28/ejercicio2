package com.mariolopez.daybyday.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val repository: TaskRepository) : ViewModel() {

    fun datesWithTasks(month: YearMonth): Flow<List<LocalDate>> =
        repository.observeDatesWithTasks(month.atDay(1), month.atEndOfMonth())

    companion object {
        fun factory(repository: TaskRepository) = viewModelFactory {
            initializer { CalendarViewModel(repository) }
        }
    }
}
