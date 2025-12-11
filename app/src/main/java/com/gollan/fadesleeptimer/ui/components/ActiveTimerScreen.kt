package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.data.TimerRepository
import com.gollan.fadesleeptimer.ui.AppSettings

@Composable
fun ActiveTimerScreen(
    timeLeft: Long,
    settings: AppSettings,
    isFaceDown: Boolean,
    onStop: () -> Unit,
    onExtend: () -> Unit,
    onSupportClick: () -> Unit // New callback
) {
    val totalDuration by TimerRepository.totalDuration.collectAsState()
    val progress = if (totalDuration > 0) timeLeft.toFloat() / totalDuration.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
         val elapsedTime = if (totalDuration > 0) totalDuration - timeLeft else 0
         
         // 1. Paid Ads (AdMob)
         // 2. Support Banner (Non-Ad version)
         // Logic: Show if free user for > 3 mins
         if (elapsedTime >= 180 && timeLeft > 60) {
             Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                 com.gollan.fadesleeptimer.ui.components.SupportBanner(
                    isVisible = true,
                    onClick = onSupportClick
                 )
             }
         }

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            if (settings.analogueClock) {
                val clockTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                val clockProgressColor = MaterialTheme.colorScheme.primary
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = clockTrackColor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    val sweepAngle = 360f * progress
                    drawArc(
                        color = clockProgressColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
            } else {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                        fontSize = 60.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        "REMAINING",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))

        // Audio Toggle (Only if built-in noise selected)
        if (settings.audioSelection != com.gollan.fadesleeptimer.ui.AudioSelection.SYSTEM) {
             val isAudioPlaying by TimerRepository.isAudioPlaying.collectAsState()
             val context = androidx.compose.ui.platform.LocalContext.current
             
             IconButton(
                onClick = {
                    val intent = android.content.Intent(context, com.gollan.fadesleeptimer.service.TimerService::class.java).apply {
                        action = com.gollan.fadesleeptimer.service.TimerService.ACTION_TOGGLE_AUDIO
                    }
                    context.startService(intent)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    if (isAudioPlaying) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                    contentDescription = "Toggle Audio",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        } else {
             Spacer(modifier = Modifier.height(30.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onExtend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("15m")
            }
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF331520),
                    contentColor = androidx.compose.ui.graphics.Color(0xFFFB7185)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF4C0519)),
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }
}
