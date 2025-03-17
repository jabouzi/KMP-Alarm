package com.skanderjabouzi.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skanderjabouzi.alarm.alarmmanager.AlarmHelper
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AlarmScreen() {
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
    var isAlarmSet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = hour,
            onValueChange = { hour = it },
            label = { Text("Hour") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = minute,
            onValueChange = { minute = it },
            label = { Text("Minute") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (hour.isNotEmpty() && minute.isNotEmpty()) {
                    val hourInt = hour.toIntOrNull()
                    val minuteInt = minute.toIntOrNull()

                    if ((hourInt != null) && (minuteInt != null) && (hourInt in (0..23))
                        && (minuteInt in (0..59))) {
                        if (isAlarmSet) {
                            AlarmHelper.cancelAlarm()
                            isAlarmSet = false
                        } else {
                            AlarmHelper.setAlarm(hourInt, minuteInt)
                            isAlarmSet = true
                        }
                    } else {
                        println("Invalid hour or minute")
                    }
                } else {
                    println("Please enter hour and minute")
                }
            }
        ) {
            Text(text = if (isAlarmSet) "Cancel Alarm" else "Set Alarm")
        }
    }
}


@Preview
@Composable
fun DefaultPreview() {
    AlarmScreen()
}