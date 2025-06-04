package com.skanderjabouzi.alarm.alarmmanager

import kotlinx.coroutines.CoroutineScope

expect object AlarmHelper {
    fun setAlarm(hour: Int, minute: Int)
    fun cancelAlarm()
    fun playAlarmSound()
    fun stopAlarmSound()
}