package com.mariolopez.daybyday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mariolopez.daybyday.ui.screens.calendar.CalendarScreen
import com.mariolopez.daybyday.ui.screens.history.HistoryScreen
import com.mariolopez.daybyday.ui.screens.history.TaskHistoryDetailScreen
import com.mariolopez.daybyday.ui.screens.settings.SettingsScreen
import com.mariolopez.daybyday.ui.screens.taskform.TaskFormScreen
import com.mariolopez.daybyday.ui.screens.today.TodayScreen

@Composable
fun DayByDayNavGraph(
    navController: NavHostController,
    forceDarkMode: Boolean,
    onForceDarkModeChange: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = Destinations.TODAY) {
        composable(
            route = Destinations.TODAY,
            arguments = listOf(navArgument("jump") { type = NavType.LongType; defaultValue = -1L })
        ) { entry ->
            val jump = entry.arguments?.getLong("jump") ?: -1L
            TodayScreen(
                jumpToEpochDay = jump,
                onAddTask = { date -> navController.navigate(Destinations.taskFormNew(date.toEpochDay())) },
                onEditTask = { taskId -> navController.navigate(Destinations.taskFormEdit(taskId)) },
                onOpenCalendar = { navController.navigate(Destinations.CALENDAR) },
                onOpenHistory = { navController.navigate(Destinations.HISTORY) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        composable(
            route = Destinations.TASK_FORM,
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("epochDay") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: -1L
            val epochDay = entry.arguments?.getLong("epochDay") ?: -1L
            TaskFormScreen(
                taskId = taskId,
                initialEpochDay = epochDay,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenTaskDetail = { taskId -> navController.navigate(Destinations.historyDetail(taskId)) }
            )
        }

        composable(
            route = Destinations.HISTORY_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: -1L
            TaskHistoryDetailScreen(taskId = taskId, onBack = { navController.popBackStack() })
        }

        composable(Destinations.CALENDAR) {
            CalendarScreen(
                onDateSelected = { date ->
                    navController.navigate(Destinations.todayWithJump(date.toEpochDay())) {
                        popUpTo(Destinations.TODAY) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                followSystemDarkMode = !forceDarkMode,
                forceDarkMode = forceDarkMode,
                onForceDarkModeChange = onForceDarkModeChange,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
