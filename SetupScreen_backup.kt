package com.example.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadesleeptimer.viewmodel.TimerViewModel
import com.example.fadesleeptimer.ui.theme.*

@Composable
fun SetupScreen(
    viewModel: TimerViewModel,
    onStartRequest: (Long) -> Unit,
    onPresetsPositioned: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null,
    onMediaButtonsPositioned: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null
) {
    val context = LocalContext.current
    val activeSound by viewModel.activeSound.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Brand Colors
    val spotifyGreen = Color(0xFF4ADE80)
    val youtubeRed = Color(0xFFF87171)
    val audibleOrange = Color(0xFFFB923C)

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

        // 1. SOURCE TOGGLE
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                SourceButton(
                    text = "System Audio",
                    isActive = activeSound == "system",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSound("system") }
                )
                SourceButton(
                    text = "Built-in Noise",
                    isActive = activeSound != "system",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSound("deep") }
                )
            }
        }

        // 2. MIDDLE SECTION
        if (activeSound == "system") {
            val settings by viewModel.settings.collectAsState()
            val hasAnyButtonVisible = settings.configureButtonVisible ||
                                     settings.spotifyButtonVisible || 
                                     settings.ytMusicButtonVisible || 
                                     settings.audibleButtonVisible || 
                                     settings.youtubeButtonVisible ||
                                     settings.youversionButtonVisible ||
                                     settings.appleMusicButtonVisible ||
                                     settings.podcastsButtonVisible ||
                                     settings.amazonMusicButtonVisible ||
                                     settings.streamingButtonVisible ||
                                     settings.calmButtonVisible ||
                                     settings.headspaceButtonVisible
            
            // --- APP SHORTCUTS GRID ---
            if (hasAnyButtonVisible) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (settings.spotifyButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Spotify",
                                icon = Icons.Rounded.MusicNote,
                                brandColor = spotifyGreen,
                                buttonId = "spotify",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.ytMusicButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "YT Music",
                                icon = Icons.Rounded.PlayCircle,
                                brandColor = youtubeRed,
                                buttonId = "youtube_music",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.audibleButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Audible",
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                brandColor = audibleOrange,
                                buttonId = "audible",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.youtubeButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "YouTube",
                                icon = Icons.Rounded.SmartDisplay,
                                brandColor = youtubeRed,
                                buttonId = "youtube",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.youversionButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Bible",
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                brandColor = Color(0xFF8B4513), // Brown/Rust
                                buttonId = "youversion",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.appleMusicButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Apple Music",
                                icon = Icons.Rounded.MusicNote,
                                brandColor = Color(0xFFFFFFFF), // White
                                buttonId = "apple_music",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.podcastsButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Podcasts",
                                icon = Icons.Rounded.Mic,
                                brandColor = Color(0xFF892CA0), // Purple
                                buttonId = "podcasts",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.amazonMusicButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Amazon Music",
                                icon = Icons.Rounded.MusicNote,
                                brandColor = Color(0xFF1DB0C4), // Teal
                                buttonId = "amazon_music",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.streamingButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Streaming",
                                icon = Icons.Rounded.Tv,
                                brandColor = Color(0xFF4A90E2), // Blue
                                buttonId = "streaming",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.calmButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Calm",
                                icon = Icons.Rounded.Spa,
                                brandColor = Color(0xFF169BDB), // Calm blue
                                buttonId = "calm",
                                viewModel = viewModel
                            )
                        }
                    }
                    if (settings.headspaceButtonVisible) {
                        item {
                            AppShortcutButton(
                                text = "Headspace",
                                icon = Icons.Rounded.SelfImprovement,
                                brandColor = Color(0xFFF4794E), // Headspace orange
                                buttonId = "headspace",
                                viewModel = viewModel
                            )
                        }
                    }
                    
                    // Configure Buttons option (conditional)
                    if (settings.configureButtonVisible) {
                        item {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            
                            val borderColor = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            val containerColor = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            val contentColor = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            
                            val shouldGlowConfigure = settings.hasSeenOnboarding && !settings.hasClickedConfigure
                            
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier
                                    .height(80.dp)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        viewModel.showButtonConfig()
                                        if (!settings.hasClickedConfigure) {
                                            viewModel.updateSettings(settings.copy(hasClickedConfigure = true))
                                        }
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        onMediaButtonsPositioned?.invoke(coordinates.boundsInWindow())
                                    }
                                    .pulsingGlow(
                                        enabled = shouldGlowConfigure,
                                        shape = RoundedCornerShape(16.dp),
                                        glowColor = MaterialTheme.colorScheme.primary
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Settings,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "CONFIGURE",
                                        color = contentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Show periodic toast reminder (once every 3 months)
                val toastContext = LocalContext.current
                LaunchedEffect(Unit) {
                    val prefs = toastContext.getSharedPreferences("app_buttons", android.content.Context.MODE_PRIVATE)
                    val lastToastTime = prefs.getLong("last_config_toast", 0L)
                    val currentTime = System.currentTimeMillis()
                    val threeMonthsMs = 90L * 24 * 60 * 60 * 1000 // 90 days in milliseconds
                    
                    if (currentTime - lastToastTime >= threeMonthsMs) {
                        android.widget.Toast.makeText(
                            toastContext,
                            "Tip: Configure media buttons from Settings → Wellness",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        
                        // Save the timestamp
                        prefs.edit().putLong("last_config_toast", currentTime).apply()
                    }
                }
            }
        } else {
            // --- NOISE SELECTION GRID ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NoiseOptionButton(
                    text = "Deep",
                    icon = Icons.Rounded.GraphicEq,
                    isSelected = activeSound == "deep",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSound("deep") }
                )
                NoiseOptionButton(
                    text = "Fan",
                    icon = Icons.Rounded.Toys,
                    isSelected = activeSound == "fan",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSound("fan") }
                )
                NoiseOptionButton(
                    text = "Rain",
                    icon = Icons.Rounded.WaterDrop,
                    isSelected = activeSound == "rain",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSound("rain") }
                )
            }
        }

        // 3. VOLUME SLIDER
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "VOLUME",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text("${volume.toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = volume,
                    onValueChange = { viewModel.updateVolume(context, it) },
                    valueRange = 0f..150f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // 4. DURATION GRID
        Column {
            Text(
                text = "SELECT DURATION",
                color = Slate700,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Use dynamic presets from settings
            val presets = settings.timerPresets
            val columns = if (presets.size <= 3) 1 else 2
            val shouldGlowPresets = settings.hasSeenOnboarding && !settings.hasStartedTimer
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        onPresetsPositioned?.invoke(coordinates.boundsInWindow())
                    }
                    .pulsingGlow(
                        enabled = shouldGlowPresets,
                        shape = RoundedCornerShape(16.dp),
                        glowColor = MaterialTheme.colorScheme.primary
                    )
            ) {
                items(presets) { min ->
                    PresetCard(
                        minutes = min,
                        isDefault = min == settings.defaultDuration,
                        onClick = { onStartRequest(min.toLong()) }
                    )
                }
            }
        }
    }
    
    // Show app picker dialog when needed
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    showAppPicker?.let { buttonId ->
        AppPickerDialog(
            onDismiss = { viewModel.dismissAppPicker() },
            onAppSelected = { packageName ->
                viewModel.saveSelectedApp(context, buttonId, packageName)
                viewModel.dismissAppPicker()
            }
        )
    }
    
    // Show button visibility config dialog
    val showButtonConfig by viewModel.showButtonConfig.collectAsState()
    if (showButtonConfig) {
        val dialogSettings by viewModel.settings.collectAsState()
        ButtonVisibilityDialog(
            currentSettings = dialogSettings,
            onDismiss = { viewModel.hideButtonConfig() },
            onSave = { updatedSettings ->
                viewModel.updateSettings(updatedSettings)
            }
        )
    }
}

