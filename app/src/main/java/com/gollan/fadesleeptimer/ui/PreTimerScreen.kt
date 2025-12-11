package com.gollan.fadesleeptimer.ui

import android.content.Intent
import android.provider.Settings
import android.provider.AlarmClock
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.ui.theme.*
import com.gollan.fadesleeptimer.ui.components.PulsingBackground
import com.gollan.fadesleeptimer.viewmodel.TimerViewModel
import com.gollan.fadesleeptimer.viewmodel.ExplanationState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun SleepForecastCard(
    timerDurationMinutes: Int,
    showAlarmButton: Boolean = false,
    onSetAlarm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wakeTime = remember(timerDurationMinutes) {
        val now = LocalDateTime.now()
        val sleepOnset = now.plusMinutes(timerDurationMinutes.toLong())
        val rawWakeTime = sleepOnset.plusHours(8)
        val minute = rawWakeTime.minute
        val roundedMinute = (minute / 15.0).roundToInt() * 15
        val finalWakeTime = rawWakeTime.withMinute(0).withSecond(0).plusMinutes(roundedMinute.toLong())
        val formatter = DateTimeFormatter.ofPattern("h:mm a") 
        finalWakeTime.format(formatter)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.optimal_wake_time_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = wakeTime,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = stringResource(R.string.sleep_cycles_suffix),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                        )
                    }
                }
            }

            if (showAlarmButton) {
                Button(
                    onClick = onSetAlarm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.set_alarm_button), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
// ...


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreTimerScreen(
    settings: AppSettings,
    durationMinutes: Long,
    onStartTimer: () -> Unit,
    onCancel: () -> Unit,
    viewModel: TimerViewModel = viewModel(),
    settingsViewModel: com.gollan.fadesleeptimer.viewmodel.SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val explanationState by viewModel.explanationState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    
    // Show hint toast on first view
    LaunchedEffect(settings.hasSeenPreTimerHint) {
        if (!settings.hasSeenPreTimerHint) {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.pre_timer_settings_hint),
                android.widget.Toast.LENGTH_LONG
            ).show()
            settingsViewModel.updateSettings(settings.copy(hasSeenPreTimerHint = true))
        }
    }
    
    PulsingBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                stringResource(R.string.ready_to_sleep_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                stringResource(R.string.pre_timer_instruction),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (settings.sleepCalculatorEnabled) {
                val durationLong = durationMinutes // Capture for lambda
                SleepForecastCard(
                    timerDurationMinutes = durationMinutes.toInt(),
                    showAlarmButton = settings.sleepCalculatorAlarmOption,
                    onSetAlarm = {
                        val now = LocalDateTime.now()
                        val sleepOnset = now.plusMinutes(durationLong)
                        val wakeTime = sleepOnset.plusHours(8) // Logic: Keep 8h

                        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_HOUR, wakeTime.hour)
                            putExtra(AlarmClock.EXTRA_MINUTES, wakeTime.minute)
                            putExtra(AlarmClock.EXTRA_MESSAGE, context.getString(R.string.fade_wake_up_alarm_message))
                            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        }
                        try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            Toast.makeText(context, context.getString(R.string.alarm_set_toast, wakeTime.format(DateTimeFormatter.ofPattern("h:mm a"))), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.alarm_error_toast, e.message), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (settings.airplaneModeReminder) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Rounded.AirplanemodeActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    stringResource(R.string.airplane_mode_title),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    stringResource(R.string.airplane_mode_desc),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.settings_button_caps), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (settings.scriptureModeEnabled) {
                com.gollan.fadesleeptimer.ui.components.ScriptureCard(
                    topic = settings.scriptureTopic
                )
            }

            if (settings.wisdomQuotesEnabled) {
                com.gollan.fadesleeptimer.ui.components.QuoteCard(
                    settings = settings,
                    onSparkleClick = { quote -> viewModel.explainQuote(quote) }
                )
            }

            if (settings.prayerReminder) {
                var showPrayerCard by remember { mutableStateOf(false) }
                val hasOtherContent = settings.airplaneModeReminder || settings.scriptureModeEnabled
                val delayMillis = if (hasOtherContent) 1500L else 250L

                LaunchedEffect(Unit) {
                    delay(delayMillis)
                    showPrayerCard = true
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = showPrayerCard,
                    enter = androidx.compose.animation.slideInHorizontally { it } + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutHorizontally { it } + androidx.compose.animation.fadeOut()
                ) {
                    com.gollan.fadesleeptimer.ui.components.PrayerCard()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartTimer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.start_timer_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null)
            }
        
        }

    IconButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel_cd), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        
        // Explanation Bottom Sheet
        if (explanationState !is ExplanationState.Idle) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissExplanation() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (explanationState) {
                        is ExplanationState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.illuminating_wisdom_loading),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        is ExplanationState.Success -> {
                            Text(
                                stringResource(R.string.understanding_quote_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                (explanationState as ExplanationState.Success).explanation,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.dismissExplanation() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(R.string.close_button))
                            }
                        }
                        is ExplanationState.Error -> {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.explanation_error_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (explanationState as ExplanationState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.dismissExplanation() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(R.string.close_button))
                            }
                        }
                        ExplanationState.Idle -> {}
                    }
                }
            }
        }
    }
}
}
