package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.platform.notifications.ReminderScheduler
import com.example.platform.security.SecurityIntegrityManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class UtsavSetuApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        SecurityIntegrityManager.auditAppSecurity(this)
        runCatching { FirebaseApp.initializeApp(this) }
        runCatching {
            val requestConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
                .build()
            com.google.android.gms.ads.MobileAds.setRequestConfiguration(requestConfig)
            com.google.android.gms.ads.MobileAds.initialize(this) {}
        }
        appContainer = AppContainer(this)
        ReminderScheduler.createNotificationChannel(this)

        // Preload catalog into database
        appContainer.applicationScope.launch {
            appContainer.festivalRepository.ensureCatalogLoaded()
        }
    }
}
