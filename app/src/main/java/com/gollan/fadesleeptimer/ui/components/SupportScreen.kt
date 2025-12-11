package com.gollan.fadesleeptimer.ui.components

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

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
import androidx.compose.runtime.mutableIntStateOf
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
import com.gollan.fadesleeptimer.ui.AppSettings

import kotlinx.coroutines.delay

@Composable
fun SupportScreen(
    settings: AppSettings,
    onClose: () -> Unit,
    onUnlock: (String) -> Unit,
    onJoinHallOfFame: () -> Unit,
    onViewHallOfFame: () -> Unit,
    onBetaUnlock: () -> Unit,
    onThemeSelect: () -> Unit // New callback
) {
    var betaUnlockClicks by remember { mutableIntStateOf(0) }
    var showCelebration by remember { mutableStateOf(false) }

    
    val context = androidx.compose.ui.platform.LocalContext.current

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
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
        if (!settings.isPremiumUnlocked) {
            android.widget.Toast.makeText(context, context.getString(R.string.beta_unlock_toast), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Removed broken shareApp function - now handled by MainScreen via onUnlock



    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF312E81), Color(0xFF0F172A)))
    var clickFeedback by remember { mutableStateOf<String?>(null) }
    
    // Clear feedback after delay
    LaunchedEffect(clickFeedback) {
        if (clickFeedback != null) {
            delay(1500)
            clickFeedback = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
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
                            if (!settings.isPremiumUnlocked) {
                                betaUnlockClicks++
                                when (betaUnlockClicks) {
                                    1 -> clickFeedback = context.getString(R.string.beta_unlock_click_2)
                                    2 -> clickFeedback = context.getString(R.string.beta_unlock_click_1)
                                    3 -> {
                                        onBetaUnlock()
                                        clickFeedback = null // Clear immediate feedback as UI updates
                                        android.widget.Toast.makeText(context, context.getString(R.string.beta_unlock_success_toast), android.widget.Toast.LENGTH_LONG).show()
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
                                Color(0xFFF472B6).copy(alpha = if (settings.isPremiumUnlocked) 0f else glowAlpha * 0.3f),
                                CircleShape
                            )
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

                    Icon(
                        Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFF472B6), // Pink-400
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (!settings.isPremiumUnlocked) Modifier.scale(scale) else Modifier)
                    )
                }

                Column {
                    Text(
                        stringResource(R.string.support_title),
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
                            if (isUnlocked) stringResource(R.string.support_subtitle_unlocked) else stringResource(R.string.support_subtitle_locked),
                            color = if (isUnlocked) Color(0xFF4ADE80) else Color(0xFFF472B6),
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
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.support_close_button), tint = Color(0xFF94A3B8))
            }
        }

        // TIER LIST
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Opt-in Ads Banner

            // Tier 1: Free (Rate)
            item {
                SupportTierItem(
                    title = stringResource(R.string.support_rate_title),
                    subtitle = if (settings.hasReviewBadge) stringResource(R.string.support_rate_subtitle_unlocked) else stringResource(R.string.support_rate_subtitle_locked),
                    icon = Icons.Rounded.Star,
                    themeColor = Color(0xFF10B981), // Emerald
                    priceText = stringResource(R.string.price_free),
                    activeText = stringResource(R.string.support_rate_active_text),
                    activeButtonColor = Color(0xFF10B981), // Emerald-600
                    activeTextColor = Color.White,
                    isActive = settings.hasReviewBadge, // Use the new badge state
                    onClick = { onUnlock("rate") }
                )
            }

            // Tier: Share the App
            item {
                SupportTierItem(
                    title = stringResource(R.string.support_share_title),
                    subtitle = stringResource(R.string.support_share_subtitle),
                    icon = Icons.Rounded.Share,
                    themeColor = Color(0xFF3B82F6), // Blue
                    priceText = stringResource(R.string.price_free),
                    activeText = stringResource(R.string.support_share_active_text),
                    activeButtonColor = Color(0xFF3B82F6), // Blue-600
                    activeTextColor = Color.White,
                    isActive = settings.isShared,
                    onClick = { onUnlock("share") }
                )
            }
            
            // Tier: Other Apps
            item {
                SupportTierItem(
                    title = stringResource(R.string.support_other_apps_title),
                    subtitle = stringResource(R.string.support_other_apps_subtitle),
                    icon = Icons.Rounded.Apps, // Generic app icon
                    themeColor = Color(0xFF8B5CF6), // Violet
                    priceText = stringResource(R.string.price_free),
                    activeText = stringResource(R.string.support_other_apps_active_text),
                    activeButtonColor = Color(0xFF8B5CF6),
                    activeTextColor = Color.White,
                    isActive = settings.hasCheckedOtherApps,
                    onClick = { 
                        onUnlock("other_apps")
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://search?q=pub:Gollan"))
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/search?q=pub:Gollan&c=apps"))
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, context.getString(R.string.support_other_apps_error), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }




            // Tier 3: Burger (Visuals)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    // Using Access Flag for Border (show it looks active if unlocked)
                    border = BorderStroke(1.dp, if (settings.hasVisualPack) Color(0xFFD946EF).copy(alpha = 0.5f) else Color(0xFF1E293B)),
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
                                .background(Color(0xFFD946EF).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color(0xFFD946EF))
                        }

                        // Text with View button
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.support_visuals_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                // View button inline with title
                                if (settings.hasVisualPack) {
                                    IconButton(
                                        onClick = onThemeSelect,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Visibility,
                                            contentDescription = stringResource(R.string.support_visuals_select_cd),
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                stringResource(if (settings.isPremiumUnlocked && !settings.isVisualPackPurchased) R.string.support_visuals_desc_unlocked else R.string.support_visuals_desc),
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        // Purchase Button
                        Button(
                            onClick = {
                                if (settings.isVisualPackPurchased) {
                                    // Already purchased
                                } else if (settings.isPremiumUnlocked) {
                                    onUnlock("visuals") // Allow voluntary purchase even if unlocked
                                } else {
                                    handleTrap()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.isVisualPackPurchased) Color(0xFFD946EF) else Color(0xFF1E293B),
                                contentColor = if (settings.isVisualPackPurchased) Color.White else Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp),
                            enabled = true
                        ) {
                            Text(
                                text = if (settings.isVisualPackPurchased) stringResource(R.string.status_unlocked) else if (settings.isPremiumUnlocked) "$0.99" else "$0.99",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
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
                                Text(stringResource(R.string.support_hof_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                // View button inline with title
                                IconButton(
                                    onClick = onViewHallOfFame,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Visibility,
                                        contentDescription = stringResource(R.string.support_hof_view_cd),
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
                                if (settings.isHallOfFamePurchased) {
                                    // Already purchased
                                } else if (settings.isPremiumUnlocked) {
                                    onJoinHallOfFame() // Allow voluntary purchase
                                } else {
                                    handleTrap()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.isHallOfFamePurchased) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                contentColor = if (settings.isHallOfFamePurchased) Color.Black else Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp),
                            enabled = true
                        ) {
                            Text(
                                text = if (settings.isHallOfFamePurchased) "Immortalized" else if (settings.isPremiumUnlocked) "$2.99" else "$2.99",
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
                    priceText = "$29.99",
                    activeText = "Sponsored",
                    activeButtonColor = Color(0xFF2563EB), // Blue-600
                    activeTextColor = Color.White,
                    isActive = settings.isSponsorPurchased, // Use Purchased Flag
                    onClick = {
                        if (settings.isSponsorPurchased) {
                            // Already sponsored
                        } else if (settings.isPremiumUnlocked) {
                            onUnlock("sponsor")
                        } else {
                            handleTrap()
                        }
                    }
                )
            }

            // Tier 6: Lego
            item {
                SupportTierItem(
                    title = "Buy My Kids Lego",
                    subtitle = "My kids get secondhand lego, and you get everything! (Unlocks All Features)",
                    icon = Icons.Rounded.Face, 
                    themeColor = Color(0xFFEF4444),
                    priceText = "$49.99",
                    activeText = "Lego Purchased!",
                    activeButtonColor = Color(0xFFDC2626), 
                    activeTextColor = Color.White,
                    isActive = settings.isLegoPurchased, 
                    onClick = { 
                        onUnlock("lego")
                        showCelebration = true 
                    }
                )
            }


            }
        }
        
        // Celebration Overlay
        if (showCelebration) {
            CelebrationOverlay(onDismiss = { showCelebration = false })
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

@Composable
fun CelebrationOverlay(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🎉🧱🎉",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "YAY LEGO!",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "I genuinely will shop on Facebook marketplace for Secondhand Lego for my kids now. Thanks a ton!",
                color = Color(0xFFCBD5E1),
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
