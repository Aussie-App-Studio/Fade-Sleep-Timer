package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.theme.Slate800
import com.gollan.fadesleeptimer.ui.theme.Slate900

@Composable
fun ButtonVisibilityDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUpdate: (AppSettings) -> Unit
) {
    var configureVisible by remember { mutableStateOf(settings.configureButtonVisible) }
    var spotifyVisible by remember { mutableStateOf(settings.spotifyButtonVisible) }
    var ytMusicVisible by remember { mutableStateOf(settings.ytMusicButtonVisible) }
    var audibleVisible by remember { mutableStateOf(settings.audibleButtonVisible) }
    var youtubeVisible by remember { mutableStateOf(settings.youtubeButtonVisible) }
    var youversionVisible by remember { mutableStateOf(settings.youversionButtonVisible) }
    var appleMusicVisible by remember { mutableStateOf(settings.appleMusicButtonVisible) }
    var podcastsVisible by remember { mutableStateOf(settings.podcastsButtonVisible) }
    var amazonMusicVisible by remember { mutableStateOf(settings.amazonMusicButtonVisible) }
    var streamingVisible by remember { mutableStateOf(settings.streamingButtonVisible) }
    var calmVisible by remember { mutableStateOf(settings.calmButtonVisible) }
    var headspaceVisible by remember { mutableStateOf(settings.headspaceButtonVisible) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // Max 90% of screen height
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = stringResource(R.string.button_config_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = stringResource(R.string.button_config_desc),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Scrollable checkboxes area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Configure button (toggleable)
                        ButtonCheckbox(
                            label = stringResource(R.string.button_configure),
                            checked = configureVisible,
                            onCheckedChange = { configureVisible = it },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Slate800.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    // Checkboxes for each button
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_spotify),
                            checked = spotifyVisible,
                            onCheckedChange = { spotifyVisible = it },
                            color = Color(0xFF4ADE80) // Spotify green
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_youtube_music),
                            checked = ytMusicVisible,
                            onCheckedChange = { ytMusicVisible = it },
                            color = Color(0xFFF87171) // YouTube red
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_audible),
                            checked = audibleVisible,
                            onCheckedChange = { audibleVisible = it },
                            color = Color(0xFFFB923C) // Audible orange
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_youtube),
                            checked = youtubeVisible,
                            onCheckedChange = { youtubeVisible = it },
                            color = Color(0xFFF87171) // YouTube red
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_bible),
                            checked = youversionVisible,
                            onCheckedChange = { youversionVisible = it },
                            color = Color(0xFF8B4513) // Brown/Rust
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_apple_music),
                            checked = appleMusicVisible,
                            onCheckedChange = { appleMusicVisible = it },
                            color = Color(0xFFFFFFFF) // White
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_podcasts),
                            checked = podcastsVisible,
                            onCheckedChange = { podcastsVisible = it },
                            color = Color(0xFF892CA0) // Purple
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_amazon_music),
                            checked = amazonMusicVisible,
                            onCheckedChange = { amazonMusicVisible = it },
                            color = Color(0xFF1DB0C4) // Teal
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_streaming),
                            checked = streamingVisible,
                            onCheckedChange = { streamingVisible = it },
                            color = Color(0xFF4A90E2) // Blue
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_calm),
                            checked = calmVisible,
                            onCheckedChange = { calmVisible = it },
                            color = Color(0xFF169BDB) // Calm blue
                        )
                    }
                    
                    item {
                        ButtonCheckbox(
                            label = stringResource(R.string.button_headspace),
                            checked = headspaceVisible,
                            onCheckedChange = { headspaceVisible = it },
                            color = Color(0xFFF4794E) // Headspace orange
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action buttons (always visible at bottom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }
                    
                    Button(
                        onClick = {
                            val updatedSettings = settings.copy(
                                configureButtonVisible = configureVisible,
                                spotifyButtonVisible = spotifyVisible,
                                ytMusicButtonVisible = ytMusicVisible,
                                audibleButtonVisible = audibleVisible,
                                youtubeButtonVisible = youtubeVisible,
                                youversionButtonVisible = youversionVisible,
                                appleMusicButtonVisible = appleMusicVisible,
                                podcastsButtonVisible = podcastsVisible,
                                amazonMusicButtonVisible = amazonMusicVisible,
                                streamingButtonVisible = streamingVisible,
                                calmButtonVisible = calmVisible,
                                headspaceButtonVisible = headspaceVisible
                            )
                            onUpdate(updatedSettings)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1) // Indigo
                        )
                    ) {
                        Text(stringResource(R.string.button_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (label) {
                    "Configure" -> Icons.Rounded.Settings
                    "Bible" -> Icons.AutoMirrored.Rounded.MenuBook
                    "Streaming" -> Icons.Rounded.Tv
                    "Calm" -> Icons.Rounded.Spa
                    "Headspace" -> Icons.Rounded.SelfImprovement
                    else -> Icons.Rounded.MusicNote
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f)
            )
        }
        
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = Slate800,
                disabledCheckedColor = color.copy(alpha = 0.8f)
            )
        )
    }
}
