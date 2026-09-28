package com.fourgeailabs.neuropath.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens for NeuroPath, codified from the palettes already
 * used by [getThemeColorScheme] and Color.kt — no new palette invented here.
 *
 * The Light set mirrors the VIBRANT default palette; the Dark set mirrors the
 * auto-dark Twilight scheme (system dark mode / manual override keep working
 * exactly as before). Theme previews (Buttercream, Mint, High Contrast) keep
 * deriving their schemes dynamically — these tokens cover the app's shared
 * semantic surfaces and status colours so screens stop hardcoding hexes.
 */
data class NeuroPathColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    /** Main card / sheet surface. */
    val surface1: Color,
    /** Secondary surface (list rows, chips, input fields). */
    val surface2: Color,
    /** Tertiary / pressed surface (dividers, toggles off-state). */
    val surface3: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val outline: Color,
    /** One accent per buddy mascot, drawn from palette values already in the theme files. */
    val buddyAccents: List<Color>,
)

object ColorTokens {
    /**
     * 14 buddy-mascot accents, one per buddy. Every value already appears in
     * the existing theme files (Vibrant / Twilight / Mint / High Contrast /
     * Pastel values) — codified, not invented.
     */
    val buddyAccentRamp = listOf(
        Color(0xFF00629D), // VibrantPrimary
        Color(0xFF004977), // VibrantSecondary
        Color(0xFFF4A261), // tertiary sand
        Color(0xFFA04D23), // VibrantTertiary
        Color(0xFF81C784), // twilight/mint green
        Color(0xFFFFB74D), // twilight amber
        Color(0xFF52B788), // mint secondary
        Color(0xFF2D6A4F), // mint primary
        Color(0xFFF95738), // high-contrast coral
        Color(0xFF0D3B66), // high-contrast navy
        Color(0xFFE8E5F8), // PastelLavender
        Color(0xFFFFE8D6), // PastelPeach
        Color(0xFFD8F3DC), // PastelTeal
        Color(0xFFE2F0D9), // PastelSky
    )

    val Light = NeuroPathColors(
        primary = VibrantPrimary,
        onPrimary = VibrantOnPrimary,
        secondary = VibrantSecondary,
        onSecondary = VibrantOnSecondary,
        tertiary = VibrantTertiary,
        onTertiary = VibrantOnTertiary,
        background = VibrantBackground,
        onBackground = VibrantOnBackground,
        surface1 = VibrantSurface,
        surface2 = VibrantSurfaceVariant,
        surface3 = VibrantOutlineVariant,
        onSurface = VibrantOnSurface,
        onSurfaceVariant = VibrantOnSurfaceVariant,
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        success = Color(0xFF2E7D32),
        onSuccess = Color.White,
        warning = Color(0xFFF4A261),
        onWarning = Color(0xFF1E212B),
        outline = VibrantOutline,
        buddyAccents = buddyAccentRamp,
    )

    val Dark = NeuroPathColors(
        primary = VibrantPrimary,
        onPrimary = Color.White,
        secondary = Color(0xFF81C784),
        onSecondary = Color.Black,
        tertiary = Color(0xFFFFB74D),
        onTertiary = Color.Black,
        background = TwilightBg,
        onBackground = Color(0xFFE8EAED),
        surface1 = TwilightSurface,
        surface2 = TwilightCard,
        surface3 = TwilightBg,
        onSurface = Color(0xFFF1F3F4),
        onSurfaceVariant = Color(0xFFB9BEC9),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        success = Color(0xFF81C784),
        onSuccess = Color(0xFF1E212B),
        warning = Color(0xFFFFB74D),
        onWarning = Color(0xFF1E212B),
        outline = VibrantSecondary,
        buddyAccents = buddyAccentRamp,
    )
}
