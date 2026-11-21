package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.panchang.City
import com.example.platform.location.CoordinateValidationResult
import com.example.platform.location.LocationCityResolver
import com.example.platform.location.LocationCityResult
import com.example.platform.location.WorldwideSearchResult
import com.example.ui.theme.LocalLiquidGlassTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Redesigned Location & City Selector Dialog for UtsavSetu.
 *
 * Hierarchy:
 * - Selected Location Summary (City, State/Region, Country, Coordinates, Time Zone)
 * - A. Primary Action: "Use My Current Location" (permission -> Fused GPS / LocationManager ->
 *      reverse geocode -> accuracy & stale-fix confirmation card -> Retry / Confirm & Save)
 * - B. Searchable Worldwide City Selection (250ms debounced search over 124+ offline cities +
 *      best-effort online worldwide Geocoder results; compact Recent & Popular locations when empty)
 * - C. Compact Expandable "Advanced – Enter GPS Coordinates" with -90..+90 / -180..+180 validation,
 *      auto-resolved & selectable time zone, and pre-save preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionDialog(
    currentCity: City,
    recentCities: List<City> = emptyList(),
    onCitySelected: (City) -> Unit,
    onDetectGpsLocation: (() -> Unit)? = null,
    isDetectingLocation: Boolean = false,
    autoTriggerGpsOnOpen: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glassTokens = LocalLiquidGlassTokens.current

    // GPS detection state (requires explicit confirmation before saving)
    var isDetectingGps by remember { mutableStateOf(isDetectingLocation) }
    var detectedGpsSuccess by remember { mutableStateOf<LocationCityResult.Success?>(null) }
    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }

    // Search state (debounced)
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var onlineCities by remember { mutableStateOf<List<City>>(emptyList()) }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineSearchNote by remember { mutableStateOf<String?>(null) }

    // Expandable Advanced Manual GPS Coordinates state
    var showAdvancedManualGps by remember { mutableStateOf(false) }
    var manualLatText by remember { mutableStateOf("") }
    var manualLonText by remember { mutableStateOf("") }
    var manualLabelText by remember { mutableStateOf("") }
    var manualTimeZoneOverride by remember { mutableStateOf<String?>(null) }
    var showTimeZoneMenu by remember { mutableStateOf(false) }
    var manualCoordsError by remember { mutableStateOf<String?>(null) }
    var manualPreviewCity by remember { mutableStateOf<City?>(null) }
    var isResolvingManualPreview by remember { mutableStateOf(false) }

    fun runGpsDetection() {
        scope.launch {
            isDetectingGps = true
            gpsErrorMessage = null
            val result = LocationCityResolver.detectUserCity(context)
            isDetectingGps = false
            when (result) {
                is LocationCityResult.Success -> {
                    detectedGpsSuccess = result
                    gpsErrorMessage = null
                }
                is LocationCityResult.Error -> {
                    detectedGpsSuccess = null
                    gpsErrorMessage = result.message
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantMap ->
        val fineGranted = grantMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = grantMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted || LocationCityResolver.hasLocationPermission(context)) {
            runGpsDetection()
        } else {
            gpsErrorMessage = "Location permission was denied. You can search any city worldwide or enter GPS coordinates below."
        }
    }

    fun requestCurrentLocationFlow() {
        if (LocationCityResolver.hasLocationPermission(context)) {
            runGpsDetection()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(autoTriggerGpsOnOpen) {
        if (autoTriggerGpsOnOpen) {
            requestCurrentLocationFlow()
        }
    }

    // Debounce search query (250ms) and invoke best-effort online worldwide Geocoder for >= 3 chars
    LaunchedEffect(searchQuery) {
        delay(250L)
        val trimmed = searchQuery.trim()
        debouncedQuery = trimmed
        if (trimmed.length < 3) {
            onlineCities = emptyList()
            isSearchingOnline = false
            onlineSearchNote = null
        } else {
            isSearchingOnline = true
            onlineSearchNote = null
            when (val res = LocationCityResolver.searchWorldwideCitiesOnline(context, trimmed)) {
                is WorldwideSearchResult.Success -> {
                    onlineCities = res.cities
                    onlineSearchNote = null
                }
                is WorldwideSearchResult.Empty -> {
                    onlineCities = emptyList()
                    onlineSearchNote = "No additional online matches found."
                }
                is WorldwideSearchResult.Unavailable -> {
                    onlineCities = emptyList()
                    onlineSearchNote = res.reason
                }
            }
            isSearchingOnline = false
        }
    }

    val offlineMatches = remember(debouncedQuery) {
        if (debouncedQuery.isBlank()) {
            emptyList()
        } else {
            LocationCityResolver.searchOfflineCitiesRanked(debouncedQuery, limit = 20)
        }
    }

    val combinedOnlineMatches = remember(offlineMatches, onlineCities) {
        val offlineKeys = offlineMatches.map { "${it.name.lowercase(Locale.US)}_${it.country.lowercase(Locale.US)}" }.toSet()
        onlineCities.filterNot { "${it.name.lowercase(Locale.US)}_${it.country.lowercase(Locale.US)}" in offlineKeys }
    }

    val popularCities = remember {
        listOf(
            City.DELHI, City.MUMBAI, City.BENGALURU, City.CHENNAI,
            City.KOLKATA, City.HYDERABAD, City.VARANASI, City.PUNE,
            City.LONDON, City.NEW_YORK, City.DUBAI, City.SINGAPORE
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("city_selection_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("city_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close location selector",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    // D. SELECTED LOCATION DISPLAY (City, State/Region, Country, Lat/Lon, Time Zone)
                    item(key = "selected_location_summary") {
                        SelectedLocationSummaryCard(
                            city = currentCity,
                            modifier = Modifier.testTag("selected_location_summary")
                        )
                    }

                    // A. PRIMARY ACTION – CURRENT LOCATION
                    item(key = "primary_gps_action") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { requestCurrentLocationFlow() },
                                enabled = !isDetectingGps,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 50.dp)
                                    .testTag("city_gps_detect_button")
                            ) {
                                if (isDetectingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Detecting Current Location…", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Use My Current Location", fontWeight = FontWeight.Bold)
                                }
                            }

                            // GPS Error / Permission Denial Notice with Retry
                            if (gpsErrorMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("gps_error_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = gpsErrorMessage ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { requestCurrentLocationFlow() }) {
                                            Text("Retry", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // GPS Detected Location Confirmation Card (requires explicit user confirmation)
                            AnimatedVisibility(visible = detectedGpsSuccess != null) {
                                detectedGpsSuccess?.let { detected ->
                                    DetectedLocationConfirmationCard(
                                        result = detected,
                                        onConfirm = {
                                            onCitySelected(detected.city)
                                            detectedGpsSuccess = null
                                            onDismiss()
                                        },
                                        onRetry = { requestCurrentLocationFlow() },
                                        onCancel = { detectedGpsSuccess = null }
                                    )
                                }
                            }
                        }
                    }

                    // B. SEARCHABLE WORLDWIDE CITY SELECTION
                    item(key = "worldwide_search_bar") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SEARCH WORLDWIDE CITIES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = glassTokens.decorativeGold
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("city_search_input"),
                                placeholder = {
                                    Text(
                                        text = "Search city, state, country, or alias (e.g. Kashi, London, Tokyo)…",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }

                    // Search Results vs. Compact Recent & Popular Locations
                    if (debouncedQuery.isBlank()) {
                        if (recentCities.isNotEmpty()) {
                            item(key = "recent_locations_section") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "RECENTLY SELECTED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(items = recentCities, key = { "recent_${it.id}" }) { recent ->
                                            val isSelected = recent.id == currentCity.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    onCitySelected(recent)
                                                    onDismiss()
                                                },
                                                label = {
                                                    Text(
                                                        text = "${recent.name}, ${recent.country}",
                                                        maxLines = 1
                                                    )
                                                },
                                                modifier = Modifier.testTag("recent_city_${recent.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "popular_locations_header") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "POPULAR LOCATIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${City.ALL_CITIES.size} offline cities searchable",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(items = popularCities, key = { "popular_${it.id}" }) { city ->
                            CityResultRow(
                                city = city,
                                isSelected = isSameCity(currentCity, city),
                                isOnlineResult = false,
                                onSelect = {
                                    onCitySelected(city)
                                    onDismiss()
                                }
                            )
                        }
                    } else {
                        if (offlineMatches.isNotEmpty()) {
                            item(key = "offline_matches_header") {
                                Text(
                                    text = "OFFLINE CITY DATABASE (${offlineMatches.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(items = offlineMatches, key = { "offline_${it.id}" }) { city ->
                                CityResultRow(
                                    city = city,
                                    isSelected = isSameCity(currentCity, city),
                                    isOnlineResult = false,
                                    onSelect = {
                                        onCitySelected(city)
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (isSearchingOnline) {
                            item(key = "online_search_loading") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Searching worldwide locations online…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (combinedOnlineMatches.isNotEmpty()) {
                            item(key = "online_matches_header") {
                                Text(
                                    text = "WORLDWIDE ONLINE MATCHES (${combinedOnlineMatches.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(items = combinedOnlineMatches, key = { "online_${it.id}" }) { city ->
                                CityResultRow(
                                    city = city,
                                    isSelected = isSameCity(currentCity, city),
                                    isOnlineResult = true,
                                    onSelect = {
                                        onCitySelected(city)
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (offlineMatches.isEmpty() && combinedOnlineMatches.isEmpty() && !isSearchingOnline) {
                            item(key = "empty_search_notice") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = glassTokens.solidElevatedContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "No matching cities found for \"$debouncedQuery\".",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = onlineSearchNote ?: "Try another spelling or expand 'Advanced – Enter GPS Coordinates' below.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else if (onlineSearchNote != null && combinedOnlineMatches.isEmpty()) {
                            item(key = "online_search_status") {
                                Text(
                                    text = onlineSearchNote ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // C. COMPACT EXPANDABLE SECTION – "Advanced – Enter GPS Coordinates"
                    item(key = "advanced_manual_gps") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable { showAdvancedManualGps = !showAdvancedManualGps }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .testTag("advanced_gps_toggle"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PinDrop,
                                            contentDescription = null,
                                            tint = glassTokens.decorativeGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Advanced – Enter GPS Coordinates",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showAdvancedManualGps) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (showAdvancedManualGps) "Collapse manual GPS" else "Expand manual GPS",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                AnimatedVisibility(visible = showAdvancedManualGps) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = manualLatText,
                                                onValueChange = {
                                                    manualLatText = it
                                                    manualPreviewCity = null
                                                    manualCoordsError = null
                                                },
                                                label = { Text("Latitude (-90 to +90)") },
                                                placeholder = { Text("e.g. 19.0760") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("custom_lat_input")
                                            )
                                            OutlinedTextField(
                                                value = manualLonText,
                                                onValueChange = {
                                                    manualLonText = it
                                                    manualPreviewCity = null
                                                    manualCoordsError = null
                                                },
                                                label = { Text("Longitude (-180 to +180)") },
                                                placeholder = { Text("e.g. 72.8777") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("custom_lon_input")
                                            )
                                        }

                                        OutlinedTextField(
                                            value = manualLabelText,
                                            onValueChange = {
                                                manualLabelText = it
                                                manualPreviewCity = null
                                            },
                                            label = { Text("Optional Location Label") },
                                            placeholder = { Text("Auto-detected via coordinates if left blank") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("custom_name_input")
                                        )

                                        // Time Zone Selector
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedCard(
                                                onClick = { showTimeZoneMenu = true },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("manual_timezone_selector")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Time Zone",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = manualTimeZoneOverride
                                                                ?.let { LocationCityResolver.formatTimeZoneSummary(it) }
                                                                ?: "Auto-resolve from coordinates",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ExpandMore,
                                                        contentDescription = "Choose time zone"
                                                    )
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = showTimeZoneMenu,
                                                onDismissRequest = { showTimeZoneMenu = false },
                                                modifier = Modifier.heightIn(max = 260.dp)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Auto-resolve from coordinates") },
                                                    onClick = {
                                                        manualTimeZoneOverride = null
                                                        manualPreviewCity = null
                                                        showTimeZoneMenu = false
                                                    }
                                                )
                                                LocationCityResolver.COMMON_TIME_ZONES.forEach { tz ->
                                                    DropdownMenuItem(
                                                        text = { Text(LocationCityResolver.formatTimeZoneSummary(tz)) },
                                                        onClick = {
                                                            manualTimeZoneOverride = tz
                                                            manualPreviewCity = manualPreviewCity?.copy(timeZoneId = tz)
                                                            showTimeZoneMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        if (manualCoordsError != null) {
                                            Text(
                                                text = manualCoordsError ?: "",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.testTag("manual_coords_error")
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                when (val validation = LocationCityResolver.validateManualCoordinates(manualLatText, manualLonText)) {
                                                    is CoordinateValidationResult.Invalid -> {
                                                        manualCoordsError = validation.errorMessage
                                                        manualPreviewCity = null
                                                    }
                                                    is CoordinateValidationResult.Valid -> {
                                                        manualCoordsError = null
                                                        scope.launch {
                                                            isResolvingManualPreview = true
                                                            val resolved = LocationCityResolver.resolveCoordinatesToCity(
                                                                context = context,
                                                                latitude = validation.latitude,
                                                                longitude = validation.longitude,
                                                                providerSource = "Manual GPS"
                                                            )
                                                            isResolvingManualPreview = false
                                                            val baseCity = when (resolved) {
                                                                is LocationCityResult.Success -> resolved.city
                                                                is LocationCityResult.Error -> City.createCustomGpsCity(
                                                                    latitude = validation.latitude,
                                                                    longitude = validation.longitude
                                                                )
                                                            }
                                                            manualPreviewCity = baseCity.copy(
                                                                name = manualLabelText.trim().takeIf { it.isNotBlank() } ?: baseCity.name,
                                                                timeZoneId = manualTimeZoneOverride ?: baseCity.timeZoneId
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !isResolvingManualPreview,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("preview_custom_coords_button")
                                        ) {
                                            if (isResolvingManualPreview) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Resolving Location…")
                                            } else {
                                                Text("Preview Resulting Location", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Preview Card before saving manual coordinates
                                        manualPreviewCity?.let { preview ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("manual_coords_preview_card")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "PREVIEW LOCATION",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${preview.name}, ${preview.state}, ${preview.country}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${formatCoords(preview.latitude, preview.longitude)} · ${LocationCityResolver.formatTimeZoneSummary(preview.timeZoneId)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Button(
                                                        onClick = {
                                                            onCitySelected(preview)
                                                            onDismiss()
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .defaultMinSize(minHeight = 48.dp)
                                                            .testTag("apply_custom_coords_button")
                                                    ) {
                                                        Text("Confirm & Save Location", fontWeight = FontWeight.Bold)
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
            }
        }
    }
}

@Composable
private fun SelectedLocationSummaryCard(
    city: City,
    modifier: Modifier = Modifier
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = glassTokens.solidElevatedContainer,
        border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "CURRENTLY SELECTED LOCATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = glassTokens.decorativeGold
            )
            Text(
                text = "${city.name}, ${city.state}, ${city.country}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatCoords(city.latitude, city.longitude)} • ${LocationCityResolver.formatTimeZoneSummary(city.timeZoneId)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetectedLocationConfirmationCard(
    result: LocationCityResult.Success,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val city = result.city
    val accuracyLabel = buildString {
        if (result.accuracyMeters != null) {
            append("±${result.accuracyMeters.roundToInt()} m")
        }
        if (result.isApproximatePermission) {
            if (isNotEmpty()) append(" · ")
            append("Approximate Permission")
        } else {
            if (isNotEmpty()) append(" · ")
            append("Precise Permission")
        }
    }
    val freshnessLabel = when {
        result.isStaleFix -> "Stale cached fix (${result.fixAgeSeconds?.div(60) ?: 10}+ min old) — tap Retry for a fresh fix"
        result.fixAgeSeconds != null && result.fixAgeSeconds <= 60 -> "Fresh fix (< 1 min old)"
        result.fixAgeSeconds != null -> "Fix age: ${result.fixAgeSeconds / 60} min"
        else -> "Live fix"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gps_confirmation_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "CONFIRM DETECTED LOCATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${city.name}, ${city.state}, ${city.country}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Coordinates: ${formatCoords(city.latitude, city.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Time Zone: ${LocationCityResolver.formatTimeZoneSummary(city.timeZoneId)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Accuracy: $accuracyLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = freshnessLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (result.isStaleFix) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = if (result.isStaleFix) FontWeight.Bold else FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("gps_confirm_save_button")
                ) {
                    Text("Confirm & Save", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("gps_retry_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry GPS", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("gps_cancel_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun CityResultRow(
    city: City,
    isSelected: Boolean,
    isOnlineResult: Boolean,
    onSelect: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else glassTokens.solidElevatedContainer,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else glassTokens.decorativeGold.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("city_item_${city.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOnlineResult) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isOnlineResult) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = if (isOnlineResult) "Online · ${city.country}" else city.country,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    text = "${city.state} · ${city.timeZoneId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatCoords(city.latitude, city.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassTokens.decorativeGold,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun isSameCity(a: City, b: City): Boolean {
    return a.id == b.id || (!a.isLocationDetected && a.name == b.name && a.state == b.state && a.country == b.country)
}

private fun formatCoords(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lonDir = if (longitude >= 0) "E" else "W"
    return String.format(Locale.US, "%.4f°%s, %.4f°%s", abs(latitude), latDir, abs(longitude), lonDir)
}
