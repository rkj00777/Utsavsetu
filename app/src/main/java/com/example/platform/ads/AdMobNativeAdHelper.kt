package com.example.platform.ads

import android.content.Context
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
import java.util.LinkedHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google AdMob Production App ID (in AndroidManifest.xml):
 * ca-app-pub-4148485857290947~5018048959
 *
 * Google AdMob Production Native Ad Unit ID:
 * ca-app-pub-4148485857290947/5042099689
 */
const val ADMOB_PRODUCTION_APP_ID = "ca-app-pub-4148485857290947~5018048959"
const val ADMOB_NATIVE_AD_UNIT_ID = "ca-app-pub-4148485857290947/5042099689"

/**
 * Official Google Native Test Ad Unit ID.
 * Strictly gated behind `BuildConfig.DEBUG && BuildConfig.USE_TEST_ADS`.
 * Release builds can never resolve this ID.
 */
const val ADMOB_NATIVE_TEST_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

/**
 * Resolves the effective Native Ad Unit ID while structurally protecting release builds
 * from ever shipping Google's test ad unit ID.
 *
 * - Release builds (`isDebug == false`): ALWAYS returns [ADMOB_NATIVE_AD_UNIT_ID].
 * - Debug builds with explicit Gradle flag (`-PUSE_TEST_ADS=true`): returns [ADMOB_NATIVE_TEST_UNIT_ID].
 * - Debug builds without the flag (default): returns [requestedAdUnitId] (never test ID).
 */
fun resolveNativeAdUnitId(
    requestedAdUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
    isDebug: Boolean = BuildConfig.DEBUG,
    useTestAds: Boolean = BuildConfig.USE_TEST_ADS
): String {
    if (!isDebug) {
        return ADMOB_NATIVE_AD_UNIT_ID
    }
    if (useTestAds) {
        return ADMOB_NATIVE_TEST_UNIT_ID
    }
    return requestedAdUnitId
        .takeIf { it.isNotBlank() && it != ADMOB_NATIVE_TEST_UNIT_ID }
        ?: ADMOB_NATIVE_AD_UNIT_ID
}

/**
 * Returns true strictly when running a debug build with the explicit Gradle `USE_TEST_ADS` flag enabled.
 */
fun isTestAdModeActive(
    isDebug: Boolean = BuildConfig.DEBUG,
    useTestAds: Boolean = BuildConfig.USE_TEST_ADS
): Boolean = isDebug && useTestAds

enum class NativeAdLoadStatus {
    IDLE,
    LOADING,
    LOADED,
    FAILED
}

data class NativeAdState(
    val adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
    val status: NativeAdLoadStatus = NativeAdLoadStatus.IDLE,
    val isLoading: Boolean = false,
    val nativeAd: NativeAd? = null,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val loadRequestCount: Int = 0,
    val loadSuccessCount: Int = 0
)

/**
 * Lifecycle-managed Native Ad loader that starts IDLE and only issues an AdMob request
 * when explicitly triggered on actual on-screen visibility (never merely on composition/prefetch).
 */
class AdMobNativeAdLoader(
    private val context: Context,
    val adUnitId: String = resolveNativeAdUnitId(),
    private val adRequestExecutor: ((String, (NativeAd) -> Unit, (Int, String) -> Unit) -> Unit)? = null
) {
    var state by mutableStateOf(
        NativeAdState(
            adUnitId = adUnitId,
            status = NativeAdLoadStatus.IDLE,
            isLoading = false
        )
    )
        private set

    @Volatile
    private var isDestroyed = false

    /**
     * Requests a native ad strictly once when a slot becomes visibly rendered on screen.
     * Deduplicates repeated visibility/recomposition callbacks while LOADING, LOADED, or FAILED.
     */
    fun requestAdOnVisible(): Boolean {
        if (isDestroyed) return false
        if (state.status == NativeAdLoadStatus.LOADING ||
            state.status == NativeAdLoadStatus.LOADED ||
            state.status == NativeAdLoadStatus.FAILED
        ) {
            return false
        }
        loadAdInternal()
        return true
    }

    /**
     * Explicit reload entry point (used only for diagnostics/tests, never triggered automatically on recomposition).
     */
    fun loadAd() {
        if (isDestroyed) return
        loadAdInternal()
    }

    private fun loadAdInternal() {
        ensureMobileAdsInitialized(context)
        state = state.copy(
            status = NativeAdLoadStatus.LOADING,
            isLoading = true,
            errorMessage = null,
            errorCode = null,
            loadRequestCount = state.loadRequestCount + 1
        )

        val customExecutor = adRequestExecutor
        if (customExecutor != null) {
            customExecutor.invoke(
                adUnitId,
                { ad -> onAdLoaded(ad) },
                { code, msg -> onAdFailed(code, msg) }
            )
            return
        }

        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { ad -> onAdLoaded(ad) }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        onAdFailed(error.code, error.message)
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
            onAdFailed(-1, e.message ?: "Failed to initiate AdLoader")
        }
    }

    private fun onAdLoaded(ad: NativeAd) {
        if (isDestroyed) {
            runCatching { ad.destroy() }
            return
        }
        runCatching { state.nativeAd?.destroy() }
        state = state.copy(
            status = NativeAdLoadStatus.LOADED,
            isLoading = false,
            nativeAd = ad,
            errorMessage = null,
            errorCode = null,
            loadSuccessCount = state.loadSuccessCount + 1
        )
    }

    private fun onAdFailed(code: Int, message: String) {
        if (isDestroyed) return
        state = state.copy(
            status = NativeAdLoadStatus.FAILED,
            isLoading = false,
            errorMessage = message,
            errorCode = code
        )
    }

    fun destroy() {
        isDestroyed = true
        runCatching { state.nativeAd?.destroy() }
        state = state.copy(
            status = NativeAdLoadStatus.IDLE,
            isLoading = false,
            nativeAd = null
        )
    }

    companion object {
        private val mobileAdsInitialized = AtomicBoolean(false)

        fun ensureMobileAdsInitialized(context: Context) {
            if (mobileAdsInitialized.compareAndSet(false, true)) {
                runCatching {
                    MobileAds.initialize(context.applicationContext) {}
                }
            }
        }
    }
}

/**
 * Slot-keyed Native Ad registry preventing duplicate ad requests across Compose recompositions,
 * LazyColumn scrolling, and tab/screen navigation, while bounding memory via LRU eviction and
 * destroying evicted or released NativeAd instances cleanly.
 */
object NativeAdSlotRegistry {
    private const val MAX_CACHED_SLOTS = 12

    private val lock = Any()
    private val slots = LinkedHashMap<String, AdMobNativeAdLoader>(16, 0.75f, true)

    fun slotKey(feedKey: String, slotIndex: Int): String = "$feedKey::slot_$slotIndex"

    fun getOrCreate(
        context: Context,
        slotKey: String,
        adUnitId: String = resolveNativeAdUnitId(),
        adRequestExecutor: ((String, (NativeAd) -> Unit, (Int, String) -> Unit) -> Unit)? = null
    ): AdMobNativeAdLoader = synchronized(lock) {
        val existing = slots[slotKey]
        if (existing != null && existing.adUnitId == adUnitId) {
            return existing
        }
        existing?.destroy()
        val loader = AdMobNativeAdLoader(
            context = context.applicationContext,
            adUnitId = adUnitId,
            adRequestExecutor = adRequestExecutor
        )
        slots[slotKey] = loader
        evictIfNeededLocked(protectedKey = slotKey)
        loader
    }

    fun activeSlotCount(): Int = synchronized(lock) { slots.size }

    fun releaseSlot(slotKey: String) = synchronized(lock) {
        slots.remove(slotKey)?.destroy()
    }

    fun releaseFeed(feedKey: String) = synchronized(lock) {
        val prefix = "$feedKey::"
        val keysToRemove = slots.keys.filter { it.startsWith(prefix) }
        for (key in keysToRemove) {
            slots.remove(key)?.destroy()
        }
    }

    fun clearAll() = synchronized(lock) {
        slots.values.forEach { it.destroy() }
        slots.clear()
    }

    private fun evictIfNeededLocked(protectedKey: String) {
        while (slots.size > MAX_CACHED_SLOTS) {
            val eldestKey = slots.keys.firstOrNull { it != protectedKey } ?: break
            slots.remove(eldestKey)?.destroy()
        }
    }
}

@Composable
fun rememberAdMobNativeAd(
    adUnitId: String = resolveNativeAdUnitId(),
    slotKey: String? = null
): AdMobNativeAdLoader {
    val context = LocalContext.current.applicationContext
    val effectiveUnitId = resolveNativeAdUnitId(adUnitId)

    if (slotKey != null) {
        return remember(slotKey, effectiveUnitId) {
            NativeAdSlotRegistry.getOrCreate(
                context = context,
                slotKey = slotKey,
                adUnitId = effectiveUnitId
            )
        }
    }

    val loader = remember(effectiveUnitId) {
        AdMobNativeAdLoader(context, effectiveUnitId)
    }

    DisposableEffect(loader) {
        onDispose {
            loader.destroy()
        }
    }

    return loader
}
