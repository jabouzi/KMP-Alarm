package com.skanderjabouzi.alarm.alarmmanager

expect object AlarmHelper {
    fun setAlarm(hour: Int, minute: Int)
    fun cancelAlarm()
    fun playAlarmSound()
    fun stopAlarmSound()
}