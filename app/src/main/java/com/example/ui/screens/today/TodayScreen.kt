package com.example.ui.screens.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.City
import com.example.panchang.LunarConvention
import com.example.panchang.PanchangDay
import com.example.panchang.PanchangEngine
import com.example.panchang.RegionalPanchangConvention
import com.example.ui.components.*
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.UtsavLocalizationEngine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Blueprint Section 5 (Screen 3): Today Dashboard.
 * Hierarchy:
 * 1. Header (Greeting + Date context on left; Notification & Search icons on right) + Compact City Row
 * 2. Signature Hero Festival Card (or Daily Panchang Card when no major festival is today)
 * 3. Daily Panchang Summary (4 compact solid cards: Tithi, Nakshatra, Sunrise, Sunset)
 * 4. Next Festival Card (with glass countdown overlay & Explore action)
 * 5. Quick Actions (Calendar, Puja Guide, Panchang, Reminders)
 * 6. Upcoming Festivals
 */
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToFestival: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCalendar: () -> Unit = {},
    onOpenPanchang: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current

    var cityMenuExpanded by remember { mutableStateOf(false) }
    var showReminderCentre by remember { mutableStateOf(false) }
    var shareTargetOccurrence by remember { mutableStateOf<FestivalOccurrence?>(null) }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("today_screen"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val panchang = state.panchang
    val prefs = state.userPreferences
    val primaryConvention = when (prefs?.hostCity?.panchangConvention) {
        RegionalPanchangConvention.PURNIMANTA_NORTH,
        RegionalPanchangConvention.ODIA_SOLAR -> LunarConvention.PURNIMANTA
        RegionalPanchangConvention.AMANTA_GUJARAT_MAHARASHTRA,
        RegionalPanchangConvention.AMANTA_SOUTH,
        RegionalPanchangConvention.TAMIL_SOLAR,
        RegionalPanchangConvention.MALAYALAM_SOLAR,
        RegionalPanchangConvention.BENGALI_SOLAR -> LunarConvention.AMANTA
        else -> prefs?.selectedTraditions?.firstOrNull()?.defaultConvention ?: LunarConvention.AMANTA
    }

    val todayFormatted = remember(state.todayDate, strings.language) {
        UtsavLocalizationEngine.formatDate(state.todayDate, "EEEE, d MMMM", strings.language)
    }

    // Dialogs for Share & Reminder Centre
    shareTargetOccurrence?.let { occ ->
        ShareFestivalDialog(
            festival = occ.festival,
            date = occ.date,
            muhuratWindow = occ.muhuratWindowFormatted,
            cityName = prefs?.hostCity?.name ?: City.DELHI.name,
            onDismiss = { shareTargetOccurrence = null }
        )
    }

    if (showReminderCentre && prefs != null) {
        ReminderCentreDialog(
            reminders = prefs.customReminders,
            dayBeforeEnabled = prefs.dayBeforeReminder,
            morningOfEnabled = prefs.morningOfReminder,
            muhuratStartEnabled = prefs.muhuratStartReminder,
            onDeleteReminder = { entry ->
                com.example.platform.notifications.ReminderScheduler.cancelCustomReminder(
                    context = context,
                    festivalId = entry.festivalId,
                    triggerType = entry.triggerType
                )
                viewModel.deleteCustomReminder(entry)
            },
            onUpdateGlobalRules = { d, m, s -> viewModel.updateGlobalReminderRules(d, m, s) },
            onDismiss = { showReminderCentre = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("today_screen"),
        contentPadding = PaddingValues(bottom = 128.dp)
    ) {
        // 1. Header and Compact City Row (Blueprint Section 5.2)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prefs?.preferredLanguage?.sampleGreeting ?: "नमस्ते",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notification / Reminder Centre Icon
                        LiquidGlassSurface(
                            cornerRadius = 22.dp,
                            strongFill = true,
                            elevation = 3.dp,
                            onClick = { showReminderCentre = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("today_reminders_icon_button")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Open reminders",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Search Button (preserves top_search_button testTag)
                        LiquidGlassSurface(
                            cornerRadius = 22.dp,
                            strongFill = true,
                            elevation = 3.dp,
                            onClick = onOpenSearch,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("top_search_button")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search festivals",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Interactive City Row
                Box {
                    val localizedCity = UtsavLocalizationEngine.localizeCityName(prefs?.hostCity?.name ?: City.DELHI.name, strings.language)
                    val localizedState = UtsavLocalizationEngine.localizeStateName(prefs?.hostCity?.state ?: City.DELHI.state, strings.language)
                    Surface(
                        onClick = { cityMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = glassTokens.solidElevatedContainer,
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("today_city_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$localizedCity, $localizedState",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change location",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (cityMenuExpanded) {
                        CitySelectionDialog(
                            currentCity = prefs?.hostCity ?: City.DELHI,
                            onCitySelected = { city ->
                                viewModel.selectCity(city)
                                cityMenuExpanded = false
                            },
                            onDismiss = { cityMenuExpanded = false }
                        )
                    }
                }
            }
        }

        // 2. Signature Hero Festival / Observance Card (Blueprint Section 5.3)
        if (panchang != null) {
            item {
                TodaySignatureHeroCard(
                    panchang = panchang,
                    convention = primaryConvention,
                    todaysFestivals = state.todaysFestivals,
                    nextFestival = state.nextFestival,
                    savedFestivalIds = prefs?.savedFestivalIds ?: emptySet(),
                    onNavigateToFestival = onNavigateToFestival,
                    onToggleSave = { viewModel.toggleSavedFestival(it) },
                    onShareFestival = { shareTargetOccurrence = it },
                    onSeeAllObservances = onOpenCalendar,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("today_hero_card")
                )
            }

            // 3. Daily Panchang Summary inside a Clean Animated Collapsible Dropdown Card
            item {
                val localizedTithi = UtsavLocalizationEngine.localizeTithiName(panchang.tithi.name, strings.language)
                val localizedNakshatra = UtsavLocalizationEngine.localizeNakshatraName(panchang.nakshatra.name, strings.language)
                val localizedAmanta = UtsavLocalizationEngine.localizeLunarMonth(panchang.amantaMonth, strings.language)
                val localizedPurnimanta = UtsavLocalizationEngine.localizeLunarMonth(panchang.purnimantaMonth, strings.language)

                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    AnimatedCollapsibleSection(
                        title = strings.todaysPanchang,
                        subtitle = "$localizedTithi • ${strings.sunriseLabel}: ${panchang.formattedSunrise()} • ${strings.sunsetLabel}: ${panchang.formattedSunset()}",
                        badgeText = localizedAmanta,
                        icon = Icons.Default.Explore,
                        iconTint = glassTokens.decorativeGold,
                        initiallyExpanded = false,
                        testTagName = "panchang_strip"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val monthLabel = buildString {
                                append("${strings.lunarMonthLabel}: $localizedAmanta / $localizedPurnimanta")
                                panchang.regionalSolarMonth?.let { append(" · $it") }
                            }
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = onOpenPanchang,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "${strings.tabPanchang} →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 1: Tithi & Nakshatra
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompactSolidPanchangCard(
                                title = strings.tithiLabel,
                                mainValue = localizedTithi,
                                supportingInfo = "${UtsavLocalizationEngine.localizePaksha(panchang.tithi.paksha.displayName.substringBefore(" "), strings.language)} · ${panchang.tithi.endTime ?: strings.fullDayLabel}",
                                icon = Icons.Default.Brightness3,
                                onClick = onOpenPanchang,
                                modifier = Modifier.weight(1f)
                            )
                            CompactSolidPanchangCard(
                                title = strings.nakshatraLabel,
                                mainValue = localizedNakshatra,
                                supportingInfo = panchang.nakshatra.endTime?.let { "${strings.untilLabel} $it" } ?: strings.fullDayLabel,
                                icon = Icons.Default.AutoAwesome,
                                onClick = onOpenPanchang,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2: Sunrise & Sunset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompactSolidPanchangCard(
                                title = strings.sunriseLabel,
                                mainValue = panchang.formattedSunrise(),
                                supportingInfo = "${strings.brahmaMuhuratLabel}: ${panchang.brahmaMuhurat.format()}",
                                icon = Icons.Default.WbSunny,
                                onClick = onOpenPanchang,
                                modifier = Modifier.weight(1f)
                            )
                            CompactSolidPanchangCard(
                                title = strings.sunsetLabel,
                                mainValue = panchang.formattedSunset(),
                                supportingInfo = "${strings.rahuKaalLabel}: ${panchang.rahuKaal.format()}",
                                icon = Icons.Default.WbTwilight,
                                onClick = onOpenPanchang,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Next Festival Card with Countdown Overlay (shown when today has its own festival)
        if (state.todaysFestivals.isNotEmpty()) {
            state.nextFestival?.let { nextFest ->
                item {
                    NextFestivalSpotlightCard(
                        occurrence = nextFest,
                        daysRemaining = state.daysUntilNextFestival,
                        cityName = UtsavLocalizationEngine.localizeCityName(prefs?.hostCity?.name ?: City.DELHI.name, strings.language),
                        onExplore = { onNavigateToFestival(nextFest.festival.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 5. Quick Actions: Calendar, Puja Guide, Panchang, Reminders (Blueprint Section 5.6)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = strings.quickActionsHeader.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTokens.decorativeGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                TodayQuickActionsBar(
                    onCalendarClick = onOpenCalendar,
                    onPujaGuideClick = {
                        state.nextFestival?.let { onNavigateToFestival(it.festival.id) } ?: onOpenSearch()
                    },
                    onPanchangClick = onOpenPanchang,
                    onRemindersClick = { showReminderCentre = true }
                )
            }
        }

        // 6. Upcoming Festivals Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.upcomingFestivalsHeader,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${strings.upcomingFestivalsSubtitlePrefix} ${UtsavLocalizationEngine.localizeCityName(prefs?.hostCity?.name ?: City.DELHI.name, strings.language)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onOpenSearch) {
                    Text(
                        text = strings.seeAllButton,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val listToShow = state.upcomingOccurrences.ifEmpty {
            state.nextSevenDaysOccurrences
        }

        itemsIndexed(
            items = listToShow,
            key = { _, it -> "${it.festival.id}_${it.date}" }
        ) { index, occurrence ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                FestivalCard(
                    occurrence = occurrence,
                    referenceDate = state.todayDate,
                    onClick = { onNavigateToFestival(occurrence.festival.id) },
                    onShareClick = { shareTargetOccurrence = occurrence }
                )
            }
            if (index == 0) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    AdMobNativeAdCard(
                        adUnitId = ADMOB_NATIVE_TEST_UNIT_ID,
                        showDismissButton = true
                    )
                }
            }
        }
    }
}

/**
 * Blueprint Section 5.3: Hero Festival Card.
 * Keeps festival title on a stable, high-contrast surface while using Liquid Glass for the
 * date badge and decorative motif. Handles Major Festival Today, Multiple Observances, and Ordinary Day states.
 */
@Composable
private fun TodaySignatureHeroCard(
    panchang: PanchangDay,
    convention: LunarConvention,
    todaysFestivals: List<FestivalOccurrence>,
    nextFestival: FestivalOccurrence?,
    savedFestivalIds: Set<String>,
    onNavigateToFestival: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onShareFestival: (FestivalOccurrence) -> Unit,
    onSeeAllObservances: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val primaryTodayFest = todaysFestivals.firstOrNull()
    val targetFestivalForCard = primaryTodayFest ?: nextFestival
    val artworkId = targetFestivalForCard?.festival?.id
    val heroSoftPress = rememberSoftPressController()

    Card(
        onClick = {
            if (targetFestivalForCard != null) {
                heroSoftPress.triggerSoftClick {
                    onNavigateToFestival(targetFestivalForCard.festival.id)
                }
            }
        },
        interactionSource = heroSoftPress.interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .softPressCardEffect(heroSoftPress)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.50f) else Color(0xFFD6C3AA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Artwork Header with Liquid Glass Date & Observance Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
            ) {
                FestivalArtworkBanner(
                    festivalId = artworkId,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Liquid Glass Date Badge
                LiquidGlassSurface(
                    cornerRadius = 14.dp,
                    strongFill = true,
                    elevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    val badgeStrings = LocalUtsavStrings.current
                    Text(
                        text = if (primaryTodayFest != null) badgeStrings.todaysFestivalBadge else badgeStrings.todaysObservanceBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Top-right Save & Share Floating Controls when a festival is active
                val targetOcc = primaryTodayFest ?: nextFestival
                if (targetOcc != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isSaved = savedFestivalIds.contains(targetOcc.festival.id)
                        LiquidGlassSurface(
                            cornerRadius = 20.dp,
                            strongFill = true,
                            elevation = 2.dp,
                            onClick = { onToggleSave(targetOcc.festival.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isSaved) "Remove from saved" else "Save festival",
                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        LiquidGlassSurface(
                            cornerRadius = 20.dp,
                            strongFill = true,
                            elevation = 2.dp,
                            onClick = { onShareFestival(targetOcc) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share festival",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Stable High-Contrast Solid Surface for Title & Primary Action
            val strings = LocalUtsavStrings.current
            Column(modifier = Modifier.padding(20.dp)) {
                if (primaryTodayFest != null) {
                    Text(
                        text = UtsavLocalizationEngine.localizeFestivalName(
                            festivalId = primaryTodayFest.festival.id,
                            englishName = primaryTodayFest.festival.nameEnglish,
                            nativeDevanagari = primaryTodayFest.festival.nameNative,
                            language = strings.language
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${primaryTodayFest.festival.nameNative} · ${strings.pujaMuhuratLabel}: ${primaryTodayFest.muhuratWindowFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = UtsavLocalizationEngine.localizeSignificance(
                            festivalId = primaryTodayFest.festival.id,
                            englishName = primaryTodayFest.festival.nameEnglish,
                            englishSignificance = primaryTodayFest.festival.significance,
                            language = strings.language
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (todaysFestivals.size > 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${strings.seeAllButton} (${todaysFestivals.size}) →",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSeeAllObservances() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onNavigateToFestival(primaryTodayFest.festival.id) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hero_view_festival_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.viewPujaGuideButton, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToFestival(primaryTodayFest.festival.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.samagriChecklistHeader, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { onShareFestival(primaryTodayFest) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.shareFestivalAction, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    // Calm Daily Panchang Observance State (when no major festival is today)
                    val monthName = UtsavLocalizationEngine.localizeLunarMonth(
                        panchang.getMonthNameForConvention(convention),
                        strings.language
                    )
                    val pakshaName = UtsavLocalizationEngine.localizePaksha(
                        panchang.tithi.paksha.displayName.substringBefore(" "),
                        strings.language
                    )
                    val tithiName = UtsavLocalizationEngine.localizeTithiName(
                        panchang.tithi.name,
                        strings.language
                    )
                    Text(
                        text = "$monthName · $pakshaName $tithiName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.nakshatraLabel}: ${UtsavLocalizationEngine.localizeNakshatraName(panchang.nakshatra.name, strings.language)} · ${strings.yogaLabel}: ${UtsavLocalizationEngine.localizeYogaName(panchang.yoga.name, strings.language)} · ${strings.abhijitMuhuratLabel}: ${panchang.abhijitMuhurat?.format() ?: "11:45 - 12:30"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    nextFestival?.let { upcoming ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = glassTokens.solidElevatedContainer,
                            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.upcomingFestivalsHeader,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = glassTokens.decorativeGold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val days = java.time.temporal.ChronoUnit.DAYS.between(PanchangEngine.localDateForCity(panchang.city), upcoming.date)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (days == 1L) strings.tomorrowBadge else String.format(UtsavLocalizationEngine.localeForLanguage(strings.language), strings.inDaysFormat, days),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = UtsavLocalizationEngine.localizeFestivalName(
                                        festivalId = upcoming.festival.id,
                                        englishName = upcoming.festival.nameEnglish,
                                        nativeDevanagari = upcoming.festival.nameNative,
                                        language = strings.language
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${upcoming.festival.nameNative} · ${UtsavLocalizationEngine.formatDate(upcoming.date, "EEEE, d MMMM yyyy", strings.language)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = UtsavLocalizationEngine.localizeSignificance(
                                        festivalId = upcoming.festival.id,
                                        englishName = upcoming.festival.nameEnglish,
                                        englishSignificance = upcoming.festival.significance,
                                        language = strings.language
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onNavigateToFestival(upcoming.festival.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .defaultMinSize(minHeight = 44.dp)
                                    ) {
                                        Text(strings.exploreVidhiButton, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                    OutlinedButton(
                                        onClick = { onShareFestival(upcoming) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .defaultMinSize(minHeight = 44.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.shareFestivalAction, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Blueprint Section 5.4: Compact Solid Panchang Card (Tithi, Nakshatra, Sunrise, Sunset).
 * Uses a solid background and is tappable to open Detailed Panchang.
 */
@Composable
private fun CompactSolidPanchangCard(
    title: String,
    mainValue: String,
    supportingInfo: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current

    Card(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = 3.dp,
            shape = RoundedCornerShape(18.dp),
            ambientColor = if (glassTokens.isDark) Color(0x44000000) else Color(0x2238271A),
            spotColor = if (glassTokens.isDark) Color(0x22D9A441) else Color(0x2B453322)
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (glassTokens.isDark) Color(0x40D9A441) else Color(0xFFD6C3AA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mainValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = supportingInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Blueprint Section 5.5: Next Festival Card with glass countdown overlay and readable date.
 */
@Composable
private fun NextFestivalSpotlightCard(
    occurrence: FestivalOccurrence,
    daysRemaining: Long,
    cityName: String,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val formattedDate = remember(occurrence.date, strings.language) {
        UtsavLocalizationEngine.formatDate(occurrence.date, "EEEE, d MMMM yyyy", strings.language)
    }
    val softPress = rememberSoftPressController()

    Card(
        onClick = { softPress.triggerSoftClick(onExplore) },
        interactionSource = softPress.interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .softPressCardEffect(softPress)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.45f) else Color(0xFFD6C3AA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Small Festival Illustration with Glass Countdown Overlay
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                FestivalArtworkBanner(
                    festivalId = occurrence.festival.id,
                    modifier = Modifier.fillMaxSize()
                )
                LiquidGlassSurface(
                    cornerRadius = 12.dp,
                    strongFill = true,
                    elevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (daysRemaining == 0L) "0" else "$daysRemaining",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (daysRemaining == 1L) strings.dayWordSingular else strings.dayWordPlural,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right: Festival Name, Date, Muhurat & Explore Action
            val strings = LocalUtsavStrings.current
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.upcomingFestivalsHeader.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassTokens.decorativeGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = UtsavLocalizationEngine.localizeFestivalName(
                        festivalId = occurrence.festival.id,
                        englishName = occurrence.festival.nameEnglish,
                        nativeDevanagari = occurrence.festival.nameNative,
                        language = strings.language
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.exploreVidhiButton} ($cityName)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Blueprint Section 5.6: Four compact Quick Action buttons:
 * Calendar, Puja Guide, Panchang, Reminders.
 */
@Composable
private fun TodayQuickActionsBar(
    onCalendarClick: () -> Unit,
    onPujaGuideClick: () -> Unit,
    onPanchangClick: () -> Unit,
    onRemindersClick: () -> Unit
) {
    val strings = LocalUtsavStrings.current
    LiquidGlassSurface(
        cornerRadius = 22.dp,
        elevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionButtonItem(
                icon = Icons.Default.CalendarMonth,
                label = strings.tabCalendar,
                onClick = onCalendarClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonItem(
                icon = Icons.Default.AutoAwesome,
                label = strings.pujaVidhiHeader,
                onClick = onPujaGuideClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonItem(
                icon = Icons.Default.WbSunny,
                label = strings.tabPanchang,
                onClick = onPanchangClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButtonItem(
                icon = Icons.Default.NotificationsActive,
                label = strings.remindAction,
                onClick = onRemindersClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButtonItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(glassTokens.solidElevatedContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
