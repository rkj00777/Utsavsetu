package com.example.platform.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager approximate delivery fallback used when Android 12+ exact alarm access
 * (`canScheduleExactAlarms()`) is unavailable. Computes today's festival & Muhurat content
 * offline via [ReminderScheduler.deliverDailyMorningNotification] and schedules tomorrow's run.
 */
class DailyMorningWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val outcome = ReminderScheduler.deliverDailyMorningNotification(
            context = applicationContext,
            mode = SchedulingMode.WORKMANAGER_APPROXIMATE
        )
        return when (outcome) {
            DeliveryAuditState.POSTED,
            DeliveryAuditState.SKIPPED_DUPLICATE,
            DeliveryAuditState.PERMISSION_BLOCKED -> Result.success()
            else -> Result.retry()
        }
    }
}
