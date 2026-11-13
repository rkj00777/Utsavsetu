package com.example.platform.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.domain.model.FestivalOccurrence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReminderScheduler {

    const val CHANNEL_ID = "utsavsetu_reminders"
    const val CHANNEL_NAME = "Sacred Festival & Muhurat Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely reminders for upcoming festivals, puja vidhi, and auspicious muhurats."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
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
        // Actively cancel any alarm code that was previously scheduled but is no longer in the active set
        // (e.g. occurrence disappeared, date moved, timing became unverified, reminder disabled)
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
