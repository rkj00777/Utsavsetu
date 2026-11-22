package com.example.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.FestivalRegionalNames
import com.example.domain.FestivalShastraDetails
import com.example.domain.FestivalShastraKnowledge
import com.example.domain.model.SamagriItem
import com.example.domain.model.VidhiStep
import com.example.panchang.FestivalDateEvaluator
import com.example.platform.audio.MantraAudioEngine
import com.example.platform.audio.MantraPlaybackState
import com.example.platform.audio.NinadaPackStatus
import com.example.platform.notifications.ReminderScheduler
import com.example.ui.components.*
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.UtsavLocalizationEngine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Blueprint Sections 8, 9, 10 (Screens 6, 7, 8):
 * - Screen 6: Festival Detail (Hero, Primary Start Puja Guide CTA, Tabular Important Timings & Muhurat on TOP, Expandable Why Celebrated & Sacred Katha)
 * - Screen 7: Guided Puja Mode (Distraction-free step-by-step with inline required items, Finish Later resume, and Calm Completion state)
 * - Screen 8: Samagri Checklist & Pooja Mantras Japa Mala Counter
 */
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val audioEngine = remember(context) { MantraAudioEngine.getInstance(context) }
    val mantraAudioState by audioEngine.playbackState.collectAsStateWithLifecycle()
    val ninadaPackStatuses by audioEngine.ninadaPackStatuses.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stop()
        }
    }

    LaunchedEffect(state.panditVoiceGender) {
        audioEngine.setVoiceGender(state.panditVoiceGender)
    }

    LaunchedEffect(strings.language) {
        audioEngine.setAppLanguage(strings.language)
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Puja Vidhi, 1 = Samagri Checklist, 2 = Mantras
    var selectedMantraTarget by remember { mutableIntStateOf(1) }
    var isPujaModeActive by remember { mutableStateOf(false) }
    var currentPujaStepIndex by remember { mutableIntStateOf(0) }
    var isPujaCompleted by remember { mutableStateOf(false) }

    var aboutExpanded by remember { mutableStateOf(false) }
    var showReminderEditor by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showResetChecklistDialog by remember { mutableStateOf(false) }

    BackHandler {
        audioEngine.stop()
        if (isPujaModeActive) {
            isPujaModeActive = false
            isPujaCompleted = false
        } else {
            onBack()
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val festival = state.festival ?: return
    val baseShastraDetails = remember(festival.id) {
        FestivalShastraKnowledge.getDetailsForFestival(festival)
    }
    val shastraDetails = remember(festival.id, strings.language) {
        UtsavLocalizationEngine.localizeShastraDetails(festival, baseShastraDetails, strings.language)
    }
    val localizedFestivalTitle = remember(festival.id, strings.language) {
        UtsavLocalizationEngine.localizeFestivalName(festival, strings.language)
    }
    val localizedCityName = remember(state.hostCity.id, strings.language) {
        UtsavLocalizationEngine.localizeCityName(state.hostCity, strings.language)
    }
    val localizedStateName = remember(state.hostCity.state, strings.language) {
        UtsavLocalizationEngine.localizeStateName(state.hostCity.state, strings.language)
    }
    val localizedVidhiSteps = remember(festival.vidhiSteps, strings.language) {
        festival.vidhiSteps.map { UtsavLocalizationEngine.localizeVidhiStep(it, strings.language) }
    }
    val localizedSamagriAll = remember(festival.samagriItems, strings.language) {
        festival.samagriItems.map { UtsavLocalizationEngine.localizeSamagriItem(it, strings.language) }
    }

    val cityToday = remember(state.hostCity.id) {
        com.example.panchang.PanchangEngine.localDateForCity(state.hostCity)
    }
    val resolvedDate = state.resolvedDate ?: cityToday
    val formattedDate = remember(resolvedDate, strings.language) {
        UtsavLocalizationEngine.formatDate(resolvedDate, "EEEE, d MMMM yyyy", strings.language)
    }
    val daysAway = remember(resolvedDate, cityToday) {
        ChronoUnit.DAYS.between(cityToday, resolvedDate)
    }

    val checkedIds = state.samagriProgress?.checkedItemIds ?: emptySet()
    val totalSamagri = localizedSamagriAll.size
    val checkedCount = checkedIds.size
    val progressFraction by animateFloatAsState(
        targetValue = if (totalSamagri > 0) checkedCount.toFloat() / totalSamagri else 0f,
        label = "samagri_progress"
    )

    // Split Samagri into Essential vs Optional/Regional (Blueprint Section 10.1)
    val essentialItems = remember(localizedSamagriAll) {
        val splitIndex = (localizedSamagriAll.size * 0.7f).toInt().coerceAtLeast(1)
        localizedSamagriAll.take(splitIndex)
    }
    val optionalItems = remember(localizedSamagriAll) {
        val splitIndex = (localizedSamagriAll.size * 0.7f).toInt().coerceAtLeast(1)
        localizedSamagriAll.drop(splitIndex)
    }

    val regionalBadges = remember(festival.id, festival.aliases) {
        FestivalRegionalNames.getRegionalNamesForFestival(festival)
    }

    // Dialogs
    if (showShareDialog) {
        ShareFestivalDialog(
            festival = festival,
            date = resolvedDate,
            muhuratWindow = state.muhuratWindow,
            cityName = localizedCityName,
            checkedSamagriIds = checkedIds,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showReminderEditor) {
        ReminderEditorDialog(
            festivalId = festival.id,
            festivalName = localizedFestivalTitle,
            dateFormatted = formattedDate,
            cityName = localizedCityName,
            onSaveReminder = { entry ->
                viewModel.saveCustomReminder(entry)
                ReminderScheduler.scheduleCustomReminder(
                    context = context,
                    entry = entry,
                    festivalDate = resolvedDate,
                    muhuratWindowFormatted = state.muhuratWindow
                )
                ReminderScheduler.scheduleRollingWindow(
                    context = context,
                    occurrences = listOf(
                        com.example.domain.model.FestivalOccurrence(
                            festival = festival,
                            date = resolvedDate,
                            muhuratWindowFormatted = state.muhuratWindow
                        )
                    ),
                    dayBeforeEnabled = true,
                    morningOfEnabled = true,
                    muhuratStartEnabled = true
                )
            },
            onDismiss = { showReminderEditor = false }
        )
    }

    if (showResetChecklistDialog) {
        AlertDialog(
            onDismissRequest = { showResetChecklistDialog = false },
            title = { Text(strings.resetChecklistTitle, fontWeight = FontWeight.Bold) },
            text = {
                Text(strings.resetChecklistMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSamagriChecklist()
                        showResetChecklistDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.resetButtonLabel, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetChecklistDialog = false }) {
                    Text(strings.cancelButtonLabel)
                }
            }
        )
    }

    // Guided Puja Mode (Blueprint Section 9 — Screen 7)
    if (isPujaModeActive && localizedVidhiSteps.isNotEmpty()) {
        GuidedPujaModeScreen(
            festivalId = festival.id,
            festivalName = localizedFestivalTitle,
            shastraDetails = shastraDetails,
            steps = localizedVidhiSteps,
            samagriItems = localizedSamagriAll,
            checkedSamagriIds = checkedIds,
            currentIndex = currentPujaStepIndex,
            isCompleted = isPujaCompleted,
            audioEngine = audioEngine,
            mantraAudioState = mantraAudioState,
            onToggleSamagri = { viewModel.toggleSamagriItem(it) },
            onPrevious = {
                audioEngine.stop()
                if (currentPujaStepIndex > 0) {
                    currentPujaStepIndex--
                    viewModel.savePujaStepProgress(currentPujaStepIndex)
                }
            },
            onNext = {
                audioEngine.stop()
                if (currentPujaStepIndex < localizedVidhiSteps.lastIndex) {
                    currentPujaStepIndex++
                    viewModel.savePujaStepProgress(currentPujaStepIndex)
                } else {
                    viewModel.savePujaStepProgress(0)
                    isPujaCompleted = true
                }
            },
            onFinishLater = {
                audioEngine.stop()
                viewModel.savePujaStepProgress(currentPujaStepIndex)
                isPujaModeActive = false
            },
            onShareCompleted = { showShareDialog = true },
            onClose = {
                audioEngine.stop()
                isPujaModeActive = false
                isPujaCompleted = false
            }
        )
        return
    }

    val isCuratedYear = FestivalDateEvaluator.isCuratedYear(resolvedDate.year)
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val heroImageHeight = 300.dp
    val sheetOverlap = 24.dp
    val revealSpacerHeight = heroImageHeight - sheetOverlap
    val revealSpacerHeightPx = remember(density) { with(density) { revealSpacerHeight.toPx() } }

    // 0f = at top (hero fully visible), 1f = scrolled past hero (hero completely hidden)
    val heroCollapseProgress by remember(listState, revealSpacerHeightPx) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                revealSpacerHeightPx <= 0f -> 0f
                else -> (listState.firstVisibleItemScrollOffset.toFloat() / revealSpacerHeightPx).coerceIn(0f, 1f)
            }
        }
    }

    val curtainTopOffsetPx by remember(listState, revealSpacerHeightPx) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 0f
                else -> (revealSpacerHeightPx - listState.firstVisibleItemScrollOffset.toFloat()).coerceAtLeast(0f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        // Layer 1: Unobstructed Top Festival Hero Artwork (collapses smoothly on scroll, returns when back at top)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 860.dp)
                .fillMaxWidth()
                .height(heroImageHeight)
                .graphicsLayer {
                    translationY = -heroCollapseProgress * (revealSpacerHeightPx * 0.45f)
                    alpha = (1f - heroCollapseProgress * 1.12f).coerceIn(0f, 1f)
                }
                .testTag("detail_hero_artwork_container")
        ) {
            FestivalArtworkBanner(
                festivalId = festival.id,
                modifier = Modifier.fillMaxSize(),
                isHeroBackground = true
            )
        }

        // Layer 2: Solid rising sheet curtain aligned with the festival heading card so scrolled content cleanly hides the image
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .graphicsLayer {
                    translationY = curtainTopOffsetPx
                }
                .clip(
                    RoundedCornerShape(
                        topStart = if (heroCollapseProgress < 0.96f) 28.dp else 0.dp,
                        topEnd = if (heroCollapseProgress < 0.96f) 28.dp else 0.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
        )

        // Layer 3: Scrollable Detail Content (transparent top spacer reveals hero artwork unobstructed)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .testTag("detail_screen"),
            contentPadding = PaddingValues(bottom = 128.dp)
        ) {
            // Transparent top spacer: keeps the hero image 100% visible when at top of screen
            item(key = "detail_hero_reveal_spacer") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(revealSpacerHeight)
                        .testTag("detail_hero_spacer")
                )
            }

            // 1. Festival Heading Card (sits cleanly below the hero image at rest, glides over & hides the image as user scrolls)
            item(key = "detail_festival_heading_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp)
                        .testTag("detail_heading_card"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subtle visual pull-sheet indicator
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(glassTokens.decorativeGold.copy(alpha = 0.55f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidGlassSurface(
                        cornerRadius = 24.dp,
                        strongFill = true,
                        elevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val regionLabel = festival.traditions.firstOrNull()?.let {
                                    UtsavLocalizationEngine.localizeTraditionName(it, strings.language).substringBefore(" (")
                                } ?: "Sacred Tradition"
                                Text(
                                    text = "${festival.nativeScriptLabel} · $regionLabel",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = glassTokens.decorativeGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isCuratedYear && daysAway >= 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = when (daysAway) {
                                                0L -> strings.todayBadge
                                                1L -> strings.tomorrowBadge
                                                else -> String.format(Locale.getDefault(), strings.inDaysFormat, daysAway)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = localizedFestivalTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${festival.nameNative} · $formattedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Primary Action: Start Puja Guide + Secondary Actions Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    val hasSavedStep = state.savedPujaStepIndex in 1..localizedVidhiSteps.lastIndex
                    Button(
                        onClick = {
                            currentPujaStepIndex = if (hasSavedStep) state.savedPujaStepIndex else 0
                            isPujaCompleted = false
                            isPujaModeActive = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_puja_guide_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasSavedStep) {
                                "${strings.startPujaGuide} (${state.savedPujaStepIndex + 1}/${localizedVidhiSteps.size})"
                            } else {
                                strings.startPujaGuide
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Actions Row: View Samagri, Add Reminder, Share Festival
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.samagriTabPrefix,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { showReminderEditor = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("detail_add_reminder_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = if (state.isReminderSet) glassTokens.auspiciousGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.setReminder,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.shareGreeting,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 3. TOP PRIORITY: Key Timings, Tithi & Shubh Muhurat in Structured Tabular Format (Placed BEFORE Why Celebrated & Katha)
            item {
                var showFullMuhuratTable by rememberSaveable { mutableStateOf(false) }
                val tableChevronRotation by animateFloatAsState(
                    targetValue = if (showFullMuhuratTable) 180f else 0f,
                    label = "muhurat_table_chevron"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("important_timings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.5.dp, glassTokens.auspiciousGreen.copy(alpha = 0.55f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.importantTimingsHeader,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = glassTokens.decorativeGold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val hasMuhurat = state.muhuratWindow.isNotBlank()
                            val timingBadgeText = when {
                                !hasMuhurat -> strings.calculatedEstimateBadge
                                isCuratedYear -> strings.verifiedMeeusBadge
                                else -> strings.calculatedEstimateBadge
                            }
                            val timingBadgeColor = when {
                                !hasMuhurat -> MaterialTheme.colorScheme.surfaceVariant
                                isCuratedYear -> glassTokens.auspiciousGreen.copy(alpha = 0.14f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val timingBadgeTextColor = when {
                                !hasMuhurat -> MaterialTheme.colorScheme.onSurfaceVariant
                                isCuratedYear -> glassTokens.auspiciousGreen
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = timingBadgeColor
                            ) {
                                Text(
                                    text = timingBadgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = timingBadgeTextColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // TABULAR MUHURAT & PANCHANG TABLE (4 Core Rows Always Visible + Animated Dropdown for Extended Rows)
                        val localizedMuhuratTitle = UtsavLocalizationEngine.localizeMuhuratTitle(
                            festival.muhuratTitle,
                            strings.language
                        )
                        val primaryWindowText = state.muhuratWindow.ifBlank {
                            "06:40 AM – 08:50 AM"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = glassTokens.decorativeGold.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("muhurat_timings_table")
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.timingParameterColHeader,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(0.42f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.timingWindowColHeader,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(0.58f)
                                )
                            }

                            HorizontalDivider(color = glassTokens.decorativeGold.copy(alpha = 0.45f))

                            // Core Row 1: Festival Date & City
                            MuhuratTableRow(
                                label = strings.festivalDateRowLabel,
                                value = "$formattedDate • $localizedCityName, $localizedStateName",
                                isAlternate = false
                            )

                            // Core Row 2: Primary Puja Muhurat (Highlighted Auspicious Row)
                            MuhuratTableRow(
                                label = strings.primaryMuhuratRowLabel,
                                value = primaryWindowText,
                                isHighlighted = true,
                                valueColor = glassTokens.auspiciousGreen
                            )

                            state.panchang?.let { dayPanchang ->
                                val localizedTithi = UtsavLocalizationEngine.localizeTithiName(
                                    dayPanchang.tithi.name,
                                    strings.language
                                )
                                val localizedPaksha = UtsavLocalizationEngine.localizePakshaName(
                                    dayPanchang.tithi.paksha,
                                    strings.language
                                )
                                val localizedSpan = state.tithiSpanFormatted?.let {
                                    UtsavLocalizationEngine.translateTerms(it, strings.language)
                                } ?: "${strings.untilLabel} ${dayPanchang.tithi.endTime ?: strings.fullDayLabel}"

                                // Core Row 3: Tithi, Paksha & Span (with subtitle so label is centered and zero empty space)
                                MuhuratTableRow(
                                    label = strings.tithiAndPakshaRowLabel,
                                    value = "$localizedTithi ($localizedPaksha)",
                                    subtitle = localizedSpan,
                                    isAlternate = true
                                )

                                // Core Row 4: Sunrise & Sunset
                                MuhuratTableRow(
                                    label = strings.sunriseSunsetRowLabel,
                                    value = "${strings.sunriseLabel}: ${dayPanchang.formattedSunrise()} • ${strings.sunsetLabel}: ${dayPanchang.formattedSunset()}",
                                    isAlternate = false,
                                    isLast = !showFullMuhuratTable
                                )
                            }

                            // Animated Dropdown Inside Table for Extended Panchang Rows
                            AnimatedVisibility(visible = showFullMuhuratTable) {
                                Column {
                                    if (state.muhuratBreakdown.isNotBlank()) {
                                        MuhuratSectionBlock(
                                            title = strings.additionalMuhuratRowLabel,
                                            titleColor = MaterialTheme.colorScheme.primary,
                                            isAlternate = true
                                        ) {
                                            val rawItems = state.muhuratBreakdown.split(" • ", "\n").filter { it.isNotBlank() }
                                            rawItems.forEach { item ->
                                                val translated = UtsavLocalizationEngine.translateTerms(item.trim(), strings.language)
                                                val parts = translated.split(":", limit = 2)
                                                if (parts.size == 2) {
                                                    MuhuratTimingItemRow(
                                                        label = parts[0].trim(),
                                                        timeRange = parts[1].trim(),
                                                        accentColor = MaterialTheme.colorScheme.primary
                                                    )
                                                } else {
                                                    Text(
                                                        text = translated,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    state.panchang?.let { dayPanchang ->
                                        val localizedNakshatra = UtsavLocalizationEngine.localizeNakshatraName(
                                            dayPanchang.nakshatra.name,
                                            strings.language
                                        )
                                        val localizedAmanta = UtsavLocalizationEngine.localizeMonthName(
                                            dayPanchang.amantaMonth,
                                            strings.language
                                        )
                                        val localizedPurnimanta = UtsavLocalizationEngine.localizeMonthName(
                                            dayPanchang.purnimantaMonth,
                                            strings.language
                                        )

                                        MuhuratTableRow(
                                            label = strings.nakshatraRowLabel,
                                            value = localizedNakshatra,
                                            subtitle = "${strings.untilLabel} ${dayPanchang.nakshatra.endTime ?: strings.fullDayLabel}",
                                            isAlternate = false
                                        )

                                        MuhuratSectionBlock(
                                            title = strings.auspiciousKaalRowLabel,
                                            titleColor = glassTokens.auspiciousGreen,
                                            isAlternate = true
                                        ) {
                                            MuhuratTimingItemRow(
                                                label = strings.brahmaMuhuratLabel,
                                                timeRange = dayPanchang.brahmaMuhurat.format(),
                                                accentColor = glassTokens.auspiciousGreen
                                            )
                                            MuhuratTimingItemRow(
                                                label = strings.abhijitMuhuratLabel,
                                                timeRange = dayPanchang.abhijitMuhurat?.format() ?: "11:45 - 12:30",
                                                accentColor = glassTokens.auspiciousGreen
                                            )
                                            if (state.godhuliMuhuratFormatted.isNotBlank()) {
                                                MuhuratTimingItemRow(
                                                    label = UtsavLocalizationEngine.translateTerms("Godhuli Muhurat", strings.language),
                                                    timeRange = state.godhuliMuhuratFormatted,
                                                    accentColor = glassTokens.auspiciousGreen
                                                )
                                            }
                                        }

                                        MuhuratSectionBlock(
                                            title = strings.avoidKaalRowLabel,
                                            titleColor = MaterialTheme.colorScheme.error,
                                            isAlternate = false
                                        ) {
                                            MuhuratTimingItemRow(
                                                label = strings.rahuKaalAvoid,
                                                timeRange = dayPanchang.rahuKaal.format(),
                                                accentColor = MaterialTheme.colorScheme.error
                                            )
                                            MuhuratTimingItemRow(
                                                label = strings.yamagandamLabel,
                                                timeRange = dayPanchang.yamagandam.format(),
                                                accentColor = MaterialTheme.colorScheme.error
                                            )
                                        }

                                        MuhuratTableRow(
                                            label = strings.lunarMonthRowLabel,
                                            value = "$localizedAmanta / $localizedPurnimanta",
                                            isAlternate = true
                                        )
                                    }

                                    // Shastra Rule as full-width row with header above text, rather than unbalanced two-column row
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(glassTokens.solidElevatedContainer.copy(alpha = 0.65f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = strings.shastraRuleRowLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = UtsavLocalizationEngine.translateTerms(shastraDetails.shastraTithiRule, strings.language),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Animated Dropdown Button to Expand/Collapse Extended Panchang Table, City Comparison & 20-Year Explorer
                        Surface(
                            onClick = { showFullMuhuratTable = !showFullMuhuratTable },
                            shape = RoundedCornerShape(12.dp),
                            color = glassTokens.solidElevatedContainer,
                            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_full_muhurat_dropdown")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (showFullMuhuratTable) strings.hideDetailsLabel else strings.moreTimingsDropdownLabel,
                                        style = MaterialTheme.typography.labelLarge,
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
                                        .size(20.dp)
                                        .rotate(tableChevronRotation)
                                )
                            }
                        }

                        AnimatedVisibility(visible = showFullMuhuratTable) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = UtsavLocalizationEngine.localizeMuhuratDescription(
                                        festival.muhuratDescription,
                                        strings.language
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // City Selector Chips for Live City Muhurat Comparison
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("detail_city_selector_row")
                                ) {
                                    val quickCities = listOf(
                                        com.example.panchang.City.DELHI,
                                        com.example.panchang.City.MUMBAI,
                                        com.example.panchang.City.VARANASI,
                                        com.example.panchang.City.AHMEDABAD,
                                        com.example.panchang.City.KOLKATA,
                                        com.example.panchang.City.BENGALURU,
                                        com.example.panchang.City.CHENNAI
                                    )
                                    items(quickCities, key = { it.id }) { cityOption ->
                                        val isSelectedCity = state.hostCity.id == cityOption.id
                                        FilterChip(
                                            selected = isSelectedCity,
                                            onClick = { viewModel.selectCity(cityOption) },
                                            label = {
                                                Text(
                                                    text = UtsavLocalizationEngine.localizeCityName(cityOption, strings.language),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelectedCity) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            modifier = Modifier.testTag("detail_city_chip_${cityOption.id}")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = strings.twentyYearExplorerLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("detail_year_selector_row"),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(FestivalDateEvaluator.SUPPORTED_YEARS.toList()) { year ->
                                        val isSelectedYear = state.selectedYear == year
                                        FilterChip(
                                            selected = isSelectedYear,
                                            onClick = { viewModel.selectYear(year) },
                                            label = {
                                                Text(
                                                    text = year.toString(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelectedYear) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.testTag("detail_year_chip_$year")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Why It Is Celebrated Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("why_celebrated_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.whyCelebratedHeader,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = glassTokens.decorativeGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = shastraDetails.presidingDeity,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Primary concise summary always visible
                        Text(
                            text = shastraDetails.whyCelebrated,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5. Interactive Animated Accordion Dropdowns for Katha, Mantra, Vrat & Regional Names (Each as a clean top-level item)
            // Dropdown 1: Puranic Legend & Sacred Katha
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    AnimatedCollapsibleSection(
                        title = strings.puranicLegendHeader,
                        icon = Icons.Default.MenuBook,
                        iconTint = MaterialTheme.colorScheme.primary,
                        initiallyExpanded = false
                    ) {
                        Text(
                            text = shastraDetails.puranicLegend,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Dropdown 2: Primary Moola Mantra
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    AnimatedCollapsibleSection(
                        title = strings.primaryMoolaMantraHeader,
                        badgeText = "${shastraDetails.pujaMantras.size} ${strings.mantrasTabPrefix}",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = glassTokens.decorativeGold,
                        initiallyExpanded = false,
                        testTagName = "detail_sacred_mantra_box"
                    ) {
                        Text(
                            text = shastraDetails.sacredMantraSanskrit,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (strings.language == com.example.data.preferences.AppLanguage.ENGLISH) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = shastraDetails.sacredMantraTransliteration,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${strings.meaningPrefix}: ${shastraDetails.sacredMantraMeaning}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.testTag("view_all_puja_mantras_chip")
                        ) {
                            Text(
                                text = "${strings.mantrasTabPrefix} (${shastraDetails.pujaMantras.size}) →",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Dropdown 3: Vrat, Fasting & Naivedya Guide
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    AnimatedCollapsibleSection(
                        title = strings.vratAndNaivedyaHeader,
                        icon = Icons.Default.Spa,
                        iconTint = glassTokens.auspiciousGreen,
                        initiallyExpanded = false
                    ) {
                        Text(
                            text = shastraDetails.vratAndNaivedyaGuide,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Dropdown 4: Regional Names & Traditions Across India
            if (regionalBadges.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        AnimatedCollapsibleSection(
                            title = "${strings.alsoKnownAsLabel} • ${strings.regionalNamesHeaderSuffix}",
                            badgeText = "${regionalBadges.size}",
                            icon = Icons.Default.Language,
                            iconTint = MaterialTheme.colorScheme.primary,
                            initiallyExpanded = false
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("detail_regional_names_row")
                            ) {
                                items(regionalBadges) { badge ->
                                    val localizedRegion = UtsavLocalizationEngine.localizeRegionalBadgeText(
                                        badge.regionOrLanguage,
                                        strings.language
                                    )
                                    val localizedFestName = UtsavLocalizationEngine.localizeRegionalBadgeText(
                                        badge.festivalName,
                                        strings.language
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = glassTokens.solidElevatedContainer,
                                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = localizedRegion,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (badge.nativeScript != null) {
                                                    "$localizedFestName (${badge.nativeScript})"
                                                } else {
                                                    localizedFestName
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = festival.traditions.joinToString(", ") {
                                    UtsavLocalizationEngine.localizeTraditionName(it, strings.language)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 6. Segmented Tab Switcher: Puja Vidhi vs Samagri Checklist vs Pooja Mantras
            item {
                LiquidGlassSurface(
                    cornerRadius = 18.dp,
                    strongFill = true,
                    elevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabs = listOf(
                            "${strings.vidhiTabPrefix} (${localizedVidhiSteps.size})",
                            "${strings.samagriTabPrefix} ($checkedCount/$totalSamagri)",
                            "${strings.mantrasTabPrefix} (${shastraDetails.pujaMantras.size})"
                        )
                        tabs.forEachIndexed { index, label ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("detail_tab_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Tab 0: Step-by-Step Puja Vidhi Overview + Solid Surface Cards
                item {
                    val totalVidhiMinutes = remember(localizedVidhiSteps) {
                        localizedVidhiSteps.sumOf { it.durationMinutes ?: 15 }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .testTag("puja_vidhi_overview_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${localizedVidhiSteps.size} ${strings.stepWordLabel} • ${strings.vidhiTabPrefix}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "~$totalVidhiMinutes ${strings.minutesShortLabel} • ${shastraDetails.presidingDeity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        isPujaCompleted = false
                                        isPujaModeActive = true
                                    }
                                ) {
                                    Text(
                                        text = strings.guidedModeButton,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    items = localizedVidhiSteps,
                    key = { it.stepNumber }
                ) { step ->
                    val stepSamagri = remember(step.stepNumber, localizedSamagriAll) {
                        if (localizedSamagriAll.isEmpty()) emptyList()
                        else {
                            val idx = ((step.stepNumber - 1).coerceAtLeast(0) * 2) % localizedSamagriAll.size
                            listOf(
                                localizedSamagriAll[idx],
                                localizedSamagriAll[(idx + 1) % localizedSamagriAll.size]
                            ).distinctBy { it.id }
                        }
                    }
                    val stepMantra = remember(step.stepNumber, shastraDetails.pujaMantras) {
                        shastraDetails.pujaMantras.find { it.stepNumber == step.stepNumber }
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        VidhiStepCard(
                            step = step,
                            requiredItems = stepSamagri,
                            stepMantra = stepMantra
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Screen 8 — Samagri Checklist (Blueprint Section 10)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$checkedCount / $totalSamagri ${strings.samagriTabPrefix}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = strings.offlineSavedNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (checkedCount > 0) {
                                    TextButton(
                                        onClick = { showResetChecklistDialog = true },
                                        modifier = Modifier.testTag("reset_checklist_button")
                                    ) {
                                        Text(
                                            text = strings.resetButtonLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = glassTokens.auspiciousGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Essential Items Header
                item {
                    Text(
                        text = strings.essentialItemsHeader,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = glassTokens.decorativeGold,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                items(
                    items = essentialItems,
                    key = { "essential_${it.id}" }
                ) { item ->
                    val isChecked = checkedIds.contains(item.id)
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        ChecklistRow(
                            item = item,
                            isChecked = isChecked,
                            onToggle = { viewModel.toggleSamagriItem(item.id) }
                        )
                    }
                }

                // Optional / Regional Items Header
                if (optionalItems.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.optionalItemsHeader,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = glassTokens.decorativeGold,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                        )
                    }

                    items(
                        items = optionalItems,
                        key = { "optional_${it.id}" }
                    ) { item ->
                        val isChecked = checkedIds.contains(item.id)
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            ChecklistRow(
                                item = item,
                                isChecked = isChecked,
                                onToggle = { viewModel.toggleSamagriItem(item.id) }
                            )
                        }
                    }
                }

                // Personal Family Custom Notes
                item {
                    var notesText by remember(state.samagriProgress?.notes) {
                        mutableStateOf(state.samagriProgress?.notes ?: "")
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = strings.familyNotesHeader,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = {
                                    notesText = it
                                    viewModel.updateNotes(it)
                                },
                                placeholder = { Text(strings.familyNotesPlaceholder) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("samagri_notes_input"),
                                minLines = 1,
                                maxLines = 4,
                            )
                        }
                    }
                }
            } else {
                // Tab 2: Complete 5-Stage Festival Pooja Mantras & Interactive Japa Mala Counter
                item {
                    var manualJapaCount by remember(festival.id) { mutableIntStateOf(0) }

                    val effectiveCount = if (mantraAudioState.currentMantraId != null) mantraAudioState.currentRound else manualJapaCount
                    val effectiveTarget = if (mantraAudioState.currentMantraId != null) mantraAudioState.targetRounds else selectedMantraTarget

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .testTag("puja_mantra_japa_counter_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.japaCounterHeader,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassTokens.decorativeGold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${shastraDetails.presidingDeity} ($effectiveCount / $effectiveTarget)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (effectiveCount > 0) {
                                    TextButton(
                                        onClick = {
                                            manualJapaCount = 0
                                            audioEngine.resetCounter()
                                            audioEngine.stop()
                                        },
                                        modifier = Modifier.testTag("reset_japa_counter_button")
                                    ) {
                                        Text(
                                            text = strings.resetButtonLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (effectiveCount >= effectiveTarget && effectiveTarget > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.japaCompletedBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassTokens.auspiciousGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (effectiveCount.toFloat() / effectiveTarget.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (effectiveCount >= effectiveTarget) glassTokens.auspiciousGreen else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            // Play/Pause Recitation & Manual Increment row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        if (mantraAudioState.isPlaying) {
                                            audioEngine.pause()
                                        } else if (mantraAudioState.currentMantraId != null) {
                                            audioEngine.resume()
                                        } else {
                                            val primaryMantra = shastraDetails.pujaMantras.firstOrNull()
                                            audioEngine.playMantra(
                                                id = primaryMantra?.id ?: "primary_${festival.id}",
                                                sanskritText = primaryMantra?.sanskrit ?: shastraDetails.sacredMantraSanskrit,
                                                mantraTitle = primaryMantra?.title ?: "${shastraDetails.presidingDeity} Puja Mantra",
                                                targetRounds = effectiveTarget,
                                                transliteration = primaryMantra?.transliteration ?: shastraDetails.sacredMantraTransliteration
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (mantraAudioState.isPlaying) MaterialTheme.colorScheme.primaryContainer else glassTokens.solidElevatedContainer
                                    ),
                                    modifier = Modifier.testTag("japa_chant_play_button")
                                ) {
                                    Icon(
                                        imageVector = if (mantraAudioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (mantraAudioState.isPlaying) strings.pauseMantraLabel else strings.playMantraLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (mantraAudioState.currentMantraId != null) {
                                            audioEngine.incrementManualRound()
                                        } else {
                                            manualJapaCount += 1
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("increment_japa_button")
                                ) {
                                    Text(
                                        text = "+1 ($effectiveCount)",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Row 1: Target Japa Counts (1x, 11x, 21x, 108x)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rounds:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                listOf(1, 11, 21, 108).forEach { targetOption ->
                                    FilterChip(
                                        selected = effectiveTarget == targetOption,
                                        onClick = {
                                            selectedMantraTarget = targetOption
                                            audioEngine.setTargetRounds(targetOption)
                                        },
                                        label = {
                                            Text(
                                                text = "${targetOption}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        modifier = Modifier.testTag("japa_target_chip_$targetOption")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Row 2: Speed and Tanpura Controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cadence:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                listOf(0.75f, 0.85f, 1.0f).forEach { speedOption ->
                                    FilterChip(
                                        selected = mantraAudioState.speed == speedOption,
                                        onClick = { audioEngine.setSpeed(speedOption) },
                                        label = {
                                            Text(
                                                text = if (speedOption == 0.85f) "0.85x (Pandit)" else "${speedOption}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        modifier = Modifier.testTag("japa_speed_chip_${(speedOption * 100).toInt()}")
                                    )
                                }

                                FilterChip(
                                    selected = mantraAudioState.isTanpuraEnabled,
                                    onClick = { audioEngine.toggleTanpura() },
                                    label = {
                                        Text(
                                            text = if (mantraAudioState.isTanpuraEnabled) "🪕 Tanpura" else "Tanpura Off",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    modifier = Modifier.testTag("japa_tanpura_chip")
                                )

                                FilterChip(
                                    selected = mantraAudioState.isTablaEnabled,
                                    onClick = { audioEngine.toggleTabla() },
                                    label = {
                                        Text(
                                            text = if (mantraAudioState.isTablaEnabled) "🥁 Tabla" else "Tabla Off",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    modifier = Modifier.testTag("japa_tabla_chip")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Ninada Neural Recitation Engine Status & Quick Pack Action
                            val sanskritPackStatus = ninadaPackStatuses["sa"] ?: NinadaPackStatus.NotDownloaded
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = glassTokens.solidElevatedContainer,
                                border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ninada_voice_status_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mantraAudioState.activeVoiceEngineLabel,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                softWrap = true
                                            )
                                            val statusSubtext = when (sanskritPackStatus) {
                                                is NinadaPackStatus.Ready ->
                                                    "Ninada Sanskrit 24 kHz neural model ready offline (${String.format(Locale.US, "%.1f", sanskritPackStatus.fileSizeMb)} MB)"
                                                is NinadaPackStatus.Downloading ->
                                                    sanskritPackStatus.statusMessage
                                                is NinadaPackStatus.Error ->
                                                    "Using Device Neural TTS · ${sanskritPackStatus.message}"
                                                NinadaPackStatus.NotDownloaded ->
                                                    "Using Device Neural TTS · Download Ninada Sanskrit (~22 MB) for studio chanting"
                                            }
                                            Text(
                                                text = mantraAudioState.audioStatusMessage ?: statusSubtext,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                softWrap = true
                                            )
                                        }
                                        if (sanskritPackStatus is NinadaPackStatus.NotDownloaded ||
                                            sanskritPackStatus is NinadaPackStatus.Error
                                        ) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = { audioEngine.downloadNinadaPack("sa") },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 40.dp)
                                                    .testTag("download_ninada_sanskrit_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download Ninada Sanskrit voice",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Ninada",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (sanskritPackStatus is NinadaPackStatus.Downloading) {
                                        LinearProgressIndicator(
                                            progress = { sanskritPackStatus.progress.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(
                    items = shastraDetails.pujaMantras,
                    key = { it.id }
                ) { mantra ->
                    var isMantraExpanded by rememberSaveable(mantra.id) {
                        mutableStateOf(mantra.stepNumber == 1)
                    }
                    val chevronRotation by animateFloatAsState(
                        targetValue = if (isMantraExpanded) 180f else 0f,
                        label = "mantra_card_chevron_${mantra.id}"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .testTag("festival_puja_mantra_card_${mantra.stepNumber}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isMantraExpanded = !isMantraExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = "${strings.stepWordLabel} ${mantra.stepNumber} • ${mantra.ritualStage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mantra.chantCountRecommendation,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = glassTokens.decorativeGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = glassTokens.decorativeGold,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(chevronRotation)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mantra.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { isMantraExpanded = !isMantraExpanded }
                            )

                            AnimatedVisibility(visible = isMantraExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = glassTokens.solidElevatedContainer,
                                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = mantra.sanskrit,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mantra.transliteration,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${strings.meaningPrefix}: ${mantra.meaning}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    val isThisMantraPlaying = mantraAudioState.isPlaying && mantraAudioState.currentMantraId == mantra.id
                                    val isThisMantraActive = mantraAudioState.currentMantraId == mantra.id
                                    val meaningAudioId = "${mantra.id}_meaning_${strings.language.code}"
                                    val isMeaningPlaying = mantraAudioState.isPlaying && mantraAudioState.currentMantraId == meaningAudioId
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                if (isThisMantraPlaying) {
                                                    audioEngine.pause()
                                                } else if (isThisMantraActive && !mantraAudioState.isPlaying) {
                                                    audioEngine.resume()
                                                } else {
                                                    audioEngine.playMantra(
                                                        id = mantra.id,
                                                        sanskritText = mantra.sanskrit,
                                                        mantraTitle = mantra.title,
                                                        targetRounds = selectedMantraTarget,
                                                        transliteration = mantra.transliteration
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = if (isThisMantraPlaying) MaterialTheme.colorScheme.primaryContainer else glassTokens.solidElevatedContainer
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .defaultMinSize(minHeight = 44.dp)
                                                .testTag("play_mantra_button_${mantra.stepNumber}")
                                        ) {
                                            Icon(
                                                imageVector = if (isThisMantraPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isThisMantraPlaying) strings.pauseMantraLabel else strings.playMantraLabel,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (mantra.meaning.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (isMeaningPlaying) {
                                                        audioEngine.stop()
                                                    } else {
                                                        audioEngine.playLocalizedNarration(
                                                            id = meaningAudioId,
                                                            title = "${mantra.title} (${strings.language.nativeName})",
                                                            text = mantra.meaning,
                                                            language = strings.language
                                                        )
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 44.dp)
                                                    .testTag("listen_meaning_button_${mantra.stepNumber}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isMeaningPlaying) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                                    contentDescription = "Listen meaning in ${strings.language.englishName}",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isMeaningPlaying) "Stop" else strings.language.nativeName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (isThisMantraActive) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = glassTokens.auspiciousGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${strings.roundLabel} ${mantraAudioState.currentRound}/${mantraAudioState.targetRounds}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = glassTokens.auspiciousGreen,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
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

        if (mantraAudioState.isPlaying || mantraAudioState.currentRound > 0) {
            FloatingMantraMiniPlayer(
                state = mantraAudioState,
                onTogglePlay = {
                    if (mantraAudioState.isPlaying) audioEngine.pause() else audioEngine.resume()
                },
                onStop = { audioEngine.stop() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
        }

        // Top status bar scrim (when hero visible) transitioning to a pinned solid top bar when hero is fully hidden
        val collapsedHeaderAlpha = ((heroCollapseProgress - 0.68f) / 0.32f).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = (1f - collapsedHeaderAlpha).coerceIn(0f, 1f) }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA000000),
                            Color(0x40000000),
                            Color.Transparent
                        )
                    )
                )
                .zIndex(9f)
        )

        val backButtonSoftPress = rememberSoftPressController()
        val saveSoftPress = rememberSoftPressController()
        val shareSoftPress = rememberSoftPressController()

        // Floating Top Navigation Bar (Always on top; shows condensed festival title once hero hides)
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = collapsedHeaderAlpha * 0.96f),
            shadowElevation = if (collapsedHeaderAlpha > 0.5f) 4.dp else 0.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(10f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 860.dp)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { backButtonSoftPress.triggerSoftClick(onBack) },
                    interactionSource = backButtonSoftPress.interactionSource,
                    shape = CircleShape,
                    color = Color(0xF2160C10), // Solid deep obsidian sanctum container
                    border = BorderStroke(1.8.dp, Color(0xFFE5B84B)), // Bright sacred gold rim
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .softPressCardEffect(backButtonSoftPress)
                        .testTag("detail_back_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backButton,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Condensed festival title fades in cleanly when the hero artwork has scrolled out of view
                Text(
                    text = localizedFestivalTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .graphicsLayer { alpha = collapsedHeaderAlpha }
                        .testTag("detail_collapsed_title")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = { saveSoftPress.triggerSoftClick { viewModel.toggleSavedFestival() } },
                        interactionSource = saveSoftPress.interactionSource,
                        shape = CircleShape,
                        color = Color(0xF2160C10),
                        border = BorderStroke(1.5.dp, Color(0xFFE5B84B).copy(alpha = 0.85f)),
                        shadowElevation = 8.dp,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .softPressCardEffect(saveSoftPress)
                            .testTag("detail_save_button")
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (state.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (state.isSaved) "Saved festival" else "Save festival",
                                tint = if (state.isSaved) Color(0xFFFF5252) else Color(0xFFFFD54F),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { shareSoftPress.triggerSoftClick { showShareDialog = true } },
                        interactionSource = shareSoftPress.interactionSource,
                        shape = CircleShape,
                        color = Color(0xF2160C10),
                        border = BorderStroke(1.5.dp, Color(0xFFE5B84B).copy(alpha = 0.85f)),
                        shadowElevation = 8.dp,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .softPressCardEffect(shareSoftPress)
                            .testTag("detail_share_button")
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = strings.shareGreeting,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating glass mini player for active mantra recitation across tabs.
 */
@Composable
private fun FloatingMantraMiniPlayer(
    state: MantraPlaybackState,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("floating_mantra_mini_player"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.mantraTitle.ifBlank { "Mantra Chanting" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${strings.roundLabel} ${state.currentRound}/${state.targetRounds} • ${state.speed}x" +
                            (if (state.isTanpuraEnabled) " • Tanpura" else "") +
                            (if (state.isTablaEnabled) " • Tabla" else ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = glassTokens.decorativeGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.testTag("floating_mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) strings.pauseMantraLabel else strings.playMantraLabel,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.testTag("floating_mini_player_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.stopMantraLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Structured two-column table row for the Festival Muhurat & Panchang Table.
 */
@Composable
private fun MuhuratTableRow(
    label: String,
    value: String,
    subtitle: String? = null,
    isAlternate: Boolean = false,
    isHighlighted: Boolean = false,
    isLast: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val rowBg = when {
        isHighlighted -> glassTokens.auspiciousGreen.copy(alpha = 0.11f)
        isAlternate -> glassTokens.solidElevatedContainer.copy(alpha = 0.65f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) glassTokens.auspiciousGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.42f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(0.58f)
            ) {
                Text(
                    text = value,
                    style = if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                    color = valueColor
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun MuhuratSectionBlock(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    isAlternate: Boolean = false,
    isLast: Boolean = false,
    content: @Composable () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val rowBg = if (isAlternate) glassTokens.solidElevatedContainer.copy(alpha = 0.65f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun MuhuratTimingItemRow(
    label: String,
    timeRange: String,
    accentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = timeRange,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = accentColor
        )
    }
}

/**
 * Blueprint Section 9 (Screen 7): Guided Puja Mode & Calm Completion Screen.
 */
@Composable
private fun GuidedPujaModeScreen(
    festivalId: String,
    festivalName: String,
    shastraDetails: FestivalShastraDetails,
    steps: List<VidhiStep>,
    samagriItems: List<SamagriItem>,
    checkedSamagriIds: Set<String>,
    currentIndex: Int,
    isCompleted: Boolean,
    audioEngine: MantraAudioEngine,
    mantraAudioState: MantraPlaybackState,
    onToggleSamagri: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinishLater: () -> Unit,
    onShareCompleted: () -> Unit,
    onClose: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val step = steps[currentIndex.coerceIn(0, steps.lastIndex)]
    val stepProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else (currentIndex + 1).toFloat() / steps.size.coerceAtLeast(1),
        label = "puja_step_progress"
    )

    val stepItems = remember(currentIndex, samagriItems) {
        if (samagriItems.isEmpty()) emptyList()
        else {
            val start = (currentIndex * 2) % samagriItems.size
            (0 until 3.coerceAtMost(samagriItems.size)).map { offset ->
                samagriItems[(start + offset) % samagriItems.size]
            }.distinctBy { it.id }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guided_puja_mode_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar + Thin Track Progress Indicator
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            onClick = onClose,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            border = BorderStroke(1.2.dp, glassTokens.decorativeGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("guided_puja_exit_button")
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Puja Guide",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$festivalName · ${strings.vidhiTabPrefix}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isCompleted) "${strings.completedStepsSummaryHeader} (${steps.size})" else "${strings.stepWordLabel} ${step.stepNumber} / ${steps.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!isCompleted) {
                        TextButton(onClick = onFinishLater) {
                            Text(
                                text = strings.finishLaterLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) glassTokens.auspiciousGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (isCompleted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.5.dp, glassTokens.auspiciousGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(glassTokens.auspiciousGreen.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = glassTokens.auspiciousGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.shubhSampannaTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${strings.shubhSampannaTitle} · $festivalName (${steps.size} ${strings.stepWordLabel})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.completedStepsSummaryHeader,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = glassTokens.decorativeGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        steps.forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = glassTokens.auspiciousGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${s.stepNumber}. ${s.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onShareCompleted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.shareFestivalBlessingButton, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(strings.returnToDetailsButton, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(vertical = 14.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                    border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            FestivalArtworkBanner(
                                festivalId = festivalId,
                                modifier = Modifier.fillMaxSize()
                            )
                            LiquidGlassSurface(
                                cornerRadius = 12.dp,
                                strongFill = true,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "STEP ${step.stepNumber} OF ${steps.size}" +
                                        (step.durationMinutes?.let { " · ~$it MIN" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = step.body,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            val activeStepMantra = remember(step.stepNumber, shastraDetails.pujaMantras) {
                                shastraDetails.pujaMantras.find { it.stepNumber == step.stepNumber }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (activeStepMantra != null) {
                                            "STEP ${step.stepNumber} MANTRA • ${activeStepMantra.ritualStage.uppercase(Locale.getDefault())}"
                                        } else {
                                            "SACRED MANTRA (${shastraDetails.presidingDeity.uppercase(Locale.getDefault())})"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = glassTokens.decorativeGold
                                    )
                                    if (activeStepMantra != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${activeStepMantra.title} (${activeStepMantra.chantCountRecommendation})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activeStepMantra?.sanskrit ?: shastraDetails.sacredMantraSanskrit,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeStepMantra?.transliteration ?: shastraDetails.sacredMantraTransliteration,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.meaningPrefix}: ${activeStepMantra?.meaning ?: shastraDetails.sacredMantraMeaning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    val targetMantraId = activeStepMantra?.id ?: "step_${step.stepNumber}"
                                    val targetSanskrit = activeStepMantra?.sanskrit ?: shastraDetails.sacredMantraSanskrit
                                    val targetTitle = activeStepMantra?.title ?: "${shastraDetails.presidingDeity} Step ${step.stepNumber}"
                                    val isStepMantraPlaying = mantraAudioState.isPlaying && mantraAudioState.currentMantraId == targetMantraId
                                    val isStepMantraActive = mantraAudioState.currentMantraId == targetMantraId

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = glassTokens.solidElevatedContainer,
                                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    if (isStepMantraPlaying) {
                                                        audioEngine.pause()
                                                    } else if (isStepMantraActive && !mantraAudioState.isPlaying) {
                                                        audioEngine.resume()
                                                    } else {
                                                        audioEngine.playMantra(
                                                            id = targetMantraId,
                                                            sanskritText = targetSanskrit,
                                                            mantraTitle = targetTitle,
                                                            targetRounds = 1,
                                                            transliteration = activeStepMantra?.transliteration ?: shastraDetails.sacredMantraTransliteration
                                                        )
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (isStepMantraPlaying) MaterialTheme.colorScheme.primaryContainer else glassTokens.solidCardContainer
                                                ),
                                                modifier = Modifier.testTag("guided_puja_play_mantra_button")
                                            ) {
                                                Icon(
                                                    imageVector = if (isStepMantraPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isStepMantraPlaying) strings.pauseMantraLabel else strings.playMantraLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (isStepMantraActive) {
                                                    Text(
                                                        text = "${strings.roundLabel} ${mantraAudioState.currentRound}/${mantraAudioState.targetRounds}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = glassTokens.decorativeGold,
                                                        modifier = Modifier.testTag("guided_puja_mantra_round_indicator")
                                                    )
                                                }

                                                FilterChip(
                                                    selected = mantraAudioState.speed != 1.0f,
                                                    onClick = {
                                                        val nextSpeed = when (mantraAudioState.speed) {
                                                            0.75f -> 1.0f
                                                            1.0f -> 1.25f
                                                            else -> 0.75f
                                                        }
                                                        audioEngine.setSpeed(nextSpeed)
                                                    },
                                                    label = {
                                                        Text(
                                                            text = "${mantraAudioState.speed}x",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )

                                                FilterChip(
                                                    selected = mantraAudioState.isTanpuraEnabled,
                                                    onClick = { audioEngine.toggleTanpura() },
                                                    label = {
                                                        Text(
                                                            text = if (mantraAudioState.isTanpuraEnabled) "🪕" else "Off",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )

                                                FilterChip(
                                                    selected = mantraAudioState.isTablaEnabled,
                                                    onClick = { audioEngine.toggleTabla() },
                                                    label = {
                                                        Text(
                                                            text = if (mantraAudioState.isTablaEnabled) "🥁" else "Off",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (stepItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${strings.samagriTabPrefix} (Step ${step.stepNumber})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = glassTokens.decorativeGold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                stepItems.forEach { item ->
                                    val checked = checkedSamagriIds.contains(item.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onToggleSamagri(item.id) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { onToggleSamagri(item.id) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name + (item.quantity?.let { " ($it)" } ?: ""),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Step Navigation Controls: [ Previous ] [ Next Step ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = currentIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(strings.backButton, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (currentIndex == steps.lastIndex) strings.completePujaButton else strings.nextPrefix,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentIndex < steps.lastIndex) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
