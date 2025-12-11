package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.SettingsSection

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoNotDisturb

import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gollan.fadesleeptimer.ui.OpenOnChargeMode
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SystemSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    // Battery Optimization Check
    val context = LocalContext.current
    var isOptimized by remember { mutableStateOf(false) }

    // Refresh check on Resume
    androidx.compose.runtime.DisposableEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isOptimized = !com.gollan.fadesleeptimer.util.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    SettingsSection(
        title = stringResource(R.string.settings_category_system),
        breadcrumbSuggestion = breadcrumbSuggestion,
        hasWarning = isOptimized,
        isCollapsible = true
    ) {
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        var showOverlayPermissionDialog by remember { mutableStateOf(false) }
        var showChargeDialog by remember { mutableStateOf(false) }

        if (isOptimized) {
            BatteryWarningCard(
                onClickFix = {
                    com.gollan.fadesleeptimer.util.BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                }
            )
        }
        
        SettingsToggle(
            title = stringResource(R.string.dnd_title),
            desc = stringResource(R.string.dnd_desc),
            icon = Icons.Rounded.DoNotDisturb,
            checked = settings.dndEnabled,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        onSettingsChanged(settings.copy(dndEnabled = true))
                    } else {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    }
                } else {
                    onSettingsChanged(settings.copy(dndEnabled = false))
                }
            },
            featureId = "dnd",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        
        if (settings.dndEnabled) {
            IndentedSettingsGroup {
                val priorityLabel = stringResource(R.string.dnd_mode_priority)
                val alarmsLabel = stringResource(R.string.dnd_mode_alarms)
                val priorityPrefix = priorityLabel.substringBefore(" ")

                TopicSelector(
                    options = listOf(priorityLabel, alarmsLabel),
                    selectedOption = if (settings.dndMode == com.gollan.fadesleeptimer.ui.DndMode.PRIORITY) priorityLabel else alarmsLabel,
                    onOptionSelected = { selected ->
                        val mode = if (selected.startsWith(priorityPrefix)) com.gollan.fadesleeptimer.ui.DndMode.PRIORITY else com.gollan.fadesleeptimer.ui.DndMode.ALARMS_ONLY
                        onSettingsChanged(settings.copy(dndMode = mode))
                    }
                )
            }
        }
        


        SettingsToggle(
            title = stringResource(R.string.battery_guard_title),
            desc = stringResource(R.string.battery_guard_desc_format, settings.batteryGuardThreshold),
            icon = Icons.Rounded.BatteryAlert,
            checked = settings.batteryGuard,
            onCheckedChange = { onSettingsChanged(settings.copy(batteryGuard = it)) },
            featureId = "battery_guard",
            breadcrumbSuggestion = breadcrumbSuggestion,
            iconTint = androidx.compose.ui.graphics.Color(0xFFEF4444)
        )
        
        if (settings.batteryGuard) {
            IndentedSettingsGroup {
                Column(modifier = Modifier.padding(end = 16.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         Text(stringResource(R.string.stop_threshold_label), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                         Text("${settings.batteryGuardThreshold}%", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                    Slider(
                        value = settings.batteryGuardThreshold.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(batteryGuardThreshold = it.toInt())) },
                        valueRange = 5f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        SettingsToggle(
            title = stringResource(R.string.go_home_title),
            desc = stringResource(R.string.go_home_desc),
            icon = Icons.Rounded.Home,
            checked = settings.goHomeOnStart,
            onCheckedChange = { onSettingsChanged(settings.copy(goHomeOnStart = it)) },
            featureId = "auto_minimize",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        SettingsToggle(
            title = stringResource(R.string.open_on_charge_title),
            desc = stringResource(R.string.open_on_charge_desc),
            icon = Icons.Rounded.Bolt,
            checked = settings.openOnChargeEnabled,
            onCheckedChange = { 
                if (it) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                        showOverlayPermissionDialog = true
                    } else {
                        showChargeDialog = true
                    }
                } else {
                    onSettingsChanged(settings.copy(openOnChargeEnabled = false))
                }
            },
            featureId = "open_on_charge",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        if (showChargeDialog) {
            OpenOnChargeDialog(
                currentMode = settings.openOnChargeMode,
                onModeSelected = { mode ->
                    onSettingsChanged(settings.copy(openOnChargeEnabled = true, openOnChargeMode = mode))
                    showChargeDialog = false
                },
                onDismiss = { showChargeDialog = false }
            )
        }

        if (showOverlayPermissionDialog) {
            OpenOnChargePermissionDialog(
                onDismiss = { showOverlayPermissionDialog = false },
                onConfirm = {
                    try {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showOverlayPermissionDialog = false
                }
            )
        }


    }
}


@Composable
fun OpenOnChargePermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text("To automatically open on charge, Fade Sleep Timer needs permission to display over other apps.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                Text("1. Tap 'Open Settings' below.", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text("2. Find 'Fade Sleep Timer' in the list.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text("3. Turn on 'Allow display over other apps'.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}
