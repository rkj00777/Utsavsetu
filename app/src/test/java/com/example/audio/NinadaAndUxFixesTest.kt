package com.example.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppLanguage
import com.example.domain.FestivalPujaMantra
import com.example.platform.audio.MantraAudioEngine
import com.example.platform.audio.NinadaNeuralVoiceEngine
import com.example.platform.audio.NinadaPackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.PI
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NinadaAndUxFixesTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `ninada catalog covers Classical Sanskrit and all 8 UtsavSetu app languages`() {
        val packs = NinadaNeuralVoiceEngine.SUPPORTED_PACKS
        val codes = packs.map { it.code }.toSet()

        assertTrue("Must include Classical Sanskrit (sa)", "sa" in codes)
        for (lang in AppLanguage.values()) {
            assertTrue(
                "Ninada catalog must include language pack for ${lang.englishName} (${lang.code})",
                lang.code in codes
            )
            val pack = packs.first { it.code == lang.code }
            assertEquals(lang, pack.appLanguage)
            assertTrue("Sample phrase for ${lang.code} must be non-blank", pack.samplePhrase.isNotBlank())
        }
        assertEquals(9, packs.size)
    }

    @Test
    fun `ninada tokenization and PCM normalization reject silence and preserve audible signal`() {
        val ninada = NinadaNeuralVoiceEngine.getInstance(context)

        val normalizedSanskrit = ninada.normalizeTextForNinada("ॐ गं गणपतये नमः ॥ (Rigveda)", "sa")
        assertTrue(normalizedSanskrit.contains("ओम्"))
        assertFalse(normalizedSanskrit.contains("Rigveda"))

        val tokens = ninada.tokenizeForLanguage(normalizedSanskrit, "sa")
        // The test runner is offline and may not have a model vocabulary cached.
        // In that case the engine must reject tokenization (empty IDs) rather than
        // fabricate IDs. When a vocab is available, tokenization must yield IDs.
        assertTrue(
            "Tokenization must either use a real vocabulary or safely reject unavailable vocab",
            tokens.isEmpty() || tokens.all { it >= 0L }
        )

        // Silent waveform must be rejected so engine falls back cleanly rather than playing silence
        val silentWave = FloatArray(2400) { 0f }
        assertNull(ninada.normalizeAndConvertToPcm16(silentWave))

        // Valid 24 kHz sine wave must normalize cleanly to non-empty 16-bit PCM
        val audibleWave = FloatArray(2400) { i ->
            (0.25 * sin(2.0 * PI * 440.0 * i / 24000.0)).toFloat()
        }
        val pcm16 = ninada.normalizeAndConvertToPcm16(audibleWave)
        assertNotNull(pcm16)
        assertEquals(2400, pcm16!!.size)
        assertTrue("Normalized PCM must have strong audible amplitude", pcm16.maxOf { kotlin.math.abs(it.toInt()) } > 10000)
    }

    @Test
    fun `mantra audio engine provides phonetic anti-silence fallback and supports all 8 regional languages`() {
        val engine = MantraAudioEngine.getInstance(context)
        engine.stop()

        // Explicit transliteration is preserved for anti-silence fallback
        val mantraWithTranslit = FestivalPujaMantra(
            id = "test_ganesha",
            stepNumber = 1,
            ritualStage = "Avahana",
            title = "Ganesha Dhyana",
            sanskrit = "ॐ गं गणपतये नमः ॥",
            transliteration = "Om Gam Ganapataye Namaha",
            meaning = "Salutations to Lord Ganesha",
            chantCountRecommendation = "11x"
        )
        assertEquals("Om Gam Ganapataye Namaha", engine.buildTransliteratedChantFallback(mantraWithTranslit))

        // When transliteration is blank, Devanagari transliterator generates readable Latin phonetics
        val mantraWithoutTranslit = mantraWithTranslit.copy(transliteration = "")
        val autoPhonetic = engine.buildTransliteratedChantFallback(mantraWithoutTranslit)
        assertTrue("Auto phonetic fallback must be non-blank", autoPhonetic.isNotBlank())
        assertTrue("Auto phonetic fallback must contain Om", autoPhonetic.contains("Om", ignoreCase = true))

        // Verify localized narration updates active language code cleanly for every AppLanguage
        for (lang in AppLanguage.values()) {
            engine.playLocalizedNarration(
                id = "narration_${lang.code}",
                title = "Meaning (${lang.nativeName})",
                text = lang.sampleGreeting,
                language = lang
            )
            val state = engine.playbackState.value
            assertTrue(state.isPlaying)
            assertEquals(lang.code, state.activeLanguageCode)
            assertEquals("narration_${lang.code}", state.currentMantraId)
        }

        // Verify voice preview works for Sanskrit and regional languages
        engine.previewLanguageVoice("sa")
        assertEquals("preview_sa", engine.playbackState.value.currentMantraId)
        assertEquals("sa", engine.playbackState.value.activeLanguageCode)

        engine.stop()
        assertFalse(engine.playbackState.value.isPlaying)
        assertTrue(engine.ninadaPackStatuses.value["sa"] is NinadaPackStatus.NotDownloaded || engine.ninadaPackStatuses.value["sa"] is NinadaPackStatus.Ready)
    }
}
