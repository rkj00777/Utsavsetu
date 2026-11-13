package com.example.platform.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.Festival
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object ShareHelper {

    /**
     * Renders a styled 1080x1350 Indian Luxury festival greeting & Muhurat card bitmap
     * and writes it to the app cache for sharing via FileProvider.
     */
    fun generateShareCardUri(
        context: Context,
        festival: Festival,
        dateFormatted: String,
        muhuratWindow: String,
        cityName: String
    ): Uri? {
        return try {
            val width = 1080
            val height = 1350
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Deep Vermilion & Plum Luxury Gradient Background
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#471224"),
                        Color.parseColor("#7A1C1C"),
                        Color.parseColor("#28122B")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Outer & Inner Gold Filigree Borders
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.parseColor("#E5A93C")
            }
            canvas.drawRoundRect(RectF(48f, 48f, width - 48f, height - 48f), 42f, 42f, borderPaint)

            borderPaint.strokeWidth = 2f
            borderPaint.alpha = 140
            canvas.drawRoundRect(RectF(68f, 68f, width - 68f, height - 68f), 32f, 32f, borderPaint)

            // 3. Sacred Mandala Aura Header Circle
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#33FFCA28")
            }
            canvas.drawCircle(width / 2f, 250f, 110f, glowPaint)
            borderPaint.alpha = 210
            canvas.drawCircle(width / 2f, 250f, 82f, borderPaint)

            // 4. Top Brand Tag
            val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF80")
                textSize = 32f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.15f
            }
            canvas.drawText("UTSAVSETU • SACRED INVITATION", width / 2f, 140f, tagPaint)

            // 5. Native Script Greeting & English Title
            val nativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFE082")
                textSize = 46f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            canvas.drawText(festival.nameNative.take(36), width / 2f, 430f, nativePaint)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 60f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            val displayTitle = if (festival.nameEnglish.length > 28) {
                festival.nameEnglish.substringBefore("(").trim()
            } else {
                festival.nameEnglish
            }
            canvas.drawText("Shubh $displayTitle", width / 2f, 520f, titlePaint)

            // 6. Translucent Cream/Glass Info Card for Date, City & Muhurat
            val cardRect = RectF(108f, 600f, width - 108f, 1030f)
            val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#26FFF8EE")
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, cardFillPaint)
            canvas.drawRoundRect(cardRect, 36f, 36f, borderPaint)

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF80")
                textSize = 30f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
            }
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 42f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            canvas.drawText("OBSERVANCE DATE & CITY", width / 2f, 680f, labelPaint)
            canvas.drawText("$dateFormatted • $cityName", width / 2f, 742f, valuePaint)

            canvas.drawText("VERIFIED PUJA MUHURAT ($cityName)", width / 2f, 855f, labelPaint)
            val muhuratPaint = Paint(valuePaint).apply {
                color = Color.parseColor("#FFD54F")
                textSize = 48f
            }
            canvas.drawText(muhuratWindow, width / 2f, 925f, muhuratPaint)

            val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E6DCCD")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(festival.muhuratTitle.take(48), width / 2f, 985f, subLabelPaint)

            // 7. Footer Blessing
            canvas.drawText(
                "May this sacred festival bring peace, health & prosperity to your home.",
                width / 2f,
                1145f,
                subLabelPaint
            )
            tagPaint.textSize = 26f
            canvas.drawText("UtsavSetu • Your Festivals. Your Traditions.", width / 2f, 1235f, tagPaint)

            val safeFestivalId = com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId(festival.id) ?: "festival"
            val cacheDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val imageFile = File(cacheDir, "utsavsetu_${safeFestivalId}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds a Google Play Store URL for UtsavSetu that:
     * 1. Opens the Google Play Store listing for installation if the receiver does not have the app installed.
     * 2. Opens directly to the specific Festival Detail page inside UtsavSetu if the app is already installed
     *    on the receiver's device (via the Play Store intent-filter in AndroidManifest.xml).
     */
    fun buildPlayStoreFestivalLink(context: Context, festivalId: String): String {
        val pkg = context.packageName
        val safeFestivalId = com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId(festivalId) ?: "diwali_lakshmi_puja"
        val encodedFestivalId = Uri.encode(safeFestivalId)
        return "https://play.google.com/store/apps/details?id=$pkg&festival_id=$encodedFestivalId&referrer=festival_id%3D$encodedFestivalId"
    }

    /**
     * Blueprint Section 13: Clean share-card format with explicit date, city-specific timing,
     * short greeting, UtsavSetu branding, Google Play Store link, AND rendered PNG share card attachment.
     */
    fun shareFestivalSummary(
        context: Context,
        festival: Festival,
        date: LocalDate,
        muhuratWindow: String,
        cityName: String
    ) {
        val dateFormatted = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
        val playStoreLink = buildPlayStoreFestivalLink(context, festival.id)

        val vidhiOverview = festival.vidhiSteps.joinToString("\n") { step ->
            "• Step ${step.stepNumber}: ${step.title}"
        }

        val shareText = """
✨ *Shubh ${festival.nameEnglish}* ✨
${festival.nameNative}

📅 *Observance Date:* $dateFormatted
📍 *Location:* $cityName (IST)
🕰️ *Verified Puja Muhurat ($cityName):* $muhuratWindow

🌸 ${festival.significance.take(160)}${if (festival.significance.length > 160) "…" else ""}

🙏 *Puja Vidhi Highlights (${festival.vidhiSteps.size} Steps):*
$vidhiOverview

📲 *Open in UtsavSetu or Install from Google Play Store:*
$playStoreLink

— _Shared via UtsavSetu · Your Festivals. Your Traditions._
        """.trimIndent()

        val imageUri = generateShareCardUri(
            context = context,
            festival = festival,
            dateFormatted = dateFormatted,
            muhuratWindow = muhuratWindow,
            cityName = cityName
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (imageUri != null) {
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri("UtsavSetu Share Card", imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "image/png"
            } else {
                type = "text/plain"
            }
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share ${festival.nameEnglish}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    /**
     * Shares the complete Step-by-Step Puja Vidhi and Puja Samagri preparation checklist
     * via Android's native share sheet, including the Google Play Store install / deep-link URL.
     */
    fun shareSamagriChecklist(
        context: Context,
        festival: Festival,
        date: LocalDate,
        cityName: String,
        checkedIds: Set<String> = emptySet()
    ) {
        val dateFormatted = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
        val playStoreLink = buildPlayStoreFestivalLink(context, festival.id)
        val vidhiList = festival.vidhiSteps.joinToString("\n\n") { step ->
            val durationLabel = step.durationMinutes?.let { " (~$it min)" } ?: ""
            "*${step.stepNumber}. ${step.title}*$durationLabel\n${step.body}"
        }
        val itemsList = festival.samagriItems.joinToString("\n") { item ->
            val mark = if (checkedIds.contains(item.id)) "☑" else "☐"
            val qty = item.quantity?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
            "$mark ${item.name}$qty"
        }

        val shareText = """
🪔 *${festival.nameEnglish} — Puja Vidhi & Samagri Guide*
📅 $dateFormatted · 📍 $cityName

🙏 *Step-by-Step Puja Vidhi (${festival.vidhiSteps.size} Steps):*
$vidhiList

🧺 *Puja Samagri Checklist (${festival.samagriItems.size} Items):*
$itemsList

📲 *Open in UtsavSetu or Install from Google Play Store:*
$playStoreLink

— _Prepared with UtsavSetu_
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share ${festival.nameEnglish} Vidhi & Samagri").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }
}
