package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import com.gollan.fadesleeptimer.ui.AppSettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ControlsSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    SettingsSection(stringResource(R.string.settings_category_controls), breadcrumbSuggestion = breadcrumbSuggestion) {
        SettingsToggle(
            title = stringResource(R.string.face_down_title),
            desc = stringResource(R.string.face_down_desc),
            icon = Icons.Rounded.ScreenRotation,
            checked = settings.faceDownStart,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(faceDownStart = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.face_down_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "face_down",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        SettingsToggle(
            title = stringResource(R.string.shake_extend_title),
            desc = stringResource(R.string.shake_extend_desc),
            icon = Icons.Rounded.Vibration,
            checked = settings.shakeToExtend,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(shakeToExtend = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.shake_extend_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "shake_extend",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        SettingsToggle(
            title = stringResource(R.string.pocket_mode_title),
            desc = stringResource(R.string.pocket_mode_desc),
            icon = Icons.Rounded.DoNotTouch,
            checked = settings.pocketMode,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(pocketMode = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.pocket_mode_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "pocket_mode",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        
        // Gradual Screen Dim Toggle (requires WRITE_SETTINGS permission)
        val hasWriteSettingsPermission = android.provider.Settings.System.canWrite(context)
        SettingsToggle(
            title = stringResource(R.string.gradual_dim_title),
            desc = if (hasWriteSettingsPermission) stringResource(R.string.gradual_dim_desc) else stringResource(R.string.gradual_dim_permission_required),
            icon = Icons.Rounded.Brightness4,
            checked = settings.gradualDimEnabled && hasWriteSettingsPermission,
            onCheckedChange = { enabled ->
                if (!hasWriteSettingsPermission) {
                    // Open system settings to grant permission
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = android.net.Uri.parse("package:" + context.packageName)
                    }
                    context.startActivity(intent)
                } else {
                    onSettingsChanged(settings.copy(gradualDimEnabled = enabled))
                }
            },
            featureId = "gradual_dim",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Widget Discovery Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.widgets_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.widgets_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
