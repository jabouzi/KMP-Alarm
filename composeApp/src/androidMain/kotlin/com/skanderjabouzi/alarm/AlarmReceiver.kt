package com.skanderjabouzi.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context, intent: Intent) {
        println("onReceive")
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmReceiver:MyWakeLock")
        wl.acquire(10*60*1000L /*10 minutes*/)
        println("Alarm !!!!!!!!!!")
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show()

        wl.release()
    }
}