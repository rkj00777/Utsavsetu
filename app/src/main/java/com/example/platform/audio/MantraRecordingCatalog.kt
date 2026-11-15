package com.example.platform.audio

import android.content.Context
import org.json.JSONObject

/** Resolves optional, human-recorded mantra audio before synthesized speech. */
internal object MantraRecordingCatalog {
    private const val CATALOG_FILE = "mantra_recordings.json"

    fun findResourceId(context: Context, mantraId: String, voiceGender: String): Int {
        return try {
            val json = context.assets.open(CATALOG_FILE).bufferedReader().use { it.readText() }
            val entry = JSONObject(json).optJSONObject(mantraId) ?: return 0
            val key = if (voiceGender.equals("FEMALE", ignoreCase = true)) "female" else "male"
            val name = entry.optString(key).takeIf { it.isNotBlank() }
                ?: entry.optString("default").takeIf { it.isNotBlank() }
                ?: return 0
            context.resources.getIdentifier(name, "raw", context.packageName)
        } catch (_: Exception) { 0 }
    }
}
