package com.gollan.fadesleeptimer.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.data.BreathingPattern
import com.gollan.fadesleeptimer.ui.theme.Indigo500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign


enum class BreathingPhase {
    INHALE, HOLD, EXHALE, POST_HOLD
}

@Composable
fun BreathingGuideScreen(
    pattern: BreathingPattern,
    durationMinutes: Int,
    isManualStart: Boolean = false, // True if started manually, false if auto-started
    vibrationEnabled: Boolean = true, // True to enable haptic feedback
    primaryColor: Color = Indigo500, // Main theme color (buttons, lung)
    highlightColor: Color = Color(0xFF818CF8), // Lighter accent (text, borders)
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    // Cleanup: Stop vibrator when this screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            vibrator.cancel()
        }
    }

    val totalTime = durationMinutes * 60 * 1000L
    
    // Allow pattern switching mid-session
    var currentPattern by remember { mutableStateOf(pattern) }
    var phase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var phaseText by remember { mutableStateOf("INHALE") }
    var remainingTime by remember { mutableStateOf(totalTime) }
    var extendedTime by remember { mutableStateOf(0L) } // Track added time
    var isComplete by remember { mutableStateOf(false) }
    
    // Animation State
    val lungScale = remember { Animatable(1f) }
    val lungOpacity = remember { Animatable(0.5f) }
    
    // Timer State


    // Timer update coroutine - updates every second
    LaunchedEffect(Unit) {
        val loopStartTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - loopStartTime < totalTime + extendedTime) {
            remainingTime = (totalTime + extendedTime) - (System.currentTimeMillis() - loopStartTime)
            delay(100) // Update 10 times per second for smooth countdown
        }
        // Session complete
        isComplete = true
        phaseText = "DONE"
        
        // Auto-close after 10 seconds
        delay(10000)
        onComplete()
    }

    LaunchedEffect(currentPattern, isComplete) {
        if (isComplete) return@LaunchedEffect // Don't restart if already complete
        
        val loopStartTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - loopStartTime < totalTime + extendedTime) {
            // 1. INHALE
            phase = BreathingPhase.INHALE
            phaseText = "INHALE"
            
            // Haptic: Continuous rising vibration (simulated with wave)
            if (vibrationEnabled && vibrator.hasVibrator()) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                     // Create a waveform that ramps up intensity? 
                     // Simple approach: Constant vibration or rapid clicks
                     // Waveform might be too complex for simple implementation, let's use a simple pattern
                     // Or just a long vibration
                     vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 100), -1)) 
                 } else {
                     vibrator.vibrate(currentPattern.inhaleMs)
                 }
            }

            // Start animations in parallel
            launch {
                lungScale.animateTo(
                    targetValue = 1.8f,
                    animationSpec = tween(durationMillis = currentPattern.inhaleMs.toInt(), easing = FastOutSlowInEasing)
                )
            }
            launch {
                lungOpacity.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = currentPattern.inhaleMs.toInt(), easing = LinearEasing)
                )
            }
            delay(currentPattern.inhaleMs)

            // 2. HOLD
            if (currentPattern.holdMs > 0) {
                phase = BreathingPhase.HOLD
                phaseText = "HOLD"
                
                // Haptic: Heartbeat (0, 100, 900)
                if (vibrationEnabled && vibrator.hasVibrator()) {
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                         val _repeat = (currentPattern.holdMs / 1000).toInt()
                         // Loop 0 means repeat indefinitely until cancelled
                         vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 900), 0)) 
                         delay(currentPattern.holdMs)
                         vibrator.cancel()
                     } else {
                         delay(currentPattern.holdMs)
                     }
                } else {
                    delay(currentPattern.holdMs)
                }
            }

            // 3. EXHALE
            phase = BreathingPhase.EXHALE
            phaseText = "EXHALE"
            
            // Haptic: Silence or very faint
            vibrator.cancel()

            // Start animations in parallel
            launch {
                lungScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = currentPattern.exhaleMs.toInt(), easing = FastOutSlowInEasing)
                )
            }
            launch {
                lungOpacity.animateTo(
                    targetValue = 0.5f,
                    animationSpec = tween(durationMillis = currentPattern.exhaleMs.toInt(), easing = LinearEasing)
                )
            }
            delay(currentPattern.exhaleMs)

            // 4. POST-HOLD
            if (currentPattern.postHoldMs > 0) {
                phase = BreathingPhase.POST_HOLD
                phaseText = "HOLD" // Or "EMPTY"
                
                // Haptic: Ticks (0, 50, 100, 50)
                if (vibrationEnabled && vibrator.hasVibrator()) {
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                         vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), -1))
                     }
                }
                delay(currentPattern.postHoldMs)
            }
        }
        
        // Snap to final state
        vibrator.cancel()
        isComplete = true
        phaseText = "DONE"
        launch {
            lungScale.animateTo(1.5f, animationSpec = tween(300))
        }
        launch {
            lungOpacity.animateTo(0.7f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Calculate responsive circle size based on screen dimensions
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val availableWidth = screenWidth - 48.dp // Accounting for 24dp padding on each side
        val availableHeight = screenHeight * 0.4f // Use 40% of screen height max
        
        // Base size is smaller of available width or height, with safety margin
        val baseCircleSize = minOf(availableWidth, availableHeight) * 0.9f
        // Max scale is 1.8x, so ensure baseSize * 1.8 fits within screen
        val safeBaseSize = minOf(baseCircleSize, availableWidth / 1.8f)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Close and +1 Min buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // +1 Minute button
                OutlinedButton(
                    onClick = { extendedTime += 60000 },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = highlightColor // Theme highlight
                    ),
                    border = BorderStroke(1.dp, highlightColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("+1 MIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                // Close Button
                IconButton(onClick = onSkip) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pattern Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${currentPattern.textPattern} PATTERN",
                    color = highlightColor, // Theme highlight
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentPattern.name.uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Central Lung
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Main Lung
                Box(
                    modifier = Modifier
                        .size(safeBaseSize)
                        .scale(lungScale.value)
                        .alpha(lungOpacity.value)
                        .background(primaryColor.copy(alpha = 0.2f), CircleShape)
                        .border(3.dp, primaryColor.copy(alpha = 0.8f), CircleShape)
                )

                // Text
                Text(
                    text = phaseText,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mode Selector Buttons (hide when complete)
            if (!isComplete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    com.gollan.fadesleeptimer.data.BreathingRepository.patterns.forEach { modePattern ->
                        val isSelected = modePattern.id == currentPattern.id
                        Button(
                            onClick = { currentPattern = modePattern },
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) primaryColor else Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = modePattern.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = modePattern.textPattern,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Timer (hide when complete)
            if (!isComplete) {
                val minutes = (remainingTime / 1000 / 60).toInt()
                val seconds = (remainingTime / 1000 % 60).toInt()
                Text(
                    text = String.format("%d:%02d Remaining", minutes, seconds),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description (hide when complete)
            if (!isComplete) {
                Text(
                    text = currentPattern.description,
                    color = Color(0xFF94A3B8), // Slate400
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Bottom buttons - different for complete vs active
            if (isComplete) {
                // Session Complete UI
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Session Complete",
                    color = highlightColor, // Theme highlight
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor, // Theme primary
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                ) {
                    Text(
                        if (isManualStart) "Close" else "Sleep Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Active session button
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155), // Slate-700
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    if (!isManualStart) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (isManualStart) "Close" else "SKIP TO TIMER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Bottom gradient overlay to separate controls from breathing circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )
    }
}
