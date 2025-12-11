package com.gollan.fadesleeptimer.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import com.gollan.fadesleeptimer.util.BedtimeNotificationHelper
import java.util.Calendar

class BedtimeReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.gollan.fadesleeptimer.ACTION_TRIGGER_REMINDER"
        const val ACTION_EXTEND = "com.gollan.fadesleeptimer.ACTION_EXTEND_BEDTIME"
        const val ACTION_DISMISS = "com.gollan.fadesleeptimer.ACTION_DISMISS_BEDTIME"
        const val REQUEST_CODE_TRIGGER = 100
        const val REQUEST_CODE_RETRY = 101
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TRIGGER_REMINDER -> handleTrigger(context)
            ACTION_EXTEND -> handleExtend(context)
            ACTION_DISMISS -> handleDismiss(context)
        }
    }

    private fun handleTrigger(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.isMusicActive) {
            // Music is playing, schedule retry in 5 minutes
            scheduleRetry(context)
        } else {
            // Show notification
            BedtimeNotificationHelper.showBedtimeNotification(context)
        }
    }

    private fun scheduleRetry(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BedtimeReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RETRY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun handleExtend(context: Context) {
        // Dismiss current notification
        BedtimeNotificationHelper.cancelNotification(context)

        // Schedule new reminder in 15 minutes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BedtimeReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TRIGGER, // Use main trigger code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun handleDismiss(context: Context) {
        BedtimeNotificationHelper.cancelNotification(context)
    }
}
