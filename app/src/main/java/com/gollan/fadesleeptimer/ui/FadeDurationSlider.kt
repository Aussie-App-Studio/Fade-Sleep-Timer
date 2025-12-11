package com.gollan.fadesleeptimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun FadeDurationSlider(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp) // Increased touch target height
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val maxWidthPx = size.width
                    val thumbSizePx = 32.dp.toPx()
                    val trackStart = thumbSizePx / 2
                    val trackEnd = maxWidthPx - (thumbSizePx / 2)
                    val trackWidth = trackEnd - trackStart
                    
                    val fraction = ((offset.x - trackStart) / trackWidth).coerceIn(0f, 1f)
                    val newValue = (fraction * 9).roundToInt() + 1
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val maxWidthPx = size.width
                    val thumbSizePx = 32.dp.toPx()
                    val trackStart = thumbSizePx / 2
                    val trackEnd = maxWidthPx - (thumbSizePx / 2)
                    val trackWidth = trackEnd - trackStart
                    
                    val fraction = ((change.position.x - trackStart) / trackWidth).coerceIn(0f, 1f)
                    val newValue = (fraction * 9).roundToInt() + 1
                    onValueChange(newValue)
                }
            }
    ) {
        val maxWidth = maxWidth
        val thumbSize = 32.dp
        val trackStart = thumbSize / 2
        val trackEnd = maxWidth - (thumbSize / 2)
        val trackWidth = trackEnd - trackStart
        
        val fraction = (value - 1f) / 9f
        val thumbOffset = trackStart + (trackWidth * fraction) - (thumbSize / 2)

        // 1. Track and Dots (Background)
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(thumbSize)
            .align(Alignment.Center)
        ) {
            val centerY = size.height / 2
            val startPx = trackStart.toPx()
            val endPx = trackEnd.toPx()
            val widthPx = endPx - startPx
            
            // Inactive Line
            drawLine(
                color = Color(0xFF334155),
                start = androidx.compose.ui.geometry.Offset(startPx, centerY),
                end = androidx.compose.ui.geometry.Offset(endPx, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Active Line
            drawLine(
                color = Color(0xFF6366F1),
                start = androidx.compose.ui.geometry.Offset(startPx, centerY),
                end = androidx.compose.ui.geometry.Offset(startPx + (widthPx * fraction), centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Dots
            for (i in 0..9) {
                val dotFraction = i / 9f
                val dotX = startPx + (widthPx * dotFraction)
                val isActive = i + 1 <= value
                
                drawCircle(
                    color = if (isActive) Color(0xFF6366F1) else Color(0xFF334155),
                    radius = 3.dp.toPx(), // 6dp diameter
                    center = androidx.compose.ui.geometry.Offset(dotX, centerY)
                )
            }
        }

        // 2. Octagon Thumb (Overlay)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .align(Alignment.CenterStart)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val w = size.width
                val h = size.height
                val cut = w * 0.29f // Octagon corner cut
                
                path.moveTo(cut, 0f)
                path.lineTo(w - cut, 0f)
                path.lineTo(w, cut)
                path.lineTo(w, h - cut)
                path.lineTo(w - cut, h)
                path.lineTo(cut, h)
                path.lineTo(0f, h - cut)
                path.lineTo(0f, cut)
                path.close()
                
                drawPath(path, color = Color(0xFF6366F1))
            }
            
            Text(
                text = "$value",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
