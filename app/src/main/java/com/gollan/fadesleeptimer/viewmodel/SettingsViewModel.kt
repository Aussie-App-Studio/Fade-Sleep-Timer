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

    // Billing
    private lateinit var billingManager: BillingManager

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        MainActivity.latestSettings = newSettings
        
        // Update Breadcrumb Suggestion
        _breadcrumbSuggestion.value = BreadcrumbManager.getNextSuggestion(newSettings)
        
        // Persist for JobService
        val prefs = MainActivity.latestContext?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs?.edit()
            ?.putBoolean("open_on_charge_enabled", newSettings.openOnChargeEnabled)
            ?.putString("open_on_charge_mode", newSettings.openOnChargeMode.name)
            ?.putInt("default_duration", newSettings.defaultDuration)
            ?.putBoolean("morning_check_enabled", newSettings.morningCheckEnabled)
            ?.putBoolean("customize_default_timer", newSettings.customizeDefaultTimer)
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
            // Onboarding Persistence
            ?.putBoolean("has_seen_onboarding", newSettings.hasSeenOnboarding)
            ?.putStringSet("breadcrumbs_unlocked", newSettings.breadcrumbsUnlocked)
            ?.putBoolean("has_clicked_settings", newSettings.hasClickedSettings)
            ?.putBoolean("has_clicked_configure", newSettings.hasClickedConfigure)
            ?.putBoolean("has_clicked_timer", newSettings.hasClickedTimer)
            // Smart Extend
            ?.putBoolean("smart_extend_enabled", newSettings.smartExtendEnabled)
            ?.putString("smart_extend_sensitivity", newSettings.smartExtendSensitivity.name)
            ?.apply()
        
        // Schedule/Cancel Job
        if (newSettings.openOnChargeEnabled) {
            MainActivity.latestContext?.let { NightstandJobService.scheduleWait(it) }
        } else {
            MainActivity.latestContext?.let { NightstandJobService.cancelAll(it) }
        }

        // Schedule/Cancel Bedtime Reminder
        // Note: This requires moving scheduleBedtimeReminder logic here or keeping it in TimerViewModel and calling it.
        // For now, we'll assume we need to move it or call a helper.
        // Let's keep it simple for now and handle the reminder logic in a separate step or helper.
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
            hallOfFameName = null
        )
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
                    if (purchases.contains("hall_of_fame_icons")) unlockFeature("hall_of_fame_icons")
                    if (purchases.contains("sponsor_feature")) unlockFeature("sponsor")
                    if (purchases.contains("lego_tier_ultimate")) unlockFeature("lego")
                }
            }
            val prefs = context.getSharedPreferences("app_stats", Context.MODE_PRIVATE)
            val saved = prefs.getInt("saved_minutes", 0)
            
            // Load Morning Check Data
            val lastSession = prefs.getLong("last_session_timestamp", 0)
            val lastExtended = prefs.getInt("last_session_extended_count", 0)
            val lastCheckedSession = prefs.getLong("last_checked_session_timestamp", 0)
            
            // Load Settings Preferences (Default Duration & Toggle)
            val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val defaultDur = settingsPrefs.getInt("default_duration", 15)
            val checkEnabled = settingsPrefs.getBoolean("morning_check_enabled", true)
            val customizeEnabled = settingsPrefs.getBoolean("customize_default_timer", false)
            val isImmortalized = settingsPrefs.getBoolean("is_immortalized", false)
            val hofName = settingsPrefs.getString("hall_of_fame_name", null)
            
            // Load Support Tiers
            val isRated = settingsPrefs.getBoolean("is_rated", false)
            val isShared = settingsPrefs.getBoolean("is_shared", false)
            val hasCheckedOtherApps = settingsPrefs.getBoolean("has_checked_other_apps", false)

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
            
            val hasReviewBadge = com.gollan.fadesleeptimer.util.ReviewManager.hasBadge(context)
            
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
            val breadcrumbs = settingsPrefs.getStringSet("breadcrumbs_unlocked", emptySet()) ?: emptySet()
            
            val hasClickedSettings = settingsPrefs.getBoolean("has_clicked_settings", false)
            val hasClickedConfigure = settingsPrefs.getBoolean("has_clicked_configure", false)
            val hasClickedTimer = settingsPrefs.getBoolean("has_clicked_timer", false)

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
                savedMinutes = saved,
                lastSessionTimestamp = lastSession,
                lastSessionExtendedCount = lastExtended,
                lastCheckedSessionTimestamp = lastCheckedSession,
                defaultDuration = defaultDur,
                morningCheckEnabled = checkEnabled,
                isImmortalized = isImmortalized,
                hallOfFameName = hofName,
                isRated = isRated,
                isShared = isShared,
                hasCheckedOtherApps = hasCheckedOtherApps,

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
                // Onboarding
                hasSeenOnboarding = hasSeenOnboarding,
                appOpenCount = newOpenCount,
                breadcrumbsUnlocked = breadcrumbs,
                hasClickedSettings = hasClickedSettings,
                hasClickedConfigure = hasClickedConfigure,
                hasClickedTimer = hasClickedTimer,
                // Smart Extend
                // Smart Extend
                smartExtendEnabled = smartExtendEnabled,
                smartExtendSensitivity = smartExtendSensitivity,
                
                // RESTORED MISSING FIELD
                customizeDefaultTimer = customizeEnabled
            )
            
            // Update Breadcrumb Suggestion immediately
            _breadcrumbSuggestion.value = BreadcrumbManager.getNextSuggestion(_settings.value)
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
