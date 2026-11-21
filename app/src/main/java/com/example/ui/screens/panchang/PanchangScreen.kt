package com.example.ui.screens.panchang

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panchang.City
import com.example.panchang.FestivalDateEvaluator
import com.example.panchang.PanchangEngine
import com.example.ui.components.AnimatedCollapsibleSection
import com.example.ui.components.CitySelectionDialog
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.PanchangStrip
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.TulsiGreen
import com.example.ui.theme.UtsavLocalizationEngine
import java.time.LocalDate
import java.util.Locale

/**
 * Blueprint Section 11 (Screen 9): Detailed Panchang & Section 11.5 (Screen 19): About These Timings.
 * Prioritises accuracy, clarity, and progressive disclosure with smooth animated dropdowns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchangScreen(
    viewModel: PanchangViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    var cityMenuExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val isToday = state.selectedDate == PanchangEngine.localDateForCity(state.hostCity)
    val formattedDate = remember(state.selectedDate, strings.language) {
        UtsavLocalizationEngine.formatDate(state.selectedDate, "EEEE, d MMMM yyyy", strings.language)
    }

    if (showDatePickerDialog) {
        val initialMillis = remember(state.selectedDate) {
            state.selectedDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = FestivalDateEvaluator.SUPPORTED_YEARS
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val pickedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.selectDate(pickedDate)
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("panchang_date_picker_confirm")
                ) {
                    Text(strings.showDetailsLabel, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.hideDetailsLabel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("panchang_screen"),
        contentPadding = PaddingValues(bottom = 128.dp)
    ) {
        // 1. Header & City Selector
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.dailyPanchangHeader,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = strings.panchangSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // City Selector Pill
                    Box {
                        val localizedCity = UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language)
                        val localizedState = UtsavLocalizationEngine.localizeStateName(state.hostCity.state, strings.language)
                        OutlinedCard(
                            onClick = { cityMenuExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = glassTokens.solidCardContainer
                            ),
                            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("panchang_city_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change city",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (cityMenuExpanded) {
                            CitySelectionDialog(
                                currentCity = state.hostCity,
                                recentCities = state.userPreferences?.recentCities.orEmpty(),
                                onCitySelected = { city ->
                                    viewModel.selectCity(city)
                                    cityMenuExpanded = false
                                },
                                onDismiss = { cityMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Date Navigation Bar: [ < ] 30 October 2026 [ > ] + Date Picker Tap + Horizontal Swipe
                var dateDragDelta by remember(state.selectedDate) { mutableFloatStateOf(0f) }
                LiquidGlassSurface(
                    cornerRadius = 20.dp,
                    strongFill = true,
                    elevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(state.selectedDate) {
                            detectHorizontalDragGestures(
                                onDragStart = { dateDragDelta = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dateDragDelta += dragAmount
                                },
                                onDragEnd = {
                                    if (dateDragDelta < -55f) {
                                        viewModel.nextDay()
                                    } else if (dateDragDelta > 55f) {
                                        viewModel.previousDay()
                                    }
                                    dateDragDelta = 0f
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousDay() },
                            modifier = Modifier.testTag("panchang_prev_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous day"
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePickerDialog = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("panchang_date_picker_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pick date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (!isToday) {
                                TextButton(
                                    onClick = { viewModel.jumpToToday() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        text = strings.tabToday,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = strings.tabToday,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glassTokens.auspiciousGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.nextDay() },
                            modifier = Modifier.testTag("panchang_next_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next day"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Collapsible 20-Year & 12-Month Jump Dropdown
                val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
                AnimatedCollapsibleSection(
                    title = strings.filtersAndJumpDropdownLabel,
                    subtitle = "${state.selectedDate.year} · ${UtsavLocalizationEngine.formatDate(state.selectedDate, "MMMM", strings.language)}",
                    initiallyExpanded = false,
                    testTagName = "panchang_jump_dropdown"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("panchang_year_selector_row"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(FestivalDateEvaluator.SUPPORTED_YEARS.toList()) { year ->
                                val isSelectedYear = state.selectedDate.year == year
                                FilterChip(
                                    selected = isSelectedYear,
                                    onClick = {
                                        val targetMonth = java.time.YearMonth.of(year, state.selectedDate.monthValue)
                                        val safeDay = state.selectedDate.dayOfMonth.coerceIn(1, targetMonth.lengthOfMonth())
                                        viewModel.selectDate(targetMonth.atDay(safeDay))
                                    },
                                    label = {
                                        Text(
                                            text = year.toString(),
                                            fontWeight = if (isSelectedYear) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("panchang_year_chip_$year")
                                )
                            }
                        }

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("panchang_month_selector_row"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items((1..12).toList()) { monthNum ->
                                val isSelectedMonth = state.selectedDate.monthValue == monthNum
                                val shortName = java.time.Month.of(monthNum).getDisplayName(java.time.format.TextStyle.SHORT, activeLocale)
                                FilterChip(
                                    selected = isSelectedMonth,
                                    onClick = {
                                        val targetMonth = java.time.YearMonth.of(state.selectedDate.year, monthNum)
                                        val safeDay = state.selectedDate.dayOfMonth.coerceIn(1, targetMonth.lengthOfMonth())
                                        viewModel.selectDate(targetMonth.atDay(safeDay))
                                    },
                                    label = {
                                        Text(
                                            text = shortName,
                                            fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("panchang_month_chip_$monthNum")
                                )
                            }
                        }
                    }
                }
            }
        }

        val panchang = state.panchangDay
        if (panchang != null) {
            // 3. Sunrise & Sunset Solid Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = glassTokens.saffronHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.sunriseLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panchang.formattedSunrise(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.sunsetLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panchang.formattedSunset(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Panchang Details Card (Compact 2x2 Grid + Lunar Month)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.panchangDetailsHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassTokens.decorativeGold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val localizedPaksha = UtsavLocalizationEngine.localizePaksha(
                            panchang.tithi.paksha.displayName.substringBefore(" "),
                            strings.language
                        )
                        val localizedTithi = UtsavLocalizationEngine.localizeTithiName(panchang.tithi.name, strings.language)
                        val localizedNakshatra = UtsavLocalizationEngine.localizeNakshatraName(panchang.nakshatra.name, strings.language)
                        val localizedYoga = UtsavLocalizationEngine.localizeYogaName(panchang.yoga.name, strings.language)
                        val localizedKarana = UtsavLocalizationEngine.localizeKaranaName(panchang.karana.name, strings.language)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PanchangDetailVerticalRow(
                                    label = strings.tithiLabel,
                                    mainValue = "$localizedPaksha $localizedTithi",
                                    timingOrContext = "${strings.untilLabel} ${panchang.tithi.endTime ?: strings.fullDayLabel}"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PanchangDetailVerticalRow(
                                    label = strings.nakshatraLabel,
                                    mainValue = localizedNakshatra,
                                    timingOrContext = "${strings.untilLabel} ${panchang.nakshatra.endTime ?: strings.fullDayLabel}"
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PanchangDetailVerticalRow(
                                    label = strings.yogaLabel,
                                    mainValue = localizedYoga,
                                    timingOrContext = "#${panchang.yoga.index}/27"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PanchangDetailVerticalRow(
                                    label = strings.karanaLabel,
                                    mainValue = localizedKarana,
                                    timingOrContext = "6°"
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        val amantaMonth = UtsavLocalizationEngine.localizeLunarMonth(
                            panchang.getMonthNameForConvention(com.example.panchang.LunarConvention.AMANTA),
                            strings.language
                        )
                        val purnimantaMonth = UtsavLocalizationEngine.localizeLunarMonth(
                            panchang.getMonthNameForConvention(com.example.panchang.LunarConvention.PURNIMANTA),
                            strings.language
                        )
                        val solarMonth = panchang.regionalSolarMonth
                        if (solarMonth != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    PanchangDetailVerticalRow(
                                        label = strings.lunarMonthLabel,
                                        mainValue = "$amantaMonth / $purnimantaMonth",
                                        timingOrContext = "Amanta · Purnimanta"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    PanchangDetailVerticalRow(
                                        label = "Regional Solar",
                                        mainValue = solarMonth.substringBefore(" ("),
                                        timingOrContext = solarMonth.substringAfter("(", "").removeSuffix(")")
                                    )
                                }
                            }
                        } else {
                            PanchangDetailVerticalRow(
                                label = strings.lunarMonthLabel,
                                mainValue = "$amantaMonth / $purnimantaMonth",
                                timingOrContext = "Amanta · Purnimanta"
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PanchangDetailVerticalRow(
                                    label = "Samvat",
                                    mainValue = "Vikram ${panchang.vikramSamvat}",
                                    timingOrContext = "Shaka ${panchang.shakaSamvat}"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                val sunSign = panchang.sunRashi.substringBefore(" (")
                                val moonSign = panchang.moonRashi.substringBefore(" (")
                                PanchangDetailVerticalRow(
                                    label = "Rashi & Disha Shool",
                                    mainValue = if (sunSign.isNotBlank() && moonSign.isNotBlank()) "☀ $sunSign · ☽ $moonSign" else "—",
                                    timingOrContext = if (panchang.dishaShool.isNotBlank()) "Disha Shool: ${panchang.dishaShool}" else ""
                                )
                            }
                        }
                    }
                }
            }

            // 5. Auspicious & Inauspicious Periods inside AnimatedCollapsibleSection
            item {
                val brahma = state.brahmaMuhurat ?: panchang.brahmaMuhurat
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    AnimatedCollapsibleSection(
                        title = strings.auspiciousTimingsHeader,
                        subtitle = "${strings.brahmaMuhuratLabel}: ${brahma.format()} · ${strings.rahuKaalLabel}: ${panchang.rahuKaal.format()}",
                        initiallyExpanded = true,
                        testTagName = "panchang_timings_dropdown"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimingPeriodRow(
                                title = strings.brahmaMuhuratLabel,
                                subtitle = strings.auspiciousTimingsHeader,
                                timeRange = brahma.format(),
                                isAuspicious = true
                            )

                            val abhijitApplicable = panchang.abhijitMuhurat != null
                            TimingPeriodRow(
                                title = strings.abhijitMuhuratLabel,
                                subtitle = strings.auspiciousTimingsHeader,
                                timeRange = panchang.abhijitMuhurat?.format() ?: "—",
                                isAuspicious = abhijitApplicable,
                                isNeutral = !abhijitApplicable
                            )

                            TimingPeriodRow(
                                title = strings.rahuKaalLabel,
                                subtitle = strings.inauspiciousTimingsHeader,
                                timeRange = panchang.rahuKaal.format(),
                                isAuspicious = false
                            )

                            TimingPeriodRow(
                                title = strings.yamagandamLabel,
                                subtitle = strings.inauspiciousTimingsHeader,
                                timeRange = panchang.yamagandam.format(),
                                isAuspicious = false
                            )

                            TimingPeriodRow(
                                title = strings.gulikaKaalLabel,
                                subtitle = strings.inauspiciousTimingsHeader,
                                timeRange = panchang.gulikaKaal.format(),
                                isAuspicious = false
                            )
                        }
                    }
                }
            }

            // 6. Visual Timeline Strip reuse (already a collapsible dropdown)
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PanchangStrip(panchang = panchang)
                }
            }

            // 7. Source and Trust Information (AnimatedCollapsibleSection)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val localizedCity = UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language)
                    val localizedState = UtsavLocalizationEngine.localizeStateName(state.hostCity.state, strings.language)
                    AnimatedCollapsibleSection(
                        title = strings.astronomicalNoteLabel,
                        subtitle = "$localizedCity, $localizedState · ${state.hostCity.panchangConvention.displayName.substringBefore(" (")} · Lahiri (${String.format(Locale.US, "%.2f", panchang.ayanamsaDegrees)}°)",
                        initiallyExpanded = false,
                        testTagName = "about_timings_card"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val latDir = if (state.hostCity.latitude >= 0.0) "N" else "S"
                            val lonDir = if (state.hostCity.longitude >= 0.0) "E" else "W"
                            val coordSummary = String.format(
                                Locale.US,
                                "%.4f° %s, %.4f° %s",
                                kotlin.math.abs(state.hostCity.latitude),
                                latDir,
                                kotlin.math.abs(state.hostCity.longitude),
                                lonDir
                            )
                            TrustMetadataRow(
                                label = strings.locationSectionHeader,
                                value = "$localizedCity, $localizedState, ${state.hostCity.country} ($coordSummary · ${state.hostCity.timeZoneId})"
                            )
                            TrustMetadataRow(
                                label = "Regional Convention",
                                value = "${state.hostCity.panchangConvention.displayName}: ${state.hostCity.panchangConvention.monthScheme}"
                            )
                            TrustMetadataRow(
                                label = strings.shastraRuleLabel,
                                value = "Drik Ganita (Meeus Ephemeris) · Lahiri Chitrapaksha (${String.format(Locale.US, "%.4f", panchang.ayanamsaDegrees)}°) · 50' atmospheric refraction"
                            )
                            TrustMetadataRow(
                                label = "Benchmark Validation",
                                value = "Benchmarked against Positional Astronomy Centre (IMD PAC, Kolkata) ephemeris tables (Tolerances: ±2 min sunrise, ±15 min tithi transition)"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangDetailVerticalRow(
    label: String,
    mainValue: String,
    timingOrContext: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = mainValue,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = timingOrContext,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TimingPeriodRow(
    title: String,
    subtitle: String,
    timeRange: String,
    isAuspicious: Boolean,
    isNeutral: Boolean = false
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val accentColor = when {
        isNeutral -> MaterialTheme.colorScheme.onSurfaceVariant
        isAuspicious -> glassTokens.auspiciousGreen
        else -> MaterialTheme.colorScheme.error
    }
    val bgColor = if (isNeutral) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    } else {
        accentColor.copy(alpha = 0.08f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = timeRange,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrustMetadataRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
