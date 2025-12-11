package com.example.fadesleeptimer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadesleeptimer.ui.AppSettings

@Composable
fun SupportScreen(
    settings: AppSettings,
    onClose: () -> Unit,
    onUnlock: (String) -> Unit,
    onJoinHallOfFame: () -> Unit,
    onViewHallOfFame: () -> Unit,
    onBetaUnlock: () -> Unit // New callback
) {
    var betaUnlockClicks by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current

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

    fun handleTrap() {
        if (!settings.isBetaMode) {
            android.widget.Toast.makeText(context, "Purchases unavailable in Beta. Tap the glowing Gift icon 3 times to unlock.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF312E81), Color(0xFF0F172A))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gift Icon with Easter Egg Logic
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            if (!settings.isBetaMode) {
                                betaUnlockClicks++
                                when (betaUnlockClicks) {
                                    1 -> android.widget.Toast.makeText(context, "2 more steps to unlock Beta...", android.widget.Toast.LENGTH_SHORT).show()
                                    2 -> android.widget.Toast.makeText(context, "1 more...", android.widget.Toast.LENGTH_SHORT).show()
                                    3 -> {
                                        onBetaUnlock()
                                        android.widget.Toast.makeText(context, "Beta Mode Unlocked!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFF472B6).copy(alpha = if (settings.isBetaMode) 0f else glowAlpha * 0.3f),
                                CircleShape
                            )
                    )
                    Icon(
                        Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFF472B6), // Pink-400
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (!settings.isBetaMode) Modifier.scale(scale) else Modifier)
                    )
                }

                Column {
                    Text(
                        "Support Development",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Animated Subtitle
                    AnimatedContent(
                        targetState = settings.isBetaMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + slideInVertically { height -> height } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutVertically { height -> -height }
                        },
                        label = "subtitle"
                    ) { isBeta ->
                        Text(
                            if (isBeta) "All tiers temporarily unlocked for Beta" else "Click the present for a surprise",
                            color = if (isBeta) Color(0xFF4ADE80) else Color(0xFFF472B6),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
            }
        }

        // TIER LIST
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tier 1: Free (Rate)
            item {
                SupportTierItem(
                    title = "Rate the App",
                    subtitle = "Unlock the 'Supporter' Badge",
                    icon = Icons.Rounded.Star,
                    themeColor = Color(0xFF10B981), // Emerald
                    priceText = "Free",
                    activeText = "Rated!",
                    activeButtonColor = Color(0xFF10B981), // Emerald-600
                    activeTextColor = Color.White,
                    isActive = settings.isRated || settings.hasSupporterBadge,
                    onClick = { onUnlock("rate") }
                )
            }

            // Tier 2: Coffee (Ads)
            item {
                SupportTierItem(
                    title = "Super Supporter",
                    subtitle = "Turn on unobtrusive banner ads to help fund development",
                    icon = Icons.Rounded.Coffee,
                    themeColor = Color(0xFFF97316), // Orange
                    priceText = "$0.00",
                    activeText = "Active",
                    activeButtonColor = Color(0xFFF97316), // Orange-600
                    activeTextColor = Color.White,
                    isActive = settings.adsEnabled,
                    isToggleable = true,
                    onClick = { onUnlock("ads") }
                )
            }

            // Tier 3: Burger (Visuals)
            item {
                SupportTierItem(
                    title = "Visual Pack",
                    subtitle = if (settings.isBetaMode && !settings.hasVisualPack) "Unlock Neon, OLED Black, and Wild & Crazy Themes ($0.00 Beta)" else "Unlock Neon, OLED Black, and Wild & Crazy Themes",
                    icon = Icons.Rounded.Palette,
                    themeColor = Color(0xFFD946EF), // Fuchsia
                    priceText = if (settings.isBetaMode) "$0.00" else "$5.00",
                    activeText = "Unlocked",
                    activeButtonColor = Color(0xFFD946EF), // Fuchsia-600
                    activeTextColor = Color.White,
                    isActive = settings.hasVisualPack,
                    onClick = {
                        if (settings.isBetaMode) {
                            onUnlock("visuals")
                        } else {
                            handleTrap()
                        }
                    }
                )
            }

            // Tier 4: Pizza (Hall of Fame)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, if (settings.hasHallOfFameAccess) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B))
                        }

                        // Text with View button
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Hall of Fame", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                // View button inline with title
                                IconButton(
                                    onClick = onViewHallOfFame,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Visibility,
                                        contentDescription = "View Hall of Fame",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text("Your name in credits forever + Custom App Icons", color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 16.sp)
                        }

                        // Purchase Button
                        Button(
                            onClick = {
                                if (settings.hasHallOfFameAccess) {
                                    // Already unlocked, maybe show toast or nothing
                                } else if (settings.isBetaMode) {
                                    onJoinHallOfFame()
                                } else {
                                    handleTrap()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.hasHallOfFameAccess) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                contentColor = if (settings.hasHallOfFameAccess) Color.Black else Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp),
                            enabled = true // Always enabled to allow trap
                        ) {
                            Text(
                                text = if (settings.hasHallOfFameAccess) "Immortalized" else if (settings.isBetaMode) "$0.00" else "$10.00",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tier 5: Sponsor
            item {
                SupportTierItem(
                    title = "Sponsor a Feature",
                    subtitle = if (settings.hasSponsorAccess) "I am not an expert coder, but I will give your feature request my best shot" else "Submit a priority request",
                    icon = Icons.Rounded.WorkspacePremium, // Crown replacement
                    themeColor = Color(0xFF6366F1), // Indigo
                    priceText = if (settings.isBetaMode) "$0.00" else "$50.00",
                    activeText = "Sponsored",
                    activeButtonColor = Color(0xFF2563EB), // Blue-600
                    activeTextColor = Color.White,
                    isActive = settings.hasSponsorAccess,
                    onClick = {
                        if (settings.hasSponsorAccess) {
                            // Already sponsored
                        } else if (settings.isBetaMode) {
                            onUnlock("sponsor")
                        } else {
                            handleTrap()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SupportTierItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    themeColor: Color,
    priceText: String,
    activeText: String,
    isActive: Boolean,
    activeButtonColor: Color,
    activeTextColor: Color = Color.White,
    isToggleable: Boolean = false,
    onClick: () -> Unit,
    extraAction: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, if (isActive) themeColor.copy(alpha = 0.5f) else Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(themeColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = themeColor)
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 16.sp)
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                extraAction?.invoke()

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) activeButtonColor else Color(0xFF1E293B),
                        contentColor = if (isActive) activeTextColor else Color(0xFFCBD5E1) // Slate-300
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp),
                    enabled = true // Always enabled to allow trap
                ) {
                    Text(
                        text = if (isActive) activeText else priceText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
