package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Blueprint Section 15.2: Consistent spacing and corner radius tokens.
 */
object UtsavSpacing {
    val XS: Dp = 4.dp   // Small icon gaps
    val SM: Dp = 8.dp   // Label spacing
    val MD: Dp = 12.dp  // Compact card content
    val LG: Dp = 16.dp  // Standard screen padding
    val XL: Dp = 24.dp  // Major card padding
    val XXL: Dp = 32.dp // Section separation
}

object UtsavCorners {
    val Chip: Dp = 14.dp       // Small buttons and chips: 12–16 dp
    val StandardCard: Dp = 20.dp // Standard cards: 18–22 dp
    val HeroCard: Dp = 26.dp   // Hero cards: 24–28 dp
    val FloatingNav: Dp = 32.dp // Floating navigation: pill-shaped or 28–32 dp
}

@Immutable
data class LiquidGlassTokens(
    val isDark: Boolean,
    val glassFillBrush: Brush,
    val glassStrongFillBrush: Brush,
    val glassBorderBrush: Brush,
    val decorativeGold: Color,
    val deepPlumAccent: Color,
    val festivalVermilion: Color,
    val saffronHighlight: Color,
    val solidCardContainer: Color,
    val solidElevatedContainer: Color,
    val auspiciousGreen: Color,
    val onGlassPrimary: Color,
    val onGlassSecondary: Color
)

val LocalLiquidGlassTokens = staticCompositionLocalOf {
    LiquidGlassTokens(
        isDark = false,
        glassFillBrush = Brush.verticalGradient(listOf(LightGlassSurfaceTop, LightGlassSurfaceBottom)),
        glassStrongFillBrush = Brush.verticalGradient(listOf(Color(0xFCFFFFFF), Color(0xF7FFF9F1))),
        glassBorderBrush = Brush.linearGradient(listOf(LightGlassBorderHighlight, LightGlassBorderSubtle)),
        decorativeGold = AntiqueGold,
        deepPlumAccent = DeepPlum,
        festivalVermilion = VermilionAccent,
        saffronHighlight = SaffronAccent,
        solidCardContainer = PureWhite,
        solidElevatedContainer = WarmSand,
        auspiciousGreen = ForestGreen,
        onGlassPrimary = CharcoalPrimary,
        onGlassSecondary = MutedBrownSecondary
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DiyaAmber,
    onPrimary = CharcoalPrimary,
    primaryContainer = ElevatedDarkVariant,
    onPrimaryContainer = DiyaAmber,
    secondary = SindoorCoral,
    onSecondary = PureWhite,
    secondaryContainer = ElevatedDarkVariant,
    onSecondaryContainer = BrightEveningText,
    tertiary = EmeraldLeaf,
    onTertiary = CharcoalPrimary,
    tertiaryContainer = ElevatedDarkVariant,
    onTertiaryContainer = EmeraldLeaf,
    background = IncenseNightBackground,
    onBackground = BrightEveningText,
    surface = WarmCharcoalSurface,
    onSurface = BrightEveningText,
    surfaceVariant = ElevatedDarkVariant,
    onSurfaceVariant = MutedEveningText,
    outline = DarkBorderOutline,
    outlineVariant = DarkBorderOutline.copy(alpha = 0.65f)
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronAccent,
    onPrimary = PureWhite,
    primaryContainer = WarmSand,
    onPrimaryContainer = CharcoalPrimary,
    secondary = VermilionAccent,
    onSecondary = PureWhite,
    secondaryContainer = WarmSand,
    onSecondaryContainer = CharcoalPrimary,
    tertiary = ForestGreen,
    onTertiary = PureWhite,
    tertiaryContainer = WarmSand,
    onTertiaryContainer = ForestGreen,
    background = SacredIvory,
    onBackground = CharcoalPrimary,
    surface = PureWhite,
    onSurface = CharcoalPrimary,
    surfaceVariant = WarmSand,
    onSurfaceVariant = MutedBrownSecondary,
    outline = BorderOutline,
    outlineVariant = BorderOutline.copy(alpha = 0.90f)
)

@Composable
fun UtsavSetuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: com.example.data.preferences.AppLanguage = com.example.data.preferences.AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val utsavStrings = stringsForLanguage(language)
    java.util.Locale.setDefault(java.util.Locale.forLanguageTag(language.code))

    val glassTokens = if (darkTheme) {
        LiquidGlassTokens(
            isDark = true,
            glassFillBrush = Brush.verticalGradient(
                listOf(DarkGlassSurfaceTop, DarkGlassSurfaceBottom)
            ),
            glassStrongFillBrush = Brush.verticalGradient(
                listOf(Color(0xFA30243B), Color(0xF5211929))
            ),
            glassBorderBrush = Brush.linearGradient(
                listOf(DarkGlassBorderHighlight, DarkGlassBorderSubtle)
            ),
            decorativeGold = SubtleAmberAccent,
            deepPlumAccent = Color(0xFFE2CCF0),
            festivalVermilion = SindoorCoral,
            saffronHighlight = DiyaAmber,
            solidCardContainer = WarmCharcoalSurface,
            solidElevatedContainer = ElevatedDarkVariant,
            auspiciousGreen = EmeraldLeaf,
            onGlassPrimary = BrightEveningText,
            onGlassSecondary = MutedEveningText
        )
    } else {
        LiquidGlassTokens(
            isDark = false,
            glassFillBrush = Brush.verticalGradient(
                listOf(LightGlassSurfaceTop, LightGlassSurfaceBottom)
            ),
            glassStrongFillBrush = Brush.verticalGradient(
                listOf(Color(0xFCFFFFFF), Color(0xF7FFF9F1))
            ),
            glassBorderBrush = Brush.linearGradient(
                listOf(LightGlassBorderHighlight, LightGlassBorderSubtle)
            ),
            decorativeGold = AntiqueGold,
            deepPlumAccent = DeepPlum,
            festivalVermilion = VermilionAccent,
            saffronHighlight = SaffronAccent,
            solidCardContainer = PureWhite,
            solidElevatedContainer = WarmSand,
            auspiciousGreen = ForestGreen,
            onGlassPrimary = CharcoalPrimary,
            onGlassSecondary = MutedBrownSecondary
        )
    }

    CompositionLocalProvider(
        LocalLiquidGlassTokens provides glassTokens,
        LocalUtsavStrings provides utsavStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
