package com.skanderjabouzi.alarm.alarmmanager

import alarm_app.composeapp.generated.resources.Res
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.skanderjabouzi.alarm.AlarmReceiver
import com.skanderjabouzi.alarm.AppActivity
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.util.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

@SuppressLint("StaticFieldLeak")
actual object AlarmHelper{
    private val context: Context = application.applicationContext
    private const val CHANNEL_ID = "alarm_channel"
    private const val NOTIFICATION_ID = 1

    private var mediaPlayer: MediaPlayer? = null

    init {
        createNotificationChannel()
    }

    @SuppressLint("ScheduleExactAlarm")
    actual fun setAlarm(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                println("Alarm scheduled for $hour:$minute")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Explain to the user that they need to grant the SCHEDULE_EXACT_ALARM permission
                // Or use setAlarmClock as a fallback (less precise)
                println("Please grant SCHEDULE_EXACT_ALARM permission")
                // You might want to show a dialog here to guide the user
            }
        } else {
            println("Alarm scheduled for $hour:$minute")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
    }

    actual fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
        Toast.makeText(context, "Alarm cancelled", Toast.LENGTH_SHORT).show()
    }

    fun showNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Replace with your icon
            .setContentTitle("Alarm!")
            .setContentText("Time to wake up!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true) // Remove notification when tapped
            //.setFullScreenIntent(createFullScreenIntent(), true) // Show notification even when locked

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createFullScreenIntent(): PendingIntent {
        val intent = Intent(context, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @OptIn(ExperimentalResourceApi::class)
    @SuppressLint("DiscouragedApi")
    actual fun playAlarmSound() {
        runBlocking {
            val bytes = Res.readBytes("files/athan.mp3")

            // Create a temporary file
            val tempFile = File.createTempFile("temp_audio", ".mp3").apply {
                deleteOnExit()
                writeBytes(bytes)
            }

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(tempFile.absolutePath.toString())
                        setVolume(0.5f, 0.5f)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                        isLooping = false
                        prepare()
                        start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Error playing alarm sound: ${e.message}")
                    }
                }
            } else {
                if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.start()
                }
            }
        }
    }

    actual fun stopAlarmSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notifications"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH // Important for alarms
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true) // Enable vibration
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}