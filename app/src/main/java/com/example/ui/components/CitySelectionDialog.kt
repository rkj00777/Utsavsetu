package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.panchang.City
import com.example.panchang.RegionalPanchangConvention
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.UtsavLocalizationEngine
import java.util.Locale

private enum class CityCategoryFilter(val label: String) {
    ALL("All (120+)"),
    METROS("Metros"),
    PILGRIMAGE("Pilgrimage Kshetras"),
    STATES("States & Districts"),
    INTERNATIONAL("International Diaspora")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionDialog(
    currentCity: City,
    onCitySelected: (City) -> Unit,
    onDetectGpsLocation: (() -> Unit)? = null,
    isDetectingLocation: Boolean = false,
    onDismiss: () -> Unit
) {
    val strings = LocalUtsavStrings.current
    val glassTokens = LocalLiquidGlassTokens.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CityCategoryFilter.ALL) }
    var showCustomCoordsEditor by remember { mutableStateOf(false) }

    // Custom coordinates fields
    var customLatText by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentCity.latitude)) }
    var customLonText by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentCity.longitude)) }
    var customNameText by remember { mutableStateOf(if (currentCity.isLocationDetected) currentCity.name else "") }
    var customStateText by remember { mutableStateOf(if (currentCity.isLocationDetected) currentCity.state else "") }
    var customCoordsError by remember { mutableStateOf<String?>(null) }

    val filteredCities = remember(searchQuery, selectedCategory) {
        val baseList = when (selectedCategory) {
            CityCategoryFilter.ALL -> City.ALL_CITIES
            CityCategoryFilter.METROS -> City.TOP_CITIES
            CityCategoryFilter.PILGRIMAGE -> City.PILGRIMAGE_CITIES
            CityCategoryFilter.STATES -> City.ALL_CITIES.filter {
                City.TOP_CITIES.none { top -> top.id == it.id } &&
                City.PILGRIMAGE_CITIES.none { p -> p.id == it.id } &&
                City.INTERNATIONAL_CITIES.none { i -> i.id == it.id }
            }
            CityCategoryFilter.INTERNATIONAL -> City.INTERNATIONAL_CITIES
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            City.ALL_CITIES.filter { city ->
                city.name.lowercase(Locale.ROOT).contains(q) ||
                city.state.lowercase(Locale.ROOT).contains(q) ||
                city.district.lowercase(Locale.ROOT).contains(q) ||
                city.country.lowercase(Locale.ROOT).contains(q) ||
                city.aliases.any { it.lowercase(Locale.ROOT).contains(q) }
            }
        }
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
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Location & City",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "120+ Curated Districts & Kshetras, or Exact GPS Coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("city_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Auto-detect banner button
                if (onDetectGpsLocation != null) {
                    FilledTonalButton(
                        onClick = onDetectGpsLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_gps_detect_button"),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isDetectingLocation,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting GPS Signal...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detect Location via Device GPS", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_search_input"),
                    placeholder = { Text("Search by city, district, state, alias (e.g. Kashi, Poona)...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CityCategoryFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedCategory == filter,
                            onClick = { selectedCategory = filter },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle for Exact Custom Coordinates
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomCoordsEditor = !showCustomCoordsEditor }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = glassTokens.decorativeGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enter Exact GPS Coordinates (Worldwide)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (showCustomCoordsEditor) "Hide" else "Enter Coordinates",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Expandable Custom Coordinates Form
                AnimatedVisibility(visible = showCustomCoordsEditor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customLatText,
                                onValueChange = { customLatText = it },
                                label = { Text("Latitude (°)") },
                                placeholder = { Text("e.g. 25.3176") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_lat_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = customLonText,
                                onValueChange = { customLonText = it },
                                label = { Text("Longitude (°)") },
                                placeholder = { Text("e.g. 82.9739") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_lon_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customNameText,
                                onValueChange = { customNameText = it },
                                label = { Text("Location / Town Name") },
                                placeholder = { Text("e.g. My Village, Vrindavan") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = customStateText,
                                onValueChange = { customStateText = it },
                                label = { Text("State / Region") },
                                placeholder = { Text("e.g. Uttar Pradesh") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_state_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        if (customCoordsError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = customCoordsError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val lat = customLatText.toDoubleOrNull()
                                val lon = customLonText.toDoubleOrNull()
                                if (lat == null || lat < -90.0 || lat > 90.0) {
                                    customCoordsError = "Please enter a valid Latitude between -90° and +90°."
                                    return@Button
                                }
                                if (lon == null || lon < -180.0 || lon > 180.0) {
                                    customCoordsError = "Please enter a valid Longitude between -180° and +180°."
                                    return@Button
                                }
                                customCoordsError = null
                                val customCity = City.createCustomGpsCity(
                                    latitude = lat,
                                    longitude = lon,
                                    name = customNameText.takeIf { it.isNotBlank() },
                                    state = customStateText.takeIf { it.isNotBlank() }
                                )
                                onCitySelected(customCity)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_custom_coords_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply Custom Coordinates & Calculate Panchang", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cities List
                Text(
                    text = "${filteredCities.size} Locations Available",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = filteredCities, key = { it.id }) { city ->
                        val isSelected = currentCity.id == city.id ||
                            (!currentCity.isLocationDetected && currentCity.name == city.name && currentCity.state == city.state)

                        Surface(
                            onClick = {
                                onCitySelected(city)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else glassTokens.solidElevatedContainer,
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
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (city.country != "India") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = city.country,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    val locationSubtext = if (city.district != city.state && city.district.isNotBlank()) {
                                        "${city.district} District, ${city.state}"
                                    } else {
                                        "${city.state}, ${city.country}"
                                    }
                                    Text(
                                        text = locationSubtext,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Coordinates and convention badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        val latDir = if (city.latitude >= 0) "N" else "S"
                                        val lonDir = if (city.longitude >= 0) "E" else "W"
                                        Text(
                                            text = String.format(Locale.US, "%.2f°%s, %.2f°%s", Math.abs(city.latitude), latDir, Math.abs(city.longitude), lonDir),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = glassTokens.decorativeGold,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• ${city.panchangConvention.displayName.substringBefore(" (")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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
                }
            }
        }
    }
}
