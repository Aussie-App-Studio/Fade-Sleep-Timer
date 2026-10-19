package com.gollan.fadesleeptimer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.res.stringResource
import com.gollan.fadesleeptimer.R

@Composable
fun HallOfFameScreen(
    name: String?, // Null if empty
    hasAccess: Boolean,
    onClose: () -> Unit,
    onJoinClick: () -> Unit,
    onSupportClick: () -> Unit,
    onReset: () -> Unit = {}
) {
    // Animation State
    val infiniteTransition = rememberInfiniteTransition(label = "hof_bg")
    
    // Time-based animation for organic movement
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Surface(color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mesmerizing Background (3 Distinct Parts)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val maxDim = size.maxDimension
                
                // Helper to create wandering points
                fun getPoint(
                    t: Float, 
                    xPhase: Float, 
                    yPhase: Float, 
                    xSpeed: Float, 
                    ySpeed: Float,
                    radius: Float
                ): androidx.compose.ui.geometry.Offset {
                    val x = width / 2 + kotlin.math.cos(t * xSpeed + xPhase) * (width * radius)
                    val y = height / 2 + kotlin.math.sin(t * ySpeed + yPhase) * (height * radius)
                    return androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                }

                drawRect(Color.Black)

                // Part 1: The Golden Core (Slow, Central, Pulsing)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB45309).copy(alpha = 0.5f), Color.Transparent),
                        center = getPoint(time, 0f, 0f, 0.3f, 0.4f, 0.2f),
                        radius = maxDim * 0.9f
                    )
                )

                // Part 2: The Amber Orbit (Faster, Wider, Elliptical)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.3f), Color.Transparent),
                        center = getPoint(time, 2f, 1f, 0.7f, 0.5f, 0.5f),
                        radius = maxDim * 0.7f
                    )
                )

                // Part 3: The Orange Drift (Counter-movement, Subtle)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEA580C).copy(alpha = 0.25f), Color.Transparent),
                        center = getPoint(time, 4f, 3f, -0.5f, -0.3f, 0.4f),
                        radius = maxDim * 0.8f
                    )
                )
                
                // Subtle Particles/Stars
                // ... (Optional, keeping it clean for now)
            }
            
            Column(modifier = Modifier.fillMaxSize()) {
            
            // HEADER
            Row(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF451A03).copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = Color(0xFFFBBF24)) // Amber-400
                    Spacer(Modifier.width(12.dp))
                    Text("Hall of Fame", color = Color(0xFFFEF3C7), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, null, tint = Color.White) // White close icon
                }
            }

            // BODY
            val permanentLegends = listOf(
                "Dreamy Record",
                "Crash S."
            )
            val allLegends = buildList {
                addAll(permanentLegends)
                if (!name.isNullOrBlank() && permanentLegends.none { it.equals(name.trim(), ignoreCase = true) }) {
                    add(name.trim())
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                androidx.compose.animation.AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(tween(500))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Crown Avatar with Glow
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(30.dp, spotColor = Color(0xFFF59E0B)) // Amber Glow
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stars, // Crown/Star
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFFBBF24)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "SLEEP TIMER LEGENDS",
                            color = Color(0xFFFBBF24).copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        allLegends.forEach { legendName ->
                            Text(
                                text = legendName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // Thank you note
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                "\"Thank you for supporting independent developers. You're the reason we can keep building calm, ad-free experiences.\"",
                                color = Color(0xFFE2E8F0),
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(20.dp),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        if (name == null) {
                            if (hasAccess) {
                                OutlinedButton(
                                    onClick = onJoinClick,
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text("Join the Hall of Fame", color = Color(0xFFFBBF24))
                                }
                            } else {
                                Button(
                                    onClick = onSupportClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF59E0B),
                                        contentColor = Color(0xFF451A03)
                                    )
                                ) {
                                    Text("Immortalize Yourself", fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Unlock via Support Developer",
                                    color = Color(0xFFFBBF24).copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            // Reset Button (Hidden/Subtle)
                            TextButton(onClick = onReset) {
                                Text("Reset (Debug)", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun HallOfFameLinkWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("One-Time Access") 
            }
        },
        text = { 
            Column {
                Text("This Google Form link can only be opened ONCE.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please be ready to fill it out immediately (takes ~2 minutes).")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Once you open it, this link will disappear from the app forever to prevent spam.", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black)
            ) {
                Text("I'm Ready, Open Form")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1)
    )
}

@Composable
fun HallOfFameJoinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    showFormLink: Boolean,
    onOpenForm: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    if (showWarning) {
        HallOfFameLinkWarningDialog(
            onDismiss = { showWarning = false },
            onConfirm = {
                showWarning = false
                onOpenForm()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/KrcRXvBSSUmBFJi28"))
                context.startActivity(intent)
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    stringResource(R.string.hall_of_fame_join_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.hall_of_fame_join_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text(stringResource(R.string.hall_of_fame_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showFormLink) {
                    Spacer(Modifier.height(16.dp))
                    
                    // Google Form Link for permanent submission
                    Text(
                        "Submit permanently via form →",
                        color = Color(0xFFFBBF24).copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            showWarning = true
                        }
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = { onConfirm(name) },
                        enabled = name.isNotBlank()
                    ) {
                        Text(stringResource(R.string.join_button))
                    }
                }
            }
        }
    }
}

@Composable
fun HallOfFameWelcomeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    showFormLink: Boolean,
    onOpenForm: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Gold glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    if (showWarning) {
        HallOfFameLinkWarningDialog(
            onDismiss = { showWarning = false },
            onConfirm = {
                showWarning = false
                onOpenForm()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/KrcRXvBSSUmBFJi28"))
                context.startActivity(intent)
            }
        )
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFF59E0B))
        ) {
            // Animated gold glow background
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF59E0B).copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension * 0.8f
                    )
                )
            }
            
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(2.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Epic crown icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color(0xFFFBBF24).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "ETCH YOUR NAME",
                        color = Color(0xFFFBBF24),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "To display in the beta for free (but unfortunately temporarily), what name should we use?",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Choose your Name", color = Color(0xFFFBBF24).copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF78716C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFFFBBF24)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Google Form Link for permanent submission
                    val context = LocalContext.current
                    Text(
                        "Submit permanently via form →",
                        color = Color(0xFFFBBF24).copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/KrcRXvBSSUmBFJi28"))
                            context.startActivity(intent)
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0xFF374151)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF9CA3AF)
                            )
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = { 
                                if (name.isNotBlank()) {
                                    onConfirm(name)
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color(0xFF451A03),
                                disabledContainerColor = Color(0xFF78716C),
                                disabledContentColor = Color(0xFF57534E)
                            )
                        ) {
                            Text(
                                "Immortalize\nMe!", 
                                fontWeight = FontWeight.Bold, 
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


