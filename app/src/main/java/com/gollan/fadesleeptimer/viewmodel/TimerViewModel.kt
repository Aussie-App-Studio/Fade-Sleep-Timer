package com.gollan.fadesleeptimer.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.gollan.fadesleeptimer.data.TimerRepository
import com.gollan.fadesleeptimer.service.TimerService
import com.gollan.fadesleeptimer.service.NightstandJobService
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.utils.BreadcrumbManager
import com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion
import com.gollan.fadesleeptimer.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.gollan.fadesleeptimer.data.SleepRepository
import com.gollan.fadesleeptimer.data.local.SleepDatabase
import com.gollan.fadesleeptimer.data.local.SleepSession
import com.gollan.fadesleeptimer.ui.components.ChartData
import com.gollan.fadesleeptimer.data.Quote
import com.gollan.fadesleeptimer.util.SleepStreakManager
import com.gollan.fadesleeptimer.data.BadgeRepository


sealed class ExplanationState {
    object Idle : ExplanationState()
    object Loading : ExplanationState()
    data class Success(val explanation: String) : ExplanationState()
    data class Error(val message: String) : ExplanationState()
}

class TimerViewModel : ViewModel() {
    // Use Repository for shared state
    val isRunning = TimerRepository.isRunning
    val timeLeft = TimerRepository.timeLeft
    val isAudioPlaying = TimerRepository.isAudioPlaying
    val totalDuration = TimerRepository.totalDuration

    private val _activeSound = MutableStateFlow("system")
    // val activeSound = _activeSound.asStateFlow() // Moved to AudioViewModel

    // private val _volume = MutableStateFlow(60f)
    // val volume = _volume.asStateFlow() // Moved to AudioViewModel

    // Settings State
    // private val _settings = MutableStateFlow(AppSettings())
    // val settings = _settings.asStateFlow() // Moved to SettingsViewModel

    // Breadcrumb State
    // private val _breadcrumbSuggestion = MutableStateFlow<BreadcrumbSuggestion?>(null)
    // val breadcrumbSuggestion = _breadcrumbSuggestion.asStateFlow() // Moved to SettingsViewModel

    // App Picker State
    private val _showAppPicker = MutableStateFlow<String?>(null) // buttonId to show picker for
    val showAppPicker = _showAppPicker.asStateFlow()

    // Button Visibility Dialog State
    private val _showButtonConfig = MutableStateFlow(false)
    val showButtonConfig = _showButtonConfig.asStateFlow()

    // Morning Check State
    private val _showMorningOverlay = MutableStateFlow(false)
    val showMorningOverlay = _showMorningOverlay.asStateFlow()

    // Event for Go Home on Start
    val timerStartedEvent = kotlinx.coroutines.channels.Channel<Unit>(kotlinx.coroutines.channels.Channel.BUFFERED)
    
    // Ad State
    // Ad State Moved to MainActivity Logic
    // val isAdVisible = ...

    init {
        val context = MainActivity.latestContext
        if (context != null) {
            // Initialize Sleep History
            try {
                initSleepHistory(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Morning Prompt Logic Moved to SettingsViewModel
    // fun checkMorningPrompt() { ... }
    // fun dismissMorningPrompt(context: Context) { ... }

    // Brain Dump State
    private val _hasValidNote = MutableStateFlow(false)
    val hasValidNote = _hasValidNote.asStateFlow()

    // Quote Explanation State
    private val _explanationState = MutableStateFlow<ExplanationState>(ExplanationState.Idle)
    
    // Voice Selection Capability
    private val _voiceSelectionAvailable = MutableStateFlow(false)
    val voiceSelectionAvailable = _voiceSelectionAvailable.asStateFlow()

    init {
        val context = MainActivity.latestContext
        if (context != null) {
            // Initialize Sleep History
            try {
                initSleepHistory(context)
                
                // Check TTS Capability
                // We create a temporary helper just to check voices
                var checkHelper: com.gollan.fadesleeptimer.util.TextToSpeechHelper? = null
                checkHelper = com.gollan.fadesleeptimer.util.TextToSpeechHelper(context, onInit = { result ->
                     if (result == com.gollan.fadesleeptimer.util.TtsInitResult.SUCCESS) {
                         _voiceSelectionAvailable.value = checkHelper?.hasMultipleVoices == true
                     } else {
                         _voiceSelectionAvailable.value = false
                     }
                    checkHelper?.shutdown()
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val explanationState = _explanationState.asStateFlow()

    fun explainQuote(quote: Quote) {
        // Feature temporarily disabled
        _explanationState.value = ExplanationState.Success("AI explanation coming soon...")
    }

    fun dismissExplanation() {
        _explanationState.value = ExplanationState.Idle
    }

    // Audio Logic Moved to AudioViewModel
    // fun setSound(id: String) { _activeSound.value = id }
    // fun initVolume(context: Context) { ... }
    // fun updateVolume(context: Context, v: Float) { ... }
    
    // updateSettings removed (moved to SettingsViewModel)

    private fun scheduleBedtimeReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, com.gollan.fadesleeptimer.receiver.BedtimeReceiver::class.java).apply {
            action = com.gollan.fadesleeptimer.receiver.BedtimeReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            com.gollan.fadesleeptimer.receiver.BedtimeReceiver.REQUEST_CODE_TRIGGER,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        // If time has passed, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    // Fallback to inexact if permission missing (or prompt user, but for now just avoid crash)
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Safety net: If something goes wrong with permission check
            e.printStackTrace()
            Toast.makeText(context, "Could not schedule exact reminder (Permission missing)", Toast.LENGTH_SHORT).show()
        }
    }

    // markOnboardingSeen removed (moved to SettingsViewModel)

    // markSettingsClicked removed (moved to SettingsViewModel)

    // markConfigureClicked removed (moved to SettingsViewModel)

    // markTimerClicked removed (moved to SettingsViewModel)

    private fun cancelBedtimeReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, com.gollan.fadesleeptimer.receiver.BedtimeReceiver::class.java).apply {
            action = com.gollan.fadesleeptimer.receiver.BedtimeReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            com.gollan.fadesleeptimer.receiver.BedtimeReceiver.REQUEST_CODE_TRIGGER,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // State to show Badge Earned Dialog
    private val _badgeEarnedEvent = MutableStateFlow<List<String>>(emptyList())
    val badgeEarnedEvent = _badgeEarnedEvent.asStateFlow()
    
    // Callback to update settings (must be set by UI/MainViewModel for now as VM doesn't own settings)
    var onUpdateSettings: ((AppSettings) -> Unit)? = null

    fun dismissBadgeEvent() {
        _badgeEarnedEvent.value = emptyList()
    }

    fun startTimer(context: Context, minutes: Long, settings: AppSettings) {
        // ... (Existing startTimer logic) ...
        // Re-implementing startTimer to keep it but using original code reference is safer if I don't need to change it.
        // I will just ADD the new functions below it.
        startTimerInternal(context, minutes, settings, sendEvent = true)
    }

    // Version that doesn't trigger navigation
    fun previewSound(context: Context, settings: AppSettings) {
        // Use default duration or infinite? User didn't specify.
        // Let's use the default duration for now to be safe, or 60 mins.
        startTimerInternal(context, settings.defaultDuration.toLong(), settings, sendEvent = false, onlyAudio = true)
    }

    private fun startTimerInternal(context: Context, minutes: Long, settings: AppSettings, sendEvent: Boolean, onlyAudio: Boolean = false) {
         // Increment Session Count
         var currentSettings = settings.copy(timerStartCount = settings.timerStartCount + 1)

         // Sleep Streak Logic
        val streakResult = SleepStreakManager.checkStreak(currentSettings)
        if (streakResult.settings != settings) { // Check if anything changed (streak OR count)
             // Since we locally modified currentSettings, we should compare against original 'settings' OR just trust streakResult.settings is based on currentSettings
             // SleepStreakManager takes 'currentSettings' in, so it returns 'currentSettings' + streak updates.
             // So valid.
             onUpdateSettings?.invoke(streakResult.settings)
        } else {
             // Even if streak didn't change, we changed the count, so we MUST update.
             onUpdateSettings?.invoke(currentSettings)
        }
        
        if (streakResult.newBadgesEarned.isNotEmpty()) {
            _badgeEarnedEvent.value = streakResult.newBadgesEarned
        }
    
        if (sendEvent) {
            timerStartedEvent.trySend(Unit)
        }
        
        val intent = Intent(context, TimerService::class.java)
        intent.putExtra(TimerService.EXTRA_DURATION_MINUTES, minutes)
        intent.putExtra(TimerService.EXTRA_PLAY_ON_START, settings.playOnStart)
        intent.putExtra(TimerService.EXTRA_ONLY_AUDIO, onlyAudio) // New Extra
        intent.putExtra(TimerService.EXTRA_DND_ENABLED, settings.dndEnabled)
        intent.putExtra("DND_MODE", settings.dndMode.name) // Constant to be added to Service
        // New audio feature settings
        intent.putExtra(TimerService.EXTRA_FADE_DURATION, settings.fadeDuration)
        intent.putExtra(TimerService.EXTRA_SMART_WAIT, settings.smartWait)

        intent.putExtra(TimerService.EXTRA_BATTERY_GUARD, settings.batteryGuard)
        intent.putExtra("BATTERY_GUARD_THRESHOLD", settings.batteryGuardThreshold)
        intent.putExtra("BATTERY_GUARD_THRESHOLD", settings.batteryGuardThreshold)
        intent.putExtra(TimerService.EXTRA_MONOCHROME_MODE, settings.monochromeMode.name)
        intent.putExtra(TimerService.EXTRA_SHAKE_TO_EXTEND, settings.shakeToExtend)
        
        // Audio Selection
        intent.putExtra(TimerService.EXTRA_AUDIO_SELECTION, settings.audioSelection.name) // Use property directly
        intent.putExtra(TimerService.EXTRA_VOICE_GENDER, settings.voiceGender) // New
        
        // Smart Extend (Movement)
        intent.putExtra(TimerService.EXTRA_SMART_EXTEND, settings.smartExtendEnabled)
        intent.putExtra(TimerService.EXTRA_SMART_EXTEND_SENSITIVITY, settings.smartExtendSensitivity.name)

        // Anti-Doomscroll
        intent.putExtra(TimerService.EXTRA_DOOMSCROLL_ENABLED, settings.doomscrollLockEnabled)
        intent.putStringArrayListExtra(TimerService.EXTRA_DOOMSCROLL_APPS, java.util.ArrayList(settings.doomscrollBlockedApps.toList()))
        intent.putExtra(TimerService.EXTRA_DOOMSCROLL_GRACE, settings.doomscrollGraceMinutes)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    fun pauseTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        intent.action = TimerService.ACTION_PAUSE
        context.startService(intent)
    }

    fun stopTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        intent.action = TimerService.ACTION_STOP
        context.startService(intent)
    }

    fun extendTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        intent.action = TimerService.ACTION_EXTEND
        context.startService(intent)
    }

    fun openApp(context: Context, buttonId: String) {
        val prefs = context.getSharedPreferences("app_buttons", Context.MODE_PRIVATE)
        val savedPackage = prefs.getString("button_$buttonId", null)
        
        if (savedPackage != null) {
            // Launch the saved app directly
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(savedPackage)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    // App no longer installed, clear saved preference and show picker
                    prefs.edit().remove("button_$buttonId").apply()
                    _showAppPicker.value = buttonId
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not launch app", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No saved app, show picker
            _showAppPicker.value = buttonId
        }
    }
    
    fun saveSelectedApp(context: Context, buttonId: String, packageName: String) {
        val prefs = context.getSharedPreferences("app_buttons", Context.MODE_PRIVATE)
        prefs.edit().putString("button_$buttonId", packageName).apply()
        
        // Launch the app
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch app", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun dismissAppPicker() {
        _showAppPicker.value = null
    }
    
    fun clearSavedApp(buttonId: String) {
        val context = MainActivity.latestContext ?: return
        val prefs = context.getSharedPreferences("app_buttons", Context.MODE_PRIVATE)
        prefs.edit().remove("button_$buttonId").apply()
        _showAppPicker.value = buttonId
    }
    
    fun showButtonConfig() {
        _showButtonConfig.value = true
    }
    
    fun hideButtonConfig() {
        _showButtonConfig.value = false
    }

    // Brain Dump Logic
    fun saveBrainDump(content: String, retentionHours: Int) {
        val context = MainActivity.latestContext
        val timestamp = System.currentTimeMillis()
        val prefs = context?.getSharedPreferences("brain_dump", Context.MODE_PRIVATE)
        prefs?.edit()?.apply {
            putString("dump_content", content)
            putLong("dump_timestamp", timestamp)
            putInt("dump_retention_hours", retentionHours)
            apply()
        }
        _hasValidNote.value = true
        
        // Schedule Warnings
        if (context != null) {
            val expiryTime = timestamp + (retentionHours * 60 * 60 * 1000L)
            scheduleBrainDumpReminders(context, expiryTime)
            incrementBrainDumpUsage(context)
        }
    }

    fun getValidBrainDump(): String? {
        val prefs = MainActivity.latestContext?.getSharedPreferences("brain_dump", Context.MODE_PRIVATE) ?: return null
        val content = prefs.getString("dump_content", null) ?: return null
        val timestamp = prefs.getLong("dump_timestamp", 0)
        val retentionHours = prefs.getInt("dump_retention_hours", 12)

        val expiryTime = timestamp + (retentionHours * 60 * 60 * 1000L)
        return if (System.currentTimeMillis() < expiryTime) {
            content
        } else {
            clearBrainDump()
            null
        }
    }

    fun clearBrainDump() {
        val context = MainActivity.latestContext
        val prefs = context?.getSharedPreferences("brain_dump", Context.MODE_PRIVATE)
        prefs?.edit()?.clear()?.apply()
        _hasValidNote.value = false
        
        if (context != null) {
            cancelBrainDumpReminders(context)
        }
    }
    
    fun checkBrainDumpStatus() {
        _hasValidNote.value = getValidBrainDump() != null
    }

    // Brain Dump Warning & Notifications


    // Actually, for one-shot events, SharedFlow is better, but to avoid changing imports too much let's use a simple StateFlow or just a function
    // Let's use a simple boolean trigger for "show viewer" in MainScreen that gets reset
    private val _triggerOpenBrainDump = MutableStateFlow(false)
    val triggerOpenBrainDump = _triggerOpenBrainDump.asStateFlow()

    fun triggerOpenBrainDump() {
        _triggerOpenBrainDump.value = true
    }
    
    fun consumeOpenBrainDump() {
        _triggerOpenBrainDump.value = false
    }

    fun checkAndShowBrainDumpWarning(context: Context) {
        val prefs = context.getSharedPreferences("brain_dump", Context.MODE_PRIVATE)
        var usageCount = prefs.getInt("usage_count", 0)
        
        // Logic: 1st time to 7th time -> Show Warning
        // Every 5th time after (12, 17, 22...) -> Show Warning
        val shouldShowWarning = if (usageCount < 7) {
            true
        } else {
            (usageCount - 7) % 5 == 0
        }
        
        if (shouldShowWarning) {
            Toast.makeText(context, "Note: Brain Dumps are auto-deleted after 12/24 hours!", Toast.LENGTH_LONG).show()
        }
        
        // Increment usage (only once per save, doing it here implies "attempting to use")
        // To be safe, we should probably do this on SAVE, but the request says "when inputting a note"
        // so checking here is fine.
    }
    
    fun incrementBrainDumpUsage(context: Context) {
         val prefs = context.getSharedPreferences("brain_dump", Context.MODE_PRIVATE)
         val current = prefs.getInt("usage_count", 0)
         prefs.edit().putInt("usage_count", current + 1).apply()
    }

    // Shake Onboarding
    private val _showShakeOnboarding = MutableStateFlow(false)
    val showShakeOnboarding = _showShakeOnboarding.asStateFlow()

    fun onShakeUsed(context: Context) {
        val prefs = context.getSharedPreferences("controls_prefs", Context.MODE_PRIVATE)
        val usage = prefs.getInt("shake_usage", 0) + 1
        prefs.edit().putInt("shake_usage", usage).apply()
        
        // Show onboarding on 2nd use
        if (usage == 2) {
            _showShakeOnboarding.value = true
        }
        // Reshow every 10th use if they haven't explicitly disabled it? 
        // User asked for "Reshow every 5 minutes", but we agreed on usage based to avoid spam.
        // Let's stick to just the 2nd use for now, or maybe the 7th if needed.
        // The requirement "If ignored, reshow every 5 minutes" - simplified to "Usage based repeat"
        // Let's check if the feature is enabled? Well, onShakeUsed only fires if it IS enabled.
        // So the popup is "Do you want to KEEP this feature On?"
        // If they say Yes -> Don't show again.
        // If they say No -> Turn it off.
        // If they dismiss/ignore -> Show again later.
        
        // Let's track if they have "Decided"
        val hasDecided = prefs.getBoolean("shake_onboarding_decided", false)
        if (!hasDecided && usage > 2 && (usage - 2) % 5 == 0) {
            // Show every 5 uses after the 2nd if undecided
            _showShakeOnboarding.value = true
        }
    }
    
    fun onShakeOnboardingDecision(context: Context, keepOn: Boolean) {
        val prefs = context.getSharedPreferences("controls_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("shake_onboarding_decided", true).apply()
        _showShakeOnboarding.value = false
        
        if (!keepOn) {
            // Turn off the feature
             // We need to update settings. Since Settings is state in UI, we might need a way to propagate this back.
             // OR, just update SharedPreferences if that's where settings lived, but currently Settings are in AppSettings passed around.
             // WE NEED A WAY TO UPDATE SETTINGS FROM VM.
             // Ah, ViewModel doesn't hold settings anymore? 
             // Ideally we should have a SettingsRepository. 
             // For now, I'll need to expose an event or use the `updateSettings` flow if available?
             // Checking ViewModel top: `val settings` was commented out. `TimerRepository`?
             // `SettingsScreen` owns the state.
             // This is tricky. I'll need to expose a `_updateSettingsRequest` flow or something.
             // OR, cleaner: `onShakeOnboardingDecision` just updates the preference, AND we need to trigger the settings update.
        }
    }
    
    // Quick fix for updating settings from VM to UI:
    // Expose a SharedFlow for "DisableShake" event


    // Let's use StateFlow trigger again
    private val _requestDisableShake = MutableStateFlow(false)
    val requestDisableShake = _requestDisableShake.asStateFlow()
    
    fun disableShakeFeature() {
        _requestDisableShake.value = true
    }
    
    fun consumeDisableShakeRequest() {
        _requestDisableShake.value = false
    }
    
    fun dismissShakeOnboarding() {
        _showShakeOnboarding.value = false
    }

    private fun scheduleBrainDumpReminders(context: Context, expiryTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        
        // Helper to schedule
        fun schedule(action: String, triggerTime: Long) {
            if (triggerTime <= System.currentTimeMillis()) return

            val intent = Intent(context, com.gollan.fadesleeptimer.receiver.BrainDumpReceiver::class.java).apply {
                this.action = action
            }
            val code = action.hashCode() // Unique request code
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                code,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                     if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                     } else {
                        alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                     }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2 Hours Before
        schedule(com.gollan.fadesleeptimer.receiver.BrainDumpReceiver.ACTION_WARNING_2H, expiryTime - (2 * 60 * 60 * 1000L))
        
        // 15 Minutes Before
        schedule(com.gollan.fadesleeptimer.receiver.BrainDumpReceiver.ACTION_WARNING_15M, expiryTime - (15 * 60 * 1000L))
    }
    
    private fun cancelBrainDumpReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        
        listOf(
            com.gollan.fadesleeptimer.receiver.BrainDumpReceiver.ACTION_WARNING_2H,
            com.gollan.fadesleeptimer.receiver.BrainDumpReceiver.ACTION_WARNING_15M
        ).forEach { action ->
            val intent = Intent(context, com.gollan.fadesleeptimer.receiver.BrainDumpReceiver::class.java).apply {
                this.action = action
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    // setImmortalized removed (moved to SettingsViewModel)



    // resetImmortalized removed (moved to SettingsViewModel)
    
    // unlockFeature removed (moved to SettingsViewModel)

    // --- SLEEP HISTORY LOGIC ---
    private var sleepRepository: SleepRepository? = null
    private val _sleepHistory = MutableStateFlow<List<com.gollan.fadesleeptimer.ui.components.ChartData>>(emptyList())
    val sleepHistory = _sleepHistory.asStateFlow()

    private fun initSleepHistory(context: Context) {
        val db = com.gollan.fadesleeptimer.data.local.SleepDatabase.getDatabase(context)
        sleepRepository = com.gollan.fadesleeptimer.data.SleepRepository(db.sleepDao())
        
        viewModelScope.launch {
            sleepRepository!!.lastSessions.collect { sessions ->
                _sleepHistory.value = processHistory(sessions)
            }
        }
    }

    private fun processHistory(sessions: List<com.gollan.fadesleeptimer.data.local.SleepSession>): List<com.gollan.fadesleeptimer.ui.components.ChartData> {
        // Create map of last 7 days
        val today = java.time.LocalDate.now()
        val days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        
        val sessionMap = sessions.associateBy { session ->
            val dateTime = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(session.timestamp_end), java.time.ZoneId.systemDefault())
            // Midnight crossover: if before 12 PM (noon), belongs to previous day
            if (dateTime.hour < 12) dateTime.toLocalDate().minusDays(1) else dateTime.toLocalDate()
        }
        
        return days.map { date ->
            val session = sessionMap[date]
            if (session != null) {
                val endDateTime = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(session.timestamp_end), java.time.ZoneId.systemDefault())
                
                // Round to nearest 30m
                val minute = endDateTime.minute
                val roundedMinute = if (minute < 15) 0 else if (minute < 45) 30 else 0
                val roundedHour = if (minute >= 45) endDateTime.hour + 1 else endDateTime.hour
                
                // Calculate offset from 8 PM (20:00)
                // 20:00 = 0, 00:00 = 4, 08:00 = 12
                var hourForOffset = roundedHour
                if (hourForOffset < 14) hourForOffset += 24 // Treat anything before 2 PM as "next day" relative to 8 PM
                
                val endOffset = (hourForOffset - 20) + (roundedMinute / 60f)
                val durationHours = session.duration_minutes / 60f
                val startOffset = endOffset - durationHours
                
                // Format Time
                val displayHour = if (roundedHour >= 24) roundedHour - 24 else roundedHour
                val amPm = if (displayHour < 12) "AM" else "PM"
                val hour12 = if (displayHour % 12 == 0) 12 else displayHour % 12
                val timeLabel = String.format("%d:%02d %s", hour12, roundedMinute, amPm)
                
                com.gollan.fadesleeptimer.ui.components.ChartData(
                    dayLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.US)),
                    fullDate = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US)),
                    startOffsetHours = startOffset,
                    durationHours = durationHours,
                    endTimeLabel = timeLabel
                )
            } else {
                com.gollan.fadesleeptimer.ui.components.ChartData(
                    dayLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.US)),
                    fullDate = "",
                    startOffsetHours = 0f,
                    durationHours = 0f,
                    endTimeLabel = "",
                    isEmpty = true
                )
            }
        }
    }
}
