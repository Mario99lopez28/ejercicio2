package com.mariolopez.daybyday.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mariolopez.daybyday.MainActivity
import com.mariolopez.daybyday.R
import com.mariolopez.daybyday.data.local.entity.TaskEntity

object NotificationHelper {
    const val CHANNEL_ID = "task_reminders"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val ACTION_MARK_DONE = "com.mariolopez.daybyday.ACTION_MARK_DONE"
    const val ACTION_SNOOZE_QUICK = "com.mariolopez.daybyday.ACTION_SNOOZE_QUICK"
    const val ACTION_OPEN_POSTPONE = "com.mariolopez.daybyday.ACTION_OPEN_POSTPONE"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                lightColor = Color.parseColor("#3B5BFD")
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showTaskReminder(context: Context, task: TaskEntity) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, task.id.toInt(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, requestCode(task.id, 1), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val postponeIntent = Intent(context, PostponeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val postponePendingIntent = PendingIntent.getActivity(
            context, requestCode(task.id, 2), postponeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ES HORA · ${task.title}")
            .setContentText("Tenés programada esta tarea para ahora.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.description?.takeIf { it.isNotBlank() } ?: "Tenés programada esta tarea para ahora."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "HECHO", donePendingIntent)
            .addAction(0, "POSPONER", postponePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(task.id.toInt(), notification)
    }

    fun dismiss(context: Context, taskId: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(taskId.toInt())
    }

    fun requestCode(taskId: Long, salt: Int): Int = (taskId * 10 + salt).toInt()
}
