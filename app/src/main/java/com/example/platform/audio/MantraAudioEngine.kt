package com.example.platform.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.preferences.AppLanguage
import com.example.domain.FestivalPujaMantra
import com.example.platform.monitoring.AppBatteryMonitor
import com.example.platform.monitoring.AppCrashMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Playback state representing active mantra audio recitation, active neural voice engine,
 * and Japa Mala progress.
 */
data class MantraPlaybackState(
    val isPlaying: Boolean = false,
    val isSynthesizing: Boolean = false,
    val currentMantraId: String? = null,
    val currentMantraTitle: String = "",
    val currentMantraSanskrit: String = "",
    val stepNumber: Int = 1,
    val currentRound: Int = 0,
    val targetRounds: Int = 1,
    val speed: Float = 0.85f,
    val isTanpuraEnabled: Boolean = true,
    val isTablaEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val activeLanguageCode: String = "sa",
    val activeVoiceEngineLabel: String = "Indian Neural Voice · Sanskrit",
    val audioStatusMessage: String? = null
) {
    val mantraTitle: String get() = currentMantraTitle
}

/**
 * Production-grade on-device Mantra Audio Recitation & Multi-Language Natural Neural Voice Engine.
 *
 * 3-Tier Audible Pipeline (never silent):
 * 1. **Ninada Neural 24 kHz ONNX** (`hemanth/ninada` / `gnumanth/ninada`) for Classical Sanskrit (`sa`)
 *    and all 8 regional app languages (`hi`, `mr`, `gu`, `ta`, `te`, `kn`, `bn`, `en`), streamed via
 *    high-priority `AudioTrack` (`USAGE_MEDIA`, `CONTENT_TYPE_SPEECH`) with AudioFocus.
 * 2. **Device High-Quality Neural TTS** (`com.google.android.tts` with automatic system TTS engine
 *    fallback), ranking `neural2`/`wavenet`/`VERY_HIGH` voices per language & Pandit gender, with
 *    automatic phonetic transliteration fallback whenever Devanagari TTS is missing on the device.
 * 3. **Temple Vadyam Accompaniment**: Soft 4-string Plucked Tanpura (C#4) & Indian Tabla (Keherwa Theka)
 *    mixed cleanly underneath the voice so chanting remains foregrounded and crystal-clear.
 */
class MantraAudioEngine private constructor(private val appContext: Context) : TextToSpeech.OnInitListener {

    private val engineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val ninadaEngine: NinadaNeuralVoiceEngine = NinadaNeuralVoiceEngine.getInstance(appContext)
    val ninadaPackStatuses: StateFlow<Map<String, NinadaPackStatus>> = ninadaEngine.packStatuses

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var triedDefaultTtsFallback = false
    private var isDevanagariVoiceActive = true
    private var isSelectedVoiceNativeMale = false
    private var isSelectedVoiceNativeFemale = false
    private var lastConfiguredVoiceLabel: String = "Indian Neural Voice"

    private val _playbackState = MutableStateFlow(MantraPlaybackState())
    val playbackState: StateFlow<MantraPlaybackState> = _playbackState.asStateFlow()

    private var activeMantra: FestivalPujaMantra? = null
    private var activePlaybackLangCode: String = "sa"
    private var activeAppLanguage: AppLanguage = AppLanguage.ENGLISH
    private var activeVoiceGender: String = "MALE" // "MALE" (Male Pandit) or "FEMALE" (Female Pandit / विदुषी)

    private val audioManager: AudioManager? by lazy {
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    private var audioFocusRequest: AudioFocusRequest? = null

    // Temple Vadyam (Tanpura + Tabla) AudioTrack generator
    private var instrumentTrack: AudioTrack? = null
    @Volatile
    private var isInstrumentsRunning = false
    private var instrumentJob: Job? = null
    private var previewAutoStopJob: Job? = null

    // Ninada 24 kHz Neural Voice AudioTrack
    private var voiceTrack: AudioTrack? = null
    private var voicePlaybackJob: Job? = null
    // A failed/disconnected native worker is circuit-broken for this app process to prevent repeat SIGILL crashes.
    @Volatile
    private var ninadaWorkerCircuitBroken: Boolean = false
    @Volatile
    private var activePlaybackGeneration: Long = 0L

    // Vibrator for Japa Bead haptic click
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        initializeTtsEngine(preferGoogleEngine = true)
    }

    private fun initializeTtsEngine(preferGoogleEngine: Boolean) {
        try {
            tts?.shutdown()
        } catch (_: Throwable) {}
        tts = null
        isTtsReady = false
        try {
            tts = if (preferGoogleEngine) {
                TextToSpeech(appContext, this, "com.google.android.tts")
            } else {
                triedDefaultTtsFallback = true
                TextToSpeech(appContext, this)
            }
        } catch (_: Throwable) {
            if (preferGoogleEngine && !triedDefaultTtsFallback) {
                triedDefaultTtsFallback = true
                try {
                    tts = TextToSpeech(appContext, this)
                } catch (_: Throwable) {}
            }
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        activeAppLanguage = language
    }

    fun getAppLanguage(): AppLanguage = activeAppLanguage

    /**
     * Set the reciting Pandit voice gender (MALE or FEMALE) and reconfigure TTS voice & pitch.
     */
    fun setVoiceGender(gender: String) {
        val normalized = if (gender.equals("FEMALE", ignoreCase = true)) "FEMALE" else "MALE"
        activeVoiceGender = normalized
        val ttsInstance = tts
        if (ttsInstance != null && isTtsReady) {
            applyVoiceAndPitchConfig(ttsInstance, activePlaybackLangCode)
        }
    }

    fun getVoiceGender(): String = activeVoiceGender

    fun downloadNinadaPack(languageCode: String) {
        engineScope.launch {
            ninadaEngine.downloadLanguagePack(languageCode, activeVoiceGender)
        }
    }

    fun deleteNinadaPack(languageCode: String) {
        engineScope.launch {
            ninadaEngine.deleteLanguagePack(languageCode)
        }
    }

    private fun buildSpeechAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }

    private fun requestSpeechAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(buildSpeechAudioAttributes())
                    .setAcceptsDelayedFocusGain(false)
                    .build()
                audioFocusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Throwable) {}
    }

    private fun abandonSpeechAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Throwable) {}
    }

    private fun candidateLocalesForLanguage(langCode: String): List<String> {
        return when (langCode.lowercase(Locale.ROOT)) {
            "sa" -> listOf("sa-IN", "hi-IN", "mr-IN", "ne-NP", "en-IN")
            "hi" -> listOf("hi-IN", "mr-IN", "sa-IN", "en-IN")
            "mr" -> listOf("mr-IN", "hi-IN", "sa-IN", "en-IN")
            "gu" -> listOf("gu-IN", "hi-IN", "en-IN")
            "ta" -> listOf("ta-IN", "en-IN", "hi-IN")
            "te" -> listOf("te-IN", "en-IN", "hi-IN")
            "kn" -> listOf("kn-IN", "en-IN", "hi-IN")
            "bn" -> listOf("bn-IN", "bn-BD", "hi-IN", "en-IN")
            "en" -> listOf("en-IN", "en-GB", "en-US")
            else -> listOf("sa-IN", "hi-IN", "mr-IN", "en-IN")
        }
    }

    private fun selectLocaleForLanguage(ttsInstance: TextToSpeech, targetLangCode: String): Locale {
        val candidateTags = candidateLocalesForLanguage(targetLangCode)
        for (tag in candidateTags) {
            val loc = Locale.forLanguageTag(tag)
            try {
                val avail = ttsInstance.isLanguageAvailable(loc)
                if (avail >= TextToSpeech.LANG_AVAILABLE) {
                    ttsInstance.language = loc
                    isDevanagariVoiceActive = loc.language in setOf("sa", "hi", "mr", "ne")
                    return loc
                }
            } catch (_: Throwable) {}
        }
        val fallback = Locale.forLanguageTag(candidateTags.first())
        try {
            val res = ttsInstance.setLanguage(fallback)
            isDevanagariVoiceActive = res >= TextToSpeech.LANG_AVAILABLE &&
                fallback.language in setOf("sa", "hi", "mr", "ne")
        } catch (_: Throwable) {
            isDevanagariVoiceActive = false
        }
        return fallback
    }

    private fun isMaleVoiceName(nameLower: String): Boolean {
        return nameLower.contains("#male") ||
            nameLower.contains("_male") ||
            nameLower.contains("-male") ||
            (nameLower.contains("male") && !nameLower.contains("female")) ||
            nameLower.contains("-hie-") || nameLower.endsWith("-hie") ||
            nameLower.contains("-hid-") || nameLower.endsWith("-hid") ||
            nameLower.contains("-hib-") || nameLower.endsWith("-hib") ||
            nameLower.contains("-end-") || nameLower.contains("-ene-") ||
            nameLower.contains("-mrb-") || nameLower.contains("-gub-") ||
            nameLower.contains("-tab-") || nameLower.contains("-tad-") ||
            nameLower.contains("-teb-") || nameLower.contains("-knb-") ||
            nameLower.contains("-bnb-") || nameLower.contains("-bnd-") ||
            nameLower.contains("wavenet-b") || nameLower.contains("wavenet-c") ||
            nameLower.contains("neural2-b") || nameLower.contains("neural2-c") ||
            nameLower.contains("standard-b") || nameLower.contains("standard-c")
    }

    private fun isFemaleVoiceName(nameLower: String): Boolean {
        return nameLower.contains("#female") ||
            nameLower.contains("_female") ||
            nameLower.contains("-female") ||
            nameLower.contains("female") ||
            nameLower.contains("woman") ||
            nameLower.contains("-hia-") || nameLower.endsWith("-hia") ||
            nameLower.contains("-hic-") || nameLower.endsWith("-hic") ||
            nameLower.contains("cfn-local") || nameLower.contains("cfn-network") ||
            nameLower.contains("-ena-") || nameLower.contains("-enc-") ||
            nameLower.contains("-mra-") || nameLower.contains("-gua-") ||
            nameLower.contains("-taa-") || nameLower.contains("-tac-") ||
            nameLower.contains("-tea-") || nameLower.contains("-kna-") ||
            nameLower.contains("-bna-") || nameLower.contains("-bnc-") ||
            nameLower.contains("wavenet-a") || nameLower.contains("wavenet-d") ||
            nameLower.contains("neural2-a") || nameLower.contains("neural2-d") ||
            nameLower.contains("standard-a") || nameLower.contains("standard-d")
    }

    private fun voiceQualityScore(voice: Voice): Int {
        val nameLower = voice.name.lowercase(Locale.ROOT)
        var score = 0
        if (nameLower.contains("neural")) score += 40
        if (nameLower.contains("wavenet")) score += 35
        if (nameLower.contains("studio") || nameLower.contains("journey")) score += 45
        when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> score += 30
            voice.quality >= Voice.QUALITY_HIGH -> score += 20
            voice.quality >= Voice.QUALITY_NORMAL -> score += 10
        }
        // Prefer local voices slightly for zero-latency reliability, unless a very-high quality neural voice exists
        if (!voice.isNetworkConnectionRequired) score += 8
        return score
    }

    private fun applyVoiceAndPitchConfig(ttsInstance: TextToSpeech, targetLangCode: String = "sa") {
        val isFemale = activeVoiceGender.equals("FEMALE", ignoreCase = true)
        isSelectedVoiceNativeMale = false
        isSelectedVoiceNativeFemale = false

        try {
            ttsInstance.setAudioAttributes(buildSpeechAudioAttributes())
        } catch (_: Throwable) {}

        val baseLocale = selectLocaleForLanguage(ttsInstance, targetLangCode)
        val priorityTags = candidateLocalesForLanguage(targetLangCode)
        val devanagariLanguages = setOf("sa", "hi", "mr", "ne")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val allVoices = ttsInstance.voices
                if (!allVoices.isNullOrEmpty()) {
                    val installedVoices = allVoices.filter { voice ->
                        voice.features == null ||
                            !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                    }.ifEmpty { allVoices.toList() }

                    val matchedVoices = installedVoices.filter { voice ->
                        val lang = voice.locale.language.lowercase(Locale.ROOT)
                        val country = voice.locale.country.uppercase(Locale.ROOT)
                        priorityTags.any { tag ->
                            val tagLang = tag.substringBefore("-").lowercase(Locale.ROOT)
                            val tagCountry = tag.substringAfter("-", "").uppercase(Locale.ROOT)
                            lang == tagLang && (tagCountry.isEmpty() || country == tagCountry || tagLang != "en")
                        }
                    }

                    val rankedVoices = matchedVoices.sortedWith(
                        compareBy<Voice> { voice ->
                            val lang = voice.locale.language.lowercase(Locale.ROOT)
                            val country = voice.locale.country.uppercase(Locale.ROOT)
                            val exactIdx = priorityTags.indexOfFirst { tag ->
                                val tagLang = tag.substringBefore("-").lowercase(Locale.ROOT)
                                val tagCountry = tag.substringAfter("-", "").uppercase(Locale.ROOT)
                                lang == tagLang && (tagCountry.isEmpty() || country == tagCountry)
                            }
                            if (exactIdx >= 0) exactIdx else priorityTags.indexOfFirst {
                                it.substringBefore("-").lowercase(Locale.ROOT) == lang
                            }.let { if (it >= 0) it + 10 else 99 }
                        }.thenByDescending { voice ->
                            val n = voice.name.lowercase(Locale.ROOT)
                            val genderMatch = if (isFemale) {
                                isFemaleVoiceName(n) && !isMaleVoiceName(n)
                            } else {
                                isMaleVoiceName(n) && !isFemaleVoiceName(n)
                            }
                            if (genderMatch) 100 else 0
                        }.thenByDescending { voice ->
                            voiceQualityScore(voice)
                        }
                    )

                    val targetVoice = rankedVoices.firstOrNull()
                    if (targetVoice != null) {
                        ttsInstance.voice = targetVoice
                        val chosenName = targetVoice.name.lowercase(Locale.ROOT)
                        isSelectedVoiceNativeMale = isMaleVoiceName(chosenName) && !isFemaleVoiceName(chosenName)
                        isSelectedVoiceNativeFemale = isFemaleVoiceName(chosenName) && !isMaleVoiceName(chosenName)
                        isDevanagariVoiceActive = targetVoice.locale.language.lowercase(Locale.ROOT) in devanagariLanguages
                        val localeLabel = NinadaNeuralVoiceEngine.SUPPORTED_PACKS
                            .firstOrNull { it.code == targetVoice.locale.language.lowercase(Locale.ROOT) }
                            ?.nativeLabel ?: targetVoice.locale.displayLanguage
                        val qualityBadge = if (targetVoice.quality >= Voice.QUALITY_HIGH || chosenName.contains("neural")) {
                            "Device Neural TTS"
                        } else {
                            "Device TTS"
                        }
                        lastConfiguredVoiceLabel = "$qualityBadge · $localeLabel"
                    } else {
                        ttsInstance.language = baseLocale
                        lastConfiguredVoiceLabel = "Device TTS · ${baseLocale.displayLanguage}"
                    }
                }
            } catch (_: Throwable) {}
        }

        val userSpeed = _playbackState.value.speed
        val isChantingSanskrit = targetLangCode.equals("sa", ignoreCase = true)
        if (isFemale) {
            val pitch = if (isSelectedVoiceNativeMale) 1.20f else if (isChantingSanskrit) 1.04f else 1.02f
            ttsInstance.setPitch(pitch)
            ttsInstance.setSpeechRate((userSpeed * (if (isChantingSanskrit) 0.92f else 0.98f)).coerceIn(0.72f, 1.25f))
        } else {
            val pitch = if (isSelectedVoiceNativeMale) 0.88f else 0.78f
            ttsInstance.setPitch(pitch)
            ttsInstance.setSpeechRate((userSpeed * (if (isChantingSanskrit) 0.90f else 0.96f)).coerceIn(0.70f, 1.20f))
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsInstance = tts ?: return
            applyVoiceAndPitchConfig(ttsInstance, activePlaybackLangCode)

            ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    engineScope.launch {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = true,
                            isSynthesizing = false
                        )
                    }
                }

                override fun onDone(utteranceId: String?) {
                    handleChantCompleted()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    handleUtteranceError(utteranceId)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    handleUtteranceError(utteranceId)
                }
            })

            isTtsReady = true
            // If playback was requested while TTS was still initializing, start immediately
            if (_playbackState.value.isPlaying && activeMantra != null && voicePlaybackJob?.isActive != true) {
                speakCurrentRound()
            }
        } else if (!triedDefaultTtsFallback) {
            // Google TTS engine was unavailable or blocked; seamlessly retry with device default TTS!
            initializeTtsEngine(preferGoogleEngine = false)
        } else {
            engineScope.launch {
                _playbackState.value = _playbackState.value.copy(
                    audioStatusMessage = "Device TTS unavailable — download Ninada Neural voice pack for offline chanting."
                )
            }
        }
    }

    private fun handleUtteranceError(utteranceId: String?) {
        engineScope.launch {
            // If a Devanagari utterance errored, retry once using phonetic transliteration in Indian English
            val mantra = activeMantra
            val ttsInstance = tts
            if (mantra != null &&
                ttsInstance != null &&
                utteranceId != null &&
                !utteranceId.endsWith("_fallback") &&
                activePlaybackLangCode == "sa"
            ) {
                val fallbackText = buildTransliteratedChantFallback(mantra)
                if (fallbackText.isNotBlank()) {
                    applyVoiceAndPitchConfig(ttsInstance, "en")
                    val retryId = "${utteranceId}_fallback"
                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, retryId)
                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                        putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                    }
                    _playbackState.value = _playbackState.value.copy(
                        activeVoiceEngineLabel = "Device Voice · Transliterated",
                        audioStatusMessage = "Using phonetic recitation — download Ninada Sanskrit pack for 24 kHz neural chanting."
                    )
                    ttsInstance.speak(fallbackText, TextToSpeech.QUEUE_FLUSH, params, retryId)
                    return@launch
                }
            }
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isSynthesizing = false,
                audioStatusMessage = "Audio output interrupted. Check media volume or download Ninada voice pack."
            )
            stopInstrumentAccompaniment()
            abandonSpeechAudioFocus()
        }
    }

    /**
     * Start chanting a specific festival puja mantra with individual fields.
     */
    fun playMantra(
        id: String,
        sanskritText: String,
        mantraTitle: String,
        targetRounds: Int = _playbackState.value.targetRounds,
        speed: Float = _playbackState.value.speed,
        withTanpura: Boolean = _playbackState.value.isTanpuraEnabled,
        withTabla: Boolean = _playbackState.value.isTablaEnabled,
        transliteration: String = ""
    ) {
        val dummyMantra = FestivalPujaMantra(
            id = id,
            stepNumber = 1,
            ritualStage = "Puja",
            title = mantraTitle,
            sanskrit = sanskritText,
            transliteration = transliteration,
            meaning = "",
            chantCountRecommendation = "${targetRounds}x"
        )
        playMantra(dummyMantra, targetRounds, speed, withTanpura, withTabla)
    }

    /**
     * Start chanting a specific festival puja mantra in Classical Sanskrit (Ninada `sa` first,
     * seamless high-quality neural TTS fallback if not yet cached).
     */
    fun playMantra(
        mantra: FestivalPujaMantra,
        targetRounds: Int = _playbackState.value.targetRounds,
        speed: Float = _playbackState.value.speed,
        withTanpura: Boolean = _playbackState.value.isTanpuraEnabled,
        withTabla: Boolean = _playbackState.value.isTablaEnabled
    ) {
        activePlaybackGeneration++
        previewAutoStopJob?.cancel()
        tts?.stop()
        stopVoiceTrack()
        activeMantra = mantra
        activePlaybackLangCode = "sa"

        val initialEngineLabel = if (ninadaEngine.isPackReady("sa", activeVoiceGender)) {
            "Ninada Neural 24kHz · संस्कृतम्"
        } else {
            lastConfiguredVoiceLabel.ifBlank { "Indian Neural TTS · संस्कृतम्" }
        }

        _playbackState.value = MantraPlaybackState(
            isPlaying = true,
            isSynthesizing = ninadaEngine.isPackReady("sa", activeVoiceGender),
            currentMantraId = mantra.id,
            currentMantraTitle = mantra.title,
            currentMantraSanskrit = mantra.sanskrit,
            stepNumber = mantra.stepNumber,
            currentRound = 0,
            targetRounds = targetRounds.coerceAtLeast(1),
            speed = speed,
            isTanpuraEnabled = withTanpura,
            isTablaEnabled = withTabla,
            isCompleted = false,
            activeLanguageCode = "sa",
            activeVoiceEngineLabel = initialEngineLabel,
            audioStatusMessage = null
        )

        requestSpeechAudioFocus()
        if (withTanpura || withTabla) {
            startInstrumentAccompaniment()
        } else {
            stopInstrumentAccompaniment()
        }

        speakCurrentRound()
    }

    /**
     * Speaks a localized mantra meaning, festival explanation, or greeting naturally in the
     * user's chosen [language] (Hindi, Marathi, Gujarati, Tamil, Telugu, Kannada, Bengali, or English),
     * using that language's Ninada 24 kHz ONNX neural pack when cached or the best matching device
     * neural voice otherwise.
     */
    fun playLocalizedNarration(
        id: String,
        title: String,
        text: String,
        language: AppLanguage = activeAppLanguage
    ) {
        if (text.isBlank()) return
        activePlaybackGeneration++
        previewAutoStopJob?.cancel()
        tts?.stop()
        stopVoiceTrack()
        stopInstrumentAccompaniment()

        activeAppLanguage = language
        activePlaybackLangCode = language.code
        activeMantra = FestivalPujaMantra(
            id = id,
            stepNumber = 1,
            ritualStage = language.nativeName,
            title = title,
            sanskrit = text,
            transliteration = text,
            meaning = text,
            chantCountRecommendation = "1x"
        )

        val engineLabel = if (ninadaEngine.isPackReady(language.code, activeVoiceGender)) {
            "Ninada Neural 24kHz · ${language.nativeName}"
        } else {
            "Device Neural TTS · ${language.nativeName}"
        }

        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            isSynthesizing = ninadaEngine.isPackReady(language.code, activeVoiceGender),
            currentMantraId = id,
            currentMantraTitle = title,
            currentMantraSanskrit = text,
            currentRound = 0,
            targetRounds = 1,
            isCompleted = false,
            activeLanguageCode = language.code,
            activeVoiceEngineLabel = engineLabel,
            audioStatusMessage = null
        )

        requestSpeechAudioFocus()
        speakCurrentRound()
    }

    /**
     * Plays a short natural voice sample for any supported Ninada language pack (`sa`, `hi`, `mr`,
     * `gu`, `ta`, `te`, `kn`, `bn`, `en`) so the user can immediately verify audible playback.
     */
    fun previewLanguageVoice(languageCode: String) {
        val pack = ninadaEngine.getPackByCode(languageCode) ?: return
        if (pack.code == "sa") {
            playMantra(
                id = "preview_sa",
                sanskritText = pack.samplePhrase,
                mantraTitle = pack.displayName,
                targetRounds = 1,
                speed = _playbackState.value.speed,
                withTanpura = false,
                withTabla = false,
                transliteration = "Om Gam Ganapataye Namaha. Shubham Bhavatu."
            )
        } else {
            playLocalizedNarration(
                id = "preview_${pack.code}",
                title = pack.displayName,
                text = pack.samplePhrase,
                language = pack.appLanguage ?: AppLanguage.ENGLISH
            )
        }
    }

    /**
     * Toggle play / pause for the current mantra.
     */
    fun togglePlayPause() {
        val state = _playbackState.value
        if (state.isPlaying) {
            pause()
        } else {
            if (activeMantra != null && !state.isCompleted) {
                resume()
            }
        }
    }

    fun pause() {
        activePlaybackGeneration++
        previewAutoStopJob?.cancel()
        tts?.stop()
        stopVoiceTrack()
        stopInstrumentAccompaniment()
        abandonSpeechAudioFocus()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isSynthesizing = false
        )
    }

    fun resume() {
        if (activeMantra == null) return
        activePlaybackGeneration++
        previewAutoStopJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = true, isCompleted = false)
        requestSpeechAudioFocus()
        if (activePlaybackLangCode == "sa" &&
            (_playbackState.value.isTanpuraEnabled || _playbackState.value.isTablaEnabled)
        ) {
            startInstrumentAccompaniment()
        }
        speakCurrentRound()
    }

    fun stop() {
        activePlaybackGeneration++
        previewAutoStopJob?.cancel()
        tts?.stop()
        stopVoiceTrack()
        stopInstrumentAccompaniment()
        abandonSpeechAudioFocus()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isSynthesizing = false,
            currentMantraId = null,
            currentRound = 0,
            isCompleted = false,
            audioStatusMessage = null
        )
    }

    fun setSpeed(speed: Float) {
        val normalized = speed.coerceIn(0.5f, 2.0f)
        _playbackState.value = _playbackState.value.copy(speed = normalized)
        tts?.let { if (isTtsReady) applyVoiceAndPitchConfig(it, activePlaybackLangCode) }
    }

    fun setTargetRounds(target: Int) {
        _playbackState.value = _playbackState.value.copy(targetRounds = target.coerceAtLeast(1))
    }

    fun toggleTanpura(enabled: Boolean? = null) {
        val newEnabled = enabled ?: !_playbackState.value.isTanpuraEnabled
        _playbackState.value = _playbackState.value.copy(isTanpuraEnabled = newEnabled)
        updateOrPreviewInstruments()
    }

    fun toggleTabla(enabled: Boolean? = null) {
        val newEnabled = enabled ?: !_playbackState.value.isTablaEnabled
        _playbackState.value = _playbackState.value.copy(isTablaEnabled = newEnabled)
        updateOrPreviewInstruments()
    }

    private fun updateOrPreviewInstruments() {
        val state = _playbackState.value
        if (state.isPlaying) {
            if (state.isTanpuraEnabled || state.isTablaEnabled) {
                startInstrumentAccompaniment()
            } else {
                stopInstrumentAccompaniment()
            }
        } else {
            previewAutoStopJob?.cancel()
            if (state.isTanpuraEnabled || state.isTablaEnabled) {
                startInstrumentAccompaniment()
                previewAutoStopJob = engineScope.launch {
                    delay(3000)
                    if (!_playbackState.value.isPlaying) {
                        stopInstrumentAccompaniment()
                    }
                }
            } else {
                stopInstrumentAccompaniment()
            }
        }
    }

    fun incrementManualRound() {
        val next = _playbackState.value.currentRound + 1
        val isDone = next >= _playbackState.value.targetRounds
        _playbackState.value = _playbackState.value.copy(
            currentRound = next,
            isCompleted = isDone
        )
        triggerBeadHaptic()
    }

    fun resetCounter() {
        _playbackState.value = _playbackState.value.copy(
            currentRound = 0,
            isCompleted = false
        )
    }

    private fun speakCurrentRound() {
        val mantra = activeMantra ?: return
        val generation = activePlaybackGeneration
        val langCode = activePlaybackLangCode
        val speed = _playbackState.value.speed

        stopVoiceTrack()

        // Ninada is the primary recitation engine. First-use model acquisition is automatic:
        // download the selected language pack on Play, then synthesize and play without another tap.
        voicePlaybackJob = engineScope.launch {
            try {
                if (ninadaWorkerCircuitBroken) {
                    _playbackState.value = _playbackState.value.copy(
                        isSynthesizing = false,
                        activeVoiceEngineLabel = "Device TTS · Fallback",
                        audioStatusMessage = "Ninada native worker previously failed in this session. Using device voice."
                    )
                    speakWithDeviceNeuralTts(mantra, langCode)
                    return@launch
                }
                if (!ninadaEngine.isPackReady(langCode, activeVoiceGender)) {
                    _playbackState.value = _playbackState.value.copy(
                        isSynthesizing = true,
                        activeVoiceEngineLabel = "Ninada Neural · Preparing",
                        audioStatusMessage = "Preparing Ninada Sanskrit voice… downloading automatically."
                    )
                    val download = ninadaEngine.downloadLanguagePack(langCode, activeVoiceGender)
                    if (generation != activePlaybackGeneration || !_playbackState.value.isPlaying) return@launch
                    if (download.isFailure || !ninadaEngine.isPackReady(langCode, activeVoiceGender)) {
                        val message = download.exceptionOrNull()?.message
                            ?: "Ninada voice pack could not be prepared."
                        _playbackState.value = _playbackState.value.copy(
                            isSynthesizing = false,
                            audioStatusMessage = "$message Using device voice as fallback."
                        )
                        speakWithDeviceNeuralTts(mantra, langCode)
                        return@launch
                    }
                }

                _playbackState.value = _playbackState.value.copy(
                    isSynthesizing = true,
                    activeVoiceEngineLabel = "Ninada Neural · Sanskrit",
                    audioStatusMessage = "Ninada is preparing the recitation…"
                )
                val synthesisText = if (langCode == "sa") {
                    formatForPanditChanting(mantra.sanskrit, devanagariSupported = true)
                } else mantra.sanskrit.trim()

                val result = NinadaV2WorkerClient(appContext).synthesize(
                    text = synthesisText,
                    language = langCode,
                    gender = activeVoiceGender,
                    speed = speed
                )
                if (generation != activePlaybackGeneration || !_playbackState.value.isPlaying) return@launch

                if (result != null && result.pcm16.isNotEmpty()) {
                    _playbackState.value = _playbackState.value.copy(
                        isSynthesizing = false,
                        activeVoiceEngineLabel = result.engineLabel,
                        audioStatusMessage = null
                    )
                    val playedOk = playPcmOnAudioTrack(result.pcm16, result.sampleRate, generation)
                    if (playedOk && generation == activePlaybackGeneration && _playbackState.value.isPlaying) {
                        handleChantCompleted()
                        return@launch
                    }
                }

                if (generation == activePlaybackGeneration && _playbackState.value.isPlaying) {
                    // Null means the isolated worker failed, disconnected, timed out, or produced invalid PCM.
                    // Circuit-break it for this process: a native SIGILL cannot be caught in Kotlin and repeat attempts are unsafe.
                    ninadaWorkerCircuitBroken = true
                    _playbackState.value = _playbackState.value.copy(
                        isSynthesizing = false,
                        activeVoiceEngineLabel = "Device TTS · Fallback",
                        audioStatusMessage = "Ninada worker failed. Device voice fallback is active; Ninada is paused for this session."
                    )
                    speakWithDeviceNeuralTts(mantra, langCode)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                AppCrashMonitor.recordHandledException("MantraAudioEngine.playMantraNinada", t)
                if (generation == activePlaybackGeneration && _playbackState.value.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        isSynthesizing = false,
                        audioStatusMessage = "Ninada could not start: ${t.message ?: "audio error"}. Using device voice."
                    )
                    speakWithDeviceNeuralTts(mantra, langCode)
                }
            }
        }
    }

    private fun speakWithDeviceNeuralTts(mantra: FestivalPujaMantra, langCode: String) {
        val ttsInstance = tts ?: return
        if (!isTtsReady) return

        applyVoiceAndPitchConfig(ttsInstance, langCode)

        val utteranceId = "mantra_${mantra.id}_${langCode}_round_${_playbackState.value.currentRound}"
        val textToSpeak: String
        val activeEngineLabel: String

        if (langCode == "sa") {
            if (isDevanagariVoiceActive) {
                textToSpeak = formatForPanditChanting(mantra.sanskrit, devanagariSupported = true)
                activeEngineLabel = lastConfiguredVoiceLabel
            } else {
                // Critical anti-silence fallback: never send raw Devanagari to a non-Devanagari voice
                textToSpeak = buildTransliteratedChantFallback(mantra)
                activeEngineLabel = "Device Voice · Phonetic Sanskrit"
            }
        } else {
            textToSpeak = mantra.sanskrit.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
            activeEngineLabel = lastConfiguredVoiceLabel
        }

        if (textToSpeak.isBlank()) return

        _playbackState.value = _playbackState.value.copy(
            isSynthesizing = false,
            activeVoiceEngineLabel = activeEngineLabel
        )

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val status = ttsInstance.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (status == TextToSpeech.ERROR) {
            handleUtteranceError(utteranceId)
        }
    }

    private suspend fun playPcmOnAudioTrack(
        pcm16: ShortArray,
        sampleRate: Int,
        generation: Long
    ): Boolean = withContext(Dispatchers.Default) {
        var track: AudioTrack? = null
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferBytes = (if (minBuf > 0) minBuf else sampleRate / 4).coerceAtLeast(4096)

            track = AudioTrack.Builder()
                .setAudioAttributes(buildSpeechAudioAttributes())
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                track.setVolume(1.0f)
            }

            voiceTrack = track
            track.play()

            var offset = 0
            val chunkSize = 2048
            while (isActive && generation == activePlaybackGeneration && offset < pcm16.size) {
                val count = (pcm16.size - offset).coerceAtMost(chunkSize)
                val written = track.write(pcm16, offset, count)
                if (written < 0) break
                offset += count
            }

            // Allow hardware audio pipeline to drain final buffer smoothly
            if (isActive && generation == activePlaybackGeneration) {
                delay(180)
            }
            offset >= pcm16.size
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            AppCrashMonitor.recordHandledException("MantraAudioEngine.playPcm", t)
            false
        } finally {
            try {
                track?.pause()
                track?.flush()
                track?.stop()
                track?.release()
            } catch (_: Throwable) {}
            if (voiceTrack === track) {
                voiceTrack = null
            }
        }
    }

    private fun stopVoiceTrack() {
        voicePlaybackJob?.cancel()
        voicePlaybackJob = null
        try {
            voiceTrack?.pause()
            voiceTrack?.flush()
            voiceTrack?.stop()
            voiceTrack?.release()
        } catch (_: Throwable) {}
        voiceTrack = null
    }

    /**
     * Formats Sanskrit mantra text for clear, natural Indian Pandit recitation:
     * 1. Strips any parenthetical English notes e.g. `(Rigveda 1.1)` or stray Latin words so the
     *    Indian TTS engine never switches to an English accent mid-mantra.
     * 2. Converts `ॐ` (U+0950) to `ओम्, ` so the TTS engine pronounces a resonant "Om".
     * 3. Adds gentle breathing pauses at pada (`।`) and shloka (`॥`) markers while keeping
     *    every Sanskrit word 100% intact for crystal-clear intelligibility.
     */
    internal fun formatForPanditChanting(raw: String, devanagariSupported: Boolean = true): String {
        val cleaned = raw
            .replace(Regex("\\([A-Za-z0-9 .,:\\-]+\\)"), " ")
            .let { text ->
                val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
                if (hasDevanagari) text.replace(Regex("[A-Za-z]+"), " ") else text
            }
            .replace("ॐ", "ओम्, ")
            .replace("॥", ". ")
            .replace("।", ", ")
            .replace(";", ", ")
            .replace("...", ", ")
            .replace("\n", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (devanagariSupported) cleaned else transliterateDevanagariToPhonetic(cleaned)
    }

    internal fun buildTransliteratedChantFallback(mantra: FestivalPujaMantra): String {
        val explicit = mantra.transliteration
            .replace("||", ". ")
            .replace("|", ", ")
            .replace("\n", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (explicit.isNotBlank()) return explicit
        return transliterateDevanagariToPhonetic(mantra.sanskrit)
    }

    internal fun transliterateDevanagariToPhonetic(text: String): String {
        val sb = StringBuilder(text.length * 2)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            val next = text.getOrNull(i + 1)
            val consonant = DEVANAGARI_CONSONANTS[c]
            if (consonant != null) {
                sb.append(consonant)
                when {
                    next == '\u094D' -> {
                        // Halant/virama suppresses inherent 'a'
                        i++
                    }
                    next != null && DEVANAGARI_MATRAS.containsKey(next) -> {
                        sb.append(DEVANAGARI_MATRAS[next])
                        i++
                    }
                    else -> sb.append("a")
                }
            } else {
                sb.append(DEVANAGARI_VOWELS_AND_SIGNS[c] ?: c.toString())
            }
            i++
        }
        return sb.toString()
            .replace("॥", ". ")
            .replace("।", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun handleChantCompleted() {
        engineScope.launch {
            val currentState = _playbackState.value
            if (!currentState.isPlaying) return@launch

            val nextRound = currentState.currentRound + 1
            triggerBeadHaptic()

            if (nextRound >= currentState.targetRounds) {
                _playbackState.value = currentState.copy(
                    currentRound = nextRound,
                    isPlaying = false,
                    isSynthesizing = false,
                    isCompleted = true
                )
                triggerCompletionHaptic()
                delay(1200)
                if (!_playbackState.value.isPlaying) {
                    stopInstrumentAccompaniment()
                    abandonSpeechAudioFocus()
                }
            } else {
                _playbackState.value = currentState.copy(currentRound = nextRound)
                delay(1100)
                if (_playbackState.value.isPlaying) {
                    speakCurrentRound()
                }
            }
        }
    }

    private fun triggerBeadHaptic() {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(35)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerCompletionHaptic() {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 80, 80, 140)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 80, 80, 140), -1)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Synthesizes soft, soothing Indian Temple Vadyam background accompaniment:
     * 1. 4-String Plucked Tanpura (Pa - Sa - Sa - Low Sa in C# = 277.18 Hz) at a gentle background drone level.
     * 2. Soft Indian Tabla (Dayan + Bayan in 8-matra Bhajan Keherwa Theka) at a subtle, meditative volume
     *    so the Pandit's voice remains 100% clear and foregrounded.
     */
    private fun startInstrumentAccompaniment() {
        if (isInstrumentsRunning) return
        isInstrumentsRunning = true

        instrumentJob = engineScope.launch(Dispatchers.Default) {
            var track: AudioTrack? = null
            try {
                val sampleRate = 44100
                val frameSamples = 1024
                val minBufferBytes = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSizeBytes = (if (minBufferBytes > 0) minBufferBytes else sampleRate / 2)
                    .coerceAtLeast(frameSamples * 4)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    track.setVolume(0.18f)
                }

                instrumentTrack = track
                track.play()

                val stringFreqs = doubleArrayOf(207.65, 277.18, 277.62, 138.59)
                val pluckIntervalSec = 1.25
                val tanpuraCycleSec = pluckIntervalSec * 4.0
                val isLowPower = AppBatteryMonitor.isLowPowerModeActive(appContext)
                val harmonicWeights = if (isLowPower) {
                    doubleArrayOf(0.45, 0.25)
                } else {
                    doubleArrayOf(0.38, 0.30, 0.16, 0.08)
                }

                val matraDurationSec = 0.40
                val tablaCycleSec = matraDurationSec * 8.0
                val dayanSaFreq = 277.18

                val pcmBuffer = ShortArray(frameSamples)
                var globalSampleIndex = 0L

                while (isActive && isInstrumentsRunning) {
                    val state = _playbackState.value
                    val tanpuraOn = state.isTanpuraEnabled
                    val tablaOn = state.isTablaEnabled

                    if (!tanpuraOn && !tablaOn) {
                        delay(60)
                        continue
                    }

                    for (i in 0 until frameSamples) {
                        val t = globalSampleIndex.toDouble() / sampleRate.toDouble()
                        globalSampleIndex++

                        var mix = 0.0

                        if (tanpuraOn) {
                            var tanpuraSignal = 0.0
                            val cyclePos = t % tanpuraCycleSec

                            for (sIdx in 0..3) {
                                val pluckStart = sIdx * pluckIntervalSec
                                val age = if (cyclePos >= pluckStart) {
                                    cyclePos - pluckStart
                                } else {
                                    cyclePos + tanpuraCycleSec - pluckStart
                                }

                                val attack = (age / 0.075).coerceIn(0.0, 1.0)
                                val decay = exp(-age / 2.8)
                                val jivariSwell = 0.75 + 0.25 * sin((age / 1.8).coerceIn(0.0, 1.0) * PI)

                                val f0 = stringFreqs[sIdx]
                                var stringWave = 0.0
                                for (h in harmonicWeights.indices) {
                                    val harmonicNum = h + 1
                                    val freq = f0 * harmonicNum
                                    val bridgePhase = 2.0 * PI * freq * t + (h * 0.35)
                                    val weight = if (harmonicNum >= 3) {
                                        harmonicWeights[h] * jivariSwell
                                    } else {
                                        harmonicWeights[h]
                                    }
                                    stringWave += sin(bridgePhase) * weight
                                }

                                tanpuraSignal += stringWave * attack * decay * 0.22
                            }
                            mix += tanpuraSignal * 0.20
                        }

                        if (tablaOn) {
                            val tablaCyclePos = t % tablaCycleSec
                            val bolIndex = (tablaCyclePos / matraDurationSec).toInt().coerceIn(0, 7)
                            val bolAge = tablaCyclePos - (bolIndex * matraDurationSec)

                            var tablaSignal = 0.0

                            val hasBayanGe = (bolIndex == 0 || bolIndex == 1 || bolIndex == 6)
                            val hasDayanNa = (bolIndex == 0 || bolIndex == 2 || bolIndex == 4 || bolIndex == 7)
                            val hasDayanTin = (bolIndex == 3 || bolIndex == 6)
                            val hasBayanKa = (bolIndex == 5)

                            if (hasBayanGe && bolAge < 0.32) {
                                val env = (bolAge / 0.010).coerceIn(0.0, 1.0) * exp(-bolAge / 0.12)
                                val pitchGlide = 106.0 + 22.0 * sin((bolAge / 0.22).coerceIn(0.0, 1.0) * PI)
                                val phaseB = 2.0 * PI * pitchGlide * bolAge
                                val bayanWave = sin(phaseB) * 0.60 + sin(phaseB * 2.0) * 0.28
                                tablaSignal += bayanWave * env * 0.42
                            }

                            if (hasDayanNa && bolAge < 0.24) {
                                val accent = if (bolIndex == 0) 0.90 else if (bolIndex == 7) 0.60 else 0.75
                                val env = (bolAge / 0.006).coerceIn(0.0, 1.0) * exp(-bolAge / 0.070)
                                val phaseD = 2.0 * PI * dayanSaFreq * bolAge
                                val dayanWave = sin(phaseD) * 0.55 + sin(phaseD * 2.0) * 0.30
                                tablaSignal += dayanWave * env * 0.36 * accent
                            }

                            if (hasDayanTin && bolAge < 0.30) {
                                val env = (bolAge / 0.006).coerceIn(0.0, 1.0) * exp(-bolAge / 0.105)
                                val phaseT = 2.0 * PI * dayanSaFreq * bolAge
                                val tinWave = sin(phaseT) * 0.60 + sin(phaseT * 2.0) * 0.28
                                tablaSignal += tinWave * env * 0.35
                            }

                            if (hasBayanKa && bolAge < 0.09) {
                                val env = (bolAge / 0.004).coerceIn(0.0, 1.0) * exp(-bolAge / 0.022)
                                val kaWave = sin(2.0 * PI * 170.0 * bolAge) * 0.65 + sin(2.0 * PI * 340.0 * bolAge) * 0.25
                                tablaSignal += kaWave * env * 0.30
                            }

                            mix += tablaSignal * 0.22
                        }

                        val clamped = mix.coerceIn(-0.60, 0.60)
                        pcmBuffer[i] = (clamped * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    }

                    track.write(pcmBuffer, 0, pcmBuffer.size)
                }
            } catch (t: Throwable) {
                AppCrashMonitor.recordHandledException("MantraAudioEngine", t)
                instrumentTrack = null
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Throwable) {}
            }
        }
    }

    private fun stopInstrumentAccompaniment() {
        isInstrumentsRunning = false
        instrumentJob?.cancel()
        instrumentJob = null
        try {
            instrumentTrack?.pause()
            instrumentTrack?.flush()
            instrumentTrack?.stop()
            instrumentTrack?.release()
        } catch (_: Exception) {}
        instrumentTrack = null
    }

    fun release() {
        stop()
        ninadaEngine.release()
        tts?.shutdown()
        tts = null
        isTtsReady = false
        engineScope.cancel()
    }

    companion object {
        @Volatile
        private var instance: MantraAudioEngine? = null

        fun getInstance(context: Context): MantraAudioEngine {
            return instance ?: synchronized(this) {
                instance ?: MantraAudioEngine(context.applicationContext).also { instance = it }
            }
        }

        private val DEVANAGARI_CONSONANTS = mapOf(
            'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng",
            'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
            'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
            'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
            'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
            'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v",
            'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h", 'ळ' to "l"
        )

        private val DEVANAGARI_MATRAS = mapOf(
            'ा' to "aa", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
            'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au"
        )

        private val DEVANAGARI_VOWELS_AND_SIGNS = mapOf(
            'ॐ' to "Om, ", 'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee",
            'उ' to "u", 'ऊ' to "oo", 'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai",
            'ओ' to "o", 'औ' to "au", 'ं' to "m", 'ँ' to "n", 'ः' to "ha",
            'ऽ' to ""
        )
    }
}

/**
 * Real-time DSP processor that transforms raw TTS synthesis into a warm, human Indian Pandit
 * vocal recitation with OLA pitch shifting, anti-metallic low-pass filter, and Temple Sanctum reverb.
 */
internal object VedicPanditVoiceProcessor {

    fun processPanditVoice(
        input: ShortArray,
        sampleRate: Int,
        pitchFactor: Float,
        tempoStretch: Float,
        isMalePandit: Boolean
    ): ShortArray {
        if (input.isEmpty()) return input

        val floatIn = FloatArray(input.size) { input[it] / 32768f }
        val pitchShifted = shiftPitchAndTempoOla(
            input = floatIn,
            sampleRate = sampleRate,
            pitchFactor = pitchFactor.coerceIn(0.60f, 1.45f),
            tempoStretch = tempoStretch.coerceIn(0.85f, 1.35f)
        )
        val filtered = applyVocalWarmthAndAntiRobotFilter(
            input = pitchShifted,
            sampleRate = sampleRate,
            isMalePandit = isMalePandit
        )
        val reverberated = applyTempleSanctumReverb(
            input = filtered,
            sampleRate = sampleRate
        )

        val out = ShortArray(reverberated.size)
        for (i in reverberated.indices) {
            var x = reverberated[i] * 1.15f
            x = x / (1f + kotlin.math.abs(x) * 0.22f)
            out[i] = (x.coerceIn(-0.98f, 0.98f) * 32767f).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }

    private fun shiftPitchAndTempoOla(
        input: FloatArray,
        sampleRate: Int,
        pitchFactor: Float,
        tempoStretch: Float
    ): FloatArray {
        val resampledLength = (input.size / pitchFactor).toInt().coerceAtLeast(1)
        val resampled = FloatArray(resampledLength)
        for (i in 0 until resampledLength) {
            val t = i.toDouble() / sampleRate.toDouble()
            val vibrato = 1.0 + 0.0022 * sin(2.0 * PI * 4.6 * t)
            val srcPos = i * pitchFactor * vibrato
            val idx0 = srcPos.toInt()
            val frac = (srcPos - idx0).toFloat()
            val s0 = if (idx0 in input.indices) input[idx0] else 0f
            val s1 = if (idx0 + 1 in input.indices) input[idx0 + 1] else s0
            resampled[i] = s0 + frac * (s1 - s0)
        }

        val targetStretch = tempoStretch * pitchFactor
        if (kotlin.math.abs(targetStretch - 1.0f) < 0.03f) {
            return resampled
        }

        val windowSize = (sampleRate * 0.036).toInt().coerceAtLeast(256)
        val hopOut = windowSize / 2
        val hopIn = (hopOut / targetStretch).toInt().coerceAtLeast(1)
        val outLength = (resampled.size * targetStretch).toInt().coerceAtLeast(windowSize)
        val output = FloatArray(outLength)
        val norm = FloatArray(outLength)

        val hann = FloatArray(windowSize) { n ->
            (0.5 * (1.0 - cos(2.0 * PI * n / (windowSize - 1)))).toFloat()
        }

        var inPos = 0
        var outPos = 0
        while (inPos + windowSize < resampled.size && outPos + windowSize < outLength) {
            for (k in 0 until windowSize) {
                val w = hann[k]
                output[outPos + k] += resampled[inPos + k] * w
                norm[outPos + k] += w
            }
            inPos += hopIn
            outPos += hopOut
        }

        for (i in output.indices) {
            if (norm[i] > 0.05f) {
                output[i] /= norm[i]
            }
        }
        return output
    }

    private fun applyVocalWarmthAndAntiRobotFilter(
        input: FloatArray,
        sampleRate: Int,
        isMalePandit: Boolean
    ): FloatArray {
        val out = FloatArray(input.size)
        val cutoffHz = if (isMalePandit) 3300.0 else 4200.0
        val rc = 1.0 / (2.0 * PI * cutoffHz)
        val dt = 1.0 / sampleRate.toDouble()
        val alphaLp = (dt / (rc + dt)).toFloat()

        val chestCutoff = if (isMalePandit) 165.0 else 250.0
        val rcChest = 1.0 / (2.0 * PI * chestCutoff)
        val alphaChest = (dt / (rcChest + dt)).toFloat()
        val chestGain = if (isMalePandit) 0.55f else 0.28f

        var lp1 = 0f
        var lp2 = 0f
        var chestState = 0f

        for (i in input.indices) {
            val x = input[i]
            chestState += alphaChest * (x - chestState)
            val warmed = x + chestState * chestGain
            lp1 += alphaLp * (warmed - lp1)
            lp2 += alphaLp * (lp1 - lp2)
            out[i] = lp2
        }
        return out
    }

    private fun applyTempleSanctumReverb(
        input: FloatArray,
        sampleRate: Int
    ): FloatArray {
        val tailSamples = (sampleRate * 0.35).toInt()
        val totalLen = input.size + tailSamples
        val out = FloatArray(totalLen)

        val d1 = (sampleRate * 0.0297).toInt().coerceAtLeast(1)
        val d2 = (sampleRate * 0.0371).toInt().coerceAtLeast(1)
        val d3 = (sampleRate * 0.0439).toInt().coerceAtLeast(1)
        val comb1 = FloatArray(d1)
        val comb2 = FloatArray(d2)
        val comb3 = FloatArray(d3)
        var idx1 = 0
        var idx2 = 0
        var idx3 = 0
        var damp1 = 0f
        var damp2 = 0f
        var damp3 = 0f

        val apDelay = (sampleRate * 0.0068).toInt().coerceAtLeast(1)
        val allPass = FloatArray(apDelay)
        var apIdx = 0

        for (i in 0 until totalLen) {
            val dry = if (i < input.size) input[i] else 0f

            val c1 = comb1[idx1]
            damp1 = 0.65f * damp1 + 0.35f * c1
            comb1[idx1] = dry + damp1 * 0.54f
            idx1 = (idx1 + 1) % d1

            val c2 = comb2[idx2]
            damp2 = 0.65f * damp2 + 0.35f * c2
            comb2[idx2] = dry + damp2 * 0.50f
            idx2 = (idx2 + 1) % d2

            val c3 = comb3[idx3]
            damp3 = 0.65f * damp3 + 0.35f * c3
            comb3[idx3] = dry + damp3 * 0.46f
            idx3 = (idx3 + 1) % d3

            val combSum = (c1 + c2 + c3) * 0.33f

            val bufOut = allPass[apIdx]
            val wet = -0.5f * combSum + bufOut
            allPass[apIdx] = combSum + 0.5f * wet
            apIdx = (apIdx + 1) % apDelay

            out[i] = dry * 0.82f + wet * 0.24f
        }
        return out
    }
}
