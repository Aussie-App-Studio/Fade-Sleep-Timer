package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BrainDumpDialog(
    settings: com.gollan.fadesleeptimer.ui.AppSettings,
    onDismiss: () -> Unit,
    onConfirm: (text: String, hours: Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var retention by remember { mutableStateOf(12) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModelStoreOwner = androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner.current
    LaunchedEffect(Unit) {
        if (viewModelStoreOwner != null) {
            val viewModel = androidx.lifecycle.ViewModelProvider(viewModelStoreOwner)[com.gollan.fadesleeptimer.viewmodel.TimerViewModel::class.java]
            viewModel.checkAndShowBrainDumpWarning(context)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Allow full width
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface, // Dynamic Surface
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.95f) // Take up 95% of width
                .fillMaxHeight(0.85f) // Take up 85% of height
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.primary) // Dynamic Primary
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Brain Dump", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface, // Dynamic OnSurface
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    "Unload your mind before sleep. Notes clear automatically.", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Dynamic OnSurfaceVariant
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Fill remaining space
                    placeholder = { Text("Worries, to-dos, or ideas for tomorrow...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Retention Toggles
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear in:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    
                    RetentionChip(
                        label = "12h",
                        selected = retention == 12,
                        onClick = { retention = 12 }
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    RetentionChip(
                        label = "24h",
                        selected = retention == 24,
                        onClick = { retention = 24 }
                    )
                }

                // Main Action
                Button(
                    onClick = { onConfirm(text, retention) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Dynamic Primary
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (text.isBlank()) "Skip & Sleep" else "Save & Sleep",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    }


                // Brain Dump Ad - Removed as requested

            }
            }
        }
    }

@Composable
fun RetentionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
