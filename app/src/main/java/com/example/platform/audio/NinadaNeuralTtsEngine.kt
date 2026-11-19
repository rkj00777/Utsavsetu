package com.example.platform.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

/**
 * On-device Sanskrit neural speech using the Ninada VITS ONNX specialists.
 *
 * Model weights are downloaded on first use and retained in app-private storage.
 * Speech is synthesized locally after download; no mantra text is sent to a speech server.
 */
internal class NinadaNeuralTtsEngine(context: Context) {
    companion object {
        private const val SAMPLE_RATE = 24_000
        private const val MODEL_BASE =
            "https://huggingface.co/gnumanth/ninada/resolve/main/onnx/"
        private const val VOCAB_URL =
            "https://huggingface.co/gnumanth/ninada/resolve/main/vocabs.json"
        private const val MODEL_VERSION = "5.1.2"
        private const val MODEL_MIN_BYTES = 10_000_000L
    }

    private val appContext = context.applicationContext
    private val modelDir = File(appContext.filesDir, "ninada/$MODEL_VERSION")
    private val environment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val sessions = ConcurrentHashMap<String, OrtSession>()
    private val vocabMutex = Mutex()
    @Volatile private var charIds: Map<String, Int>? = null
    @Volatile private var blankId: Long = -1L
    @Volatile private var activeTrack: AudioTrack? = null

    suspend fun play(
        rawText: String,
        gender: String,
        speed: Float,
        onStarted: () -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val voice = if (gender.equals("FEMALE", true)) "female" else "male"
            val session = getSession(voice)
            val vocabulary = getVocabulary()
            val clauses = splitClauses(rawText)
            if (clauses.isEmpty()) return@withContext false

            val audio = ArrayList<Float>()
            clauses.forEachIndexed { index, clause ->
                coroutineContext.ensureActive()
                val ids = encode(clause, vocabulary)
                if (ids.isNotEmpty()) {
                    val input = OnnxTensor.createTensor(
                        environment,
                        LongBuffer.wrap(ids),
                        longArrayOf(1L, ids.size.toLong())
                    )
                    val scales = OnnxTensor.createTensor(
                        environment,
                        FloatBuffer.wrap(floatArrayOf(1.0f / speed.coerceIn(0.7f, 1.3f), 0.40f, 0.0f)),
                        longArrayOf(3L)
                    )
                    try {
                        session.run(mapOf("input_ids" to input, "scales" to scales)).use { result ->
                            val waveform = result[0] as? OnnxTensor
                                ?: error("Ninada returned an unsupported waveform tensor")
                            val buffer = waveform.floatBuffer
                            while (buffer.hasRemaining()) audio.add(buffer.get())
                        }
                    } finally {
                        input.close()
                        scales.close()
                    }
                }
                if (index < clauses.lastIndex) {
                    repeat((SAMPLE_RATE * 0.16f).toInt()) { audio.add(0f) }
                }
            }
            coroutineContext.ensureActive()
            if (audio.isEmpty()) return@withContext false

            onStarted()
            playPcm(audio.toFloatArray())
            true
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            stop()
            false
        }
    }

    fun stop() {
        val track = activeTrack
        activeTrack = null
        try {
            track?.pause()
            track?.flush()
            track?.stop()
        } catch (_: Throwable) {
        } finally {
            try { track?.release() } catch (_: Throwable) {}
        }
    }

    fun close() {
        stop()
        sessions.values.forEach { try { it.close() } catch (_: Throwable) {} }
        sessions.clear()
    }

    private fun playPcm(samples: FloatArray) {
        val minBytes = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(minBytes * 2)
            .build()
        activeTrack = track
        try {
            track.play()
            val block = ShortArray(4096)
            var offset = 0
            while (offset < samples.size) {
                if (activeTrack !== track) break
                val count = minOf(block.size, samples.size - offset)
                for (i in 0 until count) {
                    block[i] = (samples[offset + i].coerceIn(-1f, 1f) * 32767f)
                        .roundToInt().toShort()
                }
                var written = 0
                while (written < count && activeTrack === track) {
                    val n = track.write(block, written, count - written)
                    if (n < 0) error("AudioTrack write failed: $n")
                    written += n
                }
                offset += count
            }
            if (activeTrack === track) {
                track.stop()
                activeTrack = null
            }
        } finally {
            try { track.release() } catch (_: Throwable) {}
            if (activeTrack === track) activeTrack = null
        }
    }

    private suspend fun getSession(voice: String): OrtSession {
        sessions[voice]?.let { return it }
        val fileName = if (voice == "female") "sa_female.onnx" else "sa.onnx"
        val model = downloadIfNeeded(File(modelDir, fileName), "$MODEL_BASE$fileName", MODEL_MIN_BYTES)
        val options = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(4)
            setInterOpNumThreads(1)
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }
        val created = environment.createSession(model.absolutePath, options)
        options.close()
        val inputNames = created.inputNames
        require(inputNames.containsAll(setOf("input_ids", "scales"))) {
            "Unexpected Ninada model inputs: $inputNames"
        }
        val previous = sessions.putIfAbsent(voice, created)
        if (previous != null) {
            created.close()
            return previous
        }
        return created
    }

    private suspend fun getVocabulary(): Map<String, Int> {
        charIds?.let { return it }
        vocabMutex.lock()
        try {
            charIds?.let { return it }
            val vocabFile = downloadIfNeeded(
                File(modelDir, "vocabs.json"),
                VOCAB_URL,
                1_000L
            )
            val root = JSONObject(vocabFile.readText(Charsets.UTF_8))
            val sanskrit = root.getJSONObject("languages").getJSONObject("sa")
            val mapJson = sanskrit.getJSONObject("char_to_id")
            val map = buildMap {
                val keys = mapJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, mapJson.getInt(key))
                }
            }
            blankId = sanskrit.getLong("blank_id")
            charIds = map
            return map
        } finally {
            vocabMutex.unlock()
        }
    }

    private fun encode(text: String, vocabulary: Map<String, Int>): LongArray {
        var cleaned = Normalizer.normalize(text, Normalizer.Form.NFC).trim()
        cleaned = cleaned
            .replace(Regex("[\\u0951-\\u0954\\u1CD0-\\u1CFF]"), "")
            .replace(Regex("\\s*ऽ+"), "ऽ")
            .replace("||", "॥")
            .replace("|", "।")
            .replace("\u200C", "")
            .replace("\u200D", "")
            .replace("\uFEFF", "")
            .replace("“", "\"").replace("”", "\"")
            .replace("‘", "'").replace("’", "'")
            .replace("–", " ").replace("—", ", ")
            .replace("…", ".").replace(";", ",").replace(":", ",")
            .replace("-", " ")
            .replace(Regex("[<>\\[\\]\"()]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val raw = cleaned.mapNotNull { vocabulary[it.toString()] }
        if (raw.isEmpty()) return longArrayOf()
        val ids = LongArray(raw.size * 2 + 1)
        ids[0] = blankId
        raw.forEachIndexed { index, id ->
            ids[index * 2 + 1] = id.toLong()
            ids[index * 2 + 2] = blankId
        }
        return ids
    }

    private fun splitClauses(text: String): List<String> =
        text.replace(Regex("\\([A-Za-z0-9 .,:\\-]+\\)"), " ")
            .replace(Regex("[A-Za-z]+"), " ")
            .replace("॥", " ॥ ")
            .replace("।", " । ")
            .split(Regex("(?<=[.!?॥।])"))
            .map { it.trim() }
            .filter { it.any { ch -> ch in '\u0900'..'\u097F' } }

    private suspend fun downloadIfNeeded(file: File, url: String, minBytes: Long): File =
        withContext(Dispatchers.IO) {
            if (file.exists() && file.length() >= minBytes) return@withContext file
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, file.name + ".download")
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "UtsavSetu-Android")
            }
            try {
                connection.connect()
                require(connection.responseCode in 200..299) {
                    "Ninada download failed: HTTP ${connection.responseCode}"
                }
                BufferedInputStream(connection.inputStream).use { input ->
                    FileOutputStream(temp).use { output -> input.copyTo(output) }
                }
                require(temp.length() >= minBytes) { "Downloaded Ninada file is incomplete" }
                if (file.exists()) file.delete()
                check(temp.renameTo(file)) { "Could not finalize Ninada model download" }
                file
            } finally {
                connection.disconnect()
                if (temp.exists()) temp.delete()
            }
        }
}
