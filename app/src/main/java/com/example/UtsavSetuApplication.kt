package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.platform.ads.AdMobNativeAdLoader
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
        // Application.onCreate runs in every declared process. Keep Ads/WebView/Firebase app UI
        // initialization out of the isolated Ninada worker process to prevent WebView data-dir locks.
        val processName = if (android.os.Build.VERSION.SDK_INT >= 28) Application.getProcessName() else ""
        val isNinadaWorker = processName.endsWith(":ninada_worker")
        if (!isNinadaWorker) runCatching {
            if (BuildConfig.DEBUG && BuildConfig.USE_TEST_ADS) {
                val requestConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                com.google.android.gms.ads.MobileAds.setRequestConfiguration(requestConfig)
            }
            AdMobNativeAdLoader.ensureMobileAdsInitialized(this)
        }
        appContainer = AppContainer(this)
        ReminderScheduler.createNotificationChannel(this)

        // Preload catalog into database and ensure daily morning & festival reminders are scheduled
        appContainer.applicationScope.launch {
            AppPerformanceMonitor.measure("FestivalRepository.ensureCatalogLoaded") {
                appContainer.festivalRepository.ensureCatalogLoaded()
            }
            AppCrashMonitor.recordBreadcrumb("Catalog", "Festival catalog preload completed")
            runCatching {
                ReminderScheduler.rescheduleFromCurrentPreferences(this@UtsavSetuApplication)
            }
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
