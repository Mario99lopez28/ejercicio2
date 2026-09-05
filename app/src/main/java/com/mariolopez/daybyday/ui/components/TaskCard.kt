package com.mariolopez.daybyday.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.model.TaskStatus
import com.mariolopez.daybyday.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    isNextUp: Boolean,
    onToggleDone: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                DismissValue.DismissedToEnd -> {
                    onToggleDone(); false
                }
                DismissValue.DismissedToStart -> {
                    onPostpone(); false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground(dismissState.dismissDirection) },
        content = {
            TaskCardContent(
                task = task,
                isNextUp = isNextUp,
                onToggleDone = onToggleDone,
                onPostpone = onPostpone,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(direction: DismissDirection?) {
    val backgroundColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val alignment: Alignment

    when (direction) {
        DismissDirection.StartToEnd -> {
            backgroundColor = MaterialTheme.colorScheme.primary
            icon = Icons.Filled.CheckCircle
            alignment = Alignment.CenterStart
        }
        DismissDirection.EndToStart -> {
            backgroundColor = MaterialTheme.colorScheme.secondary
            icon = Icons.Filled.Schedule
            alignment = Alignment.CenterEnd
        }
        else -> {
            backgroundColor = Color.Transparent
            icon = Icons.Filled.Schedule
            alignment = Alignment.Center
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor),
        contentAlignment = alignment
    ) {
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun TaskCardContent(
    task: TaskEntity,
    isNextUp: Boolean,
    onToggleDone: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDone = task.status == TaskStatus.DONE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextUp && !isDone) 6.dp else 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.currentTime.format(DateTimeUtils.timeFormatter),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isNextUp && !isDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isNextUp && !isDone) {
                            Text(
                                text = "  ·  PRÓXIMA",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (task.postponeCount > 0) {
                            Text(
                                text = "  ·  pospuesta x${task.postponeCount}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Editar") }, onClick = { menuExpanded = false; onEdit() })
                        DropdownMenuItem(text = { Text("Posponer") }, onClick = { menuExpanded = false; onPostpone() })
                        DropdownMenuItem(text = { Text("Eliminar") }, onClick = { menuExpanded = false; onDelete() })
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleDone
                    )
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isDone) "Realizada" else "Hecho",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
