package com.gollan.fadesleeptimer.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager


object IconManager {
    
    data class AppIcon(
        val id: String,
        val name: String,
        val componentName: String,
        val previewResId: Int, // Resource ID for the preview image in settings
        val color: Long // Hex color for the glow effect (e.g., 0xFF...)
    )

    val ICONS = listOf(
        AppIcon("default", "Classic Fade", ".MainActivityDefault", com.gollan.fadesleeptimer.R.mipmap.ic_launcher_foreground, 0xFF6366F1), // Indigo
        AppIcon("gold", "Golden Hour", ".MainActivityGold", com.gollan.fadesleeptimer.R.mipmap.icon_gold, 0xFFF59E0B), // Amber
        AppIcon("eyes", "Sleepy Eyes", ".MainActivityEyes", com.gollan.fadesleeptimer.R.mipmap.icon_eyes, 0xFF8B5CF6), // Violet
        AppIcon("ear", "Listening Ear", ".MainActivityEar", com.gollan.fadesleeptimer.R.mipmap.icon_ear, 0xFF0EA5E9), // Sky Blue
        AppIcon("white", "Clean White", ".MainActivityWhite", com.gollan.fadesleeptimer.R.mipmap.icon_white, 0xFFF8FAFC), // Slate 50
        AppIcon("retro", "Retro Fade", ".MainActivityRetro", com.gollan.fadesleeptimer.R.mipmap.icon_retro, 0xFFF43F5E), // Rose
        AppIcon("disco", "Disco Fever", ".MainActivityDisco", com.gollan.fadesleeptimer.R.mipmap.icon_disco, 0xFFD946EF), // Fuchsia
        AppIcon("future", "Neon Future", ".MainActivityFuture", com.gollan.fadesleeptimer.R.mipmap.icon_future, 0xFF06B6D4), // Cyan
        AppIcon("sheep", "Counting Sheep", ".MainActivitySheep", com.gollan.fadesleeptimer.R.mipmap.icon_sheep, 0xFF94A3B8), // Slate 400
        AppIcon("lamp", "Night Lamp", ".MainActivityLamp", com.gollan.fadesleeptimer.R.mipmap.icon_lamp, 0xFFFCD34D), // Amber 300
        AppIcon("christian", "Faith & Grace", ".MainActivityChristian", com.gollan.fadesleeptimer.R.mipmap.icon_christian, 0xFFE2E8F0), // Slate 200
        AppIcon("bitcoin", "HODL Tight", ".MainActivityBitcoin", com.gollan.fadesleeptimer.R.mipmap.icon_bitcoin, 0xFFF7931A), // Bitcoin Orange
        AppIcon("cat", "Purrfect Sleep", ".MainActivityCat", com.gollan.fadesleeptimer.R.mipmap.icon_cat, 0xFFFB923C) // Orange 400
    )

    fun setIcon(context: Context, iconId: String) {
        val packageManager = context.packageManager
        val packageName = context.packageName
        
        // 1. Find the selected icon
        // 1. Find the selected icon - Validation check only
        @Suppress("UNUSED_VARIABLE")
        val selectedIcon = ICONS.find { it.id == iconId } ?: return

        // 2. Disable all other icons
        ICONS.forEach { icon ->
            val state = if (icon.id == iconId) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            val fullComponentName = if (icon.componentName.startsWith(".")) {
                packageName + icon.componentName
            } else {
                icon.componentName
            }
            
            val componentName = ComponentName(packageName, fullComponentName)
            
            // Only change if necessary to avoid unnecessary restarts
            try {
                if (packageManager.getComponentEnabledSetting(componentName) != state) {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        state,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getCurrentIcon(context: Context): String {
        val packageManager = context.packageManager
        val packageName = context.packageName
        
        ICONS.forEach { icon ->
            val fullComponentName = if (icon.componentName.startsWith(".")) {
                packageName + icon.componentName
            } else {
                icon.componentName
            }
            
            val componentName = ComponentName(packageName, fullComponentName)
            try {
                if (packageManager.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return icon.id
                }
            } catch (e: Exception) {
                // Component not found, ignore
            }
        }
        return "default"
    }
}
