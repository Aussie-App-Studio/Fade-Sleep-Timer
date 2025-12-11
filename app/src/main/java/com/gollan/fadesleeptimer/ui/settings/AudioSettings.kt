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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.ui.FadeDurationSlider

@Composable
fun AudioSettings(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    SettingsSection(stringResource(R.string.settings_category_audio), breadcrumbSuggestion = breadcrumbSuggestion) {
        // Fade Duration Expandable Setting
        var isFadeExpanded by remember { mutableStateOf(false) }
        
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFadeExpanded = !isFadeExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Rounded.Timelapse, // Or AccessTime if Timelapse not available
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.fade_out_duration_title), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Text(stringResource(R.string.fade_out_duration_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.unit_minutes_value, settings.fadeDuration),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        if (isFadeExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = stringResource(if (isFadeExpanded) R.string.collapse_cd else R.string.expand_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isFadeExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 16.dp)) {
                    FadeDurationSlider(
                        value = settings.fadeDuration,
                        onValueChange = { onSettingsChanged(settings.copy(fadeDuration = it)) }
                    )
                }
            }
        }
    
        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
        
        SettingsToggle(
            title = stringResource(R.string.volume_safety_cap_title),
            desc = stringResource(R.string.volume_safety_cap_desc),
            icon = Icons.Rounded.Shield,
            checked = settings.volumeSafetyCap,
            onCheckedChange = { onSettingsChanged(settings.copy(volumeSafetyCap = it)) },
            featureId = "volume_safety",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        SettingsToggle(
            title = stringResource(R.string.play_on_start_title),
            desc = stringResource(R.string.play_on_start_desc),
            icon = Icons.Rounded.PlayArrow,
            checked = settings.playOnStart,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(playOnStart = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.play_on_start_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "play_on_start",
            breadcrumbSuggestion = breadcrumbSuggestion
        )
        SettingsToggle(
            title = stringResource(R.string.smart_wait_title),
            desc = stringResource(R.string.smart_wait_desc),
            icon = Icons.Rounded.PauseCircle,
            checked = settings.smartWait,
            onCheckedChange = { 
                onSettingsChanged(settings.copy(smartWait = it))
                if (it) android.widget.Toast.makeText(context, context.getString(R.string.smart_wait_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            featureId = "smart_wait",
            breadcrumbSuggestion = breadcrumbSuggestion
        )

    }
}
