package com.example.domain.model

import com.example.panchang.FestivalDateRule
import com.example.panchang.ObservanceTime
import com.example.panchang.Paksha
import com.example.panchang.RegionalTradition
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class GuidanceScope(val label: String, val description: String) {
    UNIVERSALLY_PRESCRIBED(
        label = "Shastra Prescribed (Universal)",
        description = "Universal injunction found in core Dharma Shastras and classical Puranas, uniformly observed across traditions."
    ),
    REGIONAL_VARIATION(
        label = "Regional Tradition Variation",
        description = "Observance practices and samagri vary significantly by regional sampradaya (e.g. North vs South vs Bengal vs Gujarat/Maharashtra)."
    ),
    GENERAL_FAMILY_PRACTICE(
        label = "General Grihastha Family Practice",
        description = "Recommended for household (Grihastha) worship; elaborate temple rituals or Tantric/Agamic rites are simplified for domestic sattvik practice."
    )
}

data class ScriptureSource(
    val scriptureName: String,
    val chapterVerseCitation: String,
    val originalSanskritRef: String? = null
)

data class EditorialReviewProvenance(
    val reviewerName: String,
    val credentials: String,
    val reviewDate: String,
    val lastRevisionDate: String,
    val regionalTradition: RegionalTradition,
    val guidanceScope: GuidanceScope = GuidanceScope.GENERAL_FAMILY_PRACTICE,
    val alternativePractices: List<String> = emptyList()
)

data class VidhiStep(
    val stepNumber: Int,
    val title: String,
    val body: String,
    val durationMinutes: Int? = null,
    val scripturalSource: ScriptureSource? = null
)

data class SamagriItem(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val notes: String? = null,
    val isUniversallyPrescribed: Boolean = true
)

data class Festival(
    val id: String,
    val version: Int,
    val nameEnglish: String,
    val nameNative: String,
    val nativeScriptLabel: String,
    val aliases: List<String>,
    val traditions: List<RegionalTradition>,
    val dateRule: FestivalDateRule,
    val muhuratTitle: String,
    val muhuratDescription: String,
    val muhuratType: String,
    val vidhiSteps: List<VidhiStep>,
    val samagriItems: List<SamagriItem>,
    val significance: String,
    val sources: String,
    val reviewedBy: String
)

sealed class MuhuratValidationState {

    data class Verified(
        val startInstant: ZonedDateTime,
        val endInstant: ZonedDateTime,
        val festivalDate: LocalDate,
        val cityId: String,
        val calculationConvention: String,
        val verificationProvenance: String,
        val formattedWindow: String
    ) : MuhuratValidationState() {
        val startTime: LocalTime get() = startInstant.toLocalTime()
        val endTime: LocalTime get() = endInstant.toLocalTime()
        val crossesMidnight: Boolean get() = endInstant.toLocalDate().isAfter(startInstant.toLocalDate())
        val isValid: Boolean get() = endInstant.isAfter(startInstant) &&
                Duration.between(startInstant, endInstant).toHours() in 0..23
    }

    data class CalculatedEstimate(
        val formattedWindow: String,
        val calculationMethod: String = "Algorithmic Dinamana division",
        val confidence: String = "Estimated"
    ) : MuhuratValidationState()

    data class Unavailable(
        val reason: String = "Timing Unavailable"
    ) : MuhuratValidationState()

    val isSchedulable: Boolean
        get() = this is Verified && this.isValid

    val displayLabel: String
        get() = when (this) {
            is Verified -> formattedWindow
            is CalculatedEstimate -> formattedWindow
            is Unavailable -> reason
        }

    data class ParsedTimeRange(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val crossesMidnight: Boolean
    )

    companion object {

        fun parseTime(raw: String): LocalTime? {
            return try {
                val clean = raw.substringBefore("(").trim().uppercase(Locale.US)
                if (clean.endsWith("AM") || clean.endsWith("PM")) {
                    val normalized = if (clean.indexOf(':') == 1) "0$clean" else clean
                    LocalTime.parse(normalized, DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
                } else {
                    val normalized = if (clean.indexOf(':') == 1) "0$clean" else clean
                    LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm", Locale.US))
                }
            } catch (e: Exception) {
                null
            }
        }

        fun parseTimeRange(formattedWindow: String?): ParsedTimeRange? {
            if (formattedWindow.isNullOrBlank()) return null
            val clean = formattedWindow.trim()
            if (!clean.contains("-")) return null
            val parts = clean.split("-").map { it.trim() }
            if (parts.size != 2) return null
            val start = parseTime(parts[0]) ?: return null
            val end = parseTime(parts[1]) ?: return null
            val crossesMidnight = end.isBefore(start) || (end == start)
            return ParsedTimeRange(
                startTime = start,
                endTime = end,
                crossesMidnight = crossesMidnight
            )
        }

        /**
         * Resolves raw formatted strings without provenance into a non-schedulable state.
         * A syntactically valid range produces [CalculatedEstimate], NEVER [Verified].
         * Blank, missing, N/A, or malformed strings produce [Unavailable].
         */
        fun resolve(formattedWindow: String?): MuhuratValidationState {
            if (formattedWindow.isNullOrBlank()) return Unavailable()
            val clean = formattedWindow.trim()
            if (clean.equals("N/A", ignoreCase = true) ||
                clean.contains("Unavailable", ignoreCase = true) ||
                clean.contains("Pending", ignoreCase = true)
            ) {
                return Unavailable()
            }

            val parsed = parseTimeRange(clean)
            return if (parsed != null) {
                CalculatedEstimate(clean)
            } else {
                Unavailable("Malformed timing string")
            }
        }

        /**
         * Explicit factory for verified muhurat timings. Requires documented verification provenance,
         * date, location, timezone, and convention.
         * Date-aware: correctly handles windows spanning midnight (such as Nishita Kaal) without
         * altering the festival's intended calendar date.
         */
        fun createVerified(
            festivalDate: LocalDate,
            cityId: String,
            startTime: LocalTime,
            endTime: LocalTime,
            zoneId: ZoneId = ZoneId.of("Asia/Kolkata"),
            calculationConvention: String = "Drik Ganita / Chitrapaksha Lahiri",
            verificationProvenance: String,
            formattedWindow: String? = null
        ): MuhuratValidationState {
            if (verificationProvenance.isBlank()) {
                val windowStr = formattedWindow ?: "${startTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))} - ${endTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))}"
                return CalculatedEstimate(windowStr, "Missing verification provenance")
            }

            val startInstant = festivalDate.atTime(startTime).atZone(zoneId)
            val endInstant = if (endTime.isBefore(startTime) || endTime == startTime) {
                // Window crosses midnight into the following morning
                festivalDate.plusDays(1).atTime(endTime).atZone(zoneId)
            } else {
                festivalDate.atTime(endTime).atZone(zoneId)
            }

            val windowStr = formattedWindow ?: "${startTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))} - ${endTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))}"

            val verified = Verified(
                startInstant = startInstant,
                endInstant = endInstant,
                festivalDate = festivalDate,
                cityId = cityId,
                calculationConvention = calculationConvention,
                verificationProvenance = verificationProvenance,
                formattedWindow = windowStr
            )

            return if (verified.isValid) {
                verified
            } else {
                Unavailable("Invalid chronological order in window: end does not follow start")
            }
        }
    }
}

data class FestivalOccurrence(
    val festival: Festival,
    val date: LocalDate,
    val muhuratWindowFormatted: String,
    val muhuratState: MuhuratValidationState = MuhuratValidationState.resolve(muhuratWindowFormatted)
)

data class SamagriProgress(
    val festivalId: String,
    val year: Int,
    val checkedItemIds: Set<String>,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isChecked(itemId: String): Boolean = checkedItemIds.contains(itemId)

    fun completionFraction(totalItems: Int): Float {
        if (totalItems <= 0) return 0f
        return (checkedItemIds.size.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
    }
}
