package com.gollan.fadesleeptimer.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.delay
import androidx.core.content.ContextCompat
import com.gollan.fadesleeptimer.R
import com.gollan.fadesleeptimer.ui.components.*
import com.gollan.fadesleeptimer.util.SensorHelper
import com.gollan.fadesleeptimer.viewmodel.TimerViewModel
import com.gollan.fadesleeptimer.viewmodel.SettingsViewModel
import androidx.compose.ui.res.stringResource
@Composable
fun MainScreen(
    audioViewModel: com.gollan.fadesleeptimer.viewmodel.AudioViewModel,
    settingsViewModel: SettingsViewModel,
    sensorHelper: SensorHelper,
    viewModel: TimerViewModel,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    
    // Hoisted Strings for Callbacks
    val timerStartedToast = stringResource(R.string.timer_started_minimizing_toast)
    val pocketLockedToast = stringResource(R.string.pocket_mode_unlocked_toast)
    val pocketUnlockInstruction = stringResource(R.string.pocket_mode_unlock_instruction)
    val morningYesToast = stringResource(R.string.morning_check_yes_toast)
    val morningNoToast = stringResource(R.string.morning_check_no_toast)
    val shakeDisabledToast = stringResource(R.string.shake_disabled_toast)
    
    // Initialize volume slider with current system volume
    LaunchedEffect(Unit) {
        audioViewModel.initVolume(context)
    }

    // Listen for system volume changes
    DisposableEffect(context) {
        val volumeReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                    audioViewModel.initVolume(context)
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

    val settings by settingsViewModel.settings.collectAsState()
    val breadcrumbSuggestion by settingsViewModel.breadcrumbSuggestion.collectAsState()
    val sleepHistory by viewModel.sleepHistory.collectAsState()
    

    
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
        sensorHelper.updateConfig(
            faceDown = settings.faceDownStart && !isRunning,
            shake = false, // Handled by TimerService
            pocketMode = settings.pocketMode
        )
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
    var pendingDuration by remember { mutableLongStateOf(0L) }

    // Brain Dump State
    var showBrainDumpDialog by remember { mutableStateOf(false) }
    var showBrainDumpViewer by remember { mutableStateOf(false) }
    var showBreathingGuide by remember { mutableStateOf(false) }
    var savedBrainDumpContent by remember { mutableStateOf<String?>(null) }
    val hasValidNote by viewModel.hasValidNote.collectAsState()
    
    // Check for valid note on resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkBrainDumpStatus()
                settingsViewModel.checkMorningPrompt()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Open Brain Dump from Notification
    val triggerOpenBrainDump by viewModel.triggerOpenBrainDump.collectAsState()
    LaunchedEffect(triggerOpenBrainDump) {
        if (triggerOpenBrainDump) {
            savedBrainDumpContent = viewModel.getValidBrainDump()
            if (savedBrainDumpContent != null) {
                showBrainDumpViewer = true
            }
            viewModel.consumeOpenBrainDump()
        }
    }

    // Go Home on Start (Final Trigger)
    // Go Home on Start (Event Triggered)
    LaunchedEffect(Unit) {
        viewModel.timerStartedEvent.receiveAsFlow().collect {
            if (settings.goHomeOnStart) {
                 Toast.makeText(context, timerStartedToast, Toast.LENGTH_SHORT).show()
                 delay(500)
                 val startMain = Intent(Intent.ACTION_MAIN)
                 startMain.addCategory(Intent.CATEGORY_HOME)
                 startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                 context.startActivity(startMain)
            }
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
        // Reset offset when burn-in protection is disabled (loop exits when condition becomes false)
        burnInOffset = IntOffset(0, 0)
    }

    // Monochrome Logic
    val isMonochrome = settings.monochromeMode == MonochromeMode.IN_APP_ALWAYS || 
                       (settings.monochromeMode == MonochromeMode.IN_APP_ON_TIMER && isRunning)

    var showHallOfFame by remember { mutableStateOf(false) }
    var showHallOfFameJoinDialog by remember { mutableStateOf(false) }
    var showSupportScreen by remember { mutableStateOf(false) }
    var showThemeSelection by remember { mutableStateOf(false) }

    // Listen for Support Event from MainActivity/ViewModel
    val openSupportEvent by settingsViewModel.openSupportEvent.collectAsState()
    LaunchedEffect(openSupportEvent) {
        if (openSupportEvent) {
            showSupportScreen = true
            settingsViewModel.consumeOpenSupport()
        }
    }

    // Spotlight Tour State
    var showSpotlightTour by remember { mutableStateOf(false) }
    var spotlightCurrentStep by remember { mutableStateOf(SpotlightStep.TIMER_PRESETS) }
    var timerPresetsRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var mediaButtonsRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var settingsButtonRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // Trigger Onboarding
    LaunchedEffect(settings.hasSeenOnboarding, isRunning) {
        if (!settings.hasSeenOnboarding && !isRunning) {
            showSpotlightTour = true
        }
    }

    // Root Container for Global Effects
    com.gollan.fadesleeptimer.ui.theme.FadeSleepTimerTheme(
        themeName = settings.theme,
        overrideMonochrome = isMonochrome
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { burnInOffset }
        ) {
             // ...
             // (Content remains, but overlays are removed below this block in the original file, 
             //  so I just need to verify where they were.
             //  Wait, the overlays were AFTER the Box content in the original file.
             //  I need to target the Theme call at the top and the Overlays at the bottom.
             //  I'll do the Theme call first.)

        // ... (Existing content) ...
        // Check permissions initially
        if (!hasPermissions) {
            PermissionsScreen(onAllGranted = { hasPermissions = true })
        } else if (showHallOfFame) {
            // ...
            BackHandler {
                showHallOfFame = false
            }
            HallOfFameScreen(
                name = settings.hallOfFameName,
                hasAccess = settings.hasHallOfFameAccess,
                onClose = { showHallOfFame = false },
                onJoinClick = {
                    if (settings.hasHallOfFameAccess || settings.isPremiumUnlocked) {
                        showHallOfFameJoinDialog = true
                    } else {
                        showHallOfFame = false
                        showSupportScreen = true
                    }
                },
                onSupportClick = {
                    showHallOfFame = false
                    showSupportScreen = true
                },
                onReset = { settingsViewModel.resetImmortalized() }
            )
        } else if (showSupportScreen) {
            // ... (Support Screen Logic)
            BackHandler {
                showSupportScreen = false
            }
            SupportScreen(
                settings = settings,
                onClose = { showSupportScreen = false },
                onUnlock = { tier ->
                    val activity = context as? android.app.Activity
                    
                    when (tier) {
                        "visual_pack" -> {
                            if (activity != null) settingsViewModel.purchaseProduct(activity, "visual_pack")
                            else Toast.makeText(context, "Error: Cannot start purchase", Toast.LENGTH_SHORT).show()
                        }
                        "sponsor" -> {
                            // Paid Item: Sponsor (Old Link logic removed/fallback kept if simple link preferred, but user asked for billing)
                            // User asked specifically for "sponsor_feature" product ID.
                            if (activity != null) settingsViewModel.purchaseProduct(activity, "sponsor_feature")
                            else Toast.makeText(context, "Error: Cannot start purchase", Toast.LENGTH_SHORT).show()
                        }
                        "lego" -> {
                             if (activity != null) settingsViewModel.purchaseProduct(activity, "lego_tier_ultimate")
                             else Toast.makeText(context, "Error: Cannot start purchase", Toast.LENGTH_SHORT).show()
                        }
                        "rate" -> {
                            settingsViewModel.unlockFeature(tier)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback to browser
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open app store", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        "share" -> {
                            settingsViewModel.unlockFeature(tier)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "I'm loving this free app I found. Check it out: https://play.google.com/store/apps/details?id=${context.packageName}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(shareIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                             // Generic unlock for free items or fallbacks
                             settingsViewModel.unlockFeature(tier)
                        }
                    }
                },
                onJoinHallOfFame = {
                     // Updated: Use Billing for Hall of Fame
                     val activity = context as? android.app.Activity
                     if (activity != null) {
                         settingsViewModel.purchaseProduct(activity, "hall_of_fame_icons")
                     } else {
                         Toast.makeText(context, "Error: Cannot start purchase", Toast.LENGTH_SHORT).show()
                     }
                },
                onViewHallOfFame = {
                    showSupportScreen = false
                    showHallOfFame = true
                },
                onBetaUnlock = {
                    settingsViewModel.updateSettings(settings.copy(isPremiumUnlocked = true))
                },
                onThemeSelect = {
                    showSupportScreen = false
                    showThemeSelection = true
                }
            )
        } else if (showThemeSelection) {
            BackHandler {
                showThemeSelection = false
            }
            ThemeSelectionScreen(
                settings = settings,
                onThemeSelected = { themeId ->
                    settingsViewModel.updateSettings(settings.copy(theme = themeId))
                },
                onClose = { showThemeSelection = false }
            )
        } else if (showSettings) {
            BackHandler {
                showSettings = false
            }
            SettingsScreen(
                settings = settings,
                sleepHistory = sleepHistory,
                onSettingsChanged = { settingsViewModel.updateSettings(it) },
                onBack = { showSettings = false },
                isTimerActive = isRunning,
                timeLeft = timeLeft,
                onStopTimer = { viewModel.stopTimer(context) },
                onHallOfFameClick = { showHallOfFame = true },
                onSupportClick = { showSupportScreen = true },
                onThemeSelect = {
                    showSettings = false
                    showThemeSelection = true
                },
                breadcrumbSuggestion = breadcrumbSuggestion
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
                        pattern = com.gollan.fadesleeptimer.data.BreathingRepository.getPattern(settings.breathingPatternId),
                        durationMinutes = settings.breathingDurationMinutes,
                        isManualStart = pendingDuration == 0L, // Manual if pendingDuration is 0
                        vibrationEnabled = settings.breathingVibrationEnabled,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        highlightColor = MaterialTheme.colorScheme.secondary,
                        onComplete = {
                            showBreathingGuide = false
                            // If manual (pendingDuration == 0), just close.
                            if (pendingDuration > 0) {
                                viewModel.startTimer(context, pendingDuration, settings)
                            }
                        },
                        onSkip = {
                            showBreathingGuide = false
                            if (pendingDuration > 0) {
                                viewModel.startTimer(context, pendingDuration, settings)
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
                                Icon(painterResource(R.drawable.ic_launcher_monochrome), contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                            Text(
                                stringResource(R.string.app_title),
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
                                                stringResource(R.string.supporter_badge),
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
                                                contentDescription = stringResource(R.string.breathing_guide_cd), 
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
                                            contentDescription = stringResource(R.string.view_note_cd), 
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Dim by default
                                        )
                                    }
                                }
                                }

                                // ON Pill Indicator (only when timer is running)
                                if (isRunning) {
                                    OnIndicatorPill()
                                }
                                
                                // SETTINGS BUTTON (Target for Spotlight)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.onGloballyPositioned { coordinates ->
                                        settingsButtonRect = coordinates.boundsInWindow()
                                    }
                                ) {
                                    // Encouragement Glow
                                    if (settings.hasSeenOnboarding && !settings.hasClickedSettings) {
                                        PulsingGlow(modifier = Modifier.size(40.dp))
                                    }
                                    
                                    IconButton(onClick = { 
                                        settingsViewModel.markSettingsClicked()
                                        showSettings = true 
                                    }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (!settings.hasClickedSettings) {
                                                PulsingGlow(
                                                    modifier = Modifier.size(48.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            Box {
                                                Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))



                        // Determine Content based on Screen
                        when {
                            // 1. Timer Running -> Active Timer Screen
                            isRunning -> {
                                ActiveTimerScreen(
                                    timeLeft = timeLeft,
                                    settings = settings,
                                    isFaceDown = sensorHelper.isFaceDown,
                                    onStop = { viewModel.stopTimer(context) },
                                    onExtend = { viewModel.extendTimer(context) },
                                    onSupportClick = { showSupportScreen = true }
                                )
                            }
                            
                            // 2. Setup Screen (Default)
                            else -> {
                                    // Pass modifiers to SetupScreen to capture targets
                                    SetupScreen(
                                        viewModel = viewModel,
                                        settingsViewModel = settingsViewModel,
                                        audioViewModel = audioViewModel,
                                        onPresetsPositioned = { rect -> timerPresetsRect = rect },
                                        onMediaButtonsPositioned = { rect -> mediaButtonsRect = rect },
                                        onStartRequest = { duration ->
                                            settingsViewModel.markTimerClicked()
                                            if (settings.brainDumpEnabled) {
                                                pendingDuration = duration
                                                showBrainDumpDialog = true
                                            } else if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.wisdomQuotesEnabled || settings.sleepCalculatorEnabled) {
                                                pendingDuration = duration
                                                showPreFlight = true
                                            } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                                                pendingDuration = duration
                                                showBreathingGuide = true
                                            } else {
                                                viewModel.startTimer(context, duration, settings)
                                            }
                                        }
                                    )
                            }
                        }
                        }
                    }
                }
                
                // Super Dim Overlay
                AnimatedVisibility(
                    visible = settings.superDim && isRunning,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut(animationSpec = tween(1000))
                ) {
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
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { 
                                        isPocketModeActive = false 
                                        Toast.makeText(context, pocketLockedToast, Toast.LENGTH_SHORT).show()
                                    },
                                    onTap = { 
                                        Toast.makeText(context, pocketUnlockInstruction, Toast.LENGTH_SHORT).show() 
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.DoNotTouch, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.pocket_mode_active), color = Color.White, fontSize = 18.sp)
                            Text(stringResource(R.string.pocket_mode_unlock_instruction), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            
            // Box intentionally left open here to wrap following overlays

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
                        viewModel.startTimer(context, pendingDuration, settings)
                    }
                },
                onCancel = { showPreFlight = false }
            )
        }
        
        // Brain Dump Dialog (Input)
        if (showBrainDumpDialog) {
            BrainDumpDialog(
                settings = settings,
                onDismiss = { 
                    showBrainDumpDialog = false 
                    // If dismissed without saving, just proceed to pre-flight or start
                    if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.wisdomQuotesEnabled || settings.sleepCalculatorEnabled) {
                        showPreFlight = true
                    } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                        showBreathingGuide = true
                    } else {
                        viewModel.startTimer(context, pendingDuration, settings)
                    }
                },
                onConfirm = { text, hours ->
                    if (text.isNotBlank()) {
                        viewModel.saveBrainDump(text, hours)
                    }
                    showBrainDumpDialog = false
                    
                    // Proceed to next step
                    if (settings.airplaneModeReminder || settings.prayerReminder || settings.scriptureModeEnabled || settings.wisdomQuotesEnabled || settings.sleepCalculatorEnabled) {
                        showPreFlight = true
                    } else if (settings.breathingGuideEnabled && settings.breathingAutoStart) {
                        showBreathingGuide = true
                    } else {
                        viewModel.startTimer(context, pendingDuration, settings)
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

        // Morning Quality Check Overlay
        val showMorningOverlay by settingsViewModel.showMorningOverlay.collectAsState()
        AnimatedVisibility(
            visible = showMorningOverlay,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            MorningCheckOverlay(
                onYes = {
                    Toast.makeText(context, morningYesToast, Toast.LENGTH_LONG).show()
                    settingsViewModel.dismissMorningPrompt(context)
                },
                onNo = {
                    Toast.makeText(context, morningNoToast, Toast.LENGTH_SHORT).show()
                    settingsViewModel.dismissMorningPrompt(context)
                }
            )
        }
        
        // Trigger Morning Check
        LaunchedEffect(Unit) {
            settingsViewModel.checkMorningPrompt()
        }
        
        // Hall of Fame Join Dialog
        if (showHallOfFameJoinDialog) {
            HallOfFameJoinDialog(
                onDismiss = { showHallOfFameJoinDialog = false },
                onConfirm = { name ->
                    settingsViewModel.setImmortalized(name)
                    showHallOfFameJoinDialog = false
                }
            )
        }
        
        // Shake Feature Onboarding Dialog
        val showShakeOnboarding by viewModel.showShakeOnboarding.collectAsState()
        if (showShakeOnboarding) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissShakeOnboarding() },
                title = { Text(stringResource(R.string.shake_dialog_title)) },
                text = { Text(stringResource(R.string.shake_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.onShakeOnboardingDecision(context, true)
                    }) {
                        Text(stringResource(R.string.keep_on_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.onShakeOnboardingDecision(context, false)
                        settingsViewModel.updateSettings(settings.copy(shakeToExtend = false))
                        Toast.makeText(context, shakeDisabledToast, Toast.LENGTH_SHORT).show()
                    }) {
                        Text(stringResource(R.string.turn_off_button))
                    }
                },
                icon = { Icon(Icons.Rounded.Vibration, contentDescription = null) }
            )
        }

        // App Picker Dialog
        val showAppPicker by viewModel.showAppPicker.collectAsState()
        if (showAppPicker != null) {
            AppPickerDialog(
                onDismiss = { viewModel.dismissAppPicker() },
                onAppSelected = { packageName ->
                    viewModel.saveSelectedApp(context, showAppPicker!!, packageName)
                    viewModel.dismissAppPicker()
                }
            )
        }

        // Spotlight Tour Overlay
        if (showSpotlightTour) {
            val timerPresetsTitle = stringResource(R.string.spotlight_presets_title)
            val timerPresetsDesc = stringResource(R.string.spotlight_presets_desc)
            val mediaButtonsTitle = stringResource(R.string.spotlight_media_title)
            val mediaButtonsDesc = stringResource(R.string.spotlight_media_desc)
            val settingsTitle = stringResource(R.string.spotlight_settings_title)
            val settingsDesc = stringResource(R.string.spotlight_settings_desc)

            val targets = buildMap {
                timerPresetsRect?.let {
                    put(
                        SpotlightStep.TIMER_PRESETS,
                        SpotlightTarget(
                            rect = it,
                            title = timerPresetsTitle,
                            description = timerPresetsDesc
                        )
                    )
                }
                mediaButtonsRect?.let {
                    put(
                        SpotlightStep.MEDIA_BUTTONS,
                        SpotlightTarget(
                            rect = it,
                            title = mediaButtonsTitle,
                            description = mediaButtonsDesc
                        )
                    )
                }
                settingsButtonRect?.let {
                    put(
                        SpotlightStep.SETTINGS,
                        SpotlightTarget(
                            rect = it,
                            title = settingsTitle,
                            description = settingsDesc
                        )
                    )
                }
            }
            
            if (targets.isNotEmpty()) {
                SpotlightTour(
                    currentStep = spotlightCurrentStep,
                    targets = targets,
                    onNext = {
                        when (spotlightCurrentStep) {
                            SpotlightStep.TIMER_PRESETS -> {
                                spotlightCurrentStep = SpotlightStep.MEDIA_BUTTONS
                            }
                            SpotlightStep.MEDIA_BUTTONS -> {
                                spotlightCurrentStep = SpotlightStep.SETTINGS
                            }
                            SpotlightStep.SETTINGS -> {
                                settingsViewModel.markOnboardingSeen()
                                showSpotlightTour = false
                            }
                        }
                    },
                    onFinish = {
                        settingsViewModel.markOnboardingSeen()
                        showSpotlightTour = false
                    }
                )
            }
        }
    } // Close Box
} // Close Theme
} // Close MainScreen
}

