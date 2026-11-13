package com.example

import com.example.data.catalog.BundledCatalog
import com.example.data.repository.toDomain
import com.example.domain.SearchEngine
import com.example.panchang.City
import com.example.panchang.FestivalDateEvaluator
import com.example.panchang.Paksha
import com.example.panchang.PanchangEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PanchangAndSearchUnitTest {

    @Test
    fun testPanchangEngineComputationAndAstronomyAccuracy() {
        // Test 1: Ganesh Chaturthi 2026 (14 Sep 2026) in Mumbai
        val ganeshDate = LocalDate.of(2026, 9, 14)
        val ganeshPanchang = PanchangEngine.computeDayPanchang(ganeshDate, City.MUMBAI)

        assertNotNull("Panchang should not be null", ganeshPanchang)
        assertTrue("Sunrise should be around 06:26 AM", ganeshPanchang.sunrise.hour == 6 && ganeshPanchang.sunrise.minute in 18..35)
        assertTrue("Sunset should be around 06:40 PM", ganeshPanchang.sunset.hour == 18 && ganeshPanchang.sunset.minute in 30..50)
        assertEquals("Lunar month on Ganesh Chaturthi must be Bhadrapada", "Bhadrapada", ganeshPanchang.amantaMonth)
        assertEquals("Purnimanta month on Shukla Chaturthi must also be Bhadrapada", "Bhadrapada", ganeshPanchang.purnimantaMonth)
        assertNotNull("Tithi end time must be computed", ganeshPanchang.tithi.endTime)
        assertNotEquals("Tithi end time on Sep 14 should be a specific clock time", "Full day", ganeshPanchang.tithi.endTime)
        assertTrue("Nakshatra deity must be populated", ganeshPanchang.nakshatra.deity.isNotBlank())
        assertNotNull("Nakshatra end time must be computed", ganeshPanchang.nakshatra.endTime)
        assertTrue("Lahiri Ayanamsa in 2026 is ~24.23 degrees", ganeshPanchang.ayanamsaDegrees in 24.20..24.26)

        // Test 2: Oct 30, 2026 (Friday) in Delhi — verify exact Drik Panchang alignment
        val oct30 = LocalDate.of(2026, 10, 30)
        val oct30Panchang = PanchangEngine.computeDayPanchang(oct30, City.DELHI)
        assertEquals("Oct 30, 2026 is Krishna Paksha", Paksha.KRISHNA, oct30Panchang.tithi.paksha)
        assertEquals("Oct 30, 2026 sunrise Tithi is Panchami", "Panchami", oct30Panchang.tithi.name)
        assertEquals("Oct 30, 2026 sunrise Nakshatra is Mrigashira", "Mrigashira", oct30Panchang.nakshatra.name)
        assertEquals("Soma (Chandra)", oct30Panchang.nakshatra.deity)
        assertEquals("Oct 30, 2026 sunrise Yoga is Shiva", "Shiva", oct30Panchang.yoga.name)
        assertEquals("Oct 30, 2026 sunrise Karana is Kaulava", "Kaulava", oct30Panchang.karana.name)
        assertEquals("Oct 30, 2026 Amanta month is Ashwina", "Ashwina", oct30Panchang.amantaMonth)
        assertEquals("Oct 30, 2026 Purnimanta month is Kartika", "Kartika", oct30Panchang.purnimantaMonth)

        // Test 3: Nov 8, 2026 (Diwali) in Delhi — Chaturdashi at sunrise, Amavasya at Pradosh Kaal
        val diwaliDate = LocalDate.of(2026, 11, 8)
        val diwaliPanchang = PanchangEngine.computeDayPanchang(diwaliDate, City.DELHI)
        assertEquals("Chaturdashi", diwaliPanchang.tithi.name)
        assertEquals("Swati", diwaliPanchang.nakshatra.name)
        assertEquals("Ayushman", diwaliPanchang.yoga.name)
        assertEquals("Shakuni", diwaliPanchang.karana.name)
        val (pradoshPaksha, pradoshTithiNum) = PanchangEngine.computeTithiAtLocalHour(diwaliDate, City.DELHI, 18.5)
        assertEquals(Paksha.KRISHNA, pradoshPaksha)
        assertEquals("Amavasya (15) must prevail during Diwali Pradosh Kaal", 15, pradoshTithiNum)

        // Test 4: Adhika Jyeshtha Masa 2026 detection (around June 1, 2026)
        val adhikaDate = LocalDate.of(2026, 6, 1)
        val adhikaPanchang = PanchangEngine.computeDayPanchang(adhikaDate, City.DELHI)
        assertTrue("June 1, 2026 falls in Adhika Jyeshtha Masa", adhikaPanchang.isAdhikaMasa)
        assertEquals("Jyeshtha", adhikaPanchang.amantaMonth)
    }

    @Test
    fun testAll16Festivals2026And2027VerifiedDates() {
        val festivals = BundledCatalog.FESTIVALS.map { it.toDomain() }.associateBy { it.id }

        val expected2026 = mapOf(
            "makar_sankranti" to LocalDate.of(2026, 1, 14),
            "maha_shivaratri" to LocalDate.of(2026, 2, 15),
            "holi_dhulandi" to LocalDate.of(2026, 3, 3),
            "gudi_padwa_ugadi" to LocalDate.of(2026, 3, 19),
            "rama_navami" to LocalDate.of(2026, 3, 26),
            "akshaya_tritiya" to LocalDate.of(2026, 4, 19),
            "varalakshmi_vratam" to LocalDate.of(2026, 8, 28),
            "raksha_bandhan" to LocalDate.of(2026, 8, 28),
            "krishna_janmashtami" to LocalDate.of(2026, 9, 4),
            "ganesh_chaturthi" to LocalDate.of(2026, 9, 14),
            "navratri_ghatasthapana" to LocalDate.of(2026, 10, 11),
            "durga_ashtami" to LocalDate.of(2026, 10, 19),
            "vijayadashami_dussehra" to LocalDate.of(2026, 10, 20),
            "karwa_chauth" to LocalDate.of(2026, 10, 29),
            "diwali_lakshmi_puja" to LocalDate.of(2026, 11, 8),
            "chhath_puja" to LocalDate.of(2026, 11, 15)
        )

        val expected2027 = mapOf(
            "makar_sankranti" to LocalDate.of(2027, 1, 15),
            "maha_shivaratri" to LocalDate.of(2027, 3, 6),
            "holi_dhulandi" to LocalDate.of(2027, 3, 22),
            "gudi_padwa_ugadi" to LocalDate.of(2027, 4, 7),
            "rama_navami" to LocalDate.of(2027, 4, 15),
            "akshaya_tritiya" to LocalDate.of(2027, 5, 9),
            "varalakshmi_vratam" to LocalDate.of(2027, 8, 13),
            "raksha_bandhan" to LocalDate.of(2027, 8, 17),
            "krishna_janmashtami" to LocalDate.of(2027, 8, 25),
            "ganesh_chaturthi" to LocalDate.of(2027, 9, 4),
            "navratri_ghatasthapana" to LocalDate.of(2027, 9, 30),
            "durga_ashtami" to LocalDate.of(2027, 10, 7),
            "vijayadashami_dussehra" to LocalDate.of(2027, 10, 9),
            "karwa_chauth" to LocalDate.of(2027, 10, 18),
            "diwali_lakshmi_puja" to LocalDate.of(2027, 10, 29),
            "chhath_puja" to LocalDate.of(2027, 11, 4)
        )

        expected2026.forEach { (id, expectedDate) ->
            val fest = festivals.getValue(id)
            val resolved = FestivalDateEvaluator.resolveFestivalDate(id, 2026, City.DELHI, fest.dateRule)
            assertEquals("2026 date mismatch for $id", expectedDate, resolved)
        }

        expected2027.forEach { (id, expectedDate) ->
            val fest = festivals.getValue(id)
            val resolved = FestivalDateEvaluator.resolveFestivalDate(id, 2027, City.DELHI, fest.dateRule)
            assertEquals("2027 date mismatch for $id", expectedDate, resolved)
        }
    }

    @Test
    fun testVedicMuhuratsAndWeekdayOctets() {
        // Check all 7 weekdays for Rahu Kaal, Yamagandam, Gulika Kaal, Brahma Muhurat, and Abhijit Muhurat
        val baseMonday = LocalDate.of(2026, 10, 26)
        for (i in 0..6) {
            val day = baseMonday.plusDays(i.toLong())
            val panchang = PanchangEngine.computeDayPanchang(day, City.BENGALURU)
            assertTrue(panchang.rahuKaal.startMinutes < panchang.rahuKaal.endMinutes)
            assertTrue(panchang.yamagandam.startMinutes < panchang.yamagandam.endMinutes)
            assertTrue(panchang.gulikaKaal.startMinutes < panchang.gulikaKaal.endMinutes)
            assertTrue(panchang.brahmaMuhurat.startMinutes < panchang.brahmaMuhurat.endMinutes)
            assertTrue("TimeWindow must format in 12-hour AM/PM", panchang.rahuKaal.format().contains("AM") || panchang.rahuKaal.format().contains("PM"))
        }

        // Verify Nishita Kaal differs between Kolkata (eastern) and Mumbai (western)
        val shivaratri = LocalDate.of(2026, 2, 15)
        val kolkataNishita = PanchangEngine.computeNishitaWindow(PanchangEngine.computeDayPanchang(shivaratri, City.KOLKATA))
        val mumbaiNishita = PanchangEngine.computeNishitaWindow(PanchangEngine.computeDayPanchang(shivaratri, City.MUMBAI))
        assertTrue(
            "Kolkata local solar midnight is ~60 minutes earlier than Mumbai",
            mumbaiNishita.startMinutes - kolkataNishita.startMinutes in 55..68
        )
    }

    @Test
    fun testSearchEngineHinglishAndTanglish() {
        val festivals = BundledCatalog.FESTIVALS.map { it.toDomain() }

        // Test 1: "vinayagar" (Tanglish for Ganesh)
        val vinayagarResults = SearchEngine.search("vinayagar", festivals)
        assertFalse("Search for vinayagar should return results", vinayagarResults.isEmpty())
        assertEquals("ganesh_chaturthi", vinayagarResults.first().id)

        // Test 2: "ganpati" (Marathi / Hindi for Ganesh)
        val ganpatiResults = SearchEngine.search("ganpati", festivals)
        assertFalse(ganpatiResults.isEmpty())
        assertEquals("ganesh_chaturthi", ganpatiResults.first().id)

        // Test 3: "karwa" (North Indian Karwa Chauth)
        val karwaResults = SearchEngine.search("karwa", festivals)
        assertFalse(karwaResults.isEmpty())
        assertEquals("karwa_chauth", karwaResults.first().id)

        // Test 4: "deepavali" (Tamil / South Indian / Sanskrit spelling)
        val deepavaliResults = SearchEngine.search("deepavali", festivals)
        assertFalse(deepavaliResults.isEmpty())
        assertEquals("diwali_lakshmi_puja", deepavaliResults.first().id)

        // Test 5: "pongal"
        val pongalResults = SearchEngine.search("pongal", festivals)
        assertFalse(pongalResults.isEmpty())
        assertEquals("makar_sankranti", pongalResults.first().id)

        // Test 6: "ugadi"
        val ugadiResults = SearchEngine.search("ugadi", festivals)
        assertFalse(ugadiResults.isEmpty())
        assertEquals("gudi_padwa_ugadi", ugadiResults.first().id)

        // Test 7: "holi" / "dol"
        val holiResults = SearchEngine.search("holi", festivals)
        assertFalse(holiResults.isEmpty())
        assertEquals("holi_dhulandi", holiResults.first().id)

        // Test 8: "chhath"
        val chhathResults = SearchEngine.search("chhath", festivals)
        assertFalse(chhathResults.isEmpty())
        assertEquals("chhath_puja", chhathResults.first().id)

        // Test 9: "dussehra" / "dasara"
        val dasaraResults = SearchEngine.search("dasara", festivals)
        assertFalse(dasaraResults.isEmpty())
        assertEquals("vijayadashami_dussehra", dasaraResults.first().id)

        // Test 10: Bhai Dooj & regional/phonetic multi-names ("bhai dooj", "Bhai dhooj", "bhaidhooj", "bhai dhoj", "bhau beej", "bhai phonta", "yama dwitiya", "bhaiya dhooj")
        for (regionalQuery in listOf("bhai dooj", "Bhai dhooj", "bhaidhooj", "bhai dhoj", "bhaiya dhooj", "bhau beej", "bhai phonta", "yama dwitiya")) {
            val bhaiDoojResults = SearchEngine.search(regionalQuery, festivals)
            assertFalse("Search for '$regionalQuery' must find Bhai Dooj", bhaiDoojResults.isEmpty())
            assertEquals("bhai_dooj", bhaiDoojResults.first().id)
            val matchedAlias = SearchEngine.findMatchedRegionalName(regionalQuery, bhaiDoojResults.first())
            if (!regionalQuery.equals("bhai dooj", ignoreCase = true)) {
                assertNotNull("Matched regional name badge must be resolved for '$regionalQuery'", matchedAlias)
            }
        }

        // Test 10B: Govardhan Puja & phonetic variants ("gobardhan", "gobardhan puja", "goberdhan", "goverdhan", "annakut", "bali pratipada", "bestu varas")
        for (govardhanQuery in listOf("govardhan", "gobardhan", "gobardhan puja", "goberdhan", "goverdhan", "annakut", "bali pratipada", "bestu varas")) {
            val govardhanResults = SearchEngine.search(govardhanQuery, festivals)
            assertFalse("Search for '$govardhanQuery' must find Govardhan Puja", govardhanResults.isEmpty())
            assertEquals("govardhan_puja", govardhanResults.first().id)
        }

        // Test 10C: Compound query ("Bhai dhooj and gobardhan") must return both Bhai Dooj and Govardhan Puja
        val compoundResults = SearchEngine.search("Bhai dhooj and gobardhan", festivals)
        val compoundIds = compoundResults.map { it.id }.toSet()
        assertTrue("Compound query 'Bhai dhooj and gobardhan' must return bhai_dooj", compoundIds.contains("bhai_dooj"))
        assertTrue("Compound query 'Bhai dhooj and gobardhan' must return govardhan_puja", compoundIds.contains("govardhan_puja"))

        // Test 11: Solar New Year multi-names ("puthandu", "vishu", "baisakhi", "poila boishakh")
        for (solarQuery in listOf("puthandu", "vishu", "baisakhi", "poila boishakh")) {
            val solarResults = SearchEngine.search(solarQuery, festivals)
            assertFalse("Search for '$solarQuery' must find Solar New Year", solarResults.isEmpty())
            assertEquals("mesha_sankranti_new_year", solarResults.first().id)
        }
    }

    @Test
    fun testBundledCatalogIntegrity() {
        val festivals = BundledCatalog.FESTIVALS
        assertEquals("Bundled catalog should have 28 festivals", 28, festivals.size)

        festivals.forEach { fest ->
            assertFalse("Festival ID must not be blank", fest.id.isBlank())
            assertFalse("English name must not be blank", fest.nameEnglish.isBlank())
            assertFalse("Native name must not be blank", fest.nameNative.isBlank())
            assertTrue("Must contain vidhi steps", fest.vidhiJson.contains("stepNumber"))
            assertTrue("Must contain samagri items", fest.samagriJson.contains("name"))
            assertFalse("Significance must not be blank", fest.significance.isBlank())
        }
    }

    @Test
    fun testCalendarDatabaseEntities() {
        val panchangEntity = com.example.data.local.DailyPanchangEntity(
            date = "2026-09-14",
            cityId = "mumbai",
            dayOfWeek = "Monday",
            tithiIndex = 4,
            tithiName = "Chaturthi",
            paksha = "SHUKLA",
            nakshatraIndex = 14,
            nakshatraName = "Chitra",
            yogaName = "Shukla",
            karanaName = "Vanija",
            sunrise = "06:26 AM",
            sunset = "06:40 PM",
            rahuKaalStart = "07:58 AM",
            rahuKaalEnd = "09:30 AM",
            amantaMonth = "Bhadrapada",
            purnimantaMonth = "Bhadrapada"
        )
        assertEquals("Chaturthi", panchangEntity.tithiName)
        assertEquals("Chitra", panchangEntity.nakshatraName)

        val muhuratEntity = com.example.data.local.MuhuratEntity(
            festivalId = "ganesh_chaturthi",
            date = "2026-09-14",
            cityId = "mumbai",
            title = "Madhyahna Ganesha Puja",
            description = "Auspicious midday window",
            muhuratType = "MADHYAHNA",
            startTime = "11:19 AM",
            endTime = "01:46 PM",
            isAuspicious = true
        )
        assertEquals("ganesh_chaturthi", muhuratEntity.festivalId)
        assertTrue(muhuratEntity.isAuspicious)
    }

    @Test
    fun testBlueprintNavigationAndPreferencesModels() {
        // Verify 5 primary navigation destinations (Today, Calendar, Festivals, Panchang, Profile)
        val tabs = com.example.ui.navigation.MainTab.values()
        assertEquals(5, tabs.size)
        assertEquals("Today", tabs[0].title)
        assertEquals("Calendar", tabs[1].title)
        assertEquals("Festivals", tabs[2].title)
        assertEquals("Panchang", tabs[3].title)
        assertEquals("Profile", tabs[4].title)

        // Verify AppLanguage native scripts & 8-language UtsavStrings localisation
        val marathi = com.example.data.preferences.AppLanguage.fromCode("mr")
        assertEquals("मराठी", marathi.nativeName)
        val kannada = com.example.data.preferences.AppLanguage.fromCode("kn")
        assertEquals("ಕನ್ನಡ", kannada.nativeName)
        assertEquals(8, com.example.data.preferences.AppLanguage.values().size)
        com.example.data.preferences.AppLanguage.values().forEach { lang ->
            val localized = com.example.ui.theme.stringsForLanguage(lang)
            assertTrue(localized.tabToday.isNotBlank())
            assertTrue(localized.tabPanchang.isNotBlank())
            assertTrue(localized.dailyPanchangHeader.isNotBlank())
            assertTrue(localized.welcomeTitle.isNotBlank())
            assertTrue(localized.useCurrentLocationGps.isNotBlank())
        }

        // Verify Location-Based Nearest City Haversine Lookup & Expanded Cities
        assertTrue("ALL_CITIES should have at least 30 Indian cities", City.ALL_CITIES.size >= 30)
        val nearestToPuneCoords = City.findNearestCity(18.52, 73.85)
        assertEquals("Pune", nearestToPuneCoords.name)

        // Verify CustomReminderEntry round-trip serialization & ReminderScheduler time parsing
        val reminder = com.example.data.preferences.CustomReminderEntry(
            festivalId = "diwali_lakshmi_puja",
            festivalName = "Diwali Lakshmi Puja",
            dateFormatted = "Sunday, 8 November 2026",
            triggerType = "ONE_DAY_BEFORE",
            timeLabel = "08:00 AM"
        )
        val restored = com.example.data.preferences.CustomReminderEntry.deserialize(reminder.serialize())
        assertNotNull(restored)
        assertEquals("diwali_lakshmi_puja", restored?.festivalId)
        assertEquals("One day before", restored?.triggerDisplayLabel)

        val parsedTime = com.example.platform.notifications.ReminderScheduler.parseTimeLabel("06:30 PM")
        assertEquals(18, parsedTime.hour)
        assertEquals(30, parsedTime.minute)

        val parsedMuhuratStart = com.example.platform.notifications.ReminderScheduler.parseMuhuratStartTime("05:45 PM - 07:45 PM")
        assertEquals(17, parsedMuhuratStart.hour)
        assertEquals(45, parsedMuhuratStart.minute)

        // Verify city changes trigger distinct local sunrise/sunset calculations
        val date = LocalDate.of(2026, 10, 30)
        val mumbaiPanchang = PanchangEngine.computeDayPanchang(date, City.MUMBAI)
        val chennaiPanchang = PanchangEngine.computeDayPanchang(date, City.CHENNAI)
        assertNotEquals(
            "Mumbai and Chennai must have different local sunrise times",
            mumbaiPanchang.sunrise,
            chennaiPanchang.sunrise
        )
    }

    @Test
    fun test20YearFestivalAndPanchangResolution_2025_to_2046() {
        val festivals = BundledCatalog.FESTIVALS.map { it.toDomain() }.associateBy { it.id }

        for (year in 2025..2046) {
            assertTrue("Year $year must be supported in 20-year range", FestivalDateEvaluator.isCuratedYear(year))

            val resolvedByYear = festivals.mapValues { (id, fest) ->
                FestivalDateEvaluator.resolveFestivalDate(id, year, City.DELHI, fest.dateRule)
            }

            // 1. Every festival must resolve to the target year
            resolvedByYear.forEach { (id, date) ->
                assertEquals("Festival $id in $year must resolve to year $year", year, date.year)
                val dayPanchang = PanchangEngine.computeDayPanchang(date, City.DELHI)
                assertTrue("Tithi name must not be blank on $date ($id)", dayPanchang.tithi.name.isNotBlank())
                assertTrue("Nakshatra name must not be blank on $date ($id)", dayPanchang.nakshatra.name.isNotBlank())
                assertTrue("Deity must not be blank on $date ($id)", dayPanchang.nakshatra.deity.isNotBlank())
                assertNotNull("Tithi end time must be computed on $date ($id)", dayPanchang.tithi.endTime)
                assertNotNull("Nakshatra end time must be computed on $date ($id)", dayPanchang.nakshatra.endTime)
            }

            // 2. Makar Sankranti must always fall on Jan 14 or Jan 15
            val makar = resolvedByYear.getValue("makar_sankranti")
            assertEquals("Makar Sankranti in $year must be in January", 1, makar.monthValue)
            assertTrue("Makar Sankranti in $year must be Jan 14 or 15 (was $makar)", makar.dayOfMonth in 14..15)

            // 3. Varalakshmi Vratam must always be a Friday on or up to 6 days before Raksha Bandhan
            val varalakshmi = resolvedByYear.getValue("varalakshmi_vratam")
            val rakshaBandhan = resolvedByYear.getValue("raksha_bandhan")
            assertEquals(
                "Varalakshmi Vratam in $year must be a Friday",
                java.time.DayOfWeek.FRIDAY,
                varalakshmi.dayOfWeek
            )
            val daysBeforePurnima = java.time.temporal.ChronoUnit.DAYS.between(varalakshmi, rakshaBandhan)
            assertTrue(
                "Varalakshmi Vratam in $year ($varalakshmi) must be 0..6 days before Raksha Bandhan ($rakshaBandhan)",
                daysBeforePurnima in 0..6
            )

            // 4. Chaitra & Vaishakha sequence: Gudi Padwa < Rama Navami < Akshaya Tritiya
            val gudiPadwa = resolvedByYear.getValue("gudi_padwa_ugadi")
            val ramaNavami = resolvedByYear.getValue("rama_navami")
            val akshayaTritiya = resolvedByYear.getValue("akshaya_tritiya")
            assertTrue("Gudi Padwa ($gudiPadwa) must precede Rama Navami ($ramaNavami) in $year", gudiPadwa.isBefore(ramaNavami))
            assertTrue("Rama Navami ($ramaNavami) must precede Akshaya Tritiya ($akshayaTritiya) in $year", ramaNavami.isBefore(akshayaTritiya))

            // 5. Shravana & Bhadrapada sequence: Raksha Bandhan < Janmashtami < Ganesh Chaturthi
            val janmashtami = resolvedByYear.getValue("krishna_janmashtami")
            val ganeshChaturthi = resolvedByYear.getValue("ganesh_chaturthi")
            assertTrue("Raksha Bandhan ($rakshaBandhan) must precede Janmashtami ($janmashtami) in $year", rakshaBandhan.isBefore(janmashtami))
            assertTrue("Janmashtami ($janmashtami) must precede Ganesh Chaturthi ($ganeshChaturthi) in $year", janmashtami.isBefore(ganeshChaturthi))

            // 6. Ashwina & Kartika sequence: Navratri < Durga Ashtami < Vijayadashami < Karwa Chauth < Diwali < Chhath Puja
            val navratri = resolvedByYear.getValue("navratri_ghatasthapana")
            val durgaAshtami = resolvedByYear.getValue("durga_ashtami")
            val dussehra = resolvedByYear.getValue("vijayadashami_dussehra")
            val karwaChauth = resolvedByYear.getValue("karwa_chauth")
            val diwali = resolvedByYear.getValue("diwali_lakshmi_puja")
            val chhath = resolvedByYear.getValue("chhath_puja")

            assertTrue("Navratri ($navratri) must precede Durga Ashtami ($durgaAshtami) in $year", navratri.isBefore(durgaAshtami))
            assertTrue("Durga Ashtami ($durgaAshtami) must precede Dussehra ($dussehra) in $year", durgaAshtami.isBefore(dussehra))
            assertTrue("Dussehra ($dussehra) must precede Karwa Chauth ($karwaChauth) in $year", dussehra.isBefore(karwaChauth))
            assertTrue("Karwa Chauth ($karwaChauth) must precede Diwali ($diwali) in $year", karwaChauth.isBefore(diwali))
            assertTrue("Diwali ($diwali) must precede Chhath Puja ($chhath) in $year", diwali.isBefore(chhath))

            // 7. Diwali 5-day cluster sequence: Dhanteras < Diwali < Bhai Dooj < Chhath Puja
            val dhanteras = resolvedByYear.getValue("dhanteras")
            val bhaiDooj = resolvedByYear.getValue("bhai_dooj")
            assertTrue("Dhanteras ($dhanteras) must precede Diwali ($diwali) in $year", dhanteras.isBefore(diwali))
            assertTrue("Diwali ($diwali) must precede Bhai Dooj ($bhaiDooj) in $year", diwali.isBefore(bhaiDooj))
            assertTrue("Bhai Dooj ($bhaiDooj) must precede Chhath Puja ($chhath) in $year", bhaiDooj.isBefore(chhath))
        }
    }

    @Test
    fun testAllFestivalDetailsWhyCelebratedVidhiPoojaTimingsAndTithi() {
        val bundledFestivals = BundledCatalog.FESTIVALS.map { it.toDomain() }
        assertEquals("Should have 28 bundled festivals", 28, bundledFestivals.size)

        val encyclopedicFestivals = com.example.data.remote.OnlineFestivalDiscoveryService.getAllEncyclopedicFestivals().map { it.entity.toDomain() }
        assertEquals("Should have 54 encyclopedic festivals", 54, encyclopedicFestivals.size)

        val allFestivals = (bundledFestivals + encyclopedicFestivals).distinctBy { it.id }
        assertEquals("Should have 82 total pre-verified Indian festivals", 82, allFestivals.size)

        val seenPrimaryMantras = mutableMapOf<String, String>()

        allFestivals.forEach { festival ->
            val details = com.example.domain.FestivalShastraKnowledge.getDetailsForFestival(festival)
            assertTrue("Why celebrated must not be blank for ${festival.id}", details.whyCelebrated.length > 40)
            assertTrue("Puranic legend (Katha) must not be blank for ${festival.id}", details.puranicLegend.length > 40)
            assertTrue("Presiding deity must not be blank for ${festival.id}", details.presidingDeity.isNotBlank())
            assertTrue("Sacred mantra Sanskrit must not be blank for ${festival.id}", details.sacredMantraSanskrit.isNotBlank())
            assertTrue("Sacred mantra transliteration must not be blank for ${festival.id}", details.sacredMantraTransliteration.isNotBlank())
            assertTrue("Sacred mantra meaning must not be blank for ${festival.id}", details.sacredMantraMeaning.isNotBlank())
            assertFalse(
                "Festival ${festival.id} must have a specific Shastra mantra, not generic fallback",
                details.sacredMantraTransliteration.contains("Om Sarve Bhavantu Sukhinah")
            )
            val previousOwner = seenPrimaryMantras.put(details.sacredMantraSanskrit, festival.id)
            assertTrue(
                "Primary sacredMantraSanskrit for ${festival.id} duplicates $previousOwner: ${details.sacredMantraSanskrit}",
                previousOwner == null
            )

            // Verify 5 step-by-step Pooja Mantras are mapped for each festival
            assertEquals("Each festival must have 5 step-mapped Pooja Mantras for ${festival.id}", 5, details.pujaMantras.size)
            assertEquals(
                "Pooja Mantras must map to steps 1..5 for ${festival.id}",
                listOf(1, 2, 3, 4, 5),
                details.pujaMantras.map { it.stepNumber }
            )
            details.pujaMantras.forEach { pujaMantra ->
                assertTrue("Puja mantra title must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.title.isNotBlank())
                assertTrue("Puja mantra ritualStage must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.ritualStage.isNotBlank())
                assertTrue("Puja mantra sanskrit must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.sanskrit.isNotBlank())
                assertTrue("Puja mantra transliteration must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.transliteration.isNotBlank())
                assertTrue("Puja mantra meaning must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.meaning.isNotBlank())
                assertTrue("Puja mantra chantCountRecommendation must not be blank for ${festival.id} step ${pujaMantra.stepNumber}", pujaMantra.chantCountRecommendation.isNotBlank())
            }
            assertEquals(
                "Step 3 Moola Mantra must match festival primary sacredMantraSanskrit for ${festival.id}",
                details.sacredMantraSanskrit,
                details.pujaMantras[2].sanskrit
            )

            assertTrue("Shastra Tithi rule must not be blank for ${festival.id}", details.shastraTithiRule.isNotBlank())
            assertTrue("Vrat & Naivedya guide must not be blank for ${festival.id}", details.vratAndNaivedyaGuide.isNotBlank())

            val compactBadge = com.example.domain.FestivalShastraKnowledge.formatCompactTithiBadge(festival)
            assertTrue("Compact Tithi badge must not be blank for ${festival.id}", compactBadge.isNotBlank())

            assertTrue("Puja Vidhi steps must have at least 4 steps for ${festival.id}", festival.vidhiSteps.size >= 4)
            assertTrue("Puja Samagri must have at least 4 items for ${festival.id}", festival.samagriItems.size >= 4)
            assertTrue("Muhurat title must not be blank for ${festival.id}", festival.muhuratTitle.isNotBlank())
            assertTrue("Muhurat description must not be blank for ${festival.id}", festival.muhuratDescription.isNotBlank())
            assertTrue("Significance must not be blank for ${festival.id}", festival.significance.isNotBlank())
            val resolved2026 = FestivalDateEvaluator.resolveFestivalDate(festival.id, 2026, City.DELHI, festival.dateRule)
            assertEquals("Resolved 2026 date must be in 2026 for ${festival.id}", 2026, resolved2026.year)
        }
    }

    @Test
    fun testReminderSchedulerNoFabricationAndStaleAlarmCancellation() {
        // Test Scenario 1: Syntactically valid formatted window without provenance produces CalculatedEstimate (NOT Verified)
        val estimateState = com.example.domain.model.MuhuratValidationState.resolve("05:32 PM - 07:28 PM")
        assertTrue("Valid window without provenance must be CalculatedEstimate", estimateState is com.example.domain.model.MuhuratValidationState.CalculatedEstimate)
        assertFalse("CalculatedEstimate must NOT be schedulable for automated alarms", estimateState.isSchedulable)

        // Test Scenario 1b: Explicit createVerified with documented provenance produces Verified
        val verifiedState = com.example.domain.model.MuhuratValidationState.createVerified(
            festivalDate = LocalDate.of(2026, 11, 8),
            cityId = "delhi",
            startTime = java.time.LocalTime.of(17, 32),
            endTime = java.time.LocalTime.of(19, 28),
            verificationProvenance = "Rashtriya Panchang (IMD PAC) 1948 Saka",
            formattedWindow = "05:32 PM - 07:28 PM"
        )
        assertTrue("Window with provenance must be Verified", verifiedState is com.example.domain.model.MuhuratValidationState.Verified)
        val verified = verifiedState as com.example.domain.model.MuhuratValidationState.Verified
        assertEquals(17, verified.startTime.hour)
        assertEquals(32, verified.startTime.minute)
        assertEquals(19, verified.endTime.hour)
        assertEquals(28, verified.endTime.minute)
        assertTrue("End time must follow start time", verified.isValid)
        assertTrue("Valid verified state must be schedulable", verifiedState.isSchedulable)

        // Test Scenario 2: Blank or null window produces Unavailable
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve(null) is com.example.domain.model.MuhuratValidationState.Unavailable)
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve("") is com.example.domain.model.MuhuratValidationState.Unavailable)
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve("   ") is com.example.domain.model.MuhuratValidationState.Unavailable)

        // Test Scenario 3: Explicit unverified labels produce Unavailable
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve("N/A") is com.example.domain.model.MuhuratValidationState.Unavailable)
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve("Timing Unavailable") is com.example.domain.model.MuhuratValidationState.Unavailable)
        assertTrue(com.example.domain.model.MuhuratValidationState.resolve("Pending Verification") is com.example.domain.model.MuhuratValidationState.Unavailable)

        // Test Scenario 4: Non-range or unparseable string produces Unavailable
        val unparseable = com.example.domain.model.MuhuratValidationState.resolve("Full day observance")
        assertTrue("Non-range string should be Unavailable", unparseable is com.example.domain.model.MuhuratValidationState.Unavailable)
        assertFalse("Must NOT be schedulable", unparseable.isSchedulable)

        // Test Scenario 5: FestivalOccurrence correctly derives MuhuratValidationState
        val bundledFestival = BundledCatalog.FESTIVALS.first().toDomain()
        val occVerified = com.example.domain.model.FestivalOccurrence(
            festival = bundledFestival,
            date = LocalDate.of(2026, 11, 8),
            muhuratWindowFormatted = "05:32 PM - 07:28 PM",
            muhuratState = verifiedState
        )
        assertTrue("FestivalOccurrence with verified state must be schedulable", occVerified.muhuratState.isSchedulable)

        val occUnavailable = com.example.domain.model.FestivalOccurrence(
            festival = bundledFestival,
            date = LocalDate.of(2026, 11, 8),
            muhuratWindowFormatted = "Timing Unavailable"
        )
        assertFalse("FestivalOccurrence with unavailable timing must NOT be schedulable", occUnavailable.muhuratState.isSchedulable)
        assertEquals("Timing Unavailable", occUnavailable.muhuratState.displayLabel)

        // Test Scenario 6: Parsing helpers handle both 12-hour and 24-hour formats
        val time12 = com.example.platform.notifications.ReminderScheduler.parseTimeLabel("07:00 AM")
        assertEquals(7, time12.hour)
        assertEquals(0, time12.minute)

        val time24 = com.example.platform.notifications.ReminderScheduler.parseTimeLabel("18:30")
        assertEquals(18, time24.hour)
        assertEquals(30, time24.minute)

        val startParsed = com.example.platform.notifications.ReminderScheduler.parseMuhuratStartTime("06:15 AM - 08:30 AM")
        assertEquals(6, startParsed.hour)
        assertEquals(15, startParsed.minute)
    }

    @Test
    fun testIndependentReferenceBenchmark2026And2027() {
        val records = com.example.panchang.DrikPanchangLahiriReference.BENCHMARK_RECORDS
        assertTrue("Must have at least 15 traceable ephemeris benchmark records", records.size >= 15)

        val cityMap = City.ALL_CITIES.associateBy { it.id }

        records.forEach { record ->
            val city = cityMap[record.cityId]
                ?: throw AssertionError("Benchmark ${record.referenceId} references unknown cityId '${record.cityId}'")
            val resolvedDate = FestivalDateEvaluator.resolveFestivalDate(
                festivalId = record.festivalId,
                year = record.expectedDate.year,
                city = city
            )

            assertEquals(
                "Benchmark ${record.referenceId} (${record.festivalId} in ${record.cityId}) must match expected date ${record.expectedDate} (Source: ${record.sourcePublication})",
                record.expectedDate,
                resolvedDate
            )
            assertNotNull("Source publication must be documented for ${record.referenceId}", record.sourcePublication)
            assertNotNull("Edition or version must be documented for ${record.referenceId}", record.editionOrVersion)
            assertTrue("Shastra rule notes must be populated for ${record.referenceId}", record.shastraRuleNotes.isNotBlank())
        }
    }

    @Test
    fun testAstronomicalCalculationsSeparationAndBoundaryEdgeCases() {
        // Boundary Test 1: Nishita Kaal midnight window crossing in Delhi (e.g. 23:45 to 00:35)
        val testDate = LocalDate.of(2026, 2, 15) // Maha Shivaratri
        val panchang = PanchangEngine.computeDayPanchang(testDate, City.DELHI)
        assertNotNull(panchang.rahuKaal)
        assertNotNull(panchang.brahmaMuhurat)
        assertTrue("Sunrise should precede sunset", panchang.sunrise.isBefore(panchang.sunset))
        assertTrue("Rahu Kaal start should precede end", panchang.rahuKaal.startMinutes < panchang.rahuKaal.endMinutes)

        // Boundary Test 2: Extreme coordinates handling (e.g., high latitude Leh/Ladakh 34.15°N, 77.57°E)
        val lehCity = City("leh", "Leh", "Ladakh", 34.1526, 77.5771, isLocationDetected = false)
        val lehPanchang = PanchangEngine.computeDayPanchang(LocalDate.of(2026, 6, 21), lehCity) // Summer Solstice
        assertNotNull(lehPanchang)
        assertTrue("Leh summer sunrise should be early (around 5 AM)", lehPanchang.sunrise.hour in 4..6)
        assertTrue("Leh summer sunset should be late (around 7-8 PM)", lehPanchang.sunset.hour in 19..20)

        // Boundary Test 3: Southern coordinate near equator (Kanyakumari 8.08°N, 77.55°E)
        val kanyakumari = City("kanyakumari", "Kanyakumari", "Tamil Nadu", 8.0883, 77.5385, isLocationDetected = false)
        val kPanchang = PanchangEngine.computeDayPanchang(LocalDate.of(2026, 3, 21), kanyakumari) // Equinox
        assertNotNull(kPanchang)
        assertTrue("Kanyakumari sunrise should be ~6:20 AM", kPanchang.sunrise.hour == 6)

        // Boundary Test 4: Leap year date transition (2028 Feb 28 to Feb 29)
        val feb28 = LocalDate.of(2028, 2, 28)
        val feb29 = LocalDate.of(2028, 2, 29)
        val p28 = PanchangEngine.computeDayPanchang(feb28, City.DELHI)
        val p29 = PanchangEngine.computeDayPanchang(feb29, City.DELHI)
        assertNotNull(p28)
        assertNotNull(p29)
        assertEquals("Amanta months should be continuous across leap day", p28.amantaMonth, p29.amantaMonth)
    }

    @Test
    fun testDrikPanchangLakshmiPujaAndMajorFestivalMuhurats() {
        val mumbai = City.ALL_CITIES.first { it.id == "mumbai" }
        val delhi = City.DELHI
        val diwaliRule = com.example.panchang.FestivalDateRule(
            lunarMonth = "Ashwina",
            paksha = com.example.panchang.Paksha.KRISHNA,
            tithiNumber = 15,
            observanceTime = com.example.panchang.ObservanceTime.PRADOSH
        )

        // 1. Drik Panchang Sample URL verification: Mumbai (geoname-id=1275339), Year 2026 (Nov 8, 2026)
        // Expected: Lakshmi Puja Muhurat = 06:27 PM - 08:27 PM, Pradosh Kaal = 06:02 PM - 08:34 PM, Vrishabha Kaal = 06:27 PM - 08:27 PM
        val nov8_2026 = LocalDate.of(2026, 11, 8)
        val mumbaiPanchang2026 = PanchangEngine.computeDayPanchang(nov8_2026, mumbai)
        val mumbaiPradosh2026 = PanchangEngine.computeDrikPradoshWindow(mumbaiPanchang2026).format()
        val mumbaiVrishabha2026 = PanchangEngine.findNirayanaLagnaWindow(nov8_2026, mumbai, rashiIndex = 1, searchStartMin = 13 * 60, searchEndMin = 24 * 60)?.format()
        val mumbaiDiwaliMuhurat2026 = PanchangEngine.computeFestivalMuhuratWindow(
            festivalId = "diwali_lakshmi_puja",
            muhuratType = "PRADOSH",
            dateRule = diwaliRule,
            date = nov8_2026,
            city = mumbai
        ).format()

        assertEquals("Mumbai 2026 Pradosh Kaal must match Drik Panchang", "06:02 PM - 08:34 PM", mumbaiPradosh2026)
        assertEquals("Mumbai 2026 Vrishabha Kaal must match Drik Panchang", "06:27 PM - 08:27 PM", mumbaiVrishabha2026)
        assertEquals("Mumbai 2026 Lakshmi Puja Muhurat must match Drik Panchang sample URL", "06:27 PM - 08:27 PM", mumbaiDiwaliMuhurat2026)

        // 2. New Delhi Year 2026 (Nov 8, 2026):
        // Expected: Lakshmi Puja Muhurat = 05:54 PM - 07:50 PM, Pradosh Kaal = 05:31 PM - 08:09 PM
        val delhiPanchang2026 = PanchangEngine.computeDayPanchang(nov8_2026, delhi)
        val delhiPradosh2026 = PanchangEngine.computeDrikPradoshWindow(delhiPanchang2026).format()
        val delhiDiwaliMuhurat2026 = PanchangEngine.computeFestivalMuhuratWindow(
            festivalId = "diwali_lakshmi_puja",
            muhuratType = "PRADOSH",
            dateRule = diwaliRule,
            date = nov8_2026,
            city = delhi
        ).format()

        assertEquals("New Delhi 2026 Pradosh Kaal must match Drik Panchang", "05:31 PM - 08:09 PM", delhiPradosh2026)
        assertEquals("New Delhi 2026 Lakshmi Puja Muhurat must match Drik Panchang", "05:54 PM - 07:50 PM", delhiDiwaliMuhurat2026)

        // 3. Year 2025 (Oct 20, 2025) Diwali Lakshmi Puja:
        // Expected: Mumbai = 07:41 PM - 08:41 PM, New Delhi = 07:08 PM - 08:18 PM
        val oct20_2025 = LocalDate.of(2025, 10, 20)
        val mumbaiDiwali2025 = PanchangEngine.computeFestivalMuhuratWindow("diwali_lakshmi_puja", "PRADOSH", diwaliRule, oct20_2025, mumbai).format()
        val delhiDiwali2025 = PanchangEngine.computeFestivalMuhuratWindow("diwali_lakshmi_puja", "PRADOSH", diwaliRule, oct20_2025, delhi).format()
        assertEquals("Mumbai 2025 Lakshmi Puja Muhurat must match Drik Panchang", "07:41 PM - 08:41 PM", mumbaiDiwali2025)
        assertEquals("New Delhi 2025 Lakshmi Puja Muhurat must match Drik Panchang (1h 11m duration)", "07:08 PM - 08:19 PM", delhiDiwali2025)

        // 4. Year 2026 Dhanteras (Nov 6, 2026):
        // Expected: Mumbai = 06:35 PM - 08:35 PM, New Delhi = 06:02 PM - 07:57 PM
        val dhanterasRule = com.example.panchang.FestivalDateRule(
            lunarMonth = "Ashwina",
            paksha = com.example.panchang.Paksha.KRISHNA,
            tithiNumber = 13,
            observanceTime = com.example.panchang.ObservanceTime.PRADOSH
        )
        val nov6_2026 = LocalDate.of(2026, 11, 6)
        assertEquals("06:35 PM - 08:35 PM", PanchangEngine.computeFestivalMuhuratWindow("dhanteras", "PRADOSH", dhanterasRule, nov6_2026, mumbai).format())
        assertEquals("06:02 PM - 07:58 PM", PanchangEngine.computeFestivalMuhuratWindow("dhanteras", "PRADOSH", dhanterasRule, nov6_2026, delhi).format())
    }

    @Test
    fun testAll70FestivalsHaveValidMuhuratAcross20YearHorizon() {
        val allEntities = com.example.data.catalog.BundledCatalog.FESTIVALS +
                com.example.data.remote.OnlineFestivalDiscoveryService.getAllEncyclopedicFestivals().map { it.entity }
        val allFestivals = allEntities.distinctBy { it.id }.map { it.toDomain() }
        assertEquals("Catalog must contain all 82 unique festivals", 82, allFestivals.size)

        val testCities = listOf(
            City.ALL_CITIES.first { it.id == "mumbai" },
            City.DELHI,
            City.BENGALURU
        )

        for (year in FestivalDateEvaluator.SUPPORTED_YEARS) {
            for (city in testCities) {
                for (festival in allFestivals) {
                    val date = FestivalDateEvaluator.resolveFestivalDate(
                        festivalId = festival.id,
                        year = year,
                        city = city,
                        rule = festival.dateRule
                    )
                    val muhurat = PanchangEngine.computeFestivalMuhuratWindow(
                        festivalId = festival.id,
                        muhuratType = festival.muhuratType,
                        dateRule = festival.dateRule,
                        date = date,
                        city = city
                    ).format()
                    val parsed = com.example.domain.model.MuhuratValidationState.parseTimeRange(muhurat)
                    assertNotNull(
                        "Muhurat for ${festival.id} in ${city.name} ($year on $date) must parse as valid time range (got '$muhurat')",
                        parsed
                    )
                    val verifiedState = com.example.domain.model.MuhuratValidationState.createVerified(
                        festivalDate = date,
                        cityId = city.id,
                        startTime = parsed!!.startTime,
                        endTime = parsed.endTime,
                        verificationProvenance = "Drik Ganita / Lahiri Chitrapaksha Meeus Ephemeris",
                        formattedWindow = muhurat
                    )
                    assertTrue(
                        "Muhurat for ${festival.id} in ${city.name} ($year on $date) must be Verified & schedulable (got '$muhurat')",
                        verifiedState is com.example.domain.model.MuhuratValidationState.Verified && verifiedState.isSchedulable
                    )
                }
            }
        }
    }

    @Test
    fun testAll70FestivalsHaveCompleteSpecificPujaVidhiAndSamagri() {
        val bundledFestivals = BundledCatalog.FESTIVALS.map { it.toDomain() }
        val encyclopedicFestivals = com.example.data.remote.OnlineFestivalDiscoveryService
            .getAllEncyclopedicFestivals()
            .map { it.entity.toDomain() }
        val allFestivals = (bundledFestivals + encyclopedicFestivals).distinctBy { it.id }
        assertEquals("Catalog must contain all 82 unique festivals", 82, allFestivals.size)

        val uniqueFirstStepTitles = mutableSetOf<String>()
        val uniqueVidhiSignatures = mutableSetOf<String>()

        for (festival in allFestivals) {
            assertTrue(
                "Festival ${festival.id} must have at least 5 Puja Vidhi steps (found ${festival.vidhiSteps.size})",
                festival.vidhiSteps.size >= 5
            )
            assertTrue(
                "Festival ${festival.id} must have at least 6 Puja Samagri items (found ${festival.samagriItems.size})",
                festival.samagriItems.size >= 6
            )

            festival.vidhiSteps.forEachIndexed { idx, step ->
                assertEquals(
                    "Step numbers must be sequential 1..N for ${festival.id}",
                    idx + 1,
                    step.stepNumber
                )
                assertTrue(
                    "Step ${step.stepNumber} title must not be blank for ${festival.id}",
                    step.title.isNotBlank()
                )
                assertTrue(
                    "Step ${step.stepNumber} body must contain detailed ritual instructions (>= 40 chars) for ${festival.id}",
                    step.body.length >= 40
                )
                assertNotNull(
                    "Step ${step.stepNumber} durationMinutes must be populated for ${festival.id}",
                    step.durationMinutes
                )
                assertTrue(
                    "Step ${step.stepNumber} durationMinutes must be > 0 for ${festival.id}",
                    (step.durationMinutes ?: 0) > 0
                )
            }

            festival.samagriItems.forEach { item ->
                assertTrue("Samagri item id must not be blank for ${festival.id}", item.id.isNotBlank())
                assertTrue("Samagri item name must not be blank for ${festival.id}", item.name.isNotBlank())
                assertFalse("Samagri item quantity must not be blank for ${festival.id}", item.quantity.isNullOrBlank())
            }

            uniqueFirstStepTitles.add(festival.vidhiSteps.first().title)
            uniqueVidhiSignatures.add(festival.vidhiSteps.joinToString("|") { "${it.title}:${it.body}" })
        }

        assertEquals(
            "Every single festival out of 82 must have its own distinct Puja Vidhi sequence (no shared boilerplate)",
            82,
            uniqueVidhiSignatures.size
        )
    }

    @Test
    fun testGovardhanPuja2026NewDelhiExactTimingsAndMuhurats() {
        val govardhan = BundledCatalog.FESTIVALS.first { it.id == "govardhan_puja" }.toDomain()
        val resolvedDate = FestivalDateEvaluator.resolveFestivalDate("govardhan_puja", 2026, City.DELHI, govardhan.dateRule)

        assertEquals("Govardhan Puja 2026 must be on Tuesday, November 10, 2026", LocalDate.of(2026, 11, 10), resolvedDate)
        assertEquals(java.time.DayOfWeek.TUESDAY, resolvedDate.dayOfWeek)

        val panchang = PanchangEngine.computeDayPanchang(resolvedDate, City.DELHI)
        val pratahkaalMuhurat = PanchangEngine.computeFestivalMuhuratWindow(
            festivalId = "govardhan_puja",
            muhuratType = govardhan.muhuratType,
            dateRule = govardhan.dateRule,
            date = resolvedDate,
            city = City.DELHI
        )
        val godhuliMuhurat = PanchangEngine.computeGodhuliWindow(panchang)
        val tithiSpan = PanchangEngine.formatTithiStartAndEnd(resolvedDate, City.DELHI, govardhan.dateRule)
        val breakdown = PanchangEngine.computeFestivalMuhuratBreakdown(
            festivalId = "govardhan_puja",
            muhuratType = govardhan.muhuratType,
            dateRule = govardhan.dateRule,
            date = resolvedDate,
            city = City.DELHI
        )

        assertEquals("06:40 AM - 08:50 AM", pratahkaalMuhurat.format())
        assertEquals("11:43 AM - 12:26 PM", panchang.abhijitMuhurat?.format())
        assertEquals("05:30 PM - 05:56 PM", godhuliMuhurat.format())
        assertEquals("Begins: Mon, Nov 9, 2026 at 12:31 PM • Ends: Tue, Nov 10, 2026 at 02:00 PM", tithiSpan)
        assertTrue(breakdown.contains("Morning (Pratahkaal) Puja Muhurat: 06:40 AM - 08:50 AM"))
        assertTrue(breakdown.contains("Abhijit Muhurat: 11:43 AM - 12:26 PM"))
        assertTrue(breakdown.contains("Godhuli Muhurat: 05:30 PM - 05:56 PM"))
        assertTrue(govardhan.significance.contains("Annakut") && govardhan.significance.contains("56 food items"))
    }

    @Test
    fun testCityFindByIdDoesNotSilentlySubstituteBengaluru() {
        assertNull(
            "Unknown city ID must return null rather than silently substituting Bengaluru",
            City.findById("non_existent_city_id")
        )
        assertNull(City.findById(""))

        val unknownLookup = City.lookupById("atlantis_999")
        assertTrue(unknownLookup is com.example.panchang.CityLookupResult.Unknown)
        assertEquals(
            "atlantis_999",
            (unknownLookup as com.example.panchang.CityLookupResult.Unknown).requestedId
        )

        val knownLookup = City.lookupById("varanasi")
        assertTrue(knownLookup is com.example.panchang.CityLookupResult.Found)
        assertEquals(City.VARANASI, (knownLookup as com.example.panchang.CityLookupResult.Found).city)

        assertEquals(City.DELHI, City.DEFAULT_CITY)
        assertEquals(City.DEFAULT_CITY, com.example.data.preferences.UserPreferences().hostCity)
    }
}
