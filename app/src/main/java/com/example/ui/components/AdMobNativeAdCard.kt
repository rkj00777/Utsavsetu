package com.example.ui.components

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID
import com.example.platform.ads.NativeAdLoadStatus
import com.example.platform.ads.isTestAdModeActive
import com.example.platform.ads.rememberAdMobNativeAd
import com.example.platform.ads.resolveNativeAdUnitId
import com.example.ui.theme.LocalLiquidGlassTokens
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Google AdMob Native Ad Card Component for UtsavSetu.
 *
 * Implements Google Play Store & Google AdMob Native Ad specifications:
 * 1. Release builds always use the production Native Ad unit (ca-app-pub-4148485857290947/5042099689).
 *    Debug builds use Google's official test ad unit strictly when `USE_TEST_ADS` is explicitly enabled via Gradle.
 * 2. Triggers an ad request ONLY when the slot is genuinely visible inside the window viewport —
 *    never merely on LazyColumn composition or prefetch.
 * 3. Caches slot state across recompositions and scrolling via [com.example.platform.ads.NativeAdSlotRegistry]
 *    and destroys NativeAd instances cleanly on lifecycle release.
 * 4. Renders ONLY real Google-served `NativeAd` objects via `NativeAdView` with registered assets
 *    and unambiguous "AD" / "Sponsored" attribution. Never displays fabricated ads or fake advertisers.
 * 5. Collapses gracefully when offline, no-fill, failed, or dismissed.
 */
@Composable
fun AdMobNativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
    slotKey: String? = null,
    showDismissButton: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    var isDismissed by rememberSaveable(slotKey ?: adUnitId) { mutableStateOf(false) }
    val effectiveAdUnitId = resolveNativeAdUnitId(adUnitId)
    val isTestMode = isTestAdModeActive()
    val adLoader = rememberAdMobNativeAd(
        adUnitId = effectiveAdUnitId,
        slotKey = slotKey
    )
    val adState = adLoader.state
    val activeAd = adState.nativeAd
    val glassTokens = LocalLiquidGlassTokens.current
    val softPress = rememberSoftPressController()
    val hostView = LocalView.current

    if (isDismissed) return

    // Visibility-triggered ad loader sentinel: occupies a 1dp invisible anchor ONLY while IDLE
    // so an ad request fires strictly when this slot enters the visible viewport (never on prefetch).
    if (adState.status == NativeAdLoadStatus.IDLE) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .onGloballyPositioned { coordinates ->
                    if (!coordinates.isAttached) return@onGloballyPositioned
                    val windowBounds = coordinates.boundsInWindow()
                    val viewportWidth = hostView.width.toFloat()
                    val viewportHeight = hostView.height.toFloat()
                    val isActuallyVisible =
                        windowBounds.width > 0f &&
                        windowBounds.height > 0f &&
                        viewportWidth > 0f &&
                        viewportHeight > 0f &&
                        windowBounds.bottom > 0f &&
                        windowBounds.top < viewportHeight &&
                        windowBounds.right > 0f &&
                        windowBounds.left < viewportWidth

                    if (isActuallyVisible) {
                        adLoader.requestAdOnVisible()
                    }
                }
        )
        return
    }

    // Collapse cleanly with zero height/spacing on loading, no-fill, or failure
    AnimatedVisibility(
        visible = activeAd != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        if (activeAd != null) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .softPressCardEffect(softPress)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                        spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
                    )
                    .testTag(slotKey?.let { "admob_native_card_$it" } ?: "admob_native_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
                border = BorderStroke(
                    width = 1.2.dp,
                    color = if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.5f) else Color(0xFFD6C3AA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Header Strip: "AD" Badge, Clear Attribution & Optional Dismiss Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "AD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (isTestMode) "Sponsored · Test Ad" else "Sponsored",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (showDismissButton) {
                            IconButton(
                                onClick = {
                                    isDismissed = true
                                    onDismiss?.invoke()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("dismiss_native_ad_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss ad",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    val isDarkTheme = glassTokens.isDark

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        factory = { ctx ->
                            val nativeAdView = NativeAdView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }

                            val rootLayout = LinearLayout(ctx).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }

                            // 1. Header (Icon + Headline + Advertiser)
                            val headerRow = LinearLayout(ctx).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER_VERTICAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }

                            val iconView = ImageView(ctx).apply {
                                id = View.generateViewId()
                                val sizePx = (46 * ctx.resources.displayMetrics.density).toInt()
                                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                                    marginEnd = (12 * ctx.resources.displayMetrics.density).toInt()
                                }
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                            headerRow.addView(iconView)

                            val headerTextCol = LinearLayout(ctx).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }

                            val headlineView = TextView(ctx).apply {
                                id = View.generateViewId()
                                textSize = 15f
                                setTypeface(typeface, Typeface.BOLD)
                                setTextColor(if (isDarkTheme) 0xFFFFF8EC.toInt() else 0xFF2B1810.toInt())
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }
                            headerTextCol.addView(headlineView)

                            val advertiserView = TextView(ctx).apply {
                                id = View.generateViewId()
                                textSize = 12f
                                setTextColor(if (isDarkTheme) 0xFFD9A441.toInt() else 0xFF8A5A00.toInt())
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }
                            headerTextCol.addView(advertiserView)

                            headerRow.addView(headerTextCol)
                            rootLayout.addView(headerRow)

                            // 2. Body Text
                            val bodyView = TextView(ctx).apply {
                                id = View.generateViewId()
                                textSize = 13f
                                setTextColor(if (isDarkTheme) 0xFFC9B8A4.toInt() else 0xFF5C4736.toInt())
                                maxLines = 2
                                ellipsize = TextUtils.TruncateAt.END
                                setPadding(
                                    0,
                                    (8 * ctx.resources.displayMetrics.density).toInt(),
                                    0,
                                    (8 * ctx.resources.displayMetrics.density).toInt()
                                )
                            }
                            rootLayout.addView(bodyView)

                            // 3. MediaView
                            val mediaView = MediaView(ctx).apply {
                                id = View.generateViewId()
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (130 * ctx.resources.displayMetrics.density).toInt()
                                ).apply {
                                    bottomMargin = (10 * ctx.resources.displayMetrics.density).toInt()
                                }
                            }
                            rootLayout.addView(mediaView)

                            // 4. Call to Action Button
                            val ctaButton = Button(ctx).apply {
                                id = View.generateViewId()
                                textSize = 13f
                                setTypeface(typeface, Typeface.BOLD)
                                setTextColor(0xFFFFFFFF.toInt())
                                val bgDrawable = GradientDrawable().apply {
                                    cornerRadius = 14 * ctx.resources.displayMetrics.density
                                    setColor(0xFFC04B00.toInt())
                                }
                                background = bgDrawable
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (48 * ctx.resources.displayMetrics.density).toInt()
                                )
                            }
                            rootLayout.addView(ctaButton)

                            nativeAdView.addView(rootLayout)

                            nativeAdView.headlineView = headlineView
                            nativeAdView.bodyView = bodyView
                            nativeAdView.callToActionView = ctaButton
                            nativeAdView.iconView = iconView
                            nativeAdView.mediaView = mediaView
                            nativeAdView.advertiserView = advertiserView

                            nativeAdView
                        },
                        update = { nativeAdView ->
                            (nativeAdView.headlineView as? TextView)?.text = activeAd.headline
                            (nativeAdView.bodyView as? TextView)?.apply {
                                text = activeAd.body
                                visibility = if (activeAd.body.isNullOrBlank()) View.GONE else View.VISIBLE
                            }
                            (nativeAdView.callToActionView as? Button)?.apply {
                                text = activeAd.callToAction ?: "LEARN MORE"
                                visibility = if (activeAd.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
                            }
                            (nativeAdView.iconView as? ImageView)?.apply {
                                if (activeAd.icon?.drawable != null) {
                                    setImageDrawable(activeAd.icon?.drawable)
                                    visibility = View.VISIBLE
                                } else {
                                    visibility = View.GONE
                                }
                            }
                            (nativeAdView.mediaView as? MediaView)?.apply {
                                if (activeAd.mediaContent != null) {
                                    mediaContent = activeAd.mediaContent
                                    visibility = View.VISIBLE
                                } else {
                                    visibility = View.GONE
                                }
                            }
                            (nativeAdView.advertiserView as? TextView)?.apply {
                                val advertiserText = activeAd.advertiser ?: activeAd.store
                                text = advertiserText
                                visibility = if (advertiserText.isNullOrBlank()) View.GONE else View.VISIBLE
                            }
                            nativeAdView.setNativeAd(activeAd)
                        }
                    )
                }
            }
        }
    }
}
