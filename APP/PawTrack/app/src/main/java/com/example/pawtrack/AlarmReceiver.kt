package com.example.pawtrack
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return

        println("Alarm triggered: $message")
        context?.let {
            if (checkNotificationPermission(it)) {
                // Create a notification channel (required for Android Oreo and above)
                createNotificationChannel(it)

                // Create and show the notification
                val notificationBuilder = NotificationCompat.Builder(it, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Reminder")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(it)) {
                    // Notification ID is used to update or cancel the notification
                    notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            }
        }
    }

    private fun checkNotificationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel(context: Context) {
        // Check SDK version for compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val descriptionText = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH // Set to IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendTestNotification(context: Context) {
        val testMessage = "This is a test notification"

        // Check if the app has the necessary permissions
        if (checkNotificationPermission(context)) {
            // Create a notification channel if not already created
            createNotificationChannel(context)

            // Create a notification
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Test Notification")
                .setContentText(testMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } else {
            // Handle the case where permissions are not granted
            // You can prompt the user to grant permissions here
        }
    }

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val NOTIFICATION_ID = 123
    }
}
