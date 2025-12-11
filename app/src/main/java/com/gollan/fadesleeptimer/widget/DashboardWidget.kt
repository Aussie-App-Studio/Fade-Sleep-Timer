package com.gollan.fadesleeptimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.Spacer
import androidx.glance.layout.width
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.gollan.fadesleeptimer.service.TimerService

class DashboardWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val isRunning = prefs[booleanPreferencesKey("is_running")] ?: false
            val timeLeft = prefs[stringPreferencesKey("time_left")] ?: "--:--"
            
            DashboardContent(isRunning, timeLeft)
        }
    }

    @Composable
    private fun DashboardContent(isRunning: Boolean, timeLeft: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1C1B1F)))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRunning) {
                Text(
                    text = timeLeft,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFE6E1E5)),
                        fontSize = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.padding(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // STOP Button
                    val stopIntent = android.content.Intent().setClassName("com.gollan.fadesleeptimer", "com.gollan.fadesleeptimer.service.TimerService")
                        .putExtra("ACTION", "STOP")
                        
                    Button(
                        text = "Stop",
                        onClick = actionStartService(stopIntent),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(Color(0xFFF2B8B5)),
                            contentColor = ColorProvider(Color(0xFF601410))
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(16.dp))
                    // EXTEND Button
                    val extendIntent = android.content.Intent().setClassName("com.gollan.fadesleeptimer", "com.gollan.fadesleeptimer.service.TimerService")
                        .putExtra("ACTION", "EXTEND")
                        
                    Button(
                        text = "+10m",
                        onClick = actionStartService(extendIntent),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(Color(0xFFD0BCFF)),
                            contentColor = ColorProvider(Color(0xFF381E72))
                        )
                    )
                }
            } else {
                Text(
                    text = "Sleep Timer Idle",
                    style = TextStyle(color = ColorProvider(Color(0xFFCAC4D0)))
                )
                Spacer(modifier = GlanceModifier.padding(8.dp))
                val startIntent = android.content.Intent().setClassName("com.gollan.fadesleeptimer", "com.gollan.fadesleeptimer.service.TimerService")
                    .putExtra("DURATION_MINUTES", 30L)
                    .putExtra("PLAY_ON_START", true)

                Button(
                    text = "Start 30m",
                    onClick = actionStartService(startIntent),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(Color(0xFFD0BCFF)),
                        contentColor = ColorProvider(Color(0xFF381E72))
                    )
                )
            }
        }
    }
}

class DashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DashboardWidget()
}
