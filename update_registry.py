import json

with open("app/src/main/assets/festivals/festival_image_manifest.json") as f:
    manifest = json.load(f)

content = """package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalLiquidGlassTokens

/**
 * Central Photographic Drawable Registry for UtsavSetu "Living India" UI.
 * Maps 100% of canonical festival IDs to high-quality local photographic vector assets.
 */
object FestivalPhotographicRegistry {
    @DrawableRes
    fun getDrawableForFestival(festivalId: String?): Int {
        if (festivalId == null) return R.drawable.img_cultural_fallback
        return when (festivalId) {
"""

for entry in manifest:
    fid = entry["canonicalFestivalId"]
    dres = entry["drawableResName"]
    content += f'            "{fid}" -> R.drawable.{dres}\n'

content += """            else -> R.drawable.img_cultural_fallback
        }
    }
}

/**
 * Selective Liquid Glass surface used exclusively for:
 * - Today screen's Tithi Hero card
 * - Floating bottom navigation
 * - Festival countdown overlays
 * - Quick-action panels
 * - Small floating controls over festival artwork
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    strongFill: Boolean = false,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_press_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (glassTokens.isDark) 0.45f else 0.12f),
                spotColor = glassTokens.decorativeGold.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(brush = if (strongFill) glassTokens.glassStrongFillBrush else glassTokens.glassFillBrush)
            .border(width = 1.dp, brush = glassTokens.glassBorderBrush, shape = shape)
            .then(clickableModifier),
        content = content
    )
}

/**
 * Photographic Indian Luxury Festival Artwork Banner.
 * Displays crisp local photographic assets mapped to festival IDs with cinematic dark scrim overlays.
 */
@Composable
fun FestivalArtworkBanner(
    festivalId: String?,
    modifier: Modifier = Modifier,
    isHeroBackground: Boolean = false
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val drawableRes = remember(festivalId) {
        FestivalPhotographicRegistry.getDrawableForFestival(festivalId)
    }

    Box(modifier = modifier) {
        // 1. High-resolution photographic asset background
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Cinematic luxury gradient scrim overlays for perfect legibility
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dark vignette top-to-bottom
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = if (isHeroBackground) 0.65f else 0.50f)
                    )
                )
            )

            // Warm golden bottom border highlight
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFFFFD700).copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, size.height - 1.dp.toPx()),
                end = Offset(size.width, size.height - 1.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/components/FestivalArtwork.kt", "w") as out:
    out.write(content)

print("Updated FestivalArtwork.kt successfully.")
