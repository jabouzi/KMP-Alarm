package com.skanderjabouzi.alarm.alarmmanager

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.CoroutineScope
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
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptionBadge
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

// Delegate to handle foreground notifications
class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (ULong) -> Unit
    ) {
        // Show alert, sound, and badge when the app is in the foreground
        withCompletionHandler(
            UNNotificationPresentationOptionAlert or
                    UNNotificationPresentationOptionSound or
                    UNNotificationPresentationOptionBadge
        )
    }
}

actual object AlarmHelper {
    private val notificationIdentifier = "alarmNotification"
    private var audioPlayer: AVAudioPlayer? = null
    private val notificationDelegate = NotificationDelegate() // Keep a strong reference to the delegate

    init {
        // Set the delegate for handling foreground notifications
        UNUserNotificationCenter.currentNotificationCenter().setDelegate(notificationDelegate)
        // Request notification permissions
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (granted) {
                println("Notification permission granted.")
            } else {
                println("Notification permission denied: ${error?.localizedDescription}")
            }
        }
    }

    actual fun setAlarm(hour: Int, minute: Int) {
        val content = UNMutableNotificationContent()
        content.setTitle("Alarm!")
        content.setBody("Time to wake up!")
        content.setSound(UNNotificationSound.defaultSound) // Standard notification sound

        val dateInfo = NSDateComponents()
        dateInfo.hour = hour.toLong()
        dateInfo.minute = minute.toLong()
        println("Alarm scheduled for $hour:$minute")

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateInfo, repeats = false)

        val request = UNNotificationRequest.requestWithIdentifier(notificationIdentifier, content, trigger)

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling notification: ${error.localizedDescription}")
            } else {
                println("Notification scheduled successfully!")
                // Note: The custom sound via playAlarmSound() is separate from the notification's own sound.
                // If you want the custom sound to play when the notification fires,
                // you might need to coordinate it, perhaps by observing notification delivery
                // or relying on the app being brought to the foreground by the notification.
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
        try {
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)
        } catch (e: Exception) {
            println("Error setting up audio session: ${e.message}")
        }


        val soundURL = NSBundle.mainBundle.URLForResource(
            "athan", withExtension = "mp3"
        )
        if (soundURL != null) {
            audioPlayer = try {
                AVAudioPlayer(contentsOfURL = soundURL, error = null).apply {
                    numberOfLoops = -1 // Loop indefinitely
                    prepareToPlay()
                    play()
                }
            } catch (e: Exception) {
                println("Error initializing AVAudioPlayer: ${e.message}")
                null
            }
        } else {
            println("Error: Could not find sound file 'athan.mp3'.")
        }
    }

    actual fun stopAlarmSound() {
        audioPlayer?.stop()
        audioPlayer = null
        // Optionally deactivate audio session if no longer needed
        // val audioSession = AVAudioSession.sharedInstance()
        // try {
        //     audioSession.setActive(false, error = null)
        // } catch (e: Exception) {
        //     println("Error deactivating audio session: ${e.message}")
        // }
    }

    // This method is called by iOSAppLifeCycleObserver when app becomes active
    fun applicationDidBecomeActive() {
        // This logic was intended to resume sound if interrupted.
        // Consider if it's still needed or if the notification handling covers it.
        // If the alarm sound is meant to play continuously until stopped, this might be relevant.
        if (audioPlayer?.isPlaying() == false && audioPlayer != null) { // Ensure audioPlayer was initialized
            println("Resuming alarm sound on app becoming active.")
            // Re-check conditions under which sound should play.
            // For example, is an alarm supposed to be "active"?
            // This might need more state management if playAlarmSound() is tied to the notification firing.
            // For now, let's assume if audioPlayer exists and isn't playing, it should resume.
            audioPlayer?.play()
        }
    }
}

class iOSAppLifeCycleObserver : NSObject() {
    @Suppress("unused")
    @OptIn(ExperimentalForeignApi::class)
    @ObjCAction
    fun applicationDidBecomeActive(notification: NSNotification) {
        AlarmHelper.applicationDidBecomeActive()
    }

    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun startObserving() {
            NSNotificationCenter.defaultCenter().addObserver(
                observer = iOSAppLifeCycleObserver(), // A new instance for the observer
                selector = NSSelectorFromString("applicationDidBecomeActive:"),
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null
            )
        }
    }
}