package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrayerCard(
    modifier: Modifier = Modifier
) {
    val prayerPrompts = remember {
        listOf(
            // The Basics
            "What are you most thankful for today?",
            "Is there a worry you need to let go of?",
            "Do you need to repent & ask forgiveness?",
            "Do you need to ask for wisdom?",
            "Be still and know.",
            "Hand over tomorrow's battles.",
            "Find something to praise God for.",
            
            // Reformed / Theologically Rich
            "Rest in His perfect sovereignty.",
            "Search your heart for hidden idols.",
            "Meditate on His finished work.",
            "Trust His providence for tomorrow.",
            "Remember your adoption in Christ.",
            "Seek His glory above all else.",
            "Preach the Gospel to yourself.",
            "He who keeps you will not slumber."
        )
    }
    
    // Pick a random prompt once
    val currentPrompt = remember { prayerPrompts.random() }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), // Distinctive container
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), // Strong border
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite, // Heart
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Pause & Pray",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentPrompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
