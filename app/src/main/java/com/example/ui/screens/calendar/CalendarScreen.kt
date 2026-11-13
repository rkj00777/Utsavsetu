package com.example.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.RegionalTradition
import com.example.ui.components.AnimatedCollapsibleSection
import com.example.ui.components.FestivalArtworkBanner
import com.example.ui.components.FestivalCard
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.PanchangStrip
import com.example.ui.components.ShareFestivalDialog
import com.example.ui.components.rememberSoftPressController
import com.example.ui.components.softPressCardEffect
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.UtsavLocalizationEngine
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Blueprint Section 6 (Screen 4): Festival Calendar (Month View & Agenda View).
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToFestival: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    var shareTargetOccurrence by remember { mutableStateOf<FestivalOccurrence?>(null) }
    var showMonthYearPickerDialog by remember { mutableStateOf(false) }

    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()
    val supportedYears = remember { com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.toList() }

    LaunchedEffect(state.currentMonth.year) {
        val idx = supportedYears.indexOf(state.currentMonth.year)
        if (idx >= 0) {
            yearListState.animateScrollToItem((idx - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(state.currentMonth.monthValue) {
        val idx = (state.currentMonth.monthValue - 1).coerceIn(0, 11)
        monthListState.animateScrollToItem((idx - 1).coerceAtLeast(0))
    }

    if (showMonthYearPickerDialog) {
        MonthYearPickerDialog(
            currentMonth = state.currentMonth,
            allYearOccurrences = state.allYearOccurrences,
            onSelectYearMonth = { targetYm ->
                viewModel.selectYearMonth(targetYm)
                showMonthYearPickerDialog = false
            },
            onSelectYearOnly = { year ->
                viewModel.selectYear(year)
            },
            onDismiss = { showMonthYearPickerDialog = false }
        )
    }

    shareTargetOccurrence?.let { occ ->
        ShareFestivalDialog(
            festival = occ.festival,
            date = occ.date,
            muhuratWindow = occ.muhuratWindowFormatted,
            cityName = state.hostCity.name,
            onDismiss = { shareTargetOccurrence = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Top Bar + Segmented Month/Agenda Control
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
                            text = strings.tabCalendar,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${strings.pujaMuhuratLabel} · ${UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Today Shortcut Button (Blueprint Section 6.1)
                        OutlinedButton(
                            onClick = { viewModel.jumpToToday() },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("calendar_today_shortcut_button")
                        ) {
                            Text(
                                text = strings.tabToday,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Toggle View Button (preserves calendar_toggle_view_button testTag)
                        LiquidGlassSurface(
                            cornerRadius = 18.dp,
                            strongFill = true,
                            elevation = 3.dp,
                            onClick = { viewModel.toggleViewMode() },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("calendar_toggle_view_button")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (state.isListView) Icons.Default.CalendarMonth else Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = "Switch Month or Agenda view",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        LiquidGlassSurface(
                            cornerRadius = 18.dp,
                            strongFill = true,
                            elevation = 3.dp,
                            onClick = onOpenSearch,
                            modifier = Modifier.size(42.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Control: Month vs Agenda (Blueprint Section 6)
                LiquidGlassSurface(
                    cornerRadius = 18.dp,
                    strongFill = true,
                    elevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isMonth = !state.isListView
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isMonth) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setListView(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.monthViewLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isMonth) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (!isMonth) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setListView(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.agendaViewLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (!isMonth) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2A. Category Filter Chips (Localized: All, Major, Regional, Fasts & Vrats)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CalendarCategoryFilter.values()) { category ->
                    FilterChip(
                        selected = state.activeCategoryFilter == category,
                        onClick = { viewModel.setCategoryFilter(category) },
                        label = {
                            Text(
                                UtsavLocalizationEngine.localizeCategoryLabel(category.label, strings.language)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("calendar_category_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // 2B. Animated Collapsible Dropdown for Tradition Filters, 20-Year Explorer & 12-Month Jump
        item {
            val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
            val activeTradLabel = state.activeTraditionFilter?.let {
                UtsavLocalizationEngine.localizeTraditionName(it, strings.language)
            } ?: strings.myTraditionsAllLabel
            val currentMonthShort = state.currentMonth.month.getDisplayName(TextStyle.SHORT, activeLocale)

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                AnimatedCollapsibleSection(
                    title = strings.filtersAndJumpDropdownLabel,
                    subtitle = "$activeTradLabel • $currentMonthShort ${state.currentMonth.year}",
                    badgeText = "${state.currentMonth.year}",
                    icon = Icons.Default.CalendarMonth,
                    iconTint = MaterialTheme.colorScheme.primary,
                    initiallyExpanded = false,
                    testTagName = "calendar_filters_dropdown"
                ) {
                    // Tradition Filters
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.activeTraditionFilter == null,
                                onClick = { viewModel.setTraditionFilter(null) },
                                label = { Text(strings.myTraditionsAllLabel) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                        items(RegionalTradition.values()) { tradition ->
                            FilterChip(
                                selected = state.activeTraditionFilter == tradition,
                                onClick = { viewModel.setTraditionFilter(tradition) },
                                label = { Text(UtsavLocalizationEngine.localizeTraditionName(tradition, strings.language)) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 20-Year Selector Bar (2025..2046)
                    LazyRow(
                        state = yearListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_year_selector_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(supportedYears) { year ->
                            val isSelectedYear = state.currentMonth.year == year
                            FilterChip(
                                selected = isSelectedYear,
                                onClick = { viewModel.selectYear(year) },
                                label = {
                                    Text(
                                        text = year.toString(),
                                        fontWeight = if (isSelectedYear) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("calendar_year_chip_$year")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 12-Month Direct Selection Chip Bar (Jan..Dec)
                    val countsByMonth = remember(state.allYearOccurrences) {
                        state.allYearOccurrences.groupingBy { it.date.monthValue }.eachCount()
                    }
                    LazyRow(
                        state = monthListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_month_selector_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..12).toList()) { monthNum ->
                            val isSelectedMonth = state.currentMonth.monthValue == monthNum
                            val shortName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, activeLocale)
                            val festCount = countsByMonth[monthNum] ?: 0
                            FilterChip(
                                selected = isSelectedMonth,
                                onClick = { viewModel.selectMonth(monthNum) },
                                label = {
                                    Text(
                                        text = if (festCount > 0) "$shortName ($festCount)" else shortName,
                                        fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("calendar_month_chip_$monthNum")
                            )
                        }
                    }
                }
            }
        }

        // 3C. Month Selector Bar (Swipeable + Tappable Month/Year Picker)
        item {
            var headerDragDelta by remember(state.currentMonth) { mutableFloatStateOf(0f) }
            val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
            LiquidGlassSurface(
                cornerRadius = 20.dp,
                strongFill = true,
                elevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .pointerInput(state.currentMonth) {
                        detectHorizontalDragGestures(
                            onDragStart = { headerDragDelta = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                headerDragDelta += dragAmount
                            },
                            onDragEnd = {
                                if (headerDragDelta < -55f) {
                                    viewModel.nextMonth()
                                } else if (headerDragDelta > 55f) {
                                    viewModel.prevMonth()
                                }
                                headerDragDelta = 0f
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
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = strings.backButton
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMonthYearPickerDialog = true }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("calendar_month_picker_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.currentMonth.month.getDisplayName(TextStyle.FULL, activeLocale)} ${state.currentMonth.year}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = strings.tabCalendar,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = strings.nextPrefix
                        )
                    }
                }
            }
        }

        if (!state.isListView) {
            // 4A. Month Grid View (Solid Card with Horizontal Swipe Month Navigation)
            item {
                var gridDragDelta by remember(state.currentMonth) { mutableFloatStateOf(0f) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("calendar_month_grid_card")
                        .pointerInput(state.currentMonth) {
                            detectHorizontalDragGestures(
                                onDragStart = { gridDragDelta = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    gridDragDelta += dragAmount
                                },
                                onDragEnd = {
                                    if (gridDragDelta < -55f) {
                                        viewModel.nextMonth()
                                    } else if (gridDragDelta > 55f) {
                                        viewModel.prevMonth()
                                    }
                                    gridDragDelta = 0f
                                }
                            )
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (glassTokens.isDark) Color(0x40D9A441) else Color(0xFFD6C3AA)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    MonthGrid(
                        yearMonth = state.currentMonth,
                        selectedDate = state.selectedDate,
                        festivalDates = state.monthOccurrences.groupBy { it.date },
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }
            }

            // Selected Day Observances Inspector
            item {
                val selectedEvents = state.monthOccurrences.filter { it.date == state.selectedDate }
                val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        text = UtsavLocalizationEngine.formatDate(state.selectedDate, "EEEE, d MMMM yyyy", strings.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (selectedEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedEvents.forEach { occ ->
                            FestivalCard(
                                occurrence = occ,
                                onClick = { onNavigateToFestival(occ.festival.id) },
                                onShareClick = { shareTargetOccurrence = occ },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Upcoming Observances in Month
            item {
                val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
                Text(
                    text = "${strings.upcomingFestivalsHeader} · ${state.currentMonth.month.getDisplayName(TextStyle.FULL, activeLocale)} (${state.monthOccurrences.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        } else {
            // 4B. Agenda View Header
            item {
                val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
                Text(
                    text = "${strings.agendaViewLabel} · ${state.currentMonth.month.getDisplayName(TextStyle.FULL, activeLocale)} (${state.monthOccurrences.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }

        if (state.monthOccurrences.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.noFestivalsThisMonth,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (state.activeTraditionFilter != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.setTraditionFilter(null) }) {
                                Text(strings.resetFilterButton, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(
                items = state.monthOccurrences,
                key = { "${it.festival.id}_${it.date}" }
            ) { occurrence ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    if (state.isListView) {
                        AgendaEventRowCard(
                            occurrence = occurrence,
                            hasReminder = state.scheduledReminderFestivalIds.contains(occurrence.festival.id),
                            onClick = { onNavigateToFestival(occurrence.festival.id) }
                        )
                    } else {
                        FestivalCard(
                            occurrence = occurrence,
                            onClick = { onNavigateToFestival(occurrence.festival.id) },
                            onShareClick = { shareTargetOccurrence = occurrence }
                        )
                    }
                }
            }
        }

        // Selected Date Detailed Panchang Strip at the bottom of Month View
        if (!state.isListView) {
            state.selectedDayPanchang?.let { panchang ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        PanchangStrip(panchang = panchang)
                    }
                }
            }
        }
    }
}

/**
 * Blueprint Section 6.2: Agenda View Row.
 * Contains Date & day of week, Festival name, One-line description, Regional badge, and Reminder status.
 */
@Composable
private fun AgendaEventRowCard(
    occurrence: FestivalOccurrence,
    hasReminder: Boolean,
    onClick: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val dayNum = occurrence.date.dayOfMonth.toString()
    val dayOfWeek = occurrence.date.dayOfWeek.getDisplayName(TextStyle.SHORT, UtsavLocalizationEngine.localeForLanguage(strings.language))
    val softPress = rememberSoftPressController()

    Card(
        onClick = { softPress.triggerSoftClick(onClick) },
        interactionSource = softPress.interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .softPressCardEffect(softPress)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (glassTokens.isDark) Color(0x44000000) else Color(0x2238271A),
                spotColor = if (glassTokens.isDark) Color(0x22D9A441) else Color(0x2B453322)
            )
            .testTag("agenda_row_${occurrence.festival.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (glassTokens.isDark) Color(0x40D9A441) else Color(0xFFD6C3AA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Date Badge
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(glassTokens.solidElevatedContainer)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayOfWeek.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dayNum,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Festival Photographic Thumbnail
            FestivalArtworkBanner(
                festivalId = occurrence.festival.id,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Info
            val strings = LocalUtsavStrings.current
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = UtsavLocalizationEngine.localizeFestivalName(
                            festivalId = occurrence.festival.id,
                            englishName = occurrence.festival.nameEnglish,
                            nativeDevanagari = occurrence.festival.nameNative,
                            language = strings.language
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (hasReminder) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Reminder enabled",
                            tint = glassTokens.auspiciousGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = UtsavLocalizationEngine.localizeSignificance(
                        festivalId = occurrence.festival.id,
                        englishName = occurrence.festival.nameEnglish,
                        englishSignificance = occurrence.festival.significance,
                        language = strings.language
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = glassTokens.solidElevatedContainer
                    ) {
                        Text(
                            text = occurrence.festival.traditions.firstOrNull()?.let {
                                UtsavLocalizationEngine.localizeTraditionName(it, strings.language)
                            } ?: strings.allRegionsLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${strings.pujaMuhuratLabel}: ${occurrence.muhuratWindowFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Blueprint Section 6.1: Calendar Cell Design.
 * Gregorian date + restrained dots to indicate observances (never clutters cell with full festival name).
 */
@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    festivalDates: Map<LocalDate, List<FestivalOccurrence>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7

    val strings = LocalUtsavStrings.current
    val activeLocale = UtsavLocalizationEngine.localeForLanguage(strings.language)
    val weekdayOrder = listOf(
        java.time.DayOfWeek.SUNDAY,
        java.time.DayOfWeek.MONDAY,
        java.time.DayOfWeek.TUESDAY,
        java.time.DayOfWeek.WEDNESDAY,
        java.time.DayOfWeek.THURSDAY,
        java.time.DayOfWeek.FRIDAY,
        java.time.DayOfWeek.SATURDAY
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayOrder.forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, activeLocale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val eventsOnDay = festivalDates[date].orEmpty()
                        val hasFestival = eventsOnDay.isNotEmpty()
                        val cellBorder = when {
                            isSelected -> null
                            hasFestival -> BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.55f))
                            isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else -> null
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> glassTokens.decorativeGold.copy(alpha = 0.22f)
                                        hasFestival -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(10.dp)) else Modifier
                                )
                                .clickable { onDateSelected(date) }
                                .testTag("calendar_day_$dayNumber"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || hasFestival || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        hasFestival -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasFestival) {
                                    val shortFestLabel = UtsavLocalizationEngine.localizeFestivalName(
                                        eventsOnDay.first().festival,
                                        strings.language
                                    )
                                        .substringBefore(" /")
                                        .substringBefore(" —")
                                        .substringBefore(" (")
                                        .trim()
                                    Text(
                                        text = shortFestLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, lineHeight = 9.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else glassTokens.decorativeGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).height(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    allYearOccurrences: List<FestivalOccurrence>,
    onSelectYearMonth: (YearMonth) -> Unit,
    onSelectYearOnly: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    var pickerYear by remember(currentMonth.year) { mutableIntStateOf(currentMonth.year) }
    val supportedYears = remember { com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.toList() }
    val countsByMonth = remember(allYearOccurrences, pickerYear, currentMonth.year) {
        if (pickerYear == currentMonth.year) {
            allYearOccurrences.groupingBy { it.date.monthValue }.eachCount()
        } else {
            emptyMap()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = glassTokens.solidCardContainer,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Month & Year",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prevYear = (pickerYear - 1).coerceAtLeast(supportedYears.first())
                            pickerYear = prevYear
                            onSelectYearOnly(prevYear)
                        },
                        enabled = pickerYear > supportedYears.first()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Year"
                        )
                    }
                    Text(
                        text = pickerYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = {
                            val nextYear = (pickerYear + 1).coerceAtMost(supportedYears.last())
                            pickerYear = nextYear
                            onSelectYearOnly(nextYear)
                        },
                        enabled = pickerYear < supportedYears.last()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Year"
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_year_picker_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 3) {
                            val monthNum = row * 3 + col + 1
                            val isSelected = pickerYear == currentMonth.year && monthNum == currentMonth.monthValue
                            val strings = LocalUtsavStrings.current
                            val monthName = Month.of(monthNum).getDisplayName(
                                TextStyle.SHORT,
                                UtsavLocalizationEngine.localeForLanguage(strings.language)
                            )
                            val festCount = countsByMonth[monthNum] ?: 0

                            Surface(
                                onClick = {
                                    onSelectYearMonth(YearMonth.of(pickerYear, monthNum))
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else glassTokens.solidElevatedContainer,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) glassTokens.decorativeGold else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("picker_month_cell_$monthNum")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (festCount > 0) {
                                        Text(
                                            text = "$festCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val strings = LocalUtsavStrings.current
            TextButton(onClick = onDismiss) {
                Text(strings.closeButtonLabel, fontWeight = FontWeight.Bold)
            }
        }
    )
}

