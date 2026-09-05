package com.mariolopez.daybyday.ui.screens.taskform

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariolopez.daybyday.data.model.ReminderOption
import com.mariolopez.daybyday.data.repository.TaskRepository
import com.mariolopez.daybyday.util.DateTimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

private val durationOptions = listOf(15, 30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskId: Long,
    initialEpochDay: Long,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TaskRepository.getInstance(context) }
    val viewModel: TaskFormViewModel = viewModel(factory = TaskFormViewModel.factory(repository))
    val isEditing = taskId > 0

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(if (initialEpochDay >= 0) LocalDate.ofEpochDay(initialEpochDay) else LocalDate.now()) }
    var time by rememberSaveable { mutableStateOf(LocalTime.of(8, 0)) }
    var durationMinutes by rememberSaveable { mutableStateOf(30) }
    var reminder by rememberSaveable { mutableStateOf(ReminderOption.AT_TIME) }
    var loadedExisting by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(taskId) { viewModel.loadTask(taskId) }
    val existing by viewModel.existingTask.collectAsState()

    LaunchedEffect(existing) {
        existing?.let {
            if (!loadedExisting) {
                title = it.title
                description = it.description.orEmpty()
                date = it.currentDate
                time = it.currentTime
                durationMinutes = it.durationMinutes
                reminder = ReminderOption.fromMinutes(it.reminderMinutesBefore)
                loadedExisting = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar tarea" else "Nueva tarea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("¿Qué tengo que hacer?") },
                modifier = Modifier.fillMaxWidth()
            )

            DaySelector(date = date, onDateChange = { date = it })

            TimeSelector(time = time, onTimeChange = { time = it })

            DurationSelector(durationMinutes = durationMinutes, onDurationChange = { durationMinutes = it })

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción opcional") },
                modifier = Modifier.fillMaxWidth()
            )

            ReminderSelector(reminder = reminder, onReminderChange = { reminder = it })

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.save(
                            taskId = taskId,
                            title = title.trim(),
                            description = description.trim().ifBlank { null },
                            date = date,
                            time = time,
                            durationMinutes = durationMinutes,
                            reminderMinutesBefore = reminder.minutesBefore,
                            onSaved = onDone
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GUARDAR")
            }
        }
    }
}

@Composable
private fun DaySelector(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    Column {
        Text("Día:", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = when (date) {
                    today -> "Hoy — ${DateTimeUtils.shortDateLabel(date)}"
                    tomorrow -> "Mañana — ${DateTimeUtils.shortDateLabel(date)}"
                    else -> DateTimeUtils.fullDateLabel(date)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
            Button(onClick = { onDateChange(today) }) { Text("Hoy") }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
            Button(onClick = { onDateChange(tomorrow) }) { Text("Mañana") }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
            Button(onClick = {
                val now = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth -> onDateChange(LocalDate.of(year, month + 1, dayOfMonth)) },
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) { Text("Elegir") }
        }
    }
}

@Composable
private fun TimeSelector(time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    val context = LocalContext.current
    Column {
        Text("Hora:", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = time.format(DateTimeUtils.timeFormatter),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onTimeChange(LocalTime.of(hour, minute)) },
                    time.hour, time.minute, true
                ).show()
            }) { Text("Elegir hora") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationSelector(durationMinutes: Int, onDurationChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Duración:", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = "$durationMinutes minutos",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                durationOptions.forEach { minutes ->
                    DropdownMenuItem(
                        text = { Text("$minutes minutos") },
                        onClick = { onDurationChange(minutes); expanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSelector(reminder: ReminderOption, onReminderChange: (ReminderOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Recordatorio:", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = reminder.label,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ReminderOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { onReminderChange(option); expanded = false }
                    )
                }
            }
        }
    }
}
