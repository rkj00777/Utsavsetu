package com.example.monitoring

import android.content.ComponentCallbacks2
import androidx.test.core.app.ApplicationProvider
import com.example.UtsavSetuApplication
import com.example.panchang.City
import com.example.panchang.FestivalDateEvaluator
import com.example.panchang.PanchangEngine
import com.example.platform.monitoring.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = UtsavSetuApplication::class)
class SystemMonitoringTest {

    private lateinit var app: UtsavSetuApplication

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        AppCrashMonitor.install(app)
        AppPerformanceMonitor.install(app)
        AppCrashMonitor.clearCrashReports()
        AppPerformanceMonitor.resetMetrics()
    }

    @Test
    fun testAppCrashMonitorBreadcrumbsAndHandledExceptions() {
        assertTrue("AppCrashMonitor must be installed", AppCrashMonitor.isInstalled())

        AppCrashMonitor.recordBreadcrumb("Navigation", "User navigated to Ganesh Chaturthi")
        AppCrashMonitor.recordBreadcrumb("Audio", "Started Pandit male chant")

        val breadcrumbs = AppCrashMonitor.getRecentBreadcrumbs()
        assertTrue("Breadcrumbs must contain recorded items", breadcrumbs.size >= 2)
        assertEquals("Navigation", breadcrumbs[breadcrumbs.size - 2].tag)
        assertEquals("User navigated to Ganesh Chaturthi", breadcrumbs[breadcrumbs.size - 2].message)

        // Record a non-fatal handled exception
        val testException = IllegalStateException("Simulated network timeout in test")
        AppCrashMonitor.recordHandledException("NetworkSync", testException, "Retry count: 3")

        val crashReports = AppCrashMonitor.getCrashReports()
        assertEquals(1, crashReports.size)
        val report = crashReports.first()

        assertFalse("Handled exception must have isFatal = false", report.isFatal)
        assertEquals("java.lang.IllegalStateException", report.exceptionClass)
        assertTrue("Exception message must contain context", report.exceptionMessage?.contains("Simulated network timeout") == true)
        assertTrue("Exception message must contain extra context", report.exceptionMessage?.contains("Retry count: 3") == true)
        assertTrue("Stack trace must not be empty", report.stackTrace.isNotEmpty())
        assertTrue("Breadcrumbs must be captured in report", report.breadcrumbs.isNotEmpty())
    }

    @Test
    fun testAppCrashMonitorReportRotationAndPersistence() {
        // Record 12 exceptions to test the MAX_SAVED_REPORTS (10) rotation limit
        for (i in 1..12) {
            AppCrashMonitor.recordHandledException("TestModule", RuntimeException("Error #$i"))
        }

        val reports = AppCrashMonitor.getCrashReports()
        assertEquals("Crash reports should be capped at 10", 10, reports.size)
        // Most recent should be Error #12
        assertTrue(reports.first().exceptionMessage?.contains("Error #12") == true)

        // Clear reports
        AppCrashMonitor.clearCrashReports()
        assertEquals(0, AppCrashMonitor.getCrashCount())
        assertNull(AppCrashMonitor.getLastCrashReport())
    }

    @Test
    fun testAppPerformanceMonitorLatencyTrackingAndMemory() {
        // Measure synthetic block
        val result = AppPerformanceMonitor.measure("TestOperation") {
            Thread.sleep(5)
            "Success"
        }
        assertEquals("Success", result)

        val metric = AppPerformanceMonitor.getMetric("TestOperation")
        assertNotNull("Metric must be registered", metric)
        assertTrue("Executions must be at least 1", metric!!.totalExecutions >= 1)
        assertTrue("Duration must be >= 4ms", metric.averageDurationMs >= 4.0)

        // Check memory snapshot
        val mem = AppPerformanceMonitor.getMemorySnapshot()
        assertTrue("Used heap must be positive", mem.usedHeapMb > 0.0)
        assertTrue("Max heap must be positive", mem.maxHeapMb > 0.0)
        assertTrue("Utilization must be between 0% and 100%", mem.utilizationPercent in 0.0..100.0)
        assertTrue("Formatted summary must be descriptive", mem.formattedSummary.contains("Heap:"))
    }

    @Test
    fun testAppPerformanceMonitorTrimMemoryClearsPanchangCaches() {
        // Pre-populate Panchang cache
        val date = LocalDate.of(2026, 11, 8)
        PanchangEngine.computeDayPanchang(date, City.DELHI)
        assertTrue("Panchang cache should contain at least 1 entry", PanchangEngine.getCacheSize() > 0)

        // Signal low memory trim
        AppPerformanceMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)

        assertEquals("Panchang cache must be completely cleared after trim", 0, PanchangEngine.getCacheSize())
        assertEquals(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW, AppPerformanceMonitor.getLastTrimLevel())
    }

    @Test
    fun testAppBatteryMonitorSnapshotAndZeroWakeLocks() {
        val snapshot = AppBatteryMonitor.getBatterySnapshot(app)

        assertTrue("Battery percentage must be in valid range", snapshot.batteryPct in 0..100)
        assertNotNull("Charging state must be recognized", snapshot.chargingState)
        assertNotNull("Optimization level must be recognized", snapshot.optimizationLevel)
        assertTrue("Zero WakeLock policy must be verified", snapshot.zeroWakeLocksVerified)

        val findings = AppBatteryMonitor.runBatteryAudit(app)
        assertTrue("Audit must contain findings", findings.isNotEmpty())
        assertTrue("Must verify zero wakelocks in audit", findings.any { it.contains("Zero WakeLock Architecture: Verified") })
        assertTrue("Must state GPS telemetry policy", findings.any { it.contains("GPS Telemetry Policy: Single-shot") })
    }

    @Test
    fun testSystemHealthAuditorReportAndDiagnosticText() {
        val report = SystemHealthAuditor.runComprehensiveAudit(app)

        assertNotNull(report)
        assertTrue("Panchang engine executions must be >= 1", report.performanceHealth.panchangExecutionsCount >= 1)
        assertTrue("Panchang latency must be reasonable (< 50ms)", report.performanceHealth.panchangAvgLatencyMs < 50.0)
        assertTrue("Zero-WakeLock must be verified", report.batteryHealth.zeroWakeLocksVerified)
        assertTrue("Breadcrumb count must be >= 1", report.crashHealth.breadcrumbCount >= 1)

        val text = report.toDiagnosticText()
        assertTrue("Diagnostic text must have report header", text.contains("=== UTSAVSETU SYSTEM HEALTH & DIAGNOSTICS REPORT ==="))
        assertTrue("Must contain Crash section", text.contains("CRASH & EXCEPTION MONITORING"))
        assertTrue("Must contain Performance section", text.contains("PERFORMANCE & MEMORY MONITORING"))
        assertTrue("Must contain Battery section", text.contains("BATTERY & ENERGY CONSERVATION"))
    }
}
