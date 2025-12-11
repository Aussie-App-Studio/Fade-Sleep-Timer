package com.gollan.fadesleeptimer.ui.theme

import android.app.Activity

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Color Definitions ---

// Default (Slate)
val Slate300 = Color(0xFFCBD5E1)
// Slate400, Slate700, Slate800, Slate900, Slate950 are in AppColors.kt
// Indigo500, Indigo400 are in AppColors.kt
val Indigo600 = Color(0xFF4F46E5)
val Indigo200 = Color(0xFFC7D2FE)
val Purple600 = Color(0xFF9333EA)
val Pink200 = Color(0xFFFBCFE8)
val Red600 = Color(0xFFDC2626)
val Red950 = Color(0xFF450A0A)
val Red500 = Color(0xFFEF4444)

// --- Color Schemes ---

private val DefaultColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo600,
    inversePrimary = Indigo200,
    background = Slate950,
    onBackground = Slate300,
    surface = Slate900,
    onSurface = Slate300,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate800,
    outlineVariant = Slate700,
    secondary = Indigo400,
    secondaryContainer = Pink200,
    tertiary = Purple600,
    error = Red600,
    onError = Color.White,
    errorContainer = Red950,
    onErrorContainer = Red500
)

// 1. Glassy & Elegant
private val GlassyElegantScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0x1AFFFFFF), // Translucent
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0x26FFFFFF),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0x40FFFFFF),
    outlineVariant = Color(0x1AFFFFFF),
    secondary = Color(0xFF94A3B8),
    tertiary = Color(0xFF64748B)
)

// 2. Red Night
private val RedNightScheme = darkColorScheme(
    primary = Color(0xFFCC0000),
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFEF4444),
    surface = Color(0xFF1A0000),
    onSurface = Color(0xFFEF4444),
    surfaceVariant = Color(0xFF330000),
    onSurfaceVariant = Color(0xFFB91C1C),
    outline = Color(0xFF450A0A),
    outlineVariant = Color(0xFF2B0505),
    secondary = Color(0xFF991B1B),
    tertiary = Color(0xFF7F1D1D)
)

// 3. Wild & Crazy
private val WildCrazyScheme = darkColorScheme(
    primary = Color(0xFF00FFFF), // Cyan
    onPrimary = Color.Black,
    background = Color(0xFFFFFF00), // Yellow
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFF00FF), // Magenta
    onSurface = Color.White,
    surfaceVariant = Color(0xFFD500F9),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF00E5FF),
    outlineVariant = Color(0xFF00B8D4),
    secondary = Color(0xFF00E676), // Green
    tertiary = Color(0xFFFF3D00) // Orange
)

// 4. Calming Green
private val CalmingGreenScheme = darkColorScheme(
    primary = Color(0xFF10b981), // Emerald
    onPrimary = Color.White,
    background = Color(0xFF052e16),
    onBackground = Color(0xFFD1FAE5),
    surface = Color(0xFF064e3b),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF065F46),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF047857),
    outlineVariant = Color(0xFF064E3B),
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFF059669)
)

// 5. OLED Black
private val OledBlackScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF222222),
    secondary = Color(0xFFCCCCCC),
    tertiary = Color(0xFF999999)
)

// 6. Girly Pink
private val GirlyPinkScheme = darkColorScheme(
    primary = Color(0xFFFB7185),
    onPrimary = Color.White,
    background = Color(0xFFFFF1F2),
    onBackground = Color(0xFF881337),
    surface = Color(0xFFFFE4E6),
    onSurface = Color(0xFF9F1239),
    surfaceVariant = Color(0xFFFECDD3),
    onSurfaceVariant = Color(0xFFBE123C),
    outline = Color(0xFFFDA4AF),
    outlineVariant = Color(0xFFFECDD3),
    secondary = Color(0xFFF43F5E),
    tertiary = Color(0xFFE11D48)
)

// 7. Manly Blue
private val ManlyBlueScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFDBEAFE),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFBFDBFE),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF93C5FD),
    outline = Color(0xFF1E40AF),
    outlineVariant = Color(0xFF172554),
    secondary = Color(0xFF60A5FA),
    tertiary = Color(0xFF2563EB)
)

// 8. Bitcoin Orange
private val BitcoinOrangeScheme = darkColorScheme(
    primary = Color(0xFFF7931A),
    onPrimary = Color.White,
    background = Color(0xFF1a1a1a),
    onBackground = Color(0xFFFFE0B2),
    surface = Color(0xFF2d2d2d),
    onSurface = Color(0xFFFFCC80),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFFFB74D),
    outline = Color(0xFFE65100),
    outlineVariant = Color(0xFF3E2723),
    secondary = Color(0xFFFF9800),
    tertiary = Color(0xFFFB8C00)
)

// 9. Ukrainian
private val UkrainianScheme = darkColorScheme(
    primary = Color(0xFFFFDD00), // Yellow
    onPrimary = Color.Black,
    background = Color(0xFF0057B7), // Blue
    onBackground = Color.White,
    surface = Color(0xFF004494),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF003366),
    onSurfaceVariant = Color(0xFFFFF9C4),
    outline = Color(0xFFFFD600),
    outlineVariant = Color(0xFFFBC02D),
    secondary = Color(0xFFFFEA00),
    tertiary = Color(0xFFFFD600)
)

// 10. CRCA Maroon
private val CrcaMaroonScheme = darkColorScheme(
    primary = Color(0xFF800000),
    onPrimary = Color.White,
    background = Color(0xFF2D0000),
    onBackground = Color(0xFFFFCDD2),
    surface = Color(0xFF4A0000),
    onSurface = Color(0xFFFFEBEE),
    surfaceVariant = Color(0xFF5D0000),
    onSurfaceVariant = Color(0xFFEF9A9A),
    outline = Color(0xFFB71C1C),
    outlineVariant = Color(0xFF7F0000),
    secondary = Color(0xFFA52714),
    tertiary = Color(0xFFC62828)
)

// 11. Forest Mist
private val ForestMistScheme = darkColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    background = Color(0xFF1B5E20),
    onBackground = Color(0xFFC8E6C9),
    surface = Color(0xFF263238),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF37474F),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF455A64),
    outlineVariant = Color(0xFF263238),
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF388E3C)
)

// 12. Ocean Depth
private val OceanDepthScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color.Black,
    background = Color(0xFF006064),
    onBackground = Color(0xFFE0F7FA),
    surface = Color(0xFF00838F),
    onSurface = Color(0xFFB2EBF2),
    surfaceVariant = Color(0xFF0097A7),
    onSurfaceVariant = Color(0xFF80DEEA),
    outline = Color(0xFF00ACC1),
    outlineVariant = Color(0xFF006064),
    secondary = Color(0xFF26C6DA),
    tertiary = Color(0xFF0097A7)
)

// 13. Desert Dune
private val DesertDuneScheme = darkColorScheme(
    primary = Color(0xFFD84315),
    onPrimary = Color.White,
    background = Color(0xFF3E2723),
    onBackground = Color(0xFFFFCCBC),
    surface = Color(0xFF4E342E),
    onSurface = Color(0xFFFFAB91),
    surfaceVariant = Color(0xFF5D4037),
    onSurfaceVariant = Color(0xFFFF8A65),
    outline = Color(0xFF6D4C41),
    outlineVariant = Color(0xFF3E2723),
    secondary = Color(0xFFFF5722),
    tertiary = Color(0xFFE64A19)
)

// 14. Arctic Frost
private val ArcticFrostScheme = darkColorScheme(
    primary = Color(0xFF03A9F4),
    onPrimary = Color.White,
    background = Color(0xFFE1F5FE),
    onBackground = Color(0xFF01579B),
    surface = Color(0xFFB3E5FC),
    onSurface = Color(0xFF0277BD),
    surfaceVariant = Color(0xFF81D4FA),
    onSurfaceVariant = Color(0xFF0288D1),
    outline = Color(0xFF4FC3F7),
    outlineVariant = Color(0xFF29B6F6),
    secondary = Color(0xFF039BE5),
    tertiary = Color(0xFF0288D1)
)

// 15. Lavender Haze
private val LavenderHazeScheme = darkColorScheme(
    primary = Color(0xFF9C27B0),
    onPrimary = Color.White,
    background = Color(0xFFF3E5F5),
    onBackground = Color(0xFF4A148C),
    surface = Color(0xFFE1BEE7),
    onSurface = Color(0xFF6A1B9A),
    surfaceVariant = Color(0xFFCE93D8),
    onSurfaceVariant = Color(0xFF7B1FA2),
    outline = Color(0xFFBA68C8),
    outlineVariant = Color(0xFFAB47BC),
    secondary = Color(0xFF8E24AA),
    tertiary = Color(0xFF7B1FA2)
)

// 16. Cyberpunk Neon
private val CyberpunkNeonScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0F7FA),
    surface = Color(0xFF212121),
    onSurface = Color(0xFF00E5FF),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFF18FFFF),
    outline = Color(0xFF00B8D4),
    outlineVariant = Color(0xFF006064),
    secondary = Color(0xFFFF4081),
    tertiary = Color(0xFFEA80FC)
)

// 17. Retro Synthwave
private val RetroSynthwaveScheme = darkColorScheme(
    primary = Color(0xFFFF4081),
    onPrimary = Color.White,
    background = Color(0xFF311B92),
    onBackground = Color(0xFFFF80AB),
    surface = Color(0xFF4527A0),
    onSurface = Color(0xFFFF4081),
    surfaceVariant = Color(0xFF512DA8),
    onSurfaceVariant = Color(0xFFEA80FC),
    outline = Color(0xFF7C4DFF),
    outlineVariant = Color(0xFF651FFF),
    secondary = Color(0xFFFFAB40),
    tertiary = Color(0xFFFF6E40)
)

// 18. Matrix Code
private val MatrixCodeScheme = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFF00C853),
    surface = Color(0xFF1B5E20),
    onSurface = Color(0xFF69F0AE),
    surfaceVariant = Color(0xFF2E7D32),
    onSurfaceVariant = Color(0xFFB9F6CA),
    outline = Color(0xFF00C853),
    outlineVariant = Color(0xFF006064),
    secondary = Color(0xFF00C853),
    tertiary = Color(0xFF00E676)
)

// 19. Space Gray
private val SpaceGrayScheme = darkColorScheme(
    primary = Color(0xFF9E9E9E),
    onPrimary = Color.Black,
    background = Color(0xFF212121),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF424242),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF616161),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242),
    secondary = Color(0xFFBDBDBD),
    tertiary = Color(0xFF9E9E9E)
)

// 20. High Contrast
private val HighContrastScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF000000),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFFFFFFF),
    outlineVariant = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFFFFFFF)
)

// 21. Toxic Lime
private val ToxicLimeScheme = darkColorScheme(
    primary = Color(0xFFC6FF00),
    onPrimary = Color.Black,
    background = Color(0xFF212121),
    onBackground = Color(0xFFF4FF81),
    surface = Color(0xFF424242),
    onSurface = Color(0xFFEEFF41),
    surfaceVariant = Color(0xFF616161),
    onSurfaceVariant = Color(0xFFC6FF00),
    outline = Color(0xFFAEEA00),
    outlineVariant = Color(0xFF64DD17),
    secondary = Color(0xFF76FF03),
    tertiary = Color(0xFF64DD17)
)

// 22. Solar Flare
private val SolarFlareScheme = darkColorScheme(
    primary = Color(0xFFFF3D00),
    onPrimary = Color.White,
    background = Color(0xFFBF360C),
    onBackground = Color(0xFFFFCCBC),
    surface = Color(0xFFD84315),
    onSurface = Color(0xFFFFAB91),
    surfaceVariant = Color(0xFFE64A19),
    onSurfaceVariant = Color(0xFFFF8A65),
    outline = Color(0xFFF4511E),
    outlineVariant = Color(0xFFBF360C),
    secondary = Color(0xFFFF6E40),
    tertiary = Color(0xFFFF9E80)
)

// 23. Cotton Candy
private val CottonCandyScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color.Black,
    background = Color(0xFFE3F2FD),
    onBackground = Color(0xFF880E4F),
    surface = Color(0xFFFCE4EC),
    onSurface = Color(0xFFC2185B),
    surfaceVariant = Color(0xFFF8BBD0),
    onSurfaceVariant = Color(0xFFE91E63),
    outline = Color(0xFFF06292),
    outlineVariant = Color(0xFFEC407A),
    secondary = Color(0xFF90CAF9),
    tertiary = Color(0xFF64B5F6)
)

// 24. Deep Magenta
private val DeepMagentaScheme = darkColorScheme(
    primary = Color(0xFFE040FB),
    onPrimary = Color.White,
    background = Color(0xFF4A148C),
    onBackground = Color(0xFFF3E5F5),
    surface = Color(0xFF7B1FA2),
    onSurface = Color(0xFFE1BEE7),
    surfaceVariant = Color(0xFF8E24AA),
    onSurfaceVariant = Color(0xFFCE93D8),
    outline = Color(0xFFAB47BC),
    outlineVariant = Color(0xFF6A1B9A),
    secondary = Color(0xFFD500F9),
    tertiary = Color(0xFFAA00FF)
)

// 25. Electric Violet
private val ElectricVioletScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    background = Color(0xFF311B92),
    onBackground = Color(0xFFD1C4E9),
    surface = Color(0xFF512DA8),
    onSurface = Color(0xFFB39DDB),
    surfaceVariant = Color(0xFF673AB7),
    onSurfaceVariant = Color(0xFF9575CD),
    outline = Color(0xFF7E57C2),
    outlineVariant = Color(0xFF4527A0),
    secondary = Color(0xFF651FFF),
    tertiary = Color(0xFF6200EA)
)

// 26. Royal Gold
private val RoyalGoldScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    onPrimary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFECB3),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFFFE082),
    surfaceVariant = Color(0xFF212121),
    onSurfaceVariant = Color(0xFFFFD54F),
    outline = Color(0xFFFFC107),
    outlineVariant = Color(0xFFFFA000),
    secondary = Color(0xFFFFCA28),
    tertiary = Color(0xFFFFB300)
)

// 27. Midnight Velvet
private val MidnightVelvetScheme = darkColorScheme(
    primary = Color(0xFF7E57C2),
    onPrimary = Color.White,
    background = Color(0xFF1A237E),
    onBackground = Color(0xFFC5CAE9),
    surface = Color(0xFF283593),
    onSurface = Color(0xFF9FA8DA),
    surfaceVariant = Color(0xFF303F9F),
    onSurfaceVariant = Color(0xFF7986CB),
    outline = Color(0xFF3949AB),
    outlineVariant = Color(0xFF1A237E),
    secondary = Color(0xFF5C6BC0),
    tertiary = Color(0xFF3F51B5)
)

// 28. Rose Gold
private val RoseGoldScheme = darkColorScheme(
    primary = Color(0xFFF8BBD0),
    onPrimary = Color.Black,
    background = Color(0xFF4E342E),
    onBackground = Color(0xFFFCE4EC),
    surface = Color(0xFF5D4037),
    onSurface = Color(0xFFF8BBD0),
    surfaceVariant = Color(0xFF6D4C41),
    onSurfaceVariant = Color(0xFFF48FB1),
    outline = Color(0xFF8D6E63),
    outlineVariant = Color(0xFF4E342E),
    secondary = Color(0xFFF06292),
    tertiary = Color(0xFFEC407A)
)

// 29. Coffee House
private val CoffeeHouseScheme = darkColorScheme(
    primary = Color(0xFF8D6E63),
    onPrimary = Color.White,
    background = Color(0xFF3E2723),
    onBackground = Color(0xFFD7CCC8),
    surface = Color(0xFF4E342E),
    onSurface = Color(0xFFBCAAA4),
    surfaceVariant = Color(0xFF5D4037),
    onSurfaceVariant = Color(0xFFA1887F),
    outline = Color(0xFF795548),
    outlineVariant = Color(0xFF3E2723),
    secondary = Color(0xFF6D4C41),
    tertiary = Color(0xFF5D4037)
)

// 30. Slate & Stone
private val SlateStoneScheme = darkColorScheme(
    primary = Color(0xFF78909C),
    onPrimary = Color.White,
    background = Color(0xFF263238),
    onBackground = Color(0xFFCFD8DC),
    surface = Color(0xFF37474F),
    onSurface = Color(0xFFB0BEC5),
    surfaceVariant = Color(0xFF455A64),
    onSurfaceVariant = Color(0xFF90A4AE),
    outline = Color(0xFF607D8B),
    outlineVariant = Color(0xFF263238),
    secondary = Color(0xFF546E7A),
    tertiary = Color(0xFF455A64)
)

// 31. True Grayscale (In-App Monochrome)
private val TrueGrayscaleScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242),
    secondary = Color(0xFFB0B0B0),
    tertiary = Color(0xFF9E9E9E)
)

@Composable
fun FadeSleepTimerTheme(
    themeName: String = "default",
    overrideMonochrome: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (overrideMonochrome) {
        TrueGrayscaleScheme
    } else {
        when (themeName) {
            "glassy_elegant" -> GlassyElegantScheme
            "red_night" -> RedNightScheme
            "wild_crazy" -> WildCrazyScheme
            "calming_green" -> CalmingGreenScheme
            "oled_black" -> OledBlackScheme
            "girly_pink" -> GirlyPinkScheme
            "manly_blue" -> ManlyBlueScheme
            "bitcoin_orange" -> BitcoinOrangeScheme
            "ukrainian" -> UkrainianScheme
            "crca_maroon" -> CrcaMaroonScheme
            "forest_mist" -> ForestMistScheme
            "ocean_depth" -> OceanDepthScheme
            "desert_dune" -> DesertDuneScheme
            "arctic_frost" -> ArcticFrostScheme
            "lavender_haze" -> LavenderHazeScheme
            "cyberpunk_neon" -> CyberpunkNeonScheme
            "retro_synthwave" -> RetroSynthwaveScheme
            "matrix_code" -> MatrixCodeScheme
            "space_gray" -> SpaceGrayScheme
            "high_contrast" -> HighContrastScheme
            "toxic_lime" -> ToxicLimeScheme
            "solar_flare" -> SolarFlareScheme
            "cotton_candy" -> CottonCandyScheme
            "deep_magenta" -> DeepMagentaScheme
            "electric_violet" -> ElectricVioletScheme
            "royal_gold" -> RoyalGoldScheme
            "midnight_velvet" -> MidnightVelvetScheme
            "rose_gold" -> RoseGoldScheme
            "coffee_house" -> CoffeeHouseScheme
            "slate_stone" -> SlateStoneScheme
            else -> DefaultColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
