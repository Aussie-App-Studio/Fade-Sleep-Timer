package com.gollan.fadesleeptimer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gollan.fadesleeptimer.R
import com.gollan.fadesleeptimer.receiver.BedtimeReceiver

object BedtimeNotificationHelper {
    const val CHANNEL_ID = "bedtime_reminder_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.bedtime_reminder_title_notif)
            val descriptionText = context.getString(R.string.bedtime_reminder_desc_notif)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showBedtimeNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Dismiss Action
        val dismissIntent = Intent(context, BedtimeReceiver::class.java).apply {
            action = BedtimeReceiver.ACTION_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Extend Action
        val extendIntent = Intent(context, BedtimeReceiver::class.java).apply {
            action = BedtimeReceiver.ACTION_EXTEND
        }
        val extendPendingIntent = PendingIntent.getBroadcast(
            context, 2, extendIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Alarm clock icon
            .setContentTitle(context.getString(R.string.bedtime_reminder_title_notif))
            .setContentText(context.getString(R.string.bedtime_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.extend_15m_button), extendPendingIntent)
            .addAction(0, context.getString(R.string.dismiss_button), dismissPendingIntent)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
