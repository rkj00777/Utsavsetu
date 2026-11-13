package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.platform.security.SecurityIntegrityManager
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private var deepLinkFestivalId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecurityIntegrityManager.configureAppCheck(this, intent)
        enableEdgeToEdge()
        SecurityIntegrityManager.secureActivityWindow(this)

        val app = application as UtsavSetuApplication
        val container = app.appContainer

        deepLinkFestivalId = extractFestivalId(intent)

        setContent {
            val prefs by container.userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = com.example.data.preferences.UserPreferences(
                    hasCompletedSetup = false,
                    hostCity = com.example.panchang.City.DELHI
                )
            )
            val targetLocale = remember(prefs.preferredLanguage) {
                val loc = UtsavLocalizationEngine.localeForLanguage(prefs.preferredLanguage)
                java.util.Locale.setDefault(loc)
                loc
            }
            key(targetLocale.language) {
                val isDark = when (prefs.themeMode) {
                    "LIGHT" -> false
                    "DARK" -> true
                    else -> isSystemInDarkTheme()
                }
                val baseDensity = LocalDensity.current
                val effectiveDensity = remember(baseDensity, prefs.textScaleMode) {
                    val multiplier = when (prefs.textScaleMode) {
                        "EXTRA_LARGE" -> 1.18f
                        "LARGE" -> 1.10f
                        else -> 1.0f
                    }
                    Density(
                        density = baseDensity.density,
                        fontScale = baseDensity.fontScale * multiplier
                    )
                }

                CompositionLocalProvider(LocalDensity provides effectiveDensity) {
                    UtsavSetuTheme(
                        darkTheme = isDark,
                        language = prefs.preferredLanguage
                    ) {
                        AppNavigation(
                            appContainer = container,
                            hasCompletedSetup = prefs.hasCompletedSetup,
                            initialFestivalId = deepLinkFestivalId
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractFestivalId(intent)?.let { id ->
            deepLinkFestivalId = id
        }
    }

    internal fun extractFestivalId(intent: Intent?): String? {
        if (intent == null) return null
        intent.data?.let { uri ->
            // 1. Custom app scheme: utsavsetu://festival?id=<festival_id>
            if (uri.scheme == "utsavsetu" && uri.host == "festival") {
                return SecurityIntegrityManager.sanitizeFestivalId(uri.getQueryParameter("id"))
            }
            // 2. Google Play Store link:
            // https://play.google.com/store/apps/details?id=com.aistudio.utsavsetu.qjvxrp&festival_id=<id>&referrer=festival_id%3D<id>
            // or market://details?id=...&festival_id=<id>
            val isPlayStoreUrl = (uri.scheme in setOf("https", "http") &&
                uri.host == "play.google.com" &&
                uri.path?.startsWith("/store/apps/details") == true) ||
                (uri.scheme == "market" && uri.host == "details")

            if (isPlayStoreUrl) {
                val pkgParam = uri.getQueryParameter("id")
                if (pkgParam == null || pkgParam == packageName) {
                    SecurityIntegrityManager.sanitizeFestivalId(uri.getQueryParameter("festival_id"))?.let {
                        return it
                    }
                    uri.getQueryParameter("referrer")?.let { referrer ->
                        val decoded = android.net.Uri.decode(referrer)
                        for (pair in decoded.split("&")) {
                            val parts = pair.split("=", limit = 2)
                            if (parts.size == 2 && (parts[0] == "festival_id" || parts[0] == "id")) {
                                SecurityIntegrityManager.sanitizeFestivalId(parts[1])?.let { return it }
                            }
                        }
                    }
                }
            }
        }
        return SecurityIntegrityManager.sanitizeFestivalId(intent.getStringExtra("festival_id"))
    }
}
