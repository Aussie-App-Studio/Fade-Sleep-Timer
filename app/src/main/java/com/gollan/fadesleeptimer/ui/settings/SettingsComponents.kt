package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.gollan.fadesleeptimer.R

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun SettingsSection(
    title: String,
    initiallyExpanded: Boolean = false,
    isExpanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null,
    content: @Composable () -> Unit
) {
    var localExpanded by remember { mutableStateOf(initiallyExpanded) }
    val expandedState = isExpanded ?: localExpanded
    val toggleAction = onToggle ?: { localExpanded = !localExpanded }
    
    // Check if this section contains the suggested feature
    // Map titles to categories
    val audioTitle = stringResource(R.string.settings_category_audio)
    val visualsTitle = stringResource(R.string.settings_category_visuals)
    val wellnessTitle = stringResource(R.string.settings_category_wellness)
    val controlTitle = stringResource(R.string.settings_category_control)
    val advancedTitle = stringResource(R.string.settings_category_advanced)
    val awesomeTitle = stringResource(R.string.settings_category_awesome)

    val category = when(title) {
        audioTitle -> "audio"
        visualsTitle -> "visuals"
        wellnessTitle -> "wellness"
        controlTitle -> "control"
        advancedTitle -> "advanced"
        awesomeTitle -> "awesome"
        else -> ""
    }
    
    val hasSuggestion = breadcrumbSuggestion?.settingsCategory == category
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column {
            // Clickable header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { toggleAction() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title.uppercase(),
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                if (hasSuggestion && !expandedState) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = if (expandedState) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(if (expandedState) R.string.collapse_cd else R.string.expand_cd),
                    tint = Color(0xFF64748B)
                )
            }
            
            // Animated collapsible content
            androidx.compose.animation.AnimatedVisibility(
                visible = expandedState,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
fun IndentedSettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    desc: String,
    icon: ImageVector?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    featureId: String? = null,
    breadcrumbSuggestion: com.gollan.fadesleeptimer.utils.BreadcrumbSuggestion? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    val isSuggested = featureId != null && breadcrumbSuggestion?.featureId == featureId
    
    // Highlight color if suggested
    val backgroundColor = if (isSuggested) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val border = if (isSuggested) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null

    Surface(
        color = backgroundColor,
        border = border,
        shape = RoundedCornerShape(12.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    if (isSuggested) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "NEW",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
    }
}

@Composable
fun SettingsDropdown(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp)
        
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedOption, color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E293B))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChip(text: String, onDelete: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.remove_cd),
                    tint = Color(0xFF64748B),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
        }
    }
}

@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalGapPx = horizontalGap.roundToPx()
        val verticalGapPx = verticalGap.roundToPx()
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + horizontalGapPx
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)
        
        val totalHeight = rows.sumOf { row -> row.maxOf { it.height } } + (rows.size - 1) * verticalGapPx
        
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalGapPx
                }
                y += rowHeight + verticalGapPx
            }
        }
    }
}

@Composable
fun AddPresetDialog(onDismiss: () -> Unit, onAdd: (Int) -> Unit) {
    var input by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.add_timer_preset_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(R.string.minutes_label_caps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            input.toIntOrNull()?.let { onAdd(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.add_button_caps), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun TopicSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
        Text(
            stringResource(R.string.topic_header),
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(options) { option ->
                val isSelected = option == selectedOption
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable { onOptionSelected(option) }
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerPresetsEditor(
    presets: List<Int>,
    onPresetsChanged: (List<Int>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.change_timer_buttons_title), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Text(stringResource(R.string.change_timer_buttons_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Icon(
                if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Chips Grid
                SimpleFlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalGap = 8.dp,
                    verticalGap = 8.dp
                ) {
                    presets.forEach { mins ->
                        val text = when {
                            mins < 60 -> stringResource(R.string.unit_minutes_short, mins)
                            mins % 60 == 0 -> stringResource(R.string.unit_hours_short, mins / 60)
                            else -> stringResource(R.string.unit_hours_decimal_short, "${mins / 60.0}".replace(".0", ""))
                        }
                        PresetChip(
                            text = text,
                            onDelete = {
                                onPresetsChanged(presets - mins)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add & Reset
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { if (presets.size < 8) showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = presets.size < 8
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_button_caps), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    
                    if (presets != listOf(5, 10, 15, 30, 45, 60)) {
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = { onPresetsChanged(listOf(5, 10, 15, 30, 45, 60)) }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.reset_default_button_text), color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPresetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { rawMins ->
                // Snap to nearest 30 mins if > 60 mins
                val mins = if (rawMins > 60) {
                    (kotlin.math.round(rawMins / 30.0) * 30).toInt()
                } else {
                    rawMins
                }

                if (!presets.contains(mins)) {
                    onPresetsChanged((presets + mins).sorted())
                }
                showAddDialog = false
            }
        )
    }
}
