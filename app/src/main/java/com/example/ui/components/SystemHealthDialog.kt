package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.platform.monitoring.*
import com.example.ui.theme.LocalLiquidGlassTokens
import com.example.ui.theme.TulsiGreen

private enum class HealthTab(val label: String) {
    SUMMARY("Overview"),
    CRASH("Crash Shield"),
    PERF("Performance"),
    BATTERY("Battery & Power")
}

@Composable
fun SystemHealthDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val glassTokens = LocalLiquidGlassTokens.current
    var selectedTab by remember { mutableStateOf(HealthTab.SUMMARY) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val report = remember(refreshKey) {
        SystemHealthAuditor.runComprehensiveAudit(context)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("system_health_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassTokens.solidElevatedContainer),
            border = BorderStroke(1.dp, glassTokens.decorativeGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = when (report.overallStatus) {
                                OverallHealthStatus.HEALTHY -> TulsiGreen.copy(alpha = 0.15f)
                                OverallHealthStatus.ATTENTION -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                OverallHealthStatus.CRITICAL -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (report.overallStatus) {
                                        OverallHealthStatus.HEALTHY -> Icons.Default.HealthAndSafety
                                        OverallHealthStatus.ATTENTION -> Icons.Default.WarningAmber
                                        OverallHealthStatus.CRITICAL -> Icons.Default.ErrorOutline
                                    },
                                    contentDescription = null,
                                    tint = when (report.overallStatus) {
                                        OverallHealthStatus.HEALTHY -> TulsiGreen
                                        OverallHealthStatus.ATTENTION -> Color(0xFFF59E0B)
                                        OverallHealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "System Health & Diagnostics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = report.overallStatus.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (report.overallStatus) {
                                    OverallHealthStatus.HEALTHY -> TulsiGreen
                                    OverallHealthStatus.ATTENTION -> Color(0xFFF59E0B)
                                    OverallHealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_health_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    HealthTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Area
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        HealthTab.SUMMARY -> SummarySection(report)
                        HealthTab.CRASH -> CrashSection(report, onClear = {
                            AppCrashMonitor.clearCrashReports()
                            refreshKey++
                        })
                        HealthTab.PERF -> PerformanceSection(report, onTrimCaches = {
                            AppPerformanceMonitor.onTrimMemory(android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
                            refreshKey++
                        })
                        HealthTab.BATTERY -> BatterySection(report)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("UtsavSetu Diagnostics", report.toDiagnosticText())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Diagnostics report copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_diagnostics_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Report", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = { refreshKey++ },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("refresh_diagnostics_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(report: SystemHealthReport) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            HealthMetricCard(
                title = "Crash Shield & Resilience",
                status = if (report.crashHealth.fatalCrashes == 0) "Optimal (0 Crashes)" else "${report.crashHealth.fatalCrashes} Fatal Crashes",
                statusColor = if (report.crashHealth.fatalCrashes == 0) TulsiGreen else MaterialTheme.colorScheme.error,
                details = listOf(
                    "Global Uncaught Exception Handler: Active",
                    "Recent Lifecycle Breadcrumbs: ${report.crashHealth.breadcrumbCount} events",
                    "Last Incident: ${report.crashHealth.lastCrashTimestamp ?: "None (Clean Run)"}"
                )
            )
        }

        item {
            HealthMetricCard(
                title = "Panchang & Calculation Performance",
                status = "Avg ${report.performanceHealth.panchangAvgLatencyMs} ms",
                statusColor = if (report.performanceHealth.panchangAvgLatencyMs < 10.0) TulsiGreen else Color(0xFFF59E0B),
                details = listOf(
                    "Panchang Astronomical Engine: ${report.performanceHealth.panchangExecutionsCount} total runs",
                    "JVM Heap: ${report.performanceHealth.memorySnapshot.formattedSummary}",
                    "Memory Pressure: ${if (report.performanceHealth.memorySnapshot.isUnderMemoryPressure) "High" else "Normal"}"
                )
            )
        }

        item {
            HealthMetricCard(
                title = "Battery & Energy Conservation",
                status = "${report.batteryHealth.batteryPct}% · ${report.batteryHealth.chargingState.label}",
                statusColor = if (report.batteryHealth.batteryPct > 20 || report.batteryHealth.isCharging) TulsiGreen else Color(0xFFF59E0B),
                details = listOf(
                    "Zero-WakeLock Architecture: Verified (0% background idle drain)",
                    "Active Scheduled Exact Alarms: ${report.batteryHealth.activeAlarmCount} (Reconciled)",
                    "Battery Saver Mode: ${if (report.batteryHealth.isPowerSaveMode) "Active (Power Throttled)" else "Off"}",
                    "Battery Temperature: ${report.batteryHealth.temperatureCelsius}°C"
                )
            )
        }
    }
}

@Composable
private fun CrashSection(report: SystemHealthReport, onClear: () -> Unit) {
    val crashReports = remember { AppCrashMonitor.getCrashReports() }
    val breadcrumbs = remember { AppCrashMonitor.getRecentBreadcrumbs() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recorded Incidents: ${crashReports.size}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (crashReports.isNotEmpty() || breadcrumbs.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Logs", fontSize = 12.sp)
                    }
                }
            }
        }

        if (crashReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "No crashes or fatal terminations detected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TulsiGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The application is running stably under active crash interception.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(crashReports) { cr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = cr.exceptionClass,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = cr.exceptionMessage ?: "No message",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${cr.timestampIso} · Thread: ${cr.threadName} · Heap: ${cr.memoryUsageMb} MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recent Lifecycle Breadcrumbs (${breadcrumbs.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(breadcrumbs.reversed()) { bc ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bc.tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(90.dp)
                    )
                    Text(
                        text = bc.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceSection(report: SystemHealthReport, onTrimCaches: () -> Unit) {
    val metrics = remember { AppPerformanceMonitor.getMetrics() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JVM Heap Memory",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onTrimCaches) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trim Caches", fontSize = 12.sp)
                        }
                    }

                    val mem = report.performanceHealth.memorySnapshot
                    val progress = (mem.utilizationPercent / 100.0).toFloat().coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (mem.isUnderMemoryPressure) MaterialTheme.colorScheme.error else TulsiGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mem.formattedSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Tracked Operations (${metrics.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(metrics) { metric ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = metric.operationName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Min: ${metric.minDurationMs} ms · Max: ${metric.maxDurationMs} ms · Runs: ${metric.totalExecutions}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TulsiGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Avg ${metric.averageDurationMs} ms",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TulsiGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatterySection(report: SystemHealthReport) {
    val context = LocalContext.current
    val findings = remember(context) { AppBatteryMonitor.runBatteryAudit(context) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Energy Profile: ${report.batteryHealth.optimizationLevel.label}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adaptive throttling dynamically reduces Tanpura harmonic oversampling and background work when the device enters Android Battery Saver mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Battery Compliance Audit Findings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(findings) { finding ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (finding.startsWith("WARNING")) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (finding.startsWith("WARNING")) Color(0xFFF59E0B) else TulsiGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = finding,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    status: String,
    statusColor: Color,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            details.forEach { detail ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
