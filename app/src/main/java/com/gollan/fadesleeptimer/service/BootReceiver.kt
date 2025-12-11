package com.gollan.fadesleeptimer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if feature is enabled
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("open_on_charge_enabled", false)
            
            if (enabled) {
                NightstandJobService.scheduleWait(context)
            }
        }
    }
}
