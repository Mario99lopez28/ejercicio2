package com.mariolopez.daybyday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Resuelve las acciones rápidas de la notificación (HECHO) sin necesidad de abrir la app. */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TaskRepository.getInstance(context)
                when (intent.action) {
                    NotificationHelper.ACTION_MARK_DONE -> {
                        repository.markDone(taskId)
                        NotificationHelper.dismiss(context, taskId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
