package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.domain.model.Festival
import com.example.panchang.City
import com.example.platform.location.LocationCityResolver
import com.example.platform.notifications.DailyMorningNotificationEngine
import com.example.platform.notifications.DeliveryAuditState
import com.example.platform.notifications.NotificationDiagnosticsSnapshot
import com.example.platform.notifications.ReminderScheduler
import com.example.platform.notifications.SchedulingMode
import com.example.platform.share.ShareHelper
import com.example.ui.theme.LocalLiquidGlassTokens
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Blueprint Section 13 (Screen 11): Share Festival Information & Visual Share-Card Preview.
 * Uses Android's native share sheet rather than forcing users into a single messaging app.
 */
@Composable
fun ShareFestivalDialog(
    festival: Festival,
    date: LocalDate,
    muhuratWindow: String,
    cityName: String,
    checkedSamagriIds: Set<String> = emptySet(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val glassTokens = LocalLiquidGlassTokens.current
    val dateFormatted = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 460.dp)
                .testTag("share_festival_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Share Festival",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close share dialog"
                        )
                    }
                }

                Text(
                    text = "Preview your festival greeting card before sharing via Android's share sheet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Visual Share-Card Preview (Blueprint 13.2)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
                    border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.5f))
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            FestivalArtworkBanner(
                                festivalId = festival.id,
                                modifier = Modifier.fillMaxSize()
                            )
                            LiquidGlassSurface(
                                cornerRadius = 12.dp,
                                strongFill = true,
                                elevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "UTSAVSETU GREETING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glassTokens.decorativeGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Shubh ${festival.nameEnglish}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = festival.nameNative,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puja Muhurat ($cityName · IST): $muhuratWindow",
                                style = MaterialTheme.typography.labelMedium,
                                color = glassTokens.auspiciousGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = festival.significance,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Includes ${festival.vidhiSteps.size}-step Puja Vidhi & ${festival.samagriItems.size} Samagri items",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassTokens.decorativeGold,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📲 Google Play Store link attached (opens festival in UtsavSetu if installed, or Play Store to install)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Share Option: Share Greeting & Verified Timings
                Button(
                    onClick = {
                        ShareHelper.shareFestivalSummary(
                            context = context,
                            festival = festival,
                            date = date,
                            muhuratWindow = muhuratWindow,
                            cityName = cityName
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_share_greeting_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Greeting & Verified Timings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Share Option: Share Puja Vidhi & Samagri Checklist
                OutlinedButton(
                    onClick = {
                        ShareHelper.shareSamagriChecklist(
                            context = context,
                            festival = festival,
                            date = date,
                            cityName = cityName,
                            checkedIds = checkedSamagriIds
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_share_samagri_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Puja Vidhi & Samagri Checklist",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Blueprint Section 12 (Screen 10): Reminder Creation Editor.
 * Requests notification permission at a meaningful moment and avoids duplicate reminders.
 */
@Composable
fun ReminderEditorDialog(
    festivalId: String,
    festivalName: String,
    dateFormatted: String,
    cityName: String,
    onSaveReminder: (CustomReminderEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current
    var selectedTrigger by remember { mutableStateOf("ONE_DAY_BEFORE") }
    var selectedTime by remember { mutableStateOf("08:00 AM") }
    var permissionDeniedNote by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionDeniedNote = !granted
            onSaveReminder(
                CustomReminderEntry(
                    festivalId = festivalId,
                    festivalName = festivalName,
                    dateFormatted = dateFormatted,
                    triggerType = selectedTrigger,
                    timeLabel = selectedTime
                )
            )
            if (granted) onDismiss()
        }
    )

    val triggerOptions = listOf(
        "ON_DAY" to "On the festival day",
        "ONE_DAY_BEFORE" to "One day before",
        "THREE_DAYS_BEFORE" to "3 days before",
        "MUHURAT_START" to "At Puja Muhurat start"
    )
    val timeOptions = listOf("06:30 AM", "08:00 AM", "06:00 PM", "07:30 PM")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .testTag("reminder_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close reminder editor")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = festivalName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$dateFormatted · $cityName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Remind me",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                triggerOptions.forEach { (key, label) ->
                    val isSelected = selectedTrigger == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTrigger = key }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedTrigger = key }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Notification time",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeOptions.forEach { time ->
                        val selected = selectedTime == time
                        FilterChip(
                            selected = selected,
                            onClick = { selectedTime = time },
                            label = {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Offline-first: Reminders work without internet and automatically adjust if you change your city.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (permissionDeniedNote) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reminder saved locally. To receive system alerts, enable notifications for UtsavSetu in Android Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onSaveReminder(
                                CustomReminderEntry(
                                    festivalId = festivalId,
                                    festivalName = festivalName,
                                    dateFormatted = dateFormatted,
                                    triggerType = selectedTrigger,
                                    timeLabel = selectedTime
                                )
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_reminder_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Reminder",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Blueprint Section 12 (Screen S15): Reminder Centre.
 * Manages:
 * - Daily Morning Festival & Muhurat notification toggle + configurable time (default 7:00 AM)
 * - Android 13+ POST_NOTIFICATIONS explanation dialog before permission prompt
 * - Honest Notification Diagnostics (Permission, Exact Alarm vs Approximate WorkManager Fallback,
 *   Next Scheduled Delivery, Test Notification action) and Daily Delivery Audit history
 * - Scheduled custom festival reminders and automatic festival rules
 */
@Composable
fun ReminderCentreDialog(
    reminders: List<CustomReminderEntry>,
    dayBeforeEnabled: Boolean,
    morningOfEnabled: Boolean,
    muhuratStartEnabled: Boolean,
    onDeleteReminder: (CustomReminderEntry) -> Unit,
    onUpdateGlobalRules: (Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
    userPreferences: UserPreferences? = null,
    onUpdateDailyMorning: ((Boolean, Int, Int) -> Unit)? = null,
    onMarkRationaleShown: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glassTokens = LocalLiquidGlassTokens.current

    val effectivePrefs = userPreferences ?: UserPreferences(
        hostCity = City.DEFAULT_CITY,
        dayBeforeReminder = dayBeforeEnabled,
        morningOfReminder = morningOfEnabled,
        muhuratStartReminder = muhuratStartEnabled,
        dailyMorningReminderEnabled = morningOfEnabled
    )

    var dailyEnabled by remember(effectivePrefs.dailyMorningReminderEnabled) {
        mutableStateOf(effectivePrefs.dailyMorningReminderEnabled)
    }
    var selectedHour by remember(effectivePrefs.dailyMorningHour) {
        mutableIntStateOf(effectivePrefs.dailyMorningHour)
    }
    var selectedMinute by remember(effectivePrefs.dailyMorningMinute) {
        mutableIntStateOf(effectivePrefs.dailyMorningMinute)
    }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var permissionStatusBanner by remember { mutableStateOf<String?>(null) }
    var testNotificationStatus by remember { mutableStateOf<String?>(null) }
    var isSendingTestNotification by remember { mutableStateOf(false) }
    var diagnosticsRefreshTick by remember { mutableIntStateOf(0) }

    val diagnostics: NotificationDiagnosticsSnapshot = remember(
        effectivePrefs,
        dailyEnabled,
        selectedHour,
        selectedMinute,
        diagnosticsRefreshTick
    ) {
        ReminderScheduler.getDiagnosticsSnapshot(
            context = context,
            preferences = effectivePrefs.copy(
                dailyMorningReminderEnabled = dailyEnabled,
                dailyMorningHour = selectedHour,
                dailyMorningMinute = selectedMinute
            )
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            diagnosticsRefreshTick += 1
            if (granted) {
                permissionStatusBanner = "Notification permission granted. Daily morning alerts are active."
                dailyEnabled = true
                onUpdateDailyMorning?.invoke(true, selectedHour, selectedMinute)
            } else {
                permissionStatusBanner = "Notification permission denied. Settings are saved, but Android will block notifications until enabled in system Settings."
            }
        }
    )

    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Text(
                    text = "Allow Daily Festival & Muhurat Alerts?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "UtsavSetu calculates daily festivals, Tithi, Abhijit Muhurat, and Brahma Muhurat locally offline for ${effectivePrefs.hostCity.name}. Notification permission is used strictly to deliver your configured reminders — never for spam or marketing.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        onMarkRationaleShown?.invoke()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier.testTag("confirm_notification_rationale_button")
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    val morningTimePresets = remember {
        listOf(
            Pair(6, 0) to "6:00 AM",
            Pair(6, 30) to "6:30 AM",
            Pair(7, 0) to "7:00 AM",
            Pair(7, 30) to "7:30 AM",
            Pair(8, 0) to "8:00 AM"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .testTag("reminder_centre_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminder Centre",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Reminder Centre")
                    }
                }

                Text(
                    text = "Offline local scheduling for ${effectivePrefs.hostCity.name} (${LocationCityResolver.formatTimeZoneSummary(effectivePrefs.hostCity.timeZoneId)}). Automatically reschedules on reboot, city change, or time-zone change.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Daily Morning Festival & Muhurat Card
                Text(
                    text = "DAILY MORNING FESTIVAL & MUHURAT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTokens.decorativeGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Morning Notification",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Festival days: Festival & Muhurat. Other days: Tithi with Abhijit & Brahma Muhurat.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = dailyEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ReminderScheduler.hasPostNotificationsPermission(context)) {
                                        if (!effectivePrefs.notificationRationaleShown) {
                                            showPermissionRationaleDialog = true
                                        } else {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    dailyEnabled = enabled
                                    onUpdateDailyMorning?.invoke(enabled, selectedHour, selectedMinute)
                                    diagnosticsRefreshTick += 1
                                },
                                modifier = Modifier.testTag("daily_morning_switch")
                            )
                        }

                        if (dailyEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Preferred morning time: ${DailyMorningNotificationEngine.formatTimeOfDay(selectedHour, selectedMinute)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Preset chips (6:00 AM .. 8:00 AM, default 7:00 AM)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                morningTimePresets.take(3).forEach { (hm, label) ->
                                    val selected = selectedHour == hm.first && selectedMinute == hm.second
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            selectedHour = hm.first
                                            selectedMinute = hm.second
                                            onUpdateDailyMorning?.invoke(true, hm.first, hm.second)
                                            diagnosticsRefreshTick += 1
                                        },
                                        label = {
                                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("daily_time_preset_${hm.first}_${hm.second}")
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                morningTimePresets.drop(3).forEach { (hm, label) ->
                                    val selected = selectedHour == hm.first && selectedMinute == hm.second
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            selectedHour = hm.first
                                            selectedMinute = hm.second
                                            onUpdateDailyMorning?.invoke(true, hm.first, hm.second)
                                            diagnosticsRefreshTick += 1
                                        },
                                        label = {
                                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("daily_time_preset_${hm.first}_${hm.second}")
                                    )
                                }
                                // Stepper -15/+15 min for custom morning time
                                OutlinedButton(
                                    onClick = {
                                        val totalMin = (selectedHour * 60 + selectedMinute + 15) % (24 * 60)
                                        selectedHour = totalMin / 60
                                        selectedMinute = totalMin % 60
                                        onUpdateDailyMorning?.invoke(true, selectedHour, selectedMinute)
                                        diagnosticsRefreshTick += 1
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("daily_time_plus_15_button")
                                ) {
                                    Text("+15m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        permissionStatusBanner?.let { note ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Honest Notification Diagnostics & Daily Delivery Audit
                AnimatedCollapsibleSection(
                    title = "Notification Diagnostics & Delivery Audit",
                    subtitle = "${diagnostics.schedulingMode.displayLabel} · ${if (diagnostics.postNotificationsGranted && diagnostics.notificationsEnabledInSystem) "Allowed" else "Blocked"}",
                    initiallyExpanded = false,
                    testTagName = "notification_diagnostics_section"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DiagnosticStatusRow(
                            label = "Android Permission (POST_NOTIFICATIONS)",
                            value = if (diagnostics.postNotificationsGranted && diagnostics.notificationsEnabledInSystem) {
                                "Granted"
                            } else {
                                "Blocked / Denied"
                            },
                            isHealthy = diagnostics.postNotificationsGranted && diagnostics.notificationsEnabledInSystem
                        )
                        DiagnosticStatusRow(
                            label = "Scheduling Mode",
                            value = diagnostics.schedulingMode.displayLabel,
                            isHealthy = diagnostics.schedulingMode == SchedulingMode.EXACT_ALARM
                        )

                        val nextDeliveryText = diagnostics.nextTriggerEpochMillis?.let { ms ->
                            val zone = DailyMorningNotificationEngine.resolveZoneId(diagnostics.timeZoneId)
                            val zdt = Instant.ofEpochMilli(ms).atZone(zone)
                            val prefix = if (diagnostics.schedulingMode == SchedulingMode.WORKMANAGER_APPROXIMATE) "Approx. " else ""
                            "$prefix${zdt.format(DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.ENGLISH))} (${zone.id})"
                        } ?: "Not scheduled"
                        DiagnosticStatusRow(
                            label = "Next Scheduled Delivery",
                            value = nextDeliveryText,
                            isHealthy = diagnostics.nextTriggerEpochMillis != null
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = diagnostics.schedulingHonestyNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        if (!diagnostics.postNotificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            OutlinedButton(
                                onClick = {
                                    if (!effectivePrefs.notificationRationaleShown) {
                                        showPermissionRationaleDialog = true
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("diagnostics_request_permission_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Grant Notification Permission", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                isSendingTestNotification = true
                                testNotificationStatus = null
                                scope.launch {
                                    val resultState = ReminderScheduler.deliverDailyMorningNotification(
                                        context = context,
                                        mode = diagnostics.schedulingMode,
                                        forceDiagnosticTest = true,
                                        overridePreferences = effectivePrefs.copy(
                                            dailyMorningReminderEnabled = dailyEnabled,
                                            dailyMorningHour = selectedHour,
                                            dailyMorningMinute = selectedMinute
                                        )
                                    )
                                    isSendingTestNotification = false
                                    diagnosticsRefreshTick += 1
                                    testNotificationStatus = when (resultState) {
                                        DeliveryAuditState.POSTED ->
                                            "Test notification posted to Android notification tray (check status bar; visibility depends on DND/system settings)."
                                        DeliveryAuditState.PERMISSION_BLOCKED ->
                                            "Blocked: Android notification permission is denied or disabled."
                                        else ->
                                            "Test notification ended with status: ${resultState.label}."
                                    }
                                }
                            },
                            enabled = !isSendingTestNotification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_test_notification_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSendingTestNotification) "Sending Test..." else "Send Test Notification",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        testNotificationStatus?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("test_notification_status_text")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "DAILY DELIVERY AUDIT (LATEST ${diagnostics.auditEntries.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = glassTokens.decorativeGold
                        )
                        Text(
                            text = "Tracks local scheduling and Android tray handoff only; does not confirm whether a notification was viewed.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (diagnostics.auditEntries.isEmpty()) {
                            Text(
                                text = "No delivery audit events recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            diagnostics.auditEntries.take(6).forEach { entry ->
                                val stamp = Instant.ofEpochMilli(entry.timestampMillis)
                                    .atZone(DailyMorningNotificationEngine.resolveZoneId(entry.timeZoneId))
                                    .format(DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.ENGLISH))
                                val badgeColor = when (entry.state) {
                                    DeliveryAuditState.POSTED -> glassTokens.auspiciousGreen
                                    DeliveryAuditState.SCHEDULED -> MaterialTheme.colorScheme.primary
                                    DeliveryAuditState.ATTEMPTED -> MaterialTheme.colorScheme.secondary
                                    DeliveryAuditState.SKIPPED_DUPLICATE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    DeliveryAuditState.PERMISSION_BLOCKED,
                                    DeliveryAuditState.FAILED -> MaterialTheme.colorScheme.error
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = glassTokens.solidCardContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = entry.state.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                            Text(
                                                text = "$stamp · ${entry.cityName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = entry.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SCHEDULED FESTIVAL REMINDERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTokens.decorativeGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reminders.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No custom festival reminders yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Open any festival and tap 'Add Reminder' to schedule a custom alert.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    reminders.forEach { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.festivalName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${entry.dateFormatted} · ${entry.triggerDisplayLabel} at ${entry.timeLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteReminder(entry) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete reminder for ${entry.festivalName}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AUTOMATIC FESTIVAL ALERTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassTokens.decorativeGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReminderRuleToggle(
                    title = "One day before (7:00 PM)",
                    subtitle = "Samagri preparation reminder",
                    checked = dayBeforeEnabled,
                    onCheckedChange = { onUpdateGlobalRules(it, morningOfEnabled, muhuratStartEnabled) }
                )
                ReminderRuleToggle(
                    title = "On the festival day (6:30 AM)",
                    subtitle = "Morning tithi & observance alert",
                    checked = morningOfEnabled,
                    onCheckedChange = { onUpdateGlobalRules(dayBeforeEnabled, it, muhuratStartEnabled) }
                )
                ReminderRuleToggle(
                    title = "At Puja Muhurat start",
                    subtitle = "City-specific auspicious window alert",
                    checked = muhuratStartEnabled,
                    onCheckedChange = { onUpdateGlobalRules(dayBeforeEnabled, morningOfEnabled, it) }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStatusRow(
    label: String,
    value: String,
    isHealthy: Boolean
) {
    val glassTokens = LocalLiquidGlassTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isHealthy) glassTokens.auspiciousGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReminderRuleToggle(
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
