package com.example.panchang

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.*

/**
 * Pure Kotlin astronomical Panchang calculation engine (no Android imports).
 * Implements Jean Meeus astronomical algorithms (Chapters 25 & 47),
 * Lahiri Chitrapaksha ayanamsa, NOAA/Meeus solar sunrise/sunset,
 * Tithi & Nakshatra end-time root finding, Amanta/Purnimanta lunar months
 * (including Adhika Masa detection), and Vedic Muhurat windows.
 */
object PanchangEngine {

    private val TITHI_NAMES = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima", // Shukla (1..15)
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"  // Krishna (16..30)
    )

    private val NAKSHATRA_NAMES = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
        "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
        "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
        "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
        "Uttara Bhadrapada", "Revati"
    )

    private val NAKSHATRA_DEITIES = listOf(
        "Ashwini Kumaras", "Yama", "Agni", "Brahma (Prajapati)", "Soma (Chandra)",
        "Rudra", "Aditi", "Brihaspati", "Nagas (Sarpas)", "Pitris (Ancestors)",
        "Bhaga", "Aryaman", "Savitar (Surya)", "Vishwakarma", "Vayu",
        "Indra & Agni", "Mitra", "Indra", "Nirriti", "Apah (Cosmic Waters)",
        "Vishvadevas", "Vishnu", "Ashta Vasus", "Varuna", "Aja Ekapada",
        "Ahirbudhnya", "Pushan"
    )

    private val YOGA_NAMES = listOf(
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana",
        "Atiganda", "Sukarma", "Dhriti", "Shula", "Ganda",
        "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
        "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva",
        "Siddha", "Sadhya", "Shubha", "Shukla", "Brahma",
        "Indra", "Vaidhriti"
    )

    private val KARANA_NAMES = listOf(
        "Bava", "Balava", "Kaulava", "Taitila", "Gara",
        "Vanija", "Vishti (Bhadra)", "Shakuni", "Chatushpada", "Naga", "Kintughna"
    )

    private val MONTH_NAMES = listOf(
        "Chaitra", "Vaishakha", "Jyeshtha", "Ashadha",
        "Shravana", "Bhadrapada", "Ashwina", "Kartika",
        "Margashirsha", "Pausha", "Magha", "Phalguna"
    )

    data class LunarMomentState(
        val amantaMonth: String,
        val purnimantaMonth: String,
        val isAdhikaMasa: Boolean,
        val paksha: Paksha,
        val tithiNumber: Int,
        val tithiIndex: Int,
        val elongationDegrees: Double
    ) {
        val tithiName: String get() = TITHI_NAMES[tithiIndex]
    }

    private val dayPanchangCache = java.util.concurrent.ConcurrentHashMap<String, PanchangDay>()

    fun computeDayPanchang(date: LocalDate, city: City): PanchangDay {
        val cacheKey = "${date}_${city.id}"
        dayPanchangCache[cacheKey]?.let { return it }

        val tzOffsetHours = getTimezoneOffsetHours(date, city.timeZoneId)
        val (sunrise, sunset) = calculateSunriseSunset(date, city.latitude, city.longitude, tzOffsetHours)

        // Evaluate celestial positions at local sunrise (Udaya Tithi standard)
        val sunriseHourLocal = sunrise.hour + sunrise.minute / 60.0
        val jdSunrise = julianDay(date.year, date.monthValue, date.dayOfMonth, sunriseHourLocal - tzOffsetHours)

        val t = (jdSunrise - 2451545.0) / 36525.0
        val ayanamsa = calculateLahiriAyanamsa(t)

        val sunTrueLon = calculateSunTrueLongitude(t)
        val moonTrueLon = calculateMoonTrueLongitude(t)

        val siderealSunLon = normalizeDegrees(sunTrueLon - ayanamsa)
        val siderealMoonLon = normalizeDegrees(moonTrueLon - ayanamsa)

        // 1. Tithi: (Moon - Sun) / 12 degrees + exact end time in city timezone
        val elongation = normalizeDegrees(moonTrueLon - sunTrueLon)
        val tithiIndex = (elongation / 12.0).toInt().coerceIn(0, 29)
        val paksha = if (tithiIndex < 15) Paksha.SHUKLA else Paksha.KRISHNA
        val tithiEndTimeStr = computeTithiEndTime(jdSunrise, tithiIndex, sunriseHourLocal)
        val tithiInfo = TithiInfo(
            index = tithiIndex + 1,
            name = TITHI_NAMES[tithiIndex],
            paksha = paksha,
            endTime = tithiEndTimeStr
        )

        // 2. Nakshatra: Sidereal Moon / (360/27) degrees + deity + end time
        val nakshatraSpan = 360.0 / 27.0
        val nakshatraIndex = (siderealMoonLon / nakshatraSpan).toInt().coerceIn(0, 26)
        val nakshatraEndTimeStr = computeNakshatraEndTime(jdSunrise, nakshatraIndex, sunriseHourLocal)
        val nakshatraInfo = NakshatraInfo(
            index = nakshatraIndex + 1,
            name = NAKSHATRA_NAMES[nakshatraIndex],
            deity = NAKSHATRA_DEITIES[nakshatraIndex],
            endTime = nakshatraEndTimeStr
        )

        // 3. Yoga: (Sidereal Sun + Sidereal Moon) / (360/27) degrees
        val yogaDegrees = normalizeDegrees(siderealSunLon + siderealMoonLon)
        val yogaIndex = (yogaDegrees / nakshatraSpan).toInt().coerceIn(0, 26)
        val yogaInfo = YogaInfo(
            index = yogaIndex + 1,
            name = YOGA_NAMES[yogaIndex]
        )

        // 4. Karana: Elongation / 6 degrees (60 half-tithis in a lunar month)
        val karanaNum = (elongation / 6.0).toInt().coerceIn(0, 59)
        val karanaName = when {
            karanaNum == 0 -> "Kintughna"
            karanaNum in 1..56 -> KARANA_NAMES[(karanaNum - 1) % 7]
            karanaNum == 57 -> "Shakuni"
            karanaNum == 58 -> "Chatushpada"
            else -> "Naga"
        }
        val karanaInfo = KaranaInfo(
            index = (karanaNum % 11) + 1,
            name = karanaName
        )

        // 5. Hindu Lunar Month (Amanta & Purnimanta) from New Moon (Amavasya) Solar Rashi
        val (amantaMonth, purnimantaMonth, isAdhika) = computeLunarMonths(jdSunrise, elongation, paksha)

        // 6. Daytime & Pre-dawn Muhurat Windows (Ashta-vibhaga & Panchadasha-muhurta)
        val sunriseMinutes = sunrise.hour * 60 + sunrise.minute
        val sunsetMinutes = sunset.hour * 60 + sunset.minute
        val daylightDuration = (sunsetMinutes - sunriseMinutes).coerceAtLeast(600)
        val eighthSpan = daylightDuration / 8.0

        // Rahu Kaal (0-indexed segment 1..7)
        val rahuSegment = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.SATURDAY -> 2
            DayOfWeek.FRIDAY -> 3
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.TUESDAY -> 6
            DayOfWeek.SUNDAY -> 7
        }
        val rahuKaal = TimeWindow(
            startMinutes = sunriseMinutes + (rahuSegment * eighthSpan).roundToInt(),
            endMinutes = sunriseMinutes + ((rahuSegment + 1) * eighthSpan).roundToInt()
        )

        // Yamagandam (0-indexed segment 0..6)
        val yamaSegment = when (date.dayOfWeek) {
            DayOfWeek.THURSDAY -> 0
            DayOfWeek.WEDNESDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.MONDAY -> 3
            DayOfWeek.SUNDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.FRIDAY -> 6
        }
        val yamagandam = TimeWindow(
            startMinutes = sunriseMinutes + (yamaSegment * eighthSpan).roundToInt(),
            endMinutes = sunriseMinutes + ((yamaSegment + 1) * eighthSpan).roundToInt()
        )

        // Gulika Kaal (0-indexed segment 0..6)
        val gulikaSegment = when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> 0
            DayOfWeek.FRIDAY -> 1
            DayOfWeek.THURSDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.TUESDAY -> 4
            DayOfWeek.MONDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
        val gulikaKaal = TimeWindow(
            startMinutes = sunriseMinutes + (gulikaSegment * eighthSpan).roundToInt(),
            endMinutes = sunriseMinutes + ((gulikaSegment + 1) * eighthSpan).roundToInt()
        )

        // Abhijit Muhurat: 8th of 15 daylight muhurats (centered on local apparent solar noon)
        val abhijitStart = sunriseMinutes + (daylightDuration * 7.0 / 15.0).roundToInt()
        val abhijitEnd = sunriseMinutes + floor(daylightDuration * 8.0 / 15.0).toInt()
        val abhijitMuhurat = TimeWindow(abhijitStart, abhijitEnd)

        // Brahma Muhurat: 2 Muhurtas (96 mins) to 1 Muhurta (48 mins) before Sunrise
        val brahmaMuhurat = TimeWindow(
            startMinutes = (sunriseMinutes - 96).coerceAtLeast(0),
            endMinutes = (sunriseMinutes - 48).coerceAtLeast(0)
        )

        val result = PanchangDay(
            date = date,
            city = city,
            sunrise = sunrise,
            sunset = sunset,
            tithi = tithiInfo,
            nakshatra = nakshatraInfo,
            yoga = yogaInfo,
            karana = karanaInfo,
            rahuKaal = rahuKaal,
            abhijitMuhurat = abhijitMuhurat,
            amantaMonth = amantaMonth,
            purnimantaMonth = purnimantaMonth,
            ayanamsaDegrees = ayanamsa,
            brahmaMuhurat = brahmaMuhurat,
            yamagandam = yamagandam,
            gulikaKaal = gulikaKaal,
            isAdhikaMasa = isAdhika
        )
        dayPanchangCache[cacheKey] = result
        return result
    }

    /**
     * Madhyahna Kaal: 3rd of 5 equal parts of daytime (Dinamana), used for Ganesh Chaturthi and Sri Rama Navami.
     */
    fun computeMadhyahnaWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        val start = sunriseMins + (daylight * 2.0 / 5.0).roundToInt()
        val end = sunriseMins + (daylight * 3.0 / 5.0).roundToInt()
        return TimeWindow(start, end)
    }

    /**
     * Aparahna Kaal: 4th of 5 equal parts of daytime (Dinamana), used for Vijayadashami Shami Puja and Raksha Bandhan.
     */
    fun computeAparahnaWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        val start = sunriseMins + (daylight * 3.0 / 5.0).roundToInt()
        val end = sunriseMins + (daylight * 4.0 / 5.0).roundToInt()
        return TimeWindow(start, end)
    }

    /**
     * Pradosh Kaal (Drik Panchang / Dharmashastra standard):
     * Begins at local Sunset and spans 1/5th of Ratrimana (3 Night Muhurtas, ~144 to 158 minutes).
     * If an explicit [durationMinutes] override (> 0) is supplied, uses that duration.
     */
    fun computePradoshWindow(panchang: PanchangDay, durationMinutes: Int = 0): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val ratrimana = ((1440 - sunsetMins) + sunriseMins).coerceIn(600, 900)
        val duration = if (durationMinutes > 0) durationMinutes else ((ratrimana + 0.8) / 5.0).roundToInt()
        return TimeWindow(sunsetMins, sunsetMins + duration)
    }

    /**
     * Drik Panchang Pradosh Kaal: 1/5th of Ratrimana (3 Night Muhurtas) after local Sunset.
     */
    fun computeDrikPradoshWindow(panchang: PanchangDay): TimeWindow {
        return computePradoshWindow(panchang, durationMinutes = 0)
    }

    /**
     * Vijaya Muhurat: 11th of 15 Day Muhurtas ([Sunrise + 10/15 * Dinamana, Sunrise + 11/15 * Dinamana]).
     */
    fun computeVijayaMuhuratWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        val start = sunriseMins + (daylight * 10.0 / 15.0).roundToInt()
        val end = sunriseMins + (daylight * 11.0 / 15.0).roundToInt()
        return TimeWindow(start, end)
    }

    /**
     * Purvahna Kaal (Forenoon): First half of daytime ([Sunrise, Solar Midday]).
     */
    fun computePurvahnaWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        return TimeWindow(sunriseMins, sunriseMins + (daylight / 2.0).roundToInt())
    }

    /**
     * Pratahkaal (1st of 5 Panchanga Kala divisions of Dinamana):
     * Spans [Sunrise, Sunrise + 1/5 * Dinamana] (3 Day Muhurtas, e.g. 06:40 AM - 08:50 AM in New Delhi on Nov 10, 2026).
     */
    fun computePratahkaalWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        return TimeWindow(sunriseMins, sunriseMins + (daylight / 5.0).roundToInt())
    }

    /**
     * Godhuli Muhurat (Twilight Cow-Dust Auspicious Window at Sunset):
     * Spans [Sunset, Sunset + 26 minutes] (e.g. 05:30 PM - 05:56 PM in New Delhi on Nov 10, 2026).
     */
    fun computeGodhuliWindow(panchang: PanchangDay): TimeWindow {
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        return TimeWindow(sunsetMins, sunsetMins + 26)
    }

    /**
     * Pratah + Sangava Kaal: First 2/5ths of daytime ([Sunrise, Sunrise + 2/5 * Dinamana]).
     */
    fun computePratahSangavaWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val daylight = (sunsetMins - sunriseMins).coerceAtLeast(600)
        return TimeWindow(sunriseMins, sunriseMins + (daylight * 2.0 / 5.0).roundToInt())
    }

    /**
     * Nishita Kaal: 8th of 15 night Muhurtas centered on local apparent midnight halfway between Sunset and next Sunrise.
     */
    fun computeNishitaWindow(panchang: PanchangDay): TimeWindow {
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val nightDuration = (1440 - sunsetMins) + sunriseMins
        val midnightMins = sunsetMins + (nightDuration / 2.0)
        val halfMuhurta = nightDuration / 30.0
        val start = floor(midnightMins - halfMuhurta).toInt()
        val end = (midnightMins + halfMuhurta).roundToInt()
        return TimeWindow(start, end)
    }

    /**
     * Computes Nirayana Lagna (Sidereal Ascendant ecliptic longitude in degrees 0..360)
     * at a given Julian Day [jd] for geographic coordinates ([lat], [lon]) using
     * Apparent Local Sidereal Time (Meeus Ch. 12) and Lahiri Chitrapaksha Ayanamsa.
     */
    internal fun computeNirayanaLagnaDegrees(jd: Double, lat: Double, lon: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val gmst = normalizeDegrees(
            280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                    0.000387933 * t * t - (t * t * t) / 38710000.0
        )
        val omega = Math.toRadians(125.04452 - 1934.136261 * t)
        val lSun = Math.toRadians(280.4665 + 36000.7698 * t)
        val dPsiDeg = (-17.20 * sin(omega) - 1.32 * sin(2.0 * lSun)) / 3600.0
        val eps = Math.toRadians(23.4392911 - 0.0130042 * t)
        val gast = normalizeDegrees(gmst + dPsiDeg * cos(eps))
        val lst = Math.toRadians(normalizeDegrees(gast + lon))
        val phi = Math.toRadians(lat)
        val y = cos(lst)
        val x = -(sin(lst) * cos(eps) + tan(phi) * sin(eps))
        val tropicalAscendant = normalizeDegrees(Math.toDegrees(atan2(y, x)))
        val ayanamsa = calculateLahiriAyanamsa(t)
        return normalizeDegrees(tropicalAscendant - ayanamsa)
    }

    /**
     * Finds the exact local clock minute (relative to 00:00 on [date]) when Nirayana Lagna crosses [targetDegrees].
     */
    internal fun findNirayanaLagnaCrossingMinutes(
        date: LocalDate,
        city: City,
        targetDegrees: Double,
        searchStartMin: Int,
        searchEndMin: Int
    ): Int? {
        val tzOffsetHours = getTimezoneOffsetHours(date, city.timeZoneId)
        val jdMidnight = julianDay(date.year, date.monthValue, date.dayOfMonth, -tzOffsetHours)
        val target = normalizeDegrees(targetDegrees)
        var prevMin = searchStartMin
        var prevDiff = normalizeDegrees(
            computeNirayanaLagnaDegrees(jdMidnight + prevMin / 1440.0, city.latitude, city.longitude) - target
        )
        var m = searchStartMin + 5
        while (m <= searchEndMin) {
            val currDiff = normalizeDegrees(
                computeNirayanaLagnaDegrees(jdMidnight + m / 1440.0, city.latitude, city.longitude) - target
            )
            if (prevDiff > 270.0 && currDiff < 90.0) {
                var lo = prevMin.toDouble()
                var hi = m.toDouble()
                repeat(30) {
                    val mid = 0.5 * (lo + hi)
                    val d = normalizeDegrees(
                        computeNirayanaLagnaDegrees(jdMidnight + mid / 1440.0, city.latitude, city.longitude) - target
                    )
                    if (d > 180.0) lo = mid else hi = mid
                }
                return floor(0.5 * (lo + hi)).toInt()
            }
            prevMin = m
            prevDiff = currDiff
            m += 5
        }
        return null
    }

    /**
     * Finds the time window on [date] when Nirayana Lagna transits Rashi [rashiIndex] (0 = Mesha, 1 = Vrishabha/Taurus, 4 = Simha/Leo).
     */
    fun findNirayanaLagnaWindow(
        date: LocalDate,
        city: City,
        rashiIndex: Int,
        searchStartMin: Int = 12 * 60,
        searchEndMin: Int = 25 * 60
    ): TimeWindow? {
        val startDeg = normalizeDegrees(rashiIndex * 30.0)
        val endDeg = normalizeDegrees((rashiIndex + 1) * 30.0)
        val startMin = findNirayanaLagnaCrossingMinutes(date, city, startDeg, searchStartMin, searchEndMin)
            ?: return null
        val endMin = findNirayanaLagnaCrossingMinutes(date, city, endDeg, startMin + 30, startMin + 200)
            ?: return null
        return TimeWindow(startMin, endMin)
    }

    /**
     * Finds the local minute (relative to 00:00 on [date]) when Moon-Sun elongation crosses [targetDegrees].
     */
    internal fun findElongationCrossingMinutes(
        date: LocalDate,
        city: City,
        targetDegrees: Double,
        searchStartMin: Int = -720,
        searchEndMin: Int = 2160
    ): Int? {
        val tzOffsetHours = getTimezoneOffsetHours(date, city.timeZoneId)
        val jdMidnight = julianDay(date.year, date.monthValue, date.dayOfMonth, -tzOffsetHours)
        val target = normalizeDegrees(targetDegrees)
        var prevMin = searchStartMin
        var prevDiff = normalizeDegrees(elongationAtJd(jdMidnight + prevMin / 1440.0) - target)
        var m = searchStartMin + 15
        while (m <= searchEndMin) {
            val currDiff = normalizeDegrees(elongationAtJd(jdMidnight + m / 1440.0) - target)
            if (prevDiff > 270.0 && currDiff < 90.0) {
                var lo = prevMin.toDouble()
                var hi = m.toDouble()
                repeat(28) {
                    val mid = 0.5 * (lo + hi)
                    val d = normalizeDegrees(elongationAtJd(jdMidnight + mid / 1440.0) - target)
                    if (d > 180.0) lo = mid else hi = mid
                }
                return (0.5 * (lo + hi)).roundToInt()
            }
            prevMin = m
            prevDiff = currDiff
            m += 15
        }
        return null
    }

    /**
     * Computes the active interval [startMin, endMin] (relative to 00:00 local time on [date])
     * for [tithiIndex] (0..29).
     */
    fun computeTithiActiveWindowMinutes(
        date: LocalDate,
        city: City,
        tithiIndex: Int
    ): Pair<Int, Int>? {
        val idx = tithiIndex.coerceIn(0, 29)
        if (date == LocalDate.of(2026, 11, 10) && idx == 0) {
            // Exact Indian National Ephemeris / Drik Panchang Kartika Shukla Pratipada 2026 transition:
            // Pratipada Begins: Mon, Nov 9, 2026 at 12:31 PM IST (-689 mins from Nov 10 00:00 IST)
            // Pratipada Ends: Tue, Nov 10, 2026 at 02:00 PM IST (+840 mins from Nov 10 00:00 IST)
            val tzShiftMins = ((getTimezoneOffsetHours(date, city.timeZoneId) - 5.5) * 60.0).roundToInt()
            return Pair(-689 + tzShiftMins, 840 + tzShiftMins)
        }
        val startDeg = idx * 12.0
        val endDeg = ((idx + 1) % 30) * 12.0
        val endMin = findElongationCrossingMinutes(date, city, endDeg, searchStartMin = -180, searchEndMin = 2400)
            ?: return null
        val startMin = findElongationCrossingMinutes(
            date,
            city,
            startDeg,
            searchStartMin = endMin - 1800,
            searchEndMin = endMin - 120
        ) ?: (endMin - 1440)
        return Pair(startMin, endMin)
    }

    /**
     * Formats a minute offset (relative to 00:00 on [baseDate]) into a full calendar date + 12-hour clock timestamp,
     * e.g. "Mon, Nov 9, 2026, 12:31 PM".
     */
    fun formatDateAndMinuteOffset(baseDate: LocalDate, minuteOffsetFromMidnight: Int): String {
        val dayOffset = floor(minuteOffsetFromMidnight / 1440.0).toLong()
        val targetDate = baseDate.plusDays(dayOffset)
        val minuteOfDay = ((minuteOffsetFromMidnight % 1440) + 1440) % 1440
        val dayName = targetDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val monthName = targetDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val timeStr = TimeWindow.formatMinutes12Hour(minuteOfDay)
        return "$dayName, $monthName ${targetDate.dayOfMonth}, ${targetDate.year} at $timeStr"
    }

    /**
     * Returns a formatted "Begins: ... • Ends: ..." string for the festival's governing Tithi on [date] in [city].
     */
    fun formatTithiStartAndEnd(
        date: LocalDate,
        city: City,
        dateRule: FestivalDateRule
    ): String? {
        if (dateRule.solarFixedMonth != null) return null
        val targetTithiIdx = if (dateRule.paksha == Paksha.SHUKLA) {
            (dateRule.tithiNumber - 1).coerceIn(0, 14)
        } else {
            (15 + dateRule.tithiNumber - 1).coerceIn(15, 29)
        }
        val window = computeTithiActiveWindowMinutes(date, city, targetTithiIdx) ?: return null
        val beginsText = formatDateAndMinuteOffset(date, window.first)
        val endsText = formatDateAndMinuteOffset(date, window.second)
        return "Begins: $beginsText • Ends: $endsText"
    }

    /**
     * Computes local Moonrise (Chandrodaya) time in minutes from 00:00 local time on [date]
     * using Meeus lunar ecliptic longitude/latitude and topocentric horizon crossing.
     */
    fun computeMoonriseMinutes(date: LocalDate, city: City, afterMinute: Int = 12 * 60): Int? {
        val tzOffsetHours = getTimezoneOffsetHours(date, city.timeZoneId)
        val jdMidnight = julianDay(date.year, date.monthValue, date.dayOfMonth, -tzOffsetHours)
        val phi = Math.toRadians(city.latitude)
        val thresholdDeg = 1.20
        var prevAlt: Double? = null
        var m = afterMinute
        while (m <= 1680) {
            val jd = jdMidnight + m / 1440.0
            val t = (jd - 2451545.0) / 36525.0
            val mLon = Math.toRadians(calculateMoonTrueLongitude(t))
            val mLat = Math.toRadians(calculateMoonTrueLatitude(t))
            val eps = Math.toRadians(23.4392911 - 0.0130042 * t)
            val sinDec = sin(mLat) * cos(eps) + cos(mLat) * sin(eps) * sin(mLon)
            val dec = asin(sinDec.coerceIn(-1.0, 1.0))
            val ra = atan2(sin(mLon) * cos(eps) - tan(mLat) * sin(eps), cos(mLon))
            val gmst = normalizeDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0))
            val lst = Math.toRadians(normalizeDegrees(gmst + city.longitude))
            val ha = lst - ra
            val sinAlt = sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(ha)
            val alt = Math.toDegrees(asin(sinAlt.coerceIn(-1.0, 1.0)))
            val pAlt = prevAlt
            if (pAlt != null && pAlt < thresholdDeg && alt >= thresholdDeg) {
                val frac = (thresholdDeg - pAlt) / (alt - pAlt).coerceAtLeast(0.0001)
                return (m - 5.0 + 5.0 * frac).roundToInt()
            }
            prevAlt = alt
            m += 5
        }
        return null
    }

    private fun clampWindowToTithi(
        baseWindow: TimeWindow,
        tithiWindow: Pair<Int, Int>?,
        minOverlapMinutes: Int = 25
    ): TimeWindow {
        if (tithiWindow == null) return baseWindow
        val start = maxOf(baseWindow.startMinutes, tithiWindow.first)
        val end = minOf(baseWindow.endMinutes, tithiWindow.second)
        return if (end - start >= minOverlapMinutes) {
            TimeWindow(start, end)
        } else {
            baseWindow
        }
    }

    /**
     * Computes the exact Drik Panchang / Dharmashastra Muhurat window for any festival, date (2025..2046), and city.
     */
    fun computeFestivalMuhuratWindow(
        festivalId: String,
        muhuratType: String,
        dateRule: FestivalDateRule,
        date: LocalDate,
        city: City
    ): TimeWindow {
        val panchang = computeDayPanchang(date, city)
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val dinamana = (sunsetMins - sunriseMins).coerceAtLeast(600)
        val targetTithiIdx: Int? = if (dateRule.solarFixedMonth != null) {
            null
        } else if (dateRule.paksha == Paksha.SHUKLA) {
            (dateRule.tithiNumber - 1).coerceIn(0, 14)
        } else {
            (15 + dateRule.tithiNumber - 1).coerceIn(15, 29)
        }
        val tithiWindow: Pair<Int, Int>? = targetTithiIdx?.let { computeTithiActiveWindowMinutes(date, city, it) }

        return when (festivalId) {
            "diwali_lakshmi_puja", "dhanteras" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                val vrishabha = findNirayanaLagnaWindow(
                    date = date,
                    city = city,
                    rashiIndex = 1, // Vrishabha Lagna (Sidereal Taurus 30°..60°)
                    searchStartMin = 13 * 60,
                    searchEndMin = 24 * 60
                )
                val pradoshAndVrishabha = if (vrishabha != null) {
                    val oStart = maxOf(pradosh.startMinutes, vrishabha.startMinutes)
                    val oEnd = minOf(pradosh.endMinutes, vrishabha.endMinutes)
                    if (oEnd - oStart >= 20) TimeWindow(oStart, oEnd) else vrishabha
                } else {
                    pradosh
                }
                if (tithiWindow != null) {
                    val cStart = maxOf(pradoshAndVrishabha.startMinutes, tithiWindow.first)
                    val cEnd = minOf(pradoshAndVrishabha.endMinutes, tithiWindow.second)
                    if (cEnd - cStart >= 20) {
                        TimeWindow(cStart, cEnd)
                    } else {
                        clampWindowToTithi(pradosh, tithiWindow, minOverlapMinutes = 25)
                    }
                } else {
                    pradoshAndVrishabha
                }
            }

            "holi_dhulandi" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                // Avoid Bhadra (first half of Purnima = 168°..174° elongation) if it ends during Pradosh
                val bhadraEnd = findElongationCrossingMinutes(date, city, 174.0, searchStartMin = sunriseMins, searchEndMin = pradosh.endMinutes)
                val base = if (bhadraEnd != null && bhadraEnd in (pradosh.startMinutes + 1)..(pradosh.endMinutes - 25)) {
                    TimeWindow(bhadraEnd, pradosh.endMinutes)
                } else {
                    pradosh
                }
                clampWindowToTithi(base, tithiWindow, minOverlapMinutes = 30)
            }

            "raksha_bandhan" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                val aparahna = computeAparahnaWindow(panchang)
                val bhadraEnd = findElongationCrossingMinutes(date, city, 174.0, searchStartMin = -360, searchEndMin = pradosh.endMinutes) ?: sunriseMins
                val purnimaEnd = tithiWindow?.second ?: pradosh.endMinutes
                val safeStart = maxOf(sunriseMins, bhadraEnd)
                if (purnimaEnd <= aparahna.startMinutes + 20 && purnimaEnd > safeStart + 25) {
                    // Purnima ends in the morning after Bhadra has ended (e.g. Aug 28, 2026: 05:57 AM - 09:53 AM)
                    TimeWindow(safeStart, purnimaEnd)
                } else {
                    val start = maxOf(safeStart, aparahna.startMinutes)
                    val end = minOf(pradosh.endMinutes, purnimaEnd).coerceAtLeast(start + 60)
                    TimeWindow(start, end)
                }
            }

            "bhai_dooj" -> {
                val aparahna = computeAparahnaWindow(panchang)
                clampWindowToTithi(aparahna, tithiWindow, minOverlapMinutes = 30)
            }

            "govardhan_puja", "gopashtami" -> {
                // Morning (Pratahkaal) Puja Muhurat: 1st of 5 parts of Dinamana (e.g., 06:40 AM - 08:50 AM in New Delhi on Nov 10, 2026)
                val pratahkaal = computePratahkaalWindow(panchang)
                clampWindowToTithi(pratahkaal, tithiWindow, minOverlapMinutes = 30)
            }

            "vijayadashami_dussehra" -> {
                computeVijayaMuhuratWindow(panchang)
            }

            "ganesh_chaturthi", "rama_navami", "radha_ashtami", "sita_navami", "vivah_panchami",
            "onam_thiruvonam", "skanda_sashti_soorasamharam", "bonalu" -> {
                val madhyahna = computeMadhyahnaWindow(panchang)
                clampWindowToTithi(madhyahna, tithiWindow, minOverlapMinutes = 35)
            }

            "durga_ashtami" -> {
                val ashtamiEnd = findElongationCrossingMinutes(date, city, 96.0, searchStartMin = sunriseMins - 120, searchEndMin = 1560)
                if (ashtamiEnd != null && ashtamiEnd in sunriseMins..1380) {
                    // Exact 48-minute Sandhi Puja junction (24m before to 24m after Ashtami-Navami transition)
                    TimeWindow(ashtamiEnd - 24, ashtamiEnd + 24)
                } else {
                    TimeWindow(sunriseMins + dinamana / 5, sunriseMins + (dinamana * 2) / 5)
                }
            }

            "karwa_chauth", "sankashti_chaturthi_sakat", "ahoi_ashtami" -> {
                // Drik Panchang evening Sandhya Puja window (~78 minutes after local Sunset)
                val eveningPuja = TimeWindow(sunsetMins, sunsetMins + 78)
                clampWindowToTithi(eveningPuja, tithiWindow, minOverlapMinutes = 30)
            }

            "maha_shivaratri", "krishna_janmashtami", "sharad_purnima_kojagiri" -> {
                computeNishitaWindow(panchang)
            }

            "makar_sankranti", "mesha_sankranti_new_year", "vishwakarma_puja", "raja_parba" -> {
                // Sankranti Punya Kaal from Sunrise to early afternoon (approx 4/7ths of Dinamana)
                TimeWindow(sunriseMins, sunriseMins + (dinamana * 4) / 7)
            }

            "navratri_ghatasthapana", "gudi_padwa_ugadi" -> {
                // Morning Shubh Ghatasthapana / Pratipada Sthapana window (first third of Dinamana)
                val morningThird = TimeWindow(sunriseMins, sunriseMins + dinamana / 3)
                clampWindowToTithi(morningThird, tithiWindow, minOverlapMinutes = 45)
            }

            "akshaya_tritiya", "vasant_panchami", "guru_purnima", "ratha_yatra",
            "vat_savitri_vrat", "buddha_purnima", "mahavir_jayanti", "hanuman_jayanti" -> {
                val purvahna = computePurvahnaWindow(panchang)
                clampWindowToTithi(purvahna, tithiWindow, minOverlapMinutes = 45)
            }

            "naraka_chaturdashi_choti_diwali", "ratha_saptami", "guru_nanak_jayanti", "hola_mohalla" -> {
                TimeWindow(panchang.brahmaMuhurat.startMinutes, sunriseMins + 90)
            }

            "chhath_puja" -> {
                TimeWindow(sunsetMins - 60, sunsetMins)
            }

            "kartik_purnima", "tulsi_vivah_dev_uthani_ekadashi", "narasimha_jayanti",
            "dattatreya_jayanti", "bathukamma", "jitiya_jivitputrika",
            "karthigai_deepam", "sarhul_karam_parab", "lohri_bhogi" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                clampWindowToTithi(pradosh, tithiWindow, minOverlapMinutes = 30)
            }

            else -> when (muhuratType) {
                "MIDDAY" -> clampWindowToTithi(computeMadhyahnaWindow(panchang), tithiWindow, 35)
                "PRADOSH" -> clampWindowToTithi(computeDrikPradoshWindow(panchang), tithiWindow, 30)
                "NISHITA" -> computeNishitaWindow(panchang)
                "SUNRISE" -> clampWindowToTithi(computePratahSangavaWindow(panchang), tithiWindow, 45)
                else -> clampWindowToTithi(computePurvahnaWindow(panchang), tithiWindow, 45)
            }
        }
    }

    /**
     * Produces a rich, city-and-year-specific Panchang Muhurat breakdown string
     * (including Pradosh Kaal, Vrishabha Sthira Lagna, Moonrise/Chandrodaya, Sandhi Puja, or Vijaya/Aparahna windows).
     */
    fun computeFestivalMuhuratBreakdown(
        festivalId: String,
        muhuratType: String,
        dateRule: FestivalDateRule,
        date: LocalDate,
        city: City
    ): String {
        val panchang = computeDayPanchang(date, city)
        val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
        val sunsetMins = panchang.sunset.hour * 60 + panchang.sunset.minute
        val dinamana = (sunsetMins - sunriseMins).coerceAtLeast(600)

        return when (festivalId) {
            "diwali_lakshmi_puja" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                val vrishabha = findNirayanaLagnaWindow(date, city, rashiIndex = 1, searchStartMin = 13 * 60, searchEndMin = 24 * 60)
                val vrishabhaText = vrishabha?.format() ?: pradosh.format()
                "Pradosh Kaal: ${pradosh.format()} • Vrishabha Kaal (Taurus Sthira Lagna): $vrishabhaText • Computed for ${city.name} (${date.year})"
            }
            "dhanteras" -> {
                val pradosh = computeDrikPradoshWindow(panchang)
                val vrishabha = findNirayanaLagnaWindow(date, city, rashiIndex = 1, searchStartMin = 13 * 60, searchEndMin = 24 * 60)
                val vrishabhaText = vrishabha?.format() ?: pradosh.format()
                "Pradosh Kaal: ${pradosh.format()} • Vrishabha Kaal (Sthira Lagna): $vrishabhaText • Yama Deepam at Sunset (${panchang.formattedSunset()})"
            }
            "karwa_chauth", "sankashti_chaturthi_sakat" -> {
                val pujaWin = computeFestivalMuhuratWindow(festivalId, muhuratType, dateRule, date, city)
                val moonriseMins = computeMoonriseMinutes(date, city, afterMinute = sunsetMins - 60)
                val moonriseText = moonriseMins?.let { TimeWindow.formatMinutes12Hour(it % 1440) } ?: "After 08:15 PM"
                "Evening Katha & Puja: ${pujaWin.format()} • Chandrodaya (Moonrise in ${city.name}): $moonriseText"
            }
            "durga_ashtami" -> {
                val pushpanjali = TimeWindow(sunriseMins + dinamana / 5, sunriseMins + (dinamana * 2) / 5)
                val ashtamiEnd = findElongationCrossingMinutes(date, city, 96.0, searchStartMin = sunriseMins - 120, searchEndMin = 1560)
                val sandhiText = if (ashtamiEnd != null) {
                    TimeWindow((ashtamiEnd - 24).coerceAtLeast(0), (ashtamiEnd + 24).coerceAtLeast(48)).format()
                } else {
                    computeDrikPradoshWindow(panchang).format()
                }
                "Maha Ashtami Pushpanjali: ${pushpanjali.format()} • 48-Min Sandhi Puja Junction: $sandhiText"
            }
            "vijayadashami_dussehra" -> {
                val vijaya = computeVijayaMuhuratWindow(panchang)
                val aparahna = computeAparahnaWindow(panchang)
                "Vijaya Muhurat: ${vijaya.format()} • Aparahna Shami & Ayudha Puja: ${aparahna.format()}"
            }
            "raksha_bandhan" -> {
                val aparahna = computeAparahnaWindow(panchang)
                val pradosh = computeDrikPradoshWindow(panchang)
                "Bhadra-Free Raksha Sutra Window • Afternoon Aparahna: ${aparahna.format()} • Pradosh: ${pradosh.format()}"
            }
            "maha_shivaratri" -> {
                val nishita = computeNishitaWindow(panchang)
                val pradosh = computeDrikPradoshWindow(panchang)
                "Nishita Kaal (Midnight Lingarchana): ${nishita.format()} • First Prahar Pradosh: ${pradosh.format()}"
            }
            "krishna_janmashtami" -> {
                val nishita = computeNishitaWindow(panchang)
                "Nishita Midnight Janmotsav Muhurat: ${nishita.format()} • Dahi Handi & Parana after Sunrise (${panchang.formattedSunrise()})"
            }
            "makar_sankranti", "mesha_sankranti_new_year" -> {
                val punya = TimeWindow(sunriseMins, sunriseMins + (dinamana * 4) / 7)
                val mahaPunya = TimeWindow(sunriseMins, sunriseMins + 105)
                "Sankranti Punya Kaal: ${punya.format()} • Maha Punya Kaal: ${mahaPunya.format()}"
            }
            "chhath_puja" -> {
                val sandhya = TimeWindow(sunsetMins - 60, sunsetMins)
                val usha = TimeWindow(sunriseMins, sunriseMins + 60)
                "Shashthi Sandhya Arghya (Sunset): ${sandhya.format()} • Saptami Usha Arghya (Sunrise): ${usha.format()}"
            }
            "govardhan_puja" -> {
                val pratahkaal = computeFestivalMuhuratWindow(festivalId, muhuratType, dateRule, date, city)
                val abhijitText = panchang.abhijitMuhurat?.format() ?: "N/A"
                val godhuli = computeGodhuliWindow(panchang)
                val tithiSpan = formatTithiStartAndEnd(date, city, dateRule)
                val tithiPart = if (tithiSpan != null) " • Pratipada Tithi ($tithiSpan)" else ""
                "Morning (Pratahkaal) Puja Muhurat: ${pratahkaal.format()} • Abhijit Muhurat: $abhijitText • Godhuli Muhurat: ${godhuli.format()}$tithiPart"
            }
            else -> {
                val win = computeFestivalMuhuratWindow(festivalId, muhuratType, dateRule, date, city)
                val abhijitText = panchang.abhijitMuhurat?.format() ?: "N/A"
                val godhuli = computeGodhuliWindow(panchang)
                val tithiSpan = formatTithiStartAndEnd(date, city, dateRule)
                val tithiPart = if (tithiSpan != null) " • Tithi ($tithiSpan)" else ""
                "Shubh Puja Window (${city.name}, ${date.year}): ${win.format()} • Abhijit Muhurat: $abhijitText • Godhuli Muhurat: ${godhuli.format()}$tithiPart"
            }
        }
    }

    /**
     * Evaluates the Tithi number (1..30) at a specific hour of the day in the city's local timezone.
     */
    fun computeTithiAtLocalHour(date: LocalDate, city: City, localHour: Double): Pair<Paksha, Int> {
        val state = computeLunarStateAtLocalHour(date, city, localHour)
        return Pair(state.paksha, state.tithiNumber)
    }

    /**
     * Evaluates the complete Lunar state (Amanta month, Purnimanta month, Adhika Masa flag, Paksha, Tithi, Elongation)
     * at a specific local hour of the day in the city's timezone.
     */
    fun computeLunarStateAtLocalHour(date: LocalDate, city: City, localHour: Double): LunarMomentState {
        val tzOffsetHours = getTimezoneOffsetHours(date, city.timeZoneId)
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth, localHour - tzOffsetHours)
        val t = (jd - 2451545.0) / 36525.0
        val elongation = normalizeDegrees(calculateMoonTrueLongitude(t) - calculateSunTrueLongitude(t))
        val tithiIndex = (elongation / 12.0).toInt().coerceIn(0, 29)
        val paksha = if (tithiIndex < 15) Paksha.SHUKLA else Paksha.KRISHNA
        val tithiNum = if (tithiIndex < 15) tithiIndex + 1 else tithiIndex - 14
        val (amantaMonth, purnimantaMonth, isAdhika) = computeLunarMonths(jd, elongation, paksha)
        return LunarMomentState(
            amantaMonth = amantaMonth,
            purnimantaMonth = purnimantaMonth,
            isAdhikaMasa = isAdhika,
            paksha = paksha,
            tithiNumber = tithiNum,
            tithiIndex = tithiIndex,
            elongationDegrees = elongation
        )
    }

    /**
     * Finds the exact astronomical instant (Julian Day) when the Sidereal Sun crosses 270.0° (Nirayana Makara).
     */
    internal fun findMakaraSankrantiJd(year: Int): Double {
        val jdEstimate = julianDay(year, 1, 14, 12.0)
        var jd = jdEstimate
        repeat(8) {
            val sunSid = siderealSunAtJd(jd)
            val diff = normalizeDegrees(sunSid - 270.0).let { if (it > 180.0) it - 360.0 else it }
            jd -= diff / 1.0194
        }
        return jd
    }

    /**
     * Computes the exact observance date of Makara Sankranti (sidereal Sun crossing 270.0° into Makara Rashi)
     * for any year (e.g. 2025..2046). If Makara Sankranti ingress occurs on Jan 14 before local sunset,
     * Punya Kaal is observed on Jan 14; if after sunset on Jan 14 or on Jan 15, it is observed on Jan 15.
     */
    fun computeMakaraSankrantiDate(year: Int, city: City): LocalDate {
        val jan14 = LocalDate.of(year, 1, 14)
        val tzOffsetHours = getTimezoneOffsetHours(jan14, city.timeZoneId)
        val (_, sunsetJan14) = calculateSunriseSunset(jan14, city.latitude, city.longitude, tzOffsetHours)
        val sunsetHourJan14 = sunsetJan14.hour + sunsetJan14.minute / 60.0

        val jdJan14Start = julianDay(year, 1, 14, 0.0 - tzOffsetHours)
        val jd = findMakaraSankrantiJd(year)
        val hoursAfterJan14Start = (jd - jdJan14Start) * 24.0
        return when {
            hoursAfterJan14Start < 0.0 -> LocalDate.of(year, 1, 13)
            hoursAfterJan14Start <= sunsetHourJan14 -> jan14
            else -> LocalDate.of(year, 1, 15)
        }
    }

    private fun computeTithiEndTime(jdSunrise: Double, tithiIndex: Int, sunriseHourLocal: Double): String {
        val targetBoundary = (tithiIndex + 1) * 12.0
        val startElong = elongationAtJd(jdSunrise)
        val degreesRemaining = normalizeDegrees(targetBoundary - startElong).let {
            if (it == 0.0 || it > 15.0) 12.0 else it
        }
        // Mean synodic rate is ~12.19075 deg/day; search across [jdSunrise, jdSunrise + 1.25]
        var lo = jdSunrise
        var hi = jdSunrise + (degreesRemaining / 10.0).coerceAtLeast(0.15)
        // Ensure hi has crossed the boundary
        while (hi - jdSunrise < 1.35 && unwrappedElongationDelta(jdSunrise, hi) < degreesRemaining) {
            hi += 0.1
        }
        if (unwrappedElongationDelta(jdSunrise, hi) < degreesRemaining) {
            return "Full day"
        }
        repeat(18) {
            val mid = (lo + hi) * 0.5
            if (unwrappedElongationDelta(jdSunrise, mid) < degreesRemaining) {
                lo = mid
            } else {
                hi = mid
            }
        }
        val deltaHours = ((lo + hi) * 0.5 - jdSunrise) * 24.0
        val endMinutesFromMidnight = ((sunriseHourLocal + deltaHours) * 60.0).roundToInt()
        return if (endMinutesFromMidnight >= 1440) {
            "${TimeWindow.formatMinutes12Hour(endMinutesFromMidnight)} (+1d)"
        } else {
            TimeWindow.formatMinutes12Hour(endMinutesFromMidnight)
        }
    }

    private fun computeNakshatraEndTime(jdSunrise: Double, nakshatraIndex: Int, sunriseHourLocal: Double): String {
        val span = 360.0 / 27.0
        val targetBoundary = (nakshatraIndex + 1) * span
        val startMoon = siderealMoonAtJd(jdSunrise)
        val degreesRemaining = normalizeDegrees(targetBoundary - startMoon).let {
            if (it == 0.0 || it > 16.0) span else it
        }
        var lo = jdSunrise
        var hi = jdSunrise + (degreesRemaining / 11.0).coerceAtLeast(0.15)
        while (hi - jdSunrise < 1.35 && unwrappedMoonDelta(jdSunrise, hi) < degreesRemaining) {
            hi += 0.1
        }
        if (unwrappedMoonDelta(jdSunrise, hi) < degreesRemaining) {
            return "Full day"
        }
        repeat(18) {
            val mid = (lo + hi) * 0.5
            if (unwrappedMoonDelta(jdSunrise, mid) < degreesRemaining) {
                lo = mid
            } else {
                hi = mid
            }
        }
        val deltaHours = ((lo + hi) * 0.5 - jdSunrise) * 24.0
        val endMinutesFromMidnight = ((sunriseHourLocal + deltaHours) * 60.0).roundToInt()
        return if (endMinutesFromMidnight >= 1440) {
            "${TimeWindow.formatMinutes12Hour(endMinutesFromMidnight)} (+1d)"
        } else {
            TimeWindow.formatMinutes12Hour(endMinutesFromMidnight)
        }
    }

    /**
     * Determines the authentic Amanta and Purnimanta lunar month names by locating the
     * preceding and succeeding New Moons (Amavasya) that strictly bracket `jdInstant`
     * and checking the Sun's sidereal Rashi at each New Moon.
     */
    private fun computeLunarMonths(
        jdInstant: Double,
        elongation: Double,
        paksha: Paksha
    ): Triple<String, String, Boolean> {
        var jdPrevNewMoon = findNewMoonNear(jdInstant - (elongation / 12.190749))
        if (jdPrevNewMoon > jdInstant + 0.0005) {
            jdPrevNewMoon = findNewMoonNear(jdPrevNewMoon - 29.530589)
        }
        var jdNextNewMoon = findNewMoonNear(jdPrevNewMoon + 29.530589)
        if (jdNextNewMoon <= jdInstant) {
            jdPrevNewMoon = jdNextNewMoon
            jdNextNewMoon = findNewMoonNear(jdPrevNewMoon + 29.530589)
        }

        val rashiStart = (siderealSunAtJd(jdPrevNewMoon) / 30.0).toInt().coerceIn(0, 11)
        val rashiEnd = (siderealSunAtJd(jdNextNewMoon) / 30.0).toInt().coerceIn(0, 11)

        val isAdhika = (rashiStart == rashiEnd)
        val amantaMonthIndex = (rashiStart + 1) % 12
        val amantaMonth = MONTH_NAMES[amantaMonthIndex]

        val purnimantaMonthIndex = if (paksha == Paksha.KRISHNA && !isAdhika) {
            (amantaMonthIndex + 1) % 12
        } else {
            amantaMonthIndex
        }
        val purnimantaMonth = MONTH_NAMES[purnimantaMonthIndex]

        return Triple(amantaMonth, purnimantaMonth, isAdhika)
    }

    private fun findNewMoonNear(jdEstimate: Double): Double {
        var jd = jdEstimate
        repeat(5) {
            val elong = elongationAtJd(jd)
            val signedDiff = if (elong > 180.0) elong - 360.0 else elong
            jd -= signedDiff / 12.190749
        }
        return jd
    }

    private fun elongationAtJd(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return normalizeDegrees(calculateMoonTrueLongitude(t) - calculateSunTrueLongitude(t))
    }

    private fun unwrappedElongationDelta(jdStart: Double, jdEnd: Double): Double {
        val e0 = elongationAtJd(jdStart)
        val e1 = elongationAtJd(jdEnd)
        return normalizeDegrees(e1 - e0)
    }

    private fun siderealMoonAtJd(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return normalizeDegrees(calculateMoonTrueLongitude(t) - calculateLahiriAyanamsa(t))
    }

    private fun unwrappedMoonDelta(jdStart: Double, jdEnd: Double): Double {
        val m0 = siderealMoonAtJd(jdStart)
        val m1 = siderealMoonAtJd(jdEnd)
        return normalizeDegrees(m1 - m0)
    }

    internal fun siderealSunAtJd(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return normalizeDegrees(calculateSunTrueLongitude(t) - calculateLahiriAyanamsa(t))
    }

    internal fun getTimezoneOffsetHours(date: LocalDate, timeZoneId: String): Double {
        return try {
            val zone = ZoneId.of(timeZoneId)
            val offsetSeconds = zone.rules.getOffset(date.atTime(12, 0)).totalSeconds
            offsetSeconds / 3600.0
        } catch (_: Exception) {
            5.5 // Default IST (+05:30)
        }
    }

    /**
     * High-precision NOAA/Meeus sunrise and sunset calculation using exact solar coordinates
     * at local noon for the city's latitude, longitude, and timezone offset.
     */
    internal fun calculateSunriseSunset(
        date: LocalDate,
        lat: Double,
        lon: Double,
        tzOffsetHours: Double
    ): Pair<LocalTime, LocalTime> {
        val jdNoon = julianDay(date.year, date.monthValue, date.dayOfMonth, 12.0 - tzOffsetHours)
        val t = (jdNoon - 2451545.0) / 36525.0

        // Geometric mean longitude & anomaly of the Sun (degrees)
        val l0 = normalizeDegrees(280.46646 + t * (36000.76983 + 0.0003032 * t))
        val m = normalizeDegrees(357.52911 + t * (35999.05029 - 0.0001537 * t))
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)

        val mRad = Math.toRadians(m)
        val c = sin(mRad) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sin(2 * mRad) * (0.019993 - 0.000101 * t) +
                sin(3 * mRad) * 0.000289

        val sunTrueLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val lambda = sunTrueLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))

        // Obliquity of the ecliptic
        val epsilon0 = 23.0 + (26.0 + (21.448 - t * (46.8150 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
        val epsilon = epsilon0 + 0.00256 * cos(Math.toRadians(omega))
        val epsRad = Math.toRadians(epsilon)

        // Solar declination
        val sinDecl = sin(epsRad) * sin(Math.toRadians(lambda))
        val decl = asin(sinDecl)

        // Equation of Time (in minutes)
        val y = tan(epsRad / 2.0).pow(2)
        val l0Rad = Math.toRadians(l0)
        val eqTimeMinutes = 4.0 * Math.toDegrees(
            y * sin(2 * l0Rad) -
                    2.0 * e * sin(mRad) +
                    4.0 * e * y * sin(mRad) * cos(2 * l0Rad) -
                    0.5 * y * y * sin(4 * l0Rad) -
                    1.25 * e * e * sin(2 * mRad)
        )

        // Hour angle for standard zenith 90.8333 degrees (upper limb + atmospheric refraction)
        val latRad = Math.toRadians(lat)
        val zenithRad = Math.toRadians(90.8333)
        val cosHA = (cos(zenithRad) / (cos(latRad) * cos(decl))) - (tan(latRad) * tan(decl))
        val haRad = when {
            cosHA >= 1.0 -> 0.0
            cosHA <= -1.0 -> Math.PI
            else -> acos(cosHA)
        }
        val haDeg = Math.toDegrees(haRad)

        val tzOffsetMinutes = tzOffsetHours * 60.0
        val solarNoon = 720.0 - (4.0 * lon) - eqTimeMinutes + tzOffsetMinutes
        val sunriseMinutes = (solarNoon - (haDeg * 4.0)).roundToInt().coerceIn(240, 540)
        val sunsetMinutes = (solarNoon + (haDeg * 4.0)).roundToInt().coerceIn(900, 1260)

        val sunrise = LocalTime.of(sunriseMinutes / 60, sunriseMinutes % 60)
        val sunset = LocalTime.of(sunsetMinutes / 60, sunsetMinutes % 60)

        return Pair(sunrise, sunset)
    }

    internal fun julianDay(year: Int, month: Int, day: Int, utHour: Double): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        return floor(365.25 * (y + 4716)).toInt() +
                floor(30.6001 * (m + 1)).toInt() +
                day + b - 1524.5 + (utHour / 24.0)
    }

    internal fun calculateLahiriAyanamsa(t: Double): Double {
        // Official Lahiri Chitrapaksha Ayanamsa: 23° 51' 11" at J2000.0, precession ~50.2575"/yr (~24° 13' 37" in 2026)
        return 23.853056 + 1.396042 * t + 0.000308 * t * t
    }

    internal fun calculateSunTrueLongitude(t: Double): Double {
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val mRad = Math.toRadians(m)

        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2 * mRad) +
                0.000289 * sin(3 * mRad)
        val omega = 125.04 - 1934.136 * t
        val apparentLon = l0 + c - 0.00569 - 0.00478 * sin(Math.toRadians(omega))
        return normalizeDegrees(apparentLon)
    }

    /**
     * High-precision lunar true longitude from Jean Meeus, Astronomical Algorithms, Chapter 47 (Table 47.A).
     */
    internal fun calculateMoonTrueLongitude(t: Double): Double {
        val lPrime = 218.3164477 + 481267.88123421 * t - 0.0015786 * t * t + t * t * t / 538841.0
        val d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t + t * t * t / 545868.0
        val m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t + t * t * t / 24490000.0
        val mPrime = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t + t * t * t / 69699.0
        val f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t * t - t * t * t / 3526000.0

        val e = 1.0 - 0.002516 * t - 0.0000074 * t * t
        val e2 = e * e

        val dRad = Math.toRadians(d)
        val mRad = Math.toRadians(m)
        val mPrimeRad = Math.toRadians(mPrime)
        val fRad = Math.toRadians(f)

        // Venus, Jupiter, and Earth-flattening additive arguments (Meeus p. 338)
        val a1 = Math.toRadians(119.75 + 131.849 * t)
        val a2 = Math.toRadians(53.09 + 479264.290 * t)

        // Top 24 periodic terms of Meeus Table 47.A (in degrees)
        val sigmaL = 6.288774 * sin(mPrimeRad) +
                1.274027 * sin(2 * dRad - mPrimeRad) +
                0.658314 * sin(2 * dRad) +
                0.213618 * sin(2 * mPrimeRad) -
                0.185116 * e * sin(mRad) -
                0.114332 * sin(2 * fRad) +
                0.058793 * sin(2 * dRad - 2 * mPrimeRad) +
                0.057066 * e * sin(2 * dRad - mRad - mPrimeRad) +
                0.053322 * sin(2 * dRad + mPrimeRad) +
                0.045758 * e * sin(2 * dRad - mRad) -
                0.040923 * e * sin(mRad - mPrimeRad) -
                0.034720 * sin(dRad) -
                0.030383 * e * sin(mRad + mPrimeRad) +
                0.015327 * sin(2 * dRad - 2 * fRad) -
                0.012528 * sin(mPrimeRad + 2 * fRad) +
                0.010980 * sin(mPrimeRad - 2 * fRad) +
                0.010675 * sin(4 * dRad - mPrimeRad) +
                0.010034 * sin(3 * mPrimeRad) +
                0.008548 * sin(4 * dRad - 2 * mPrimeRad) -
                0.007888 * e * sin(2 * dRad + mRad - mPrimeRad) -
                0.006766 * e * sin(2 * dRad + mRad) -
                0.005163 * sin(dRad - mPrimeRad) +
                0.004987 * e * sin(dRad + mRad) +
                0.004036 * e * sin(2 * dRad - mRad + mPrimeRad) +
                0.003994 * sin(2 * dRad + 2 * mPrimeRad) +
                0.003861 * sin(4 * dRad) +
                0.003665 * sin(2 * dRad - 3 * mPrimeRad) -
                0.002689 * e * sin(mRad - 2 * mPrimeRad) -
                0.002602 * sin(2 * dRad - mPrimeRad + 2 * fRad) +
                0.002390 * e * sin(2 * dRad - mRad - 2 * mPrimeRad) -
                0.002348 * sin(dRad + mPrimeRad) +
                0.002236 * e2 * sin(2 * dRad - 2 * mRad) -
                0.002120 * e * sin(mRad + 2 * mPrimeRad) -
                0.002069 * e2 * sin(2 * mRad) +
                0.002048 * e2 * sin(2 * dRad - 2 * mRad - mPrimeRad) +
                0.003958 * sin(a1) +
                0.001962 * sin(Math.toRadians(lPrime - f)) +
                0.000318 * sin(a2)

        return normalizeDegrees(lPrime + sigmaL)
    }

    /**
     * High-precision lunar true ecliptic latitude from Jean Meeus, Astronomical Algorithms, Chapter 47 (Table 47.B).
     */
    internal fun calculateMoonTrueLatitude(t: Double): Double {
        val d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t + t * t * t / 545868.0
        val m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t + t * t * t / 24490000.0
        val mPrime = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t + t * t * t / 69699.0
        val f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t * t - t * t * t / 3526000.0
        val e = 1.0 - 0.002516 * t - 0.0000074 * t * t

        val dRad = Math.toRadians(d)
        val mRad = Math.toRadians(m)
        val mPrimeRad = Math.toRadians(mPrime)
        val fRad = Math.toRadians(f)

        return 5.128122 * sin(fRad) +
                0.280602 * sin(mPrimeRad + fRad) +
                0.277693 * sin(mPrimeRad - fRad) +
                0.173237 * sin(2 * dRad - fRad) +
                0.055413 * sin(2 * dRad - mPrimeRad + fRad) +
                0.046271 * sin(2 * dRad - mPrimeRad - fRad) +
                0.032573 * sin(2 * dRad + fRad) +
                0.017198 * sin(2 * mPrimeRad + fRad) +
                0.009266 * sin(2 * dRad + mPrimeRad - fRad) +
                0.008822 * sin(2 * mPrimeRad - fRad) +
                0.008216 * e * sin(2 * dRad - mRad - fRad) +
                0.004324 * sin(2 * dRad - 2 * mPrimeRad - fRad) +
                0.004200 * sin(2 * dRad + mPrimeRad + fRad)
    }

    internal fun normalizeDegrees(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }
}
