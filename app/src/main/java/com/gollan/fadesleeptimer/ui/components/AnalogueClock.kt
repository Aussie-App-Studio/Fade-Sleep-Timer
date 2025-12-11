package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnalogueClock(
    timeLeft: Long,
    totalDuration: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (totalDuration > 0) timeLeft.toFloat() / totalDuration.toFloat() else 0f
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val size = Size(diameter, diameter)

            // Background Circle (Track)
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc (Shrinking Pie)
            // To make it a "shrinking pie chart", we usually fill it. 
            // But the description says "shrinking pie chart", which implies a filled arc.
            // However, the previous implementation likely matched the style of the app (minimalist).
            // Let's stick to a filled arc for "pie chart" or a thick stroke.
            // "Visualise time as a shrinking pie chart" -> Filled arc makes most sense.
            
            drawArc(
                color = color,
                startAngle = -90f, // Start from top
                sweepAngle = 360f * progress,
                useCenter = true // True for pie chart (filled)
            )
        }
    }
}
