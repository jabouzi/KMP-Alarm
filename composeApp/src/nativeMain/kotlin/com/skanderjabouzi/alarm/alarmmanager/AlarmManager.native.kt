package com.skanderjabouzi.alarm.alarmmanager

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSDateComponents
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject

actual object AlarmHelper {
    private val notificationIdentifier = "alarmNotification"
    private var audioPlayer: AVAudioPlayer? = null

    actual fun setAlarm(hour: Int, minute: Int) {
        val content = UNMutableNotificationContent()
        content.setTitle("Alarm!")
        content.setBody("Time to wake up!")
        content.setSound(UNNotificationSound.defaultSound)

        val dateInfo = NSDateComponents()
        dateInfo.hour = hour.toLong()
        dateInfo.minute = minute.toLong()
        println("Alarm scheduled $dateInfo")

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateInfo, repeats = false)

        val request = UNNotificationRequest.requestWithIdentifier(notificationIdentifier, content, trigger)

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling notification: ${error.localizedDescription}")
            } else {
                println("Notification scheduled successfully!")
            }
        }
    }

    actual fun cancelAlarm() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(notificationIdentifier))
        println("Alarm cancelled")
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun playAlarmSound() {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
        audioSession.setActive(true, error = null)

        val soundURL = NSBundle.mainBundle.URLForResource(
            "athan", withExtension = "mp3"
        ) // Replace with your sound file
        if (soundURL != null) {
            audioPlayer = AVAudioPlayer(contentsOfURL = soundURL, error = null)
            audioPlayer?.numberOfLoops = -1 // Loop indefinitely
            audioPlayer?.prepareToPlay()
            audioPlayer?.play()
        } else {
            println("Error: Could not find sound file.")
        }
    }

    actual fun stopAlarmSound() {
        audioPlayer?.stop()
        audioPlayer = null
    }

    fun applicationDidBecomeActive() {
        // Resume playing the full sound if the app becomes active while the alarm is going off
        if (audioPlayer?.isPlaying() == false) {
            playAlarmSound()
        }
    }
}

class iOSAppLifeCycleObserver : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun applicationDidBecomeActive(notification: NSNotification) {
        AlarmHelper.applicationDidBecomeActive()
    }

    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun startObserving() {
            NSNotificationCenter.defaultCenter().addObserver(
                observer = iOSAppLifeCycleObserver(),
                selector = NSSelectorFromString("applicationDidBecomeActive:"),
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null
            )
        }
    }
}