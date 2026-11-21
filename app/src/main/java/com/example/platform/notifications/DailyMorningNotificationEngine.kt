package com.example.platform.notifications

import android.content.Context
import com.example.domain.model.FestivalOccurrence
import com.example.domain.model.MuhuratValidationState
import com.example.panchang.City
import com.example.panchang.PanchangDay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SchedulingMode(val displayLabel: String) {
    EXACT_ALARM("Exact Alarm (AlarmManager)"),
    WORKMANAGER_APPROXIMATE("Approximate Delivery (WorkManager Fallback)"),
    DISABLED("Disabled")
}

/**
 * Distinct audit states for daily morning notification lifecycle.
 * Intentionally never claims the notification was seen or read by the user.
 */
enum class DeliveryAuditState(val label: String) {
    SCHEDULED("Scheduled"),
    ATTEMPTED("Attempted"),
    POSTED("Posted to Android"),
    PERMISSION_BLOCKED("Permission Blocked"),
    SKIPPED_DUPLICATE("Skipped (Already Posted)"),
    FAILED("Failed")
}

data class DailyDeliveryAuditEntry(
    val timestampMillis: Long,
    val localDate: String,
    val cityName: String,
    val timeZoneId: String,
    val mode: SchedulingMode,
    val state: DeliveryAuditState,
    val detail: String
)

data class NotificationDiagnosticsSnapshot(
    val postNotificationsGranted: Boolean,
    val notificationsEnabledInSystem: Boolean,
    val canScheduleExactAlarms: Boolean,
    val schedulingMode: SchedulingMode,
    val schedulingHonestyNote: String,
    val nextTriggerEpochMillis: Long?,
    val dailyMorningEnabled: Boolean,
    val configuredHour: Int,
    val configuredMinute: Int,
    val cityName: String,
    val timeZoneId: String,
    val lastDeliveredKey: String?,
    val auditEntries: List<DailyDeliveryAuditEntry>
) {
    val configuredTimeFormatted: String
        get() = DailyMorningNotificationEngine.formatTimeOfDay(configuredHour, configuredMinute)
}

data class DailyNotificationPayload(
    val title: String,
    val body: String,
    val festivalId: String?,
    val localDate: LocalDate,
    val hasFestival: Boolean,
    val hasMuhurat: Boolean
)

/**
 * Single-source offline daily morning notification payload builder, trigger time calculator,
 * deduplication guard, and delivery audit persistence store.
 *
 * Strictly delegates all astronomical/Muhurat timings to [PanchangDay] and [MuhuratValidationState].
 * Never fabricates or hardcodes fixed Muhurat clock values.
 */
object DailyMorningNotificationEngine {

    private const val PREFS_NAME = "utsavsetu_daily_morning_audit"
    private const val KEY_NEXT_TRIGGER_MS = "next_trigger_epoch_ms"
    private const val KEY_SCHEDULING_MODE = "scheduling_mode"
    private const val KEY_SCHEDULED_CITY = "scheduled_city_name"
    private const val KEY_SCHEDULED_TZ = "scheduled_timezone_id"
    private const val KEY_LAST_POSTED_KEY = "last_posted_date_city_key"
    private const val KEY_AUDIT_LOG = "daily_audit_log_entries"

    private const val MAX_AUDIT_ENTRIES = 15
    private const val FIELD_SEP = "\u001F"
    private const val RECORD_SEP = "\u001E"

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private val TIME_12H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    fun formatTimeOfDay(hour: Int, minute: Int): String {
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)).format(TIME_12H_FORMATTER)
    }

    fun resolveZoneId(timeZoneId: String?): ZoneId {
        return runCatching {
            if (!timeZoneId.isNullOrBlank()) ZoneId.of(timeZoneId) else ZoneId.systemDefault()
        }.getOrElse { ZoneId.systemDefault() }
    }

    /**
     * Computes the next strictly future occurrence of [hour]:[minute] in the selected [zoneId].
     */
    fun computeNextMorningTrigger(
        nowInstant: Instant,
        hour: Int,
        minute: Int,
        zoneId: ZoneId
    ): ZonedDateTime {
        val nowZoned = nowInstant.atZone(zoneId)
        val targetTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        val candidateToday = nowZoned.toLocalDate().atTime(targetTime).atZone(zoneId)
        return if (candidateToday.toInstant().isAfter(nowInstant)) {
            candidateToday
        } else {
            nowZoned.toLocalDate().plusDays(1).atTime(targetTime).atZone(zoneId)
        }
    }

    /**
     * Builds concise, date- and city-specific morning notification content strictly from
     * local [PanchangDay] and [FestivalOccurrence] data. Never invents timings.
     */
    fun buildDailyPayload(
        date: LocalDate,
        city: City,
        todaysOccurrences: List<FestivalOccurrence>,
        panchangDay: PanchangDay,
        includeBrahmaMuhurat: Boolean = true
    ): DailyNotificationPayload {
        val dateLabel = date.format(DATE_FORMATTER)

        if (todaysOccurrences.isNotEmpty()) {
            val primary = todaysOccurrences.first()
            val extraCount = todaysOccurrences.size - 1
            val title = if (extraCount > 0) {
                "Today's Festival – ${primary.festival.nameEnglish} (+$extraCount more)"
            } else {
                "Today's Festival – ${primary.festival.nameEnglish}"
            }

            val conciseDetails = trimConciseSentence(primary.festival.significance, maxLength = 96)

            val verifiedState = primary.muhuratState as? MuhuratValidationState.Verified
            val verifiedFestivalMuhurat = if (verifiedState != null && verifiedState.isValid) {
                val label = primary.festival.muhuratTitle
                    .takeIf { it.isNotBlank() }
                    ?: "Puja Muhurat"
                label to verifiedState.formattedWindow
            } else {
                null
            }

            val fallbackPanchangMuhurat = if (verifiedFestivalMuhurat == null) {
                panchangDay.abhijitMuhurat?.let { "Abhijit Muhurat" to it.format() }
            } else null

            val muhuratPair = verifiedFestivalMuhurat ?: fallbackPanchangMuhurat

            val body = if (muhuratPair != null) {
                "$conciseDetails ($dateLabel, ${city.name}). Muhurat: ${muhuratPair.first}, ${muhuratPair.second}."
            } else {
                "$conciseDetails ($dateLabel, ${city.name})."
            }

            return DailyNotificationPayload(
                title = title,
                body = body,
                festivalId = primary.festival.id,
                localDate = date,
                hasFestival = true,
                hasMuhurat = muhuratPair != null
            )
        }

        // Non-festival day: show Tithi with real calculated Abhijit and Brahma Muhurat timings
        val pakshaShort = panchangDay.tithi.paksha.displayName.substringBefore(" ")
        val tithiLabel = "$pakshaShort ${panchangDay.tithi.name}".trim()

        val muhuratParts = mutableListOf<String>()
        panchangDay.abhijitMuhurat?.let { abhijit ->
            muhuratParts.add("Abhijit Muhurat: ${abhijit.format()}")
        }
        if (includeBrahmaMuhurat) {
            muhuratParts.add("Brahma Muhurat: ${panchangDay.brahmaMuhurat.format()}")
        }

        if (muhuratParts.isNotEmpty()) {
            val title = "Daily Panchang – $tithiLabel"
            val body = "${city.name} ($dateLabel) • ${muhuratParts.joinToString(" · ")}."
            return DailyNotificationPayload(
                title = title,
                body = body,
                festivalId = null,
                localDate = date,
                hasFestival = false,
                hasMuhurat = true
            )
        }

        // Neither festival nor Muhurat timings available: concise honest notice, never inventing timings
        return DailyNotificationPayload(
            title = "Daily Panchang – ${city.name}",
            body = "$dateLabel: $tithiLabel. No festival or special Muhurat window applies today.",
            festivalId = null,
            localDate = date,
            hasFestival = false,
            hasMuhurat = false
        )
    }

    private fun trimConciseSentence(text: String, maxLength: Int): String {
        val clean = text.trim().replace(Regex("\\s+"), " ")
        if (clean.isEmpty()) return "Auspicious observance today"
        val firstSentence = clean.substringBefore(". ").trimEnd('.')
        return if (firstSentence.length <= maxLength) {
            firstSentence
        } else {
            firstSentence.take(maxLength - 1).trimEnd() + "…"
        }
    }

    fun deliveryDeduplicationKey(localDate: LocalDate, cityId: String): String {
        return "${localDate}_${cityId}"
    }

    fun hasAlreadyPostedFor(context: Context, localDate: LocalDate, cityId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastKey = prefs.getString(KEY_LAST_POSTED_KEY, null)
        return lastKey == deliveryDeduplicationKey(localDate, cityId)
    }

    fun markPostedFor(context: Context, localDate: LocalDate, cityId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_POSTED_KEY, deliveryDeduplicationKey(localDate, cityId)).apply()
    }

    fun getLastPostedKey(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_POSTED_KEY, null)
    }

    fun saveScheduleMetadata(
        context: Context,
        nextTriggerEpochMillis: Long?,
        mode: SchedulingMode,
        cityName: String,
        timeZoneId: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (nextTriggerEpochMillis != null) {
                putLong(KEY_NEXT_TRIGGER_MS, nextTriggerEpochMillis)
            } else {
                remove(KEY_NEXT_TRIGGER_MS)
            }
            putString(KEY_SCHEDULING_MODE, mode.name)
            putString(KEY_SCHEDULED_CITY, cityName)
            putString(KEY_SCHEDULED_TZ, timeZoneId)
        }.apply()
    }

    fun getNextTriggerEpochMillis(context: Context): Long? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_NEXT_TRIGGER_MS)) prefs.getLong(KEY_NEXT_TRIGGER_MS, 0L) else null
    }

    fun getStoredSchedulingMode(context: Context): SchedulingMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCHEDULING_MODE, null)
        return SchedulingMode.values().firstOrNull { it.name == raw } ?: SchedulingMode.DISABLED
    }

    fun recordAuditEntry(context: Context, entry: DailyDeliveryAuditEntry) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = getAuditEntries(context)
        val updated = (listOf(entry) + existing).take(MAX_AUDIT_ENTRIES)
        val serialized = updated.joinToString(RECORD_SEP) { e ->
            listOf(
                e.timestampMillis.toString(),
                e.localDate,
                e.cityName.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                e.timeZoneId,
                e.mode.name,
                e.state.name,
                e.detail.replace(FIELD_SEP, " ").replace(RECORD_SEP, " ")
            ).joinToString(FIELD_SEP)
        }
        prefs.edit().putString(KEY_AUDIT_LOG, serialized).apply()
    }

    fun getAuditEntries(context: Context): List<DailyDeliveryAuditEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUDIT_LOG, null)
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { record ->
            val parts = record.split(FIELD_SEP)
            if (parts.size < 7) return@mapNotNull null
            val ts = parts[0].toLongOrNull() ?: return@mapNotNull null
            val mode = SchedulingMode.values().firstOrNull { it.name == parts[4] } ?: SchedulingMode.DISABLED
            val state = DeliveryAuditState.values().firstOrNull { it.name == parts[5] } ?: DeliveryAuditState.FAILED
            DailyDeliveryAuditEntry(
                timestampMillis = ts,
                localDate = parts[1],
                cityName = parts[2],
                timeZoneId = parts[3],
                mode = mode,
                state = state,
                detail = parts[6]
            )
        }
    }

    fun clearAuditAndStateForTesting(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
