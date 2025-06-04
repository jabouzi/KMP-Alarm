package com.skanderjabouzi.alarm

import alarm_app.composeapp.generated.resources.Res
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.skanderjabouzi.alarm.alarmmanager.rememberExactAlarmPermissionController
import com.skanderjabouzi.alarm.alarmmanager.rememberNotificationPermissionController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotificationPermissionRequest()
            TestAudioRwarning()
            App()
        }
    }
}

@Preview
@Composable
fun AppPreview() { App() }

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalResourceApi::class)
@Composable
fun TestAudioRwarning() {
    // This is a placeholder for the audio warning test.
    // You can implement your audio warning logic here.
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            val bytes = Res.readBytes("files/athan.mp3")
            println("Audio file size: ${bytes.size} bytes")
        }
    }
}

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
