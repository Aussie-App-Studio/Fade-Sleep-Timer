package com.gollan.fadesleeptimer.service

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gollan.fadesleeptimer.MainActivity
import com.gollan.fadesleeptimer.ui.OpenOnChargeMode
import java.time.LocalTime

class NightstandJobService : JobService() {

    companion object {
        const val JOB_ID_WAIT_FOR_CHARGE = 1001
        const val JOB_ID_WAIT_FOR_UNPLUG = 1002

        fun scheduleWait(context: Context) {
            val componentName = ComponentName(context, NightstandJobService::class.java)
            val jobScheduler = context.getSystemService(JobScheduler::class.java)

            // Check if we are currently charging
            val batteryStatus: Intent? = android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }
            val status: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == android.os.BatteryManager.BATTERY_STATUS_FULL

            if (isCharging) {
                // Already charging, wait for unplug
                val info = JobInfo.Builder(JOB_ID_WAIT_FOR_UNPLUG, componentName)
                    .setRequiresCharging(false) // Wait until NOT charging
                    .setPersisted(true)
                    .build()
                jobScheduler.schedule(info)
            } else {
                // Not charging, wait for charge
                val info = JobInfo.Builder(JOB_ID_WAIT_FOR_CHARGE, componentName)
                    .setRequiresCharging(true) // Wait until charging
                    .setPersisted(true)
                    .build()
                jobScheduler.schedule(info)
            }
        }
        
        fun cancelAll(context: Context) {
            val jobScheduler = context.getSystemService(JobScheduler::class.java)
            jobScheduler.cancel(JOB_ID_WAIT_FOR_CHARGE)
            jobScheduler.cancel(JOB_ID_WAIT_FOR_UNPLUG)
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        when (params?.jobId) {
            JOB_ID_WAIT_FOR_CHARGE -> {
                // Charge Detected!
                handleChargeDetected()
                
                // Schedule next state: Wait for unplug
                val componentName = ComponentName(this, NightstandJobService::class.java)
                val info = JobInfo.Builder(JOB_ID_WAIT_FOR_UNPLUG, componentName)
                    .setRequiresCharging(false)
                    .setPersisted(true)
                    .build()
                getSystemService(JobScheduler::class.java).schedule(info)
            }
            JOB_ID_WAIT_FOR_UNPLUG -> {
                // Unplug Detected!
                
                // Schedule next state: Wait for charge
                val componentName = ComponentName(this, NightstandJobService::class.java)
                val info = JobInfo.Builder(JOB_ID_WAIT_FOR_CHARGE, componentName)
                    .setRequiresCharging(true)
                    .setPersisted(true)
                    .build()
                getSystemService(JobScheduler::class.java).schedule(info)
            }
        }
        return false // Work is done synchronously
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true // Reschedule if failed/stopped
    }

    private fun handleChargeDetected() {
        // Check Settings (via static holder in MainActivity for now, or SharedPrefs ideally)
        // Since this service runs in background, MainActivity might be dead.
        // We really should use SharedPreferences for robustness.
        // But for this task, let's try to read the static first, if null/default, we skip.
        // Actually, let's implement a simple SharedPrefs reader here to be safe.
        
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("open_on_charge_enabled", false)
        val modeStr = prefs.getString("open_on_charge_mode", "STRICT")
        val mode = try {
            OpenOnChargeMode.valueOf(modeStr ?: "STRICT")
        } catch (e: Exception) { OpenOnChargeMode.STRICT }

        if (enabled && isCurrentTimeInWindow(mode)) {
            triggerNotification()
        }
    }

    private fun isCurrentTimeInWindow(mode: OpenOnChargeMode): Boolean {
        if (mode == OpenOnChargeMode.ALWAYS) return true

        val now = LocalTime.now()
        val (start, end) = when (mode) {
            OpenOnChargeMode.STRICT -> Pair(LocalTime.of(20, 0), LocalTime.of(4, 0))
            OpenOnChargeMode.LATE -> Pair(LocalTime.of(1, 0), LocalTime.of(5, 0))
            OpenOnChargeMode.MORNING -> Pair(LocalTime.of(5, 0), LocalTime.of(10, 0))
            else -> return true
        }

        return if (start.isBefore(end)) {
            now.isAfter(start) && now.isBefore(end)
        } else {
            // Spans midnight
            now.isAfter(start) || now.isBefore(end)
        }
    }

    private fun triggerNotification() {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Try to launch directly if permission granted
        val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(this)
        } else {
            true
        }

        if (canDrawOverlays) {
            try {
                startActivity(contentIntent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nightstand Mode"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("nightstand_channel", name, importance)
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "nightstand_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Power Connected")
            .setContentText("Tap to open Sleep Timer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(30000)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(999, notification)
            }
        } else {
            NotificationManagerCompat.from(this).notify(999, notification)
        }
    }
}
