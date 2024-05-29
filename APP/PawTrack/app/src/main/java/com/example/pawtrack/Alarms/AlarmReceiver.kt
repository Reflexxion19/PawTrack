package com.example.pawtrack.Alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pawtrack.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "onReceive called")

        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: run {
            Log.e("AlarmReceiver", "No message found in intent")
            return
        }

        context?.let {
            createNotificationChannel(it)

            val notificationBuilder = NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(it)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
                Log.d("AlarmReceiver", "Notification displayed")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val descriptionText = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("AlarmReceiver", "Notification channel created")
        }
    }

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val NOTIFICATION_ID = 123
    }
}
