package com.example.panchang

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class Paksha(val displayName: String) {
    SHUKLA("Shukla Paksha (Waxing)"),
    KRISHNA("Krishna Paksha (Waning)")
}

enum class RegionalTradition(
    val id: String,
    val displayName: String,
    val scriptLabel: String,
    val defaultConvention: LunarConvention
) {
    NORTH_INDIAN("NORTH_INDIAN", "North Indian (Hindi)", "उत्तर भारतीय", LunarConvention.PURNIMANTA),
    MAHARASHTRIAN("MAHARASHTRIAN", "Maharashtrian & Konkani (Marathi)", "महाराष्ट्रीयन", LunarConvention.AMANTA),
    TAMIL("TAMIL", "Tamil & South Indian (Tamil)", "தமிழ் மரபு", LunarConvention.AMANTA),
    KANNADA_TELUGU("KANNADA_TELUGU", "Kannada & Telugu (Ugadi)", "ಕನ್ನಡ / తెలుగు", LunarConvention.AMANTA),
    BENGALI("BENGALI", "Bengali & Eastern (Bangla)", "বাঙালি উৎসব", LunarConvention.AMANTA),
    GUJARATI("GUJARATI", "Gujarati (Vikram Samvat)", "ગુજરાતી સંસ્કૃતિ", LunarConvention.AMANTA)
}

enum class LunarConvention {
    AMANTA,     // New moon to new moon (South & West India)
    PURNIMANTA  // Full moon to full moon (North India)
}

data class TimeWindow(
    val startMinutes: Int,
    val endMinutes: Int
) {
    val durationMinutes: Int get() = endMinutes - startMinutes

    fun format(): String {
        return "${formatMinutes12Hour(startMinutes)} - ${formatMinutes12Hour(endMinutes)}"
    }

    fun format24Hour(): String {
        val sh = ((startMinutes / 60) % 24 + 24) % 24
        val sm = ((startMinutes % 60) + 60) % 60
        val eh = ((endMinutes / 60) % 24 + 24) % 24
        val em = ((endMinutes % 60) + 60) % 60
        return String.format(Locale.US, "%02d:%02d - %02d:%02d", sh, sm, eh, em)
    }

    fun containsMinutes(minutes: Int): Boolean {
        return minutes in startMinutes..endMinutes
    }

    companion object {
        fun formatMinutes12Hour(totalMinutes: Int): String {
            val normalized = ((totalMinutes % 1440) + 1440) % 1440
            val hour24 = normalized / 60
            val minute = normalized % 60
            val amPm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
        }
    }
}

data class TithiInfo(
    val index: Int,            // 1..30 (1..15 Shukla, 16..30 Krishna)
    val name: String,          // e.g. "Chaturthi", "Purnima", "Amavasya"
    val paksha: Paksha,
    val endTime: String? = null // e.g. "07:25 PM" or "Full day"
)

data class NakshatraInfo(
    val index: Int,            // 1..27
    val name: String,
    val deity: String = "",
    val endTime: String? = null
)

data class YogaInfo(
    val index: Int,            // 1..27
    val name: String
)

data class KaranaInfo(
    val index: Int,            // 1..11
    val name: String
)

data class PanchangDay(
    val date: LocalDate,
    val city: City,
    val sunrise: LocalTime,
    val sunset: LocalTime,
    val tithi: TithiInfo,
    val nakshatra: NakshatraInfo,
    val yoga: YogaInfo,
    val karana: KaranaInfo,
    val rahuKaal: TimeWindow,
    val abhijitMuhurat: TimeWindow?,
    val amantaMonth: String,
    val purnimantaMonth: String,
    val ayanamsaDegrees: Double = 24.23,
    val brahmaMuhurat: TimeWindow = TimeWindow(270, 318),
    val yamagandam: TimeWindow = TimeWindow(450, 540),
    val gulikaKaal: TimeWindow = TimeWindow(540, 630),
    val isAdhikaMasa: Boolean = false
) {
    val dayOfWeek: String = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

    fun formattedSunrise(): String = sunrise.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    fun formattedSunset(): String = sunset.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))

    fun getMonthNameForConvention(convention: LunarConvention): String {
        val base = if (convention == LunarConvention.PURNIMANTA) purnimantaMonth else amantaMonth
        return if (isAdhikaMasa) "Adhika $base" else base
    }
}
