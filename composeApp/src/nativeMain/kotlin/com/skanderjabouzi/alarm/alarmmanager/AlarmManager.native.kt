package com.skanderjabouzi.alarm.alarmmanager

import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual object AlarmHelper {
    actual fun setAlarm(hour: Int, minute: Int) {

        /*

        let content = UNMutableNotificationContent()
        content.title = "Wake me up"
        content.body = "Every Monday at 1pm"
        content.sound = UNNotificationSound(named: UNNotificationSoundName("alert.caf"))

        // Configure the recurring date.
        var dateComponents = DateComponents()
        dateComponents.calendar = Calendar.current

        dateComponents.weekday = 2  // Monday
        dateComponents.hour = 13    // 13:00 hours

        // Create the trigger as a repeating event.
        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)

        // Create the request
        let uuidString = UUID().uuidString
        let request = UNNotificationRequest(identifier: uuidString, content: content, trigger: trigger)

        // Schedule the request with the system.
        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.add(request) { (error) in
  if error != nil {
    // Handle any errors.
  }
}

         */
        val content = UNMutableNotificationContent()
        content.setTitle("Alarm!")
        content.setBody("Time to wake up!")
        content.setSound(UNNotificationSound.defaultSound)

        val dateInfo = NSDateComponents()
        dateInfo.hour = hour.toLong()
        dateInfo.minute = minute.toLong()
        println("Alarm scheduled $dateInfo")

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateInfo, repeats = false)

        val request = UNNotificationRequest.requestWithIdentifier("alarmNotification", content, trigger)

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
            .removePendingNotificationRequestsWithIdentifiers(listOf("alarmNotification"))
        println("Alarm cancelled")
    }
}