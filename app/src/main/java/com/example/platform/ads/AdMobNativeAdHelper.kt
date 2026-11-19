package com.example.platform.ads

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

private const val TAG = "UtsavSetuAdMob"

object AdMobSdkInitializer {
    @Volatile private var initializationStarted = false
    @Volatile private var initializationComplete = false
    private val pendingCallbacks = mutableListOf<() -> Unit>()

    fun initialize(context: Context, onReady: () -> Unit) {
        var shouldStartInitialization = false
        synchronized(this) {
            if (!initializationComplete) {
                pendingCallbacks.add(onReady)
                if (!initializationStarted) {
                    initializationStarted = true
                    shouldStartInitialization = true
                }
            }
        }

        if (initializationComplete) {
            onReady()
        } else if (shouldStartInitialization) {
            try {
                MobileAds.initialize(context.applicationContext) {
                    val callbacks = synchronized(this) {
                        initializationComplete = true
                        pendingCallbacks.toList().also { pendingCallbacks.clear() }
                    }
                    callbacks.forEach { callback -> runCatching(callback) }
                    Log.i(TAG, "Google Mobile Ads SDK initialization completed")
                }
            } catch (error: Exception) {
                val callbacks = synchronized(this) {
                    initializationStarted = false
                    pendingCallbacks.toList().also { pendingCallbacks.clear() }
                }
                Log.e(TAG, "Google Mobile Ads SDK initialization failed", error)
                callbacks.forEach { callback -> runCatching(callback) }
            }
        }
    }
}

/** Google AdMob production native ad unit ID supplied by the publisher. */
const val ADMOB_NATIVE_AD_UNIT_ID = "ca-app-pub-4148485857290947/5042099689"
const val ADMOB_NATIVE_TEST_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

val DEFAULT_ADMOB_NATIVE_AD_UNIT_ID: String
    get() = if (BuildConfig.DEBUG) ADMOB_NATIVE_TEST_UNIT_ID else ADMOB_NATIVE_AD_UNIT_ID

data class NativeAdState(
    val adUnitId: String = DEFAULT_ADMOB_NATIVE_AD_UNIT_ID,
    val isLoading: Boolean = false,
    val nativeAd: NativeAd? = null,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val loadSuccessCount: Int = 0
)

class AdMobNativeAdLoader(
    private val context: Context,
    val adUnitId: String = DEFAULT_ADMOB_NATIVE_AD_UNIT_ID
) {
    var state by mutableStateOf(NativeAdState(adUnitId = adUnitId, isLoading = true))
        private set

    init {
        AdMobSdkInitializer.initialize(context) { loadAd() }
    }

    fun loadAd() {
        state = state.copy(isLoading = true, errorMessage = null, errorCode = null)
        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { ad ->
                    state.nativeAd?.destroy()
                    state = state.copy(
                        isLoading = false,
                        nativeAd = ad,
                        errorMessage = null,
                        errorCode = null,
                        loadSuccessCount = state.loadSuccessCount + 1
                    )
                    Log.i(TAG, "Native ad loaded successfully for unit ${adUnitId.takeLast(10)}")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        val adapterErrors = error.responseInfo?.adapterResponses
                            ?.joinToString(separator = " | ") { response ->
                                "${response.adapterClassName}: ${response.description} (latency=${response.latencyMillis}ms)"
                            }
                            .orEmpty()
                        val details = buildString {
                            append(error.message)
                            append(" | domain=").append(error.domain)
                            error.responseInfo?.let { append(" | responseInfo=").append(it) }
                            if (adapterErrors.isNotBlank()) append(" | adapters=").append(adapterErrors)
                        }
                        state = state.copy(
                            isLoading = false,
                            errorMessage = details,
                            errorCode = error.code
                        )
                        Log.e(TAG, "Native ad failed: code=${error.code}, $details")
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
            Log.d(TAG, "Native ad request started for unit ${adUnitId.takeLast(10)}")
        } catch (error: Exception) {
            state = state.copy(
                isLoading = false,
                errorMessage = error.message ?: "Failed to initiate AdLoader",
                errorCode = -1
            )
            Log.e(TAG, "Unable to start native ad request", error)
        }
    }

    fun destroy() {
        runCatching { state.nativeAd?.destroy() }
        state = state.copy(nativeAd = null)
    }
}

@Composable
fun rememberAdMobNativeAd(
    adUnitId: String = DEFAULT_ADMOB_NATIVE_AD_UNIT_ID
): AdMobNativeAdLoader {
    val context = LocalContext.current.applicationContext
    val loader = remember(adUnitId) { AdMobNativeAdLoader(context, adUnitId) }

    DisposableEffect(loader) {
        onDispose { loader.destroy() }
    }
    return loader
}
