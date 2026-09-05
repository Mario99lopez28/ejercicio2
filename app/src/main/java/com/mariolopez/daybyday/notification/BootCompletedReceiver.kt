package com.mariolopez.daybyday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mariolopez.daybyday.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Android borra las alarmas exactas al reiniciar el dispositivo; hay que reprogramarlas. */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TaskRepository.getInstance(context).rescheduleAllPendingAlarms()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
