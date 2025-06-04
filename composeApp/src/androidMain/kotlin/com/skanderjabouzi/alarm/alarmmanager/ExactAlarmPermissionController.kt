package com.skanderjabouzi.alarm.alarmmanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

interface ExactAlarmPermissionController {
    fun requestPermission(onResult: (isGranted: Boolean) -> Unit)
    fun isPermissionGranted(): Boolean
}

@Composable
fun rememberExactAlarmPermissionController(): ExactAlarmPermissionController {
    val context = LocalContext.current
    var currentOnResultCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }

    // Launcher to handle the result from the settings screen
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            // After returning from settings, check the permission status again
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isGranted = (context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).canScheduleExactAlarms()
                currentOnResultCallback?.invoke(isGranted)
                currentOnResultCallback = null
            }
            permissionRequested = false // Reset for next request
        }
    )

    // Effect to re-check permission if app is resumed and a request was pending
    // This handles the case where the user grants permission in settings and returns
    LaunchedEffect(permissionRequested, currentOnResultCallback) {
        if (permissionRequested && currentOnResultCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val isGranted = (context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).canScheduleExactAlarms()
            if (isGranted) {
                currentOnResultCallback?.invoke(true)
                currentOnResultCallback = null
                permissionRequested = false
            }
        }
    }

    return remember {
        object : ExactAlarmPermissionController {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun requestPermission(onResult: (isGranted: Boolean) -> Unit) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    onResult(true)
                } else {
                    currentOnResultCallback = onResult
                    permissionRequested = true
                    // Intent to open the app's settings page for scheduling exact alarms
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    // Check if there's an activity to handle this intent
                    if (intent.resolveActivity(context.packageManager) != null) {
                        settingsLauncher.launch(intent)
                    } else {
                        // Fallback or error handling if the intent cannot be resolved
                        // This typically shouldn't happen for this specific intent on S+
                        onResult(false) // Or show an error message
                        currentOnResultCallback = null
                        permissionRequested = false
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.S) // Changed from TIRAMISU to S for canScheduleExactAlarms
            override fun isPermissionGranted(): Boolean {
                // For SCHEDULE_EXACT_ALARM, the check is different
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    alarmManager.canScheduleExactAlarms()
                } else {
                    // On versions below S, this permission is not needed / doesn't exist
                    // or is implicitly granted if declared (though it's an S+ feature)
                    true // Or false, depending on how you want to interpret it for older versions
                }
            }
        }
    }
}