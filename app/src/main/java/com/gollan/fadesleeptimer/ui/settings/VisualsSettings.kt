package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.utils.IconManager
import com.gollan.fadesleeptimer.ui.MonochromeMode

import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Close

@Composable
fun VisualsSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMonochromeDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    SettingsSection(title = stringResource(R.string.settings_category_visuals), breadcrumbSuggestion = breadcrumbSuggestion) {
        // Wedge Clock
        SettingsToggle(
            title = stringResource(R.string.wedge_clock_title),
            desc = stringResource(R.string.wedge_clock_desc),
            icon = Icons.Rounded.AccessTime,
            checked = settings.analogueClock,
            onCheckedChange = { onSettingsChanged(settings.copy(analogueClock = it)) },
            featureId = "analogue_clock",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        // Super Dim Overlay
        SettingsToggle(
            title = stringResource(R.string.super_dim_title),
            desc = stringResource(R.string.super_dim_desc),
            icon = Icons.Rounded.BrightnessLow,
            checked = settings.superDim,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(superDim = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.super_dim_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "super_dim",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        // Grayscale Mode (formerly Monochrome)
        SettingsToggle(
            title = stringResource(R.string.grayscale_mode_title),
            desc = stringResource(R.string.grayscale_mode_desc),
            icon = Icons.Rounded.Contrast,
            checked = settings.monochromeMode != MonochromeMode.OFF,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    // If turning ON, show dialog to choose mode (default to Timer if currently OFF)
                    if (settings.monochromeMode == MonochromeMode.OFF) {
                        onSettingsChanged(settings.copy(monochromeMode = MonochromeMode.IN_APP_ON_TIMER))
                    }
                    showMonochromeDialog = true
                } else {
                    // If turning OFF, set to OFF immediately
                    onSettingsChanged(settings.copy(monochromeMode = MonochromeMode.OFF))
                }
            },
            featureId = "monochrome",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
    }

    if (showMonochromeDialog) {
        MonochromeDialog(
            currentMode = settings.monochromeMode,
            onDismiss = { showMonochromeDialog = false },
            onSelect = { mode ->
                if (mode == MonochromeMode.SYSTEM_NIGHT_FILTER) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && 
                        !android.provider.Settings.canDrawOverlays(context)) {
                        showOverlayPermissionDialog = true
                        showMonochromeDialog = false
                        return@MonochromeDialog
                    }
                    android.widget.Toast.makeText(context, context.getString(R.string.red_night_light_toast), android.widget.Toast.LENGTH_LONG).show()
                }
                onSettingsChanged(settings.copy(monochromeMode = mode))
                showMonochromeDialog = false
            }
        )
    }

    if (showOverlayPermissionDialog) {
        OverlayPermissionDialog(
            onDismiss = { showOverlayPermissionDialog = false },
            onConfirm = {
                try {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showOverlayPermissionDialog = false
            }
        )
    }


}

// ... (IconSelectionScreen and other composables remain unchanged) ...

@Composable
fun MonochromeDialog(
    currentMode: MonochromeMode,
    onDismiss: () -> Unit,
    onSelect: (MonochromeMode) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.grayscale_options_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close_button), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Options
                val options = listOf(
                    Triple(MonochromeMode.IN_APP_ON_TIMER, stringResource(R.string.grayscale_mode_timer), stringResource(R.string.grayscale_desc_timer)),
                    Triple(MonochromeMode.IN_APP_ALWAYS, stringResource(R.string.grayscale_mode_always), stringResource(R.string.grayscale_desc_always)),
                    Triple(MonochromeMode.SYSTEM_NIGHT_FILTER, stringResource(R.string.grayscale_mode_night), stringResource(R.string.grayscale_desc_night))
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { (mode, title, desc) ->
                        val isSelected = currentMode == mode
                        val icon = when (mode) {
                            MonochromeMode.IN_APP_ON_TIMER -> Icons.Rounded.Timer
                            MonochromeMode.IN_APP_ALWAYS -> Icons.Rounded.Smartphone
                            MonochromeMode.SYSTEM_NIGHT_FILTER -> Icons.Rounded.BrightnessLow
                            MonochromeMode.SYSTEM_MANUAL -> Icons.Rounded.Settings
                            else -> Icons.Rounded.Contrast
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelectionScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onBack: () -> Unit
) {
    // Icon Change State
    var showIconChangeDialog by remember { mutableStateOf(false) }
    var pendingIconId by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // ROBUST LAYOUT: Use a simple Box to hold the background and the content
    Box(modifier = Modifier.fillMaxSize()) {
        
        // 1. Background Layer
        MovingBackground()
        
        // 2. Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Safe area
        ) {
            // HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.settings_back_content_description),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        stringResource(R.string.premium_icons_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                blurRadius = 10f
                            )
                        )
                    )
                    Text(
                        stringResource(R.string.premium_icons_subtitle),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.offset(y = (-4).dp) // Pull up slightly
                    )
                }
            }

            // GRID CONTENT
            // Critical Fix: Use weight(1f) to ensure the grid takes up remaining space 
            // and doesn't expand infinitely.
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
                modifier = Modifier.weight(1f) 
            ) {
                items(IconManager.ICONS.size) { index ->
                    val icon = IconManager.ICONS[index]
                    val isSelected = icon.id == settings.appIcon
                    
                    IconItem(
                        icon = icon,
                        isSelected = isSelected,
                        onClick = {
                            pendingIconId = icon.id
                            showIconChangeDialog = true
                        }
                    )
                }
            }
        }
        
        // DIALOG
        if (showIconChangeDialog && pendingIconId != null) {
            val icon = IconManager.ICONS.find { it.id == pendingIconId }
            if (icon != null) {
                AlertDialog(
                    onDismissRequest = { showIconChangeDialog = false },
                    title = { Text(stringResource(R.string.change_icon_title)) },
                    text = { Text(stringResource(R.string.change_icon_message, icon.name)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onSettingsChanged(settings.copy(appIcon = pendingIconId!!))
                                IconManager.setIcon(context, pendingIconId!!)
                                showIconChangeDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.change_icon_button))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showIconChangeDialog = false }) {
                            Text(stringResource(R.string.cancel_button))
                        }
                    },
                    containerColor = Color(0xFF1E293B),
                    titleContentColor = Color.White,
                    textContentColor = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun IconItem(
    icon: IconManager.AppIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // GLOW CONTAINER
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            // 1. The Glow (Behind) - ALWAYS VISIBLE but stronger when selected
            val infiniteTransition = rememberInfiniteTransition(label = "glow")
            // Increased alpha for better visibility
            val targetAlpha = if (isSelected) 1.0f else 0.6f 
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f, // Higher base alpha
                targetValue = targetAlpha,
                animationSpec = infiniteRepeatable(
                    animation = tween(if (isSelected) 1000 else 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            
            val glowColor = Color(icon.color)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = alpha),
                            Color.Transparent
                        )
                    ),
                    // Increased radius to peek out more (was / 1.6f)
                    radius = size.minDimension / 1.3f 
                )
            }

            // 2. The Icon Card
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = if (isSelected) BorderStroke(2.dp, glowColor) else null,
                shadowElevation = 8.dp,
                modifier = Modifier.size(72.dp)
            ) {
                // Fix: Use AndroidView with ImageView to safely render Adaptive Icons (XML)
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.ImageView(ctx).apply {
                            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        imageView.setImageResource(icon.previewResId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 3. Selection Checkmark
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        // NAME
        Text(
            text = icon.name,
            color = if (isSelected) Color(icon.color) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
fun MovingBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Time-based animation for organic movement
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF020617))) {
        val width = size.width
        val height = size.height
        val maxDim = size.maxDimension
        
        // Helper to create wandering points
        fun getPoint(
            t: Float, 
            xPhase: Float, 
            yPhase: Float, 
            xSpeed: Float, 
            ySpeed: Float,
            radius: Float
        ): androidx.compose.ui.geometry.Offset {
            val x = width / 2 + kotlin.math.cos(t * xSpeed + xPhase) * (width * radius)
            val y = height / 2 + kotlin.math.sin(t * ySpeed + yPhase) * (height * radius)
            return androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
        }

        // Deep Space Base
        drawRect(Color(0xFF020617))

        // 1. Deep Indigo Nebula (Slow, Large)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF312E81).copy(alpha = 0.4f), Color.Transparent),
                center = getPoint(time, 0f, 1f, 0.5f, 0.7f, 0.3f),
                radius = maxDim * 1.2f
            )
        )

        // 2. Vibrant Purple/Pink Aurora (Faster, Medium)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC026D3).copy(alpha = 0.25f), Color.Transparent),
                center = getPoint(time, 2f, 0f, 0.8f, 0.6f, 0.4f),
                radius = maxDim * 1.0f
            )
        )

        // 3. Cyan/Teal Drift (Counter-movement)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0891B2).copy(alpha = 0.2f), Color.Transparent),
                center = getPoint(time, 4f, 3f, -0.6f, -0.4f, 0.35f),
                radius = maxDim * 1.1f
            )
        )

        // 4. Golden Amber Glow (Subtle highlight)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD97706).copy(alpha = 0.15f), Color.Transparent),
                center = getPoint(time, 1f, 5f, 0.3f, 0.9f, 0.45f),
                radius = maxDim * 0.8f
            )
        )
        
        // 5. Overlay Noise/Texture (Optional, simulated with small dots)
        // Kept simple for performance, but adds depth
    }
}

@Composable
fun SettingsItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Text(desc, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}





@Composable
fun OverlayPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_required_title)) },
        text = {
            Column {
                Text(stringResource(R.string.overlay_permission_desc))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.overlay_step_1), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.overlay_step_2))
                Text(stringResource(R.string.overlay_step_3))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.open_settings_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
