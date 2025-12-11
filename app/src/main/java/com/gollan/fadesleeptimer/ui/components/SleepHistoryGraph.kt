package com.gollan.fadesleeptimer.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ChartData(
    val dayLabel: String, // "Mon", "Tue"
    val fullDate: String, // "Mon, Nov 12"
    val startOffsetHours: Float, // Hours from 9:00 PM (e.g. 0 = 9PM, 4 = 1AM)
    val durationHours: Float, // Duration in hours
    val endTimeLabel: String, // "11:30 PM"
    val isEmpty: Boolean = false
)

@Composable
fun SleepHistoryGraph(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        // Graph Title / Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bedtime Consistency",
                color = onSurfaceColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "8 PM - 8 AM",
                color = onSurfaceColor.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(surfaceColor, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Calculate clicked column
                        val columnWidth = size.width / 7
                        val index = (offset.x / columnWidth).toInt().coerceIn(0, 6)
                        if (index < data.size && !data[index].isEmpty) {
                            selectedIndex = if (selectedIndex == index) null else index
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)) {
                val width = size.width
                val height = size.height
                val columnWidth = width / 7
                
                // Y-Axis Range: 8 PM (20:00) to 8 AM (08:00 next day) = 12 Hours
                val totalRangeHours = 12f
                
                // Draw Grid Lines (Every 4 hours: 8PM (0), 12AM (4), 4AM (8), 8AM (12))
                val gridLines = listOf(0f, 4f, 8f, 12f) // Offsets from 8 PM
                gridLines.forEach { offset ->
                    val y = (offset / totalRangeHours) * height
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bars
                data.forEachIndexed { index, item ->
                    if (!item.isEmpty) {
                        val x = index * columnWidth + (columnWidth / 2) - (12.dp.toPx() / 2)
                        
                        // Calculate Y position
                        // startOffsetHours is from 9 PM. 
                        // If startOffsetHours < 0 (before 9 PM), clamp or show?
                        // Let's assume data is sanitized to be within or close to window.
                        
                        val barTop = (item.startOffsetHours / totalRangeHours) * height
                        val barHeight = (item.durationHours / totalRangeHours) * height
                        
                        // Draw Bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (selectedIndex == index) 
                                    listOf(Color(0xFF818CF8), Color(0xFF6366F1)) // Highlight
                                else 
                                    listOf(Color(0xFF6366F1).copy(alpha = 0.7f), Color(0xFF4F46E5).copy(alpha = 0.7f)),
                                startY = barTop,
                                endY = barTop + barHeight
                            ),
                            topLeft = Offset(x, barTop),
                            size = Size(12.dp.toPx(), barHeight.coerceAtLeast(4.dp.toPx())), // Min height for visibility
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                    
                    // Draw Day Label
                    drawContext.canvas.nativeCanvas.drawText(
                        item.dayLabel,
                        index * columnWidth + (columnWidth / 2),
                        height + 24.dp.toPx(),
                        Paint().apply {
                            color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }
            
            // Detail Bubble (Overlay)
            if (selectedIndex != null && selectedIndex!! < data.size) {
                val item = data[selectedIndex!!]
                if (!item.isEmpty) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.fullDate, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Finished at ${item.endTimeLabel}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
