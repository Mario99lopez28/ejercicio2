package com.mariolopez.daybyday.notification

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.model.PostponeOption
import com.mariolopez.daybyday.data.repository.TaskRepository
import com.mariolopez.daybyday.ui.components.PostponeOptionsList
import com.mariolopez.daybyday.ui.theme.DayByDayTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

/** Actividad transparente que muestra las opciones de postergación desde la notificación. */
class PostponeActivity : ComponentActivity() {

    private lateinit var repository: TaskRepository
    private var taskId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = TaskRepository.getInstance(applicationContext)
        taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)

        setContent {
            DayByDayTheme {
                var task by remember { mutableStateOf<TaskEntity?>(null) }

                LaunchedEffect(taskId) {
                    task = repository.getTask(taskId)
                    if (task == null) finish()
                }

                task?.let { currentTask ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 8.dp
                        ) {
                            PostponeOptionsList(
                                taskTitle = currentTask.title,
                                onOptionSelected = { option -> applyPostpone(option) },
                                onCustomSelected = { showCustomPicker(currentTask) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyPostpone(option: PostponeOption) {
        lifecycleScope.launch {
            repository.postpone(taskId, option)
            finish()
        }
    }

    private fun showCustomPicker(task: TaskEntity) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        val dateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hour, minute)
                        applyPostpone(PostponeOption.Custom(dateTime))
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
