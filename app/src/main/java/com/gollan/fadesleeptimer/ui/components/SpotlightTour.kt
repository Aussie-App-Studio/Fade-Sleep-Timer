package com.gollan.fadesleeptimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

enum class SpotlightStep {
    TIMER_PRESETS,
    MEDIA_BUTTONS,
    SETTINGS
}

data class SpotlightTarget(
    val rect: Rect,
    val title: String,
    val description: String
)

@Composable
fun SpotlightTour(
    currentStep: SpotlightStep,
    targets: Map<SpotlightStep, SpotlightTarget>,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val target = targets[currentStep] ?: return
    
    // Animate the spotlight position
    val targetOffset by animateOffsetAsState(
        targetValue = Offset(target.rect.center.x, target.rect.center.y),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "spotlight"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
    ) {
        // Dark overlay (covers everything)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        )
        
        // Circular spotlight glow (makes highlighted area more visible)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightRadius = maxOf(target.rect.width, target.rect.height) * 0.8f
            
            // Draw a radial gradient glow
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = targetOffset,
                    radius = spotlightRadius
                ),
                radius = spotlightRadius,
                center = targetOffset
            )
        }

        // Info card
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = constraints.maxHeight.toFloat()
            val screenWidth = constraints.maxWidth.toFloat()
            
            val density = LocalDensity.current
            
            // Determine if card should be above or below
            // Add padding to avoid covering the spotlight
            val spotlightBottom = target.rect.bottom
            val spotlightTop = target.rect.top
            val cardHeight = with(density) { 200.dp.toPx() } // Approximate card height
            
            val showBelow = spotlightTop > screenHeight / 2 || 
                           (screenHeight - spotlightBottom) > cardHeight + with(density) { 50.dp.toPx() }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = if (showBelow) Arrangement.Bottom else Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B), // Slate 800
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            target.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            target.description,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (currentStep == SpotlightStep.SETTINGS) {
                                    onFinish()
                                } else {
                                    onNext()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (currentStep == SpotlightStep.SETTINGS) stringResource(R.string.spotlight_got_it) else stringResource(R.string.spotlight_next),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
