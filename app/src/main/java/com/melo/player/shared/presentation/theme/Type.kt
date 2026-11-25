/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/cromaguy>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Melo typography: Geom (display/headline/title) + Nunito (body/label).
 * Nunito is a variable font from Google Fonts under the SIL Open Font License 1.1.
 */

package com.melo.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import android.graphics.Typeface
import androidx.compose.ui.unit.sp
import com.melo.player.R

private fun meloVariations(weight: Int, opticalSize: Float): FontVariation.Settings =
    FontVariation.Settings(
        FontVariation.Setting("opsz", opticalSize),
        FontVariation.weight(weight)
    )

/** Geom: expressive geometric display face. */
val MeloDisplayFont = FontFamily(
    Font(R.font.geom, FontWeight.W200),
    Font(R.font.geom, FontWeight.W300),
    Font(R.font.geom, FontWeight.W400),
    Font(R.font.geom, FontWeight.W500),
    Font(R.font.geom, FontWeight.W600),
    Font(R.font.geom, FontWeight.W700),
    Font(R.font.geom, FontWeight.W800)
)

/** Nunito: variable body face (wght 200–1000). */
val MeloBodyFont = FontFamily(
    Font(R.font.nunito_variable, FontWeight.W200, variationSettings = meloVariations(200, 16f)),
    Font(R.font.nunito_variable, FontWeight.W300, variationSettings = meloVariations(300, 16f)),
    Font(R.font.nunito_variable, FontWeight.W400, variationSettings = meloVariations(400, 16f)),
    Font(R.font.nunito_variable, FontWeight.W500, variationSettings = meloVariations(500, 16f)),
    Font(R.font.nunito_variable, FontWeight.W600, variationSettings = meloVariations(600, 16f)),
    Font(R.font.nunito_variable, FontWeight.W700, variationSettings = meloVariations(700, 16f)),
    Font(R.font.nunito_variable, FontWeight.W800, variationSettings = meloVariations(800, 16f)),
    Font(R.font.nunito_variable, FontWeight.W900, variationSettings = meloVariations(900, 16f))
)

// Material Design 3 Typography Scale — Melo pairing.
// Display/headline/title use Geom for expressive hierarchy;
// body/label use Nunito for long-form readability in 27 languages.
val Typography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W700,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W700,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.35).sp
    ),
    displaySmall = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W600,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),

    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.15).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles
    titleLarge = TextStyle(
        fontFamily = MeloDisplayFont,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles
    bodyLarge = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles
    labelLarge = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MeloBodyFont,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Get typography for a font option. "Melo" (default) uses the Geom + Nunito
 * paired expressive scale; the rest apply one family across all roles.
 */
fun getTypographyForFont(fontName: String): Typography = when (fontName) {
    "Melo" -> Typography
    else -> createTypographyWithFontFamily(getFontFamilyByName(fontName))
}

/** Get typography with an externally loaded FontFamily (user-imported font). */
fun getTypographyWithCustomFont(customFontFamily: FontFamily?): Typography =
    createTypographyWithFontFamily(customFontFamily ?: FontFamily.Default)

fun getFontFamilyByName(fontName: String): FontFamily = when (fontName) {
    "Melo", "Geom" -> MeloDisplayFont
    "Nunito", "Modern" -> MeloBodyFont
    "Slate", "Classic" -> FontFamily(Typeface.SERIF)
    "JetBrains", "Typewriter" -> FontFamily(Typeface.MONOSPACE)
    "Quicksand", "Playful", "Casual" -> {
        val roundedTypeface = try {
            Typeface.create("sans-serif-rounded", Typeface.NORMAL)
        } catch (e: Exception) {
            null
        } ?: try {
            Typeface.create("casual", Typeface.NORMAL)
        } catch (e: Exception) {
            Typeface.SANS_SERIF
        }
        FontFamily(roundedTypeface)
    }
    "System" -> FontFamily(Typeface.DEFAULT)
    else -> MeloDisplayFont
}

/** Creates Typography with a single FontFamily across all M3 roles. */
private fun createTypographyWithFontFamily(fontFamily: FontFamily): Typography = Typography(
    displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W700, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W700, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = (-0.35).sp),
    displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp),
    headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.2).sp),
    headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.15).sp),
    headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.W600, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

/** Text style for font preview in theme customization. */
fun getFontPreviewStyle(fontName: String): TextStyle = TextStyle(
    fontFamily = getFontFamilyByName(fontName),
    fontWeight = FontWeight.W400,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

/** Text style for a user-imported font preview. */
fun getCustomFontPreviewStyle(customFontFamily: FontFamily?): TextStyle = TextStyle(
    fontFamily = customFontFamily ?: FontFamily.Default,
    fontWeight = FontWeight.W400,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)
