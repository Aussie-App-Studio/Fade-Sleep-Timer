package com.gollan.fadesleeptimer.utils

import com.gollan.fadesleeptimer.ui.AppSettings

data class BreadcrumbSuggestion(
    val featureId: String,
    val label: String,
    val description: String,
    val settingsCategory: String // "audio", "visuals", "wellness", "control", "advanced"
)

object BreadcrumbManager {
    
    fun getNextSuggestion(settings: AppSettings): BreadcrumbSuggestion? {
        val opens = settings.appOpenCount
        
        // Early Stage (3-12 opens)
        if (opens >= 3 && !settings.breathingGuideEnabled) {
            return BreadcrumbSuggestion("breathing", "Breathing Exercises", "Relax before you sleep", "wellness")
        }
        if (opens >= 5 && !settings.prayerReminder) {
            return BreadcrumbSuggestion("prayer", "Prayer Reminder", "Set a reminder to pray", "wellness")
        }
        if (opens >= 7 && !settings.scriptureModeEnabled) {
            return BreadcrumbSuggestion("scripture", "Bedtime Scripture", "Read a verse before bed", "wellness")
        }
        if (opens >= 10 && !settings.sleepCalculatorEnabled) {
            return BreadcrumbSuggestion("sleep_calc", "Sleep Calculator", "Calculate wake up times", "wellness")
        }
        if (opens >= 12 && !settings.brainDumpEnabled) {
            return BreadcrumbSuggestion("brain_dump", "Brain Dump", "Clear your mind", "wellness")
        }
        
        // Audio Optimization (14-16 opens)
        if (opens >= 14 && !settings.smartWait) {
            return BreadcrumbSuggestion("smart_wait", "Smart Wait", "Wait for audio to end", "audio")
        }
        if (opens >= 15 && !settings.playOnStart) {
            return BreadcrumbSuggestion("play_on_start", "Play on Start", "Auto-resume media", "audio")
        }

        
        // Visual Enhancement (18-22 opens)
        if (opens >= 18 && !settings.superDim) {
            return BreadcrumbSuggestion("super_dim", "Super Dim", "Extra dark overlay", "visuals")
        }
        if (opens >= 20 && !settings.analogueClock) {
            return BreadcrumbSuggestion("analogue_clock", "Analogue Clock", "Classic clock face", "visuals")
        }
        if (opens >= 22 && settings.monochromeMode == com.gollan.fadesleeptimer.ui.MonochromeMode.OFF) {
            return BreadcrumbSuggestion("monochrome", "Monochrome Mode", "Black & white screen", "visuals")
        }
        
        // Control Features (24-26 opens)
        if (opens >= 24 && !settings.shakeToExtend) {
            return BreadcrumbSuggestion("shake_extend", "Shake to Extend", "Shake to add time", "control")
        }
        if (opens >= 25 && !settings.faceDownStart) {
            return BreadcrumbSuggestion("face_down", "Face-Down Start", "Flip to start timer", "control")
        }
        if (opens >= 26 && !settings.pocketMode) {
            return BreadcrumbSuggestion("pocket_mode", "Pocket Mode", "Prevent accidental touches", "control")
        }
        
        // Advanced (28-46 opens)
        if (opens >= 28 && !settings.volumeSafetyCap) {
            return BreadcrumbSuggestion("volume_safety", "Volume Safety", "Limit max volume", "audio")
        }
        if (opens >= 30 && !settings.goHomeOnStart) {
            return BreadcrumbSuggestion("auto_minimize", "Auto-Minimize", "Go home when timer starts", "audio") // Grouped in Audio/General
        }
        if (opens >= 32 && !settings.batteryGuard) {
            return BreadcrumbSuggestion("battery_guard", "Battery Guard", "Save power when low", "audio")
        }
        if (opens >= 34 && !settings.openOnChargeEnabled) {
            return BreadcrumbSuggestion("open_on_charge", "Open on Charge", "Nightstand mode", "audio") // Grouped in Audio/General
        }
        
        // Late stage suggestions (re-prompts or deeper features)
        if (opens >= 40 && !settings.airplaneModeReminder) {
            return BreadcrumbSuggestion("airplane_mode", "Airplane Mode", "Reminder to disconnect", "audio")
        }
        
        return null
    }
}
