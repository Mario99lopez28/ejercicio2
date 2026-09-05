package com.mariolopez.daybyday

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.mariolopez.daybyday.notification.NotificationHelper
import com.mariolopez.daybyday.ui.navigation.DayByDayNavGraph
import com.mariolopez.daybyday.ui.navigation.Destinations
import com.mariolopez.daybyday.ui.theme.DayByDayTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        val openTaskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)

        setContent {
            var forceDarkMode by remember { mutableStateOf(false) }
            DayByDayTheme(darkTheme = forceDarkMode || isSystemDark()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    DayByDayNavGraph(
                        navController = navController,
                        forceDarkMode = forceDarkMode,
                        onForceDarkModeChange = { forceDarkMode = it }
                    )
                    if (openTaskId > 0) {
                        androidx.compose.runtime.LaunchedEffect(openTaskId) {
                            navController.navigate(Destinations.taskFormEdit(openTaskId))
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun isSystemDark(): Boolean = androidx.compose.foundation.isSystemInDarkTheme()
