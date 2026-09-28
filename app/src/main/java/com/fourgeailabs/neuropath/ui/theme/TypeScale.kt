package com.fourgeailabs.neuropath.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Complete Material 3 type scale set in the Fredoka font family.
 *
 * Ad-hoc .sp values found across ui/ map to the nearest slot like this:
 *  48/40/38/36/32sp -> displayLarge/Medium/Small
 *  30/28/26/24sp    -> displaySmall / headlineLarge
 *  22/20/19/18sp    -> headlineLarge / headlineMedium / headlineSmall
 *  17/16/15/14sp    -> titleLarge / titleMedium / bodyLarge / bodyMedium
 *  13/12.5/12sp     -> bodyMedium / bodySmall
 *  11.5/11/10.5/10sp-> bodySmall (minimum body text is 12sp — see microtext)
 *  9.5/9/8.5/8sp    -> below minimum; flagged microtext, not a scale slot
 *
 * The OpenDyslexic/dyslexia path: [getDyslexiaTypography] stays the single
 * entry point used by MainActivity (wider letter spacing, taller line height,
 * Medium base weight when the profile's dyslexia font toggle is on). This
 * scale keeps that mechanism intact — [typography] accepts the same toggle
 * and must keep matching its behaviour if it is migrated.
 */
object TypeScale {

    // ---- Display ----
    val displayLarge = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.5.sp,
    )
    val displayMedium = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.5.sp,
    )
    val displaySmall = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.5.sp,
    )

    // ---- Headline ----
    val headlineLarge = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.5.sp,
    )
    val headlineMedium = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.5.sp,
    )
    val headlineSmall = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    )

    // ---- Title ----
    val titleLarge = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    )
    val titleMedium = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.5.sp,
    )
    val titleSmall = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp,
    )

    // ---- Body (minimum body text: 12sp) ----
    val bodyLarge = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    )

    // ---- Label ----
    val labelLarge = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp,
    )
    val labelMedium = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = FredokaFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    )

    /**
     * Full Material 3 Typography built from this scale, honouring the
     * dyslexia typography toggle exactly the way [getDyslexiaTypography] does:
     * 1.2sp letter spacing, 1.5x line height and a heavier base weight when
     * enabled; 0.5sp spacing and 1.3x line height otherwise.
     */
    fun typography(dyslexiaEnabled: Boolean = false): Typography {
        val letterSpacing: TextUnit = if (dyslexiaEnabled) 1.2.sp else 0.5.sp
        val lineHeightMultiplier: Float = if (dyslexiaEnabled) 1.5f else 1.3f
        fun TextStyle.adapted(): TextStyle = copy(
            letterSpacing = letterSpacing,
            lineHeight = lineHeight * lineHeightMultiplier,
            fontWeight = when {
                dyslexiaEnabled && (this == bodyLarge || this == bodyMedium) -> FontWeight.Medium
                else -> fontWeight
            },
        )
        return Typography(
            displayLarge = displayLarge.adapted(),
            displayMedium = displayMedium.adapted(),
            displaySmall = displaySmall.adapted(),
            headlineLarge = headlineLarge.adapted(),
            headlineMedium = headlineMedium.adapted(),
            headlineSmall = headlineSmall.adapted(),
            titleLarge = titleLarge.adapted(),
            titleMedium = titleMedium.adapted(),
            titleSmall = titleSmall.adapted(),
            bodyLarge = bodyLarge.adapted(),
            bodyMedium = bodyMedium.adapted(),
            bodySmall = bodySmall.adapted(),
            labelLarge = labelLarge.adapted(),
            labelMedium = labelMedium.adapted(),
            labelSmall = labelSmall.adapted(),
        )
    }
}
