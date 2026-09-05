package com.mariolopez.daybyday.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import com.mariolopez.daybyday.data.model.PostponeOption

/**
 * Lista de opciones rápidas de postergación, usada tanto desde la notificación
 * (PostponeActivity) como desde la tarjeta de tarea en la app.
 */
@Composable
fun PostponeOptionsList(
    taskTitle: String,
    onOptionSelected: (PostponeOption) -> Unit,
    onCustomSelected: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "¿Para cuándo querés posponer \"$taskTitle\"?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(20.dp)
        )
        PostponeOption.quickOptions().forEach { option ->
            PostponeRow(icon = Icons.Filled.Schedule, label = option.label) {
                onOptionSelected(option)
            }
        }
        PostponeRow(icon = Icons.Filled.CalendarMonth, label = "Elegir fecha y hora") {
            onCustomSelected()
        }
    }
}

@Composable
private fun PostponeRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        androidx.compose.foundation.layout.Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider()
}
