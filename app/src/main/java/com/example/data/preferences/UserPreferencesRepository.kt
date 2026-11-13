package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "utsavsetu_preferences")

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val sampleGreeting: String
) {
    ENGLISH("en", "English", "English", "Namaste · Shubh Din"),
    HINDI("hi", "हिन्दी", "Hindi", "नमस्ते · शुभ दिन"),
    MARATHI("mr", "मराठी", "Marathi", "नमस्कार · शुभ दिवस"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "નમસ્તે · શુભ દિવસ"),
    TAMIL("ta", "தமிழ்", "Tamil", "வணக்கம் · நல்வரவு"),
    TELUGU("te", "తెలుగు", "Telugu", "నమస్కారం · శుభ దినం"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "ನಮಸ್ಕಾರ · ಶುಭ ದಿನ"),
    BENGALI("bn", "বাংলা", "Bengali", "নমস্কার · শুভ দিন");

    companion object {
        fun fromCode(code: String): AppLanguage =
            values().firstOrNull { it.code == code } ?: ENGLISH
    }
}

data class CustomReminderEntry(
    val festivalId: String,
    val festivalName: String,
    val dateFormatted: String,
    val triggerType: String, // "ON_DAY", "ONE_DAY_BEFORE", "THREE_DAYS_BEFORE", "MUHURAT_START"
    val timeLabel: String    // e.g. "07:00 AM", "08:00 AM", "06:00 PM"
) {
    fun serialize(): String = "$festivalId|$festivalName|$dateFormatted|$triggerType|$timeLabel"

    val triggerDisplayLabel: String
        get() = when (triggerType) {
            "ON_DAY" -> "On the festival day"
            "ONE_DAY_BEFORE" -> "One day before"
            "THREE_DAYS_BEFORE" -> "3 days before"
            "MUHURAT_START" -> "At Puja Muhurat start"
            else -> "One day before"
        }

    companion object {
        fun deserialize(raw: String): CustomReminderEntry? {
            val parts = raw.split("|")
            if (parts.size < 5) return null
            return CustomReminderEntry(
                festivalId = parts[0],
                festivalName = parts[1],
                dateFormatted = parts[2],
                triggerType = parts[3],
                timeLabel = parts[4]
            )
        }
    }
}

data class UserPreferences(
    val selectedTraditions: Set<RegionalTradition> = setOf(RegionalTradition.NORTH_INDIAN),
    val hostCity: City = City.DELHI,
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
    val dayBeforeReminder: Boolean = true,
    val morningOfReminder: Boolean = true,
    val muhuratStartReminder: Boolean = false,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val textScaleMode: String = "NORMAL", // "NORMAL", "LARGE"
    val hasCompletedSetup: Boolean = true,
    val dpdpConsentGiven: Boolean = true,
    val savedFestivalIds: Set<String> = emptySet(),
    val customReminders: List<CustomReminderEntry> = emptyList(),
    val pujaStepProgress: Map<String, Int> = emptyMap(),
    val appMode: String = "CONSUMER",
    val panditVoiceGender: String = "MALE"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_TRADITIONS = stringSetPreferencesKey("selected_traditions")
        val HOST_CITY_ID = stringPreferencesKey("host_city_id")
        val CUSTOM_CITY_NAME = stringPreferencesKey("custom_city_name")
        val CUSTOM_CITY_STATE = stringPreferencesKey("custom_city_state")
        val CUSTOM_CITY_LAT = doublePreferencesKey("custom_city_lat")
        val CUSTOM_CITY_LON = doublePreferencesKey("custom_city_lon")
        val CUSTOM_CITY_IS_GPS = booleanPreferencesKey("custom_city_is_gps")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val REMINDER_DAY_BEFORE = booleanPreferencesKey("reminder_day_before")
        val REMINDER_MORNING_OF = booleanPreferencesKey("reminder_morning_of")
        val REMINDER_MUHURAT_START = booleanPreferencesKey("reminder_muhurat_start")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SCALE_MODE = stringPreferencesKey("text_scale_mode")
        val HAS_COMPLETED_SETUP = booleanPreferencesKey("has_completed_setup")
        val DPDP_CONSENT = booleanPreferencesKey("dpdp_consent")
        val SAVED_FESTIVAL_IDS = stringSetPreferencesKey("saved_festival_ids")
        val CUSTOM_REMINDERS = stringSetPreferencesKey("custom_reminders")
        val PUJA_STEP_PROGRESS = stringSetPreferencesKey("puja_step_progress")
        val APP_MODE = stringPreferencesKey("app_mode")
        val PANDIT_VOICE_GENDER = stringPreferencesKey("pandit_voice_gender")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val traditionIds = preferences[PreferencesKeys.SELECTED_TRADITIONS] ?: setOf(
                RegionalTradition.NORTH_INDIAN.id,
                RegionalTradition.MAHARASHTRIAN.id
            )
            val traditions = traditionIds.mapNotNull { id ->
                RegionalTradition.values().firstOrNull { it.id == id }
            }.toSet().ifEmpty { setOf(RegionalTradition.NORTH_INDIAN) }

            val cityId = preferences[PreferencesKeys.HOST_CITY_ID] ?: "delhi"
            val customName = preferences[PreferencesKeys.CUSTOM_CITY_NAME]
            val customState = preferences[PreferencesKeys.CUSTOM_CITY_STATE]
            val customLat = preferences[PreferencesKeys.CUSTOM_CITY_LAT]
            val customLon = preferences[PreferencesKeys.CUSTOM_CITY_LON]
            val isGps = preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] ?: false

            val knownCity = City.ALL_CITIES.firstOrNull { it.id == cityId }
            val city = when {
                customName != null && customLat != null && customLon != null && (knownCity == null || isGps) -> {
                    City(
                        id = cityId,
                        name = customName,
                        state = customState ?: "India",
                        latitude = customLat,
                        longitude = customLon,
                        isLocationDetected = isGps
                    )
                }
                knownCity != null -> knownCity
                else -> City.DELHI
            }

            val langCode = preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "en"
            val language = AppLanguage.fromCode(langCode)

            val dayBefore = preferences[PreferencesKeys.REMINDER_DAY_BEFORE] ?: true
            val morningOf = preferences[PreferencesKeys.REMINDER_MORNING_OF] ?: true
            val muhuratStart = preferences[PreferencesKeys.REMINDER_MUHURAT_START] ?: true
            val theme = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
            val textScale = preferences[PreferencesKeys.TEXT_SCALE_MODE] ?: "NORMAL"
            val completedSetup = preferences[PreferencesKeys.HAS_COMPLETED_SETUP] ?: false
            val dpdpConsent = preferences[PreferencesKeys.DPDP_CONSENT] ?: false
            val savedIds = preferences[PreferencesKeys.SAVED_FESTIVAL_IDS] ?: emptySet()
            val rawReminders = preferences[PreferencesKeys.CUSTOM_REMINDERS] ?: emptySet()
            val reminders = rawReminders.mapNotNull { CustomReminderEntry.deserialize(it) }

            val rawStepProgress = preferences[PreferencesKeys.PUJA_STEP_PROGRESS] ?: emptySet()
            val stepProgressMap = rawStepProgress.mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val step = parts[1].toIntOrNull()
                    if (step != null) parts[0] to step else null
                } else null
            }.toMap()

            val appMode = preferences[PreferencesKeys.APP_MODE] ?: "CONSUMER"
            val voiceGender = preferences[PreferencesKeys.PANDIT_VOICE_GENDER] ?: "MALE"

            UserPreferences(
                selectedTraditions = traditions,
                hostCity = city,
                preferredLanguage = language,
                dayBeforeReminder = dayBefore,
                morningOfReminder = morningOf,
                muhuratStartReminder = muhuratStart,
                themeMode = theme,
                textScaleMode = textScale,
                hasCompletedSetup = completedSetup,
                dpdpConsentGiven = dpdpConsent,
                savedFestivalIds = savedIds,
                customReminders = reminders,
                pujaStepProgress = stepProgressMap,
                appMode = appMode,
                panditVoiceGender = voiceGender
            )
        }

    suspend fun updateTraditions(traditions: Set<RegionalTradition>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TRADITIONS] = traditions.map { it.id }.toSet()
        }
    }

    suspend fun updateHostCity(cityId: String) {
        val known = City.findById(cityId)
        updateHostCityObject(known)
    }

    suspend fun updateHostCityObject(city: City) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOST_CITY_ID] = city.id
            preferences[PreferencesKeys.CUSTOM_CITY_NAME] = city.name
            preferences[PreferencesKeys.CUSTOM_CITY_STATE] = city.state
            preferences[PreferencesKeys.CUSTOM_CITY_LAT] = city.latitude
            preferences[PreferencesKeys.CUSTOM_CITY_LON] = city.longitude
            preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] = city.isLocationDetected
        }
    }

    suspend fun updateLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = languageCode
        }
    }

    suspend fun updateReminders(dayBefore: Boolean, morningOf: Boolean, muhuratStart: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_DAY_BEFORE] = dayBefore
            preferences[PreferencesKeys.REMINDER_MORNING_OF] = morningOf
            preferences[PreferencesKeys.REMINDER_MUHURAT_START] = muhuratStart
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    suspend fun updateTextScaleMode(scaleMode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_SCALE_MODE] = scaleMode
        }
    }

    suspend fun updatePanditVoiceGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PANDIT_VOICE_GENDER] = gender
        }
    }

    suspend fun toggleSavedFestival(festivalId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SAVED_FESTIVAL_IDS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(festivalId)) {
                current.remove(festivalId)
            } else {
                current.add(festivalId)
            }
            preferences[PreferencesKeys.SAVED_FESTIVAL_IDS] = current
        }
    }

    suspend fun upsertCustomReminder(entry: CustomReminderEntry) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_REMINDERS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { raw ->
                val parsed = CustomReminderEntry.deserialize(raw)
                parsed != null && parsed.festivalId == entry.festivalId && parsed.triggerType == entry.triggerType
            }
            current.add(entry.serialize())
            preferences[PreferencesKeys.CUSTOM_REMINDERS] = current
        }
    }

    suspend fun deleteCustomReminder(festivalId: String, triggerType: String? = null) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_REMINDERS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { raw ->
                val parsed = CustomReminderEntry.deserialize(raw)
                parsed != null && parsed.festivalId == festivalId &&
                    (triggerType == null || parsed.triggerType == triggerType)
            }
            preferences[PreferencesKeys.CUSTOM_REMINDERS] = current
        }
    }

    suspend fun savePujaStepProgress(festivalId: String, stepIndex: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PUJA_STEP_PROGRESS]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$festivalId:") }
            if (stepIndex > 0) {
                current.add("$festivalId:$stepIndex")
            }
            preferences[PreferencesKeys.PUJA_STEP_PROGRESS] = current
        }
    }

    suspend fun completeSetup(
        traditions: Set<RegionalTradition>,
        cityId: String,
        dpdpConsent: Boolean,
        languageCode: String = "en",
        city: City? = null
    ) {
        val resolvedCity = city ?: City.findById(cityId)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TRADITIONS] = traditions.map { it.id }.toSet()
            preferences[PreferencesKeys.HOST_CITY_ID] = resolvedCity.id
            preferences[PreferencesKeys.CUSTOM_CITY_NAME] = resolvedCity.name
            preferences[PreferencesKeys.CUSTOM_CITY_STATE] = resolvedCity.state
            preferences[PreferencesKeys.CUSTOM_CITY_LAT] = resolvedCity.latitude
            preferences[PreferencesKeys.CUSTOM_CITY_LON] = resolvedCity.longitude
            preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] = resolvedCity.isLocationDetected
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = languageCode
            preferences[PreferencesKeys.DPDP_CONSENT] = dpdpConsent
            preferences[PreferencesKeys.HAS_COMPLETED_SETUP] = true
        }
    }

    suspend fun restoreFromCloud(
        traditionIds: List<String>?,
        hostCityId: String?,
        preferredLanguageCode: String?,
        dayBeforeReminder: Boolean?,
        morningOfReminder: Boolean?,
        muhuratStartReminder: Boolean?,
        themeMode: String?,
        textScaleMode: String?,
        savedFestivalIds: List<String>?,
        customRemindersRaw: List<String>?,
        pujaStepProgressMap: Map<String, Int>?,
        appMode: String?,
        panditVoiceGender: String? = null
    ) {
        context.dataStore.edit { preferences ->
            if (!traditionIds.isNullOrEmpty()) {
                preferences[PreferencesKeys.SELECTED_TRADITIONS] = traditionIds.toSet()
            }
            if (!hostCityId.isNullOrBlank()) {
                val c = City.findById(hostCityId)
                preferences[PreferencesKeys.HOST_CITY_ID] = c.id
                preferences[PreferencesKeys.CUSTOM_CITY_NAME] = c.name
                preferences[PreferencesKeys.CUSTOM_CITY_STATE] = c.state
                preferences[PreferencesKeys.CUSTOM_CITY_LAT] = c.latitude
                preferences[PreferencesKeys.CUSTOM_CITY_LON] = c.longitude
                preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] = false
            }
            if (!preferredLanguageCode.isNullOrBlank()) {
                preferences[PreferencesKeys.PREFERRED_LANGUAGE] = preferredLanguageCode
            }
            if (dayBeforeReminder != null) {
                preferences[PreferencesKeys.REMINDER_DAY_BEFORE] = dayBeforeReminder
            }
            if (morningOfReminder != null) {
                preferences[PreferencesKeys.REMINDER_MORNING_OF] = morningOfReminder
            }
            if (muhuratStartReminder != null) {
                preferences[PreferencesKeys.REMINDER_MUHURAT_START] = muhuratStartReminder
            }
            if (!themeMode.isNullOrBlank()) {
                preferences[PreferencesKeys.THEME_MODE] = themeMode
            }
            if (!textScaleMode.isNullOrBlank()) {
                preferences[PreferencesKeys.TEXT_SCALE_MODE] = textScaleMode
            }
            if (savedFestivalIds != null) {
                val mergedSaved = (preferences[PreferencesKeys.SAVED_FESTIVAL_IDS] ?: emptySet()) + savedFestivalIds.toSet()
                preferences[PreferencesKeys.SAVED_FESTIVAL_IDS] = mergedSaved
            }
            if (customRemindersRaw != null) {
                val mergedReminders = (preferences[PreferencesKeys.CUSTOM_REMINDERS] ?: emptySet()) + customRemindersRaw.toSet()
                preferences[PreferencesKeys.CUSTOM_REMINDERS] = mergedReminders
            }
            if (pujaStepProgressMap != null) {
                val current = preferences[PreferencesKeys.PUJA_STEP_PROGRESS]?.toMutableSet() ?: mutableSetOf()
                for ((festId, stepIdx) in pujaStepProgressMap) {
                    current.removeAll { it.startsWith("$festId:") }
                    if (stepIdx > 0) {
                        current.add("$festId:$stepIdx")
                    }
                }
                preferences[PreferencesKeys.PUJA_STEP_PROGRESS] = current
            }
            if (!appMode.isNullOrBlank()) {
                preferences[PreferencesKeys.APP_MODE] = appMode
            }
            if (!panditVoiceGender.isNullOrBlank() && (panditVoiceGender == "MALE" || panditVoiceGender == "FEMALE")) {
                preferences[PreferencesKeys.PANDIT_VOICE_GENDER] = panditVoiceGender
            }
            preferences[PreferencesKeys.HAS_COMPLETED_SETUP] = true
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
