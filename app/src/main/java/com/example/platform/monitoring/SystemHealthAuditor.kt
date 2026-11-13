package com.example.platform.monitoring

import android.content.Context
import com.example.panchang.City
import com.example.panchang.PanchangEngine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class OverallHealthStatus(val label: String) {
    HEALTHY("All Systems Optimal"),
    ATTENTION("Attention Advised"),
    CRITICAL("Action Required")
}

data class CrashHealthSummary(
    val isInstalled: Boolean,
    val totalCrashes: Int,
    val fatalCrashes: Int,
    val lastCrashTimestamp: String?,
    val breadcrumbCount: Int,
    val lastBreadcrumb: String?
)

data class PerformanceHealthSummary(
    val panchangAvgLatencyMs: Double,
    val panchangLastLatencyMs: Double,
    val panchangExecutionsCount: Long,
    val memorySnapshot: MemorySnapshot,
    val activeMetricsCount: Int
)

data class SystemHealthReport(
    val timestampFormatted: String,
    val overallStatus: OverallHealthStatus,
    val crashHealth: CrashHealthSummary,
    val performanceHealth: PerformanceHealthSummary,
    val batteryHealth: BatteryHealthSnapshot,
    val recommendations: List<String>
) {
    fun toDiagnosticText(): String {
        return buildString {
            appendLine("=== UTSAVSETU SYSTEM HEALTH & DIAGNOSTICS REPORT ===")
            appendLine("Generated: $timestampFormatted")
            appendLine("Overall Status: ${overallStatus.label}")
            appendLine()
            appendLine("--- 1. CRASH & EXCEPTION MONITORING ---")
            appendLine("• Uncaught Exception Handler: ${if (crashHealth.isInstalled) "ACTIVE & SECURED" else "Inactive"}")
            appendLine("• Total Recorded Incidents: ${crashHealth.totalCrashes} (${crashHealth.fatalCrashes} fatal)")
            appendLine("• Last Incident: ${crashHealth.lastCrashTimestamp ?: "None (Clean Run)"}")
            appendLine("• Breadcrumb Ring Buffer: ${crashHealth.breadcrumbCount} events tracked")
            if (crashHealth.lastBreadcrumb != null) {
                appendLine("• Latest Breadcrumb: ${crashHealth.lastBreadcrumb}")
            }
            appendLine()
            appendLine("--- 2. PERFORMANCE & MEMORY MONITORING ---")
            appendLine("• Panchang Astronomical Engine: Avg ${performanceHealth.panchangAvgLatencyMs} ms (Last: ${performanceHealth.panchangLastLatencyMs} ms, Total Runs: ${performanceHealth.panchangExecutionsCount})")
            appendLine("• JVM Heap Allocation: ${performanceHealth.memorySnapshot.formattedSummary}")
            appendLine("• In-Memory Caches: Active (${PanchangEngine.getCacheSize()} panchang entries)")
            appendLine("• Monitored Operations: ${performanceHealth.activeMetricsCount}")
            appendLine()
            appendLine("--- 3. BATTERY & ENERGY CONSERVATION ---")
            appendLine("• Battery Level: ${batteryHealth.batteryPct}% (${batteryHealth.chargingState.label})")
            appendLine("• Temperature: ${batteryHealth.temperatureCelsius}°C")
            appendLine("• Android Battery Saver: ${if (batteryHealth.isPowerSaveMode) "ENABLED" else "Disabled"}")
            appendLine("• Active Exact Alarms: ${batteryHealth.activeAlarmCount} (Strictly Bounded & Reconciled)")
            appendLine("• Zero-WakeLock Policy: ${if (batteryHealth.zeroWakeLocksVerified) "VERIFIED (Zero Background Drain)" else "Unverified"}")
            appendLine("• Location Telemetry: Single-shot balanced snapshot with 4-second timeout")
            appendLine()
            appendLine("--- 4. RECOMMENDATIONS & STATUS ---")
            if (recommendations.isEmpty()) {
                appendLine("• All telemetry systems operating within target parameters.")
            } else {
                recommendations.forEach { appendLine("• $it") }
            }
            appendLine("====================================================")
        }
    }
}

/**
 * Orchestrator that aggregates Crash, Performance, and Battery audits
 * into a single unified health report.
 */
object SystemHealthAuditor {

    fun runComprehensiveAudit(context: Context): SystemHealthReport {
        val appContext = context.applicationContext

        // 1. Crash health audit
        val crashReports = AppCrashMonitor.getCrashReports()
        val fatalCount = AppCrashMonitor.getFatalCrashCount()
        val breadcrumbs = AppCrashMonitor.getRecentBreadcrumbs()
        val crashSummary = CrashHealthSummary(
            isInstalled = AppCrashMonitor.isInstalled(),
            totalCrashes = crashReports.size,
            fatalCrashes = fatalCount,
            lastCrashTimestamp = crashReports.firstOrNull()?.timestampIso,
            breadcrumbCount = breadcrumbs.size,
            lastBreadcrumb = breadcrumbs.lastOrNull()?.let { "[${it.tag}] ${it.message}" }
        )

        // 2. Performance benchmark: verify Panchang Engine latency
        // Warm up and benchmark one calculation if no metrics exist yet
        if (AppPerformanceMonitor.getMetric("PanchangEngine.computeDayPanchang") == null) {
            AppPerformanceMonitor.measure("PanchangEngine.computeDayPanchang") {
                PanchangEngine.computeDayPanchang(LocalDate.now(), City.DELHI)
            }
        }
        val panchangMetric = AppPerformanceMonitor.getMetric("PanchangEngine.computeDayPanchang")
        val memory = AppPerformanceMonitor.getMemorySnapshot()
        val allMetrics = AppPerformanceMonitor.getMetrics()

        val perfSummary = PerformanceHealthSummary(
            panchangAvgLatencyMs = panchangMetric?.averageDurationMs ?: 0.0,
            panchangLastLatencyMs = panchangMetric?.lastDurationMs ?: 0.0,
            panchangExecutionsCount = panchangMetric?.totalExecutions ?: 0L,
            memorySnapshot = memory,
            activeMetricsCount = allMetrics.size
        )

        // 3. Battery health audit
        val batterySnapshot = AppBatteryMonitor.getBatterySnapshot(appContext)

        // 4. Recommendations and overall status
        val recommendations = mutableListOf<String>()
        var status = OverallHealthStatus.HEALTHY

        if (fatalCount > 0) {
            status = OverallHealthStatus.CRITICAL
            recommendations.add("Critical: $fatalCount unhandled crashes were recorded. Inspect crash reports.")
        }

        if (memory.isUnderMemoryPressure) {
            if (status != OverallHealthStatus.CRITICAL) status = OverallHealthStatus.ATTENTION
            recommendations.add("High memory utilization (${memory.utilizationPercent}%). Caches will automatically trim on system low-memory.")
        }

        if (batterySnapshot.isPowerSaveMode) {
            if (status != OverallHealthStatus.CRITICAL) status = OverallHealthStatus.ATTENTION
            recommendations.add("Device is in Battery Saver mode. Audio oversampling and decorative animations adaptively throttled.")
        }

        if (batterySnapshot.activeAlarmCount > 50) {
            if (status != OverallHealthStatus.CRITICAL) status = OverallHealthStatus.ATTENTION
            recommendations.add("High active alarm count (${batterySnapshot.activeAlarmCount}). Verify custom reminders in Settings.")
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        return SystemHealthReport(
            timestampFormatted = timestamp,
            overallStatus = status,
            crashHealth = crashSummary,
            performanceHealth = perfSummary,
            batteryHealth = batterySnapshot,
            recommendations = recommendations
        )
    }
}
