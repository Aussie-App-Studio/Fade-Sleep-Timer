package com.gollan.fadesleeptimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gollan.fadesleeptimer.service.TimerService

class FadeWidget : GlanceAppWidget() {
    
    companion object {
        val KEY_IS_RUNNING = booleanPreferencesKey("is_running")
        val KEY_TIME_LEFT = stringPreferencesKey("time_left")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val isRunning = currentState(key = KEY_IS_RUNNING) ?: false
        val timeLeft = currentState(key = KEY_TIME_LEFT) ?: "--:--"
        val size = androidx.glance.LocalSize.current
        val isSmall = size.height < 100.dp || size.width < 150.dp
        
        // Brand Colors
        // val surfaceColor = Color(0xFF1C1B1F) // Unused
        val primaryColor = Color(0xFFD0BCFF)
        val onSurfaceColor = Color(0xFFE6E1E5)
        val surfaceVariant = Color(0xFF49454F) 
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.ImageProvider(com.gollan.fadesleeptimer.R.drawable.widget_background_gradient))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Watermark (Subtle Texture)
            if (!isSmall) {
                androidx.glance.Image(
                    provider = androidx.glance.ImageProvider(com.gollan.fadesleeptimer.R.mipmap.ic_launcher), // Using app icon or valid drawable
                    contentDescription = null,
                    modifier = GlanceModifier
                        .size(80.dp)
                        .padding(bottom = 30.dp),
                     colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.1f)))
                )
            }
            if (isSmall) {
                // COMPACT MODE (Row Layout)
                 if (isRunning) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(
                            text = timeLeft,
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        // Stop Icon Button (Text for now, keep it simple)
                        Button(
                            text = "Stop",
                            onClick = androidx.glance.appwidget.action.actionStartService(
                                android.content.Intent(androidx.glance.LocalContext.current, TimerService::class.java).setAction(TimerService.ACTION_STOP)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(Color(0xFF8B0000)),
                                contentColor = ColorProvider(Color.White)
                            )
                        )
                     }
                 } else {
                     Button(
                        text = "Start",
                        onClick = androidx.glance.appwidget.action.actionStartService(
                            android.content.Intent(androidx.glance.LocalContext.current, TimerService::class.java).setAction(TimerService.ACTION_START)
                        ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(primaryColor),
                            contentColor = ColorProvider(Color(0xFF381E72))
                        )
                    )
                 }
            } else {
                // EXPANDED MODE (Column Layout - Original)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRunning) {
                        Text(
                            text = timeLeft,
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "REMAINING",
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor.copy(alpha = 0.6f)),
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        Row(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val context = androidx.glance.LocalContext.current
                            // +15m Button
                            Button(
                                text = "+15m",
                                onClick = androidx.glance.appwidget.action.actionStartService(
                                    android.content.Intent(context, TimerService::class.java).setAction(TimerService.ACTION_EXTEND)
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ColorProvider(surfaceVariant),
                                    contentColor = ColorProvider(onSurfaceColor)
                                ),
                                modifier = GlanceModifier.height(48.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            // Stop Button
                            Button(
                                text = "Stop",
                                onClick = androidx.glance.appwidget.action.actionStartService(
                                    android.content.Intent(context, TimerService::class.java).setAction(TimerService.ACTION_STOP)
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ColorProvider(Color(0xFF8B0000)), 
                                    contentColor = ColorProvider(Color.White)
                                ),
                                modifier = GlanceModifier.height(48.dp)
                            )
                        }
                    } else {
                        val context = androidx.glance.LocalContext.current
                        Text(
                            text = "Ready to Sleep?",
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        Button(
                            text = "Start Timer",
                            onClick = androidx.glance.appwidget.action.actionStartService(
                                android.content.Intent(context, TimerService::class.java).setAction(TimerService.ACTION_START)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(primaryColor),
                                contentColor = ColorProvider(Color(0xFF381E72)) // OnPrimary
                            ),
                             modifier = GlanceModifier.fillMaxWidth().height(48.dp)
                        )
                    }
                }
            }
        }
    }
}
