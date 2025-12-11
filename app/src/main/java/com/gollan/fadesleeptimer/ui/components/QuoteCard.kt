package com.gollan.fadesleeptimer.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.data.QuoteRepository

@Composable
fun QuoteCard(
    settings: com.gollan.fadesleeptimer.ui.AppSettings,
    onSparkleClick: (com.gollan.fadesleeptimer.data.Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Build selected categories set based on settings
    val selectedCategories = buildSet {
        if (settings.wisdomQuotesPeaceComfort) add("Peace & Comfort")
        if (settings.wisdomQuotesGodGlory) add("God's Glory & Sovereignty")
        if (settings.wisdomQuotesGospelGrace) add("The Gospel & Grace")
        if (settings.wisdomQuotesWisdomLiving) add("Wisdom & Living")
    }
    
    val quote = remember(selectedCategories) { 
        QuoteRepository.getRandomQuote(selectedCategories) 
    }

    // Gentle fade animation for sparkle icon
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconAlpha"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quote Text - Match ScriptureCard font style
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Author Row - Right Aligned with Grokipedia Link and Sparkle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val authorLink = com.gollan.fadesleeptimer.data.GrokipediaLinks.getLink(quote.author)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (authorLink != null) {
                        Modifier.clickable {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(authorLink)
                                )
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // If browser fails, silently ignore
                            }
                        }
                    } else {
                        Modifier
                    }
                ) {
                    Text(
                        text = "— ${quote.author}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        color = if (authorLink != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (authorLink != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Learn more about ${quote.author}",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Sparkle Button with gentle fade animation
                IconButton(
                    onClick = { onSparkleClick(quote) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Get contextual wisdom",
                        tint = Color(0xFFD4AF37).copy(alpha = iconAlpha), // Muted gold with gentle fade
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
