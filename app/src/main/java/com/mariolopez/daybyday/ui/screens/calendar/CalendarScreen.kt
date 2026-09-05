package com.mariolopez.daybyday.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariolopez.daybyday.data.repository.TaskRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onDateSelected: (LocalDate) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { TaskRepository.getInstance(context) }
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(repository))

    var month by remember { mutableStateOf(YearMonth.now()) }
    val datesWithTasks by viewModel.datesWithTasks(month).collectAsState(initial = emptyList())
    val spanish = Locale("es", "ES")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Box(Modifier.fillMaxWidth()) {
                IconButton(onClick = { month = month.minusMonths(1) }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(
                    text = "${month.month.getDisplayName(TextStyle.FULL, spanish).replaceFirstChar { it.uppercase() }} ${month.year}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(onClick = { month = month.plusMonths(1) }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                }
            }

            val firstDay = month.atDay(1)
            val leadingBlanks = (firstDay.dayOfWeek.value % 7)
            val totalDays = month.lengthOfMonth()
            val cells = List(leadingBlanks) { null } + (1..totalDays).map { month.atDay(it) }

            Row7Header(spanish)

            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
                items(cells) { date ->
                    if (date == null) {
                        Box(Modifier.aspectRatio(1f))
                    } else {
                        DayCell(
                            date = date,
                            hasTasks = datesWithTasks.contains(date),
                            isToday = date == LocalDate.now(),
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Row7Header(spanish: Locale) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
        val days = listOf(java.time.DayOfWeek.SUNDAY, java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY)
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, spanish).uppercase(spanish),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, hasTasks: Boolean, isToday: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasTasks) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary)
                        .aspectRatio(1f)
                        .fillMaxWidth(0.15f)
                )
            }
        }
    }
}
