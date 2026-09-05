package com.mariolopez.daybyday.ui.screens.today

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.model.PostponeOption
import com.mariolopez.daybyday.data.model.TaskStatus
import com.mariolopez.daybyday.data.repository.TaskRepository
import com.mariolopez.daybyday.ui.components.DayNavigator
import com.mariolopez.daybyday.ui.components.PostponeOptionsList
import com.mariolopez.daybyday.ui.components.TaskCard
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

private const val PAGE_COUNT = 40000
private const val CENTER_PAGE = PAGE_COUNT / 2

fun pageForDate(date: LocalDate): Int = (CENTER_PAGE + (date.toEpochDay() - LocalDate.now().toEpochDay())).toInt()
fun dateForPage(page: Int): LocalDate = LocalDate.now().plusDays((page - CENTER_PAGE).toLong())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    jumpToEpochDay: Long,
    onAddTask: (LocalDate) -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TaskRepository.getInstance(context) }
    val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(repository))
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = CENTER_PAGE, pageCount = { PAGE_COUNT })

    LaunchedEffect(jumpToEpochDay) {
        if (jumpToEpochDay >= 0) {
            val target = pageForDate(LocalDate.ofEpochDay(jumpToEpochDay))
            pagerState.scrollToPage(target)
        }
    }

    var postponeTarget by remember { mutableStateOf<TaskEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onOpenHistory) { Text("Historial") }
                    TextButton(onClick = onOpenSettings) { Text("Ajustes") }
                }
                DayNavigator(
                    date = dateForPage(pagerState.currentPage),
                    onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    onOpenCalendar = onOpenCalendar
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTask(dateForPage(pagerState.currentPage)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("NUEVA TAREA") }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val date = dateForPage(page)
            val tasks by viewModel.tasksForDate(date).collectAsState(initial = emptyList())
            DayTaskList(
                date = date,
                tasks = tasks,
                onToggleDone = { viewModel.toggleDone(it) },
                onPostponeRequest = { postponeTarget = it },
                onEdit = onEditTask,
                onDelete = { viewModel.deleteTask(it.id) }
            )
        }
    }

    val current = postponeTarget
    if (current != null) {
        ModalBottomSheet(onDismissRequest = { postponeTarget = null }, sheetState = sheetState) {
            PostponeOptionsList(
                taskTitle = current.title,
                onOptionSelected = { option ->
                    viewModel.postpone(current.id, option)
                    postponeTarget = null
                },
                onCustomSelected = {
                    postponeTarget = null
                    showCustomDatePicker(context) { dateTime ->
                        viewModel.postpone(current.id, PostponeOption.Custom(dateTime))
                    }
                }
            )
        }
    }
}

@Composable
private fun DayTaskList(
    date: LocalDate,
    tasks: List<TaskEntity>,
    onToggleDone: (TaskEntity) -> Unit,
    onPostponeRequest: (TaskEntity) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    val now = LocalDateTime.now()
    val isToday = date == LocalDate.now()
    val nextUpId = if (isToday) {
        tasks.filter { it.status == TaskStatus.PENDING }
            .filter { LocalDateTime.of(it.currentDate, it.currentTime).isAfter(now) }
            .minByOrNull { it.currentTime }
            ?.id
    } else null

    if (tasks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No tenés tareas cargadas para este día.\nTocá \"+ NUEVA TAREA\" para agregar una.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                isNextUp = task.id == nextUpId,
                onToggleDone = { onToggleDone(task) },
                onPostpone = { onPostponeRequest(task) },
                onEdit = { onEdit(task.id) },
                onDelete = { onDelete(task) }
            )
        }
    }
}

private fun showCustomDatePicker(context: Context, onPicked: (LocalDateTime) -> Unit) {
    val now = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onPicked(LocalDateTime.of(year, month + 1, dayOfMonth, hour, minute))
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
