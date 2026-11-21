package com.example.ui.screens.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AppLanguage
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import com.example.platform.auth.AuthHelper
import com.example.platform.location.LocationCityResolver
import com.example.platform.notifications.DailyMorningNotificationEngine
import com.example.ui.components.AnimatedCollapsibleSection
import com.example.ui.components.CitySelectionDialog
import com.example.ui.components.FestivalArtworkBanner
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.ReminderCentreDialog
import com.example.ui.components.SystemHealthDialog
import com.example.ui.components.TraditionChip
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.LocalUtsavStrings
import com.example.ui.theme.TulsiGreen
import com.example.ui.theme.UtsavLocalizationEngine
import java.util.Locale
import kotlin.math.abs

/**
 * Blueprint Section 14 (Screen 12): Profile & Settings.
 * Structured sections with progressive disclosure so Language, Location, Traditions, and Display
 * are immediately accessible while Reminders, Privacy, and About expand smoothly on demand.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onDataDeleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = state.userPreferences ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glassTokens = LocalLiquidGlassTokens.current

    var cityMenuExpanded by remember { mutableStateOf(false) }
    var autoTriggerGpsInDialog by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var showReminderCentre by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAboutPanchangDialog by remember { mutableStateOf(false) }
    var showSystemHealthDialog by remember { mutableStateOf(false) }
    val strings = com.example.ui.theme.LocalUtsavStrings.current

    if (showReminderCentre) {
        ReminderCentreDialog(
            reminders = prefs.customReminders,
            dayBeforeEnabled = prefs.dayBeforeReminder,
            morningOfEnabled = prefs.morningOfReminder,
            muhuratStartEnabled = prefs.muhuratStartReminder,
            userPreferences = prefs,
            onDeleteReminder = { entry ->
                com.example.platform.notifications.ReminderScheduler.cancelCustomReminder(
                    context = context,
                    festivalId = entry.festivalId,
                    triggerType = entry.triggerType
                )
                viewModel.deleteCustomReminder(entry)
            },
            onUpdateGlobalRules = { d, m, s -> viewModel.updateReminders(d, m, s) },
            onUpdateDailyMorning = { enabled, hour, minute ->
                viewModel.updateDailyMorningSettings(enabled, hour, minute)
            },
            onMarkRationaleShown = { viewModel.markNotificationRationaleShown() },
            onDismiss = { showReminderCentre = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text("Privacy Policy & DPDP Rights", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "1. Offline-First By Design: Your city selection, regional traditions, daily Panchang computations, and Samagri checklists are stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "2. Zero Background Location Tracking: UtsavSetu uses fixed city coordinates selected by you. We never track continuous GPS location.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "3. Optional Cloud Sync: Only if you explicitly sign in with Google, your preferences and Samagri checklist progress are synced to your private Firestore document.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "4. Right to Erasure (DPDP Act 2023): You can permanently wipe both local and cloud data at any time using 'Clear Local & Cloud Data'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(strings.hideDetailsLabel, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAboutPanchangDialog) {
        AlertDialog(
            onDismissRequest = { showAboutPanchangDialog = false },
            title = {
                Text("About Panchang & Calculation Methods", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "• Astronomical Engine: High-precision Jean Meeus solar and lunar longitude algorithms computed locally on your device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Ayanamsa Standard: Lahiri Chitrapaksha Ayanamsa (24.1°) aligned with the Rashtriya Panchang of the Positional Astronomy Centre, Kolkata.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Regional Conventions: Supports both Purnimanta (North India) and Amanta (Maharashtra, Gujarat, Karnataka, Andhra/Telangana, Tamil Nadu) lunar month reckoning.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Observance Rules: Evaluates Udaya Tithi (sunrise), Madhyahna (midday), Pradosh Kaal (dusk), and Nishita Kaal (midnight) per Dharmashastra texts (Dharma Sindhu & Nirnaya Sindhu).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutPanchangDialog = false }) {
                    Text(strings.hideDetailsLabel, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showSystemHealthDialog) {
        SystemHealthDialog(
            onDismiss = { showSystemHealthDialog = false }
        )
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            title = { Text("Clear Local & Cloud Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Under the DPDP Act 2023, this permanently deletes your saved Samagri checklists, custom reminders, preferences, and optional cloud backup, and resets UtsavSetu."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showDeleteDialog(false)
                        viewModel.deleteMyData(onDataDeleted)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_data_button")
                ) {
                    Text("Delete Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 128.dp)
            .testTag("profile_screen")
    ) {
        // 1. Header Banner with Liquid Glass Account & Sync Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            FestivalArtworkBanner(
                festivalId = "diwali_lakshmi_puja",
                modifier = Modifier.matchParentSize(),
                isHeroBackground = true
            )

            LiquidGlassSurface(
                cornerRadius = 24.dp,
                strongFill = true,
                elevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.settingsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.isUserSignedIn) {
                                    "Cloud Sync · ${state.userEmail ?: state.userDisplayName}"
                                } else {
                                    strings.appSubtitle
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.isUserSignedIn) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.triggerSync() },
                                enabled = !state.isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isSyncing) "..." else "Sync", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { AuthHelper.signOut(context, scope) {} },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.signOutLabel, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                AuthHelper.signInWithGoogle(
                                    context = context,
                                    scope = scope,
                                    onSuccess = { viewModel.triggerSync() },
                                    onError = {},
                                    onCancelled = {}
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.googleCloudBackupLabel, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // 2. Language Card (Primary - Always Visible)
            ProfileSectionCard(title = strings.languageSectionHeader) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { languageMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_language_selector"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = glassTokens.solidElevatedContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${prefs.preferredLanguage.nativeName} (${prefs.preferredLanguage.englishName})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = prefs.preferredLanguage.sampleGreeting,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${strings.changeButton} →",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                        text = "${lang.nativeName} · ${lang.englishName}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    viewModel.updateLanguage(lang)
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Location (City and Region - Primary Always Visible)
            ProfileSectionCard(title = strings.locationSectionHeader) {
                Button(
                    onClick = {
                        autoTriggerGpsInDialog = true
                        cityMenuExpanded = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("profile_detect_location_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.useCurrentLocationGps,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    val latDir = if (prefs.hostCity.latitude >= 0.0) "N" else "S"
                    val lonDir = if (prefs.hostCity.longitude >= 0.0) "E" else "W"
                    val coordSummary = String.format(
                        Locale.US,
                        "%.4f° %s, %.4f° %s",
                        abs(prefs.hostCity.latitude),
                        latDir,
                        abs(prefs.hostCity.longitude),
                        lonDir
                    )
                    val tzSummary = LocationCityResolver.formatTimeZoneSummary(prefs.hostCity.timeZoneId)

                    OutlinedCard(
                        onClick = {
                            autoTriggerGpsInDialog = false
                            cityMenuExpanded = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("profile_selected_location_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = glassTokens.solidElevatedContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = UtsavLocalizationEngine.localizeCityName(prefs.hostCity.name, strings.language),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${UtsavLocalizationEngine.localizeStateName(prefs.hostCity.state, strings.language)}, ${prefs.hostCity.country}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$coordSummary · $tzSummary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${strings.changeButton} →",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (cityMenuExpanded) {
                        CitySelectionDialog(
                            currentCity = prefs.hostCity,
                            recentCities = prefs.recentCities,
                            autoTriggerGpsOnOpen = autoTriggerGpsInDialog,
                            onCitySelected = { city ->
                                viewModel.updateHostCityObject(city)
                                cityMenuExpanded = false
                                autoTriggerGpsInDialog = false
                            },
                            onDismiss = {
                                cityMenuExpanded = false
                                autoTriggerGpsInDialog = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. App Preferences (Theme & Accessibility Text Size - Single-Line Balanced Chips)
            ProfileSectionCard(title = strings.appPreferencesSectionHeader) {
                Text(
                    text = strings.appearanceThemeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SYSTEM" to strings.autoThemeLabel,
                        "LIGHT" to strings.lightThemeLabel,
                        "DARK" to strings.darkThemeLabel
                    ).forEach { (mode, label) ->
                        val isSelected = prefs.themeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateThemeMode(mode) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.textSizeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("NORMAL" to strings.standardTextLabel, "LARGE" to strings.largeTextLabel).forEach { (scale, label) ->
                        val isSelected = prefs.textScaleMode == scale
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateTextScaleMode(scale) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = strings.panditVoiceLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMale = prefs.panditVoiceGender == "MALE"
                    val isFemale = prefs.panditVoiceGender == "FEMALE"

                    // Option 1: Male Pandit (पुरुष पुरोहित)
                    OutlinedCard(
                        onClick = { viewModel.updatePanditVoiceGender("MALE") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_option_male"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isMale) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                            else glassTokens.solidElevatedContainer
                        ),
                        border = BorderStroke(
                            width = if (isMale) 1.6.dp else 1.dp,
                            color = if (isMale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isMale,
                                onClick = { viewModel.updatePanditVoiceGender("MALE") }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.malePanditTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isMale) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.malePanditSub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Option 2: Female Pandit (महिला पुरोहित / विदुषी)
                    OutlinedCard(
                        onClick = { viewModel.updatePanditVoiceGender("FEMALE") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_option_female"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isFemale) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                            else glassTokens.solidElevatedContainer
                        ),
                        border = BorderStroke(
                            width = if (isFemale) 1.6.dp else 1.dp,
                            color = if (isFemale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isFemale,
                                onClick = { viewModel.updatePanditVoiceGender("FEMALE") }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.femalePanditTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isFemale) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.femalePanditSub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Collapsible Regional Traditions Section
            AnimatedCollapsibleSection(
                title = strings.traditionsSectionHeader,
                subtitle = prefs.selectedTraditions.joinToString(", ") {
                    UtsavLocalizationEngine.localizeTraditionName(it, strings.language)
                },
                initiallyExpanded = false,
                testTagName = "profile_traditions_dropdown"
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RegionalTradition.values().forEach { tradition ->
                        TraditionChip(
                            tradition = tradition,
                            isSelected = prefs.selectedTraditions.contains(tradition),
                            onToggle = { viewModel.toggleTradition(tradition) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Collapsible Reminders & Daily Morning Notifications Section
            val dailyMorningSummary = if (prefs.dailyMorningReminderEnabled) {
                "Daily ${DailyMorningNotificationEngine.formatTimeOfDay(prefs.dailyMorningHour, prefs.dailyMorningMinute)} · ${prefs.customReminders.size} custom"
            } else {
                "Daily off · ${prefs.customReminders.size} custom"
            }
            AnimatedCollapsibleSection(
                title = strings.remindersSectionHeader,
                subtitle = dailyMorningSummary,
                initiallyExpanded = false,
                testTagName = "profile_reminders_dropdown"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Morning Schedule & Diagnostics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { showReminderCentre = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("profile_open_reminder_centre_button")
                        ) {
                            Text("Manage →", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    ProfileSwitchRow(
                        title = "Daily Morning Festival & Muhurat",
                        subtitle = "${DailyMorningNotificationEngine.formatTimeOfDay(prefs.dailyMorningHour, prefs.dailyMorningMinute)} (${prefs.hostCity.name})",
                        checked = prefs.dailyMorningReminderEnabled,
                        onCheckedChange = {
                            viewModel.updateDailyMorningSettings(it, prefs.dailyMorningHour, prefs.dailyMorningMinute)
                        }
                    )
                    ProfileSwitchRow(
                        title = strings.dayBeforeReminderTitle,
                        subtitle = strings.tabSamagri,
                        checked = prefs.dayBeforeReminder,
                        onCheckedChange = {
                            viewModel.updateReminders(it, prefs.morningOfReminder, prefs.muhuratStartReminder)
                        }
                    )
                    ProfileSwitchRow(
                        title = strings.festivalMorningReminderTitle,
                        subtitle = strings.tithiLabel,
                        checked = prefs.morningOfReminder,
                        onCheckedChange = {
                            viewModel.updateReminders(prefs.dayBeforeReminder, it, prefs.muhuratStartReminder)
                        }
                    )
                    ProfileSwitchRow(
                        title = strings.muhuratStartReminderTitle,
                        subtitle = strings.auspiciousTimingsHeader,
                        checked = prefs.muhuratStartReminder,
                        onCheckedChange = {
                            viewModel.updateReminders(prefs.dayBeforeReminder, prefs.morningOfReminder, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Data & Privacy & About (Kept accessible with testTags intact)
            AnimatedCollapsibleSection(
                title = "${strings.dataPrivacySectionHeader} & ${strings.aboutSectionHeader}",
                subtitle = "UtsavSetu v1.0.0 · DPDP Act 2023",
                initiallyExpanded = true,
                testTagName = "profile_privacy_about_dropdown"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // UtsavSetu App Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_brand_identity_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UtsavSetu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "वैदिक उत्सव एवं पंचांग",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lahiri Chitrapaksha Panchang • v1.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showPrivacyPolicyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_policy_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.dataPrivacySectionHeader, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { showAboutPanchangDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("about_panchang_data_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.aboutSectionHeader, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { showSystemHealthDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("system_health_diagnostics_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Health & Diagnostics", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { viewModel.showDeleteDialog(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_my_data_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.clearLocalAndCloudDataLabel, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = glassTokens.decorativeGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ProfileSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
