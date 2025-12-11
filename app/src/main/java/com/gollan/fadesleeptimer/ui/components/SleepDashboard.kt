package com.gollan.fadesleeptimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.data.Badge
import com.gollan.fadesleeptimer.data.BadgeRepository
import com.gollan.fadesleeptimer.ui.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDashboard(
    settings: AppSettings,
    sleepHistory: List<ChartData>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep Journey") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Streak Flame
            StreakFlame(streakCount = settings.sleepStreakCount)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 2. Consistency / Stats (Reuse existing if needed, or simple text for now)
            // Existing BedtimeConsistencyScreen logic could be merged here, 
            // but for now let's focus on the gamification part.
            // We can show a mini-summary.
            
            Text(
                "Your Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Badges Grid
            BadgeGrid(unlockedIds = settings.unlockedBadges)
            
            Spacer(modifier = Modifier.height(24.dp))
            
             // 4. Consistency Chart (Simplified or referenced)
             // If we have history, show it
             if (sleepHistory.isNotEmpty()) {
                 Text(
                    "Consistency",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                 Spacer(modifier = Modifier.height(8.dp))
                 // Reuse the graph from BedtimeConsistencyScreen? 
                 // It's not exposed as a verified component yet, let's just make a placeholder or copy simple logic if needed.
                 // For now, let's keep it clean.
                 // BedtimeConsistencyChart(data = sleepHistory)
             }
        }
    }
}

@Composable
fun StreakFlame(streakCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val _scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Outer Glow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF5722).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Flame Icon
            Icon(
                Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFF5722), // Orange/Fire
                modifier = Modifier
                    .size(80.dp) // * scale logic if we want
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "$streakCount Day Streak",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Hit your bedtime target to keep it 🔥",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun BadgeGrid(unlockedIds: Set<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(BadgeRepository.badges) { badge ->
            BadgeItem(badge = badge, isUnlocked = unlockedIds.contains(badge.id))
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, isUnlocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) badge.color.copy(alpha = 0.2f) 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                     if (!isUnlocked) Modifier.alpha(0.5f) else Modifier
                )
        ) {
            Icon(
                if (isUnlocked) badge.icon else Icons.Rounded.Lock,
                contentDescription = badge.name,
                tint = if (isUnlocked) badge.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            badge.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            "${badge.requiredStreak} Days",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
