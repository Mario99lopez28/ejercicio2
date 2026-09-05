package com.mariolopez.daybyday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import com.mariolopez.daybyday.data.model.TaskStatus
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Se dispara cuando llega la hora programada de una tarea: vibra y muestra la notificación. */
class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TaskRepository.getInstance(context)
                val task = repository.getTask(taskId)
                if (task != null && task.status == TaskStatus.PENDING) {
                    vibrate(context)
                    NotificationHelper.showTaskReminder(context, task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun vibrate(context: Context) {
        val pattern = longArrayOf(0, 400, 200, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService<VibratorManager>()
            manager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            val vibrator = context.getSystemService<Vibrator>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        }
    }
}
