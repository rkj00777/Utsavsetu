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
import com.example.domain.model.Festival
import com.example.platform.share.ShareHelper
import com.example.ui.theme.LocalLiquidGlassTokens
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
 * View and manage scheduled festival reminders and global reminder rules.
 */
@Composable
fun ReminderCentreDialog(
    reminders: List<CustomReminderEntry>,
    dayBeforeEnabled: Boolean,
    morningOfEnabled: Boolean,
    muhuratStartEnabled: Boolean,
    onDeleteReminder: (CustomReminderEntry) -> Unit,
    onUpdateGlobalRules: (Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val glassTokens = LocalLiquidGlassTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 460.dp)
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
                    text = "Locally scheduled reminders fire offline and reschedule automatically after device reboot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
