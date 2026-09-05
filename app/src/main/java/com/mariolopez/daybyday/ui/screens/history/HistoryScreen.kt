package com.mariolopez.daybyday.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariolopez.daybyday.data.model.HistoryEventType
import com.mariolopez.daybyday.data.repository.TaskRepository
import com.mariolopez.daybyday.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit, onOpenTaskDetail: (Long) -> Unit) {
    val context = LocalContext.current
    val repository = remember { TaskRepository.getInstance(context) }
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(repository))

    val allHistory by viewModel.allHistory().collectAsState(initial = emptyList())
    val latestPerTask = remember(allHistory) {
        allHistory.groupBy { it.taskId }
            .map { (_, entries) -> entries.maxByOrNull { it.timestamp }!! }
            .sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        if (latestPerTask.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Todavía no hay actividad registrada.", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(latestPerTask, key = { it.taskId }) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onOpenTaskDetail(entry.taskId) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.taskTitle.uppercase(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${eventLabel(entry.eventType)} · ${formatTimestamp(entry.timestamp)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(entry.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun eventLabel(type: HistoryEventType): String = when (type) {
    HistoryEventType.CREATED -> "Programada"
    HistoryEventType.EDITED -> "Editada"
    HistoryEventType.POSTPONED -> "Pospuesta"
    HistoryEventType.COMPLETED -> "Realizada"
    HistoryEventType.REOPENED -> "Reabierta"
    HistoryEventType.DELETED -> "Eliminada"
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd/MM HH:mm", Locale("es", "ES")).format(Date(timestamp))
