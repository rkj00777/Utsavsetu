package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.platform.monitoring.AppCrashMonitor
import com.example.platform.monitoring.AppPerformanceMonitor
import com.example.platform.notifications.ReminderScheduler
import com.example.platform.security.SecurityIntegrityManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class UtsavSetuApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // 1. Install Crash & Performance Monitoring as early as possible
        AppCrashMonitor.install(this)
        AppPerformanceMonitor.install(this)
        AppCrashMonitor.recordBreadcrumb("Lifecycle", "Application onCreate started")

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

        // Preload catalog into database with performance profiling
        appContainer.applicationScope.launch {
            AppPerformanceMonitor.measure("FestivalRepository.ensureCatalogLoaded") {
                appContainer.festivalRepository.ensureCatalogLoaded()
            }
            AppCrashMonitor.recordBreadcrumb("Catalog", "Festival catalog preload completed")
        }
        AppCrashMonitor.recordBreadcrumb("Lifecycle", "Application onCreate completed")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppPerformanceMonitor.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppPerformanceMonitor.onLowMemory()
    }
}
