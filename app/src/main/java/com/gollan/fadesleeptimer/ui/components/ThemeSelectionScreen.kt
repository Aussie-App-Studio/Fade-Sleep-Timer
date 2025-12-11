package com.gollan.fadesleeptimer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.ui.theme.*

data class ThemeOption(
    val id: String,
    val name: String,
    val primary: Color,
    val background: Color,
    val surface: Color,
    val isPremium: Boolean = false
)

@Composable
fun ThemeSelectionScreen(
    settings: AppSettings,
    onThemeSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val themes = remember {
        listOf(
            // Top Picks (User Requested Order)
            ThemeOption("default", "Default (Slate)", Indigo500, Slate950, Slate900),
            ThemeOption("glassy_elegant", "Glassy & Elegant", Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0x1AFFFFFF)),
            ThemeOption("oled_black", "OLED Black", Color.White, Color.Black, Color.Black),
            ThemeOption("bitcoin_orange", "Bitcoin Orange", Color(0xFFF7931A), Color(0xFF1a1a1a), Color(0xFF2d2d2d)),
            ThemeOption("royal_gold", "Royal Gold", Color(0xFFFFD700), Color(0xFF000000), Color(0xFF121212)),
            ThemeOption("slate_stone", "Slate & Stone", Color(0xFF78909C), Color(0xFF263238), Color(0xFF37474F)),
            ThemeOption("red_night", "Red Night", Color(0xFFCC0000), Color(0xFF000000), Color(0xFF1A0000)),

            // The Rest
            ThemeOption("wild_crazy", "Wild & Crazy", Color(0xFF00FFFF), Color(0xFFFFFF00), Color(0xFFFF00FF)),
            ThemeOption("calming_green", "Calming Green", Color(0xFF10b981), Color(0xFF052e16), Color(0xFF064e3b)),
            ThemeOption("girly_pink", "Girly Pink", Color(0xFFFB7185), Color(0xFFFFF1F2), Color(0xFFFFE4E6)),
            ThemeOption("manly_blue", "Manly Blue", Color(0xFF3B82F6), Color(0xFF0B1120), Color(0xFF1E293B)),
            ThemeOption("ukrainian", "Ukrainian", Color(0xFFFFDD00), Color(0xFF0057B7), Color(0xFF004494)),
            ThemeOption("crca_maroon", "CRCA Maroon", Color(0xFF800000), Color(0xFF2D0000), Color(0xFF4A0000)),
            
            // Nature
            ThemeOption("forest_mist", "Forest Mist", Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF263238)),
            ThemeOption("ocean_depth", "Ocean Depth", Color(0xFF00BCD4), Color(0xFF006064), Color(0xFF00838F)),
            ThemeOption("desert_dune", "Desert Dune", Color(0xFFD84315), Color(0xFF3E2723), Color(0xFF4E342E)),
            ThemeOption("arctic_frost", "Arctic Frost", Color(0xFF03A9F4), Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
            ThemeOption("lavender_haze", "Lavender Haze", Color(0xFF9C27B0), Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
            
            // Tech
            ThemeOption("cyberpunk_neon", "Cyberpunk Neon", Color(0xFF00E5FF), Color(0xFF121212), Color(0xFF212121)),
            ThemeOption("retro_synthwave", "Retro Synthwave", Color(0xFFFF4081), Color(0xFF311B92), Color(0xFF4527A0)),
            ThemeOption("matrix_code", "Matrix Code", Color(0xFF00E676), Color(0xFF000000), Color(0xFF1B5E20)),
            ThemeOption("space_gray", "Space Gray", Color(0xFF9E9E9E), Color(0xFF212121), Color(0xFF424242)),
            ThemeOption("high_contrast", "High Contrast", Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFF000000)),
            
            // Vibrant
            ThemeOption("toxic_lime", "Toxic Lime", Color(0xFFC6FF00), Color(0xFF212121), Color(0xFF424242)),
            ThemeOption("solar_flare", "Solar Flare", Color(0xFFFF3D00), Color(0xFFBF360C), Color(0xFFD84315)),
            ThemeOption("cotton_candy", "Cotton Candy", Color(0xFFF48FB1), Color(0xFFE3F2FD), Color(0xFFFCE4EC)),
            ThemeOption("deep_magenta", "Deep Magenta", Color(0xFFE040FB), Color(0xFF4A148C), Color(0xFF7B1FA2)),
            ThemeOption("electric_violet", "Electric Violet", Color(0xFF7C4DFF), Color(0xFF311B92), Color(0xFF512DA8)),
            
            // Luxury
            ThemeOption("midnight_velvet", "Midnight Velvet", Color(0xFF7E57C2), Color(0xFF1A237E), Color(0xFF283593)),
            ThemeOption("rose_gold", "Rose Gold", Color(0xFFF8BBD0), Color(0xFF4E342E), Color(0xFF5D4037)),
            ThemeOption("coffee_house", "Coffee House", Color(0xFF8D6E63), Color(0xFF3E2723), Color(0xFF4E342E))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Select Theme",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themes) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = settings.theme == theme.id,
                    onClick = { onThemeSelected(theme.id) }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // UI Preview Mockup
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mock Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.surface)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.surface)
                    )
                }

                // Mock Content
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.primary.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.surface)
                    )
                }

                // Mock Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (theme.primary.luminance() > 0.5f) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f))
                    )
                }
            }

            // Theme Name Label (Bottom Overlay)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            // Selected Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
