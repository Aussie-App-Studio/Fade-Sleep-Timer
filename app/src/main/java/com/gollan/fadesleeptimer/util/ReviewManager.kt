package com.gollan.fadesleeptimer.util

import android.content.Context
import android.content.SharedPreferences
import com.gollan.fadesleeptimer.ui.AppSettings
import java.util.concurrent.TimeUnit

object ReviewManager {

    private const val PREFS_NAME = "review_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch_timestamp"
    private const val KEY_PROMPT_COUNT = "prompt_count"
    private const val KEY_LAST_PROMPT = "last_prompt_timestamp"
    private const val KEY_BADGE_EARNED = "badge_earned"
    
    // 12 Witty Prompts (Cycling)
    fun getPromptText(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_PROMPT_COUNT, 0)
        
        val prompts = context.resources.getStringArray(com.gollan.fadesleeptimer.R.array.review_prompts)
        
        // Cycle logic based on prompts size
        val index = if (count < prompts.size) {
             count
        } else {
             // 12 -> 1, 13 -> 2, etc. (assuming size is 12)
             1 + ((count - prompts.size) % (prompts.size - 1))
        }
        
        return prompts.getOrElse(index) { prompts[0] }
    }

    fun shouldShowPrompt(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        
        // 1. First Launch
        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (firstLaunch == 0L) {
            firstLaunch = now
            prefs.edit().putLong(KEY_FIRST_LAUNCH, firstLaunch).apply()
            return false // Don't show on very first instant
        }

        val count = prefs.getInt(KEY_PROMPT_COUNT, 0)
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT, 0)
        
        val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(now - firstLaunch)
        val daysSinceLast = TimeUnit.MILLISECONDS.toDays(now - lastPrompt)

        return when (count) {
            0 -> daysSinceInstall >= 7 // Week 1
            1 -> daysSinceInstall >= 30 // Month 1
            else -> daysSinceLast >= 90 // Quarterly
        }
    }

    fun markPromptShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_PROMPT_COUNT, 0)
        prefs.edit()
            .putInt(KEY_PROMPT_COUNT, count + 1)
            .putLong(KEY_LAST_PROMPT, System.currentTimeMillis())
            .apply()
    }
    
    fun setBadgeEarned(context: Context) {
         context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
             .edit().putBoolean(KEY_BADGE_EARNED, true).apply()
    }
    
    fun hasBadge(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BADGE_EARNED, false)
    }
}
