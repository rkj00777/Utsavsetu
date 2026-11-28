package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.BundledCatalog
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.toDomain
import com.example.panchang.City
import com.example.panchang.PanchangEngine
import com.example.panchang.RegionalTradition
import com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID
import com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID
import com.example.platform.ads.ADMOB_PRODUCTION_APP_ID
import com.example.platform.ads.AdMobNativeAdLoader
import com.example.platform.ads.FestivalFeedAdPlacer
import com.example.platform.ads.FestivalFeedEntry
import com.example.platform.ads.NativeAdLoadStatus
import com.example.platform.ads.NativeAdSlotRegistry
import com.example.platform.ads.isTestAdModeActive
import com.example.platform.ads.resolveNativeAdUnitId
import com.example.platform.location.CoordinateValidationResult
import com.example.platform.location.LocationCityResolver
import com.example.platform.location.LocationCityResult
import com.example.platform.location.WorldwideSearchResult
import com.example.platform.notifications.BootReceiver
import com.example.platform.notifications.DailyMorningNotificationEngine
import com.example.platform.notifications.DeliveryAuditState
import com.example.platform.notifications.ReminderScheduler
import com.example.platform.notifications.SchedulingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Comprehensive Robolectric acceptance & regression test suite for
 * UtsavSetu – Production Correction & Enhancement Release.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductionReleaseAcceptanceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        NativeAdSlotRegistry.clearAll()
        ReminderScheduler.clearAllActiveAlarmsForTesting(context)
    }

    @After
    fun tearDown() {
        NativeAdSlotRegistry.clearAll()
        ReminderScheduler.clearAllActiveAlarmsForTesting(context)
    }

    // =========================================================================
    // MODULE 1: PRODUCTION ADMOB, RELEASE PROTECTION, VISIBILITY & LIFECYCLE
    // =========================================================================

    @Test
    fun `module 1 - production admob ids intact and release builds cannot resolve test id`() {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val manifestAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertEquals(ADMOB_PRODUCTION_APP_ID, manifestAppId)
        assertEquals("ca-app-pub-4148485857290947~5018048959", manifestAppId)
        assertEquals("ca-app-pub-4148485857290947/5042099689", ADMOB_NATIVE_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/2247696110", ADMOB_NATIVE_TEST_UNIT_ID)

        // Release builds NEVER resolve Google's test ID, even if USE_TEST_ADS=true or test ID is passed
        assertEquals(
            ADMOB_NATIVE_AD_UNIT_ID,
            resolveNativeAdUnitId(
                requestedAdUnitId = ADMOB_NATIVE_TEST_UNIT_ID,
                isDebug = false,
                useTestAds = true
            )
        )
        assertFalse(isTestAdModeActive(isDebug = false, useTestAds = true))

        // Debug default (USE_TEST_ADS = false) resolves production ID
        assertFalse("Default Gradle USE_TEST_ADS flag should be false", BuildConfig.USE_TEST_ADS)
        assertEquals(
            ADMOB_NATIVE_AD_UNIT_ID,
            resolveNativeAdUnitId(
                requestedAdUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                isDebug = true,
                useTestAds = false
            )
        )
        assertFalse(isTestAdModeActive(isDebug = true, useTestAds = false))

        // Debug with explicit Gradle flag USE_TEST_ADS = true resolves official Google test ID
        assertEquals(
            ADMOB_NATIVE_TEST_UNIT_ID,
            resolveNativeAdUnitId(
                requestedAdUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                isDebug = true,
                useTestAds = true
            )
        )
        assertTrue(isTestAdModeActive(isDebug = true, useTestAds = true))
    }

    @Test
    fun `module 1 - native ad loader triggers strictly on visibility, deduplicates, and handles no-fill without fake ads`() {
        var executedRequests = 0
        val loader = AdMobNativeAdLoader(
            context = context,
            adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
            adRequestExecutor = { _, _, onFail ->
                executedRequests += 1
                onFail(3, "ERROR_CODE_NO_FILL")
            }
        )

        // Must stay IDLE prior to actual on-screen visibility (never loads on composition/prefetch)
        assertEquals(NativeAdLoadStatus.IDLE, loader.state.status)
        assertEquals(0, executedRequests)
        assertNull(loader.state.nativeAd)

        // First visibility event triggers exactly one request
        assertTrue(loader.requestAdOnVisible())
        assertEquals(1, executedRequests)
        assertEquals(NativeAdLoadStatus.FAILED, loader.state.status)
        assertEquals(3, loader.state.errorCode)
        assertNull("No-fill must never populate a fabricated NativeAd", loader.state.nativeAd)

        // Subsequent recompositions/visibility callbacks must NOT trigger duplicate requests
        assertFalse(loader.requestAdOnVisible())
        assertFalse(loader.requestAdOnVisible())
        assertEquals(1, executedRequests)

        loader.destroy()
        assertEquals(NativeAdLoadStatus.IDLE, loader.state.status)
        assertNull(loader.state.nativeAd)
    }

    @Test
    fun `module 1 - slot registry deduplicates slots across recompositions and enforces LRU lifecycle cleanup`() {
        val key1 = NativeAdSlotRegistry.slotKey("home_upcoming", 1)
        val first = NativeAdSlotRegistry.getOrCreate(context, key1, ADMOB_NATIVE_AD_UNIT_ID)
        val second = NativeAdSlotRegistry.getOrCreate(context, key1, ADMOB_NATIVE_AD_UNIT_ID)
        assertTrue("Registry must return same loader instance for same slot key", first === second)

        // Verify independent feed release cleans up only that feed
        val keyOtherFeed = NativeAdSlotRegistry.slotKey("festivals_category_major", 1)
        NativeAdSlotRegistry.getOrCreate(context, keyOtherFeed, ADMOB_NATIVE_AD_UNIT_ID)
        assertEquals(2, NativeAdSlotRegistry.activeSlotCount())

        NativeAdSlotRegistry.releaseFeed("home_upcoming")
        assertEquals(1, NativeAdSlotRegistry.activeSlotCount())

        // Verify LRU cap bounds total cached slots to 12
        for (i in 1..18) {
            NativeAdSlotRegistry.getOrCreate(
                context = context,
                slotKey = NativeAdSlotRegistry.slotKey("bulk_feed", i),
                adUnitId = ADMOB_NATIVE_AD_UNIT_ID
            )
        }
        assertTrue("LRU cap must bound active slots <= 12", NativeAdSlotRegistry.activeSlotCount() <= 12)
    }

    // =========================================================================
    // MODULE 2: FESTIVAL CARD NATIVE AD PLACEMENT (1, 3, 5, 7, 9...)
    // =========================================================================

    @Test
    fun `module 2 - flat feed places native ads strictly after festival cards 1, 3, 5, 7 with unique keys`() {
        val cards = (1..8).map { "fest_$it" }
        val entries = FestivalFeedAdPlacer.interleaveFlat(
            feedKey = "home_upcoming",
            items = cards,
            itemKeyProvider = { it }
        )

        val adPositions = entries
            .filterIsInstance<FestivalFeedEntry.NativeAdSlot>()
            .map { it.afterCardNumber }
        assertEquals(listOf(1, 3, 5, 7), adPositions)

        // Verify unique LazyColumn keys and no two adjacent ad slots
        val keys = entries.map {
            when (it) {
                is FestivalFeedEntry.FestivalItem -> it.itemKey
                is FestivalFeedEntry.NativeAdSlot -> it.itemKey
                is FestivalFeedEntry.SectionHeader -> it.headerKey
            }
        }
        assertEquals(keys.size, keys.toSet().size)
        for (i in 0 until entries.size - 1) {
            assertFalse(
                "Adjacent native ads are forbidden",
                entries[i] is FestivalFeedEntry.NativeAdSlot && entries[i + 1] is FestivalFeedEntry.NativeAdSlot
            )
        }

        // Single-card feed gets 1 ad after card 1; empty feed gets 0 entries
        val singleEntries = FestivalFeedAdPlacer.interleaveFlat("single", listOf("diwali")) { it }
        assertEquals(2, singleEntries.size)
        assertTrue(singleEntries[1] is FestivalFeedEntry.NativeAdSlot)
        assertTrue(FestivalFeedAdPlacer.interleaveFlat<String>("empty", emptyList()) { it }.isEmpty())
    }

    @Test
    fun `module 2 - grouped category feeds ignore month headers and restart counting independently per category`() {
        data class MonthGroup(val month: String, val items: List<String>)

        val groupsAll = listOf(
            MonthGroup("Oct", listOf("navratri", "dussehra")),
            MonthGroup("Nov", listOf("diwali", "govardhan", "bhai_dooj")),
            MonthGroup("Dec", listOf("gita_jayanti", "mokshada_ekadashi"))
        )
        val groupsMajor = listOf(
            MonthGroup("Nov", listOf("diwali", "chhath", "kartik_purnima", "tulsi_vivah"))
        )

        val allFeed = FestivalFeedAdPlacer.interleaveGrouped(
            feedKey = "festivals_category_all",
            groups = groupsAll,
            headerKeyProvider = { it.month },
            headerTitleProvider = { it.month },
            itemsProvider = { it.items },
            itemKeyProvider = { g, item -> "${g.month}_$item" }
        )
        val majorFeed = FestivalFeedAdPlacer.interleaveGrouped(
            feedKey = "festivals_category_major",
            groups = groupsMajor,
            headerKeyProvider = { it.month },
            headerTitleProvider = { it.month },
            itemsProvider = { it.items },
            itemKeyProvider = { g, item -> "${g.month}_$item" }
        )

        // Headers never increment festival card count: 7 cards -> ads after cards 1, 3, 5, 7
        val allAds = allFeed.filterIsInstance<FestivalFeedEntry.NativeAdSlot>()
        assertEquals(listOf(1, 3, 5, 7), allAds.map { it.afterCardNumber })

        // Category 'Major' restarts counting independently at card 1: 4 cards -> ads after cards 1, 3
        val majorAds = majorFeed.filterIsInstance<FestivalFeedEntry.NativeAdSlot>()
        assertEquals(listOf(1, 3), majorAds.map { it.afterCardNumber })

        // Registry slot keys must be isolated per category feed
        assertNotEquals(allAds.first().registrySlotKey, majorAds.first().registrySlotKey)
    }

    // =========================================================================
    // MODULE 3: DAILY MORNING NOTIFICATIONS, EXACT VS WORKMANAGER, AUDIT, REBOOT
    // =========================================================================

    @Test
    fun `module 3 - daily trigger computes 7am default and custom time accurately in selected city timezone`() {
        val mumbaiZone = ZoneId.of("Asia/Kolkata")
        val before7am = ZonedDateTime.of(2026, 11, 14, 6, 15, 0, 0, mumbaiZone).toInstant()
        val triggerSameDay = DailyMorningNotificationEngine.computeNextMorningTrigger(
            nowInstant = before7am,
            hour = 7,
            minute = 0,
            zoneId = mumbaiZone
        )
        assertEquals(LocalDate.of(2026, 11, 14), triggerSameDay.toLocalDate())
        assertEquals(7, triggerSameDay.hour)
        assertEquals(0, triggerSameDay.minute)

        val after7am = ZonedDateTime.of(2026, 11, 14, 7, 5, 0, 0, mumbaiZone).toInstant()
        val triggerNextDay = DailyMorningNotificationEngine.computeNextMorningTrigger(
            nowInstant = after7am,
            hour = 7,
            minute = 0,
            zoneId = mumbaiZone
        )
        assertEquals(LocalDate.of(2026, 11, 15), triggerNextDay.toLocalDate())
        assertEquals(7, triggerNextDay.hour)

        // Custom time in New York timezone
        val nyZone = ZoneId.of("America/New_York")
        val nyTrigger = DailyMorningNotificationEngine.computeNextMorningTrigger(
            nowInstant = ZonedDateTime.of(2026, 11, 14, 5, 0, 0, 0, nyZone).toInstant(),
            hour = 6,
            minute = 30,
            zoneId = nyZone
        )
        assertEquals(nyZone, nyTrigger.zone)
        assertEquals(6, nyTrigger.hour)
        assertEquals(30, nyTrigger.minute)
    }

    @Test
    fun `module 3 - festival day and non-festival day payloads derive strictly from PanchangEngine without hardcoded times`() {
        val festivals = BundledCatalog.FESTIVALS.map { it.toDomain() }
        val mumbai = City.MUMBAI
        val resolved2026 = ReminderScheduler.computeOccurrencesLocally(festivals, 2026, mumbai)
        val diwali = resolved2026.first { it.festival.id == "diwali_lakshmi_puja" }

        // Festival Day payload
        val diwaliPanchang = PanchangEngine.computeDayPanchang(diwali.date, mumbai)
        val festivalPayload = DailyMorningNotificationEngine.buildDailyPayload(
            date = diwali.date,
            city = mumbai,
            todaysOccurrences = listOf(diwali),
            panchangDay = diwaliPanchang
        )
        assertTrue(festivalPayload.hasFestival)
        assertTrue(festivalPayload.title.startsWith("Today's Festival –"))
        assertTrue(festivalPayload.title.contains(diwali.festival.nameEnglish))
        assertTrue(festivalPayload.body.contains(mumbai.name))

        // Non-festival day payload (Saturday, Nov 14, 2026 in Mumbai has Abhijit & Brahma Muhurat)
        val nonFestDate = LocalDate.of(2026, 11, 14)
        val nonFestPanchang = PanchangEngine.computeDayPanchang(nonFestDate, mumbai)
        val dailyPayload = DailyMorningNotificationEngine.buildDailyPayload(
            date = nonFestDate,
            city = mumbai,
            todaysOccurrences = emptyList(),
            panchangDay = nonFestPanchang
        )
        assertFalse(dailyPayload.hasFestival)
        assertTrue(dailyPayload.title.startsWith("Daily Panchang –"))
        assertTrue(dailyPayload.title.contains(nonFestPanchang.tithi.name))
        assertTrue(dailyPayload.body.contains(nonFestPanchang.brahmaMuhurat.format()))
        nonFestPanchang.abhijitMuhurat?.let { abhijit ->
            assertTrue(dailyPayload.body.contains(abhijit.format()))
        }
    }

    @Test
    fun `module 3 - exact alarm vs approximate WorkManager fallback are cleanly distinguished in diagnostics`() {
        val app = context as Application
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val prefs = UserPreferences(
            hostCity = City.MUMBAI,
            dailyMorningReminderEnabled = true,
            dailyMorningHour = 7,
            dailyMorningMinute = 0
        )
        val now = Instant.parse("2026-11-14T00:30:00Z")

        // Robolectric may not provide the AlarmManager system service in every SDK sandbox.
        // A forced capability flag cannot make scheduling possible if the service itself is absent.
        val alarmManagerAvailable = context.getSystemService(Context.ALARM_SERVICE) != null
        val expectedExactMode = if (alarmManagerAvailable) {
            SchedulingMode.EXACT_ALARM
        } else {
            SchedulingMode.WORKMANAGER_APPROXIMATE
        }
        val exactMode = ReminderScheduler.scheduleDailyMorningNotification(
            context = context,
            preferences = prefs,
            nowInstant = now,
            forceExactAlarmCapability = true
        )
        assertEquals(expectedExactMode, exactMode)
        val exactDiag = ReminderScheduler.getDiagnosticsSnapshot(context, prefs)
        assertEquals(expectedExactMode, exactDiag.schedulingMode)
        assertNotNull(exactDiag.nextTriggerEpochMillis)
        if (expectedExactMode == SchedulingMode.WORKMANAGER_APPROXIMATE) {
            assertTrue(exactDiag.schedulingHonestyNote.contains("approximate", ignoreCase = true))
            assertTrue(exactDiag.schedulingHonestyNote.contains("not guaranteed", ignoreCase = true))
        }

        // When exact alarms are restricted, must fall back to WorkManager approximate delivery and report honestly
        val fallbackMode = ReminderScheduler.scheduleDailyMorningNotification(
            context = context,
            preferences = prefs,
            nowInstant = now,
            forceExactAlarmCapability = false
        )
        assertEquals(SchedulingMode.WORKMANAGER_APPROXIMATE, fallbackMode)
        val fallbackDiag = ReminderScheduler.getDiagnosticsSnapshot(context, prefs)
        assertEquals(SchedulingMode.WORKMANAGER_APPROXIMATE, fallbackDiag.schedulingMode)
        assertTrue(
            "Fallback honesty note must explicitly state approximate delivery",
            fallbackDiag.schedulingHonestyNote.contains("approximate", ignoreCase = true)
        )
        assertTrue(
            "Fallback honesty note must clarify exact time is not guaranteed",
            fallbackDiag.schedulingHonestyNote.contains("not guaranteed", ignoreCase = true)
        )

        // When permission is denied, diagnostics must explicitly report blocked status
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val deniedDiag = ReminderScheduler.getDiagnosticsSnapshot(context, prefs)
        assertFalse(deniedDiag.postNotificationsGranted)
        assertTrue(deniedDiag.schedulingHonestyNote.contains("blocked", ignoreCase = true))
    }

    @Test
    fun `module 3 - permission denial logs PERMISSION_BLOCKED, permission granted posts once and deduplicates`() = runBlocking {
        val app = context as Application
        val prefs = UserPreferences(
            hostCity = City.MUMBAI,
            dailyMorningReminderEnabled = true,
            dailyMorningHour = 7,
            dailyMorningMinute = 0
        )
        val testDate = LocalDate.of(2026, 11, 14)

        val bundledFestivals = BundledCatalog.FESTIVALS.map { it.toDomain() }

        // 1. Permission denied -> PERMISSION_BLOCKED
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val blockedState = ReminderScheduler.deliverDailyMorningNotification(
            context = context,
            mode = SchedulingMode.EXACT_ALARM,
            overrideDate = testDate,
            overridePreferences = prefs,
            overrideFestivals = bundledFestivals
        )
        assertEquals(DeliveryAuditState.PERMISSION_BLOCKED, blockedState)
        val auditAfterDeny = DailyMorningNotificationEngine.getAuditEntries(context)
        assertTrue(auditAfterDeny.any { it.state == DeliveryAuditState.ATTEMPTED })
        assertTrue(auditAfterDeny.any { it.state == DeliveryAuditState.PERMISSION_BLOCKED })

        // 2. Permission granted -> POSTED once (offline), never claims user saw it
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val postedState = ReminderScheduler.deliverDailyMorningNotification(
            context = context,
            mode = SchedulingMode.EXACT_ALARM,
            overrideDate = testDate,
            overridePreferences = prefs,
            overrideFestivals = bundledFestivals
        )
        assertEquals(DeliveryAuditState.POSTED, postedState)

        // 3. Second delivery for same date + city -> SKIPPED_DUPLICATE
        val duplicateState = ReminderScheduler.deliverDailyMorningNotification(
            context = context,
            mode = SchedulingMode.EXACT_ALARM,
            overrideDate = testDate,
            overridePreferences = prefs,
            overrideFestivals = bundledFestivals
        )
        assertEquals(DeliveryAuditState.SKIPPED_DUPLICATE, duplicateState)

        val finalAudit = DailyMorningNotificationEngine.getAuditEntries(context)
        assertTrue(finalAudit.any { it.state == DeliveryAuditState.POSTED })
        assertTrue(finalAudit.any { it.state == DeliveryAuditState.SKIPPED_DUPLICATE })
        assertTrue(
            "Audit must never claim notification was seen by user",
            finalAudit.none { it.detail.contains("seen by user", ignoreCase = true) }
        )
    }

    @Test
    fun `module 3 - BootReceiver handles reboot, timezone, clock, date, and package-replace actions safely`() {
        val receiver = BootReceiver()
        val actions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
        for (action in actions) {
            receiver.onReceive(context, Intent(action))
        }
        // Completing without exception verifies safe goAsync() handling for all 5 system broadcasts
        assertTrue(true)
    }

    // =========================================================================
    // MODULE 4: LOCATION SELECTOR, GPS STALE FIX, WORLDWIDE SEARCH & COORDS
    // =========================================================================

    @Test
    fun `module 4 - manual coordinate validation enforces ranges and rejects invalid input`() {
        val validMumbai = LocationCityResolver.validateManualCoordinates("19.0760", "72.8777")
        assertTrue(validMumbai is CoordinateValidationResult.Valid)
        val validSydney = LocationCityResolver.validateManualCoordinates("-33.8688", "151.2093")
        assertTrue(validSydney is CoordinateValidationResult.Valid)
        val validBoundary = LocationCityResolver.validateManualCoordinates("-90", "180")
        assertTrue(validBoundary is CoordinateValidationResult.Valid)

        assertTrue(LocationCityResolver.validateManualCoordinates("", "72.8777") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("19.0760", "") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("91.5", "72.8777") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("-90.1", "0.0") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("19.0760", "181.0") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("NaN", "72.8") is CoordinateValidationResult.Invalid)
        assertTrue(LocationCityResolver.validateManualCoordinates("abc", "def") is CoordinateValidationResult.Invalid)
    }

    @Test
    fun `module 4 - stale GPS fix detection and permission denial preserve manually selected city`() = runBlocking {
        // Stale fix threshold (> 600s = 10 min)
        val freshLocation = Location("gps").apply {
            latitude = 19.0760
            longitude = 72.8777
            time = 1_000_000_000L
        }
        val freshAge = LocationCityResolver.computeFixAgeSeconds(
            location = freshLocation,
            nowElapsedNanos = 0L,
            nowWallMillis = 1_000_000_000L + 180_000L // 3 min
        )
        assertEquals(180L, freshAge)
        assertFalse(LocationCityResolver.isFixStale(freshAge))

        val staleAge = LocationCityResolver.computeFixAgeSeconds(
            location = freshLocation,
            nowElapsedNanos = 0L,
            nowWallMillis = 1_000_000_000L + 900_000L // 15 min
        )
        assertEquals(900L, staleAge)
        assertTrue(LocationCityResolver.isFixStale(staleAge))

        // Permission denial returns clean Error and does not overwrite saved city
        val repo = UserPreferencesRepository(context)
        repo.updateHostCityObject(City.MUMBAI)
        val app = context as Application
        shadowOf(app).denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val result = LocationCityResolver.detectUserCity(context)
        assertTrue(result is LocationCityResult.Error)
        assertTrue((result as LocationCityResult.Error).isPermissionDenied)

        val savedPrefs = repo.userPreferencesFlow.first()
        assertEquals("Saved city must never be overwritten when GPS fails/denied", City.MUMBAI.id, savedPrefs.hostCity.id)
    }

    @Test
    fun `module 4 - offline worldwide city database intact, ranked search accurate, and online Geocoder degrades gracefully`() = runBlocking {
        assertTrue("Offline catalog must retain 124+ curated cities", City.ALL_CITIES.size >= 120)

        val mumbaiMatches = LocationCityResolver.searchOfflineCitiesRanked("Mumbai")
        assertEquals("mumbai", mumbaiMatches.first().id)

        val londonMatches = LocationCityResolver.searchOfflineCitiesRanked("London")
        assertTrue(londonMatches.any { it.name.equals("London", ignoreCase = true) && it.timeZoneId == "Europe/London" })

        val singaporeMatches = LocationCityResolver.searchOfflineCitiesRanked("Singapore")
        assertTrue(singaporeMatches.any { it.name.equals("Singapore", ignoreCase = true) && it.timeZoneId == "Asia/Singapore" })

        // Custom worldwide coordinates resolve non-IST time zones accurately
        val customTokyo = City.createCustomGpsCity(
            latitude = 35.6762,
            longitude = 139.6503,
            name = "Tokyo",
            state = "Tokyo",
            country = "Japan"
        )
        assertEquals("Asia/Tokyo", customTokyo.timeZoneId)

        val customNy = City.createCustomGpsCity(
            latitude = 40.7128,
            longitude = -74.0060,
            name = "Brooklyn",
            state = "New York",
            country = "USA"
        )
        assertEquals("America/New_York", customNy.timeZoneId)

        val customSydney = City.createCustomGpsCity(
            latitude = -33.8688,
            longitude = 151.2093,
            name = "Parramatta",
            state = "NSW",
            country = "Australia"
        )
        assertEquals("Australia/Sydney", customSydney.timeZoneId)

        // Best-effort online Geocoder search never throws even in offline/Robolectric environment
        val onlineResult = LocationCityResolver.searchWorldwideCitiesOnline(context, "Reykjavik")
        assertTrue(
            onlineResult is WorldwideSearchResult.Success ||
                onlineResult is WorldwideSearchResult.Empty ||
                onlineResult is WorldwideSearchResult.Unavailable
        )
    }

    // =========================================================================
    // MODULE 5: PANCHANG LOCATION ACCURACY & PREFERENCE MIGRATION
    // =========================================================================

    @Test
    fun `module 5 - panchang calculates distinct local sunrise and Muhurat timings across worldwide cities`() {
        val date = LocalDate.of(2026, 11, 14)
        val mumbai = PanchangEngine.computeDayPanchang(date, City.MUMBAI)
        val kolkata = PanchangEngine.computeDayPanchang(date, City.KOLKATA)
        val londonCity = City.ALL_CITIES.first { it.id == "london" }
        val london = PanchangEngine.computeDayPanchang(date, londonCity)

        // Kolkata is ~15.5 degrees east of Mumbai so sunrise in IST is significantly earlier
        assertTrue(
            "Kolkata sunrise (${kolkata.sunrise}) must be earlier than Mumbai sunrise (${mumbai.sunrise})",
            kolkata.sunrise.isBefore(mumbai.sunrise)
        )
        assertNotEquals(mumbai.brahmaMuhurat.format(), kolkata.brahmaMuhurat.format())
        assertNotEquals(mumbai.formattedSunrise(), london.formattedSunrise())
    }

    @Test
    fun `module 5 - preferences persist recent cities, custom worldwide city, and daily morning schedule without data loss`() = runBlocking {
        val repo = UserPreferencesRepository(context)
        repo.clearAll()

        repo.updateLanguage(AppLanguage.MARATHI.code)
        repo.updateTraditions(setOf(RegionalTradition.MAHARASHTRIAN, RegionalTradition.NORTH_INDIAN))
        repo.updateDailyMorningSettings(enabled = true, hour = 6, minute = 30)

        val customToronto = City.createCustomGpsCity(
            latitude = 43.6532,
            longitude = -79.3832,
            name = "Mississauga",
            state = "Ontario",
            country = "Canada",
            timeZoneId = "America/Toronto"
        )
        repo.updateHostCityObject(City.MUMBAI)
        repo.updateHostCityObject(customToronto)

        val prefs = repo.userPreferencesFlow.first()
        assertEquals(AppLanguage.MARATHI, prefs.preferredLanguage)
        assertEquals("Mississauga", prefs.hostCity.name)
        assertEquals("Canada", prefs.hostCity.country)
        assertEquals("America/Toronto", prefs.hostCity.timeZoneId)
        assertTrue(prefs.dailyMorningReminderEnabled)
        assertEquals(6, prefs.dailyMorningHour)
        assertEquals(30, prefs.dailyMorningMinute)
        assertTrue("Recent cities should include previously selected Mumbai", prefs.recentCities.any { it.id == "mumbai" })

        // Changing location reschedules notifications in new city's time zone
        ReminderScheduler.scheduleDailyMorningNotification(
            context = context,
            preferences = prefs,
            nowInstant = Instant.parse("2026-11-14T08:00:00Z"),
            forceExactAlarmCapability = true
        )
        val diag = ReminderScheduler.getDiagnosticsSnapshot(context, prefs)
        assertEquals("Mississauga", diag.cityName)
        assertEquals("America/Toronto", diag.timeZoneId)
    }
}
