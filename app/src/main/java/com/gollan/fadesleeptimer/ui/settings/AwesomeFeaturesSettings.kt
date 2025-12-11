package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.service.ScreenLockService
import com.gollan.fadesleeptimer.ui.components.AppPickerDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Add

@Composable
fun AwesomeFeaturesSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,

    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val context = LocalContext.current
    var showAppPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showAppPicker) {
        com.gollan.fadesleeptimer.ui.components.AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { packageName ->
                onSettingsChanged(settings.copy(doomscrollBlockedApps = settings.doomscrollBlockedApps + packageName))
            }
        )
    }

    var showPermissionDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showPermissionDialog) {
        AccessibilityInstructionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                com.gollan.fadesleeptimer.service.ScreenLockService.openAccessibilitySettings(context)
            }
        )
    }

    SettingsSection(stringResource(R.string.settings_category_awesome), breadcrumbSuggestion = breadcrumbSuggestion) {


        // Bedtime Reminder
        SettingsToggle(
            title = stringResource(R.string.bedtime_reminder_title),
            desc = stringResource(R.string.bedtime_reminder_desc),
            icon = Icons.Rounded.Notifications,
            checked = settings.bedtimeReminderEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(bedtimeReminderEnabled = it)) },
            featureId = "bedtime_reminder",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (settings.bedtimeReminderEnabled) {
            IndentedSettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    onSettingsChanged(settings.copy(bedtimeHour = hour, bedtimeMinute = minute))
                                },
                                settings.bedtimeHour,
                                settings.bedtimeMinute,
                                false // 12-hour format preferred for sleep apps usually
                            ).show()
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.reminder_time_label),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    
                    // Format time (e.g., 10:00 PM)
                    val amPm = if (settings.bedtimeHour < 12) "AM" else "PM"
                    val hour12 = if (settings.bedtimeHour % 12 == 0) 12 else settings.bedtimeHour % 12
                    val minuteStr = settings.bedtimeMinute.toString().padStart(2, '0')
                    
                    Text(
                        text = "$hour12:$minuteStr $amPm",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }

        SettingsToggle(
            title = stringResource(R.string.prayer_reminder_title),
            desc = stringResource(R.string.prayer_reminder_desc),
            icon = Icons.Rounded.Favorite,
            checked = settings.prayerReminder,
            onCheckedChange = { onSettingsChanged(settings.copy(prayerReminder = it)) },
            featureId = "prayer",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        
        SettingsToggle(
            title = stringResource(R.string.brain_dump_title),
            desc = stringResource(R.string.brain_dump_desc),
            icon = Icons.Rounded.Edit,
            checked = settings.brainDumpEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(brainDumpEnabled = it)) },
            featureId = "brain_dump",
            breadcrumbSuggestion = breadcrumbSuggestion
        )


        SettingsToggle(
            title = stringResource(R.string.scripture_mode_title),
            desc = stringResource(R.string.scripture_mode_desc),
            icon = Icons.Rounded.AutoStories,
            checked = settings.scriptureModeEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(scriptureModeEnabled = it)) },
            featureId = "scripture",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (settings.scriptureModeEnabled) {
            TopicSelector(
                options = listOf(stringResource(R.string.scripture_topic_random)) + com.gollan.fadesleeptimer.data.ScriptureRepository.categories.keys.toList(),
                selectedOption = settings.scriptureTopic,
                onOptionSelected = { onSettingsChanged(settings.copy(scriptureTopic = it)) }
            )
        }

        SettingsToggle(
            title = stringResource(R.string.wisdom_quotes_title),
            desc = stringResource(R.string.wisdom_quotes_desc),
            icon = Icons.Rounded.AutoAwesome,
            checked = settings.wisdomQuotesEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(wisdomQuotesEnabled = it)) },
            featureId = "wisdom_quotes",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (settings.wisdomQuotesEnabled) {
            IndentedSettingsGroup {
                SettingsToggle(
                    title = stringResource(R.string.wisdom_category_peace),
                    desc = stringResource(R.string.wisdom_desc_peace),
                    icon = null,
                    checked = settings.wisdomQuotesPeaceComfort,
                    onCheckedChange = { onSettingsChanged(settings.copy(wisdomQuotesPeaceComfort = it)) }
                )
                SettingsToggle(
                    title = stringResource(R.string.wisdom_category_glory),
                    desc = stringResource(R.string.wisdom_desc_glory),
                    icon = null,
                    checked = settings.wisdomQuotesGodGlory,
                    onCheckedChange = { onSettingsChanged(settings.copy(wisdomQuotesGodGlory = it)) }
                )
                SettingsToggle(
                    title = stringResource(R.string.wisdom_category_gospel),
                    desc = stringResource(R.string.wisdom_desc_gospel),
                    icon = null,
                    checked = settings.wisdomQuotesGospelGrace,
                    onCheckedChange = { onSettingsChanged(settings.copy(wisdomQuotesGospelGrace = it)) }
                )
                SettingsToggle(
                    title = stringResource(R.string.wisdom_category_living),
                    desc = stringResource(R.string.wisdom_desc_living),
                    icon = null,
                    checked = settings.wisdomQuotesWisdomLiving,
                    onCheckedChange = { onSettingsChanged(settings.copy(wisdomQuotesWisdomLiving = it)) }
                )
            }
        }


        SettingsToggle(
            title = stringResource(R.string.sleep_calc_title),
            desc = stringResource(R.string.sleep_calc_desc),
            icon = Icons.Rounded.AutoAwesome, // Or Alarm/Schedule if available, using AutoAwesome for now
            checked = settings.sleepCalculatorEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(sleepCalculatorEnabled = it)) },
            featureId = "sleep_calc",
            breadcrumbSuggestion = breadcrumbSuggestion
        )




        if (settings.sleepCalculatorEnabled) {
            IndentedSettingsGroup {
                SettingsToggle(
                    title = stringResource(R.string.alarm_button_setting_title),
                    desc = stringResource(R.string.alarm_button_setting_desc), // Escaped in XML, here normal
                    icon = null,
                    checked = settings.sleepCalculatorAlarmOption,
                    onCheckedChange = { onSettingsChanged(settings.copy(sleepCalculatorAlarmOption = it)) }
                )
            }
        }

        SettingsToggle(
            title = stringResource(R.string.breathing_guide_title),
            desc = stringResource(R.string.breathing_guide_desc),
            icon = Icons.Rounded.Air,
            checked = settings.breathingGuideEnabled,
            onCheckedChange = { onSettingsChanged(settings.copy(breathingGuideEnabled = it)) },
            featureId = "breathing",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (settings.breathingGuideEnabled) {
            IndentedSettingsGroup {
                SettingsToggle(
                    title = stringResource(R.string.haptic_vibration_title),
                    desc = stringResource(R.string.haptic_vibration_desc),
                    icon = null,
                    checked = settings.breathingVibrationEnabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(breathingVibrationEnabled = it)) }
                )
                
                SettingsToggle(
                    title = stringResource(R.string.breathing_auto_start_title),
                    desc = stringResource(R.string.breathing_auto_start_desc),
                    icon = null,
                    checked = settings.breathingAutoStart,
                    onCheckedChange = { onSettingsChanged(settings.copy(breathingAutoStart = it)) }
                )
                
                TopicSelector(
                    options = com.gollan.fadesleeptimer.data.BreathingRepository.patterns.map { it.name },
                    selectedOption = com.gollan.fadesleeptimer.data.BreathingRepository.getPattern(settings.breathingPatternId).name,
                    onOptionSelected = { name ->
                        val id = com.gollan.fadesleeptimer.data.BreathingRepository.patterns.find { it.name == name }?.id ?: "deep"
                        onSettingsChanged(settings.copy(breathingPatternId = id))
                    }
                )
                
                // Duration Slider (1-10 mins)
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.breathing_duration_label, settings.breathingDurationMinutes),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Slider(
                        value = settings.breathingDurationMinutes.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(breathingDurationMinutes = it.toInt())) },
                        valueRange = 1f..10f,
                        steps = 8, // 1, 2, ... 10
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
        // Anti-Doomscroll Lock
        SettingsToggle(
            title = stringResource(R.string.doomscroll_title),
            desc = stringResource(R.string.doomscroll_desc),
            icon = Icons.Rounded.Lock,
            checked = settings.doomscrollLockEnabled,
            onCheckedChange = { isEnabled ->
                // Check permission first logic?
                if (isEnabled && !com.gollan.fadesleeptimer.service.ScreenLockService.isEnabled(context)) {
                    showPermissionDialog = true
                } else {
                    onSettingsChanged(settings.copy(doomscrollLockEnabled = isEnabled))
                }
            },
            featureId = "doomscroll",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (settings.doomscrollLockEnabled) {
            IndentedSettingsGroup {
                // Grace Period Slider
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    Text(
                        text = stringResource(R.string.grace_period_label, settings.doomscrollGraceMinutes),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.grace_period_desc),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = settings.doomscrollGraceMinutes.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(doomscrollGraceMinutes = it.toInt())) },
                        valueRange = 0f..30f,
                        steps = 29, 
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Blocked Apps List
                Text(
                    text = stringResource(R.string.blocked_apps_label, settings.doomscrollBlockedApps.size),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                settings.doomscrollBlockedApps.forEach { packageName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = packageName, // Ideally resolve label, but package is fine for now
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        androidx.compose.material3.IconButton(
                            onClick = {
                                onSettingsChanged(settings.copy(doomscrollBlockedApps = settings.doomscrollBlockedApps - packageName))
                            }
                        ) {
                            Icon(Icons.Rounded.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                androidx.compose.material3.Button(
                    onClick = { showAppPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_app_to_block_button))
                }
            }
        }
    }
}

@Composable
fun AccessibilityInstructionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_required_title), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text(
                    stringResource(R.string.accessibility_explanation_doomscroll),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(R.string.instructions_label), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(stringResource(R.string.accessibility_step_1), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.accessibility_step_2_doomscroll), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.accessibility_step_3_doomscroll), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.accessibility_step_4_doomscroll), color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.go_to_settings_button))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}
