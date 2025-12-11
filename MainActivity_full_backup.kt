package com.example.fadesleeptimer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadesleeptimer.data.TimerRepository
import com.example.fadesleeptimer.service.NightstandJobService
import com.example.fadesleeptimer.ui.AppSettings
import com.example.fadesleeptimer.ui.MonochromeMode
import com.example.fadesleeptimer.ui.PermissionsScreen
import com.example.fadesleeptimer.ui.PreTimerScreen
import com.example.fadesleeptimer.ui.SettingsScreen
import com.example.fadesleeptimer.ui.components.*

import com.example.fadesleeptimer.util.InactivityMonitor
import com.example.fadesleeptimer.util.SensorHelper
import com.example.fadesleeptimer.viewmodel.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

import com.example.fadesleeptimer.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var inactivityMonitor: InactivityMonitor
    private lateinit var sensorHelper: SensorHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latestContext = this
        
        inactivityMonitor = InactivityMonitor(this, CoroutineScope(Dispatchers.Main))

        createNightstandChannel()
        com.example.fadesleeptimer.util.BedtimeNotificationHelper.createChannel(this)

        setContent {
            val viewModel: TimerViewModel = viewModel()
            
            // Initialize SensorHelper once with the Compose ViewModel
            LaunchedEffect(Unit) {
                if (!::sensorHelper.isInitialized) {
                    sensorHelper = SensorHelper(this@MainActivity, viewModel)
                }
            }
            
            com.example.fadesleeptimer.ui.theme.FadeSleepTimerTheme {
                FadeSleepTimerApp(viewModel = viewModel, sensorHelper = sensorHelper)
            }
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
        if (::sensorHelper.isInitialized) {
            sensorHelper.cleanup()
        }
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
            val notificationManager: android.app.NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        var latestSettings: AppSettings = AppSettings()
        var latestContext: Context? = null
    }
}

@Composable
fun FadeSleepTimerApp(
    viewModel: TimerViewModel = viewModel(),
    sensorHelper: SensorHelper
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    // Initialize volume slider with current system volume
    LaunchedEffect(Unit) {
        viewModel.initVolume(context)
    }

    // Listen for system volume changes
    DisposableEffect(context) {
        val volumeReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                    viewModel.initVolume(context)
                }
            }
        }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(volumeReceiver, filter)

        onDispose {
            context.unregisterReceiver(volumeReceiver)
        }
    }

    val isRunning by viewModel.isRunning.collectAsState(initial = false)
    val timeLeft by viewModel.timeLeft.collectAsState(initial = 0L)
    val settings by viewModel.settings.collectAsState()
    val sleepHistory by viewModel.sleepHistory.collectAsState()
    val isAdVisible by viewModel.isAdVisible.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var hasPermissions by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    var isPocketModeActive by remember { mutableStateOf(false) }
    
    // Update Sensor Helper
    LaunchedEffect(settings, isRunning) {
        sensorHelper.updateState(settings, isRunning)
    }
    
    // Pocket Mode Callback
    DisposableEffect(sensorHelper) {
        sensorHelper.onPocketModeChanged = { active ->
            isPocketModeActive = active
        }
        onDispose {
            sensorHelper.onPocketModeChanged = null
        }
    }
    
    // Pre-Flight Check State
    var showPreFlight by remember { mutableStateOf(false) }
    var pendingDuration by remember { mutableStateOf(0L) }

    // Brain Dump State
    var showBrainDumpDialog by remember { mutableStateOf(false) }
    var showBrainDumpViewer by remember { mutableStateOf(false) }
    var showBreathingGuide by remember { mutableStateOf(false) }
    var savedBrainDumpContent by remember { mutableStateOf<String?>(null) }
    val hasValidNote by viewModel.hasValidNote.collectAsState()
    
    // Increment app open count for breadcrumb tracking
    LaunchedEffect(Unit) {
        viewModel.updateSettings(settings.copy(appOpenCount = settings.appOpenCount + 1))
    }
    
    // Check for valid note on resume
    LaunchedEffect(lifecycleOwner.lifecycle.currentState) {
        if (lifecycleOwner.lifecycle.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.checkBrainDumpStatus()
        }
    }

    // Go Home on Start (Final Trigger)
    LaunchedEffect(isRunning) {
        if (isRunning && settings.goHomeOnStart) {
            delay(500) 
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(startMain)
        }
    }

    // OLED Burn-in Protection
    var burnInOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    LaunchedEffect(settings.burnInProtection) {
        while (settings.burnInProtection) {
            delay(60000) // 1 minute
            val x = (-3..3).random()
            val y = (-3..3).random()
            burnInOffset = IntOffset(x, y)
        }
        if (!settings.burnInProtection) {
            burnInOffset = IntOffset(0, 0)
        }
    }

    // Monochrome Logic
    val isMonochrome = settings.monochromeMode == MonochromeMode.IN_APP_ALWAYS || 
                       (settings.monochromeMode == MonochromeMode.IN_APP_ON_TIMER && isRunning)

    var showHallOfFame by remember { mutableStateOf(false) }
    var showHallOfFameJoinDialog by remember { mutableStateOf(false) }
    var showSupportScreen by remember { mutableStateOf(false) }
    
    // Spotlight Tour State
    var spotlightCurrentStep by remember { mutableStateOf(com.example.fadesleeptimer.ui.components.SpotlightStep.TIMER_PRESETS) }
    var settingsButtonRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var timerPresetsRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var mediaButtonsRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val showSpotlightTour = !settings.hasSeenOnboarding && (
        (spotlightCurrentStep == com.example.fadesleeptimer.ui.components.SpotlightStep.TIMER_PRESETS && timerPresetsRect != null) ||
        (spotlightCurrentStep == com.example.fadesleeptimer.ui.components.SpotlightStep.MEDIA_BUTTONS && mediaButtonsRect != null) ||
        (spotlightCurrentStep == com.example.fadesleeptimer.ui.components.SpotlightStep.SETTINGS && settingsButtonRect != null)
    )

    // Root Container for Global Effects
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { burnInOffset }
    ) {
        // Check permissions initially
        if (!hasPermissions) {
            PermissionsScreen(onAllGranted = { hasPermissions = true })
        } else if (showHallOfFame) {
            // Hall of Fame Screen (Top Level)
            BackHandler {
                showHallOfFame = false
            }
            com.example.fadesleeptimer.ui.components.HallOfFameScreen(
                name = settings.hallOfFameName,
                onClose = { showHallOfFame = false },
                onJoinClick = {
                    if (settings.hasHallOfFameAccess || settings.isBetaMode) {
                        showHallOfFameJoinDialog = true
                    } else {
                        showHallOfFame = false
                        showSupportScreen = true
                    }
                },
                onReset = { viewModel.resetImmortalized() }
            )
        } else if (showSupportScreen) {
            // Support Development Screen (Top Level)
            BackHandler {
                showSupportScreen = false
            }
            com.example.fadesleeptimer.ui.components.SupportScreen(
                settings = settings,
                onClose = { showSupportScreen = false },
                onUnlock = { tier ->
                    viewModel.unlockFeature(tier)
                    if (tier == "sponsor") {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://forms.gle/GaN4oQA91Tephbpi9"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                        }
                    } else if (tier == "rate") {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to browser
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                Toast.makeText(context, "Could not open app store", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Toast.makeText(context, "Beta Mode: Feature Unlocked for Free!", Toast.LENGTH_SHORT).show()
                },
                onJoinHallOfFame = {
                    // Logic for Hall of Fame unlock
                    // In Beta, we just unlock and show dialog immediately
                    viewModel.unlockFeature("hall_of_fame") // Just in case we track it separately
                    showHallOfFameJoinDialog = true
                },
                onViewHallOfFame = {
                    showSupportScreen = false
                    showHallOfFame = true
                },
                onBetaUnlock = {
                    viewModel.updateSettings(settings.copy(isBetaMode = true))
                }
            )
        } else if (showSettings) {
            BackHandler {
                showSettings = false
            }
            SettingsScreen(
                settings = settings,
                sleepHistory = sleepHistory,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onBack = { showSettings = false },
                isTimerActive = isRunning,
                timeLeft = timeLeft,
                onStopTimer = { viewModel.stopTimer(context) },
                onHallOfFameClick = { showHallOfFame = true },
                onSupportClick = { showSupportScreen = true }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Determine which screen to show
                val currentScreen = when {
                    isRunning -> "timer"
                    showBreathingGuide -> "breathing"
                    else -> "setup"
                }
                
                if (currentScreen == "breathing") {
                    // Breathing guide takes full screen edge-to-edge
                    BreathingGuideScreen(
                        pattern = com.example.fadesleeptimer.data.BreathingRepository.getPattern(settings.breathingPatternId),
                        durationMinutes = settings.breathingDurationMinutes,
                        isManualStart = pendingDuration == 0L, // Manual if pendingDuration is 0
                        vibrationEnabled = settings.breathingVibrationEnabled,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        highlightColor = MaterialTheme.colorScheme.secondary,
                        onComplete = {
                            showBreathingGuide = false
                            // Only start timer if pendingDuration > 0 (Auto-start flow)
                            // If manual (pendingDuration == 0), just close.
                            if (pendingDuration > 0) {
                                viewModel.startTimer(context, pendingDuration, settings.playOnStart, settings.dndEnabled)
                            }
                        },
                        onSkip = {
                            showBreathingGuide = false
                            if (pendingDuration > 0) {
                                viewModel.startTimer(context, pendingDuration, settings.playOnStart, settings.dndEnabled)
                            }
                        }
                    )
                } else {
                    // Normal layout with header for setup and timer screens
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // HEADER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Rounded.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    "Sleep Timer",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Supporter Badge
                                if (settings.showSupporterBadge) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.VerifiedUser,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Supporter",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Brain Dump Viewer Button (only if not running and has note)
                                if (!isRunning) {
                                    // Manual Breathing Guide Button
                                    if (settings.breathingGuideEnabled) {
                                        IconButton(onClick = { 
                                            pendingDuration = 0 // Manual mode, no timer after? Or use default?
                                            // Prompt implies manual access "without running the sleep timer afterwards"
                                            // But BreathingGuideScreen takes duration. We'll use settings duration.
                                            showBreathingGuide = true 
                                        }) {
                                            Icon(
                                                Icons.Rounded.Air, 
                                                contentDescription = "Breathing Guide", 
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    if (settings.brainDumpEnabled && hasValidNote) {
                                    IconButton(onClick = { 
                                        savedBrainDumpContent = viewModel.getValidBrainDump()
                                        if (savedBrainDumpContent != null) {
                                            showBrainDumpViewer = true
                                        }
                                    }) {
                                        Icon(
                                            Icons.Rounded.Description, 
                                            contentDescription = "View Note", 
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Dim by default
                                        )
                                    }
                                }
                                }

                                // ON Pill Indicator (only when timer is running)
                                if (isRunning) {
                                    OnIndicatorPill()
                                }
                                // Settings Button with Breadcrumb Badge
                                val activeBreadcrumb = remember(settings) {
                                    com.example.fadesleeptimer.utils.BreadcrumbManager.getActiveBreadcrumb(
                                        settings,
                                        settings.appOpenCount
                                    )
                                }
                                
                                val shouldGlowSettings = settings.hasSeenOnboarding && !settings.hasClickedSettings
                                
                                Box(
                                    modifier = Modifier.pulsingGlow(
                                        enabled = shouldGlowSettings,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                ) {
                                    IconButton(
                                        onClick = {
                                            showSettings = true
                                            if (!settings.hasClickedSettings) {
                                                viewModel.updateSettings(settings.copy(hasClickedSettings = true))
                                            }
                                        },
                                        modifier = Modifier.onGloballyPositioned { coordinates ->
                                            val rect = coordinates.boundsInWindow()
                                            settingsButtonRect = rect
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    
                                    // Breadcrumb indicator dot
                                    if (activeBreadcrumb != com.example.fadesleeptimer.utils.Breadcrumb.NONE) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 4.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // MAIN CONTENT SWITCHER
                        Crossfade(targetState = currentScreen, label = "MainSwitch") { screen ->
                            when (screen) {
                                "timer" -> {
                                    ActiveTimerScreen(
                                        timeLeft = timeLeft,
                                        settings = settings,
                                        onStop = { viewModel.stopTimer(context) },
                                        onExtend = { 
                                            viewModel.extendTimer(context)
                                        }
                                    )
                                }
                                else -> {
                                    SetupScreen(
                                        viewModel = viewModel,
                                        onStartRequest = { duration ->
                                            // Mark timer as started for glow effect
                                            if (!settings.hasStartedTimer) {
                                                viewModel.updateSettings(settings.copy(hasStartedTimer = true))
                                            }
                                            
                                            if (settings.brainDumpEnabled) {
                                                pendingDuration = duration
                                                showBrainDumpDialog = true
                                            } else if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.sleepCalculatorEnabled) {
                                                pendingDuration = duration
                                                showPreFlight = true
                                            } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                                                pendingDuration = duration
                                                showBreathingGuide = true
                                            } else {
                                                viewModel.startTimer(context, duration, settings.playOnStart, settings.dndEnabled)
                                            }
                                        },
                                        onPresetsPositioned = { rect ->
                                            timerPresetsRect = rect
                                        },
                                        onMediaButtonsPositioned = { rect ->
                                            mediaButtonsRect = rect
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Super Dim Overlay
                if (settings.superDim) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                    )
                }

                // Pocket Mode Overlay (Blocks touches)
                if (isPocketModeActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .clickable(enabled = true, onClick = {}) // Consume clicks
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.DoNotTouch, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Pocket Mode Active", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // Monochrome Overlay
        if (isMonochrome) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Gray,
                    blendMode = BlendMode.Saturation
                )
            }
        }

        // Pre-Timer Overlay
        if (showPreFlight) {
            PreTimerScreen(
                settings = settings,
                durationMinutes = pendingDuration,
                onStartTimer = {
                    showPreFlight = false
                    if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                        showBreathingGuide = true
                    } else {
                        viewModel.startTimer(context, pendingDuration, settings.playOnStart, settings.dndEnabled)
                    }
                },
                onCancel = { showPreFlight = false }
            )
        }
        
        // Brain Dump Dialog (Input)
        if (showBrainDumpDialog) {
            BrainDumpDialog(
                onDismiss = { 
                    showBrainDumpDialog = false 
                    // If dismissed without saving, just proceed to pre-flight or start
                    if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.sleepCalculatorEnabled) {
                        showPreFlight = true
                    } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                        showBreathingGuide = true
                    } else {
                        viewModel.startTimer(context, pendingDuration, settings.playOnStart, settings.dndEnabled)
                    }
                },
                onConfirm = { text, hours ->
                    if (text.isNotBlank()) {
                        viewModel.saveBrainDump(text, hours)
                    }
                    showBrainDumpDialog = false
                    
                    // Proceed to next step
                    if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.sleepCalculatorEnabled) {
                        showPreFlight = true
                    } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                        showBreathingGuide = true
                    } else {
                        viewModel.startTimer(context, pendingDuration, settings.playOnStart, settings.dndEnabled)
                    }
                }
            )
        }
        
        // Brain Dump Viewer (Read-Only)
        if (showBrainDumpViewer && savedBrainDumpContent != null) {
            BrainDumpViewer(
                content = savedBrainDumpContent!!,
                onDismiss = { showBrainDumpViewer = false },
                onDelete = {
                    viewModel.clearBrainDump()
                    savedBrainDumpContent = null
                    showBrainDumpViewer = false
                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Spotlight Tour Overlay
        if (showSpotlightTour) {
            val targets = buildMap {
                timerPresetsRect?.let {
                    put(
                        com.example.fadesleeptimer.ui.components.SpotlightStep.TIMER_PRESETS,
                        com.example.fadesleeptimer.ui.components.SpotlightTarget(
                            rect = it,
                            title = "Choose Your Duration ⏱️",
                            description = "Pick how long you want to fade. Your music will slowly fade out over the final 5 minutes, then pause automatically. Perfect for falling asleep!"
                        )
                    )
                }
                mediaButtonsRect?.let {
                    put(
                        com.example.fadesleeptimer.ui.components.SpotlightStep.MEDIA_BUTTONS,
                        com.example.fadesleeptimer.ui.components.SpotlightTarget(
                            rect = it,
                            title = "Quick Media Access 🎵",
                            description = "Tap 'Configure' to add buttons for your favorite media apps (Spotify, YouTube Music, etc.) or remove this button if you don't need it."
                        )
                    )
                }
                settingsButtonRect?.let {
                    put(
                        com.example.fadesleeptimer.ui.components.SpotlightStep.SETTINGS,
                        com.example.fadesleeptimer.ui.components.SpotlightTarget(
                            rect = it,
                            title = "Discover More ⚙️",
                            description = "Tap Settings to unlock breathing exercises, prayer reminders, scripture mode, and 20+ other features. Make Fade your own!"
                        )
                    )
                }
            }
            
            if (targets.isNotEmpty()) {
                com.example.fadesleeptimer.ui.components.SpotlightTour(
                    currentStep = spotlightCurrentStep,
                    targets = targets,
                    onNext = {
                        when (spotlightCurrentStep) {
                            com.example.fadesleeptimer.ui.components.SpotlightStep.TIMER_PRESETS -> {
                                spotlightCurrentStep = com.example.fadesleeptimer.ui.components.SpotlightStep.MEDIA_BUTTONS
                            }
                            com.example.fadesleeptimer.ui.components.SpotlightStep.MEDIA_BUTTONS -> {
                                spotlightCurrentStep = com.example.fadesleeptimer.ui.components.SpotlightStep.SETTINGS
                            }
                            com.example.fadesleeptimer.ui.components.SpotlightStep.SETTINGS -> {
                                viewModel.updateSettings(settings.copy(hasSeenOnboarding = true))
                            }
                        }
                    },
                    onFinish = {
                        viewModel.updateSettings(settings.copy(hasSeenOnboarding = true))
                    }
                )
            }
        }

        // Morning Quality Check Overlay
        val showMorningOverlay by viewModel.showMorningOverlay.collectAsState()
        AnimatedVisibility(
            visible = showMorningOverlay,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            MorningCheckOverlay(
                onYes = {
                    Toast.makeText(context, "Tip: Try selecting a longer duration tonight.", Toast.LENGTH_LONG).show()
                    viewModel.dismissMorningPrompt(context)
                },
                onNo = {
                    Toast.makeText(context, "Great! We'll keep your preferences as is.", Toast.LENGTH_SHORT).show()
                    viewModel.dismissMorningPrompt(context)
                }
            )
        }
        
        // Trigger Morning Check
        LaunchedEffect(Unit) {
            viewModel.checkMorningPrompt()
        }
        
        // Hall of Fame Join Dialog
        if (showHallOfFameJoinDialog) {
            com.example.fadesleeptimer.ui.components.HallOfFameJoinDialog(
                onDismiss = { showHallOfFameJoinDialog = false },
                onConfirm = { name ->
                    viewModel.setImmortalized(name)
                    showHallOfFameJoinDialog = false
                }
            )
        }

        // Beta Banner Ad
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            com.example.fadesleeptimer.ui.components.BetaBannerAd(
                isVisible = isAdVisible,
                onDonateClick = { showSupportScreen = true }
            )
        }
    }
}

@Composable
fun OnIndicatorPill() {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "ON",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
