package com.example.ui.screens.setup

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AppLanguage
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import com.example.platform.auth.AuthHelper
import com.example.platform.location.LocationCityResolver
import com.example.ui.components.FestivalArtworkBanner
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.TraditionChip
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.TulsiGreen
import com.example.ui.theme.stringsForLanguage
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onSetupComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val glassTokens = LocalLiquidGlassTokens.current
    var cityMenuExpanded by remember { mutableStateOf(false) }

    // Immediately compute localized strings for the currently selected language so switching
    // language on the Welcome/Language screen updates the entire UI with zero frame lag.
    val strings = remember(state.selectedLanguage) {
        Locale.setDefault(Locale.forLanguageTag(state.selectedLanguage.code))
        stringsForLanguage(state.selectedLanguage)
    }
    val stepTitles = strings.stepTitles

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectCityFromLocation(context)
        } else {
            viewModel.detectCityFromLocation(context)
        }
    }

    BackHandler(enabled = state.currentStep > 0) {
        viewModel.previousStep()
    }

    val topCities = remember { City.TOP_CITIES }
    val additionalCities = remember { City.ALL_CITIES.filter { top -> topCities.none { it.id == top.id } } }

    val filteredCities = remember(state.citySearchQuery) {
        if (state.citySearchQuery.isBlank()) {
            emptyList()
        } else {
            val q = state.citySearchQuery.trim()
            City.searchCities(q)
        }
    }

    CompositionLocalProvider(LocalUtsavStrings provides strings) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("setup_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            if (state.currentStep == 0) {
                // Full-Screen Cinematic Welcome Composition (Reworked per Gate 3 Visual Direction)
                WelcomeComposition(
                    state = state,
                    strings = strings,
                    onGetStarted = { viewModel.nextStep() },
                    onExploreDirectly = { viewModel.completeSetup(onSetupComplete) },
                    onSelectLanguage = { viewModel.selectLanguage(it) }
                )
            } else {
                // 4-Step Setup Wizard Sequence (1. City/Location, 2. Traditions, 3. Reminders, 4. Cloud Sync)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Artwork Banner with Back Button & Explore Skip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        FestivalArtworkBanner(
                            festivalId = "welcome_utsavsetu",
                            modifier = Modifier.fillMaxSize(),
                            isHeroBackground = true
                        )

                        // Top Navigation Bar: Back Arrow + Step Indicator + Explore Skip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassSurface(
                                cornerRadius = 18.dp,
                                elevation = 4.dp,
                                onClick = { viewModel.previousStep() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous step",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Step ${state.currentStep}/4",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            LiquidGlassSurface(
                                cornerRadius = 18.dp,
                                elevation = 4.dp,
                                onClick = { viewModel.completeSetup(onSetupComplete) },
                                modifier = Modifier.testTag("explore_first_button")
                            ) {
                                Text(
                                    text = strings.exploreFirstCta,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        LiquidGlassSurface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            cornerRadius = 24.dp,
                            elevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ॐ · UTSAVSETU · SETUP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glassTokens.decorativeGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stepTitles.getOrElse(state.currentStep) { "Preferences" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Interactive 4-Step Progress Pills (Steps 1 to 4)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..4).forEach { stepIndex ->
                            val isSelected = state.currentStep == stepIndex
                            val isDone = stepIndex < state.currentStep
                            val title = stepTitles.getOrElse(stepIndex) { "Step $stepIndex" }
                            Surface(
                                onClick = { viewModel.setStep(stepIndex) },
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else -> glassTokens.solidElevatedContainer
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("setup_step_pill_$stepIndex")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$stepIndex. $title",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                    AnimatedContent(
                        targetState = state.currentStep,
                        label = "onboarding_wizard_step"
                    ) { step ->
                        when (step) {
                            0 -> {
                                // Step 0: Welcome Highlights + Quick Language Switcher
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = strings.welcomeTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = strings.welcomeBody,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Instant Language Bar right on Welcome screen too
                                        Text(
                                            text = strings.stepLanguageTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AppLanguage.values().forEach { lang ->
                                                val selected = state.selectedLanguage == lang
                                                FilterChip(
                                                    selected = selected,
                                                    onClick = { viewModel.selectLanguage(lang) },
                                                    label = {
                                                        Text(
                                                            text = "${lang.nativeName} (${lang.englishName})",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("welcome_lang_chip_${lang.code}")
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            WelcomeFeaturePill(
                                                icon = Icons.Default.CalendarMonth,
                                                label = strings.welcomePillFestivals,
                                                modifier = Modifier.weight(1f)
                                            )
                                            WelcomeFeaturePill(
                                                icon = Icons.Default.WbSunny,
                                                label = strings.welcomePillPanchang,
                                                modifier = Modifier.weight(1f)
                                            )
                                            WelcomeFeaturePill(
                                                icon = Icons.Default.AutoAwesome,
                                                label = strings.welcomePillVidhi,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = glassTokens.solidElevatedContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = glassTokens.auspiciousGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = strings.welcomeOfflineNote,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // Step 1: Language Selection (Immediately changes entire system/app language)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = strings.stepLanguageTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = strings.stepLanguageSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        AppLanguage.values().forEach { lang ->
                                            val isSelected = state.selectedLanguage == lang
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else glassTokens.solidElevatedContainer.copy(alpha = 0.5f)
                                                    )
                                                    .clickable { viewModel.selectLanguage(lang) }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    .testTag("language_row_${lang.code}"),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "${lang.nativeName} · ${lang.englishName}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = lang.sampleGreeting,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.selectLanguage(lang) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // Step 2: City Selection with GPS Location-Based Detection + Top Cities + Search
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = strings.stepCityTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = strings.stepCitySubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Prominent Location-Based City Selection (GPS) Option
                                        Button(
                                            onClick = {
                                                if (LocationCityResolver.hasLocationPermission(context)) {
                                                    viewModel.detectCityFromLocation(context)
                                                } else {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                }
                                            },
                                            enabled = !state.isDetectingLocation,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("detect_location_gps_button")
                                        ) {
                                            if (state.isDetectingLocation) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = strings.detectingLocationLabel,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.MyLocation,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = strings.useCurrentLocationGps,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        if (state.locationStatusMessage != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = glassTokens.auspiciousGreen.copy(alpha = 0.14f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationSearching,
                                                        contentDescription = null,
                                                        tint = glassTokens.auspiciousGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = state.locationStatusMessage ?: "",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = state.citySearchQuery,
                                            onValueChange = { viewModel.onCitySearchQueryChanged(it) },
                                            placeholder = { Text(strings.citySearchPlaceholder) },
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                            trailingIcon = {
                                                if (state.citySearchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { viewModel.onCitySearchQueryChanged("") }) {
                                                        Icon(Icons.Default.Clear, contentDescription = "Clear city search")
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("city_search_input")
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        if (state.citySearchQuery.isNotBlank()) {
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                filteredCities.forEach { city ->
                                                    val selected = state.selectedCity.id == city.id
                                                    FilterChip(
                                                        selected = selected,
                                                        onClick = { viewModel.selectCity(city) },
                                                        label = {
                                                            Text(
                                                                text = "${city.name} (${city.state})",
                                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        },
                                                        leadingIcon = if (selected) {
                                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null
                                                    )
                                                }
                                            }
                                        } else {
                                            // Top Metro Cities
                                            Text(
                                                text = strings.topCitiesHeader,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = glassTokens.decorativeGold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                topCities.forEach { city ->
                                                    val selected = state.selectedCity.id == city.id
                                                    FilterChip(
                                                        selected = selected,
                                                        onClick = { viewModel.selectCity(city) },
                                                        label = {
                                                            Text(
                                                                text = city.name,
                                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        },
                                                        leadingIcon = if (selected) {
                                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Additional Indian Cities & Sacred Pilgrimage Towns
                                            Text(
                                                text = strings.moreCitiesHeader,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = glassTokens.decorativeGold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                additionalCities.forEach { city ->
                                                    val selected = state.selectedCity.id == city.id
                                                    FilterChip(
                                                        selected = selected,
                                                        onClick = { viewModel.selectCity(city) },
                                                        label = {
                                                            Text(
                                                                text = city.name,
                                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        },
                                                        leadingIcon = if (selected) {
                                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                        } else null
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedCard(
                                                onClick = { cityMenuExpanded = true },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("city_picker_card"),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.outlinedCardColors(
                                                    containerColor = glassTokens.solidElevatedContainer
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (state.selectedCity.isLocationDetected) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = "${strings.selectedCityPrefix}: ${state.selectedCity.name}",
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${state.selectedCity.state} · ${String.format(Locale.US, "%.2f°N, %.2f°E", state.selectedCity.latitude, state.selectedCity.longitude)}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = strings.changeButton,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = cityMenuExpanded,
                                                onDismissRequest = { cityMenuExpanded = false },
                                                modifier = Modifier
                                                    .widthIn(min = 220.dp, max = 300.dp)
                                                    .heightIn(max = 280.dp)
                                            ) {
                                                City.ALL_CITIES.forEach { city ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "${city.name} (${city.state})",
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        },
                                                        onClick = {
                                                            viewModel.selectCity(city)
                                                            cityMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // Step 3: Regional Tradition & Calendar Convention
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = strings.stepTraditionTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = strings.stepTraditionSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RegionalTradition.values().forEach { tradition ->
                                                val isSelected = state.selectedTraditions.contains(tradition)
                                                TraditionChip(
                                                    tradition = tradition,
                                                    isSelected = isSelected,
                                                    onToggle = { viewModel.toggleTradition(tradition) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // Step 4: Reminders, Cloud Sync & DPDP Privacy
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Text(
                                                text = strings.stepRemindersTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = strings.stepRemindersSubtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(strings.dayBeforeReminderTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(strings.dayBeforeReminderDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Switch(
                                                    checked = state.dayBeforeReminder,
                                                    onCheckedChange = {
                                                        viewModel.updateReminderPrefs(it, state.morningOfReminder, state.muhuratStartReminder)
                                                    }
                                                )
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(strings.festivalMorningReminderTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(strings.festivalMorningReminderDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Switch(
                                                    checked = state.morningOfReminder,
                                                    onCheckedChange = {
                                                        viewModel.updateReminderPrefs(state.dayBeforeReminder, it, state.muhuratStartReminder)
                                                    }
                                                )
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(strings.muhuratStartReminderTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(strings.muhuratStartReminderDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Switch(
                                                    checked = state.muhuratStartReminder,
                                                    onCheckedChange = {
                                                        viewModel.updateReminderPrefs(state.dayBeforeReminder, state.morningOfReminder, it)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Privacy & DPDP Notice
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
                                        border = BorderStroke(1.dp, glassTokens.auspiciousGreen.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = glassTokens.auspiciousGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "DPDP Privacy & Offline-First Guarantee",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Core Panchang and festival guides work 100% offline. Location is only read on-demand when you tap 'Use Current Location'.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Optional Google Sign-In
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.setSigningIn(true)
                                            AuthHelper.signInWithGoogle(
                                                context = context,
                                                scope = coroutineScope,
                                                onSuccess = { viewModel.setSigningIn(false) },
                                                onError = { error -> viewModel.setAuthError(error) },
                                                onCancelled = { viewModel.setSigningIn(false) }
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("google_signin_button"),
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !state.isSigningIn
                                    ) {
                                        if (state.isSigningIn) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text(
                                                text = "Optional: Sign in with Google for Cloud Sync",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }

                                    if (state.authError != null) {
                                        Text(
                                            text = state.authError ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wizard Navigation Controls (Back + Next Step) & Direct Get Started CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.currentStep > 0) {
                            OutlinedButton(
                                onClick = { viewModel.previousStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("wizard_back_button"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.backButton, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (state.currentStep < 4) {
                            Button(
                                onClick = { viewModel.nextStep() },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(52.dp)
                                    .testTag("wizard_next_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "${strings.nextPrefix}: ${stepTitles.getOrElse(state.currentStep + 1) { "" }}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Get Started / Finish Setup Button (Always available with testTag "continue_button")
                    Button(
                        onClick = { viewModel.completeSetup(onSetupComplete) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("continue_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.currentStep == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (state.currentStep == 4) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = if (state.currentStep == 4) strings.getStartedEnter else strings.saveDefaultsEnter,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
                }
            }
        }
    }
}
}

@Composable
private fun WelcomeFeaturePill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeComposition(
    state: SetupUiState,
    strings: com.example.ui.theme.UtsavStrings,
    onGetStarted: () -> Unit,
    onExploreDirectly: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("welcome_composition")
    ) {
        // Full-screen cinematic cultural backdrop artwork
        FestivalArtworkBanner(
            festivalId = "welcome_utsavsetu",
            modifier = Modifier.fillMaxSize(),
            isHeroBackground = true
        )

        // Top Status Bar: UtsavSetu Identity + Language Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UtsavSetu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "वैदिक उत्सव एवं पंचांग",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box {
                LiquidGlassSurface(
                    cornerRadius = 20.dp,
                    elevation = 4.dp,
                    onClick = { languageMenuExpanded = true },
                    modifier = Modifier.testTag("welcome_language_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌐 ${state.selectedLanguage.nativeName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                    modifier = Modifier
                        .widthIn(min = 220.dp, max = 300.dp)
                        .heightIn(max = 280.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.nativeName} (${lang.englishName})",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onSelectLanguage(lang)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Bottom Editorial Glass Card: Prominent Headline, Supporting text, Get Started & Explore buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 640.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiquidGlassSurface(
                cornerRadius = 28.dp,
                strongFill = true,
                elevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE VEDIC CALENDAR & RITUAL COMPANION",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassTokens.decorativeGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.heroTagline, // "Celebrate every moment. Honour every tradition."
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.heroSubtagline, // "Your festivals, Panchang, puja rituals and meaningful moments—together in one place."
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Prominent Primary Action: Get Started
                    Button(
                        onClick = onGetStarted,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("welcome_get_started_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restrained Secondary Option: Explore UtsavSetu Directly
                    TextButton(
                        onClick = onExploreDirectly,
                        modifier = Modifier.testTag("welcome_explore_button")
                    ) {
                        Text(
                            text = strings.exploreFirstCta,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
