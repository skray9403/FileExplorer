package com.explorer.fileexplorer.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// Primary palette — deep dark with cyan accent
val DarkBackground = Color(0xFF0D0D0D)
val DarkSurface = Color(0xFF161616)
val DarkSurfaceVariant = Color(0xFF1E1E1E)
val DarkSurfaceElevated = Color(0xFF252525)
val DarkBorder = Color(0xFF2A2A2A)

// OLED palette — pure-black background, lifts only on surface elevations.
// Standard Material black-mode pattern: AMOLED panels save real power on #000.
val OledBackground = Color(0xFF000000)
val OledSurface = Color(0xFF0A0A0A)
val OledSurfaceVariant = Color(0xFF141414)
val OledSurfaceElevated = Color(0xFF1F1F1F)
val OledBorder = Color(0xFF222222)

// Light palette — standard Material 3 light surfaces.
val LightBackground = Color(0xFFFAFAFA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEEEEE)
val LightSurfaceElevated = Color(0xFFE0E0E0)
val LightBorder = Color(0xFFCFCFCF)
val LightTextPrimary = Color(0xFF121212)
val LightTextSecondary = Color(0xFF4A4A4A)

// Monomail AA-contrast muted surface colors
val LightOnSurfaceMuted = Color(0xFF5C5C5C)
val DarkOnSurfaceMuted = Color(0xFFA0A0A0)

// Accent
val AccentCyan = Color(0xFF00BCD4)
val AccentCyanDark = Color(0xFF008C9E)
val AccentCyanLight = Color(0xFF4DD0E1)

// Secondary
val AccentPurple = Color(0xFFBB86FC)
val AccentGreen = Color(0xFF4CAF50)
val AccentOrange = Color(0xFFFF9800)
val AccentRed = Color(0xFFEF5350)
val AccentYellow = Color(0xFFFFEB3B)

// Text
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF9E9E9E)
val TextDisabled = Color(0xFF616161)

// File type colors
val ColorFolder = Color(0xFF90CAF9)
val ColorImage = Color(0xFFE91E63)
val ColorVideo = Color(0xFFFF5722)
val ColorAudio = Color(0xFF4CAF50)
val ColorArchive = Color(0xFFFF9800)
val ColorDocument = Color(0xFF2196F3)
val ColorCode = Color(0xFFCDDC39)
val ColorApk = Color(0xFF00BCD4)
val ColorDefault = Color(0xFF9E9E9E)

/**
 * Monomail opacity tokens for consistent text and icon hierarchy across Folio.
 */
object MonoOpacity {
    const val heading = 1.0f
    const val body = 0.80f
    const val secondary = 0.60f
    const val tertiary = 0.45f
    const val disabled = 0.30f
    const val subtle = 0.15f
    const val whisper = 0.08f
}

/**
 * Tinted shadow colors for Material 3 surfaces.
 */
object MonoShadows {
    val light = Color(0x1A000000)
    val medium = Color(0x26000000)
    val strong = Color(0x33000000)
    val dark = Color(0x1AFFFFFF)
    val darkMedium = Color(0x26FFFFFF)
}

/**
 * Extra tokens Material 3 ColorScheme doesn't have an explicit slot for.
 */
data class FolioExtendedColors(
    val onSurfaceMuted: Color,
)

val LightExtendedColors = FolioExtendedColors(onSurfaceMuted = LightOnSurfaceMuted)
val DarkExtendedColors = FolioExtendedColors(onSurfaceMuted = DarkOnSurfaceMuted)

val LocalFolioExtendedColors = staticCompositionLocalOf { LightExtendedColors }
