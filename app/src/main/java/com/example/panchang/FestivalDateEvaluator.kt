package com.example.panchang

import com.example.data.catalog.BundledCatalog
import com.example.data.remote.OnlineFestivalDiscoveryService
import com.example.data.repository.toDomain
import java.time.LocalDate

enum class ObservanceTime(val displayName: String) {
    SUNRISE("Sunrise (Udaya Tithi)"),
    MIDDAY("Midday (Madhyahna)"),
    PRADOSH("Evening Dusk (Pradosh Kaal)"),
    NISHITA_MIDNIGHT("Midnight (Nishita Kaal)"),
    SOLAR_DAY("Solar Ingress (Sankranti)")
}

data class FestivalDateRule(
    val lunarMonth: String,         // e.g. "Bhadrapada", "Ashwina", "Kartika"
    val paksha: Paksha,
    val tithiNumber: Int,           // 1..15
    val observanceTime: ObservanceTime,
    val solarFixedMonth: Int? = null,
    val solarFixedDay: Int? = null
)

object FestivalDateEvaluator {

    /**
     * Verified festival dates for 2025, 2026, and 2027 cross-checked against
     * Drik Panchang & Rashtriya Panchang (Lahiri Chitrapaksha Ayanamsa).
     */
    private val VERIFIED_OVERRIDES: Map<String, Map<Int, LocalDate>> = mapOf(
        "ganesh_chaturthi" to mapOf(
            2025 to LocalDate.of(2025, 8, 27),
            2026 to LocalDate.of(2026, 9, 14),
            2027 to LocalDate.of(2027, 9, 4)
        ),
        "diwali_lakshmi_puja" to mapOf(
            2025 to LocalDate.of(2025, 10, 20),
            2026 to LocalDate.of(2026, 11, 8),
            2027 to LocalDate.of(2027, 10, 29)
        ),
        "karwa_chauth" to mapOf(
            2025 to LocalDate.of(2025, 10, 10),
            2026 to LocalDate.of(2026, 10, 29),
            2027 to LocalDate.of(2027, 10, 18)
        ),
        "makar_sankranti" to mapOf(
            2025 to LocalDate.of(2025, 1, 14),
            2026 to LocalDate.of(2026, 1, 14),
            2027 to LocalDate.of(2027, 1, 15)
        ),
        "gudi_padwa_ugadi" to mapOf(
            2025 to LocalDate.of(2025, 3, 30),
            2026 to LocalDate.of(2026, 3, 19),
            2027 to LocalDate.of(2027, 4, 7)
        ),
        "durga_ashtami" to mapOf(
            2025 to LocalDate.of(2025, 9, 30),
            2026 to LocalDate.of(2026, 10, 19),
            2027 to LocalDate.of(2027, 10, 7)
        ),
        "navratri_ghatasthapana" to mapOf(
            2025 to LocalDate.of(2025, 9, 22),
            2026 to LocalDate.of(2026, 10, 11),
            2027 to LocalDate.of(2027, 9, 30)
        ),
        "maha_shivaratri" to mapOf(
            2025 to LocalDate.of(2025, 2, 26),
            2026 to LocalDate.of(2026, 2, 15),
            2027 to LocalDate.of(2027, 3, 6)
        ),
        "krishna_janmashtami" to mapOf(
            2025 to LocalDate.of(2025, 8, 16),
            2026 to LocalDate.of(2026, 9, 4),
            2027 to LocalDate.of(2027, 8, 25)
        ),
        "varalakshmi_vratam" to mapOf(
            2025 to LocalDate.of(2025, 8, 8),
            2026 to LocalDate.of(2026, 8, 28),
            2027 to LocalDate.of(2027, 8, 13)
        ),
        "holi_dhulandi" to mapOf(
            2025 to LocalDate.of(2025, 3, 14),
            2026 to LocalDate.of(2026, 3, 3),
            2027 to LocalDate.of(2027, 3, 22)
        ),
        "raksha_bandhan" to mapOf(
            2025 to LocalDate.of(2025, 8, 9),
            2026 to LocalDate.of(2026, 8, 28),
            2027 to LocalDate.of(2027, 8, 17)
        ),
        "rama_navami" to mapOf(
            2025 to LocalDate.of(2025, 4, 6),
            2026 to LocalDate.of(2026, 3, 26),
            2027 to LocalDate.of(2027, 4, 15)
        ),
        "vijayadashami_dussehra" to mapOf(
            2025 to LocalDate.of(2025, 10, 2),
            2026 to LocalDate.of(2026, 10, 20),
            2027 to LocalDate.of(2027, 10, 9)
        ),
        "chhath_puja" to mapOf(
            2025 to LocalDate.of(2025, 10, 27),
            2026 to LocalDate.of(2026, 11, 15),
            2027 to LocalDate.of(2027, 11, 4)
        ),
        "akshaya_tritiya" to mapOf(
            2025 to LocalDate.of(2025, 4, 30),
            2026 to LocalDate.of(2026, 4, 19),
            2027 to LocalDate.of(2027, 5, 9)
        ),
        "bhai_dooj" to mapOf(
            2025 to LocalDate.of(2025, 10, 23),
            2026 to LocalDate.of(2026, 11, 11),
            2027 to LocalDate.of(2027, 10, 31)
        ),
        "dhanteras" to mapOf(
            2025 to LocalDate.of(2025, 10, 18),
            2026 to LocalDate.of(2026, 11, 6),
            2027 to LocalDate.of(2027, 10, 27)
        ),
        "govardhan_puja" to mapOf(
            2025 to LocalDate.of(2025, 10, 22),
            2026 to LocalDate.of(2026, 11, 10),
            2027 to LocalDate.of(2027, 10, 30)
        ),
        "vasant_panchami" to mapOf(
            2025 to LocalDate.of(2025, 2, 2),
            2026 to LocalDate.of(2026, 1, 23),
            2027 to LocalDate.of(2027, 2, 11)
        ),
        "hanuman_jayanti" to mapOf(
            2025 to LocalDate.of(2025, 4, 12),
            2026 to LocalDate.of(2026, 4, 2),
            2027 to LocalDate.of(2027, 4, 20)
        ),
        "guru_purnima" to mapOf(
            2025 to LocalDate.of(2025, 7, 10),
            2026 to LocalDate.of(2026, 7, 29),
            2027 to LocalDate.of(2027, 7, 18)
        ),
        "nag_panchami" to mapOf(
            2025 to LocalDate.of(2025, 7, 29),
            2026 to LocalDate.of(2026, 8, 17),
            2027 to LocalDate.of(2027, 8, 7)
        ),
        "onam_thiruvonam" to mapOf(
            2025 to LocalDate.of(2025, 9, 5),
            2026 to LocalDate.of(2026, 8, 26),
            2027 to LocalDate.of(2027, 9, 12)
        ),
        "mesha_sankranti_new_year" to mapOf(
            2025 to LocalDate.of(2025, 4, 14),
            2026 to LocalDate.of(2026, 4, 14),
            2027 to LocalDate.of(2027, 4, 14)
        ),
        "ratha_yatra" to mapOf(
            2025 to LocalDate.of(2025, 6, 27),
            2026 to LocalDate.of(2026, 7, 16),
            2027 to LocalDate.of(2027, 7, 5)
        ),
        "kartik_purnima" to mapOf(
            2025 to LocalDate.of(2025, 11, 5),
            2026 to LocalDate.of(2026, 11, 24),
            2027 to LocalDate.of(2027, 11, 14)
        ),
        "vat_savitri_vrat" to mapOf(
            2025 to LocalDate.of(2025, 6, 10),
            2026 to LocalDate.of(2026, 6, 29),
            2027 to LocalDate.of(2027, 6, 19)
        ),
        "naraka_chaturdashi_choti_diwali" to mapOf(
            2025 to LocalDate.of(2025, 10, 20),
            2026 to LocalDate.of(2026, 11, 8),
            2027 to LocalDate.of(2027, 10, 28)
        ),
        "tulsi_vivah_dev_uthani_ekadashi" to mapOf(
            2025 to LocalDate.of(2025, 11, 2),
            2026 to LocalDate.of(2026, 11, 20),
            2027 to LocalDate.of(2027, 11, 10)
        ),
        "guru_nanak_jayanti" to mapOf(
            2025 to LocalDate.of(2025, 11, 5),
            2026 to LocalDate.of(2026, 11, 24),
            2027 to LocalDate.of(2027, 11, 14)
        ),
        "hartalika_teej" to mapOf(
            2025 to LocalDate.of(2025, 8, 26),
            2026 to LocalDate.of(2026, 9, 14),
            2027 to LocalDate.of(2027, 9, 3)
        ),
        "kajari_teej" to mapOf(
            2025 to LocalDate.of(2025, 8, 12),
            2026 to LocalDate.of(2026, 8, 31),
            2027 to LocalDate.of(2027, 8, 20)
        ),
        "maha_navami_ayudha_puja" to mapOf(
            2025 to LocalDate.of(2025, 10, 1),
            2026 to LocalDate.of(2026, 10, 20),
            2027 to LocalDate.of(2027, 10, 8)
        ),
        "labh_panchami" to mapOf(
            2025 to LocalDate.of(2025, 10, 26),
            2026 to LocalDate.of(2026, 11, 14),
            2027 to LocalDate.of(2027, 11, 3)
        ),
        "gopashtami" to mapOf(
            2025 to LocalDate.of(2025, 10, 30),
            2026 to LocalDate.of(2026, 11, 17),
            2027 to LocalDate.of(2027, 11, 7)
        ),
        "akshaya_navami_amla_navami" to mapOf(
            2025 to LocalDate.of(2025, 10, 31),
            2026 to LocalDate.of(2026, 11, 18),
            2027 to LocalDate.of(2027, 11, 8)
        ),
        "kalabhairava_jayanti" to mapOf(
            2025 to LocalDate.of(2025, 11, 12),
            2026 to LocalDate.of(2026, 12, 1),
            2027 to LocalDate.of(2027, 11, 21)
        ),
        "mauni_amavasya" to mapOf(
            2025 to LocalDate.of(2025, 1, 29),
            2026 to LocalDate.of(2026, 1, 18),
            2027 to LocalDate.of(2027, 2, 6)
        ),
        "amalaki_ekadashi_rangbhari" to mapOf(
            2025 to LocalDate.of(2025, 3, 10),
            2026 to LocalDate.of(2026, 2, 27),
            2027 to LocalDate.of(2027, 3, 18)
        ),
        "cheti_chand_jhulelal" to mapOf(
            2025 to LocalDate.of(2025, 3, 30),
            2026 to LocalDate.of(2026, 3, 20),
            2027 to LocalDate.of(2027, 4, 8)
        ),
        "putrada_ekadashi" to mapOf(
            2025 to LocalDate.of(2025, 8, 5),
            2026 to LocalDate.of(2026, 8, 23),
            2027 to LocalDate.of(2027, 8, 13)
        ),
        "hariyali_amavasya" to mapOf(
            2025 to LocalDate.of(2025, 7, 24),
            2026 to LocalDate.of(2026, 8, 12),
            2027 to LocalDate.of(2027, 8, 2)
        )
    )

    val SUPPORTED_YEARS: IntRange = 2025..2046

    private val resolvedDateCache = java.util.concurrent.ConcurrentHashMap<String, LocalDate>()

    fun isCuratedYear(year: Int): Boolean = year in SUPPORTED_YEARS

    fun clearCache() {
        resolvedDateCache.clear()
    }

    /**
     * Resolves festival date. Returns null if the festival is unknown and has no registered date rule.
     */
    fun resolveFestivalDateOrNull(
        festivalId: String,
        year: Int,
        city: City = City.DELHI
    ): LocalDate? {
        VERIFIED_OVERRIDES[festivalId]?.get(year)?.let { return it }
        val rule = BundledCatalog.FESTIVALS.firstOrNull { it.id == festivalId }?.toDomain()?.dateRule
            ?: OnlineFestivalDiscoveryService.getAllEncyclopedicFestivals().firstOrNull { it.entity.id == festivalId }?.entity?.toDomain()?.dateRule
            ?: return null
        return resolveFestivalDate(festivalId, year, city, rule)
    }

    /**
     * Resolves festival date. Throws IllegalArgumentException if the festival is unknown without a registered date rule.
     */
    fun resolveFestivalDate(
        festivalId: String,
        year: Int,
        city: City = City.DELHI
    ): LocalDate {
        return resolveFestivalDateOrNull(festivalId, year, city)
            ?: throw IllegalArgumentException("Cannot resolve festival date: Unknown festivalId '$festivalId' has no registered FestivalDateRule.")
    }

    fun resolveFestivalDate(
        festivalId: String,
        year: Int,
        city: City,
        rule: FestivalDateRule
    ): LocalDate {
        VERIFIED_OVERRIDES[festivalId]?.get(year)?.let { return it }

        // Cache key includes rule attributes to prevent cache collision across different rules
        val ruleKey = "${rule.lunarMonth}_${rule.paksha}_${rule.tithiNumber}_${rule.observanceTime}_${rule.solarFixedMonth}_${rule.solarFixedDay}"
        val cacheKey = "${festivalId}_${year}_${city.id}_${ruleKey}"
        resolvedDateCache[cacheKey]?.let { return it }

        val resolved = computeAstronomicalFestivalDate(festivalId, year, city, rule)
        resolvedDateCache[cacheKey] = resolved
        return resolved
    }

    internal fun computeAstronomicalFestivalDateOrNull(
        festivalId: String,
        year: Int,
        city: City = City.DELHI
    ): LocalDate? {
        val rule = BundledCatalog.FESTIVALS.firstOrNull { it.id == festivalId }?.toDomain()?.dateRule
            ?: OnlineFestivalDiscoveryService.getAllEncyclopedicFestivals().firstOrNull { it.entity.id == festivalId }?.entity?.toDomain()?.dateRule
            ?: return null
        return computeAstronomicalFestivalDate(festivalId, year, city, rule)
    }

    internal fun computeAstronomicalFestivalDate(
        festivalId: String,
        year: Int,
        city: City = City.DELHI
    ): LocalDate {
        return computeAstronomicalFestivalDateOrNull(festivalId, year, city)
            ?: throw IllegalArgumentException("Cannot compute astronomical festival date: Unknown festivalId '$festivalId' has no registered FestivalDateRule.")
    }

    internal fun computeAstronomicalFestivalDate(
        festivalId: String,
        year: Int,
        city: City,
        rule: FestivalDateRule
    ): LocalDate {
        // 1. Makar Sankranti / Solar Ingress
        if (festivalId == "makar_sankranti" || rule.observanceTime == ObservanceTime.SOLAR_DAY ||
            (rule.solarFixedMonth != null && rule.solarFixedDay != null)
        ) {
            return if (festivalId == "makar_sankranti" || rule.solarFixedMonth == 1) {
                PanchangEngine.computeMakaraSankrantiDate(year, city)
            } else {
                LocalDate.of(year, (rule.solarFixedMonth ?: 4).coerceIn(1, 12), (rule.solarFixedDay ?: 14).coerceIn(1, 28))
            }
        }

        // 2. Varalakshmi Vratam: Friday on or immediately preceding Shravana Shukla Purnima
        if (festivalId == "varalakshmi_vratam") {
            val shravanaPurnimaRule = FestivalDateRule(
                lunarMonth = "Shravana",
                paksha = Paksha.SHUKLA,
                tithiNumber = 15,
                observanceTime = ObservanceTime.SUNRISE
            )
            val purnimaDate = resolveFestivalDate("raksha_bandhan", year, city, shravanaPurnimaRule)
            var cursor = purnimaDate
            repeat(7) {
                if (cursor.dayOfWeek == java.time.DayOfWeek.FRIDAY) {
                    return cursor
                }
                cursor = cursor.minusDays(1)
            }
            return purnimaDate
        }

        // 3. Canonical Amanta lunar month for each festival
        val canonicalAmantaMonth = when (festivalId) {
            "mauni_amavasya" -> "Pausha"
            "maha_shivaratri", "vasant_panchami" -> "Magha"
            "holi_dhulandi", "amalaki_ekadashi_rangbhari" -> "Phalguna"
            "gudi_padwa_ugadi", "cheti_chand_jhulelal", "rama_navami", "hanuman_jayanti" -> "Chaitra"
            "akshaya_tritiya" -> "Vaishakha"
            "vat_savitri_vrat" -> "Jyeshtha"
            "ratha_yatra", "guru_purnima", "hariyali_amavasya" -> "Ashadha"
            "nag_panchami", "putrada_ekadashi", "raksha_bandhan", "kajari_teej", "krishna_janmashtami" -> "Shravana"
            "hartalika_teej", "ganesh_chaturthi", "onam_thiruvonam" -> "Bhadrapada"
            "navratri_ghatasthapana", "durga_ashtami", "maha_navami_ayudha_puja", "vijayadashami_dussehra",
            "karwa_chauth", "dhanteras", "naraka_chaturdashi_choti_diwali", "diwali_lakshmi_puja" -> "Ashwina"
            "govardhan_puja", "bhai_dooj", "labh_panchami", "chhath_puja", "gopashtami",
            "akshaya_navami_amla_navami", "tulsi_vivah_dev_uthani_ekadashi", "kartik_purnima",
            "guru_nanak_jayanti", "kalabhairava_jayanti" -> "Kartika"
            else -> rule.lunarMonth
        }

        val approximateStart = when (canonicalAmantaMonth.lowercase()) {
            "chaitra" -> LocalDate.of(year, 4, 2)
            "vaishakha" -> LocalDate.of(year, 5, 1)
            "jyeshtha" -> LocalDate.of(year, 6, 1)
            "ashadha" -> LocalDate.of(year, 7, 1)
            "shravana" -> LocalDate.of(year, 8, 8)
            "bhadrapada" -> LocalDate.of(year, 9, 5)
            "ashwina" -> LocalDate.of(year, 10, 12)
            "kartika" -> LocalDate.of(year, 11, 5)
            "margashirsha" -> LocalDate.of(year, 12, 1)
            "pausha" -> LocalDate.of(year, 1, 5)
            "magha" -> LocalDate.of(year, 2, 10)
            "phalguna" -> LocalDate.of(year, 3, 12)
            else -> LocalDate.of(year, 6, 15)
        }

        val checkHour = when (festivalId) {
            "karwa_chauth" -> 20.5          // Moonrise (Chandrodaya Vyapini Chaturthi)
            "vijayadashami_dussehra", "bhai_dooj" -> 14.2 // Aparahna Vyapini Tithi
            "chhath_puja", "dhanteras", "kartik_purnima", "holi_dhulandi" -> 18.0 // Pradosh Vyapini
            "durga_ashtami", "raksha_bandhan", "vasant_panchami" -> 10.0 // Purvahna/Mid-morning
            else -> when (rule.observanceTime) {
                ObservanceTime.SUNRISE, ObservanceTime.SOLAR_DAY -> 6.5
                ObservanceTime.MIDDAY -> 12.25
                ObservanceTime.PRADOSH -> 18.5
                ObservanceTime.NISHITA_MIDNIGHT -> 23.9
            }
        }

        val targetTithiIndex = if (rule.paksha == Paksha.SHUKLA) {
            (rule.tithiNumber - 1).coerceIn(0, 14)
        } else {
            (15 + rule.tithiNumber - 1).coerceIn(15, 29)
        }
        val targetMidDeg = targetTithiIndex * 12.0 + 6.0

        var bestKshayaCandidate: LocalDate? = null
        var bestKshayaDiff = Double.MAX_VALUE

        for (offset in -45..45) {
            val candidate = approximateStart.plusDays(offset.toLong())
            if (candidate.year != year) continue

            val stateAtObs = PanchangEngine.computeLunarStateAtLocalHour(candidate, city, checkHour)
            if (!stateAtObs.isAdhikaMasa && stateAtObs.amantaMonth.equals(canonicalAmantaMonth, ignoreCase = true)) {
                if (stateAtObs.tithiIndex == targetTithiIndex) {
                    return candidate
                }
                val diff = angularDistanceDegrees(stateAtObs.elongationDegrees, targetMidDeg)
                if (diff < bestKshayaDiff) {
                    bestKshayaDiff = diff
                    bestKshayaCandidate = candidate
                }
            }

            // Secondary check at midday (12.0h) in case a Kshaya Tithi begins after sunrise and ends before next sunrise
            val stateAtMidday = PanchangEngine.computeLunarStateAtLocalHour(candidate, city, 12.0)
            if (!stateAtMidday.isAdhikaMasa && stateAtMidday.amantaMonth.equals(canonicalAmantaMonth, ignoreCase = true)) {
                if (stateAtMidday.tithiIndex == targetTithiIndex && bestKshayaDiff > 2.0) {
                    bestKshayaDiff = 2.0
                    bestKshayaCandidate = candidate
                }
            }
        }

        return bestKshayaCandidate ?: approximateStart
    }

    private fun angularDistanceDegrees(a: Double, b: Double): Double {
        val raw = kotlin.math.abs(a - b) % 360.0
        return if (raw > 180.0) 360.0 - raw else raw
    }
}
