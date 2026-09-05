package com.mariolopez.daybyday.ui.screens.history

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.repository.TaskRepository
import com.mariolopez.daybyday.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryDetailScreen(taskId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { TaskRepository.getInstance(context) }
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(repository))

    val entries by viewModel.historyForTask(taskId).collectAsState(initial = emptyList())
    var task by remember { mutableStateOf<TaskEntity?>(null) }
    LaunchedEffect(taskId) { task = viewModel.getTask(taskId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entries.firstOrNull()?.taskTitle?.uppercase() ?: "Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            task?.let {
                Text(
                    text = "Fecha original: ${DateTimeUtils.fullDateLabel(it.originalDate)} — ${it.originalTime.format(DateTimeUtils.timeFormatter)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                SimpleDateFormat("dd/MM HH:mm", Locale("es", "ES")).format(Date(entry.timestamp)),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(entry.message, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
