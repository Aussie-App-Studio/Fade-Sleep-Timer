package com.gollan.fadesleeptimer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gollan.fadesleeptimer.MainActivity
import com.gollan.fadesleeptimer.R

object BrainDumpNotificationHelper {
    const val CHANNEL_ID = "brain_dump_reminders"
    const val NOTIFICATION_ID = 2001
    const val EXTRA_OPEN_BRAIN_DUMP = "open_brain_dump"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.brain_dump_channel_name)
            val descriptionText = context.getString(R.string.brain_dump_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showWarningNotification(context: Context, timeLeftString: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Open App Action
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_OPEN_BRAIN_DUMP, true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            openIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome) // Using the existing asset
            .setContentTitle(context.getString(R.string.brain_dump_title_notif))
            .setContentText(context.getString(R.string.brain_dump_text_format, timeLeftString))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
