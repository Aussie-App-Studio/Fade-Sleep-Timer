package com.gollan.fadesleeptimer.ui.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.components.PulsingGlow
import com.gollan.fadesleeptimer.ui.theme.Slate800

@Composable
fun DurationGrid(
    settings: AppSettings,
    onDurationSelected: (Long) -> Unit,
    onGloballyPositioned: (Rect) -> Unit
) {
    val durations = listOf(15L, 30L, 45L, 60L, 90L, 120L)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                onGloballyPositioned(coordinates.boundsInWindow())
            }
    ) {
        items(durations) { minutes ->
            Box(contentAlignment = Alignment.Center) {
                // Encouragement Glow
                if (settings.hasSeenOnboarding && !settings.hasClickedTimer) {
                    PulsingGlow(modifier = Modifier.size(80.dp, 60.dp))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Dynamic Background
                        .clickable { onDurationSelected(minutes) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$minutes",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Dynamic Text
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "MIN",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // Dynamic Subtext
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
