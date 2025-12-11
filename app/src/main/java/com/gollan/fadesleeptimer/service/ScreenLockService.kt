package com.gollan.fadesleeptimer.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*

class ScreenLockService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var gracePeriodJob: Job? = null
    private var currentBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ScreenLockService connected")
        
        // Configure the service - using config file mostly now
        // Store that the service is running
        instance = this
        
        // Ensure we listen to window changes
        val info = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val packageName = event.packageName?.toString() ?: return
        
        // Logic:
        // 1. Check if Timer is Running (via TimerService binding or simplistic check if process is alive is hard, 
        //    so we might need a repository/shared pref check. For simplicity, we'll check a static flag on TimerService)
        // 2. Check if Doomscroll Lock is Enabled in Prefs (Repository)
        
        // Accessing Repository within Service (Simplified for minimal DI friction)
        // In a real app we'd inject this, but here we can instantiate or use a singleton.
        // HOWEVER, accessing DataStore from an AccessibilityService can be tricky due to context.
        // We will read SharedPreferences directly for performance/simplicity in this specific high-frequency service.
        
        if (!com.gollan.fadesleeptimer.service.TimerService.isRunning) {
             cancelGracePeriod()
             return
        }

        // Removed direct preference check to avoid dependency issues.
        // Relying on TimerService static state which is updated by MainActivity/Service start.
        // val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        // Or specific DataStore name if using Proto DataStore...
        // Assuming we are saving these to standard shared prefs or we need to bridge it.
        // Since the AppSettings are in DataStore, we might need a quick helper or observer.
        
        // CRITICAL: Reading DataStore synchronously in AccessibilityEvent is bad.
        // Ideally, MainActivity pushes updates to a static config object or SharedPrefs.
        // For this implementation, let's assume `TimerService` holds the active "Lock Config" 
        // and exposes it statically for this service to read cheaply.
        
        if (!TimerService.doomscrollLockEnabled) return
        
        if (TimerService.doomscrollBlockedApps.contains(packageName)) {
            // User entered a blocked app
            if (currentBlockedPackage != packageName) {
                currentBlockedPackage = packageName
                startGracePeriod(TimerService.doomscrollGraceMinutes)
            }
        } else {
            // User left blocked app context
            // If they are on the Launcher, keep the grace period running? 
            // Usually, "Leave app" = "Stop doomscrolling", so we reset.
            if (!isLauncher(packageName)) {
                cancelGracePeriod()
            }
        }
    }
    
    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun startGracePeriod(minutes: Int) {
        if (gracePeriodJob?.isActive == true) return // Already running
        
        val effectiveMinutes = if (minutes < 0) 0 else minutes
        
        Log.d(TAG, "Starting grace period: $effectiveMinutes mins")
        
        gracePeriodJob = serviceScope.launch {
            if (effectiveMinutes > 0) {
                 Toast.makeText(applicationContext, "Sleep Focus: Closing in $effectiveMinutes mins", Toast.LENGTH_SHORT).show()
                 delay(effectiveMinutes * 60 * 1000L)
            }
            
            // Time's up!
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(applicationContext, "Sleep Focus Active", Toast.LENGTH_SHORT).show()
            currentBlockedPackage = null // Reset so re-entry triggers logic again
        }
    }

    private fun cancelGracePeriod() {
        if (gracePeriodJob?.isActive == true) {
            Log.d(TAG, "Cancelling grace period")
            gracePeriodJob?.cancel()
            gracePeriodJob = null
            currentBlockedPackage = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onInterrupt() {
        Log.d(TAG, "ScreenLockService interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    companion object {
        private const val TAG = "ScreenLockService"
        private var instance: ScreenLockService? = null

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = enabledServices.split(":")
            val packageName = context.packageName
            
            return colonSplitter.any { 
                it.contains(packageName) && it.contains(ScreenLockService::class.java.simpleName)
            }
        }

        fun lockScreen(): Boolean {
            val service = instance
            if (service == null) return false
            return service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
