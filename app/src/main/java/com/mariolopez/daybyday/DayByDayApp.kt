package com.mariolopez.daybyday

import android.app.Application
import com.mariolopez.daybyday.notification.NotificationHelper

class DayByDayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
