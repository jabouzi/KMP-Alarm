package com.skanderjabouzi.alarm.alarmmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

interface NotificationPermissionController {
    fun requestPermission(onResult: (isGranted: Boolean) -> Unit)
    fun isPermissionGranted(): Boolean
}

@Composable
fun rememberNotificationPermissionController(): NotificationPermissionController {
    val context = LocalContext.current
    var currentOnResultCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean -> // Explicitly typed
            currentOnResultCallback?.invoke(isGranted)
            currentOnResultCallback = null
        }
    )

    return remember {
        object : NotificationPermissionController {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun requestPermission(onResult: (isGranted: Boolean) -> Unit) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    onResult(true)
                } else {
                    currentOnResultCallback = onResult
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun isPermissionGranted(): Boolean {
                return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}