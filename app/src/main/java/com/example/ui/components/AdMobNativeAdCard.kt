package com.example.ui.components

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.platform.ads.ADMOB_NATIVE_AD_UNIT_ID
import com.example.platform.ads.ADMOB_NATIVE_TEST_UNIT_ID
import com.example.platform.ads.rememberAdMobNativeAd
import com.example.ui.theme.LocalLiquidGlassTokens
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Google AdMob Native Ad Card Component for UtsavSetu.
 *
 * Implements Google Play Store & Google AdMob Native Ad specifications:
 * 1. Production Ad Unit: ca-app-pub-4148485857290947/5042099689
 * 2. Unambiguous "AD" badge attribution complying with Google Play Developer Policy.
 * 3. Soft-press tactile touch response (`softPressCardEffect`) matching festival cards.
 * 4. Dual rendering:
 *    - Real Google AdMob `NativeAdView` with registered headline, body, callToAction, icon, and media views.
 *    - High-fidelity devotional preview mode when offline, loading, or in automated test environments.
 * 5. Dismissible with 'X' button so user retains control.
 */
@Composable
fun AdMobNativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID,
    showDismissButton: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }
    val adLoader = rememberAdMobNativeAd(adUnitId = adUnitId)
    val adState = adLoader.state
    val glassTokens = LocalLiquidGlassTokens.current
    val context = LocalContext.current
    val softPress = rememberSoftPressController()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .softPressCardEffect(softPress)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = if (glassTokens.isDark) Color(0x66000000) else Color(0x2838271A),
                    spotColor = if (glassTokens.isDark) Color(0x38D9A441) else Color(0x33453322)
                )
                .testTag("admob_native_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = glassTokens.solidCardContainer),
            border = BorderStroke(
                width = 1.2.dp,
                color = if (glassTokens.isDark) glassTokens.decorativeGold.copy(alpha = 0.5f) else Color(0xFFD6C3AA)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Header Strip: "AD" Badge, Unit ID Tag, Status & Dismiss Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // High-contrast "AD" badge
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

                        Column {
                            Text(
                                text = "Google AdMob Native",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = glassTokens.decorativeGold
                            )
                            Text(
                                text = "Unit: ...${adUnitId.takeLast(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Refresh button to test ad reloading
                        IconButton(
                            onClick = { adLoader.loadAd() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("reload_native_ad_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload ad",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showDismissButton) {
                            IconButton(
                                onClick = {
                                    isVisible = false
                                    onDismiss?.invoke()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("dismiss_native_ad_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss ad",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Native Ad Content: Real AdMob View or High-Fidelity Test Preview
                if (adState.nativeAd != null) {
                    val activeAd = adState.nativeAd
                    val isDarkTheme = glassTokens.isDark

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                                setPadding(0, (8 * ctx.resources.displayMetrics.density).toInt(), 0, (8 * ctx.resources.displayMetrics.density).toInt())
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
                                    (44 * ctx.resources.displayMetrics.density).toInt()
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
                                text = activeAd.advertiser ?: activeAd.store ?: "Sponsored Ad"
                                visibility = View.VISIBLE
                            }
                            nativeAdView.setNativeAd(activeAd)
                        }
                    )
                } else {
                    // Compose High-Fidelity Test Preview Card (Offline, Loading, or Test Bed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(glassTokens.decorativeGold.copy(alpha = 0.16f))
                                    .border(1.dp, glassTokens.decorativeGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ॐ",
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pure Brass Akhand Diya & Puja Thali Set",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Sponsored • Vedic Craft Heritage (Google Test Ad)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassTokens.decorativeGold,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Handcrafted pure brass Panchpatra, Puja Bell, and Akhand Diya for auspicious festival rituals and daily mandir devotion. Verified Vedic purity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = glassTokens.decorativeGold,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.9 (1,480+ Devotee Reviews)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    softPress.triggerSoftClick {
                                        // Open official Google test ad click destination or demo product link
                                        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://googleads.github.io/googleads-mobile-android-examples/"))
                                        runCatching { context.startActivity(testIntent) }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("native_ad_cta_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VIEW PUJA COLLECTION",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Status Note & Diagnostic feedback
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (adState.isLoading) {
                                    "AdMob Request: Requesting live test ad..."
                                } else if (adState.errorMessage != null) {
                                    "AdMob Code ${adState.errorCode ?: 0}: ${adState.errorMessage}"
                                } else {
                                    "Native Ad Unit • ca-app-pub-4148485857290947/5042099689"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
