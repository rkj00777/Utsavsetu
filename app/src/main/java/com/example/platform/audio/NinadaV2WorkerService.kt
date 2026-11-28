package com.example.platform.audio

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.DataOutputStream
import java.io.BufferedOutputStream

class NinadaV2WorkerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val engine by lazy { NinadaNeuralVoiceEngine.getInstance(applicationContext) }
    private val messenger by lazy { Messenger(Handler(Looper.getMainLooper()) { msg -> handle(msg); true }) }
    override fun onBind(intent: Intent?): IBinder = messenger.binder

    private fun handle(msg: Message) {
        val reply = msg.replyTo ?: return
        val d = msg.data ?: Bundle.EMPTY
        val id = d.getLong(ID)
        if (msg.what != SYNTH) { send(reply, id, "unsupported"); return }
        val t = d.getString(TEXT).orEmpty()
        val lang = d.getString(LANG).orEmpty()
        val gender = d.getString(GENDER).orEmpty()
        val speed = d.getFloat(SPEED, 1f)
        if (t.isBlank() || t.length > 4000 || lang !in LANGS || gender !in setOf("MALE", "FEMALE") || speed !in .5f..2f) {
            send(reply, id, "invalid"); return
        }
        scope.launch {
            val result = try { engine.synthesizePcm24k(t, lang, gender, speed) } catch (e: Throwable) { Log.e(TAG, "Synthesis exception lang=$lang id=$id", e); null }
            if (result == null) Log.w(TAG, "Synthesis returned null lang=$lang id=$id; check pack, tokenizer, and ONNX output")
            val fileName = result?.takeIf { it.sampleRate == 24000 && it.pcm16.isNotEmpty() && it.pcm16.size <= MAX_SAMPLES }?.let {
                try {
                    val dir = File(cacheDir, "ninada_worker_pcm").apply { mkdirs() }
                    val file = File(dir, "$id.pcm").apply { delete() }
                    DataOutputStream(BufferedOutputStream(file.outputStream())).use { out -> for (sample in it.pcm16) out.writeShort(sample.toInt()) }
                    file.name
                } catch (_: Throwable) { null }
            }
            val m = Message.obtain(null, RESULT)
            m.data = Bundle().apply {
                putLong(ID, id)
                if (fileName != null) {
                    putString(STATUS, "ok"); putInt(RATE, result!!.sampleRate)
                    putString(FILE, fileName); putString(LABEL, result.engineLabel)
                } else putString(STATUS, "fail")
            }
            try { reply.send(m) } catch (e: RemoteException) { Log.e(TAG, "IPC reply failed id=$id", e) }
        }
    }
    private fun send(reply: Messenger, id: Long, status: String) {
        val m = Message.obtain(null, RESULT); m.data = Bundle().apply { putLong(ID, id); putString(STATUS, status) }
        try { reply.send(m) } catch (_: RemoteException) {}
    }
    override fun onDestroy() { engine.release(); scope.cancel(); super.onDestroy() }
    companion object {
        private const val TAG = "NinadaV2Worker"
        const val SYNTH=100; const val RESULT=101; const val ID="id"; const val TEXT="text"; const val LANG="lang"
        const val GENDER="gender"; const val SPEED="speed"; const val STATUS="status"; const val RATE="rate"
        const val FILE="file"; const val LABEL="label"; const val MAX_SAMPLES=1_200_000
        val LANGS=setOf("sa","hi","mr","gu","ta","te","kn","bn","en")
    }
}
