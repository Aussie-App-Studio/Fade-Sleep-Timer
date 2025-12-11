package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.components.ButtonVisibilityDialog

@Composable
fun PersonalizationSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    var showButtonConfig by remember { mutableStateOf(false) }
    var showSmartExtendHelp by remember { mutableStateOf(false) }

    SettingsSection(stringResource(R.string.settings_category_personalization)) {
        
        TimerPresetsEditor(
            presets = settings.timerPresets,
            onPresetsChanged = { onSettingsChanged(settings.copy(timerPresets = it)) }
        )
        
        SettingsToggle(
            title = stringResource(R.string.airplane_mode_reminder_title),
            desc = stringResource(R.string.airplane_mode_reminder_desc),
            icon = Icons.Rounded.AirplanemodeActive,
            checked = settings.airplaneModeReminder,
            onCheckedChange = { onSettingsChanged(settings.copy(airplaneModeReminder = it)) },
            featureId = "airplane_mode",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        SettingsToggle(
            title = stringResource(R.string.adjust_default_timer_title),
            desc = stringResource(R.string.adjust_default_timer_desc),
            icon = Icons.Rounded.AutoAwesome,
            checked = settings.customizeDefaultTimer,
            onCheckedChange = { onSettingsChanged(settings.copy(customizeDefaultTimer = it)) }
        )

        if (settings.customizeDefaultTimer) {
            IndentedSettingsGroup {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    // Helper function for formatting
                    fun formatDuration(minutes: Int): String {
                        return when {
                            minutes < 60 -> "${minutes}m"
                            minutes % 60 == 0 -> "${minutes / 60}h"
                            else -> "${minutes / 60.0}h"
                        }
                    }

                    Text(
                        text = stringResource(R.string.default_duration_label_format, formatDuration(settings.defaultDuration)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    // Snapping Slider Logic
                    val sortedPresets = settings.timerPresets.sorted()
                    val steps = if (sortedPresets.size > 1) sortedPresets.size - 2 else 0
                    val valueRange = 0f..(sortedPresets.size - 1).toFloat()
                    val currentIndex = sortedPresets.indexOf(settings.defaultDuration).takeIf { it >= 0 } ?: 0
                    
                    Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = { index ->
                            val newIndex = kotlin.math.round(index).toInt().coerceIn(0, sortedPresets.size - 1)
                            onSettingsChanged(settings.copy(defaultDuration = sortedPresets[newIndex]))
                        },
                        valueRange = valueRange,
                        steps = if (steps >= 0) steps else 0,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            activeTickColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        sortedPresets.forEach { preset ->
                            Text(
                                text = formatDuration(preset),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                SettingsToggle(
                    title = stringResource(R.string.timer_check_in_title),
                    desc = stringResource(R.string.timer_check_in_desc),
                    icon = null,
                    checked = settings.morningCheckEnabled,
                    onCheckedChange = { onSettingsChanged(settings.copy(morningCheckEnabled = it)) }
                )
            }
        }
        
        // Smart Extend Control
        SettingsToggle(
            title = stringResource(R.string.smart_extend_title),
            desc = stringResource(R.string.smart_extend_desc),
            icon = Icons.Rounded.AutoAwesome,
            checked = settings.smartExtendEnabled,
            onCheckedChange = { 
                if (!settings.smartExtendEnabled) showSmartExtendHelp = true
                onSettingsChanged(settings.copy(smartExtendEnabled = it)) 
            }
        )

        if (settings.smartExtendEnabled) {
            IndentedSettingsGroup {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.sensitivity_header),
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val sensitivityOptions = com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.values().toList()
                    val currentSensitivity = settings.smartExtendSensitivity
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (option in sensitivityOptions) {
                            val isSelected = option == currentSensitivity
                            val label = when(option) {
                                com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.LOW -> stringResource(R.string.sensitivity_low)
                                com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.MEDIUM -> stringResource(R.string.sensitivity_med)
                                com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.HIGH -> stringResource(R.string.sensitivity_high)
                            }
                            
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clickable { onSettingsChanged(settings.copy(smartExtendSensitivity = option)) }
                                    .weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Text(
                        text = when(settings.smartExtendSensitivity) {
                            com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.LOW -> stringResource(R.string.sensitivity_low_desc)
                            com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.MEDIUM -> stringResource(R.string.sensitivity_med_desc)
                            com.gollan.fadesleeptimer.ui.SmartExtendSensitivity.HIGH -> stringResource(R.string.sensitivity_high_desc)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Configure Media Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showButtonConfig = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Rounded.Apps, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.configure_buttons_title), 
                        color = MaterialTheme.colorScheme.onBackground, 
                        fontSize = 16.sp
                    )
                    Text(
                        stringResource(R.string.configure_buttons_desc), 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
    
    // Help Dialog (Outside Section, but inside function)
    if (showSmartExtendHelp) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSmartExtendHelp = false },
            icon = { Icon(Icons.Rounded.AutoAwesome, null) },
            title = { Text(stringResource(R.string.smart_extend_help_title)) },
            text = {
                Text(stringResource(R.string.smart_extend_help_text))
            },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { showSmartExtendHelp = false }) {
                    Text(stringResource(R.string.got_it_button))
                }
            }
        )
    }

    // Button visibility dialog
    if (showButtonConfig) {
        ButtonVisibilityDialog(
            settings = settings,
            onDismiss = { showButtonConfig = false },
            onUpdate = { updatedSettings ->
                onSettingsChanged(updatedSettings)
                showButtonConfig = false
            }
        )
    }
}
