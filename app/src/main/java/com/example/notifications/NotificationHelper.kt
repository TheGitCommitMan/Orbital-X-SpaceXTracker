package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.notifications.PassAlertReceiver

object NotificationHelper {

    fun schedulePassNotification(
        context: Context,
        passTimeMs: Long,
        title: String,
        message: String,
        alertId: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, PassAlertReceiver::class.java).apply {
            putExtra(PassAlertReceiver.EXTRA_TITLE, title)
            putExtra(PassAlertReceiver.EXTRA_MESSAGE, message)
            putExtra(PassAlertReceiver.EXTRA_ALERT_ID, alertId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Allows executing alarm even if system is in Idle/Doze mode
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    passTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    passTimeMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to non-exact if permission is not declared
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                passTimeMs,
                pendingIntent
            )
        }
    }

    fun cancelPassNotification(context: Context, alertId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PassAlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
