package com.gollan.fadesleeptimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun PulsingBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    // Infinite transition for the pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Animate the radius of the gradient
    val radiusScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    // Animate the alpha of the gradient
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 1.2f * radiusScale

            val gradient = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = alpha),
                    primaryColor.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius
            )

            drawRect(brush = gradient)
        }
        
        // Content sits on top
        content()
    }
}
