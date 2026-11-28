package com.example.platform.audio

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.content.Context
import android.util.Log
import com.example.data.preferences.AppLanguage
import com.example.platform.monitoring.AppCrashMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Metadata descriptor for each Ninada (hemanth/ninada · gnumanth/ninada) 24 kHz
 * Mobile-Tier ONNX neural voice specialist covering Classical Sanskrit and all
 * 8 regional UtsavSetu app languages.
 */
data class NinadaLanguagePack(
    val code: String,
    val appLanguage: AppLanguage?,
    val displayName: String,
    val nativeLabel: String,
    val bcp47Locale: String,
    val approxSizeMb: Int,
    val samplePhrase: String,
    val isPrimarySacredLanguage: Boolean = false
)

sealed class NinadaPackStatus {
    data object NotDownloaded : NinadaPackStatus()
    data class Downloading(val progress: Float, val statusMessage: String) : NinadaPackStatus()
    data class Ready(val fileSizeMb: Double, val modelFileName: String) : NinadaPackStatus()
    data class Error(val message: String) : NinadaPackStatus()
}

data class NinadaSynthesisResult(
    val pcm16: ShortArray,
    val sampleRate: Int,
    val packCode: String,
    val engineLabel: String
)

/**
 * On-device Neural TTS Engine integrating the open-source Ninada (निनाद) Pan-Indic &
 * Classical Sanskrit ONNX model suite (GitHub: hemanth/ninada · Hugging Face: gnumanth/ninada).
 *
 * Uses Microsoft ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime-android`) to execute
 * mobile-tier 24 kHz iSTFTNet / VITS specialists cached locally in `filesDir/ninada_models`.
 * Dynamically introspects `models_manifest.json`, `vocabs.json`, and `OrtSession.inputInfo`
 * so updates to Ninada's ONNX checkpoints continue working reliably across all 9 languages.
 */
class NinadaNeuralVoiceEngine private constructor(private val appContext: Context) {

    private val modelsDir: File = File(appContext.filesDir, "ninada_models").apply { mkdirs() }
    private val manifestFile: File = File(modelsDir, "models_manifest.json")
    private val vocabsFile: File = File(modelsDir, "vocabs.json")

    private val _packStatuses = MutableStateFlow<Map<String, NinadaPackStatus>>(emptyMap())
    val packStatuses: StateFlow<Map<String, NinadaPackStatus>> = _packStatuses.asStateFlow()

    private val sessionMutex = Mutex()
    private val downloadMutex = Mutex()
    private var activeSessionKey: String? = null
    private var activeSession: OrtSession? = null
    private var ortEnv: OrtEnvironment? = null

    init {
        refreshInstalledStatuses()
    }

    fun refreshInstalledStatuses() {
        val map = LinkedHashMap<String, NinadaPackStatus>()
        for (pack in SUPPORTED_PACKS) {
            val existing = findCachedModelFile(pack.code, "MALE")
                ?: findCachedModelFile(pack.code, "FEMALE")
                ?: findCachedModelFile(pack.code, null)
            map[pack.code] = if (existing != null && existing.length() > MIN_VALID_ONNX_BYTES) {
                NinadaPackStatus.Ready(
                    fileSizeMb = existing.length().toDouble() / (1024.0 * 1024.0),
                    modelFileName = existing.name
                )
            } else {
                NinadaPackStatus.NotDownloaded
            }
        }
        _packStatuses.value = map
    }

    fun isPackReady(languageCode: String, gender: String? = null): Boolean {
        val code = normalizeLangCode(languageCode)
        val model = findCachedModelFile(code, gender)
            ?: findCachedModelFile(code, null)
            ?: findCachedModelFile(code, if (gender.equals("FEMALE", true)) "MALE" else "FEMALE")
        return model != null && model.length() > MIN_VALID_ONNX_BYTES
    }

    fun getPackForLanguage(language: AppLanguage): NinadaLanguagePack {
        return SUPPORTED_PACKS.firstOrNull { it.appLanguage == language }
            ?: SUPPORTED_PACKS.first()
    }

    fun getPackByCode(code: String): NinadaLanguagePack? {
        val norm = normalizeLangCode(code)
        return SUPPORTED_PACKS.firstOrNull { it.code == norm }
    }

    /**
     * Downloads and caches the Ninada 24 kHz Mobile-Tier ONNX specialist + tokenizer vocab
     * for [languageCode] (e.g. "sa", "hi", "mr", "gu", "ta", "te", "kn", "bn", "en") and
     * preferred [gender] ("MALE" or "FEMALE") from Hugging Face `gnumanth/ninada`.
     */
    suspend fun downloadLanguagePack(
        languageCode: String,
        gender: String = "MALE"
    ): Result<File> = withContext(Dispatchers.IO) {
        val code = normalizeLangCode(languageCode)
        val normGender = if (gender.equals("FEMALE", ignoreCase = true)) "female" else "male"

        downloadMutex.withLock {
            try {
                updatePackStatus(
                    code,
                    NinadaPackStatus.Downloading(0.05f, "Fetching Ninada catalog…")
                )

                // 1. Fetch manifest & vocab metadata (best-effort cached locally)
                val manifestJson = fetchOrLoadManifest()
                ensureVocabFileDownloaded(code, manifestJson)

                // 2. Resolve candidate remote ONNX URLs (Mobile Tier first, Studio Tier fallback)
                val candidatePaths = resolveCandidateModelPaths(code, normGender, manifestJson)
                var downloadedFile: File? = null
                var lastFailure: String? = null

                for ((index, remoteRelPath) in candidatePaths.withIndex()) {
                    val cleanRel = remoteRelPath.trimStart('/')
                    val urlString = "$HF_BASE_RESOLVE_URL/$cleanRel"
                    val localFileName = "${code}_${normGender}.onnx"
                    val targetFile = File(modelsDir, localFileName)
                    val tempFile = File(modelsDir, "$localFileName.tmp")

                    updatePackStatus(
                        code,
                        NinadaPackStatus.Downloading(
                            progress = 0.12f,
                            statusMessage = "Downloading Ninada ${code.uppercase(Locale.US)} neural model…"
                        )
                    )

                    val downloadResult = downloadFileWithProgress(
                        urlString = urlString,
                        destTmp = tempFile
                    ) { fraction ->
                        val scaled = 0.12f + (fraction * 0.85f)
                        updatePackStatus(
                            code,
                            NinadaPackStatus.Downloading(
                                progress = scaled.coerceIn(0.12f, 0.97f),
                                statusMessage = "Downloading Ninada ${code.uppercase(Locale.US)} (${(fraction * 100).roundToInt()}%)…"
                            )
                        )
                    }

                    if (downloadResult.isSuccess && tempFile.length() > MIN_VALID_ONNX_BYTES) {
                        if (targetFile.exists()) targetFile.delete()
                        if (!tempFile.renameTo(targetFile)) {
                            tempFile.copyTo(targetFile, overwrite = true)
                            tempFile.delete()
                        }
                        downloadedFile = targetFile
                        break
                    } else {
                        tempFile.delete()
                        lastFailure = downloadResult.exceptionOrNull()?.message
                            ?: "Model path $cleanRel unavailable (${index + 1}/${candidatePaths.size})"
                    }
                }

                if (downloadedFile != null && downloadedFile.exists()) {
                    val sizeMb = downloadedFile.length().toDouble() / (1024.0 * 1024.0)
                    updatePackStatus(
                        code,
                        NinadaPackStatus.Ready(
                            fileSizeMb = sizeMb,
                            modelFileName = downloadedFile.name
                        )
                    )
                    Result.success(downloadedFile)
                } else {
                    val errMsg = lastFailure
                        ?: "Could not reach Hugging Face (gnumanth/ninada). Device neural TTS will be used."
                    updatePackStatus(code, NinadaPackStatus.Error(errMsg))
                    Result.failure(IllegalStateException(errMsg))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                AppCrashMonitor.recordHandledException("NinadaNeuralVoiceEngine.download", t)
                val errMsg = t.message ?: "Download failed; using device neural TTS."
                updatePackStatus(code, NinadaPackStatus.Error(errMsg))
                Result.failure(t)
            }
        }
    }

    /**
     * Removes cached ONNX model files for a specific language code to free device storage.
     */
    suspend fun deleteLanguagePack(languageCode: String) = withContext(Dispatchers.IO) {
        val code = normalizeLangCode(languageCode)
        sessionMutex.withLock {
            if (activeSessionKey?.startsWith("${code}_") == true) {
                closeActiveSessionLocked()
            }
        }
        modelsDir.listFiles()
            ?.filter { it.name.startsWith("${code}_") || it.name == "$code.onnx" }
            ?.forEach { it.delete() }
        refreshInstalledStatuses()
    }

    /**
     * Synthesizes [rawText] into 24,000 Hz 16-bit mono PCM using the cached Ninada ONNX model.
     * Returns `null` cleanly if the pack is not installed, ONNX native runtime is unavailable,
     * or synthesis yields empty/silent output, allowing [MantraAudioEngine] to fall back
     * seamlessly to device neural TTS without silence.
     */
    suspend fun synthesizePcm24k(
        rawText: String,
        languageCode: String,
        gender: String,
        speed: Float
    ): NinadaSynthesisResult? = withContext(Dispatchers.Default) {
        val code = normalizeLangCode(languageCode)
        val modelFile = findCachedModelFile(code, gender)
            ?: findCachedModelFile(code, null)
            ?: findCachedModelFile(code, if (gender.equals("FEMALE", true)) "MALE" else "FEMALE")
            ?: run { Log.e(TAG, "DIAG model missing lang=$code gender=$gender dir=${modelsDir.absolutePath}"); return@withContext null }
        Log.i(TAG, "DIAG model found lang=$code file=${modelFile.name} bytes=${modelFile.length()}")

        if (modelFile.length() <= MIN_VALID_ONNX_BYTES) return@withContext null

        val preparedText = normalizeTextForNinada(rawText, code)
        if (preparedText.isBlank()) return@withContext null

        val tokenIds = tokenizeForLanguage(preparedText, code)
        Log.i(TAG, "DIAG normalized chars=${preparedText.length} tokens=${tokenIds.size} vocabFile=${vocabsFile.exists()} vocabBytes=${if (vocabsFile.exists()) vocabsFile.length() else 0}")
        if (tokenIds.isEmpty()) { Log.e(TAG, "DIAG empty tokens lang=$code"); return@withContext null }

        sessionMutex.withLock {
            val createdTensors = mutableListOf<OnnxTensor>()
            var result: OrtSession.Result? = null
            try {
                val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }
                val sessionKey = "${code}_${modelFile.name}_${modelFile.lastModified()}"
                val session = if (activeSessionKey == sessionKey && activeSession != null) {
                    activeSession!!
                } else {
                    closeActiveSessionLocked()
                    val options = OrtSession.SessionOptions().apply {
                        // Conservative settings for mobile devices: avoid aggressive graph/kernel
                        // optimization paths implicated by native SIGILL during Ninada inference.
                        setIntraOpNumThreads(1)
                        setInterOpNumThreads(1)
                        setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
                        setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT)
                    }
                    Log.i(TAG, "DIAG creating ORT session lang=$code model=${modelFile.name}")
                    env.createSession(modelFile.absolutePath, options).also {
                        Log.i(TAG, "DIAG session created inputs=${it.inputInfo.keys} outputs=${it.outputInfo.keys}")
                        activeSession = it
                        activeSessionKey = sessionKey
                    }
                }

                val inputs = buildDynamicOnnxInputs(
                    env = env,
                    session = session,
                    tokenIds = tokenIds,
                    gender = gender,
                    speed = speed,
                    createdTensors = createdTensors
                )
                Log.i(TAG, "DIAG tensor binding inputs=${inputs.keys}")
                if (inputs.isEmpty()) { Log.e(TAG, "DIAG no tensors bound"); return@withContext null }

                Log.i(TAG, "DIAG inference starting lang=$code tokenCount=${tokenIds.size}")
                result = session.run(inputs)
                Log.i(TAG, "DIAG inference returned outputs=${result?.size()}")
                val floatWave = extractAudioFloatArray(result) ?: run { Log.e(TAG, "DIAG waveform extraction failed"); return@withContext null }
                Log.i(TAG, "DIAG waveform samples=${floatWave.size}")
                if (floatWave.size < 400) return@withContext null

                val pcm16 = normalizeAndConvertToPcm16(floatWave) ?: return@withContext null
                val packName = getPackByCode(code)?.nativeLabel ?: code.uppercase(Locale.US)
                NinadaSynthesisResult(
                    pcm16 = pcm16,
                    sampleRate = NINADA_SAMPLE_RATE,
                    packCode = code,
                    engineLabel = "Ninada Neural 24kHz · $packName"
                )
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.e(TAG, "DIAG Ninada exception lang=$code", t)
                AppCrashMonitor.recordHandledException("NinadaNeuralVoiceEngine.synthesize", t)
                null
            } finally {
                try {
                    result?.close()
                } catch (_: Throwable) {}
                createdTensors.forEach { tensor ->
                    try {
                        tensor.close()
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    /**
     * Dynamically inspects [session.inputInfo] to bind standard Ninada / VITS / iSTFTNet
     * input tensors (`input_ids`/`tokens`/`x`, `input_lengths`/`token_lens`/`x_length`,
     * `scales` or individual `noise_scale`/`length_scale`/`noise_scale_w`, and `sid`/`speaker_id`).
     */
    private fun buildDynamicOnnxInputs(
        env: OrtEnvironment,
        session: OrtSession,
        tokenIds: LongArray,
        gender: String,
        speed: Float,
        createdTensors: MutableList<OnnxTensor>
    ): Map<String, OnnxTensor> {
        val inputs = LinkedHashMap<String, OnnxTensor>()
        val lengthScale = (1.0f / speed.coerceIn(0.65f, 1.5f)).coerceIn(0.65f, 1.55f)
        val noiseScale = 0.667f
        val noiseScaleW = 0.8f
        val speakerId = if (gender.equals("FEMALE", ignoreCase = true)) 1L else 0L

        for ((name, nodeInfo) in session.inputInfo) {
            val tensorInfo = nodeInfo.info as? TensorInfo ?: continue
            val lower = name.lowercase(Locale.ROOT)
            val rank = tensorInfo.shape.size
            val javaType = tensorInfo.type

            val tensor: OnnxTensor? = when {
                lower.contains("len") -> {
                    when (javaType) {
                        OnnxJavaType.INT32 -> OnnxTensor.createTensor(
                            env,
                            IntBuffer.wrap(intArrayOf(tokenIds.size)),
                            longArrayOf(1L)
                        )
                        else -> OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(longArrayOf(tokenIds.size.toLong())),
                            longArrayOf(1L)
                        )
                    }
                }
                lower == "scales" -> {
                    val scales = floatArrayOf(noiseScale, lengthScale, noiseScaleW)
                    val shape = if (rank == 2) longArrayOf(1L, 3L) else longArrayOf(3L)
                    OnnxTensor.createTensor(env, FloatBuffer.wrap(scales), shape)
                }
                lower.contains("length_scale") -> {
                    val shape = if (rank == 0) longArrayOf() else longArrayOf(1L)
                    OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArrayOf(lengthScale)), shape)
                }
                lower.contains("noise_scale_w") || lower.contains("noise_w") -> {
                    val shape = if (rank == 0) longArrayOf() else longArrayOf(1L)
                    OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArrayOf(noiseScaleW)), shape)
                }
                lower.contains("noise") -> {
                    val shape = if (rank == 0) longArrayOf() else longArrayOf(1L)
                    OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArrayOf(noiseScale)), shape)
                }
                lower == "sid" || lower.contains("speaker") -> {
                    when (javaType) {
                        OnnxJavaType.INT32 -> OnnxTensor.createTensor(
                            env,
                            IntBuffer.wrap(intArrayOf(speakerId.toInt())),
                            longArrayOf(1L)
                        )
                        else -> OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(longArrayOf(speakerId)),
                            longArrayOf(1L)
                        )
                    }
                }
                lower.contains("style") && javaType == OnnxJavaType.FLOAT -> {
                    val dim = tensorInfo.shape.lastOrNull()?.takeIf { it > 0 }?.toInt() ?: 128
                    val zeros = FloatArray(dim)
                    val shape = if (rank == 2) longArrayOf(1L, dim.toLong()) else longArrayOf(dim.toLong())
                    OnnxTensor.createTensor(env, FloatBuffer.wrap(zeros), shape)
                }
                lower == "x" || lower.contains("input") || lower.contains("token") || lower.contains("id") -> {
                    val shape = if (rank == 1) {
                        longArrayOf(tokenIds.size.toLong())
                    } else {
                        longArrayOf(1L, tokenIds.size.toLong())
                    }
                    when (javaType) {
                        OnnxJavaType.INT32 -> {
                            val intTokens = IntArray(tokenIds.size) { tokenIds[it].toInt() }
                            OnnxTensor.createTensor(env, IntBuffer.wrap(intTokens), shape)
                        }
                        else -> OnnxTensor.createTensor(env, LongBuffer.wrap(tokenIds), shape)
                    }
                }
                else -> null
            }

            if (tensor != null) {
                createdTensors.add(tensor)
                inputs[name] = tensor
            }
        }
        return inputs
    }

    /**
     * Tokenizes [text] into integer IDs using Ninada's cached `vocabs.json` for [langCode],
     * falling back to direct Pan-Indic script table indexing when a language-specific map
     * is embedded inside a single vocab dictionary.
     */
    internal fun tokenizeForLanguage(text: String, langCode: String): LongArray {
        val vocabMap = loadVocabMapForLanguage(langCode)
        // Never invent token IDs: the model vocabulary is part of the ONNX contract.
        // Missing or mismatched vocab must trigger device TTS fallback, not garbage speech.
        if (vocabMap.isEmpty()) {
            Log.e(TAG, "Ninada tokenization rejected: no valid vocabulary for lang=$langCode")
            return LongArray(0)
        }
        val ids = ArrayList<Long>(text.length * 2 + 2)
        val blankId = vocabMap["_"] ?: vocabMap["<BLNK>"] ?: vocabMap["<blank>"] ?: vocabMap["<PAD>"] ?: vocabMap["<pad>"] ?: 0L
        val spaceId = vocabMap[" "] ?: blankId
        val useBlankInterleaving = vocabMap.containsKey("_") || vocabMap.containsKey("<BLNK>") || vocabMap.containsKey("<blank>") || vocabMap.containsKey("<PAD>") || vocabMap.containsKey("<pad>")
        if (useBlankInterleaving) ids.add(blankId)
        for (ch in text) {
            val key = ch.toString()
            val mapped = vocabMap[key]
                ?: vocabMap[key.lowercase(Locale.ROOT)]
                ?: if (ch.isWhitespace()) spaceId else null
            if (mapped == null) {
                Log.e(TAG, "Ninada tokenization rejected: unmapped character in input lang=$langCode")
                return LongArray(0)
            }
            ids.add(mapped)
            if (useBlankInterleaving) ids.add(blankId)
        }
        return ids.toLongArray()
    }

    internal fun normalizeTextForNinada(raw: String, langCode: String): String {
        val cleaned = raw
            .replace(Regex("\\([A-Za-z0-9 .,:\\-]+\\)"), " ")
            .replace("ॐ", "ओम्, ")
            .replace("॥", ". ")
            .replace("।", ", ")
            .replace(";", ", ")
            .replace("...", ", ")
            .replace("\n", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (langCode == "sa" && cleaned.any { it in '\u0900'..'\u097F' }) {
            cleaned.replace(Regex("[A-Za-z]+"), " ").replace(Regex("\\s+"), " ").trim()
        } else {
            cleaned
        }
    }

    private fun loadVocabMapForLanguage(langCode: String): Map<String, Long> {
        val perLangFile = File(modelsDir, "vocab_${langCode}.json")
        val targetFile = when {
            perLangFile.exists() && perLangFile.length() > 10 -> perLangFile
            vocabsFile.exists() && vocabsFile.length() > 10 -> vocabsFile
            else -> return emptyMap()
        }
        return try {
            val content = targetFile.readText(Charsets.UTF_8).trim()
            if (content.startsWith("[")) {
                parseVocabArray(JSONArray(content))
            } else {
                val root = JSONObject(content)
                when {
                    root.has("languages") && root.getJSONObject("languages").has(langCode) -> parseVocabNode(root.getJSONObject("languages").get(langCode))
                    root.has(langCode) -> parseVocabNode(root.get(langCode))
                    root.has("vocabs") && root.getJSONObject("vocabs").has(langCode) -> parseVocabNode(root.getJSONObject("vocabs").get(langCode))
                    root.has("symbols") -> parseVocabNode(root.get("symbols"))
                    root.has("token_to_id") -> parseVocabNode(root.get("token_to_id"))
                    else -> parseVocabObject(root)
                }
            }
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    private fun parseVocabNode(node: Any): Map<String, Long> {
        return when (node) {
            is JSONArray -> parseVocabArray(node)
            is JSONObject -> {
                when {
                    node.has("char_to_id") -> parseVocabNode(node.get("char_to_id"))
                    node.has("symbols") -> parseVocabNode(node.get("symbols"))
                    node.has("token_to_id") -> parseVocabNode(node.get("token_to_id"))
                    node.has("vocab") -> parseVocabNode(node.get("vocab"))
                    else -> parseVocabObject(node)
                }
            }
            else -> emptyMap()
        }
    }

    private fun parseVocabArray(arr: JSONArray): Map<String, Long> {
        val map = LinkedHashMap<String, Long>(arr.length())
        for (i in 0 until arr.length()) {
            val sym = arr.optString(i, "")
            if (sym.isNotEmpty()) map[sym] = i.toLong()
        }
        return map
    }

    private fun parseVocabObject(obj: JSONObject): Map<String, Long> {
        val map = LinkedHashMap<String, Long>(obj.length())
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val v = obj.opt(key)
            if (v is Number) {
                map[key] = v.toLong()
            } else if (v is String) {
                // Reverse mapping: {"0": "_", "1": "अ"}
                key.toLongOrNull()?.let { id -> map[v] = id }
            }
        }
        return map
    }

    private fun extractAudioFloatArray(result: OrtSession.Result): FloatArray? {
        if (result.size() == 0) return null
        val outputValue = result[0].value ?: return null
        val collector = ArrayList<Float>(16384)
        flattenFloatTensor(outputValue, collector)
        if (collector.isEmpty()) return null
        return FloatArray(collector.size) { collector[it] }
    }

    private fun flattenFloatTensor(value: Any?, out: MutableList<Float>) {
        when (value) {
            is FloatArray -> {
                for (sample in value) out.add(sample)
            }
            is Array<*> -> {
                for (child in value) flattenFloatTensor(child, out)
            }
        }
    }

    /**
     * Validates non-silent energy (RMS > threshold) and normalizes waveform safely to
     * 16-bit PCM with smooth soft-limiting so playback is crisp and audible on every speaker.
     */
    internal fun normalizeAndConvertToPcm16(wave: FloatArray): ShortArray? {
        if (wave.isEmpty()) return null
        var sumSq = 0.0
        var peak = 0f
        for (sample in wave) {
            if (!sample.isFinite()) continue
            val a = abs(sample)
            if (a > peak) peak = a
            sumSq += sample * sample
        }
        val rms = sqrt(sumSq / wave.size.toDouble())
        if (rms < 0.0015 || peak < 0.005f) return null

        val gain = (0.86f / peak).coerceAtMost(6.0f)
        val out = ShortArray(wave.size)
        for (i in wave.indices) {
            val raw = if (wave[i].isFinite()) wave[i] * gain else 0f
            val soft = raw / (1f + abs(raw) * 0.14f)
            out[i] = (soft.coerceIn(-0.98f, 0.98f) * 32767f).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }

    private fun fetchOrLoadManifest(): JSONObject? {
        val remoteText = fetchSmallText("$HF_BASE_RESOLVE_URL/models_manifest.json")
        if (remoteText != null && remoteText.trim().startsWith("{")) {
            try {
                manifestFile.writeText(remoteText, Charsets.UTF_8)
                return JSONObject(remoteText)
            } catch (_: Throwable) {}
        }
        if (manifestFile.exists()) {
            return try {
                JSONObject(manifestFile.readText(Charsets.UTF_8))
            } catch (_: Throwable) {
                null
            }
        }
        return null
    }

    private fun ensureVocabFileDownloaded(code: String, manifest: JSONObject?) {
        val candidateVocabPaths = LinkedHashSet<String>()
        manifest?.let { extractPathsMatching(it, setOf("vocab", "token"), candidateVocabPaths) }
        candidateVocabPaths.add("vocabs.json")
        candidateVocabPaths.add("vocab.json")
        candidateVocabPaths.add("onnx_mobile/vocabs.json")
        candidateVocabPaths.add("onnx_mobile/vocab.json")
        candidateVocabPaths.add("onnx/vocab.json")
        candidateVocabPaths.add("onnx_mobile/${code}_vocab.json")

        for (rel in candidateVocabPaths) {
            val txt = fetchSmallText("$HF_BASE_RESOLVE_URL/${rel.trimStart('/')}")
            if (txt != null && (txt.trim().startsWith("{") || txt.trim().startsWith("["))) {
                val dest = if (rel.contains(code)) File(modelsDir, "vocab_${code}.json") else vocabsFile
                try {
                    dest.writeText(txt, Charsets.UTF_8)
                    return
                } catch (_: Throwable) {}
            }
        }
    }

    private fun resolveCandidateModelPaths(
        code: String,
        gender: String,
        manifest: JSONObject?
    ): List<String> {
        val paths = LinkedHashSet<String>()
        val otherGender = if (gender == "female") "male" else "female"

        // 1. Paths discovered dynamically from Ninada's official models_manifest.json
        manifest?.let { json ->
            val allOnnx = LinkedHashSet<String>()
            extractPathsMatching(json, setOf(".onnx"), allOnnx)
            val mobileExact = allOnnx.filter {
                it.contains("mobile", true) && matchesLanguageAndGender(it, code, gender)
            }
            val mobileLang = allOnnx.filter {
                it.contains("mobile", true) && matchesLanguage(it, code)
            }
            val studioExact = allOnnx.filter {
                !it.contains("mobile", true) && matchesLanguageAndGender(it, code, gender)
            }
            val studioLang = allOnnx.filter {
                !it.contains("mobile", true) && matchesLanguage(it, code)
            }
            paths.addAll(mobileExact)
            paths.addAll(mobileLang)
            paths.addAll(studioExact)
            paths.addAll(studioLang)
        }

        // 2. Canonical Ninada Mobile-Tier & Studio-Tier Hugging Face paths
        paths.add("onnx_mobile/${code}_${gender}.onnx")
        paths.add("onnx_mobile/${code}.onnx")
        paths.add("onnx_mobile/${code}_${otherGender}.onnx")
        paths.add("onnx/${code}_${gender}.onnx")
        paths.add("onnx/${code}.onnx")
        paths.add("onnx/${code}_${otherGender}.onnx")
        return paths.toList()
    }

    private fun extractPathsMatching(node: Any?, keywords: Set<String>, out: MutableSet<String>) {
        when (node) {
            is JSONObject -> {
                val keys = node.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = node.opt(k)
                    if (v is String && keywords.any { v.lowercase(Locale.ROOT).contains(it) }) {
                        out.add(v)
                    } else {
                        extractPathsMatching(v, keywords, out)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    val v = node.opt(i)
                    if (v is String && keywords.any { v.lowercase(Locale.ROOT).contains(it) }) {
                        out.add(v)
                    } else {
                        extractPathsMatching(v, keywords, out)
                    }
                }
            }
        }
    }

    private fun matchesLanguage(path: String, code: String): Boolean {
        val fileName = path.substringAfterLast('/').lowercase(Locale.ROOT)
        return fileName == "$code.onnx" ||
            fileName.startsWith("${code}_") ||
            fileName.startsWith("${code}-") ||
            path.lowercase(Locale.ROOT).contains("/$code/")
    }

    private fun matchesLanguageAndGender(path: String, code: String, gender: String): Boolean {
        val lower = path.lowercase(Locale.ROOT)
        return matchesLanguage(lower, code) && lower.contains(gender)
    }

    private fun fetchSmallText(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 10_000
                readTimeout = 12_000
                setRequestProperty("User-Agent", "UtsavSetu-Android/1.0")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun downloadFileWithProgress(
        urlString: String,
        destTmp: File,
        onProgress: (Float) -> Unit
    ): Result<File> {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 45_000
                setRequestProperty("User-Agent", "UtsavSetu-Android/1.0")
            }
            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                return Result.failure(IllegalStateException("HTTP $responseCode"))
            }
            val totalBytes = conn.contentLengthLong
            BufferedInputStream(conn.inputStream).use { input ->
                FileOutputStream(destTmp).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var lastReportedPct = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0) {
                            val fraction = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            val pct = (fraction * 100).toInt()
                            if (pct != lastReportedPct && pct % 2 == 0) {
                                lastReportedPct = pct
                                onProgress(fraction)
                            }
                        }
                    }
                    output.flush()
                }
            }
            Result.success(destTmp)
        } catch (t: Throwable) {
            Result.failure(t)
        } finally {
            conn?.disconnect()
        }
    }

    private fun findCachedModelFile(code: String, gender: String?): File? {
        val candidates = if (gender != null) {
            val g = if (gender.equals("FEMALE", ignoreCase = true)) "female" else "male"
            listOf(File(modelsDir, "${code}_${g}.onnx"), File(modelsDir, "$code.onnx"))
        } else {
            listOf(
                File(modelsDir, "$code.onnx"),
                File(modelsDir, "${code}_male.onnx"),
                File(modelsDir, "${code}_female.onnx")
            )
        }
        return candidates.firstOrNull { it.exists() && it.length() > MIN_VALID_ONNX_BYTES }
    }

    private fun updatePackStatus(code: String, status: NinadaPackStatus) {
        val current = _packStatuses.value.toMutableMap()
        current[code] = status
        _packStatuses.value = current
    }

    private fun closeActiveSessionLocked() {
        try {
            activeSession?.close()
        } catch (_: Throwable) {}
        activeSession = null
        activeSessionKey = null
    }

    fun release() {
        try {
            activeSession?.close()
        } catch (_: Throwable) {}
        activeSession = null
        activeSessionKey = null
    }

    private fun normalizeLangCode(code: String): String {
        val base = code.lowercase(Locale.ROOT).substringBefore("-").substringBefore("_")
        return if (SUPPORTED_PACKS.any { it.code == base }) base else "sa"
    }

    private fun scriptUnicodeBase(langCode: String): Int = when (langCode) {
        "sa", "hi", "mr" -> 0x0900 // Devanagari
        "bn" -> 0x0980             // Bengali
        "gu" -> 0x0A80             // Gujarati
        "ta" -> 0x0B80             // Tamil
        "te" -> 0x0C00             // Telugu
        "kn" -> 0x0C80             // Kannada
        else -> 0x0020             // Latin / Indian English
    }

    companion object {
        private const val TAG = "NinadaNeuralVoice"
        const val NINADA_SAMPLE_RATE = 24_000
        private const val MIN_VALID_ONNX_BYTES = 100_000L
        const val HF_BASE_RESOLVE_URL = "https://huggingface.co/gnumanth/ninada/resolve/main"

        /**
         * Complete Ninada 24 kHz Pan-Indic + Classical Sanskrit language pack catalog
         * matching Classical Sanskrit and every single language supported by UtsavSetu.
         */
        val SUPPORTED_PACKS: List<NinadaLanguagePack> = listOf(
            NinadaLanguagePack(
                code = "sa",
                appLanguage = null,
                displayName = "Classical Sanskrit (Mantras)",
                nativeLabel = "संस्कृतम्",
                bcp47Locale = "sa-IN",
                approxSizeMb = 22,
                samplePhrase = "ओम्, गं गणपतये नमः। शुभं भवतु।",
                isPrimarySacredLanguage = true
            ),
            NinadaLanguagePack(
                code = "hi",
                appLanguage = AppLanguage.HINDI,
                displayName = "Hindi (Ninada Neural)",
                nativeLabel = "हिन्दी",
                bcp47Locale = "hi-IN",
                approxSizeMb = 22,
                samplePhrase = "नमस्ते। आपका दिन शुभ और मंगलमय हो।"
            ),
            NinadaLanguagePack(
                code = "mr",
                appLanguage = AppLanguage.MARATHI,
                displayName = "Marathi (Ninada Neural)",
                nativeLabel = "मराठी",
                bcp47Locale = "mr-IN",
                approxSizeMb = 22,
                samplePhrase = "नमस्कार। तुमचा दिवस शुभ आणि मंगलमय जावो."
            ),
            NinadaLanguagePack(
                code = "gu",
                appLanguage = AppLanguage.GUJARATI,
                displayName = "Gujarati (Ninada Neural)",
                nativeLabel = "ગુજરાતી",
                bcp47Locale = "gu-IN",
                approxSizeMb = 22,
                samplePhrase = "નમસ્તે. તમારો દિવસ શુભ અને મંગલમય રહે."
            ),
            NinadaLanguagePack(
                code = "ta",
                appLanguage = AppLanguage.TAMIL,
                displayName = "Tamil (Ninada Neural)",
                nativeLabel = "தமிழ்",
                bcp47Locale = "ta-IN",
                approxSizeMb = 22,
                samplePhrase = "வணக்கம். உங்கள் நாள் இனிதாகவும் மங்களகரமாகவும் அமையட்டும்."
            ),
            NinadaLanguagePack(
                code = "te",
                appLanguage = AppLanguage.TELUGU,
                displayName = "Telugu (Ninada Neural)",
                nativeLabel = "తెలుగు",
                bcp47Locale = "te-IN",
                approxSizeMb = 22,
                samplePhrase = "నమస్కారం. మీ రోజు శుభప్రదంగా మరియు మంగళకరంగా ఉండాలి."
            ),
            NinadaLanguagePack(
                code = "kn",
                appLanguage = AppLanguage.KANNADA,
                displayName = "Kannada (Ninada Neural)",
                nativeLabel = "ಕನ್ನಡ",
                bcp47Locale = "kn-IN",
                approxSizeMb = 22,
                samplePhrase = "ನಮಸ್ಕಾರ. ನಿಮ್ಮ ದಿನ ಶುಭ ಮತ್ತು ಮಂಗಳಕರವಾಗಿರಲಿ."
            ),
            NinadaLanguagePack(
                code = "bn",
                appLanguage = AppLanguage.BENGALI,
                displayName = "Bengali (Ninada Neural)",
                nativeLabel = "বাংলা",
                bcp47Locale = "bn-IN",
                approxSizeMb = 22,
                samplePhrase = "নমস্কার। আপনার দিন শুভ এবং মঙ্গলময় হোক।"
            ),
            NinadaLanguagePack(
                code = "en",
                appLanguage = AppLanguage.ENGLISH,
                displayName = "Indian English (Ninada Neural)",
                nativeLabel = "English (India)",
                bcp47Locale = "en-IN",
                approxSizeMb = 22,
                samplePhrase = "Namaste. May your worship and festival celebration be auspicious."
            )
        )

        @Volatile
        private var instance: NinadaNeuralVoiceEngine? = null

        fun getInstance(context: Context): NinadaNeuralVoiceEngine {
            return instance ?: synchronized(this) {
                instance ?: NinadaNeuralVoiceEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
