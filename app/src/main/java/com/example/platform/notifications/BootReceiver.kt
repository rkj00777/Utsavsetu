package com.example.platform.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.UtsavSetuApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? UtsavSetuApplication ?: return
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val prefs = app.appContainer.userPreferencesRepository.userPreferencesFlow.first()
                val festivals = app.appContainer.festivalRepository.getAllFestivals().first()
                val year = LocalDate.now().year
                val occurrences = app.appContainer.festivalRepository.computeOccurrencesForYear(
                    festivals = festivals,
                    year = year,
                    city = prefs.hostCity
                )

                ReminderScheduler.rescheduleAllReminders(
                    context = context,
                    occurrences = occurrences,
                    preferences = prefs
                )
            }
        }
    }
}
