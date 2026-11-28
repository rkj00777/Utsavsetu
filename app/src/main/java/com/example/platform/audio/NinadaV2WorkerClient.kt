package com.example.platform.audio

import android.content.*
import android.os.*
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.DataInputStream
import java.io.BufferedInputStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

class NinadaV2WorkerClient(private val context: Context) {
    suspend fun synthesize(text: String, language: String, gender: String, speed: Float): NinadaSynthesisResult? =
        suspendCancellableCoroutine { c ->
            val id = ids.incrementAndGet()
            var done = false
            var bound = false
            val handler = Handler(Looper.getMainLooper())
            lateinit var conn: ServiceConnection
            lateinit var timeout: Runnable
            fun cleanup() { if (bound) try { context.unbindService(conn) } catch (_: Throwable) {} }
            fun finish(v: NinadaSynthesisResult?) {
                if (done) return
                done = true
                handler.removeCallbacks(timeout)
                cleanup()
                if (c.isActive) c.resume(v)
            }
            timeout = Runnable { finish(null) }
            val reply = Messenger(Handler(Looper.getMainLooper()) { msg ->
                val d = msg.data
                if (msg.what == NinadaV2WorkerService.RESULT && d.getLong(NinadaV2WorkerService.ID) == id) {
                    val filename = d.getString(NinadaV2WorkerService.FILE).orEmpty()
                    val rate = d.getInt(NinadaV2WorkerService.RATE)
                    val label = d.getString(NinadaV2WorkerService.LABEL).orEmpty()
                    val status = d.getString(NinadaV2WorkerService.STATUS).orEmpty()
                    Log.i("NinadaV2Client", "Worker response id=$id status=$status rate=$rate file=$filename")
                    val result = if (status == "ok" && filename.isNotBlank() && rate == 24000) {
                        readPcmFile(filename)?.let { NinadaSynthesisResult(it, rate, language, label) }
                    } else null
                    if (result == null) Log.w("NinadaV2Client", "No PCM result for request id=$id")
                    finish(result)
                }
                true
            })
            conn = object : ServiceConnection {
                override fun onServiceConnected(n: ComponentName, b: IBinder) {
                    val m = Message.obtain(null, NinadaV2WorkerService.SYNTH)
                    m.replyTo = reply
                    m.data = Bundle().apply {
                        putLong(NinadaV2WorkerService.ID, id)
                        putString(NinadaV2WorkerService.TEXT, text)
                        putString(NinadaV2WorkerService.LANG, language)
                        putString(NinadaV2WorkerService.GENDER, gender)
                        putFloat(NinadaV2WorkerService.SPEED, speed)
                    }
                    try { Messenger(b).send(m) } catch (_: RemoteException) { finish(null) }
                }
                override fun onServiceDisconnected(n: ComponentName) { Log.e("NinadaV2Client", "Worker disconnected"); finish(null) }
                override fun onBindingDied(n: ComponentName) { Log.e("NinadaV2Client", "Worker binding died"); finish(null) }
                override fun onNullBinding(n: ComponentName) { Log.e("NinadaV2Client", "Worker null binding"); finish(null) }
            }
            try {
                bound = context.bindService(Intent(context, NinadaV2WorkerService::class.java), conn, Context.BIND_AUTO_CREATE)
                if (bound) handler.postDelayed(timeout, 45000) else finish(null)
            } catch (_: Throwable) { finish(null) }
            c.invokeOnCancellation { handler.removeCallbacks(timeout); cleanup() }
        }

    private fun readPcmFile(name: String): ShortArray? {
        val dir = File(context.cacheDir, "ninada_worker_pcm").canonicalFile
        val file = File(dir, name).canonicalFile
        if (!file.path.startsWith(dir.path + File.separator) || !file.exists()) return null
        try {
            val bytes = file.length()
            if (bytes <= 0 || bytes > MAX_PCM_BYTES || bytes % 2L != 0L) return null
            val out = ShortArray((bytes / 2).toInt())
            DataInputStream(BufferedInputStream(file.inputStream())).use { input ->
                for (i in out.indices) out[i] = input.readShort()
            }
            return out
        } catch (_: Throwable) { return null }
        finally { try { file.delete() } catch (_: Throwable) {} }
    }

    companion object {
        private val ids = AtomicLong()
        private const val MAX_PCM_BYTES = 2_400_000L
    }
}
