package com.mariolopez.daybyday.ui.navigation

object Destinations {
    const val TODAY = "today?jump={jump}"
    const val TODAY_ROOT = "today"
    const val TASK_FORM = "taskForm?taskId={taskId}&epochDay={epochDay}"
    const val TASK_FORM_ROOT = "taskForm"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "historyDetail/{taskId}"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"

    fun todayWithJump(epochDay: Long) = "$TODAY_ROOT?jump=$epochDay"
    fun taskFormNew(epochDay: Long) = "$TASK_FORM_ROOT?taskId=-1&epochDay=$epochDay"
    fun taskFormEdit(taskId: Long) = "$TASK_FORM_ROOT?taskId=$taskId&epochDay=-1"
    fun historyDetail(taskId: Long) = "historyDetail/$taskId"
}
