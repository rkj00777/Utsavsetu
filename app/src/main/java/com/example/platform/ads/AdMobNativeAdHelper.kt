package com.example.platform.ads

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.example.BuildConfig

/**
 * Google AdMob Production Native Ad Unit ID provided by publisher:
 * ca-app-pub-4148485857290947/5042099689
 */
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
        runCatching {
            MobileAds.initialize(context) {}
        }
        loadAd()
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
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        state = state.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            errorCode = error.code
                        )
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            state = state.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to initiate AdLoader",
                errorCode = -1
            )
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
    val loader = remember(adUnitId) {
        AdMobNativeAdLoader(context, adUnitId)
    }

    DisposableEffect(loader) {
        onDispose {
            loader.destroy()
        }
    }

    return loader
}
