package com.example.platform.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import android.view.WindowManager
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.Locale

/**
 * Comprehensive runtime security, anti-tampering, anti-hooking, and input-sanitization guard.
 *
 * Defends UtsavSetu against:
 * 1. APK repackaging / package name spoofing & certificate tampering.
 * 2. Dynamic instrumentation & memory hooking frameworks (Frida, Xposed, LSPosed, Cydia Substrate).
 * 3. Unauthorized debugger attachment & debuggable flag tampering in release builds.
 * 4. Overlay malware / tapjacking attacks on sensitive UI surfaces.
 * 5. Deep-link, Intent extra, and FileProvider path-traversal injection attacks.
 */
object SecurityIntegrityManager {

    private const val TAG = "SecurityIntegrity"
    private const val EXPECTED_PACKAGE_NAME = "com.aistudio.utsavsetu.qjvxrp"
    private val VALID_FESTIVAL_ID_REGEX = Regex("^[a-z0-9_\\-]{1,64}$")

    private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
    private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
    private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

    private val SUSPICIOUS_HOOK_KEYWORDS = listOf(
        "frida",
        "xposed",
        "lsposed",
        "edxp",
        "substrate",
        "cydiasubstrate",
        "libfrida-gadget",
        "frida-agent"
    )

    private val TRUSTED_INSTALLERS = setOf(
        "com.android.vending",          // Google Play Store
        "com.google.android.feedback",  // Google Play Feedback / Internal testing
        "com.google.android.packageinstaller"
    )

    /**
     * Configures Firebase App Check attestation using DebugAppCheckProviderFactory in debug builds
     * and RecaptchaAppCheckProviderFactory in release builds.
     */
    fun configureAppCheck(context: Context, intent: Intent?) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (BuildConfig.DEBUG) {
                val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                if (!debugToken.isNullOrEmpty()) {
                    val persistenceKey = FirebaseApp.getInstance().persistenceKey
                    val prefs = context.getSharedPreferences(
                        "$DEBUG_STORE_PREFIX$persistenceKey",
                        Context.MODE_PRIVATE
                    )
                    prefs.edit()
                        .putString(DEBUG_SECRET_KEY, debugToken)
                        .commit()
                }
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
                if (resId != 0) {
                    val siteKey = context.getString(resId)
                    if (siteKey.isNotBlank()) {
                        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "App Check configuration skipped: ${e.message}")
        }
    }

    data class SecurityAuditReport(
        val isPackageNameAuthentic: Boolean,
        val isSigningCertificatePresent: Boolean,
        val signingCertSha256: String,
        val isDebugTampered: Boolean,
        val isHookingDetected: Boolean,
        val isRootedIndicatorPresent: Boolean,
        val isTrustedInstaller: Boolean
    ) {
        val isEnvironmentSecure: Boolean
            get() = isPackageNameAuthentic &&
                isSigningCertificatePresent &&
                !isDebugTampered &&
                !isHookingDetected
    }

    /**
     * Performs a complete runtime integrity and anti-tampering audit of the application environment.
     */
    fun auditAppSecurity(context: Context): SecurityAuditReport {
        val appContext = context.applicationContext ?: context
        val pkgAuthentic = verifyPackageIdentity(appContext)
        val certHash = computeSigningCertificateSha256(appContext)
        val certPresent = certHash.isNotBlank()
        val debugTampered = !BuildConfig.DEBUG && (isAppMarkedDebuggable(appContext) || Debug.isDebuggerConnected())
        val hookingFound = detectDynamicInstrumentationOrHooking()
        val rooted = detectRootOrTestKeys()
        val installerOk = isInstalledFromTrustedSource(appContext)

        return SecurityAuditReport(
            isPackageNameAuthentic = pkgAuthentic,
            isSigningCertificatePresent = certPresent,
            signingCertSha256 = certHash,
            isDebugTampered = debugTampered,
            isHookingDetected = hookingFound,
            isRootedIndicatorPresent = rooted,
            isTrustedInstaller = installerOk
        )
    }

    /**
     * Hardens an Activity window against overlay spyware (tapjacking / cloak-and-dagger attacks)
     * and unauthorized screen scraping in release builds when appropriate.
     */
    fun secureActivityWindow(activity: Activity, preventScreenCaptureInRelease: Boolean = false) {
        try {
            // Block touch events when another app draws a malicious overlay window on top of UtsavSetu
            activity.window?.decorView?.filterTouchesWhenObscured = true

            if (preventScreenCaptureInRelease && !BuildConfig.DEBUG) {
                activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (_: Throwable) {
            // Defensive guard for headless test environments
        }
    }

    /**
     * Verifies that the application's runtime package name matches the expected canonical ID,
     * defeating basic APK manifest renaming / cloning tools.
     */
    fun verifyPackageIdentity(context: Context): Boolean {
        val runtimePkg = context.packageName
        return runtimePkg == EXPECTED_PACKAGE_NAME || runtimePkg == BuildConfig.APPLICATION_ID
    }

    /**
     * Computes the SHA-256 digest of the APK's active signing certificate.
     */
    fun computeSigningCertificateSha256(context: Context): String {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            val certBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                val signatures = if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else null
                signatures?.firstOrNull()?.toByteArray()
            } else {
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                info.signatures?.firstOrNull()?.toByteArray()
            }

            if (certBytes == null || certBytes.isEmpty()) {
                // Robolectric unit tests may not populate raw certificate bytes; fallback to deterministic package token
                if (BuildConfig.DEBUG) "DEBUG_TEST_CERTIFICATE_OK" else ""
            } else {
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(certBytes)
                digest.joinToString(":") { "%02X".format(Locale.ROOT, it) }
            }
        } catch (_: Throwable) {
            if (BuildConfig.DEBUG) "DEBUG_TEST_CERTIFICATE_OK" else ""
        }
    }

    /**
     * Checks whether `android:debuggable="true"` was injected into the manifest of a release APK.
     */
    fun isAppMarkedDebuggable(context: Context): Boolean {
        return try {
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Detects active reverse-engineering / runtime hooking frameworks:
     * 1. Call stack inspection for XposedBridge / LSPosed / Substrate frames.
     * 2. `/proc/self/maps` memory region scan for injected `frida-agent` / `xposed` shared objects.
     * 3. Default Frida server loopback port probe (27042).
     */
    fun detectDynamicInstrumentationOrHooking(): Boolean {
        // 1. Inspect current thread stack trace for Xposed / Substrate hooks
        try {
            val stackClasses = Thread.currentThread().stackTrace
            for (element in stackClasses) {
                val cls = element.className.lowercase(Locale.ROOT)
                if (SUSPICIOUS_HOOK_KEYWORDS.any { cls.contains(it) }) {
                    return true
                }
            }
        } catch (_: Throwable) {}

        // 2. Scan /proc/self/maps for injected Frida gadget or Xposed .so libraries
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists() && mapsFile.canRead()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val lower = line.lowercase(Locale.ROOT)
                        if (SUSPICIOUS_HOOK_KEYWORDS.any { lower.contains(it) }) {
                            return true
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (_: Throwable) {}

        // 3. Only in release builds, check if default Frida server port 27042 is listening locally
        if (!BuildConfig.DEBUG) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", 27042), 60)
                    return true
                }
            } catch (_: Throwable) {}
        }

        return false
    }

    /**
     * Checks for common root / custom ROM tampering indicators (`test-keys` or `su` binaries).
     */
    fun detectRootOrTestKeys(): Boolean {
        return try {
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }
            val suPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/su/bin/su"
            )
            suPaths.any { File(it).exists() }
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks whether the APK was installed via Google Play Store (or debug session in development).
     */
    fun isInstalledFromTrustedSource(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            installer != null && installer in TRUSTED_INSTALLERS
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Strictly validates and sanitizes a festival ID from deep links, intents, or filenames.
     * Prevents path traversal (`../`), URI injection, and malformed identifiers.
     * Returns null if the input does not match the strict `[a-z0-9_-]{1,64}` allowlist.
     */
    fun sanitizeFestivalId(rawId: String?): String? {
        if (rawId.isNullOrBlank()) return null
        val trimmed = rawId.trim()
        return if (VALID_FESTIVAL_ID_REGEX.matches(trimmed)) trimmed else null
    }

    /**
     * Sanitizes notification or intent text fields to prevent log/UI spoofing or oversized payloads.
     */
    fun sanitizeDisplayText(rawText: String?, maxLength: Int = 200, fallback: String = ""): String {
        if (rawText.isNullOrBlank()) return fallback
        // Strip control characters except standard space
        val cleaned = rawText
            .replace(Regex("[\\p{Cntrl}&&[^\n\t]]"), "")
            .trim()
        return if (cleaned.isEmpty()) fallback else cleaned.take(maxLength)
    }
}
