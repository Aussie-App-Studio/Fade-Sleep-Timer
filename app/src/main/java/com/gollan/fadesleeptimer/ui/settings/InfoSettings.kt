package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import com.gollan.fadesleeptimer.ui.AppSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoSettings(
    settings: AppSettings,
    sleepHistory: List<com.gollan.fadesleeptimer.ui.components.ChartData>,
    onSettingsChanged: (AppSettings) -> Unit,
    onSleepJourneyClick: () -> Unit,
    onHallOfFameClick: () -> Unit

) {
    SettingsSection(stringResource(R.string.settings_category_info), initiallyExpanded = true) {

        
        StatCard(savedMinutes = settings.savedMinutes)



        // Sleep Journey Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSleepJourneyClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Assessment, contentDescription = null, tint = Color(0xFF8B5CF6)) // Violet-500
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.sleep_journey_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Text(stringResource(R.string.sleep_journey_desc), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }

        // Hall of Fame Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHallOfFameClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24)) // Amber-400
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.hall_of_fame_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Text(stringResource(R.string.hall_of_fame_desc), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StatCard(savedMinutes: Int) {
    // Format Logic
    val displayValue = if (savedMinutes < 60) {
        stringResource(R.string.saved_minutes_format, savedMinutes)
    } else {
        stringResource(R.string.saved_hours_format, savedMinutes / 60f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .drawBehind { 
                // Top Border
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 16.dp), // Inner padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)), // Slate-800
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.BatteryChargingFull,
                contentDescription = null,
                tint = Color(0xFF34D399) // Emerald-400
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text Stack
        Column {
            Text(
                text = stringResource(R.string.battery_data_saved_title),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.battery_data_saved_desc),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
