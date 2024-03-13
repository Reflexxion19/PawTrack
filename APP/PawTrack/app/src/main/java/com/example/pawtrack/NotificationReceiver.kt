package com.example.pawtrack

import androidx.core.content.ContextCompat
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create and display notification
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
        val notification = createNotification(context, "Reminder Title", "Reminder message")
        notificationManager.notify(0, notification)
    }

    private fun createNotification(context: Context, title: String, message: String): Notification {
        // Create and configure notification
        // You can customize the notification as per your requirements
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    companion object {
        private const val CHANNEL_ID = "pawtrackNotif"
    }
}