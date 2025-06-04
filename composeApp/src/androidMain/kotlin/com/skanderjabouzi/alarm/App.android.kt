package com.skanderjabouzi.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.skanderjabouzi.alarm.alarmmanager.rememberExactAlarmPermissionController
import com.skanderjabouzi.alarm.alarmmanager.rememberNotificationPermissionController

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotificationPermissionRequest()
            App()
        }
    }
}

@Preview
@Composable
fun AppPreview() { App() }

@Composable
fun NotificationPermissionRequest() {
    val notificationPermissionController = rememberNotificationPermissionController()
    LaunchedEffect(Unit) {
        notificationPermissionController.requestPermission { isGranted: Boolean -> // Explicitly typed
            if (isGranted) {
                println("Notification permission granted")
            } else {
                println("Notification permission denied")
            }
        }
    }
    ExactAlarmPermissionRequest()
}

@Composable
fun ExactAlarmPermissionRequest() {
    val exactAlarmPermissionController = rememberExactAlarmPermissionController()
    LaunchedEffect(Unit) {
        exactAlarmPermissionController.requestPermission { isGranted: Boolean -> // Explicitly typed
            if (isGranted) {
                println("Exact alarm permission granted")
            } else {
                println("Exact alarm permission denied")
            }
        }
    }
}
