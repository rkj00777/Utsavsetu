package com.example.platform.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import com.example.UtsavSetuApplication
import com.example.data.catalog.BundledCatalog
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.toDomain
import com.example.domain.model.Festival
import com.example.domain.model.FestivalOccurrence
import com.example.domain.model.MuhuratValidationState
import com.example.panchang.City
import com.example.panchang.FestivalDateEvaluator
import com.example.panchang.PanchangEngine
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val CHANNEL_ID = "utsavsetu_reminders"
    const val CHANNEL_NAME = "Sacred Festival & Muhurat Reminders"

    const val DAILY_CHANNEL_ID = "utsavsetu_daily_morning"
    const val DAILY_CHANNEL_NAME = "Daily Morning Panchang & Festivals"

    const val ACTION_DAILY_MORNING_PANCHANG = "com.example.ACTION_DAILY_MORNING_PANCHANG"
    const val DAILY_MORNING_REQUEST_CODE = 70007
    const val DAILY_MORNING_NOTIFICATION_ID = 70008
    const val DAILY_MORNING_WORK_NAME = "utsavsetu_daily_morning_work"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val festivalChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely reminders for upcoming festivals, puja vidhi, and auspicious muhurats."
                enableVibration(true)
            }
            manager.createNotificationChannel(festivalChannel)

            val dailyChannel = NotificationChannel(
                DAILY_CHANNEL_ID,
                DAILY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily morning festival and Muhurat timings computed locally for your selected city."
                enableVibration(true)
            }
            manager.createNotificationChannel(dailyChannel)
        }
    }

    fun hasPostNotificationsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun areNotificationsEnabledInSystem(context: Context): Boolean {
        return runCatching { NotificationManagerCompat.from(context).areNotificationsEnabled() }
            .getOrDefault(true)
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
        } else {
            true
        }
    }

    /**
     * Schedules (or cancels) the single daily morning Festival & Muhurat notification.
     * - Uses `AlarmManager.setExactAndAllowWhileIdle` when exact alarm access is permitted.
     * - Falls back cleanly to WorkManager (`OneTimeWorkRequest`) for approximate delivery when
     *   exact alarms are restricted on Android 12+.
     */
    fun scheduleDailyMorningNotification(
        context: Context,
        preferences: UserPreferences,
        nowInstant: Instant = Instant.now(),
        forceExactAlarmCapability: Boolean? = null
    ): SchedulingMode {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val city = preferences.hostCity
        val zoneId = DailyMorningNotificationEngine.resolveZoneId(city.timeZoneId)
        val todayLocal = nowInstant.atZone(zoneId).toLocalDate()

        if (!preferences.dailyMorningReminderEnabled) {
            cancelDailyMorningSchedule(context, alarmManager)
            DailyMorningNotificationEngine.saveScheduleMetadata(
                context = context,
                nextTriggerEpochMillis = null,
                mode = SchedulingMode.DISABLED,
                cityName = city.name,
                timeZoneId = zoneId.id
            )
            DailyMorningNotificationEngine.recordAuditEntry(
                context = context,
                entry = DailyDeliveryAuditEntry(
                    timestampMillis = nowInstant.toEpochMilli(),
                    localDate = todayLocal.toString(),
                    cityName = city.name,
                    timeZoneId = zoneId.id,
                    mode = SchedulingMode.DISABLED,
                    state = DeliveryAuditState.SCHEDULED,
                    detail = "Daily morning notifications disabled by user."
                )
            )
            return SchedulingMode.DISABLED
        }

        val nextTrigger = DailyMorningNotificationEngine.computeNextMorningTrigger(
            nowInstant = nowInstant,
            hour = preferences.dailyMorningHour,
            minute = preferences.dailyMorningMinute,
            zoneId = zoneId
        )
        val triggerEpochMillis = nextTrigger.toInstant().toEpochMilli()
        val delayMillis = Duration.between(nowInstant, nextTrigger.toInstant()).toMillis().coerceAtLeast(1_000L)

        val exactPermitted = forceExactAlarmCapability ?: canScheduleExactAlarms(context)
        val mode = if (exactPermitted && alarmManager != null) {
            cancelDailyMorningWorkManager(context)
            scheduleDailyAlarm(context, alarmManager, triggerEpochMillis)
            SchedulingMode.EXACT_ALARM
        } else {
            if (alarmManager != null) {
                cancelDailyAlarm(context, alarmManager)
            }
            scheduleDailyWorkManagerFallback(context, delayMillis)
            SchedulingMode.WORKMANAGER_APPROXIMATE
        }

        DailyMorningNotificationEngine.saveScheduleMetadata(
            context = context,
            nextTriggerEpochMillis = triggerEpochMillis,
            mode = mode,
            cityName = city.name,
            timeZoneId = zoneId.id
        )

        val timeFormatted = DailyMorningNotificationEngine.formatTimeOfDay(
            preferences.dailyMorningHour,
            preferences.dailyMorningMinute
        )
        val modeNote = if (mode == SchedulingMode.EXACT_ALARM) {
            "Exact alarm scheduled for ${nextTrigger.toLocalDate()} at $timeFormatted (${zoneId.id})."
        } else {
            "Approximate WorkManager delivery scheduled for ${nextTrigger.toLocalDate()} around $timeFormatted (${zoneId.id}); exact-time delivery is not guaranteed by Android when exact alarm access is unavailable."
        }

        DailyMorningNotificationEngine.recordAuditEntry(
            context = context,
            entry = DailyDeliveryAuditEntry(
                timestampMillis = nowInstant.toEpochMilli(),
                localDate = nextTrigger.toLocalDate().toString(),
                cityName = city.name,
                timeZoneId = zoneId.id,
                mode = mode,
                state = DeliveryAuditState.SCHEDULED,
                detail = modeNote
            )
        )

        return mode
    }

    /**
     * Computes fresh date- and city-specific festival & Muhurat timings locally offline,
     * posts the morning notification if permitted and not already posted today, records explicit
     * audit states (`ATTEMPTED` -> `POSTED` / `PERMISSION_BLOCKED` / `SKIPPED_DUPLICATE` / `FAILED`),
     * and schedules the next day's morning notification.
     */
    suspend fun deliverDailyMorningNotification(
        context: Context,
        mode: SchedulingMode,
        forceDiagnosticTest: Boolean = false,
        overrideDate: LocalDate? = null,
        overridePreferences: UserPreferences? = null,
        overrideFestivals: List<Festival>? = null,
        nowInstant: Instant = Instant.now()
    ): DeliveryAuditState {
        createNotificationChannel(context)

        val prefs = overridePreferences ?: loadCurrentPreferencesSafely(context)
        val city = prefs.hostCity
        val zoneId = DailyMorningNotificationEngine.resolveZoneId(city.timeZoneId)
        val targetDate = overrideDate ?: nowInstant.atZone(zoneId).toLocalDate()

        DailyMorningNotificationEngine.recordAuditEntry(
            context = context,
            entry = DailyDeliveryAuditEntry(
                timestampMillis = nowInstant.toEpochMilli(),
                localDate = targetDate.toString(),
                cityName = city.name,
                timeZoneId = zoneId.id,
                mode = mode,
                state = DeliveryAuditState.ATTEMPTED,
                detail = if (forceDiagnosticTest) {
                    "Diagnostic test notification attempted for ${city.name}."
                } else {
                    "Daily morning delivery attempted via ${mode.displayLabel}."
                }
            )
        )

        if (!forceDiagnosticTest && !prefs.dailyMorningReminderEnabled) {
            return DeliveryAuditState.FAILED
        }

        if (!hasPostNotificationsPermission(context) || !areNotificationsEnabledInSystem(context)) {
            DailyMorningNotificationEngine.recordAuditEntry(
                context = context,
                entry = DailyDeliveryAuditEntry(
                    timestampMillis = nowInstant.toEpochMilli(),
                    localDate = targetDate.toString(),
                    cityName = city.name,
                    timeZoneId = zoneId.id,
                    mode = mode,
                    state = DeliveryAuditState.PERMISSION_BLOCKED,
                    detail = "Blocked: POST_NOTIFICATIONS permission or Android system notifications disabled."
                )
            )
            if (!forceDiagnosticTest) {
                scheduleDailyMorningNotification(context, prefs, nowInstant.plusSeconds(60))
            }
            return DeliveryAuditState.PERMISSION_BLOCKED
        }

        if (!forceDiagnosticTest && DailyMorningNotificationEngine.hasAlreadyPostedFor(context, targetDate, city.id)) {
            DailyMorningNotificationEngine.recordAuditEntry(
                context = context,
                entry = DailyDeliveryAuditEntry(
                    timestampMillis = nowInstant.toEpochMilli(),
                    localDate = targetDate.toString(),
                    cityName = city.name,
                    timeZoneId = zoneId.id,
                    mode = mode,
                    state = DeliveryAuditState.SKIPPED_DUPLICATE,
                    detail = "Already posted for $targetDate in ${city.name}; skipped duplicate."
                )
            )
            scheduleDailyMorningNotification(context, prefs, nowInstant.plusSeconds(60))
            return DeliveryAuditState.SKIPPED_DUPLICATE
        }

        return try {
            val panchangDay = PanchangEngine.computeDayPanchang(targetDate, city)
            val allFestivals = overrideFestivals ?: loadFestivalsOfflineSafely(context)
            val traditionFiltered = allFestivals.filter { fest ->
                fest.traditions.any { prefs.selectedTraditions.contains(it) }
            }.ifEmpty { allFestivals }

            val todaysOccurrences = computeOccurrencesLocally(
                festivals = traditionFiltered,
                year = targetDate.year,
                city = city
            ).filter { it.date == targetDate }

            val payload = DailyMorningNotificationEngine.buildDailyPayload(
                date = targetDate,
                city = city,
                todaysOccurrences = todaysOccurrences,
                panchangDay = panchangDay
            )

            val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (payload.festivalId != null) {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("utsavsetu://festival?id=${payload.festivalId}")
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                DAILY_MORNING_NOTIFICATION_ID,
                deepLinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(payload.title)
                .setContentText(payload.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifId = if (forceDiagnosticTest) DAILY_MORNING_NOTIFICATION_ID + 1 else DAILY_MORNING_NOTIFICATION_ID
            notificationManager.notify(notifId, notification)

            if (!forceDiagnosticTest) {
                DailyMorningNotificationEngine.markPostedFor(context, targetDate, city.id)
                // Reschedule tomorrow's morning delivery strictly after current trigger instant
                scheduleDailyMorningNotification(context, prefs, nowInstant.plusSeconds(65))
            }

            DailyMorningNotificationEngine.recordAuditEntry(
                context = context,
                entry = DailyDeliveryAuditEntry(
                    timestampMillis = nowInstant.toEpochMilli(),
                    localDate = targetDate.toString(),
                    cityName = city.name,
                    timeZoneId = zoneId.id,
                    mode = mode,
                    state = DeliveryAuditState.POSTED,
                    detail = "Posted to Android notification tray: ${payload.title}"
                )
            )
            DeliveryAuditState.POSTED
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to build/post daily morning notification", e)
            DailyMorningNotificationEngine.recordAuditEntry(
                context = context,
                entry = DailyDeliveryAuditEntry(
                    timestampMillis = nowInstant.toEpochMilli(),
                    localDate = targetDate.toString(),
                    cityName = city.name,
                    timeZoneId = zoneId.id,
                    mode = mode,
                    state = DeliveryAuditState.FAILED,
                    detail = "Error during daily computation/posting: ${e.message ?: "Unknown error"}"
                )
            )
            if (!forceDiagnosticTest) {
                runCatching { scheduleDailyMorningNotification(context, prefs, nowInstant.plusSeconds(65)) }
            }
            DeliveryAuditState.FAILED
        }
    }

    /**
     * Builds an honest diagnostic snapshot of notification permissions, scheduling mode,
     * next scheduled delivery, and daily delivery audit history.
     */
    fun getDiagnosticsSnapshot(
        context: Context,
        preferences: UserPreferences
    ): NotificationDiagnosticsSnapshot {
        val postGranted = hasPostNotificationsPermission(context)
        val systemEnabled = areNotificationsEnabledInSystem(context)
        val exactAllowed = canScheduleExactAlarms(context)
        val storedMode = if (!preferences.dailyMorningReminderEnabled) {
            SchedulingMode.DISABLED
        } else {
            val saved = DailyMorningNotificationEngine.getStoredSchedulingMode(context)
            if (saved == SchedulingMode.DISABLED) {
                if (exactAllowed) SchedulingMode.EXACT_ALARM else SchedulingMode.WORKMANAGER_APPROXIMATE
            } else {
                saved
            }
        }

        val modeDescription = when (storedMode) {
            SchedulingMode.DISABLED ->
                "Daily morning notifications are turned off. Turn them on to schedule local festival and Muhurat alerts."
            SchedulingMode.EXACT_ALARM ->
                "Scheduled via Android Exact Alarm. Delivery still respects Do Not Disturb, Focus mode, and OEM battery restrictions."
            SchedulingMode.WORKMANAGER_APPROXIMATE ->
                "Scheduled via WorkManager approximate delivery fallback because Android exact alarm access is unavailable. Delivery time is approximate and not guaranteed at the exact minute; OS battery optimization and Do Not Disturb also apply."
        }
        val honestyNote = if (!postGranted || !systemEnabled) {
            "Notifications are currently blocked by Android permission or system settings. UtsavSetu cannot override OS notification permissions or Do Not Disturb. $modeDescription"
        } else {
            modeDescription
        }

        return NotificationDiagnosticsSnapshot(
            postNotificationsGranted = postGranted,
            notificationsEnabledInSystem = systemEnabled,
            canScheduleExactAlarms = exactAllowed,
            schedulingMode = storedMode,
            schedulingHonestyNote = honestyNote,
            nextTriggerEpochMillis = if (preferences.dailyMorningReminderEnabled) {
                DailyMorningNotificationEngine.getNextTriggerEpochMillis(context)
            } else null,
            dailyMorningEnabled = preferences.dailyMorningReminderEnabled,
            configuredHour = preferences.dailyMorningHour,
            configuredMinute = preferences.dailyMorningMinute,
            cityName = preferences.hostCity.name,
            timeZoneId = preferences.hostCity.timeZoneId,
            lastDeliveredKey = DailyMorningNotificationEngine.getLastPostedKey(context),
            auditEntries = DailyMorningNotificationEngine.getAuditEntries(context)
        )
    }

    /**
     * Unified rescheduler called on startup, location change, preference change, reboot,
     * time-zone change, clock change, or date change.
     */
    suspend fun rescheduleFromCurrentPreferences(context: Context) {
        val prefs = loadCurrentPreferencesSafely(context)
        val festivals = loadFestivalsOfflineSafely(context)
        val today = PanchangEngine.localDateForCity(prefs.hostCity)
        val currentYearOcc = computeOccurrencesLocally(festivals, today.year, prefs.hostCity)
        val nextYearOcc = computeOccurrencesLocally(festivals, today.year + 1, prefs.hostCity)
        rescheduleAllReminders(
            context = context,
            occurrences = currentYearOcc + nextYearOcc,
            preferences = prefs
        )
    }

    /**
     * Pure local occurrence & Muhurat evaluator backed strictly by [FestivalDateEvaluator]
     * and [PanchangEngine] (single source of truth).
     */
    fun computeOccurrencesLocally(
        festivals: List<Festival>,
        year: Int,
        city: City
    ): List<FestivalOccurrence> {
        val zoneId = DailyMorningNotificationEngine.resolveZoneId(city.timeZoneId)
        return festivals.map { festival ->
            val resolvedDate = FestivalDateEvaluator.resolveFestivalDate(
                festivalId = festival.id,
                year = year,
                city = city,
                rule = festival.dateRule
            )
            val muhuratWindow = PanchangEngine.computeFestivalMuhuratWindow(
                festivalId = festival.id,
                muhuratType = festival.muhuratType,
                dateRule = festival.dateRule,
                date = resolvedDate,
                city = city
            ).format()
            val parsedRange = MuhuratValidationState.parseTimeRange(muhuratWindow)
            val muhuratState = if (parsedRange != null) {
                MuhuratValidationState.createVerified(
                    festivalDate = resolvedDate,
                    cityId = city.id,
                    startTime = parsedRange.startTime,
                    endTime = parsedRange.endTime,
                    zoneId = zoneId,
                    verificationProvenance = "Drik Ganita / Lahiri Chitrapaksha Meeus Ephemeris",
                    formattedWindow = muhuratWindow
                )
            } else {
                MuhuratValidationState.resolve(muhuratWindow)
            }
            FestivalOccurrence(
                festival = festival,
                date = resolvedDate,
                muhuratWindowFormatted = muhuratWindow,
                muhuratState = muhuratState
            )
        }.sortedBy { it.date }
    }

    private suspend fun loadCurrentPreferencesSafely(context: Context): UserPreferences {
        return runCatching {
            val app = context.applicationContext as? UtsavSetuApplication
            if (app != null) {
                app.appContainer.userPreferencesRepository.userPreferencesFlow.first()
            } else {
                UserPreferencesRepository(context.applicationContext).userPreferencesFlow.first()
            }
        }.getOrElse { UserPreferences(hostCity = City.DEFAULT_CITY) }
    }

    private suspend fun loadFestivalsOfflineSafely(context: Context): List<Festival> {
        return runCatching {
            val app = context.applicationContext as? UtsavSetuApplication
            val fromRoom = app?.appContainer?.festivalRepository?.getAllFestivals()?.first().orEmpty()
            fromRoom.ifEmpty { BundledCatalog.FESTIVALS.map { it.toDomain() } }
        }.getOrElse { BundledCatalog.FESTIVALS.map { it.toDomain() } }
    }

    private fun scheduleDailyAlarm(context: Context, alarmManager: AlarmManager, triggerEpochMillis: Long) {
        val pendingIntent = buildDailyMorningPendingIntent(context)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w("ReminderScheduler", "Exact alarm security exception; falling back to WorkManager: ${e.message}")
            val delay = (triggerEpochMillis - System.currentTimeMillis()).coerceAtLeast(1_000L)
            scheduleDailyWorkManagerFallback(context, delay)
        } catch (e: Exception) {
            Log.w("ReminderScheduler", "Failed to schedule daily alarm: ${e.message}")
        }
    }

    private fun scheduleDailyWorkManagerFallback(context: Context, initialDelayMillis: Long) {
        runCatching {
            val request = OneTimeWorkRequestBuilder<DailyMorningWorker>()
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .addTag(DAILY_MORNING_WORK_NAME)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                DAILY_MORNING_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }.onFailure { e ->
            Log.w("ReminderScheduler", "WorkManager fallback scheduling skipped/failed: ${e.message}")
        }
    }

    private fun cancelDailyAlarm(context: Context, alarmManager: AlarmManager) {
        alarmManager.cancel(buildDailyMorningPendingIntent(context))
    }

    private fun cancelDailyMorningWorkManager(context: Context) {
        runCatching {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(DAILY_MORNING_WORK_NAME)
        }
    }

    private fun cancelDailyMorningSchedule(context: Context, alarmManager: AlarmManager?) {
        if (alarmManager != null) {
            cancelDailyAlarm(context, alarmManager)
        }
        cancelDailyMorningWorkManager(context)
    }

    private fun buildDailyMorningPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_DAILY_MORNING_PANCHANG
        }
        return PendingIntent.getBroadcast(
            context,
            DAILY_MORNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun parseTimeLabel(label: String, defaultTime: LocalTime = LocalTime.of(7, 0)): LocalTime {
        return try {
            val clean = label.substringBefore("(").trim().uppercase(Locale.US)
            if (clean.endsWith("AM") || clean.endsWith("PM")) {
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                val normalized = if (clean.indexOf(':') == 1) "0$clean" else clean
                LocalTime.parse(normalized, formatter)
            } else {
                val formatter24 = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
                val normalized = if (clean.indexOf(':') == 1) "0$clean" else clean
                LocalTime.parse(normalized, formatter24)
            }
        } catch (e: Exception) {
            defaultTime
        }
    }

    fun parseMuhuratStartTime(muhuratWindowFormatted: String): LocalTime {
        val startPart = muhuratWindowFormatted.substringBefore("-").trim()
        return parseTimeLabel(startPart, LocalTime.of(11, 0))
    }

    private const val PREFS_NAME = "utsavsetu_active_alarms"
    private const val KEY_ROLLING_CODES = "active_rolling_request_codes"
    private const val KEY_CUSTOM_CODES = "active_custom_request_codes"

    fun customReminderRequestCode(festivalId: String, triggerType: String): Int {
        return "custom_${festivalId}_$triggerType".hashCode()
    }

    fun rollingDayBeforeCode(festivalId: String): Int = (festivalId.hashCode() * 3) + 1
    fun rollingMorningCode(festivalId: String): Int = (festivalId.hashCode() * 3) + 2
    fun rollingMuhuratCode(festivalId: String): Int = (festivalId.hashCode() * 3) + 3

    fun getActiveRollingAlarmCodes(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ROLLING_CODES, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    fun getActiveCustomAlarmCodes(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_CUSTOM_CODES, emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    fun isAlarmScheduled(context: Context, requestCode: Int): Boolean {
        return getActiveRollingAlarmCodes(context).contains(requestCode) ||
            getActiveCustomAlarmCodes(context).contains(requestCode)
    }

    fun clearAllActiveAlarmsForTesting(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        DailyMorningNotificationEngine.clearAuditAndStateForTesting(context)
    }

    fun scheduleCustomReminder(
        context: Context,
        entry: CustomReminderEntry,
        festivalDate: LocalDate?,
        muhuratState: com.example.domain.model.MuhuratValidationState? = null,
        muhuratWindowFormatted: String? = null
    ): Boolean {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        val requestCode = customReminderRequestCode(entry.festivalId, entry.triggerType)

        // 1. Missing or unverified festival occurrence: do NOT guess fictitious dates!
        if (festivalDate == null) {
            Log.w("ReminderScheduler", "Cannot schedule reminder for ${entry.festivalId}: occurrence date is missing. Cancelling any stale alarm.")
            cancelCustomReminder(context, entry.festivalId, entry.triggerType)
            return false
        }

        val now = LocalDateTime.now()
        val chosenTime = parseTimeLabel(entry.timeLabel, LocalTime.of(7, 0))

        if (entry.triggerType == "MUHURAT_START") {
            // 2. Strict Muhurat validation: do NOT fabricate or schedule if unverified
            val validation = muhuratState ?: com.example.domain.model.MuhuratValidationState.resolve(muhuratWindowFormatted)
            if (!validation.isSchedulable || validation !is com.example.domain.model.MuhuratValidationState.Verified) {
                Log.w("ReminderScheduler", "Omission: Muhurat timing for ${entry.festivalId} is unverified ($muhuratWindowFormatted). Suppressing muhurat alarm and cancelling stale alarms.")
                cancelCustomReminder(context, entry.festivalId, entry.triggerType)
                return false
            }
            // Date-aware trigger calculation: accounts for midnight crossing
            val triggerDateTime = validation.startInstant.minusMinutes(30).toLocalDateTime()
            if (triggerDateTime.isAfter(now)) {
                scheduleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    triggerAt = triggerDateTime,
                    requestCode = requestCode,
                    festivalId = entry.festivalId,
                    title = "Puja Muhurat Starting Soon",
                    message = "${entry.festivalName} puja window (${validation.formattedWindow}) starts in 30 minutes."
                )
                recordCustomAlarmCode(context, requestCode)
                return true
            } else {
                cancelCustomReminder(context, entry.festivalId, entry.triggerType)
                return false
            }
        } else {
            // 3. General or user-selected reminder
            val triggerDateTime = when (entry.triggerType) {
                "THREE_DAYS_BEFORE" -> festivalDate.minusDays(3).atTime(chosenTime)
                "ONE_DAY_BEFORE" -> festivalDate.minusDays(1).atTime(chosenTime)
                "ON_DAY" -> festivalDate.atTime(chosenTime)
                else -> festivalDate.minusDays(1).atTime(chosenTime)
            }

            if (triggerDateTime.isAfter(now)) {
                scheduleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    triggerAt = triggerDateTime,
                    requestCode = requestCode,
                    festivalId = entry.festivalId,
                    title = "Reminder: ${entry.festivalName}",
                    message = "${entry.triggerDisplayLabel} • Observance: ${entry.dateFormatted}"
                )
                recordCustomAlarmCode(context, requestCode)
                return true
            } else {
                cancelCustomReminder(context, entry.festivalId, entry.triggerType)
                return false
            }
        }
    }

    fun scheduleCustomReminder(
        context: Context,
        entry: CustomReminderEntry,
        festivalDate: LocalDate?,
        muhuratWindowFormatted: String?
    ): Boolean {
        return scheduleCustomReminder(
            context = context,
            entry = entry,
            festivalDate = festivalDate,
            muhuratState = null,
            muhuratWindowFormatted = muhuratWindowFormatted
        )
    }

    fun cancelCustomReminder(
        context: Context,
        festivalId: String,
        triggerType: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggers = if (triggerType != null) {
            listOf(triggerType)
        } else {
            listOf("ON_DAY", "ONE_DAY_BEFORE", "THREE_DAYS_BEFORE", "MUHURAT_START")
        }
        for (type in triggers) {
            val code = customReminderRequestCode(festivalId, type)
            cancelAlarmByCode(context, alarmManager, code)
            removeCustomAlarmCode(context, code)
        }
    }

    fun rescheduleAllReminders(
        context: Context,
        occurrences: List<FestivalOccurrence>,
        preferences: UserPreferences
    ) {
        scheduleDailyMorningNotification(
            context = context,
            preferences = preferences
        )

        scheduleRollingWindow(
            context = context,
            occurrences = occurrences,
            dayBeforeEnabled = preferences.dayBeforeReminder,
            morningOfEnabled = preferences.morningOfReminder,
            muhuratStartEnabled = preferences.muhuratStartReminder
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val previousCustomCodes = getActiveCustomAlarmCodes(context)
        val newlyScheduledCustomCodes = mutableSetOf<Int>()
        val occurrenceById = occurrences.associateBy { it.festival.id }

        for (custom in preferences.customReminders) {
            val occ = occurrenceById[custom.festivalId]
            if (occ != null) {
                val scheduled = scheduleCustomReminder(
                    context = context,
                    entry = custom,
                    festivalDate = occ.date,
                    muhuratState = occ.muhuratState,
                    muhuratWindowFormatted = occ.muhuratWindowFormatted
                )
                if (scheduled) {
                    newlyScheduledCustomCodes.add(customReminderRequestCode(custom.festivalId, custom.triggerType))
                }
            } else {
                // Occurrence missing or deleted: actively cancel stale alarm!
                cancelCustomReminder(context, custom.festivalId, custom.triggerType)
            }
        }

        // Reconcile custom alarms: cancel any previously active custom codes that are no longer valid
        if (alarmManager != null) {
            for (staleCode in previousCustomCodes) {
                if (!newlyScheduledCustomCodes.contains(staleCode)) {
                    cancelAlarmByCode(context, alarmManager, staleCode)
                }
            }
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_CUSTOM_CODES, newlyScheduledCustomCodes.map { it.toString() }.toSet()).apply()
    }

    fun scheduleRollingWindow(
        context: Context,
        occurrences: List<FestivalOccurrence>,
        dayBeforeEnabled: Boolean,
        morningOfEnabled: Boolean,
        muhuratStartEnabled: Boolean
    ) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = LocalDateTime.now()
        val windowEnd = now.plusDays(60)

        val previousRollingCodes = getActiveRollingAlarmCodes(context)
        val newlyScheduledCodes = mutableSetOf<Int>()

        for (item in occurrences) {
            val festDate = item.date
            val fest = item.festival

            // 1. General Day-Before Festival Reminder (07:00 PM evening)
            val dayBeforeCode = rollingDayBeforeCode(fest.id)
            if (dayBeforeEnabled) {
                val dayBeforeTime = festDate.minusDays(1).atTime(19, 0)
                if (dayBeforeTime.isAfter(now) && dayBeforeTime.isBefore(windowEnd)) {
                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        triggerAt = dayBeforeTime,
                        requestCode = dayBeforeCode,
                        festivalId = fest.id,
                        title = "Tomorrow: ${fest.nameEnglish}",
                        message = "Prepare your puja samagri for tomorrow's sacred observance."
                    )
                    newlyScheduledCodes.add(dayBeforeCode)
                }
            }

            // 2. Morning-of Observance Reminder (06:30 AM) - Does NOT claim 06:30 AM is a muhurat
            val morningCode = rollingMorningCode(fest.id)
            if (morningOfEnabled) {
                val morningTime = festDate.atTime(6, 30)
                if (morningTime.isAfter(now) && morningTime.isBefore(windowEnd)) {
                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        triggerAt = morningTime,
                        requestCode = morningCode,
                        festivalId = fest.id,
                        title = "Today is ${fest.nameEnglish}",
                        message = "Shubh ${fest.nameNative}! Open UtsavSetu to check Panchang and available puja timings."
                    )
                    newlyScheduledCodes.add(morningCode)
                }
            }

            // 3. Muhurat-Start Reminder: STRICTLY scheduled only if Muhurat is Verified & Valid
            val muhuratCode = rollingMuhuratCode(fest.id)
            val muhuratState = item.muhuratState
            if (muhuratStartEnabled && muhuratState is com.example.domain.model.MuhuratValidationState.Verified && muhuratState.isValid) {
                val muhuratTime = muhuratState.startInstant.minusMinutes(30).toLocalDateTime()
                if (muhuratTime.isAfter(now) && muhuratTime.isBefore(windowEnd)) {
                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        triggerAt = muhuratTime,
                        requestCode = muhuratCode,
                        festivalId = fest.id,
                        title = "Puja Muhurat Starting Soon",
                        message = "${fest.nameEnglish} puja window (${muhuratState.formattedWindow}) starts in 30 minutes."
                    )
                    newlyScheduledCodes.add(muhuratCode)
                }
            }
        }

        // FULL ALARM RECONCILIATION:
        for (staleCode in previousRollingCodes) {
            if (!newlyScheduledCodes.contains(staleCode)) {
                cancelAlarmByCode(context, alarmManager, staleCode)
            }
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ROLLING_CODES, newlyScheduledCodes.map { it.toString() }.toSet()).apply()
    }

    private fun recordCustomAlarmCode(context: Context, requestCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_CUSTOM_CODES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(requestCode.toString())
        prefs.edit().putStringSet(KEY_CUSTOM_CODES, current).apply()
    }

    private fun removeCustomAlarmCode(context: Context, requestCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_CUSTOM_CODES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(requestCode.toString())
        prefs.edit().putStringSet(KEY_CUSTOM_CODES, current).apply()
    }

    private fun cancelAlarmByCode(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerAt: LocalDateTime,
        requestCode: Int,
        festivalId: String,
        title: String,
        message: String
    ) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("festival_id", festivalId)
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerEpochMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
            Log.d("ReminderScheduler", "Scheduled alarm for $festivalId at $triggerAt")
        } catch (e: Exception) {
            Log.w("ReminderScheduler", "Failed to schedule exact alarm: ${e.message}")
        }
    }
}
