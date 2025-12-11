package com.gollan.fadesleeptimer.util

import android.content.Context
import com.gollan.fadesleeptimer.ui.AppSettings
import java.util.concurrent.TimeUnit

object SupportPromptManager {

    private const val PREFS_NAME = "support_prompt_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch_timestamp"
    private const val KEY_LAST_PROMPT = "last_prompt_timestamp"
    private const val KEY_PROMPT_INDEX = "prompt_index"

    fun getPromptText(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_PROMPT_INDEX, 0)
        val prompts = context.resources.getStringArray(com.gollan.fadesleeptimer.R.array.support_prompts)
        return prompts.getOrElse(index % prompts.size) { prompts[0] }
    }

    fun shouldShowPrompt(context: Context, settings: AppSettings): Boolean {
        // 1. Check if user has ALREADY supported
        if (hasSupported(settings)) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        
        // Ensure First Launch is tracked (ReviewManager might track it too, but safety first)
        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (firstLaunch == 0L) {
             // Try to sync with ReviewManager if possible, or just set now
             // For simplicity, let's look at ReviewManager's prefs if available, or just set now
            firstLaunch = now
            prefs.edit().putLong(KEY_FIRST_LAUNCH, firstLaunch).apply()
            return false
        }

        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT, 0)
        val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(now - firstLaunch)
        val daysSinceLast = TimeUnit.MILLISECONDS.toDays(now - lastPrompt)

        // Rule: Start checking after 14 days
        if (daysSinceInstall < 14) return false
        
        // Rule: Interval > 30 days
        if (lastPrompt != 0L && daysSinceLast < 30) return false
        
        // If never shown (lastPrompt == 0) and days > 14 -> Show
        // If shown before, and daysSinceLast > 30 -> Show
        return true
    }

    fun markPromptShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_PROMPT_INDEX, 0)
        prefs.edit()
            .putInt(KEY_PROMPT_INDEX, index + 1)
            .putLong(KEY_LAST_PROMPT, System.currentTimeMillis())
            .apply()
    }
    
    private fun hasSupported(settings: AppSettings): Boolean {
        // Check ALL support avenues
        return settings.isRated ||
               settings.isShared ||
               settings.hasVisualPack ||
               settings.hasHallOfFameAccess ||
               settings.isSponsor ||
               settings.isLegoPurchased
    }
}
