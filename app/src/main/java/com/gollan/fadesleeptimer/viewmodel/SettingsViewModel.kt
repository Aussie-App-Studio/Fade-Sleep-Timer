package com.gollan.fadesleeptimer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gollan.fadesleeptimer.MainActivity
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.utils.BreadcrumbManager
import com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion
import com.gollan.fadesleeptimer.service.NightstandJobService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.gollan.fadesleeptimer.data.BillingManager

class SettingsViewModel : ViewModel() {

    // Settings State
    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    // Breadcrumb State
    private val _breadcrumbSuggestion = MutableStateFlow<BreadcrumbSuggestion?>(null)
    val breadcrumbSuggestion = _breadcrumbSuggestion.asStateFlow()
    
    // UI Events
    private val _openSupportEvent = MutableStateFlow(false)
    val openSupportEvent = _openSupportEvent.asStateFlow()
    
    private val _openSettingsEvent = MutableStateFlow(false)
    val openSettingsEvent = _openSettingsEvent.asStateFlow()
    
    fun triggerOpenSupport() {
        _openSupportEvent.value = true
    }
    
    fun consumeOpenSupport() {
        _openSupportEvent.value = false
    }
    
    fun triggerOpenSettings() {
        _openSettingsEvent.value = true
    }
    
    fun consumeOpenSettings() {
        _openSettingsEvent.value = false
    }
    
    // Hall of Fame Welcome (after purchase)
    private val _showHallOfFameWelcome = MutableStateFlow(false)
    val showHallOfFameWelcome = _showHallOfFameWelcome.asStateFlow()
    
    fun triggerHallOfFameWelcome() {
        _showHallOfFameWelcome.value = true
    }
    
    fun consumeHallOfFameWelcome() {
        _showHallOfFameWelcome.value = false
    }

    // Billing
    private lateinit var billingManager: BillingManager

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        MainActivity.latestSettings = newSettings
        
        // Update Breadcrumb Suggestion
        _breadcrumbSuggestion.value = BreadcrumbManager.getNextSuggestion(newSettings)
        
        // Persist all settings
        val prefs = MainActivity.latestContext?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs?.edit()
            // Core & Controls
            ?.putBoolean("play_on_start", newSettings.playOnStart)
            ?.putBoolean("super_dim", newSettings.superDim)
            ?.putBoolean("dnd_enabled", newSettings.dndEnabled)
            ?.putString("dnd_mode", newSettings.dndMode.name)
            ?.putString("timer_presets", newSettings.timerPresets.joinToString(","))
            ?.putBoolean("face_down_start", newSettings.faceDownStart)
            ?.putBoolean("shake_to_extend", newSettings.shakeToExtend)
            ?.putBoolean("pocket_mode", newSettings.pocketMode)
            ?.putBoolean("gradual_dim_enabled", newSettings.gradualDimEnabled)
            // Audio & Playback
            ?.putString("audio_selection", newSettings.audioSelection.name)
            ?.putString("voice_gender", newSettings.voiceGender)
            ?.putInt("fade_duration", newSettings.fadeDuration)
            ?.putBoolean("volume_safety_cap", newSettings.volumeSafetyCap)
            ?.putBoolean("smart_wait", newSettings.smartWait)
            // System
            ?.putBoolean("airplane_mode_reminder", newSettings.airplaneModeReminder)
            ?.putBoolean("battery_guard", newSettings.batteryGuard)
            ?.putInt("battery_guard_threshold", newSettings.batteryGuardThreshold)
            ?.putBoolean("go_home_on_start", newSettings.goHomeOnStart)
            ?.putBoolean("open_on_charge_enabled", newSettings.openOnChargeEnabled)
            ?.putString("open_on_charge_mode", newSettings.openOnChargeMode.name)
            // Wellness & Personalization
            ?.putBoolean("prayer_reminder", newSettings.prayerReminder)
            ?.putBoolean("brain_dump_enabled", newSettings.brainDumpEnabled)
            ?.putBoolean("scripture_mode_enabled", newSettings.scriptureModeEnabled)
            ?.putString("scripture_topic", newSettings.scriptureTopic)
            ?.putBoolean("wisdom_quotes_enabled", newSettings.wisdomQuotesEnabled)
            ?.putBoolean("wisdom_quotes_peace_comfort", newSettings.wisdomQuotesPeaceComfort)
            ?.putBoolean("wisdom_quotes_god_glory", newSettings.wisdomQuotesGodGlory)
            ?.putBoolean("wisdom_quotes_gospel_grace", newSettings.wisdomQuotesGospelGrace)
            ?.putBoolean("wisdom_quotes_wisdom_living", newSettings.wisdomQuotesWisdomLiving)
            ?.putBoolean("sleep_calculator_enabled", newSettings.sleepCalculatorEnabled)
            ?.putBoolean("sleep_calculator_alarm_option", newSettings.sleepCalculatorAlarmOption)
            ?.putString("app_icon", newSettings.appIcon)
            // Breathing Guide
            ?.putBoolean("breathing_guide_enabled", newSettings.breathingGuideEnabled)
            ?.putBoolean("breathing_auto_start", newSettings.breathingAutoStart)
            ?.putString("breathing_pattern_id", newSettings.breathingPatternId)
            ?.putInt("breathing_duration_minutes", newSettings.breathingDurationMinutes)
            ?.putBoolean("breathing_vibration_enabled", newSettings.breathingVibrationEnabled)
            // Visuals & Display
            ?.putBoolean("analogue_clock", newSettings.analogueClock)
            ?.putString("monochrome_mode", newSettings.monochromeMode.name)
            ?.putBoolean("burn_in_protection", newSettings.burnInProtection)
            ?.putBoolean("has_interacted_with_monochrome", newSettings.hasInteractedWithMonochrome)
            ?.putString("theme", newSettings.theme)
            // Default Timer & Morning Check
            ?.putInt("default_duration", newSettings.defaultDuration)
            ?.putBoolean("morning_check_enabled", newSettings.morningCheckEnabled)
            ?.putBoolean("customize_default_timer", newSettings.customizeDefaultTimer)
            // Hall of Fame & Support
            ?.putBoolean("is_immortalized", newSettings.isImmortalized)
            ?.putString("hall_of_fame_name", newSettings.hallOfFameName)
            ?.putBoolean("is_rated", newSettings.isRated)
            ?.putBoolean("is_shared", newSettings.isShared)
            ?.putBoolean("has_checked_other_apps", newSettings.hasCheckedOtherApps)
            ?.putBoolean("visuals_unlocked", newSettings.visualsUnlocked)
            ?.putBoolean("is_sponsor", newSettings.isSponsor)
            ?.putBoolean("is_lego_purchased", newSettings.isLegoPurchased)
            // Premium Rewards Persistence
            ?.putBoolean("has_supporter_badge", newSettings.hasSupporterBadge)
            ?.putBoolean("show_supporter_badge", newSettings.showSupporterBadge)
            ?.putBoolean("has_review_badge", newSettings.hasReviewBadge)
            ?.putBoolean("has_visual_pack", newSettings.isVisualPackPurchased) // Persist Ownership
            ?.putBoolean("has_hall_of_fame_access", newSettings.isHallOfFamePurchased) // Persist Ownership
            ?.putBoolean("has_sponsor_access", newSettings.isSponsorPurchased) // Persist Ownership
            ?.putBoolean("is_premium_unlocked", newSettings.isPremiumUnlocked) // Persist Easter Egg
            // Hall of Fame One-Time Link
            ?.putBoolean("hall_of_fame_form_opened", newSettings.hallOfFameFormOpened)
            // Bedtime Reminder Persistence
            ?.putBoolean("bedtime_reminder_enabled", newSettings.bedtimeReminderEnabled)
            ?.putInt("bedtime_hour", newSettings.bedtimeHour)
            ?.putInt("bedtime_minute", newSettings.bedtimeMinute)
            // Button Visibility Persistence
            ?.putBoolean("configure_button_visible", newSettings.configureButtonVisible)
            ?.putBoolean("spotify_button_visible", newSettings.spotifyButtonVisible)
            ?.putBoolean("ytmusic_button_visible", newSettings.ytMusicButtonVisible)
            ?.putBoolean("audible_button_visible", newSettings.audibleButtonVisible)
            ?.putBoolean("youtube_button_visible", newSettings.youtubeButtonVisible)
            ?.putBoolean("youversion_button_visible", newSettings.youversionButtonVisible)
            ?.putBoolean("applemusic_button_visible", newSettings.appleMusicButtonVisible)
            ?.putBoolean("podcasts_button_visible", newSettings.podcastsButtonVisible)
            ?.putBoolean("amazonmusic_button_visible", newSettings.amazonMusicButtonVisible)
            ?.putBoolean("streaming_button_visible", newSettings.streamingButtonVisible)
            ?.putBoolean("calm_button_visible", newSettings.calmButtonVisible)
            ?.putBoolean("headspace_button_visible", newSettings.headspaceButtonVisible)
            // Onboarding & Interaction Persistence
            ?.putBoolean("has_seen_onboarding", newSettings.hasSeenOnboarding)
            ?.putInt("timer_start_count", newSettings.timerStartCount)
            ?.putStringSet("breadcrumbs_unlocked", newSettings.breadcrumbsUnlocked.toSet())
            ?.putBoolean("has_clicked_settings", newSettings.hasClickedSettings)
            ?.putBoolean("has_clicked_configure", newSettings.hasClickedConfigure)
            ?.putBoolean("has_clicked_timer", newSettings.hasClickedTimer)
            ?.putBoolean("has_started_timer", newSettings.hasStartedTimer)
            ?.putString("selected_apps", newSettings.selectedApps.joinToString(","))
            ?.putBoolean("has_seen_pre_timer_hint", newSettings.hasSeenPreTimerHint)
            // Anti-Doomscroll
            ?.putBoolean("doomscroll_lock_enabled", newSettings.doomscrollLockEnabled)
            ?.putStringSet("doomscroll_blocked_apps", newSettings.doomscrollBlockedApps.toSet())
            ?.putInt("doomscroll_grace_minutes", newSettings.doomscrollGraceMinutes)
            // Sleep Streaks & Gamification
            ?.putInt("sleep_streak_count", newSettings.sleepStreakCount)
            ?.putString("last_sleep_date", newSettings.lastSleepDate)
            ?.putStringSet("unlocked_badges", newSettings.unlockedBadges.toSet())
            // Smart Extend
            ?.putBoolean("smart_extend_enabled", newSettings.smartExtendEnabled)
            ?.putString("smart_extend_sensitivity", newSettings.smartExtendSensitivity.name)
            ?.apply()
        
        // Schedule/Cancel Job
        if (newSettings.openOnChargeEnabled) {
            MainActivity.latestContext?.let { 
                NightstandJobService.scheduleWait(it)
                // Enable Receiver
                try {
                    val receiver = android.content.ComponentName(it, com.gollan.fadesleeptimer.service.BootReceiver::class.java)
                    it.packageManager.setComponentEnabledSetting(
                        receiver,
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            MainActivity.latestContext?.let { 
                NightstandJobService.cancelAll(it)
                // Disable Receiver
                try {
                    val receiver = android.content.ComponentName(it, com.gollan.fadesleeptimer.service.BootReceiver::class.java)
                    it.packageManager.setComponentEnabledSetting(
                        receiver,
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Schedule/Cancel Bedtime Reminder
        MainActivity.latestContext?.let { ctx ->
            if (newSettings.bedtimeReminderEnabled) {
                scheduleBedtimeReminder(ctx, newSettings.bedtimeHour, newSettings.bedtimeMinute)
            } else {
                cancelBedtimeReminder(ctx)
            }
        }
    }

    private fun scheduleBedtimeReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = android.content.Intent(context, com.gollan.fadesleeptimer.receiver.BedtimeReceiver::class.java).apply {
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

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun cancelBedtimeReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = android.content.Intent(context, com.gollan.fadesleeptimer.receiver.BedtimeReceiver::class.java).apply {
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

    fun markOnboardingSeen() {
        updateSettings(_settings.value.copy(hasSeenOnboarding = true))
    }

    fun markSettingsClicked() {
        if (!_settings.value.hasClickedSettings) {
            updateSettings(_settings.value.copy(hasClickedSettings = true))
        }
    }

    fun markConfigureClicked() {
        if (!_settings.value.hasClickedConfigure) {
            updateSettings(_settings.value.copy(hasClickedConfigure = true))
        }
    }

    fun markTimerClicked() {
        if (!_settings.value.hasClickedTimer) {
            updateSettings(_settings.value.copy(hasClickedTimer = true))
        }
    }
    
    fun setImmortalized(name: String) {
        val newSettings = _settings.value.copy(
            isImmortalized = true,
            hallOfFameName = name
        )
        updateSettings(newSettings)
    }

    fun resetImmortalized() {
        val newSettings = _settings.value.copy(
            isImmortalized = false,
            hallOfFameName = null,
            hallOfFameFormOpened = false
        )
        updateSettings(newSettings)
    }
    
    fun setHallOfFameFormOpened() {
        val newSettings = _settings.value.copy(hallOfFameFormOpened = true)
        updateSettings(newSettings)
    }
    
    fun unlockFeature(tierId: String) {
        when(tierId) {
            "supporter" -> updateSettings(_settings.value.copy(hasSupporterBadge = true, showSupporterBadge = true))
            "rate" -> updateSettings(_settings.value.copy(isRated = true, hasReviewBadge = true))
            "share" -> updateSettings(_settings.value.copy(isShared = true, hasSupporterBadge = true, showSupporterBadge = true))
            "other_apps" -> updateSettings(_settings.value.copy(hasCheckedOtherApps = true))

            // When unlocking via purchase, set both PURCHASED and ACCESS
            "visual_pack", "visuals" -> {
                 val newSettings = _settings.value.copy(isVisualPackPurchased = true, hasVisualPack = true, visualsUnlocked = true)
                 updateSettings(newSettings)
            }
            "hall_of_fame_icons" -> {
                val newSettings = _settings.value.copy(isHallOfFamePurchased = true, hasHallOfFameAccess = true)
                updateSettings(newSettings)
            }
            "sponsor" -> {
                val newSettings = _settings.value.copy(isSponsorPurchased = true, hasSponsorAccess = true, isSponsor = true)
                updateSettings(newSettings)
            }
            "lego" -> updateSettings(_settings.value.copy(
                isLegoPurchased = true,
                hasVisualPack = true, visualsUnlocked = true, isVisualPackPurchased = true, // Lego implies ownership too? Let's say yes for simplicity or just access
                hasHallOfFameAccess = true, isHallOfFamePurchased = true,
                hasSponsorAccess = true, isSponsor = true, isSponsorPurchased = true
            ))
        }
    }





    fun purchaseProduct(activity: android.app.Activity, productId: String) {
        if (::billingManager.isInitialized) {
            billingManager.launchBillingFlow(activity, productId)
        }
    }

    init {
        val context = MainActivity.latestContext
        if (context != null) {
            // Initialize Billing
            billingManager = BillingManager(context, viewModelScope)
            billingManager.startConnection()
            
            // Listen for Purchases
            viewModelScope.launch {
                billingManager.purchasedProductIds.collect { purchases ->
                    if (purchases.contains("visual_pack")) unlockFeature("visual_pack")
                    if (purchases.contains("hall_of_fame_icons")) {
                        unlockFeature("hall_of_fame_icons")
                        // Trigger welcome dialog if user hasn't set their name yet
                        if (_settings.value.hallOfFameName == null) {
                            triggerHallOfFameWelcome()
                        }
                    }
                    if (purchases.contains("sponsor_feature")) unlockFeature("sponsor")
                    if (purchases.contains("lego_tier_ultimate")) {
                        unlockFeature("lego")
                        // Trigger welcome dialog if user hasn't set their name yet
                        if (_settings.value.hallOfFameName == null) {
                            triggerHallOfFameWelcome()
                        }
                    }
                }
            }
            val prefs = context.getSharedPreferences("app_stats", Context.MODE_PRIVATE)
            val saved = prefs.getInt("saved_minutes", 0)
            
            // Load Morning Check Data
            val lastSession = prefs.getLong("last_session_timestamp", 0)
            val lastExtended = prefs.getInt("last_session_extended_count", 0)
            val lastCheckedSession = prefs.getLong("last_checked_session_timestamp", 0)
            
            // Load Settings Preferences
            val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

            // Core & Controls
            val playOnStart = settingsPrefs.getBoolean("play_on_start", false)
            val superDim = settingsPrefs.getBoolean("super_dim", false)
            val dndEnabled = settingsPrefs.getBoolean("dnd_enabled", true)
            val dndModeName = settingsPrefs.getString("dnd_mode", "PRIORITY")
            val dndMode = try {
                com.gollan.fadesleeptimer.ui.DndMode.valueOf(dndModeName ?: "PRIORITY")
            } catch (e: Exception) { com.gollan.fadesleeptimer.ui.DndMode.PRIORITY }
            val timerPresetsStr = settingsPrefs.getString("timer_presets", null)
            val timerPresets = if (timerPresetsStr != null) {
                timerPresetsStr.split(",").mapNotNull { it.trim().toIntOrNull() }.ifEmpty { listOf(5, 10, 15, 30, 45, 60) }
            } else {
                listOf(5, 10, 15, 30, 45, 60)
            }
            val faceDownStart = settingsPrefs.getBoolean("face_down_start", false)
            val shakeToExtend = settingsPrefs.getBoolean("shake_to_extend", true)
            val pocketMode = settingsPrefs.getBoolean("pocket_mode", false)
            val gradualDimEnabled = settingsPrefs.getBoolean("gradual_dim_enabled", false)

            // Audio & Playback
            val audioSelectionName = settingsPrefs.getString("audio_selection", "SYSTEM")
            val audioSelection = try {
                com.gollan.fadesleeptimer.ui.AudioSelection.valueOf(audioSelectionName ?: "SYSTEM")
            } catch (e: Exception) { com.gollan.fadesleeptimer.ui.AudioSelection.SYSTEM }
            val voiceGender = settingsPrefs.getString("voice_gender", "DEFAULT") ?: "DEFAULT"
            val fadeDuration = settingsPrefs.getInt("fade_duration", 5)
            val volumeSafetyCap = settingsPrefs.getBoolean("volume_safety_cap", false)
            val smartWait = settingsPrefs.getBoolean("smart_wait", false)

            // System
            val airplaneModeReminder = settingsPrefs.getBoolean("airplane_mode_reminder", false)
            val batteryGuard = settingsPrefs.getBoolean("battery_guard", false)
            val batteryGuardThreshold = settingsPrefs.getInt("battery_guard_threshold", 15)
            val goHomeOnStart = settingsPrefs.getBoolean("go_home_on_start", false)
            val openOnChargeEnabled = settingsPrefs.getBoolean("open_on_charge_enabled", false)
            val openOnChargeModeName = settingsPrefs.getString("open_on_charge_mode", "STRICT")
            val openOnChargeMode = try {
                com.gollan.fadesleeptimer.ui.OpenOnChargeMode.valueOf(openOnChargeModeName ?: "STRICT")
            } catch (e: Exception) { com.gollan.fadesleeptimer.ui.OpenOnChargeMode.STRICT }

            // Wellness & Personalization
            val prayerReminder = settingsPrefs.getBoolean("prayer_reminder", true)
            val brainDumpEnabled = settingsPrefs.getBoolean("brain_dump_enabled", false)
            val scriptureModeEnabled = settingsPrefs.getBoolean("scripture_mode_enabled", true)
            val scriptureTopic = settingsPrefs.getString("scripture_topic", "Random") ?: "Random"
            val wisdomQuotesEnabled = settingsPrefs.getBoolean("wisdom_quotes_enabled", true)
            val wisdomQuotesPeaceComfort = settingsPrefs.getBoolean("wisdom_quotes_peace_comfort", true)
            val wisdomQuotesGodGlory = settingsPrefs.getBoolean("wisdom_quotes_god_glory", true)
            val wisdomQuotesGospelGrace = settingsPrefs.getBoolean("wisdom_quotes_gospel_grace", true)
            val wisdomQuotesWisdomLiving = settingsPrefs.getBoolean("wisdom_quotes_wisdom_living", true)
            val sleepCalculatorEnabled = settingsPrefs.getBoolean("sleep_calculator_enabled", false)
            val sleepCalculatorAlarmOption = settingsPrefs.getBoolean("sleep_calculator_alarm_option", false)
            val appIcon = settingsPrefs.getString("app_icon", "default") ?: "default"

            // Breathing Guide
            val breathingGuideEnabled = settingsPrefs.getBoolean("breathing_guide_enabled", false)
            val breathingAutoStart = settingsPrefs.getBoolean("breathing_auto_start", false)
            val breathingPatternId = settingsPrefs.getString("breathing_pattern_id", "deep") ?: "deep"
            val breathingDurationMinutes = settingsPrefs.getInt("breathing_duration_minutes", 1)
            val breathingVibrationEnabled = settingsPrefs.getBoolean("breathing_vibration_enabled", true)

            // Visuals & Display
            val analogueClock = settingsPrefs.getBoolean("analogue_clock", false)
            val monochromeModeName = settingsPrefs.getString("monochrome_mode", "OFF")
            val monochromeMode = try {
                com.gollan.fadesleeptimer.ui.MonochromeMode.valueOf(monochromeModeName ?: "OFF")
            } catch (e: Exception) { com.gollan.fadesleeptimer.ui.MonochromeMode.OFF }
            val burnInProtection = settingsPrefs.getBoolean("burn_in_protection", false)
            val hasInteractedWithMonochrome = settingsPrefs.getBoolean("has_interacted_with_monochrome", false)
            val theme = settingsPrefs.getString("theme", "default") ?: "default"

            // Default Duration & Morning Check
            val defaultDur = settingsPrefs.getInt("default_duration", 15)
            val checkEnabled = settingsPrefs.getBoolean("morning_check_enabled", true)
            val customizeEnabled = settingsPrefs.getBoolean("customize_default_timer", false)
            val isImmortalized = settingsPrefs.getBoolean("is_immortalized", false)
            val hofName = settingsPrefs.getString("hall_of_fame_name", null)
            val hallOfFameFormOpened = settingsPrefs.getBoolean("hall_of_fame_form_opened", false)
            
            // Load Support Tiers
            val isRated = settingsPrefs.getBoolean("is_rated", false)
            val isShared = settingsPrefs.getBoolean("is_shared", false)
            val hasCheckedOtherApps = settingsPrefs.getBoolean("has_checked_other_apps", false)
            val hasSupporterBadge = settingsPrefs.getBoolean("has_supporter_badge", false)
            val showSupporterBadge = settingsPrefs.getBoolean("show_supporter_badge", false)

            val visualsUnlocked = settingsPrefs.getBoolean("visuals_unlocked", false)
            val isSponsor = settingsPrefs.getBoolean("is_sponsor", false)
            val isLegoPurchased = settingsPrefs.getBoolean("is_lego_purchased", false)
            val isPremiumUnlocked = settingsPrefs.getBoolean("is_premium_unlocked", false)

            // Load PURCHASED state (Mapping old keys to new Ownership flags to preserve User Purchases)
            val isVisualPackPurchased = settingsPrefs.getBoolean("has_visual_pack", false)
            val isHallOfFamePurchased = settingsPrefs.getBoolean("has_hall_of_fame_access", false)
            val isSponsorPurchased = settingsPrefs.getBoolean("has_sponsor_access", false) // Note: is_sponsor was also used
            
            // Compute ACCESS state
            // Access = Purchased OR Lego OR PremiumUnlocked
            val effectiveVisualPack = isVisualPackPurchased || isLegoPurchased || isPremiumUnlocked
            val effectiveHallAccess = isHallOfFamePurchased || isLegoPurchased || isPremiumUnlocked
            val effectiveSponsor = isSponsorPurchased || isLegoPurchased || isPremiumUnlocked || isSponsor // Fallback to old boolean if needed? "is_sponsor" was boolean.
            
            val effectiveVisuals = if (effectiveVisualPack) true else visualsUnlocked // Keep visualsUnlocked for now just in case
            
            val hasReviewBadge = com.gollan.fadesleeptimer.util.ReviewManager.hasBadge(context) || settingsPrefs.getBoolean("has_review_badge", false)
            
            // Load Bedtime Reminder
            val bedtimeEnabled = settingsPrefs.getBoolean("bedtime_reminder_enabled", false)
            val bedtimeHour = settingsPrefs.getInt("bedtime_hour", 22)
            val bedtimeMinute = settingsPrefs.getInt("bedtime_minute", 0)
            
            // Load Button Visibility
            val configureVisible = settingsPrefs.getBoolean("configure_button_visible", true)
            val spotifyVisible = settingsPrefs.getBoolean("spotify_button_visible", false)
            val ytMusicVisible = settingsPrefs.getBoolean("ytmusic_button_visible", false)
            val audibleVisible = settingsPrefs.getBoolean("audible_button_visible", false)
            val youtubeVisible = settingsPrefs.getBoolean("youtube_button_visible", false)
            val youversionVisible = settingsPrefs.getBoolean("youversion_button_visible", false)
            val appleMusicVisible = settingsPrefs.getBoolean("applemusic_button_visible", false)
            val podcastsVisible = settingsPrefs.getBoolean("podcasts_button_visible", false)
            val amazonMusicVisible = settingsPrefs.getBoolean("amazonmusic_button_visible", false)
            val streamingVisible = settingsPrefs.getBoolean("streaming_button_visible", false)
            val calmVisible = settingsPrefs.getBoolean("calm_button_visible", false)
            val headspaceVisible = settingsPrefs.getBoolean("headspace_button_visible", false)

            // Load Onboarding & Breadcrumbs
            val hasSeenOnboarding = settingsPrefs.getBoolean("has_seen_onboarding", false)
            val appOpenCount = settingsPrefs.getInt("app_open_count", 0)
            val timerStartCount = settingsPrefs.getInt("timer_start_count", 0)
            val breadcrumbs = settingsPrefs.getStringSet("breadcrumbs_unlocked", emptySet())?.toSet() ?: emptySet()
            
            val hasClickedSettings = settingsPrefs.getBoolean("has_clicked_settings", false)
            val hasClickedConfigure = settingsPrefs.getBoolean("has_clicked_configure", false)
            val hasClickedTimer = settingsPrefs.getBoolean("has_clicked_timer", false)
            val hasStartedTimer = settingsPrefs.getBoolean("has_started_timer", false)
            val selectedAppsStr = settingsPrefs.getString("selected_apps", null)
            val selectedApps = if (!selectedAppsStr.isNullOrBlank()) selectedAppsStr.split(",") else emptyList()
            val hasSeenPreTimerHint = settingsPrefs.getBoolean("has_seen_pre_timer_hint", false)

            // Anti-Doomscroll
            val doomscrollLockEnabled = settingsPrefs.getBoolean("doomscroll_lock_enabled", false)
            val doomscrollBlockedApps = settingsPrefs.getStringSet("doomscroll_blocked_apps", emptySet())?.toSet() ?: emptySet()
            val doomscrollGraceMinutes = settingsPrefs.getInt("doomscroll_grace_minutes", 5)

            // Sleep Streaks & Gamification
            val sleepStreakCount = settingsPrefs.getInt("sleep_streak_count", 0)
            val lastSleepDate = settingsPrefs.getString("last_sleep_date", null)
            val unlockedBadges = settingsPrefs.getStringSet("unlocked_badges", emptySet())?.toSet() ?: emptySet()

            // Increment Open Count (Breadcrumbs)
            val newOpenCount = appOpenCount + 1
            settingsPrefs.edit().putInt("app_open_count", newOpenCount).apply()

            // Load Smart Extend
            val smartExtendEnabled = settingsPrefs.getBoolean("smart_extend_enabled", false)
            val smartExtendSensitivityName = settingsPrefs.getString("smart_extend_sensitivity", "MEDIUM")
            val smartExtendSensitivity = try {
                 com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.valueOf(smartExtendSensitivityName ?: "MEDIUM")
            } catch (e: Exception) { com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.MEDIUM }

            _settings.value = _settings.value.copy(
                playOnStart = playOnStart,
                superDim = superDim,
                dndEnabled = dndEnabled,
                dndMode = dndMode,
                timerPresets = timerPresets,
                faceDownStart = faceDownStart,
                shakeToExtend = shakeToExtend,
                pocketMode = pocketMode,
                gradualDimEnabled = gradualDimEnabled,
                audioSelection = audioSelection,
                voiceGender = voiceGender,
                fadeDuration = fadeDuration,
                volumeSafetyCap = volumeSafetyCap,
                smartWait = smartWait,
                airplaneModeReminder = airplaneModeReminder,
                batteryGuard = batteryGuard,
                batteryGuardThreshold = batteryGuardThreshold,
                goHomeOnStart = goHomeOnStart,
                openOnChargeEnabled = openOnChargeEnabled,
                openOnChargeMode = openOnChargeMode,
                prayerReminder = prayerReminder,
                brainDumpEnabled = brainDumpEnabled,
                scriptureModeEnabled = scriptureModeEnabled,
                scriptureTopic = scriptureTopic,
                wisdomQuotesEnabled = wisdomQuotesEnabled,
                wisdomQuotesPeaceComfort = wisdomQuotesPeaceComfort,
                wisdomQuotesGodGlory = wisdomQuotesGodGlory,
                wisdomQuotesGospelGrace = wisdomQuotesGospelGrace,
                wisdomQuotesWisdomLiving = wisdomQuotesWisdomLiving,
                sleepCalculatorEnabled = sleepCalculatorEnabled,
                sleepCalculatorAlarmOption = sleepCalculatorAlarmOption,
                appIcon = appIcon,
                breathingGuideEnabled = breathingGuideEnabled,
                breathingAutoStart = breathingAutoStart,
                breathingPatternId = breathingPatternId,
                breathingDurationMinutes = breathingDurationMinutes,
                breathingVibrationEnabled = breathingVibrationEnabled,
                analogueClock = analogueClock,
                monochromeMode = monochromeMode,
                burnInProtection = burnInProtection,
                hasInteractedWithMonochrome = hasInteractedWithMonochrome,
                theme = theme,
                savedMinutes = saved,
                lastSessionTimestamp = lastSession,
                lastSessionExtendedCount = lastExtended,
                lastCheckedSessionTimestamp = lastCheckedSession,
                defaultDuration = defaultDur,
                morningCheckEnabled = checkEnabled,
                isImmortalized = isImmortalized,
                hallOfFameName = hofName,
                hallOfFameFormOpened = hallOfFameFormOpened,
                isRated = isRated,
                isShared = isShared,
                hasCheckedOtherApps = hasCheckedOtherApps,
                hasSupporterBadge = hasSupporterBadge,
                showSupporterBadge = showSupporterBadge,

                visualsUnlocked = effectiveVisuals,
                isSponsor = effectiveSponsor,
                isLegoPurchased = isLegoPurchased,

                hasVisualPack = effectiveVisualPack,
                hasReviewBadge = hasReviewBadge,
                hasHallOfFameAccess = effectiveHallAccess,
                hasSponsorAccess = effectiveSponsor,
                // Ownership Flags
                isVisualPackPurchased = isVisualPackPurchased,
                isHallOfFamePurchased = isHallOfFamePurchased,
                isSponsorPurchased = isSponsorPurchased,
                isPremiumUnlocked = isPremiumUnlocked, // Easter Egg State
                
                bedtimeReminderEnabled = bedtimeEnabled,
                bedtimeHour = bedtimeHour,
                bedtimeMinute = bedtimeMinute,
                configureButtonVisible = configureVisible,
                spotifyButtonVisible = spotifyVisible,
                ytMusicButtonVisible = ytMusicVisible,
                audibleButtonVisible = audibleVisible,
                youtubeButtonVisible = youtubeVisible,
                youversionButtonVisible = youversionVisible,
                appleMusicButtonVisible = appleMusicVisible,
                podcastsButtonVisible = podcastsVisible,
                amazonMusicButtonVisible = amazonMusicVisible,
                streamingButtonVisible = streamingVisible,
                calmButtonVisible = calmVisible,
                headspaceButtonVisible = headspaceVisible,
                // Onboarding & Interaction
                hasSeenOnboarding = hasSeenOnboarding,
                appOpenCount = newOpenCount,
                timerStartCount = timerStartCount,
                breadcrumbsUnlocked = breadcrumbs,
                hasClickedSettings = hasClickedSettings,
                hasClickedConfigure = hasClickedConfigure,
                hasClickedTimer = hasClickedTimer,
                hasStartedTimer = hasStartedTimer,
                selectedApps = selectedApps,
                hasSeenPreTimerHint = hasSeenPreTimerHint,
                // Anti-Doomscroll
                doomscrollLockEnabled = doomscrollLockEnabled,
                doomscrollBlockedApps = doomscrollBlockedApps,
                doomscrollGraceMinutes = doomscrollGraceMinutes,
                // Sleep Streaks & Gamification
                sleepStreakCount = sleepStreakCount,
                lastSleepDate = lastSleepDate,
                unlockedBadges = unlockedBadges,
                // Smart Extend
                smartExtendEnabled = smartExtendEnabled,
                smartExtendSensitivity = smartExtendSensitivity,
                
                customizeDefaultTimer = customizeEnabled
            )
            MainActivity.latestSettings = _settings.value
            
            // Update Breadcrumb Suggestion immediately
            _breadcrumbSuggestion.value = BreadcrumbManager.getNextSuggestion(_settings.value)
        }
        
        // Sync BootReceiver State on Startup (Fix for Android 15 restriction)
        // Ensure that if the user HAS the setting enabled, the receiver is also enabled (since we disabled it by default in manifest)
        if (context != null) {
            try {
                // Initialize default value for open_on_charge_enabled, defaulting to false if not found
                val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val openOnChargeEnabled = settingsPrefs.getBoolean("open_on_charge_enabled", false)

                val receiver = android.content.ComponentName(context, com.gollan.fadesleeptimer.service.BootReceiver::class.java)
                val expectedState = if (openOnChargeEnabled) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                
                context.packageManager.setComponentEnabledSetting(
                    receiver,
                    expectedState,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    // Morning Check State
    private val _showMorningOverlay = MutableStateFlow(false)
    val showMorningOverlay = _showMorningOverlay.asStateFlow()

    fun checkMorningPrompt() {
        // Only check if feature is enabled
        if (!_settings.value.customizeDefaultTimer) return
        if (!_settings.value.morningCheckEnabled) return

        val lastSession = _settings.value.lastSessionTimestamp
        if (lastSession == 0L) return // No previous session

        val now = System.currentTimeMillis()
        val timeSinceSession = now - lastSession
        
        // Logic:
        // 1. Must be at least 8 hours after session (ensure full sleep)
        // 2. Must be within 24 hours of session (ensure relevance)
        // 3. Must not have checked this specific session yet
        
        val eightHoursMillis = 8 * 60 * 60 * 1000L
        val twentyFourHoursMillis = 24 * 60 * 60 * 1000L
        
        if (timeSinceSession in eightHoursMillis..twentyFourHoursMillis) {
            if (_settings.value.lastCheckedSessionTimestamp != lastSession) {
                _showMorningOverlay.value = true
            }
        }
    }

    fun dismissMorningPrompt(context: Context) {
        _showMorningOverlay.value = false
        
        // Mark this specific session as checked
        val lastSession = _settings.value.lastSessionTimestamp
        val prefs = context.getSharedPreferences("app_stats", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_checked_session_timestamp", lastSession).apply()
        
        _settings.value = _settings.value.copy(lastCheckedSessionTimestamp = lastSession)
    }
}
