package com.gollan.fadesleeptimer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.gollan.fadesleeptimer.ui.settings.*
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.components.BedtimeConsistencyScreen
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

@Composable
fun SupportDevelopmentButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF9333EA))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFBCFE8).copy(alpha = alpha), // Pink-200
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.support_development_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            stringResource(R.string.support_development_subtitle),
                            color = Color(0xFFC7D2FE), // Indigo-200
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}



@Composable
fun SettingsScreen(
    settings: AppSettings,
    sleepHistory: List<com.gollan.fadesleeptimer.ui.components.ChartData> = emptyList(),
    onSettingsChanged: (AppSettings) -> Unit,
    onBack: () -> Unit,
    isTimerActive: Boolean = false,
    timeLeft: Long = 0L,
    onStopTimer: () -> Unit = {},
    onHallOfFameClick: () -> Unit,
    onSupportClick: () -> Unit,
    onThemeSelect: () -> Unit, // New callback
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showBedtimeConsistency by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    var showIconSelectionScreen by remember { mutableStateOf(false) }

    if (showPremiumDialog) {
        com.gollan.fadesleeptimer.ui.components.PremiumFeaturesDialog(
            settings = settings,
            onDismiss = { showPremiumDialog = false },
            onToggleSupporterBadge = { 
                onSettingsChanged(settings.copy(showSupporterBadge = it)) 
            },
            onCustomizeIcons = { 
                showPremiumDialog = false
                showIconSelectionScreen = true 
            },
            onThemeSelect = {
                showPremiumDialog = false
                onThemeSelect()
            },
            onViewHallOfFame = {
                showPremiumDialog = false
                onHallOfFameClick()
            },
            onOpenSponsorForm = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://forms.gle/GaN4oQA91Tephbpi9"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Could not open link", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onBetaUnlock = {
                onSettingsChanged(settings.copy(isPremiumUnlocked = true))
            }
        )
    }

    if (showIconSelectionScreen) {
        com.gollan.fadesleeptimer.ui.settings.IconSelectionScreen(
            settings = settings,
            onSettingsChanged = onSettingsChanged,
            onBack = { showIconSelectionScreen = false }
        )

    } else if (showBedtimeConsistency) {
        com.gollan.fadesleeptimer.ui.components.SleepDashboard(
            settings = settings,
            sleepHistory = sleepHistory,
            onBack = { showBedtimeConsistency = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        // Main Settings Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.settings_back_content_description))
                    }
                    Text(
                        stringResource(R.string.settings_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Timer Pill (Aligned right)
                AnimatedVisibility(
                    visible = isTimerActive,
                    enter = fadeIn() + androidx.compose.animation.expandHorizontally(),
                    exit = fadeOut() + androidx.compose.animation.shrinkHorizontally()
                ) {
                    TimerCapsule(
                        timeLeft = timeLeft,
                        onStop = onStopTimer
                    )
                }
            }

            // Scrollable Settings List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Support Developer Button
                item {
                    SupportDevelopmentButton(onClick = onSupportClick)

                    // Premium Features Button (Only visible if unlocked)
                    if (settings.isPremiumUnlocked || settings.hasSupporterBadge || settings.hasVisualPack || settings.hasHallOfFameAccess || settings.hasSponsorAccess) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PremiumFeaturesButton(onClick = { showPremiumDialog = true })
                    }
                }

                item {
                    AwesomeFeaturesSettings(
                        settings = settings,
                        onSettingsChanged = onSettingsChanged,
                        breadcrumbSuggestion = breadcrumbSuggestion
                    )
                }

                item {
                    PersonalizationSettings(settings, onSettingsChanged, breadcrumbSuggestion)
                }

                item {
                    VisualsSettings(settings, onSettingsChanged, breadcrumbSuggestion)
                }

                item {
                    AudioSettings(settings, onSettingsChanged, breadcrumbSuggestion)
                }

                item {
                    SystemSettings(settings, onSettingsChanged, breadcrumbSuggestion)
                }

                item {
                    ControlsSettings(settings, onSettingsChanged, breadcrumbSuggestion)
                }

                item {
                    InfoSettings(
                        settings = settings,
                        sleepHistory = sleepHistory,
                        onSettingsChanged = onSettingsChanged,
                        onSleepJourneyClick = { showBedtimeConsistency = true },
                        onHallOfFameClick = onHallOfFameClick
                    )
                }
            }
        }
    }
    }
}


@Composable
fun TimerCapsule(
    timeLeft: Long,
    onStop: () -> Unit
) {
    // Pulsing animation for countdown text
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
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Countdown Text
            Text(
                text = "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )

            // Stop Button
            IconButton(
                onClick = onStop,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Rounded.Stop,
                    contentDescription = stringResource(R.string.stop_timer_content_description),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TimerPresetsEditor(
    presets: List<Int>,
    onPresetsChanged: (List<Int>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.timer_presets_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Text(stringResource(R.string.timer_presets_subtitle), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
            Icon(
                if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Chips Grid
                SimpleFlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp
                ) {
                    presets.forEach { mins ->
                        PresetChip(
                            text = if (mins >= 60) "${mins / 60}h" else "${mins}m",
                            onDelete = {
                                onPresetsChanged(presets - mins)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add & Reset
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { if (presets.size < 8) showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = presets.size < 8
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_button), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    TextButton(onClick = { onPresetsChanged(listOf(5, 10, 15, 30, 45, 60)) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.reset_default_button), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPresetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { mins ->
                if (!presets.contains(mins)) {
                    onPresetsChanged((presets + mins).sorted())
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PresetChip(text: String, onDelete: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: androidx.compose.ui.unit.Dp = 0.dp,
    verticalGap: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalGapPx = horizontalGap.roundToPx()
        val verticalGapPx = verticalGap.roundToPx()
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + horizontalGapPx
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)
        
        val totalHeight = rows.sumOf { row -> row.maxOf { it.height } } + (rows.size - 1) * verticalGapPx
        
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalGapPx
                }
                y += rowHeight + verticalGapPx
            }
        }
    }
}

@Composable
fun AddPresetDialog(onDismiss: () -> Unit, onAdd: (Int) -> Unit) {
    var input by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.add_preset_dialog_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(R.string.minutes_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            input.toIntOrNull()?.let { onAdd(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.add_button), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null,
    hasWarning: Boolean = false,
    isCollapsible: Boolean = false,
    initiallyCollapsed: Boolean = false,
    content: @Composable () -> Unit
) {
    var collapsed by remember { mutableStateOf(initiallyCollapsed) }
    val rotation by animateFloatAsState(targetValue = if (collapsed) 0f else 180f, label = "arrow_rotation")

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isCollapsible) { collapsed = !collapsed }
                    .padding(end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title.uppercase(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .padding(bottom = 0.dp)
                    )
                    if (hasWarning) {
                        val infiniteTransition = rememberInfiniteTransition(label = "warning_blink")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "warning_blink_alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = alpha), CircleShape)
                        )
                    }
                }
                
                if (isCollapsible) {
                     Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (collapsed) stringResource(R.string.expand_content_description) else stringResource(R.string.collapse_content_description),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .rotate(rotation)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = !collapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    desc: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    featureId: String? = null,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val isSuggested = featureId != null && breadcrumbSuggestion?.featureId == featureId
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                if (isSuggested) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    if (isSuggested) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.new_label),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(desc, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}


@Composable
fun PremiumFeaturesButton(onClick: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = widthPx + 300f, // Ensure it goes past the end
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )

        val brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF59E0B), // Amber
                Color(0xFFFCD34D), // Amber-300
                Color(0xFFF59E0B)
            ),
            start = androidx.compose.ui.geometry.Offset(translateAnim - 300f, 0f),
            end = androidx.compose.ui.geometry.Offset(translateAnim, 300f) // Diagonal effect
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .background(brush) // Use the shimmer brush
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                stringResource(R.string.premium_features_title),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            Text(
                                stringResource(R.string.premium_features_subtitle),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
