package com.gollan.fadesleeptimer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gollan.fadesleeptimer.ui.AppSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.gollan.fadesleeptimer.ui.components.*

import com.gollan.fadesleeptimer.util.InactivityMonitor
import com.gollan.fadesleeptimer.util.SensorHelper
import com.gollan.fadesleeptimer.viewmodel.TimerViewModel
import com.gollan.fadesleeptimer.viewmodel.AudioViewModel
import com.gollan.fadesleeptimer.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import com.gollan.fadesleeptimer.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var inactivityMonitor: InactivityMonitor
    // sensorHelper removed from class properties to prevent double init
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latestContext = this
        
        inactivityMonitor = InactivityMonitor(this, CoroutineScope(Dispatchers.Main))
        // sensorHelper init removed from here.

        createNightstandChannel()
        com.gollan.fadesleeptimer.util.BedtimeNotificationHelper.createChannel(this)
        com.gollan.fadesleeptimer.util.BrainDumpNotificationHelper.createChannel(this)

        // Check for Brain Dump Intent
        if (intent?.getBooleanExtra(com.gollan.fadesleeptimer.util.BrainDumpNotificationHelper.EXTRA_OPEN_BRAIN_DUMP, false) == true) {
            val viewModel = androidx.lifecycle.ViewModelProvider(this)[TimerViewModel::class.java]
            viewModel.triggerOpenBrainDump()
        }
        
        // Handle App Action intent (from Google Assistant voice command)
        handleAppActionIntent(intent)

        setContent {
            val viewModel: TimerViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            val audioViewModel: AudioViewModel = viewModel()
            
            // Initialize SensorHelper with callbacks
            val context = LocalContext.current
            var isFaceDown by remember { mutableStateOf(false) }
            
            val sensorHelper = remember { 
                SensorHelper(
                    context, 
                    onFaceDown = {
                        val currentSettings = settingsViewModel.settings.value
                        viewModel.startTimer(context, 15, currentSettings.copy(playOnStart = true))
                    },
                    onFaceDownChanged = { isFaceDown = it }
                ) 
            }
            
            // Listen for Settings Updates from TimerVM (Streak Logic)
            LaunchedEffect(viewModel) {
                viewModel.onUpdateSettings = { newSettings ->
                    settingsViewModel.updateSettings(newSettings)
                }
            }
            
            // Handle App Action (from Google Assistant voice command)
            LaunchedEffect(Unit) {
                when (val action = pendingAppAction) {
                    is AppAction.StartTimer -> {
                        val currentSettings = settingsViewModel.settings.value
                        viewModel.startTimer(context, action.durationMinutes.toLong(), currentSettings)
                        android.util.Log.d("AppAction", "Started timer for ${action.durationMinutes} minutes")
                    }
                    is AppAction.StopTimer -> {
                        viewModel.stopTimer(context)
                        android.util.Log.d("AppAction", "Stopped timer")
                    }
                    is AppAction.ExtendTimer -> {
                        viewModel.extendTimer(context)
                        android.util.Log.d("AppAction", "Extended timer by 15 minutes")
                    }
                    is AppAction.OpenSettings -> {
                        settingsViewModel.triggerOpenSettings()
                        android.util.Log.d("AppAction", "Opening settings")
                    }
                    null -> { /* No action pending */ }
                }
                pendingAppAction = null // Clear after use
            }
            
            // Listen for Badge Events
            val badgeEvents by viewModel.badgeEarnedEvent.collectAsState()
            var showBadgeDialog by remember { mutableStateOf(false) }
            
            LaunchedEffect(badgeEvents) {
                if (badgeEvents.isNotEmpty()) {
                    showBadgeDialog = true
                }
            }
            
            if (showBadgeDialog && badgeEvents.isNotEmpty()) {
                val badgeId = badgeEvents.first() // Show first earned for now
                val badge = com.gollan.fadesleeptimer.data.BadgeRepository.getBadge(badgeId)
                
                if (badge != null) {
                    AlertDialog(
                        onDismissRequest = { 
                            viewModel.dismissBadgeEvent()
                            showBadgeDialog = false 
                        },
                        icon = { Icon(badge.icon, null, tint = badge.color) },
                        title = { Text("Badge Unlocked!") },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(badge.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                 Spacer(Modifier.height(8.dp))
                                Text(badge.description)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { 
                                viewModel.dismissBadgeEvent()
                                showBadgeDialog = false 
                            }) {
                                Text("Awesome!")
                            }
                        }
                    )
                }
            }

            // Smart Review Prompt Logic
            var showReviewDialog by remember { mutableStateOf(false) }
            var reviewPromptText by remember { mutableStateOf("") }
            
            // Smart Support Prompt Logic
            var showSupportPromptDialog by remember { mutableStateOf(false) }
            var supportPromptText by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                // Check Review FIRST
                val shouldShowReview = com.gollan.fadesleeptimer.util.ReviewManager.shouldShowPrompt(context)
                if (shouldShowReview) {
                    reviewPromptText = com.gollan.fadesleeptimer.util.ReviewManager.getPromptText(context)
                    showReviewDialog = true
                } else {
                     // Only check Support if Review is NOT showing
                     // Also pass settings to check for existing support
                     val currentSettings = settingsViewModel.settings.value // Access simple value
                     // We might need to gather settings first or just pass basic context if we can read sp inside? 
                     // SupportPromptManager needs AppSettings object.
                     // The viewmodel settings are already loaded.
                     if (com.gollan.fadesleeptimer.util.SupportPromptManager.shouldShowPrompt(context, settingsViewModel.settings.value)) {
                         supportPromptText = com.gollan.fadesleeptimer.util.SupportPromptManager.getPromptText(context)
                         showSupportPromptDialog = true
                     }
                }
            }
            
            if (showReviewDialog) {
                com.gollan.fadesleeptimer.ui.components.ReviewRequestDialog(
                    promptText = reviewPromptText,
                    onDismiss = { 
                        showReviewDialog = false
                        com.gollan.fadesleeptimer.util.ReviewManager.markPromptShown(context)
                    },
                    onReviewClick = {
                        showReviewDialog = false
                        com.gollan.fadesleeptimer.util.ReviewManager.markPromptShown(context)
                        
                        // Launch Google Review Flow
                        val manager = com.google.android.play.core.review.ReviewManagerFactory.create(context)
                        val request = manager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val reviewInfo = task.result
                                val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)
                                flow.addOnCompleteListener { _ ->
                                    com.gollan.fadesleeptimer.util.ReviewManager.setBadgeEarned(context)
                                    settingsViewModel.unlockFeature("rate") 
                                }
                            }
                        }
                    }
                )
            }
            
            if (showSupportPromptDialog) {
                com.gollan.fadesleeptimer.ui.components.SupportPromptDialog(
                    promptText = supportPromptText,
                    onDismiss = {
                        showSupportPromptDialog = false
                        com.gollan.fadesleeptimer.util.SupportPromptManager.markPromptShown(context)
                    },
                    onCheckItOut = {
                        showSupportPromptDialog = false
                        com.gollan.fadesleeptimer.util.SupportPromptManager.markPromptShown(context)
                        // Navigate to Support Screen
                        // We need a way to trigger the support screen.
                        // The MainScreen has 'showSupportScreen' state, but we are outside it.
                        // We can use a ViewModel event or Intent?
                        // Or better yet, we can pass a special flag to MainScreen?
                        // Simpler: Just rely on MainScreen observing a VM state?
                        // Actually, MainScreen uses local state `var showSupportScreen by remember`.
                        // We can't easily reach it from here without refactoring.
                        // Alternative: Send a Broadcast or update ViewModel that MainScreen observes.
                        
                        // Let's use the ViewModel to signal "ShowSupport".
                        // Add `_openSupportEvent` to SettingsViewModel.
                        settingsViewModel.triggerOpenSupport()
                    }
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    sensorHelper.cleanup()
                }
            }
            
            com.gollan.fadesleeptimer.ui.MainScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                audioViewModel = audioViewModel,
                sensorHelper = sensorHelper
            )
        }
    }
    
    override fun onUserInteraction() {
        super.onUserInteraction()
        inactivityMonitor.onUserInteraction()
    }
    
    override fun onResume() {
        super.onResume()
        inactivityMonitor.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        inactivityMonitor.onPause()
    }
    
    override fun onStop() {
        super.onStop()
        inactivityMonitor.onStop()
        // sensorHelper.cleanup() handled by DisposableEffect
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (latestContext == this) {
            latestContext = null
        }
    }

    private fun createNightstandChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nightstand Mode"
            val descriptionText = "Auto-launch notification"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("nightstand_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Handle App Action intents from Google Assistant.
     * Supports: start_timer, stop_timer, extend_timer, open_settings
     */
    private fun handleAppActionIntent(intent: Intent?) {
        if (intent == null) return
        
        // Get action type from intent
        val actionType = intent.getStringExtra("app_action_type") ?: return
        
        android.util.Log.d("AppAction", "Received action: $actionType")
        
        when (actionType) {
            "start_timer" -> {
                val durationString = intent.getStringExtra("timer_duration")
                val durationMinutes = parseDuration(durationString)
                pendingAppAction = AppAction.StartTimer(durationMinutes)
            }
            "stop_timer" -> {
                pendingAppAction = AppAction.StopTimer
            }
            "extend_timer" -> {
                pendingAppAction = AppAction.ExtendTimer
            }
            "open_settings" -> {
                pendingAppAction = AppAction.OpenSettings
            }
        }
    }
    
    private fun parseDuration(durationString: String?): Int {
        if (durationString.isNullOrBlank()) return 30 // Default 30 minutes
        
        // Try to extract number from string like "30 minutes", "1 hour", "45m"
        val regex = Regex("""(\d+)\s*(hour|hr|h|minute|min|m)?""", RegexOption.IGNORE_CASE)
        val match = regex.find(durationString)
        
        return if (match != null) {
            val value = match.groupValues[1].toIntOrNull() ?: 30
            val unit = match.groupValues[2].lowercase()
            
            when {
                unit.startsWith("h") -> value * 60 // Convert hours to minutes
                else -> value // Already in minutes
            }
        } else {
            30 // Default
        }
    }
    
    // Sealed class for type-safe App Actions
    sealed class AppAction {
        data class StartTimer(val durationMinutes: Int) : AppAction()
        data object StopTimer : AppAction()
        data object ExtendTimer : AppAction()
        data object OpenSettings : AppAction()
    }
    
    // Pending App Action (used by setContent)
    private var pendingAppAction: AppAction? = null

    companion object {
        var latestSettings: AppSettings = AppSettings()
        var latestContext: Context? = null
    }
}




