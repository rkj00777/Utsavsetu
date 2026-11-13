package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.example.R

val PlayfairFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.3).sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    displaySmall = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    headlineLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    headlineSmall = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    titleLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.5.sp,
        lineHeight = 22.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.15.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.None
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 23.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.None
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.None
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.25.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None
    )
)
