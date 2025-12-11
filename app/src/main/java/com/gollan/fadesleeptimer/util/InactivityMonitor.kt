package com.gollan.fadesleeptimer.util

import android.content.Context
import android.widget.Toast
import com.gollan.fadesleeptimer.data.TimerRepository
import com.gollan.fadesleeptimer.service.ScreenLockService
import kotlinx.coroutines.*

class InactivityMonitor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var lastInteractionTime = 0L
    private var isInForeground = false
    private var inactivityJob: Job? = null

    fun onUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
    }

    fun onResume() {
        isInForeground = true
        lastInteractionTime = System.currentTimeMillis()
        startMonitoring()
    }

    fun onPause() {
        isInForeground = false
        stopMonitoring()
    }

    fun onStop() {
        stopMonitoring()
    }

    private fun startMonitoring() {
        stopMonitoring() // Cancel any existing job
        
        inactivityJob = scope.launch {
            while (isActive) {
                delay(1000) // Check every second
                
                val isTimerRunning = TimerRepository.isRunning.value
                if (!isTimerRunning || !isInForeground) {
                    continue // Skip if timer not running or app in background
                }
                
                val inactiveTime = System.currentTimeMillis() - lastInteractionTime
                val twoMinutes = 2 * 60 * 1000L
                val threeMinutes = 3 * 60 * 1000L
                
                // Show warning at 2 minutes
                if (inactiveTime >= twoMinutes && inactiveTime < twoMinutes + 1000) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "No activity detected, screen will turn off in 60 seconds to save battery",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
                // Lock screen at 3 minutes
                if (inactiveTime >= threeMinutes) {
                    if (ScreenLockService.lockScreen()) {
                        // Successfully locked screen
                        lastInteractionTime = System.currentTimeMillis() // Reset to prevent repeated attempts
                    } else {
                        // Service not available - show message once
                        if (inactiveTime < threeMinutes + 1000) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Enable Accessibility permission for auto screen-off",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        lastInteractionTime = System.currentTimeMillis() // Reset
                    }
                }
            }
        }
    }

    private fun stopMonitoring() {
        inactivityJob?.cancel()
        inactivityJob = null
    }
}
