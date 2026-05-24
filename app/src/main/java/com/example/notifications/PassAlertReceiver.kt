package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PassAlertReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "orbital_passes_channel"
        const val EXTRA_TITLE = "extra_pass_title"
        const val EXTRA_MESSAGE = "extra_pass_message"
        const val EXTRA_ALERT_ID = "extra_alert_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Satellite Pass Incoming"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "A SpaceX satellite is passing over your location now!"
        val alertId = intent.getStringExtra(EXTRA_ALERT_ID) ?: System.currentTimeMillis().toString()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure Channel exists
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Satellite Passes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you regarding upcoming satellite passes"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when notification clicked (opens main activity)
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alertId.hashCode(),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(alertId.hashCode(), notification)
    }
}
