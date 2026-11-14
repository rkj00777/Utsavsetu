package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.example.data.preferences.AppLanguage
import com.example.domain.FestivalRegionalNames
import com.example.domain.FestivalShastraKnowledge
import com.example.domain.model.FestivalOccurrence
import com.example.domain.model.SamagriItem
import com.example.domain.model.VidhiStep
import com.example.panchang.PanchangDay
import com.example.panchang.RegionalTradition
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Reusable Animated Collapsible Accordion / Dropdown Card for progressive disclosure across screens.
 * Uses smooth spring-backed vertical expansion, fade transitions, and a rotating chevron icon.
 */
@Composable
fun AnimatedCollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Default.AutoAwesome,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    initiallyExpanded: Boolean = false,
    badgeText: String? = null,
    testTagName: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dropdown_chevron_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 4.dp else 2.5.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (glassTokens.isDark) Color(0x55000000) else Color(0x2238271A),
                spotColor = if (glassTokens.isDark) Color(0x33D9A441) else Color(0x2B453322)
            )
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (expanded) glassTokens.decorativeGold.copy(alpha = 0.80f)
            else (if (glassTokens.isDark) Color(0x40D9A441) else Color(0xFFD6C3AA))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 3.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .defaultMinSize(minHeight = 52.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (!badgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = glassTokens.decorativeGold.copy(alpha = 0.16f),
                        modifier = Modifier.widthIn(max = 110.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun TraditionChip(
    tradition: RegionalTradition,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else glassTokens.solidElevatedContainer,
        label = "tradition_chip_bg"
    )

    Surface(
        onClick = onToggle,
        modifier = modifier
            .testTag("tradition_chip_${tradition.id}")
            .defaultMinSize(minHeight = 42.dp),
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) glassTokens.decorativeGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = UtsavLocalizationEngine.localizeTraditionName(tradition, strings.language),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TithiBadge(
    tithiName: String,
    pakshaName: String,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BrightnessMedium,
                contentDescription = "Tithi Phase",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$tithiName • $pakshaName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MuhuratChip(
    timeText: String,
    modifier: Modifier = Modifier,
    isAuspicious: Boolean = true
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val bgColor = if (isAuspicious) {
        glassTokens.auspiciousGreen.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val textColor = if (isAuspicious) {
        glassTokens.auspiciousGreen
    } else {
        MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAuspicious) Icons.Default.AutoAwesome else Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * SOLID SURFACE Panchang Dashboard Card with Animated Dropdown for Secondary Panchang Limbs.
 * Replaces the squished 4-column row with a balanced 2x2 grid inside an animated dropdown so no box
 * ever stretches vertically with empty space.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PanchangStrip(
    panchang: PanchangDay,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    var showFullLimbs by rememberSaveable { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (showFullLimbs) 180f else 0f,
        label = "panchang_strip_chevron"
    )

    val localizedCity = UtsavLocalizationEngine.localizeCityName(panchang.city, strings.language)
    val localizedWeekday = UtsavLocalizationEngine.formatDate(panchang.date, "EEEE", strings.language)
    val localizedTithi = UtsavLocalizationEngine.localizeTithiName(panchang.tithi.name, strings.language)
    val localizedPaksha = UtsavLocalizationEngine.localizePakshaName(panchang.tithi.paksha, strings.language)
    val localizedAmanta = UtsavLocalizationEngine.localizeMonthName(panchang.amantaMonth, strings.language)
    val localizedPurnimanta = UtsavLocalizationEngine.localizeMonthName(panchang.purnimantaMonth, strings.language)
    val localizedNakshatra = UtsavLocalizationEngine.localizeNakshatraName(panchang.nakshatra.name, strings.language)
    val localizedYoga = UtsavLocalizationEngine.localizeYogaName(panchang.yoga.name, strings.language)
    val localizedKarana = UtsavLocalizationEngine.localizeKaranaName(panchang.karana.name, strings.language)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("panchang_strip"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = glassTokens.decorativeGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${strings.dailyPanchangHeader} • $localizedCity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedWeekday,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tithi, Paksha, End Time & Lunar Month FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tithiEndSuffix = panchang.tithi.endTime?.let { " ($it)" } ?: ""
                TithiBadge(
                    tithiName = "$localizedTithi$tithiEndSuffix",
                    pakshaName = localizedPaksha
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = glassTokens.solidElevatedContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    val baseLunar = if (panchang.amantaMonth == panchang.purnimantaMonth) {
                        localizedAmanta
                    } else {
                        "$localizedAmanta / $localizedPurnimanta"
                    }
                    val monthBadgeText = panchang.regionalSolarMonth?.let {
                        "$baseLunar · ${it.substringBefore(" (")}"
                    } ?: baseLunar
                    Text(
                        text = monthBadgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Solar & Muhurat Timings Row (3 balanced columns, single-line labels)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PanchangMetricBox(
                    label = strings.sunriseLabel,
                    value = panchang.formattedSunrise(),
                    icon = Icons.Default.WbSunny,
                    accentColor = MarigoldSaffron,
                    containerColor = glassTokens.solidElevatedContainer,
                    modifier = Modifier.weight(1f)
                )
                PanchangMetricBox(
                    label = strings.auspiciousWindow,
                    value = panchang.abhijitMuhurat?.format() ?: "11:45 - 12:33",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = glassTokens.auspiciousGreen,
                    containerColor = glassTokens.solidElevatedContainer,
                    modifier = Modifier.weight(1f)
                )
                PanchangMetricBox(
                    label = strings.sunsetLabel,
                    value = panchang.formattedSunset(),
                    icon = Icons.Default.NightsStay,
                    accentColor = MaterialTheme.colorScheme.primary,
                    containerColor = glassTokens.solidElevatedContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smooth Animated Dropdown Toggle for Nakshatra, Yoga, Karana, Rahu Kaal & Timeline
            Surface(
                onClick = { showFullLimbs = !showFullLimbs },
                shape = RoundedCornerShape(12.dp),
                color = glassTokens.solidElevatedContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showFullLimbs) {
                            strings.hideDetailsLabel
                        } else {
                            "${strings.nakshatraLabel}: $localizedNakshatra • ${strings.rahuKaalAvoid}: ${panchang.rahuKaal.format()}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = showFullLimbs,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    DailyPanchangTimeline(panchang = panchang)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Balanced 2x2 Grid so no column ever stretches into a narrow vertical tower
                    val nakshatraValue = panchang.nakshatra.endTime?.let {
                        "$localizedNakshatra ($it)"
                    } ?: localizedNakshatra

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PanchangSubMetric(
                            title = strings.nakshatraLabel,
                            value = nakshatraValue,
                            modifier = Modifier.weight(1f)
                        )
                        PanchangSubMetric(
                            title = strings.rahuKaalAvoid,
                            value = panchang.rahuKaal.format(),
                            valueColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PanchangSubMetric(
                            title = strings.yogaLabel,
                            value = localizedYoga,
                            modifier = Modifier.weight(1f)
                        )
                        PanchangSubMetric(
                            title = strings.karanaLabel,
                            value = localizedKarana,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPanchangTimeline(panchang: PanchangDay) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val sunriseMin = (panchang.sunrise.hour * 60 + panchang.sunrise.minute).toFloat()
    val sunsetMin = (panchang.sunset.hour * 60 + panchang.sunset.minute).toFloat()
    val daySpan = (sunsetMin - sunriseMin).coerceAtLeast(600f)

    val abhijitStartFrac = ((panchang.abhijitMuhurat?.startMinutes?.toFloat() ?: (sunriseMin + daySpan * 0.46f)) - sunriseMin) / daySpan
    val abhijitEndFrac = ((panchang.abhijitMuhurat?.endMinutes?.toFloat() ?: (sunriseMin + daySpan * 0.54f)) - sunriseMin) / daySpan
    val rahuStartFrac = (panchang.rahuKaal.startMinutes.toFloat() - sunriseMin) / daySpan
    val rahuEndFrac = (panchang.rahuKaal.endMinutes.toFloat() - sunriseMin) / daySpan

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val auspiciousColor = glassTokens.auspiciousGreen
    val rahuColor = MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
    val goldColor = glassTokens.decorativeGold

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.sunriseLabel} → ${strings.sunsetLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(auspiciousColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.abhijitMuhuratLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(rahuColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.rahuKaalAvoid.substringBefore(" ("),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            drawRoundRect(
                color = trackColor,
                size = size,
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            val aStart = (abhijitStartFrac.coerceIn(0f, 1f)) * size.width
            val aWidth = ((abhijitEndFrac - abhijitStartFrac).coerceAtLeast(0.06f)) * size.width
            drawRoundRect(
                color = auspiciousColor,
                topLeft = Offset(aStart, 0f),
                size = Size(aWidth, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            val rStart = (rahuStartFrac.coerceIn(0f, 1f)) * size.width
            val rWidth = ((rahuEndFrac - rahuStartFrac).coerceAtLeast(0.08f)) * size.width
            drawRoundRect(
                color = rahuColor,
                topLeft = Offset(rStart, 0f),
                size = Size(rWidth, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            drawCircle(
                color = goldColor,
                radius = size.height * 0.45f,
                center = Offset(size.height * 0.5f, size.height * 0.5f)
            )
            drawCircle(
                color = goldColor,
                radius = size.height * 0.45f,
                center = Offset(size.width - size.height * 0.5f, size.height * 0.5f)
            )
        }
    }
}

@Composable
private fun PanchangMetricBox(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PanchangSubMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Reusable controller and Modifier for tactile, cushioned "Soft Press" on festival cards.
 * When touched or tapped, the card gently sinks down (scales to 0.962f and shifts down by 5.dp)
 * with a warm spring response, then rebounds smoothly as it opens the festival details screen.
 */
@Stable
class SoftPressCardController(
    val interactionSource: MutableInteractionSource,
    private val scope: CoroutineScope
) {
    var isTapDipping by mutableStateOf(false)
        private set

    fun triggerSoftClick(onClick: () -> Unit) {
        if (isTapDipping) return
        scope.launch {
            isTapDipping = true
            delay(110L)
            isTapDipping = false
            delay(45L)
            onClick()
        }
    }
}

@Composable
fun rememberSoftPressController(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): SoftPressCardController {
    val scope = rememberCoroutineScope()
    return remember(interactionSource, scope) {
        SoftPressCardController(interactionSource, scope)
    }
}

@Composable
fun Modifier.softPressCardEffect(
    controller: SoftPressCardController,
    pressedScale: Float = 0.962f,
    pressedTranslationYDp: Float = 5f
): Modifier {
    val isPressed by controller.interactionSource.collectIsPressedAsState()
    val active = isPressed || controller.isTapDipping
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { pressedTranslationYDp.dp.toPx() }

    val scale by animateFloatAsState(
        targetValue = if (active) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "soft_press_scale"
    )
    val translationY by animateFloatAsState(
        targetValue = if (active) maxOffsetPx else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "soft_press_translation_y"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.translationY = translationY
    }
}

/**
 * Clean, Scannable Festival Card with Progressive Disclosure:
 * - Primary view shows Artwork Banner, Localized Date & Countdown, Localized Title, Muhurat & Tithi chips, and 2-line significance.
 * - Regional names & ritual summary are tucked inside a smooth animated toggle so lists never look cluttered.
 * - Includes cushioned soft-press downward touch animation before opening festival details.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FestivalCard(
    occurrence: FestivalOccurrence,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    matchedRegionalName: String? = null,
    onShareClick: (() -> Unit)? = null,
    referenceDate: LocalDate = LocalDate.now()
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val festival = occurrence.festival
    val localizedName = UtsavLocalizationEngine.localizeFestivalName(festival, strings.language)
    val localizedSignificance = UtsavLocalizationEngine.localizeSignificance(festival, strings.language)
    val regionalSummary = UtsavLocalizationEngine.localizeRegionalSummary(festival, strings.language)
    val dateStr = UtsavLocalizationEngine.formatDate(occurrence.date, "EEE, d MMM yyyy", strings.language)
    val daysDiff = ChronoUnit.DAYS.between(referenceDate, occurrence.date)
    val countdownLabel = when {
        daysDiff == 0L -> strings.todayBadge
        daysDiff == 1L -> strings.tomorrowBadge
        daysDiff > 1L -> String.format(UtsavLocalizationEngine.localeForLanguage(strings.language), strings.inDaysFormat, daysDiff)
        else -> dateStr
    }
    var showRegionalNames by rememberSaveable(festival.id) { mutableStateOf(false) }
    val softPress = rememberSoftPressController()
    val isHeld by softPress.interactionSource.collectIsPressedAsState()
    val isPressedVisual = isHeld || softPress.isTapDipping

    Card(
        onClick = { softPress.triggerSoftClick(onClick) },
        interactionSource = softPress.interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .softPressCardEffect(softPress)
            .shadow(
                elevation = if (isPressedVisual) 1.5.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
            )
            .testTag("festival_card_${festival.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = if (isPressedVisual) 1.8.dp else 1.2.dp,
            color = if (isPressedVisual) glassTokens.decorativeGold else (if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.45f) else Color(0xFFD6C3AA))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        Column {
            // Top Artwork Banner (92.dp for balanced proportion)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                FestivalArtworkBanner(
                    festivalId = festival.id,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Liquid Glass Date Pill (Top Left)
                LiquidGlassSurface(
                    cornerRadius = 50.dp,
                    elevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = glassTokens.onGlassPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = glassTokens.onGlassPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Floating Liquid Glass Countdown Overlay (Top Right)
                if (daysDiff >= 0L) {
                    LiquidGlassSurface(
                        cornerRadius = 50.dp,
                        strongFill = true,
                        elevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = countdownLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Native Script Subtitle Pill at bottom-left of artwork
                Surface(
                    color = Color(0xD9161219),
                    shape = RoundedCornerShape(topEnd = 12.dp),
                    border = BorderStroke(0.5.dp, glassTokens.decorativeGold.copy(alpha = 0.45f)),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = festival.nameNative,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Solid Surface Scannable Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = localizedName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (matchedRegionalName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = glassTokens.decorativeGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "${strings.alsoKnownAsLabel}: $matchedRegionalName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // FlowRow ensures MuhuratChip & Tithi badge wrap gracefully on any phone width
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MuhuratChip(timeText = "${strings.auspiciousWindow}: ${occurrence.muhuratWindowFormatted}")
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = UtsavLocalizationEngine.localizeTithiBadge(
                                FestivalShastraKnowledge.formatCompactTithiBadge(festival),
                                strings.language
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = localizedSignificance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Animated Dropdown for Regional Names if available
                AnimatedVisibility(
                    visible = showRegionalNames && regionalSummary.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = glassTokens.decorativeGold.copy(alpha = 0.10f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "${strings.alsoKnownAsLabel}: $regionalSummary",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = regionalSummary.isNotBlank()) {
                                showRegionalNames = !showRegionalNames
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = glassTokens.auspiciousGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${festival.vidhiSteps.size} ${strings.vidhiTabPrefix} • ${festival.samagriItems.size} ${strings.samagriTabPrefix}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (regionalSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showRegionalNames) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = strings.alsoKnownAsLabel,
                                tint = glassTokens.decorativeGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onShareClick != null) {
                            IconButton(
                                onClick = onShareClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "${strings.shareGreeting} ${festival.nameEnglish}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = strings.exploreVidhiButton,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * SOLID SURFACE Interactive Samagri Checklist Row with spring completion animation.
 */
@Composable
fun ChecklistRow(
    item: SamagriItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val containerColor by animateColorAsState(
        targetValue = if (isChecked) {
            glassTokens.auspiciousGreen.copy(alpha = 0.12f)
        } else {
            glassTokens.solidCardContainer
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checklist_row_bg"
    )

    Surface(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .testTag("checklist_row_${item.id}")
            .defaultMinSize(minHeight = 54.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isChecked) glassTokens.auspiciousGreen.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = glassTokens.auspiciousGreen,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val localizedSamagri = UtsavLocalizationEngine.localizeSamagriItem(item, strings.language)
                Text(
                    text = localizedSamagri.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isChecked) FontWeight.Medium else FontWeight.SemiBold,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
                if (localizedSamagri.quantity != null || localizedSamagri.notes != null) {
                    val detail = listOfNotNull(localizedSamagri.quantity, localizedSamagri.notes).joinToString(" • ")
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isChecked) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = glassTokens.auspiciousGreen.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassTokens.auspiciousGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * SOLID SURFACE Puja Vidhi Step Card with Animated Dropdown for Step Mantra & Samagri.
 * Prevents walls of text by showing the step title and action clearly, with a smooth dropdown
 * to reveal the Sanskrit/Transliteration Mantra and required Samagri for that step.
 */
@Composable
fun VidhiStepCard(
    step: VidhiStep,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    requiredItems: List<SamagriItem> = emptyList(),
    stepMantra: com.example.domain.FestivalPujaMantra? = null
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    var showStepExtras by rememberSaveable(step.stepNumber, isHighlighted) {
        mutableStateOf(isHighlighted)
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (showStepExtras) 180f else 0f,
        label = "vidhi_step_chevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighlighted) 4.dp else 2.5.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (glassTokens.isDark) Color(0x55000000) else Color(0x2238271A),
                spotColor = if (glassTokens.isDark) Color(0x33D9A441) else Color(0x2B453322)
            )
            .testTag("vidhi_step_card_${step.stepNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.2.dp,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else (if (glassTokens.isDark) Color(0x40D9A441) else Color(0xFFD6C3AA))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 3.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(1.dp, glassTokens.decorativeGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.stepNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (step.durationMinutes != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = glassTokens.decorativeGold.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "${step.durationMinutes} ${strings.minutesShortLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            if (stepMantra != null || requiredItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = { showStepExtras = !showStepExtras },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = glassTokens.decorativeGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.stepMantraAndSamagriLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showStepExtras,
                    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        if (stepMantra != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vidhi_step_mantra_${step.stepNumber}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stepMantra.ritualStage,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = glassTokens.decorativeGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stepMantra.chantCountRecommendation,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stepMantra.sanskrit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (strings.language == AppLanguage.ENGLISH) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stepMantra.transliteration,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.meaningPrefix}: ${stepMantra.meaning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (requiredItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${strings.samagriTabPrefix} (${step.stepNumber}): " +
                                    requiredItems.joinToString(" • ") { item ->
                                        val locItem = UtsavLocalizationEngine.localizeSamagriItem(item, strings.language)
                                        locItem.name + (locItem.quantity?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "")
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = glassTokens.decorativeGold.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.verifiedMeeusBadge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.SearchOff,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(16.dp))
            actionButton()
        }
    }
}

/**
 * Sacred Indian Luxury Animated Splash Screen ("UtsavSetu").
 * Features:
 * - Deep Kumkum-Vermilion, Saffron & Temple Night radial gradient backdrop with ornate golden arch frame.
 * - Dual counter-rotating 24-ray Vedic Surya halo & 16-petal Sacred Lotus (Padma) geometry in Canvas.
 * - Rising golden Diya prana sparks and breathing sacred aura.
 * - Spring-animated entrance of the UtsavSetu master emblem (`R.drawable.ic_utsavsetu_master_logo`) and Sanskrit invocation.
 * - Dismisses immediately on tap (with `testTag("splash_screen")`) or automatically after completion.
 */
@Composable
fun UtsavAnimatedSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startEntrance by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startEntrance = true
        delay(2100L)
        onSplashComplete()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_logo_scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (startEntrance) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_content_alpha"
    )
    val textSlideY by animateFloatAsState(
        targetValue = if (startEntrance) 0f else 28f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "splash_text_slide"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sacred_splash_mandala")
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mandala_outer_rot"
    )
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mandala_inner_rot"
    )
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mandala_aura_pulse"
    )
    val sparkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diya_sparks_progress"
    )

    val templeGold = Color(0xFFE5B84B)
    val brightMarigold = Color(0xFFFF9933)
    val sacredKumkum = Color(0xFF8C1D24)
    val deepSanctum = Color(0xFF17070C)
    val warmIvory = Color(0xFFFFF8EC)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF59141C),
                        Color(0xFF2B0912),
                        deepSanctum
                    )
                )
            )
            .testTag("splash_screen")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashComplete
            ),
        contentAlignment = Alignment.Center
    ) {
        // Sacred Mandala, Surya Rays & Floating Diya Sparks Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - size.height * 0.05f)
            val baseRadius = minOf(size.width, size.height) * 0.34f

            // 1. Outer Temple Border Frame
            val frameInset = 18.dp.toPx()
            drawRoundRect(
                color = templeGold.copy(alpha = 0.28f),
                topLeft = Offset(frameInset, frameInset),
                size = Size(size.width - frameInset * 2, size.height - frameInset * 2),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )
            val innerFrameInset = 24.dp.toPx()
            drawRoundRect(
                color = templeGold.copy(alpha = 0.14f),
                topLeft = Offset(innerFrameInset, innerFrameInset),
                size = Size(size.width - innerFrameInset * 2, size.height - innerFrameInset * 2),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // 2. Breathing Saffron-Gold Radial Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        brightMarigold.copy(alpha = 0.32f * auraPulse),
                        sacredKumkum.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.45f * auraPulse
                ),
                radius = baseRadius * 1.45f * auraPulse,
                center = center
            )

            // 3. Outer Rotating 24-Ray Vedic Surya Chakra
            rotate(degrees = outerRotation, pivot = center) {
                val rayCount = 24
                for (i in 0 until rayCount) {
                    val angleRad = (i * (360f / rayCount)) * (PI.toFloat() / 180f)
                    val isMajor = i % 2 == 0
                    val innerR = baseRadius * 0.62f
                    val outerR = if (isMajor) baseRadius * 1.12f * auraPulse else baseRadius * 0.94f
                    val start = Offset(
                        center.x + cos(angleRad) * innerR,
                        center.y + sin(angleRad) * innerR
                    )
                    val end = Offset(
                        center.x + cos(angleRad) * outerR,
                        center.y + sin(angleRad) * outerR
                    )
                    drawLine(
                        color = templeGold.copy(alpha = if (isMajor) 0.42f else 0.22f),
                        start = start,
                        end = end,
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Auspicious Bindu dot at ray tip
                    drawCircle(
                        color = templeGold.copy(alpha = if (isMajor) 0.65f else 0.35f),
                        radius = if (isMajor) 3.dp.toPx() else 1.8.dp.toPx(),
                        center = end
                    )
                }

                // Concentric filigree rings
                drawCircle(
                    color = templeGold.copy(alpha = 0.32f),
                    radius = baseRadius * 0.98f,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = brightMarigold.copy(alpha = 0.24f),
                    radius = baseRadius * 0.80f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 4. Counter-Rotating 16-Petal Sacred Lotus (Padma Mandala)
            rotate(degrees = innerRotation, pivot = center) {
                val petalCount = 16
                val petalStep = 360f / petalCount
                for (i in 0 until petalCount) {
                    rotate(degrees = i * petalStep, pivot = center) {
                        val tipY = center.y - baseRadius * 0.78f
                        val baseY = center.y - baseRadius * 0.34f
                        val ctrlSpread = baseRadius * 0.16f
                        val petalPath = Path().apply {
                            moveTo(center.x, baseY)
                            quadraticTo(
                                center.x - ctrlSpread,
                                (baseY + tipY) * 0.5f,
                                center.x,
                                tipY
                            )
                            quadraticTo(
                                center.x + ctrlSpread,
                                (baseY + tipY) * 0.5f,
                                center.x,
                                baseY
                            )
                            close()
                        }
                        drawPath(
                            path = petalPath,
                            color = brightMarigold.copy(alpha = 0.14f)
                        )
                        drawPath(
                            path = petalPath,
                            color = templeGold.copy(alpha = 0.48f),
                            style = Stroke(width = 1.3.dp.toPx())
                        )
                    }
                }
            }

            // 5. Rising Golden Diya Prana Sparks
            val sparkCount = 14
            for (i in 0 until sparkCount) {
                val phase = (sparkProgress + i * (1f / sparkCount)) % 1f
                val horizontalOffset = sin((i * 47 + phase * 6.28f).toDouble()).toFloat() * (baseRadius * 1.1f)
                val sparkX = center.x + horizontalOffset
                val sparkY = size.height * (0.85f - phase * 0.72f)
                val sparkAlpha = sin(phase * PI).toFloat().coerceIn(0f, 1f) * 0.55f
                drawCircle(
                    color = templeGold.copy(alpha = sparkAlpha),
                    radius = (1.8f + (i % 3) * 1.1f).dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }

        // Foreground Emblem, Sanskrit Shloka & Brand Identity
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Auspicious Sanskrit Invocation Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0x40000000),
                border = BorderStroke(1.dp, templeGold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = templeGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "॥ ॐ शुभं भवतु · मंगलं भवतु ॥",
                        style = MaterialTheme.typography.labelMedium,
                        color = templeGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = templeGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Sacred Medallion Holding the UtsavSetu Master Logo
            Box(
                modifier = Modifier
                    .size(138.dp)
                    .graphicsLayer {
                        scaleX = logoScale * auraPulse.coerceIn(0.97f, 1.03f)
                        scaleY = logoScale * auraPulse.coerceIn(0.97f, 1.03f)
                    }
                    .shadow(
                        elevation = 18.dp,
                        shape = CircleShape,
                        ambientColor = brightMarigold,
                        spotColor = templeGold
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4A1219),
                                Color(0xFF23080E)
                            )
                        )
                    )
                    .border(2.2.dp, templeGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_utsavsetu_master_logo),
                    contentDescription = "UtsavSetu Sacred Emblem",
                    modifier = Modifier
                        .size(114.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Name & Sacred Subtitle with subtle upward slide
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = textSlideY
                }
            ) {
                Text(
                    text = "UtsavSetu",
                    fontFamily = FontFamily.Serif,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = warmIvory,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "उत्सवसेतु · सनातन पंचांग एवं पर्व सेतु",
                    style = MaterialTheme.typography.titleSmall,
                    color = templeGold,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Your Festivals · Your Traditions · Verified Drik Panchang",
                    style = MaterialTheme.typography.bodySmall,
                    color = warmIvory.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

