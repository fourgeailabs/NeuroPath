package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.data.model.WorldTheme

// Soft soothing sensory-friendly base colors
val PastelLavender = Color(0xFFE8E5F8)
val PastelTeal = Color(0xFFD8F3DC)
val PastelPeach = Color(0xFFFFE8D6)
val PastelSky = Color(0xFFE2F0D9)
val ButtercreamBg = Color(0xFFFAF7EE)
val ButtercreamCard = Color(0xFFF0EBD8)

val TwilightBg = Color(0xFF1E212B)
val TwilightSurface = Color(0xFF2B2F3E)
val TwilightCard = Color(0xFF383D50)

fun getThemeColorScheme(
    worldTheme: WorldTheme,
    contrastMode: String,
    darkTheme: Boolean = false
): ColorScheme {
    val primary = Color(worldTheme.primaryHex)
    val secondary = Color(worldTheme.secondaryHex)
    val tertiary = Color(0xFFF4A261)

    return when (contrastMode) {
        "TWILIGHT_DARK" -> darkColorScheme(
            primary = secondary,
            secondary = Color(0xFF81C784),
            tertiary = Color(0xFFFFB74D),
            background = TwilightBg,
            surface = TwilightSurface,
            surfaceVariant = TwilightCard,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color(0xFFE8EAED),
            onSurface = Color(0xFFF1F3F4)
        )
        "BUTTERCREAM" -> lightColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = ButtercreamBg,
            surface = ButtercreamCard,
            surfaceVariant = Color(0xFFE5DFC5),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2C2523),
            onSurface = Color(0xFF2C2523)
        )
        "MINT" -> lightColorScheme(
            primary = Color(0xFF2D6A4F),
            secondary = Color(0xFF52B788),
            tertiary = Color(0xFF74C69D),
            background = Color(0xFFF3FBF7),
            surface = Color(0xFFE3F6EC),
            surfaceVariant = Color(0xFFD0F0E0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1B382B),
            onSurface = Color(0xFF1B382B)
        )
        "HIGH_CONTRAST" -> lightColorScheme(
            primary = Color(0xFF0D3B66),
            secondary = Color(0xFFF95738),
            tertiary = Color(0xFFEE964B),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEBEBEB),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000)
        )
        else -> lightColorScheme( // VIBRANT PALETTE default
            primary = VibrantPrimary,
            onPrimary = VibrantOnPrimary,
            primaryContainer = VibrantPrimaryContainer,
            onPrimaryContainer = VibrantOnPrimaryContainer,
            secondary = VibrantSecondary,
            onSecondary = VibrantOnSecondary,
            secondaryContainer = VibrantSecondaryContainer,
            onSecondaryContainer = VibrantOnSecondaryContainer,
            tertiary = VibrantTertiary,
            onTertiary = VibrantOnTertiary,
            tertiaryContainer = VibrantTertiaryContainer,
            onTertiaryContainer = VibrantOnTertiaryContainer,
            background = VibrantBackground,
            onBackground = VibrantOnBackground,
            surface = VibrantSurface,
            onSurface = VibrantOnSurface,
            surfaceVariant = VibrantSurfaceVariant,
            onSurfaceVariant = VibrantOnSurfaceVariant,
            outline = VibrantOutline,
            outlineVariant = VibrantOutlineVariant
        )
    }
}

// Dyslexia & Sensory Typography Builder
fun getDyslexiaTypography(isDyslexiaEnabled: Boolean): androidx.compose.material3.Typography {
    val letterSpacing: TextUnit = if (isDyslexiaEnabled) 1.2.sp else 0.5.sp
    val lineHeightMultiplier: Float = if (isDyslexiaEnabled) 1.5f else 1.3f

    return androidx.compose.material3.Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp * lineHeightMultiplier,
            letterSpacing = letterSpacing
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp * lineHeightMultiplier,
            letterSpacing = letterSpacing
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp * lineHeightMultiplier,
            letterSpacing = letterSpacing
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isDyslexiaEnabled) FontWeight.Medium else FontWeight.Normal,
            fontSize = if (isDyslexiaEnabled) 18.sp else 16.sp,
            lineHeight = 26.sp * lineHeightMultiplier,
            letterSpacing = letterSpacing
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isDyslexiaEnabled) FontWeight.Medium else FontWeight.Normal,
            fontSize = if (isDyslexiaEnabled) 15.sp else 14.sp,
            lineHeight = 22.sp * lineHeightMultiplier,
            letterSpacing = letterSpacing
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = letterSpacing
        )
    )
}
