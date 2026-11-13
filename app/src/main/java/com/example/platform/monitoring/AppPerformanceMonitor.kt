package com.example.platform.monitoring

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import android.util.Log
import com.example.panchang.FestivalDateEvaluator
import com.example.panchang.PanchangEngine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

data class MemorySnapshot(
    val usedHeapMb: Double,
    val maxHeapMb: Double,
    val freeHeapMb: Double,
    val utilizationPercent: Double,
    val isUnderMemoryPressure: Boolean
) {
    val formattedSummary: String
        get() = String.format(
            java.util.Locale.US,
            "Heap: %.1f MB / %.1f MB (%.1f%%) · %s",
            usedHeapMb,
            maxHeapMb,
            utilizationPercent,
            if (isUnderMemoryPressure) "High Pressure" else "Normal"
        )
}

data class PerformanceMetric(
    val operationName: String,
    val lastDurationMs: Double,
    val averageDurationMs: Double,
    val minDurationMs: Double,
    val maxDurationMs: Double,
    val totalExecutions: Long
)

private class MetricAccumulator(
    val operationName: String
) {
    private val totalExecutions = AtomicLong(0)
    private val totalNanos = AtomicLong(0)
    private val minNanos = AtomicLong(Long.MAX_VALUE)
    private val maxNanos = AtomicLong(0)
    private val lastNanos = AtomicLong(0)

    fun record(nanos: Long) {
        totalExecutions.incrementAndGet()
        totalNanos.addAndGet(nanos)
        lastNanos.set(nanos)

        var curMin = minNanos.get()
        while (nanos < curMin) {
            if (minNanos.compareAndSet(curMin, nanos)) break
            curMin = minNanos.get()
        }

        var curMax = maxNanos.get()
        while (nanos > curMax) {
            if (maxNanos.compareAndSet(curMax, nanos)) break
            curMax = maxNanos.get()
        }
    }

    fun toMetric(): PerformanceMetric {
        val count = totalExecutions.get()
        if (count == 0L) {
            return PerformanceMetric(
                operationName = operationName,
                lastDurationMs = 0.0,
                averageDurationMs = 0.0,
                minDurationMs = 0.0,
                maxDurationMs = 0.0,
                totalExecutions = 0L
            )
        }
        val avgMs = (totalNanos.get().toDouble() / count) / 1_000_000.0
        val minMs = minNanos.get().toDouble() / 1_000_000.0
        val maxMs = maxNanos.get().toDouble() / 1_000_000.0
        val lastMs = lastNanos.get().toDouble() / 1_000_000.0

        return PerformanceMetric(
            operationName = operationName,
            lastDurationMs = (lastMs * 100.0).toLong() / 100.0,
            averageDurationMs = (avgMs * 100.0).toLong() / 100.0,
            minDurationMs = (minMs * 100.0).toLong() / 100.0,
            maxDurationMs = (maxMs * 100.0).toLong() / 100.0,
            totalExecutions = count
        )
    }
}

/**
 * Enterprise Performance, Memory Footprint & Low-Memory Monitor.
 *
 * Capabilities:
 * 1. Nanosecond-precision execution tracking with rolling average/min/max latencies.
 * 2. JVM Heap memory telemetry & heap pressure detection (> 85% utilization).
 * 3. Proactive ComponentCallbacks2 integration to evict Panchang & festival date caches
 *    on system memory trimming (TRIM_MEMORY_RUNNING_LOW, UI_HIDDEN, etc.).
 * 4. Benchmarking harness for Panchang astronomical calculations and catalog queries.
 */
object AppPerformanceMonitor : ComponentCallbacks2 {

    private const val TAG = "AppPerformanceMonitor"
    private val isInstalled = AtomicBoolean(false)
    private val metricsMap = ConcurrentHashMap<String, MetricAccumulator>()
    private var lastTrimLevel = AtomicReference<Int?>(null)

    fun install(context: Context) {
        if (isInstalled.compareAndSet(false, true)) {
            val app = context.applicationContext as? Application
            app?.registerComponentCallbacks(this)
            AppCrashMonitor.recordBreadcrumb("Performance", "AppPerformanceMonitor registered with ComponentCallbacks2")
            Log.i(TAG, "AppPerformanceMonitor installed")
        }
    }

    inline fun <T> measure(operationName: String, block: () -> T): T {
        val startNanos = System.nanoTime()
        return try {
            block()
        } finally {
            val duration = System.nanoTime() - startNanos
            recordExecution(operationName, duration)
        }
    }

    fun recordExecution(operationName: String, durationNanos: Long) {
        val accumulator = metricsMap.computeIfAbsent(operationName) { MetricAccumulator(operationName) }
        accumulator.record(durationNanos)
    }

    fun getMemorySnapshot(): MemorySnapshot {
        val runtime = Runtime.getRuntime()
        val totalMem = runtime.totalMemory().toDouble()
        val freeMem = runtime.freeMemory().toDouble()
        val maxMem = runtime.maxMemory().toDouble()

        val usedBytes = totalMem - freeMem
        val usedMb = usedBytes / (1024.0 * 1024.0)
        val maxMb = maxMem / (1024.0 * 1024.0)
        val freeMb = (maxMem - usedBytes) / (1024.0 * 1024.0)

        val utilization = if (maxMb > 0) (usedMb / maxMb) * 100.0 else 0.0
        val isPressure = utilization >= 85.0

        return MemorySnapshot(
            usedHeapMb = (usedMb * 10.0).toLong() / 10.0,
            maxHeapMb = (maxMb * 10.0).toLong() / 10.0,
            freeHeapMb = (freeMb * 10.0).toLong() / 10.0,
            utilizationPercent = (utilization * 10.0).toLong() / 10.0,
            isUnderMemoryPressure = isPressure
        )
    }

    fun getMetrics(): List<PerformanceMetric> {
        return metricsMap.values.map { it.toMetric() }.sortedByDescending { it.averageDurationMs }
    }

    fun getMetric(operationName: String): PerformanceMetric? {
        return metricsMap[operationName]?.toMetric()
    }

    fun resetMetrics() {
        metricsMap.clear()
    }

    fun getLastTrimLevel(): Int? = lastTrimLevel.get()

    override fun onTrimMemory(level: Int) {
        lastTrimLevel.set(level)
        AppCrashMonitor.recordBreadcrumb("MemoryTrim", "onTrimMemory received with level $level")
        Log.w(TAG, "onTrimMemory level $level: performing defensive cache clearance")

        // Evict astronomical caches to reclaim RAM immediately
        PanchangEngine.clearCache()
        FestivalDateEvaluator.clearCache()

        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            System.gc()
        }
    }

    override fun onLowMemory() {
        lastTrimLevel.set(-1)
        AppCrashMonitor.recordBreadcrumb("MemoryTrim", "onLowMemory received: critical RAM pressure")
        Log.e(TAG, "onLowMemory: clearing all in-memory caches and requesting GC")
        PanchangEngine.clearCache()
        FestivalDateEvaluator.clearCache()
        System.gc()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // No-op for performance monitor
    }
}
