package com.gollan.fadesleeptimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gollan.fadesleeptimer.util.BrainDumpNotificationHelper

class BrainDumpReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_WARNING_2H = "com.gollan.fadesleeptimer.action.WARNING_2H"
        const val ACTION_WARNING_15M = "com.gollan.fadesleeptimer.action.WARNING_15M"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_WARNING_2H -> {
                BrainDumpNotificationHelper.showWarningNotification(context, "2 hours")
            }
            ACTION_WARNING_15M -> {
                BrainDumpNotificationHelper.showWarningNotification(context, "15 minutes")
            }
        }
    }
}
