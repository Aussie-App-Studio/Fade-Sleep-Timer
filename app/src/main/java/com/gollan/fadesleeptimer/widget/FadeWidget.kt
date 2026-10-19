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
        
        // Premium Color Palette
        val accentPurple = Color(0xFFA78BFA)     // Soft violet
        val accentBlue = Color(0xFF818CF8)       // Indigo
        val textPrimary = Color(0xFFF1F5F9)      // Almost white
        val textSecondary = Color(0xFF94A3B8)    // Muted gray
        val surfaceLight = Color(0xFF334155)     // Slate for buttons
        val dangerRed = Color(0xFFF87171)        // Soft red
        val dangerBg = Color(0xFF7F1D1D)         // Dark red bg
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.ImageProvider(com.gollan.fadesleeptimer.R.drawable.widget_background_gradient))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSmall) {
                // ═══════════════════════════════════════════
                // COMPACT MODE - Minimal elegant design
                // ═══════════════════════════════════════════
                if (isRunning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        // Time display with glow effect simulation
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = timeLeft,
                                style = TextStyle(
                                    color = ColorProvider(accentPurple),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        // Stop button - Compact pill
                        Button(
                            text = "✕",
                            onClick = androidx.glance.appwidget.action.actionStartService(
                                android.content.Intent(androidx.glance.LocalContext.current, TimerService::class.java)
                                    .setAction(TimerService.ACTION_STOP)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(dangerBg),
                                contentColor = ColorProvider(dangerRed)
                            ),
                            modifier = GlanceModifier.size(36.dp)
                        )
                    }
                } else {
                    // Idle state - Single beautiful button
                    Button(
                        text = "💤 Sleep",
                        onClick = androidx.glance.appwidget.action.actionStartService(
                            android.content.Intent(androidx.glance.LocalContext.current, TimerService::class.java)
                                .setAction(TimerService.ACTION_START)
                        ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(accentPurple),
                            contentColor = ColorProvider(Color(0xFF1e1b4b))
                        ),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            } else {
                // ═══════════════════════════════════════════
                // EXPANDED MODE - Premium dashboard feel
                // ═══════════════════════════════════════════
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    if (isRunning) {
                        // ── Active Timer State ──
                        
                        // Small label
                        Text(
                            text = "SLEEPING IN",
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        
                        // Large time display
                        Text(
                            text = timeLeft,
                            style = TextStyle(
                                color = ColorProvider(textPrimary),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Subtle underline text
                        Text(
                            text = "minutes",
                            style = TextStyle(
                                color = ColorProvider(accentPurple.copy(alpha = 0.7f)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        
                        Spacer(modifier = GlanceModifier.height(16.dp))
                        
                        // Action buttons row
                        Row(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            val context = androidx.glance.LocalContext.current
                            
                            // Extend button - Soft style
                            Button(
                                text = "+15",
                                onClick = androidx.glance.appwidget.action.actionStartService(
                                    android.content.Intent(context, TimerService::class.java)
                                        .setAction(TimerService.ACTION_EXTEND)
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ColorProvider(surfaceLight),
                                    contentColor = ColorProvider(textPrimary)
                                ),
                                modifier = GlanceModifier.defaultWeight().height(44.dp)
                            )
                            
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            
                            // Stop button - Danger style
                            Button(
                                text = "Stop",
                                onClick = androidx.glance.appwidget.action.actionStartService(
                                    android.content.Intent(context, TimerService::class.java)
                                        .setAction(TimerService.ACTION_STOP)
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ColorProvider(dangerBg),
                                    contentColor = ColorProvider(dangerRed)
                                ),
                                modifier = GlanceModifier.defaultWeight().height(44.dp)
                            )
                        }
                    } else {
                        // ── Idle State ──
                        val context = androidx.glance.LocalContext.current
                        
                        // Moon icon (emoji as text)
                        Text(
                            text = "🌙",
                            style = TextStyle(
                                fontSize = 32.sp
                            )
                        )
                        
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        
                        // Greeting text
                        Text(
                            text = "Ready to rest?",
                            style = TextStyle(
                                color = ColorProvider(textPrimary),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        Text(
                            text = "Start your sleep timer",
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 12.sp
                            )
                        )
                        
                        Spacer(modifier = GlanceModifier.height(16.dp))
                        
                        // Start button - Premium gradient simulation
                        Button(
                            text = "Start Timer",
                            onClick = androidx.glance.appwidget.action.actionStartService(
                                android.content.Intent(context, TimerService::class.java)
                                    .setAction(TimerService.ACTION_START)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(accentPurple),
                                contentColor = ColorProvider(Color(0xFF1e1b4b))
                            ),
                            modifier = GlanceModifier.fillMaxWidth().height(48.dp)
                        )
                    }
                }
            }
        }
    }
}
