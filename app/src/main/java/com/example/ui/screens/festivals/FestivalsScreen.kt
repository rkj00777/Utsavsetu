package com.example.ui.screens.festivals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID
import com.example.platform.ads.FestivalFeedAdPlacer
import com.example.platform.ads.FestivalFeedEntry
import com.example.ui.components.AdMobNativeAdCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.RegionalTradition
import com.example.ui.components.AnimatedCollapsibleSection
import com.example.ui.components.FestivalArtworkBanner
import com.example.ui.components.FestivalCard
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.ShareFestivalDialog
import com.example.ui.components.rememberSoftPressController
import com.example.ui.components.softPressCardEffect
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.TulsiGreen
import com.example.ui.theme.UtsavLocalizationEngine
import java.time.YearMonth
import java.util.Locale

/**
 * Blueprint Section 7 (Screen 5): Festival Discovery.
 * - Search bar with Hinglish/Tanglish suggestions & clear button
 * - Featured Festival hero card
 * - Browse by category (Coming soon, Major, Fasts, Saved) & Regional filters
 * - Helpful empty state ("Couldn't find that festival. Try another spelling or browse all festivals.")
 */
@Composable
fun FestivalsScreen(
    viewModel: FestivalsViewModel,
    onNavigateToFestival: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    var shareTargetOccurrence by remember { mutableStateOf<FestivalOccurrence?>(null) }

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
            .testTag("festivals_screen"),
        contentPadding = PaddingValues(bottom = 128.dp)
    ) {
        // 1. Header & Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = strings.tabFestivals,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${strings.upcomingFestivalsSubtitlePrefix} ${UtsavLocalizationEngine.localizeCityName(state.hostCity.name, strings.language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Liquid Glass Search Bar
                LiquidGlassSurface(
                    cornerRadius = 20.dp,
                    strongFill = true,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("festival_search_input"),
                        placeholder = {
                            Text(
                                text = strings.searchFestivalsPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // 2. Browse by Category Chips + Collapsible Regional & Quick-Search Filters
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DiscoveryCategory.values()) { category ->
                        FilterChip(
                            selected = state.activeCategory == category,
                            onClick = { viewModel.setCategory(category) },
                            label = {
                                Text(
                                    text = UtsavLocalizationEngine.localizeCategoryLabel(category.label, strings.language),
                                    fontWeight = if (state.activeCategory == category) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    val activeRegionText = state.activeTraditionFilter?.let {
                        UtsavLocalizationEngine.localizeTraditionName(it, strings.language)
                    } ?: strings.allRegionsLabel
                    AnimatedCollapsibleSection(
                        title = strings.traditionsSectionHeader,
                        subtitle = activeRegionText,
                        initiallyExpanded = state.activeTraditionFilter != null,
                        testTagName = "festivals_region_filter_dropdown"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = state.activeTraditionFilter == null,
                                        onClick = { viewModel.setTraditionFilter(null) },
                                        label = { Text(strings.allRegionsLabel, maxLines = 1) },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                                items(RegionalTradition.values()) { tradition ->
                                    FilterChip(
                                        selected = state.activeTraditionFilter == tradition,
                                        onClick = { viewModel.setTraditionFilter(tradition) },
                                        label = {
                                            Text(
                                                text = UtsavLocalizationEngine.localizeTraditionName(tradition, strings.language),
                                                maxLines = 1
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }

                            // Quick Search Suggestions inside dropdown
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val suggestions = listOf(
                                    "diwali_lakshmi_puja" to "Deepavali",
                                    "bhai_dooj" to "Bhai Dooj",
                                    "govardhan_puja" to "Govardhan Puja",
                                    "chhath_puja" to "Chhath Puja",
                                    "makar_sankranti" to "Pongal / Sankranti",
                                    "gudi_padwa_ugadi" to "Ugadi / Gudi Padwa",
                                    "hartalika_teej" to "Hartalika Teej"
                                )
                                items(suggestions) { (festId, fallbackName) ->
                                    val localizedSuggestion = UtsavLocalizationEngine.localizeFestivalName(
                                        festivalId = festId,
                                        englishName = fallbackName,
                                        nativeDevanagari = fallbackName,
                                        language = strings.language
                                    )
                                    SuggestionChip(
                                        onClick = { viewModel.onSearchQueryChanged(fallbackName.substringBefore(" /")) },
                                        label = {
                                            Text(
                                                text = localizedSuggestion,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Featured Festival Card (shown when not actively typing a search query)
        if (!state.isSearching && state.featuredOccurrence != null && state.activeCategory == DiscoveryCategory.ALL) {
            val featured = state.featuredOccurrence!!
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = strings.featuredCelebrationHeader,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = glassTokens.decorativeGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FeaturedDiscoveryCard(
                        occurrence = featured,
                        isSaved = state.savedFestivalIds.contains(featured.festival.id),
                        onToggleSave = { viewModel.toggleSavedFestival(featured.festival.id) },
                        onExplore = { onNavigateToFestival(featured.festival.id) }
                    )
                }
            }
        }

        // 4. Search Results or Grouped Catalogue
        if (state.isSearching) {
            if (state.onlineStatusMessage != null || state.isSearchingOnline) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = glassTokens.auspiciousGreen.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, glassTokens.auspiciousGreen.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isSearchingOnline) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = glassTokens.auspiciousGreen
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = glassTokens.auspiciousGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (state.isSearchingOnline) {
                                    strings.searchingOnlineLabel
                                } else {
                                    state.onlineStatusMessage ?: ""
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (state.searchResults.isEmpty()) {
                // Blueprint Section 7.1 Empty State + Live Online Search & Update App
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.festivalNotFoundOffline,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search online Panchang & Wikipedia knowledge bases to automatically add '${state.searchQuery}' with local city timings and regional names.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.searchOnlineAndUpdateApp() },
                                enabled = !state.isSearchingOnline,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("search_online_update_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isSearchingOnline) strings.searchingOnlineLabel else strings.searchOnlineButton,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.onSearchQueryChanged("") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.browseAllFestivals, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val searchFeedEntries = FestivalFeedAdPlacer.interleaveFlat(
                    feedKey = "festivals_search",
                    items = state.searchResults,
                    itemKeyProvider = { "${it.festival.id}_${it.date}" }
                )
                items(
                    items = searchFeedEntries,
                    key = { entry ->
                        when (entry) {
                            is FestivalFeedEntry.FestivalItem -> entry.itemKey
                            is FestivalFeedEntry.NativeAdSlot -> entry.itemKey
                            is FestivalFeedEntry.SectionHeader -> entry.headerKey
                        }
                    }
                ) { entry ->
                    when (entry) {
                        is FestivalFeedEntry.FestivalItem -> {
                            val occurrence = entry.item
                            val matchedAlias = state.matchedAliases[occurrence.festival.id]
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                FestivalCard(
                                    occurrence = occurrence,
                                    referenceDate = com.example.panchang.PanchangEngine.localDateForCity(state.hostCity),
                                    matchedRegionalName = matchedAlias,
                                    onClick = { onNavigateToFestival(occurrence.festival.id) },
                                    onShareClick = { shareTargetOccurrence = occurrence }
                                )
                            }
                        }
                        is FestivalFeedEntry.NativeAdSlot -> {
                            AdMobNativeAdCard(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                                slotKey = entry.registrySlotKey,
                                showDismissButton = true
                            )
                        }
                        is FestivalFeedEntry.SectionHeader -> Unit
                    }
                }

                // Online Search & Live Catalog Updater Footer even when partial local results exist
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
                        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.lookingForAnotherFestival,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Search online & add '${state.searchQuery}' to your app catalog",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.searchOnlineAndUpdateApp() },
                                enabled = !state.isSearchingOnline,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("search_online_footer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.updateCatalogButton, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            if (state.groupedFestivals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (state.activeCategory == DiscoveryCategory.SAVED) {
                                    strings.noSavedFestivals
                                } else {
                                    strings.noFestivalsFound
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    viewModel.setCategory(DiscoveryCategory.ALL)
                                    viewModel.setTraditionFilter(null)
                                }
                            ) {
                                Text(strings.browseAllFestivals, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                val categoryFeedKey = "festivals_category_${state.activeCategory.name.lowercase(Locale.US)}"
                val groupedFeedEntries = FestivalFeedAdPlacer.interleaveGrouped(
                    feedKey = categoryFeedKey,
                    groups = state.groupedFestivals,
                    headerKeyProvider = { group -> "${group.monthName}_${group.year}" },
                    headerTitleProvider = { group ->
                        val firstOccDate = group.occurrences.firstOrNull()?.date
                        if (firstOccDate != null) {
                            UtsavLocalizationEngine.formatYearMonth(
                                YearMonth.of(firstOccDate.year, firstOccDate.monthValue),
                                "MMMM yyyy",
                                strings.language
                            )
                        } else {
                            "${group.monthName} ${group.year}"
                        }
                    },
                    itemsProvider = { group -> group.occurrences },
                    itemKeyProvider = { group, occ -> "${group.monthName}_${group.year}_${occ.festival.id}_${occ.date}" }
                )

                items(
                    items = groupedFeedEntries,
                    key = { entry ->
                        when (entry) {
                            is FestivalFeedEntry.SectionHeader -> entry.headerKey
                            is FestivalFeedEntry.FestivalItem -> entry.itemKey
                            is FestivalFeedEntry.NativeAdSlot -> entry.itemKey
                        }
                    }
                ) { entry ->
                    when (entry) {
                        is FestivalFeedEntry.SectionHeader -> {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)
                            )
                        }
                        is FestivalFeedEntry.FestivalItem -> {
                            val occurrence = entry.item
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                FestivalCard(
                                    occurrence = occurrence,
                                    referenceDate = com.example.panchang.PanchangEngine.localDateForCity(state.hostCity),
                                    onClick = { onNavigateToFestival(occurrence.festival.id) },
                                    onShareClick = { shareTargetOccurrence = occurrence }
                                )
                            }
                        }
                        is FestivalFeedEntry.NativeAdSlot -> {
                            AdMobNativeAdCard(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                                slotKey = entry.registrySlotKey,
                                showDismissButton = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedDiscoveryCard(
    occurrence: FestivalOccurrence,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onExplore: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    val strings = LocalUtsavStrings.current
    val dateFormatted = remember(occurrence.date, strings.language) {
        UtsavLocalizationEngine.formatDate(occurrence.date, "EEEE, d MMMM yyyy", strings.language)
    }
    val softPress = rememberSoftPressController()

    Card(
        onClick = { softPress.triggerSoftClick(onExplore) },
        interactionSource = softPress.interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .softPressCardEffect(softPress)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.5f) else Color(0xFFD6C3AA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
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
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                LiquidGlassSurface(
                    cornerRadius = 18.dp,
                    strongFill = true,
                    elevation = 2.dp,
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(38.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Unsave festival" else "Save festival",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = UtsavLocalizationEngine.localizeFestivalName(
                        festivalId = occurrence.festival.id,
                        englishName = occurrence.festival.nameEnglish,
                        nativeDevanagari = occurrence.festival.nameNative,
                        language = strings.language
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = UtsavLocalizationEngine.localizeSignificance(
                        festivalId = occurrence.festival.id,
                        englishName = occurrence.festival.nameEnglish,
                        englishSignificance = occurrence.festival.significance,
                        language = strings.language
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.exploreVidhiButton,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
