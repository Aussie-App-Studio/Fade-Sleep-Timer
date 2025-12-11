package com.gollan.fadesleeptimer.ui.settings

import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gollan.fadesleeptimer.ui.theme.*
import com.gollan.fadesleeptimer.ui.OpenOnChargeMode

@Composable
fun OpenOnChargeDialog(
    currentMode: OpenOnChargeMode,
    onModeSelected: (OpenOnChargeMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.open_on_charge_dialog_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    stringResource(R.string.open_on_charge_dialog_desc),
                    color = Slate700,
                    fontSize = 14.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeOption(
                        title = stringResource(R.string.open_on_charge_always),
                        subtitle = stringResource(R.string.open_on_charge_always_desc),
                        selected = currentMode == OpenOnChargeMode.ALWAYS,
                        onClick = { onModeSelected(OpenOnChargeMode.ALWAYS) }
                    )
                    ModeOption(
                        title = stringResource(R.string.open_on_charge_strict),
                        subtitle = stringResource(R.string.open_on_charge_strict_desc),
                        selected = currentMode == OpenOnChargeMode.STRICT,
                        onClick = { onModeSelected(OpenOnChargeMode.STRICT) }
                    )
                    ModeOption(
                        title = stringResource(R.string.open_on_charge_late),
                        subtitle = stringResource(R.string.open_on_charge_late_desc),
                        selected = currentMode == OpenOnChargeMode.LATE,
                        onClick = { onModeSelected(OpenOnChargeMode.LATE) }
                    )
                    ModeOption(
                        title = stringResource(R.string.open_on_charge_morning),
                        subtitle = stringResource(R.string.open_on_charge_morning_desc),
                        selected = currentMode == OpenOnChargeMode.MORNING,
                        onClick = { onModeSelected(OpenOnChargeMode.MORNING) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_button).uppercase(), color = Slate700)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) Indigo500.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Indigo500,
                unselectedColor = Slate700
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Slate700, fontSize = 12.sp)
        }
    }
}
