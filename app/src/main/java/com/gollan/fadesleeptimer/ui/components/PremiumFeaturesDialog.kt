package com.gollan.fadesleeptimer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import com.gollan.fadesleeptimer.ui.AppSettings

import kotlinx.coroutines.delay

@Composable
fun PremiumFeaturesDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onToggleSupporterBadge: (Boolean) -> Unit,
    onCustomizeIcons: () -> Unit,
    onThemeSelect: () -> Unit, // New callback for themes
    onViewHallOfFame: () -> Unit,
    onOpenSponsorForm: () -> Unit,
    onBetaUnlock: () -> Unit
) {
    var betaUnlockClicks by remember { mutableStateOf(0) }
    var clickFeedback by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Clear feedback
    LaunchedEffect(clickFeedback) {
        if (clickFeedback != null) {
            delay(1500)
            clickFeedback = null
        }
    }
    
    // Animation for the gift icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Moving Gold Border Animation
    val borderTransition = rememberInfiniteTransition(label = "border")
    val rotation by borderTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val borderBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFF59E0B), // Amber
            Color(0xFFFCD34D), // Amber-300
            Color(0xFFF59E0B), // Amber
            Color(0xFF78350F), // Amber-900 (Darker for contrast)
            Color(0xFFF59E0B)  // Loop back
        )
    )

    fun handleTrap() {
        if (!settings.isPremiumUnlocked) {
            android.widget.Toast.makeText(context, "Purchases unavailable in Premium. Tap the glowing Gift icon 3 times to unlock.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        // We use a Box to apply the rotating border effect properly
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(1.dp) // Space for border
                .drawBehind {
                    rotate(rotation) {
                        drawRect(borderBrush)
                    }
                }
                .clip(RoundedCornerShape(24.dp)) // Clip the rotating rect to shape
        ) {
             Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth().padding(2.dp) // Inner padding to show border
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable {
                                if (!settings.isPremiumUnlocked) {
                                    betaUnlockClicks++
                                    when (betaUnlockClicks) {
                                        1 -> clickFeedback = "2 more..."
                                        2 -> clickFeedback = "1 more..."
                                        3 -> {
                                            onBetaUnlock()
                                            clickFeedback = null
                                            android.widget.Toast.makeText(context, "Premium Mode Unlocked! Check Settings.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                    ) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFFF59E0B).copy(alpha = if (settings.isPremiumUnlocked) 0f else glowAlpha * 0.3f),
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Icon(
                            Icons.Rounded.CardGiftcard,
                            contentDescription = "Premium Unlock",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(48.dp)
                                .then(if (!settings.isPremiumUnlocked) Modifier.scale(scale) else Modifier)
                        )
                        
                        // Instant Feedback Text
                        androidx.compose.animation.AnimatedVisibility(
                            visible = clickFeedback != null && !settings.isPremiumUnlocked,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = clickFeedback ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Premium Rewards",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Animated Subtitle
                    AnimatedContent(
                        targetState = settings.isPremiumUnlocked,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + slideInVertically { height -> height } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutVertically { height -> -height }
                        },
                        label = "subtitle"
                    ) { isUnlocked ->
                        Text(
                            if (isUnlocked) "All tiers Unlocked (Premium)" else "Click the present for a surprise",
                            color = if (isUnlocked) Color(0xFF4ADE80) else Color(0xFF94A3B8), // Green if unlocked, gray otherwise
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Rewards List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Supporter Badge
                        item {
                            RewardItem(
                                title = "Supporter Badge",
                                subtitle = if (settings.isPremiumUnlocked) "$0.00 (Premium)" else "Show a badge on the main screen",
                                icon = Icons.Rounded.Verified,
                                isUnlocked = settings.hasSupporterBadge || settings.isPremiumUnlocked,
                                action = {
                                    Switch(
                                        checked = settings.showSupporterBadge,
                                        onCheckedChange = { 
                                            if (settings.hasSupporterBadge || settings.isPremiumUnlocked) {
                                                onToggleSupporterBadge(it)
                                            } else {
                                                handleTrap()
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFF59E0B),
                                            checkedTrackColor = Color(0xFFF59E0B).copy(alpha = 0.5f)
                                        ),
                                        enabled = true
                                    )
                                }
                            )
                        }

                        // 2. Visual Pack (Themes)
                        item {
                            RewardItem(
                                title = "Visual Pack",
                                subtitle = if (settings.isPremiumUnlocked) "$0.00 (Premium)" else "Unlock 30+ Themes",
                                icon = Icons.Rounded.Palette,
                                isUnlocked = settings.hasVisualPack || settings.isPremiumUnlocked,
                                action = {
                                    Button(
                                        onClick = {
                                            if (settings.hasVisualPack || settings.isPremiumUnlocked) {
                                                onDismiss()
                                                onThemeSelect() // Open Themes
                                            } else {
                                                handleTrap()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD946EF).copy(alpha = 0.2f),
                                            contentColor = Color(0xFFD946EF)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (settings.isPremiumUnlocked && !settings.hasVisualPack) "Unlock" else "Themes", fontSize = 12.sp)
                                    }
                                }
                            )
                        }

                        // 3. Custom Icons (New)
                        item {
                            RewardItem(
                                title = "Custom Icons",
                                subtitle = if (settings.isPremiumUnlocked) "$0.00 (Premium)" else "Change App Icon",
                                icon = Icons.Rounded.AppShortcut, // Or similar
                                isUnlocked = settings.hasVisualPack || settings.isPremiumUnlocked, // Assume unlocks with Premium
                                action = {
                                    Button(
                                        onClick = {
                                            if (settings.hasVisualPack || settings.isPremiumUnlocked) {
                                                onDismiss()
                                                onCustomizeIcons() // Open Icon Picker
                                            } else {
                                                handleTrap()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                            contentColor = Color(0xFF10B981)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (settings.isPremiumUnlocked && !settings.hasVisualPack) "Unlock" else "Icons", fontSize = 12.sp)
                                    }
                                }
                            )
                        }



                        // 5. Sponsor
                        item {
                            RewardItem(
                                title = "Request a Feature",
                                subtitle = if (settings.isPremiumUnlocked) "$0.00 (Premium)" else "Priority feature requests",
                                icon = Icons.Rounded.Star,
                                isUnlocked = settings.hasSponsorAccess || settings.isPremiumUnlocked,
                                action = {
                                    Button(
                                        onClick = {
                                            if (settings.hasSponsorAccess || settings.isPremiumUnlocked) {
                                                onOpenSponsorForm()
                                            } else {
                                                handleTrap()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6366F1).copy(alpha = 0.2f),
                                            contentColor = Color(0xFF6366F1)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (settings.isPremiumUnlocked && !settings.hasSponsorAccess) "Request" else "Open", fontSize = 12.sp)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isUnlocked) icon else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFFF59E0B) else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isUnlocked) Color.White else Color(0xFF64748B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = if (isUnlocked) Color(0xFF94A3B8) else Color(0xFF475569),
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        action()
    }
}
