package com.gollan.fadesleeptimer.ui

import android.Manifest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R
import com.gollan.fadesleeptimer.service.ScreenLockService

@Composable
fun PermissionsScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    var hasAccessibilityPermission by remember {
        mutableStateOf(ScreenLockService.isEnabled(context))
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    
    // Refresh permissions when app resumes (user might have changed them in settings)
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
                hasAccessibilityPermission = ScreenLockService.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Launcher for Notification Permission
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    // Check if all needed are granted
    // Check if all needed are granted
    // Removed auto-advance to allow user to see/grant optional permissions
    // The "Continue" button below handles the progression.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Slate950
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = null,
            tint = Color(0xFF818CF8), // Indigo400
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.permissions_needed_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permissions_needed_desc),
            color = Color(0xFF94A3B8), // Slate400
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Promise
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)), // Emerald border
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = stringResource(R.string.privacy_promise_cd),
                    tint = Color(0xFF10B981), // Emerald-500
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.privacy_promise_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.privacy_promise_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Source Promise
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f)), // Indigo border
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = stringResource(R.string.open_source_cd),
                        tint = Color(0xFF6366F1), // Indigo-500
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.open_source_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.open_source_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF6366F1)
                    ),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.view_source_code_button), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notification Permission Card
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = stringResource(R.string.notifications_permission_title),
                desc = if (hasNotificationPermission) stringResource(R.string.notifications_enabled_desc) else stringResource(R.string.notifications_required_desc),
                icon = Icons.Rounded.Notifications,
                buttonText = if (hasNotificationPermission) stringResource(R.string.allowed_button) else stringResource(R.string.allow_button),
                onClick = { 
                    if (!hasNotificationPermission) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) 
                    } else {
                        // Optional: Open settings if they want to revoke?
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                },
                isGranted = hasNotificationPermission
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Battery Optimization Warning (Critical for Timer)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        var isIgnoringBatteryOptimizations by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
                } else {
                    true
                }
            )
        }

        // Refresh battery state on resume
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionCard(
                title = stringResource(R.string.unrestricted_battery_title),
                desc = if (isIgnoringBatteryOptimizations) stringResource(R.string.battery_enabled_desc) else stringResource(R.string.battery_critical_desc),
                icon = Icons.Rounded.BatteryAlert,
                buttonText = if (isIgnoringBatteryOptimizations) stringResource(R.string.enabled_button) else stringResource(R.string.fix_button),
                onClick = {
                    if (!isIgnoringBatteryOptimizations) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                // Fallback to app details
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    } else {
                        // Already granted, open settings to allow revocation
                        try {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                },
                isGranted = isIgnoringBatteryOptimizations
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Accessibility Permission Card (Optional)
        var showAccessibilityDialog by remember { mutableStateOf(false) }
        
        if (showAccessibilityDialog) {
            AlertDialog(
                onDismissRequest = { showAccessibilityDialog = false },
                title = { Text(stringResource(R.string.accessibility_dialog_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.accessibility_step_intro))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.accessibility_step_1))
                        Text(stringResource(R.string.accessibility_step_2))
                        Text(stringResource(R.string.accessibility_step_3))
                        Text(stringResource(R.string.accessibility_step_4))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAccessibilityDialog = false
                            ScreenLockService.openAccessibilitySettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text(stringResource(R.string.go_to_settings_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAccessibilityDialog = false }) {
                        Text(stringResource(R.string.cancel_button))
                    }
                },
                containerColor = Color(0xFF1E293B),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF94A3B8)
            )
        }

        PermissionCard(
            title = stringResource(R.string.auto_screen_off_title),
            desc = if (hasAccessibilityPermission) stringResource(R.string.auto_screen_off_enabled_desc) else stringResource(R.string.auto_screen_off_optional_desc),
            icon = Icons.Rounded.Accessibility,
            buttonText = if (hasAccessibilityPermission) stringResource(R.string.enabled_button) else stringResource(R.string.enable_button),
            onClick = { 
                showAccessibilityDialog = true
            },
            isGranted = hasAccessibilityPermission
        )
        
        if (hasNotificationPermission) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAllGranted,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Text(stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onClick: () -> Unit,
    isGranted: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Slate900
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) Color(0xFF059669) else Color(0xFF6366F1)
                ),
                enabled = true
            ) {
                if (isGranted) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(buttonText)
            }
        }
    }
}
