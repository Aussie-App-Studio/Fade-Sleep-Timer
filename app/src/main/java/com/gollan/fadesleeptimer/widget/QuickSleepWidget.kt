package com.gollan.fadesleeptimer.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.gollan.fadesleeptimer.service.TimerService

class QuickSleepWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }

    @Composable
    private fun MyContent() {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1C1B1F)))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuickButton("15m", 15)
            Spacer(modifier = GlanceModifier.width(8.dp))
            QuickButton("30m", 30)
            Spacer(modifier = GlanceModifier.width(8.dp))
            QuickButton("60m", 60)
        }
    }

    @Composable
    private fun QuickButton(label: String, minutes: Long) {
        val intent = Intent().setClassName("com.gollan.fadesleeptimer", "com.gollan.fadesleeptimer.service.TimerService")
            .putExtra("DURATION_MINUTES", minutes)
            .putExtra("PLAY_ON_START", true)
            
        Button(
            text = label,
            onClick = actionStartService(intent),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorProvider(Color(0xFFD0BCFF)),
                contentColor = ColorProvider(Color(0xFF381E72))
            )
        )
    }
}

class QuickSleepWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickSleepWidget()
}
