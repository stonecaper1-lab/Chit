package io.github.stonecaper1lab.chit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Expressive shapes: bigger, softer corners.
private val ChitShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(44.dp),
)

// Expressive type: heavier, more confident weights (system font, no font files).
private val base = Typography()
private val ChitTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold),
)

// Vibrant brand colours for Android 11 and older (Android 12+ uses wallpaper colours).
private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6B3A), onPrimary = Color.White,
    primaryContainer = Color(0xFFA5F5B4), onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF4F6354), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8D5), onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF3B6470), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBEEAF8), onTertiaryContainer = Color(0xFF001F28),
    background = Color(0xFFF6FBF3), onBackground = Color(0xFF181D18),
    surface = Color(0xFFF6FBF3), onSurface = Color(0xFF181D18),
    surfaceVariant = Color(0xFFDDE5DB), onSurfaceVariant = Color(0xFF414941),
    outline = Color(0xFF717971),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AD89A), onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF005229), onPrimaryContainer = Color(0xFFA5F5B4),
    secondary = Color(0xFFB6CCBA), onSecondary = Color(0xFF223527),
    secondaryContainer = Color(0xFF384B3C), onSecondaryContainer = Color(0xFFD1E8D5),
    tertiary = Color(0xFFA3CDDB), onTertiary = Color(0xFF053541),
    tertiaryContainer = Color(0xFF224C58), onTertiaryContainer = Color(0xFFBEEAF8),
    background = Color(0xFF101510), onBackground = Color(0xFFDFE4DC),
    surface = Color(0xFF101510), onSurface = Color(0xFFDFE4DC),
    surfaceVariant = Color(0xFF414941), onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF8B938A),
)

@Composable
fun ChitTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, typography = ChitTypography, shapes = ChitShapes, content = content)
}
