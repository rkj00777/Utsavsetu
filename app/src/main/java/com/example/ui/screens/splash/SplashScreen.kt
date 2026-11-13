package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.UtsavDeepBurgundy
import com.example.ui.theme.UtsavIvory
import com.example.ui.theme.UtsavLightGold
import com.example.ui.theme.UtsavMetallicGold
import kotlinx.coroutines.delay

/**
 * Official UtsavSetu Brand Splash Screen:
 * - Deep Burgundy (#520019) background
 * - Centrally displayed official UtsavSetu master crest & logo
 * - Restrained gold breathing glow and subtle scale transition
 * - Ultra-fast & elegant: transitions in < 1s without blocking navigation
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnim = true
        delay(800) // Fast, elegant launch experience
        onSplashComplete()
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (startAnim) 1.0f else 0.92f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "pulse_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (startAnim) 0.50f else 0.15f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onSplashComplete() }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6B0B26), // Luminous center burgundy
                        UtsavDeepBurgundy, // #520019 core
                        Color(0xFF2E0010)  // Dark maroon vignetted edges
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Master Logo with breathing gold aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(pulseScale)
                    .size(240.dp)
            ) {
                // Subtle radial gold aura
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    UtsavLightGold.copy(alpha = glowAlpha),
                                    UtsavMetallicGold.copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Master Artwork
                Image(
                    painter = painterResource(id = R.drawable.ic_utsavsetu_master_logo),
                    contentDescription = "UtsavSetu Official Brand Identity",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(UtsavLightGold, UtsavMetallicGold, Color(0xFF996B1F))
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .testTag("splash_master_logo")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Brand Tagline
            Text(
                text = "वैदिक पंचांग • उत्सव • मुहूर्त • पूजा विधि",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = UtsavIvory.copy(alpha = 0.90f),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "AUTHENTIC INDIAN FESTIVE CALENDAR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = UtsavLightGold.copy(alpha = 0.85f),
                letterSpacing = 2.0.sp
            )
        }
    }
}
