package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.panchang.City
import com.example.panchang.CityLookupResult
import com.example.panchang.RegionalPanchangConvention
import com.example.panchang.RegionalTradition
import com.example.platform.monitoring.AppCrashMonitor
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
    val dailyMorningReminderEnabled: Boolean = true,
    val dailyMorningHour: Int = 7,
    val dailyMorningMinute: Int = 0,
    val notificationRationaleShown: Boolean = false,
    val recentCities: List<City> = emptyList(),
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
        val CUSTOM_CITY_COUNTRY = stringPreferencesKey("custom_city_country")
        val CUSTOM_CITY_TIMEZONE = stringPreferencesKey("custom_city_timezone")
        val CUSTOM_CITY_CONVENTION = stringPreferencesKey("custom_city_convention")
        val CUSTOM_CITY_LAT = doublePreferencesKey("custom_city_lat")
        val CUSTOM_CITY_LON = doublePreferencesKey("custom_city_lon")
        val CUSTOM_CITY_IS_GPS = booleanPreferencesKey("custom_city_is_gps")
        val RECENT_CITIES = stringPreferencesKey("recent_cities_serialized")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val REMINDER_DAY_BEFORE = booleanPreferencesKey("reminder_day_before")
        val REMINDER_MORNING_OF = booleanPreferencesKey("reminder_morning_of")
        val REMINDER_MUHURAT_START = booleanPreferencesKey("reminder_muhurat_start")
        val DAILY_MORNING_ENABLED = booleanPreferencesKey("daily_morning_enabled")
        val DAILY_MORNING_HOUR = intPreferencesKey("daily_morning_hour")
        val DAILY_MORNING_MINUTE = intPreferencesKey("daily_morning_minute")
        val NOTIFICATION_RATIONALE_SHOWN = booleanPreferencesKey("notification_rationale_shown")
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

            val cityId = preferences[PreferencesKeys.HOST_CITY_ID] ?: City.DEFAULT_CITY.id
            val customName = preferences[PreferencesKeys.CUSTOM_CITY_NAME]
            val customState = preferences[PreferencesKeys.CUSTOM_CITY_STATE]
            val customCountry = preferences[PreferencesKeys.CUSTOM_CITY_COUNTRY]
            val customTimeZone = preferences[PreferencesKeys.CUSTOM_CITY_TIMEZONE]
            val customConventionCode = preferences[PreferencesKeys.CUSTOM_CITY_CONVENTION]
            val customLat = preferences[PreferencesKeys.CUSTOM_CITY_LAT]
            val customLon = preferences[PreferencesKeys.CUSTOM_CITY_LON]
            val isGps = preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] ?: false

            val knownCity = City.findById(cityId)
            val city = when {
                customName != null && customLat != null && customLon != null && (knownCity == null || isGps) -> {
                    val nearest = City.findNearestCity(customLat, customLon)
                    val resolvedCountry = customCountry?.takeIf { it.isNotBlank() }
                        ?: knownCity?.country
                        ?: nearest.country
                    val resolvedTz = customTimeZone?.takeIf { it.isNotBlank() }
                        ?: knownCity?.timeZoneId
                        ?: City.resolveTimeZoneForCoordinates(customLat, customLon, resolvedCountry)
                    val resolvedConvention = RegionalPanchangConvention.values()
                        .firstOrNull { it.code == customConventionCode }
                        ?: knownCity?.panchangConvention
                        ?: if (resolvedCountry.equals("India", ignoreCase = true)) {
                            nearest.panchangConvention
                        } else {
                            RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR
                        }
                    City(
                        id = cityId,
                        name = customName,
                        state = customState ?: knownCity?.state ?: nearest.state,
                        country = resolvedCountry,
                        latitude = customLat,
                        longitude = customLon,
                        timeZoneId = resolvedTz,
                        isLocationDetected = isGps,
                        panchangConvention = resolvedConvention
                    )
                }
                knownCity != null -> knownCity
                else -> City.DEFAULT_CITY
            }

            val recentCities = deserializeRecentCities(preferences[PreferencesKeys.RECENT_CITIES])

            val langCode = preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "en"
            val language = AppLanguage.fromCode(langCode)

            val dayBefore = preferences[PreferencesKeys.REMINDER_DAY_BEFORE] ?: true
            val morningOf = preferences[PreferencesKeys.REMINDER_MORNING_OF] ?: true
            val muhuratStart = preferences[PreferencesKeys.REMINDER_MUHURAT_START] ?: true
            // Backward-compatible migration: default dailyMorningEnabled to existing morningOf preference
            val dailyMorningEnabled = preferences[PreferencesKeys.DAILY_MORNING_ENABLED] ?: morningOf
            val dailyMorningHour = (preferences[PreferencesKeys.DAILY_MORNING_HOUR] ?: 7).coerceIn(0, 23)
            val dailyMorningMinute = (preferences[PreferencesKeys.DAILY_MORNING_MINUTE] ?: 0).coerceIn(0, 59)
            val rationaleShown = preferences[PreferencesKeys.NOTIFICATION_RATIONALE_SHOWN] ?: false

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
                dailyMorningReminderEnabled = dailyMorningEnabled,
                dailyMorningHour = dailyMorningHour,
                dailyMorningMinute = dailyMorningMinute,
                notificationRationaleShown = rationaleShown,
                recentCities = recentCities,
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

    suspend fun updateHostCity(cityId: String): Boolean {
        return when (val lookup = City.lookupById(cityId)) {
            is CityLookupResult.Found -> {
                updateHostCityObject(lookup.city)
                true
            }
            is CityLookupResult.Unknown -> {
                AppCrashMonitor.recordBreadcrumb("CityLookup", "Ignored updateHostCity for unknown cityId: ${lookup.requestedId}")
                false
            }
        }
    }

    suspend fun updateHostCityObject(city: City) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOST_CITY_ID] = city.id
            preferences[PreferencesKeys.CUSTOM_CITY_NAME] = city.name
            preferences[PreferencesKeys.CUSTOM_CITY_STATE] = city.state
            preferences[PreferencesKeys.CUSTOM_CITY_COUNTRY] = city.country
            preferences[PreferencesKeys.CUSTOM_CITY_TIMEZONE] = city.timeZoneId
            preferences[PreferencesKeys.CUSTOM_CITY_CONVENTION] = city.panchangConvention.code
            preferences[PreferencesKeys.CUSTOM_CITY_LAT] = city.latitude
            preferences[PreferencesKeys.CUSTOM_CITY_LON] = city.longitude
            preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] = city.isLocationDetected

            val existingRecents = deserializeRecentCities(preferences[PreferencesKeys.RECENT_CITIES])
            val updatedRecents = (listOf(city) + existingRecents.filterNot {
                it.id == city.id || (it.name.equals(city.name, ignoreCase = true) && it.state.equals(city.state, ignoreCase = true))
            }).take(MAX_RECENT_CITIES)
            preferences[PreferencesKeys.RECENT_CITIES] = serializeRecentCities(updatedRecents)
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

    suspend fun updateDailyMorningSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_MORNING_ENABLED] = enabled
            preferences[PreferencesKeys.DAILY_MORNING_HOUR] = hour.coerceIn(0, 23)
            preferences[PreferencesKeys.DAILY_MORNING_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun markNotificationRationaleShown() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_RATIONALE_SHOWN] = true
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
            ?: throw IllegalArgumentException("Unknown city ID in completeSetup: '$cityId'")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TRADITIONS] = traditions.map { it.id }.toSet()
            preferences[PreferencesKeys.HOST_CITY_ID] = resolvedCity.id
            preferences[PreferencesKeys.CUSTOM_CITY_NAME] = resolvedCity.name
            preferences[PreferencesKeys.CUSTOM_CITY_STATE] = resolvedCity.state
            preferences[PreferencesKeys.CUSTOM_CITY_COUNTRY] = resolvedCity.country
            preferences[PreferencesKeys.CUSTOM_CITY_TIMEZONE] = resolvedCity.timeZoneId
            preferences[PreferencesKeys.CUSTOM_CITY_CONVENTION] = resolvedCity.panchangConvention.code
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
                when (val lookup = City.lookupById(hostCityId)) {
                    is CityLookupResult.Found -> {
                        val c = lookup.city
                        preferences[PreferencesKeys.HOST_CITY_ID] = c.id
                        preferences[PreferencesKeys.CUSTOM_CITY_NAME] = c.name
                        preferences[PreferencesKeys.CUSTOM_CITY_STATE] = c.state
                        preferences[PreferencesKeys.CUSTOM_CITY_COUNTRY] = c.country
                        preferences[PreferencesKeys.CUSTOM_CITY_TIMEZONE] = c.timeZoneId
                        preferences[PreferencesKeys.CUSTOM_CITY_CONVENTION] = c.panchangConvention.code
                        preferences[PreferencesKeys.CUSTOM_CITY_LAT] = c.latitude
                        preferences[PreferencesKeys.CUSTOM_CITY_LON] = c.longitude
                        preferences[PreferencesKeys.CUSTOM_CITY_IS_GPS] = false
                    }
                    is CityLookupResult.Unknown -> {
                        AppCrashMonitor.recordBreadcrumb(
                            "CloudRestore",
                            "Skipped unknown hostCityId '${lookup.requestedId}' during cloud restore"
                        )
                    }
                }
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

    companion object {
        private const val MAX_RECENT_CITIES = 5
        private const val FIELD_SEP = "\u001F"
        private const val RECORD_SEP = "\u001E"

        internal fun serializeRecentCities(cities: List<City>): String {
            return cities.take(MAX_RECENT_CITIES).joinToString(RECORD_SEP) { city ->
                listOf(
                    city.id,
                    city.name.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                    city.state.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                    city.country.replace(FIELD_SEP, " ").replace(RECORD_SEP, " "),
                    city.latitude.toString(),
                    city.longitude.toString(),
                    city.timeZoneId,
                    city.isLocationDetected.toString(),
                    city.panchangConvention.code
                ).joinToString(FIELD_SEP)
            }
        }

        internal fun deserializeRecentCities(raw: String?): List<City> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(RECORD_SEP).mapNotNull { record ->
                val parts = record.split(FIELD_SEP)
                if (parts.size < 9) return@mapNotNull null
                val id = parts[0]
                val isGps = parts[7].toBooleanStrictOrNull() ?: false
                val known = City.findById(id)
                if (known != null && !isGps) {
                    known
                } else {
                    val lat = parts[4].toDoubleOrNull() ?: return@mapNotNull null
                    val lon = parts[5].toDoubleOrNull() ?: return@mapNotNull null
                    val convention = RegionalPanchangConvention.values()
                        .firstOrNull { it.code == parts[8] }
                        ?: RegionalPanchangConvention.DIASPORA_LOCAL_SOLAR
                    City(
                        id = id,
                        name = parts[1],
                        state = parts[2],
                        country = parts[3],
                        latitude = lat,
                        longitude = lon,
                        timeZoneId = parts[6],
                        isLocationDetected = isGps,
                        panchangConvention = convention
                    )
                }
            }.take(MAX_RECENT_CITIES)
        }
    }
}
