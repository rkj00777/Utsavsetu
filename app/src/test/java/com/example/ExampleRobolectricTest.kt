package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.repository.toDomain
import com.example.platform.audio.MantraAudioEngine
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        composeTestRule.waitForIdle()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UtsavSetu", appName)
    }

    private fun dismissSplashAndWelcome() {
        composeTestRule.waitForIdle()
        val splashNodes = composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("splash_screen")).fetchSemanticsNodes()
        if (splashNodes.isNotEmpty()) {
            composeTestRule.onNodeWithTag("splash_screen").performClick()
            composeTestRule.waitForIdle()
        }
        val exploreNodes = composeTestRule.onAllNodes(androidx.compose.ui.test.hasTestTag("welcome_explore_button")).fetchSemanticsNodes()
        if (exploreNodes.isNotEmpty()) {
            composeTestRule.onNodeWithTag("welcome_explore_button").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `verify brand identity resources and adaptive icon configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val masterLogoResId = context.resources.getIdentifier("ic_utsavsetu_master_logo", "drawable", context.packageName)
        assertTrue("Master logo drawable must exist", masterLogoResId != 0)
        val launcherFgResId = context.resources.getIdentifier("ic_utsavsetu_launcher_fg", "drawable", context.packageName)
        assertTrue("Launcher foreground drawable must exist", launcherFgResId != 0)
        val monochromeResId = context.resources.getIdentifier("ic_launcher_monochrome", "drawable", context.packageName)
        assertTrue("Monochrome launcher icon must exist", monochromeResId != 0)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w320dp-h640dp")
    fun `compact mobile 320x640 renders welcome and navigates all 5 tabs cleanly`() {
        dismissSplashAndWelcome()
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_calendar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_festivals").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("festivals_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_panchang").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("panchang_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w600dp-h960dp")
    fun `medium tablet portrait 600x960 renders calendar grid and festival search cleanly`() {
        dismissSplashAndWelcome()
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_calendar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_month_grid_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_festivals").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("festivals_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("festival_search_input").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w1024dp-h768dp-land")
    fun `expanded tablet landscape 1024x768 renders all screens and festival detail cleanly`() {
        dismissSplashAndWelcome()
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("today_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_calendar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_month_grid_card").assertIsDisplayed()

        // Verify adaptive MonthGrid height on 1024x768 landscape tablet is compact and fits within viewport
        val screenBounds = composeTestRule.onNodeWithTag("calendar_screen").fetchSemanticsNode().boundsInRoot
        val gridBounds = composeTestRule.onNodeWithTag("calendar_month_grid_card").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Tablet calendar grid height (${gridBounds.height}) must remain compact (< 75% of screen height ${screenBounds.height}) on landscape tablet",
            gridBounds.height < screenBounds.height * 0.75f
        )
        assertTrue(
            "Tablet calendar content width (${screenBounds.width}) must be centered and constrained on 1024dp wide screen",
            screenBounds.width <= 900f
        )
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h740dp")
    fun `audit all 8 languages and dropdowns for compact height and zero tall empty boxes`() {
        // 1. Audit all 8 languages in UtsavLocalizationEngine & FestivalPujaMantraCatalog
        val allFestivals = com.example.data.catalog.BundledCatalog.FESTIVALS.map { it.toDomain() }
        for (lang in com.example.data.preferences.AppLanguage.values()) {
            val strings = com.example.ui.theme.stringsForLanguage(lang)
            assertTrue("Language $lang must have non-blank day words", strings.dayWordSingular.isNotBlank() && strings.dayWordPlural.isNotBlank())
            assertTrue("Language $lang must have non-blank step and reset labels", strings.stepWordLabel.isNotBlank() && strings.resetButtonLabel.isNotBlank())
            assertTrue("Language $lang must have localized reminder titles", strings.dayBeforeReminderTitle.isNotBlank() && strings.festivalMorningReminderTitle.isNotBlank() && strings.muhuratStartReminderTitle.isNotBlank())
            assertTrue("Language $lang must have localized reminder descriptions", strings.dayBeforeReminderDesc.isNotBlank() && strings.festivalMorningReminderDesc.isNotBlank() && strings.muhuratStartReminderDesc.isNotBlank())
            for (fest in allFestivals) {
                val baseDetails = com.example.domain.FestivalShastraKnowledge.getDetailsForFestival(fest)
                val localized = com.example.ui.theme.UtsavLocalizationEngine.localizeShastraDetails(fest, baseDetails, lang)
                assertEquals(5, localized.pujaMantras.size)
                for (mantra in localized.pujaMantras) {
                    // Verify ritualStage and chantCountRecommendation are concise badge strings (never 100-char sentences)
                    assertTrue(
                        "Mantra ritualStage '${mantra.ritualStage}' in $lang must be concise (< 60 chars)",
                        mantra.ritualStage.length < 60
                    )
                    assertTrue(
                        "Mantra chantCountRecommendation '${mantra.chantCountRecommendation}' in $lang must be concise (< 45 chars)",
                        mantra.chantCountRecommendation.length < 45
                    )
                }
            }
        }

        // 2. Audit live Compose UI bounds outside and inside dropdowns in English
        dismissSplashAndWelcome()

        // Expand Today's Panchang dropdown and verify its height is compact
        composeTestRule.onNodeWithTag("panchang_strip").performClick()
        composeTestRule.waitForIdle()
        val panchangStripBounds = composeTestRule.onNodeWithTag("panchang_strip").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expanded Today Panchang dropdown height (${panchangStripBounds.height}) must be compact (< 400px / ~150dp)",
            panchangStripBounds.height < 400f
        )

        // Open Panchang tab and verify screen renders cleanly
        composeTestRule.onNodeWithTag("nav_tab_panchang").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("panchang_screen").assertIsDisplayed()

        // Open Profile tab and verify profile_screen renders cleanly
        composeTestRule.onNodeWithTag("nav_tab_profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    }

    @Test
    fun `test mantra audio engine state transitions and japa controls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = com.example.platform.audio.MantraAudioEngine.getInstance(context)

        // Initial state
        engine.stop()
        val initial = engine.playbackState.value
        assertEquals(false, initial.isPlaying)
        assertEquals(0, initial.currentRound)

        // Start playback
        engine.playMantra(
            id = "ganesha_test_1",
            sanskritText = "ॐ गं गणपतये नमः",
            mantraTitle = "Ganesha Dhyana",
            targetRounds = 21
        )
        val playingState = engine.playbackState.value
        assertEquals("ganesha_test_1", playingState.currentMantraId)
        assertEquals("Ganesha Dhyana", playingState.mantraTitle)
        assertEquals(21, playingState.targetRounds)
        assertEquals(true, playingState.isPlaying)

        // Speed adjustment
        engine.setSpeed(1.25f)
        assertEquals(1.25f, engine.playbackState.value.speed)

        // Tanpura & Tabla toggles
        val prevTanpura = engine.playbackState.value.isTanpuraEnabled
        engine.toggleTanpura()
        assertEquals(!prevTanpura, engine.playbackState.value.isTanpuraEnabled)
        engine.toggleTanpura() // restore

        val prevTabla = engine.playbackState.value.isTablaEnabled
        engine.toggleTabla()
        assertEquals(!prevTabla, engine.playbackState.value.isTablaEnabled)
        engine.toggleTabla() // restore

        // Manual round increments
        engine.incrementManualRound()
        assertEquals(1, engine.playbackState.value.currentRound)
        engine.incrementManualRound()
        assertEquals(2, engine.playbackState.value.currentRound)

        // Target rounds adjustment
        engine.setTargetRounds(108)
        assertEquals(108, engine.playbackState.value.targetRounds)

        // Pause and resume
        engine.pause()
        assertEquals(false, engine.playbackState.value.isPlaying)
        engine.resume()
        assertEquals(true, engine.playbackState.value.isPlaying)

        // Stop
        engine.stop()
        val stoppedState = engine.playbackState.value
        assertEquals(false, stoppedState.isPlaying)
        assertEquals(null, stoppedState.currentMantraId)
        assertEquals(0, stoppedState.currentRound)
    }

    @Test
    fun `test pandit voice gender switching and preference persistence`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = MantraAudioEngine.getInstance(context)

        // Default voice gender is MALE
        engine.setVoiceGender("MALE")
        assertEquals("MALE", engine.getVoiceGender())

        // Switch to FEMALE
        engine.setVoiceGender("FEMALE")
        assertEquals("FEMALE", engine.getVoiceGender())

        // Switch back to MALE
        engine.setVoiceGender("MALE")
        assertEquals("MALE", engine.getVoiceGender())

        // Verify formatForPanditChanting strips any accidental Latin tokens and converts ॐ to ओम्
        val formatted = engine.formatForPanditChanting("ॐ गं गणपतये नमः (Rigveda 1.1)", devanagariSupported = true)
        assertTrue("Formatted chant must convert ॐ to ओम्", formatted.contains("ओम्"))
        assertTrue("Formatted chant must strip Latin text", !formatted.contains("Rigveda"))

        // Verify VedicPanditVoiceProcessor DSP produces distinct valid audio for Male and Female Pandit
        val testSignal = ShortArray(4410) { i ->
            (kotlin.math.sin(2.0 * Math.PI * 220.0 * i / 22050.0) * 12000).toInt().toShort()
        }
        val maleProcessed = com.example.platform.audio.VedicPanditVoiceProcessor.processPanditVoice(
            input = testSignal,
            sampleRate = 22050,
            pitchFactor = 0.72f,
            tempoStretch = 1.10f,
            isMalePandit = true
        )
        val femaleProcessed = com.example.platform.audio.VedicPanditVoiceProcessor.processPanditVoice(
            input = testSignal,
            sampleRate = 22050,
            pitchFactor = 1.06f,
            tempoStretch = 1.10f,
            isMalePandit = false
        )
        assertTrue("Male Pandit DSP output must be non-empty with reverb tail", maleProcessed.size > testSignal.size)
        assertTrue("Female Pandit DSP output must be non-empty with reverb tail", femaleProcessed.size > testSignal.size)

        // Test UserPreferencesRepository updates
        val prefsRepo = UserPreferencesRepository(context)
        prefsRepo.updatePanditVoiceGender("FEMALE")
        val updatedPrefs = prefsRepo.userPreferencesFlow.first()
        assertEquals("FEMALE", updatedPrefs.panditVoiceGender)

        prefsRepo.updatePanditVoiceGender("MALE")
        val restoredPrefs = prefsRepo.userPreferencesFlow.first()
        assertEquals("MALE", restoredPrefs.panditVoiceGender)
    }

    @Test
    fun `verify google play store share link and festival deep link extraction`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val festivalId = "diwali_lakshmi_puja"
        val playStoreLink = com.example.platform.share.ShareHelper.buildPlayStoreFestivalLink(context, festivalId)

        assertTrue(
            "Share link must be a Google Play Store URL",
            playStoreLink.startsWith("https://play.google.com/store/apps/details?id=${context.packageName}")
        )
        assertTrue(
            "Share link must contain festival_id parameter",
            playStoreLink.contains("festival_id=$festivalId")
        )
        assertTrue(
            "Share link must contain referrer parameter for Play Store install attribution",
            playStoreLink.contains("referrer=festival_id%3D$festivalId")
        )

        // Verify MainActivity extracts festival_id when receiver already has the app installed
        val playStoreIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(playStoreLink)
        )
        val extractedFromPlayUrl = composeTestRule.activity.extractFestivalId(playStoreIntent)
        assertEquals(festivalId, extractedFromPlayUrl)

        // Verify MainActivity also extracts festival_id from referrer-only Play Store URL
        val referrerOnlyIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}&referrer=festival_id%3Dholi")
        )
        assertEquals("holi", composeTestRule.activity.extractFestivalId(referrerOnlyIntent))
    }

    @Test
    fun `verify runtime security integrity manager and anti-injection sanitization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = com.example.platform.security.SecurityIntegrityManager.auditAppSecurity(context)

        assertTrue("Package identity must match authentic UtsavSetu applicationId", report.isPackageNameAuthentic)
        assertTrue("Signing certificate verification must succeed", report.isSigningCertificatePresent)
        assertEquals(false, report.isHookingDetected)
        assertTrue("Overall security environment must be verified secure", report.isEnvironmentSecure)

        // Verify tapjacking protection is enabled on MainActivity decorView
        assertTrue(
            "MainActivity decorView must filter touches when obscured by overlay malware",
            composeTestRule.activity.window.decorView.filterTouchesWhenObscured
        )

        // Verify strict deep-link & filename sanitization blocks path traversal and injection payloads
        assertEquals(
            "diwali_lakshmi_puja",
            com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId("diwali_lakshmi_puja")
        )
        assertEquals(
            null,
            com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId("../../etc/passwd")
        )
        assertEquals(
            null,
            com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId("<script>alert(1)</script>")
        )
        assertEquals(
            null,
            com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId("diwali; DROP TABLE festivals;--")
        )

        // Verify MainActivity rejects malicious deep-link URIs
        val maliciousIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}&festival_id=../../evil")
        )
        assertEquals(null, composeTestRule.activity.extractFestivalId(maliciousIntent))
    }

    @Test
    fun `test google admob native ad integration and unit id configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify Manifest contains production com.google.android.gms.ads.APPLICATION_ID
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
        val admobAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertTrue("AdMob APPLICATION_ID must be configured in AndroidManifest.xml", !admobAppId.isNullOrBlank())
        assertEquals("ca-app-pub-4148485857290947~5018048959", admobAppId)

        // 2. Verify Google AdMob production and official test native ad unit ID constants
        assertEquals(
            "ca-app-pub-4148485857290947/5042099689",
            com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID
        )
        assertEquals(
            "ca-app-pub-3940256099942544/2247696110",
            com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID
        )

        // 3. Verify release builds ALWAYS resolve production ID and debug builds default to production unless USE_TEST_ADS=true
        assertEquals(
            com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID,
            com.example.platform.ads.resolveNativeAdUnitId(
                requestedAdUnitId = com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID,
                isDebug = false,
                useTestAds = true
            )
        )
        assertEquals(
            com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID,
            com.example.platform.ads.resolveNativeAdUnitId(
                requestedAdUnitId = com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID,
                isDebug = true,
                useTestAds = false
            )
        )
        assertEquals(
            com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID,
            com.example.platform.ads.resolveNativeAdUnitId(
                requestedAdUnitId = com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID,
                isDebug = true,
                useTestAds = true
            )
        )

        // 4. Verify AdMobNativeAdLoader starts IDLE (visibility-gated) and initializes safely
        val effectiveUnitId = com.example.platform.ads.resolveNativeAdUnitId()
        val loader = com.example.platform.ads.AdMobNativeAdLoader(
            context = context,
            adUnitId = effectiveUnitId
        )
        assertEquals(effectiveUnitId, loader.state.adUnitId)
        assertEquals(com.example.platform.ads.NativeAdLoadStatus.IDLE, loader.state.status)
        assertEquals(0, loader.state.loadRequestCount)
        assertEquals(null, loader.state.nativeAd)
        loader.destroy()
    }
}



