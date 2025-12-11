package com.gollan.fadesleeptimer.ui



data class AppSettings(
    val playOnStart: Boolean = false,
    val superDim: Boolean = false,
    val dndEnabled: Boolean = true,
    val dndMode: DndMode = DndMode.PRIORITY,
    val timerPresets: List<Int> = listOf(5, 10, 15, 30, 45, 60),
    val faceDownStart: Boolean = false,
    val shakeToExtend: Boolean = true,
    val pocketMode: Boolean = false,
    // Audio & Playback Features
    val audioSelection: AudioSelection = AudioSelection.SYSTEM,
    val voiceGender: String = "DEFAULT", // MALE, FEMALE, DEFAULT
    val fadeDuration: Int = 5, // minutes (1-10)
    val volumeSafetyCap: Boolean = false,
    val smartWait: Boolean = false,

    val airplaneModeReminder: Boolean = false,
    val batteryGuard: Boolean = false,
    val batteryGuardThreshold: Int = 15,
    val goHomeOnStart: Boolean = false,
    val openOnChargeEnabled: Boolean = false,
    val openOnChargeMode: OpenOnChargeMode = OpenOnChargeMode.STRICT,
    // Wellness & Personalization
    val prayerReminder: Boolean = true,
    val brainDumpEnabled: Boolean = false,
    val scriptureModeEnabled: Boolean = true,
    val scriptureTopic: String = "Random",
    val wisdomQuotesEnabled: Boolean = true,
    val wisdomQuotesPeaceComfort: Boolean = true,
    val wisdomQuotesGodGlory: Boolean = true,
    val wisdomQuotesGospelGrace: Boolean = true,
    val wisdomQuotesWisdomLiving: Boolean = true,
    val sleepCalculatorEnabled: Boolean = false,
    val sleepCalculatorAlarmOption: Boolean = false,
    val appIcon: String = "default",
    // Premium Rewards
    val hasSupporterBadge: Boolean = false,
    val showSupporterBadge: Boolean = false,
    val hasReviewBadge: Boolean = false, // New badge state
    val hasVisualPack: Boolean = false,
    val hasHallOfFameAccess: Boolean = false,
    val hasSponsorAccess: Boolean = false,
    // Explicit Purchased States (Ownership)
    val isVisualPackPurchased: Boolean = false,
    val isHallOfFamePurchased: Boolean = false,
    val isSponsorPurchased: Boolean = false,
    // Breathing Guide
    val breathingGuideEnabled: Boolean = false,
    val breathingAutoStart: Boolean = false,
    val breathingPatternId: String = "deep",
    val breathingDurationMinutes: Int = 1,
    val breathingVibrationEnabled: Boolean = true,
    // Bedtime Reminder
    val bedtimeReminderEnabled: Boolean = false,
    val bedtimeHour: Int = 22, // 10 PM
    val bedtimeMinute: Int = 0,
    // Visuals & Display Features
    val analogueClock: Boolean = false,
    val monochromeMode: MonochromeMode = MonochromeMode.OFF,
    val burnInProtection: Boolean = false,
    val hasInteractedWithMonochrome: Boolean = false,
    val theme: String = "default",
    // Stats
    val savedMinutes: Int = 0,
    // Morning Check & Default Timer
    val morningCheckEnabled: Boolean = true,
    val lastSessionTimestamp: Long = 0,
    val lastSessionExtendedCount: Int = 0,
    val lastCheckedSessionTimestamp: Long = 0,
    val customizeDefaultTimer: Boolean = false,
    val defaultDuration: Int = 15,
    // Hall of Fame
    val isImmortalized: Boolean = false,
    val hallOfFameName: String? = null,
    // Support Tiers
    val isRated: Boolean = false,
    val isShared: Boolean = false, // New
    val hasCheckedOtherApps: Boolean = false, // New

    val visualsUnlocked: Boolean = false,
    val isSponsor: Boolean = false,
    val isLegoPurchased: Boolean = false,
    // Media App Button Visibility
    val configureButtonVisible: Boolean = true,
    val spotifyButtonVisible: Boolean = false,
    val ytMusicButtonVisible: Boolean = false,
    val audibleButtonVisible: Boolean = false,
    val youtubeButtonVisible: Boolean = false,
    val youversionButtonVisible: Boolean = false,
    val appleMusicButtonVisible: Boolean = false,
    val podcastsButtonVisible: Boolean = false,
    val amazonMusicButtonVisible: Boolean = false,
    val streamingButtonVisible: Boolean = false,
    val calmButtonVisible: Boolean = false,
    val headspaceButtonVisible: Boolean = false,
    // Beta
    // Premium Unlock
    val isPremiumUnlocked: Boolean = false,
    
    // Onboarding & Breadcrumbs
    val hasSeenOnboarding: Boolean = false,
    val appOpenCount: Int = 0,
    val timerStartCount: Int = 0,
    val breadcrumbsUnlocked: Set<String> = emptySet(),
    
    // Interaction Tracking (Encouragement Glow)
    val hasClickedSettings: Boolean = false,
    val hasClickedConfigure: Boolean = false,
    val hasClickedTimer: Boolean = false,
    val hasStartedTimer: Boolean = false,
    
    // Dynamic App Shortcuts
    val selectedApps: List<String> = emptyList(),
    
    // PreTimer Hint
    val hasSeenPreTimerHint: Boolean = false,
    
    // Anti-Doomscroll
    val doomscrollLockEnabled: Boolean = false,
    val doomscrollBlockedApps: Set<String> = emptySet(),
    val doomscrollGraceMinutes: Int = 5,
    
    // Sleep Streaks & Gamification
    val sleepStreakCount: Int = 0,
    val lastSleepDate: String? = null, // Format: YYYY-MM-DD
    val unlockedBadges: Set<String> = emptySet(),
    // Smart Extend (Movement Detection)
    val smartExtendEnabled: Boolean = false,
    val smartExtendSensitivity: SmartExtendSensitivity = SmartExtendSensitivity.MEDIUM
)

enum class SmartExtendSensitivity {
    LOW,    // Requires significant movement
    MEDIUM, // Default
    HIGH    // Detects subtle movements
}

enum class MonochromeMode {
    OFF,
    IN_APP_ON_TIMER,
    IN_APP_ALWAYS,
    SYSTEM_NIGHT_FILTER,
    SYSTEM_MANUAL
}

enum class OpenOnChargeMode {
    ALWAYS,
    STRICT,
    LATE,
    MORNING
}

enum class DndMode {
    PRIORITY,
    ALARMS_ONLY
}

enum class AudioSelection {
    SYSTEM,
    WIKI_GENERAL,
    WIKI_BIO,
    WIKI_HISTORY
}
