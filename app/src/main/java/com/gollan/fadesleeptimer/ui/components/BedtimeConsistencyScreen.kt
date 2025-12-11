package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BedtimeConsistencyScreen(
    sleepHistory: List<ChartData>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Bedtime Consistency",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Content
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Assessment, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Last 7 Days",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "See how consistent your sleep schedule has been.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (sleepHistory.isEmpty()) {
                     Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(300.dp),
                         contentAlignment = Alignment.Center
                     ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(
                                 Icons.Rounded.Assessment, 
                                 contentDescription = null, 
                                 tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                 modifier = Modifier.size(48.dp)
                             )
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(
                                 "No sleep data yet",
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                 fontWeight = FontWeight.Bold
                             )
                             Text(
                                 "Complete a timer to see your consistency",
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                 fontSize = 12.sp
                             )
                         }
                     }
                } else {
                    SleepHistoryGraph(
                        data = sleepHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
}
