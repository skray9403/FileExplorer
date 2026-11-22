package com.explorer.fileexplorer.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// M3 Expressive Seeded Fallback Palettes (used when Dynamic Color is unavailable or disabled)
val ExpressiveLightColorScheme = lightColorScheme(
    primary = Color(0xFF1F5D46),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8F2CE),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF4C6358),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DA),
    onSecondaryContainer = Color(0xFF092017),
    tertiary = Color(0xFF3E6374),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC2E8FC),
    onTertiaryContainer = Color(0xFF001F2A),
    background = Color(0xFFF6FAF6),
    onBackground = Color(0xFF181D1A),
    surface = Color(0xFFF6FAF6),
    onSurface = Color(0xFF181D1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404944),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F5F1),
    surfaceContainer = Color(0xFFEAEFEB),
    surfaceContainerHigh = Color(0xFFE4EAE5),
    surfaceContainerHighest = Color(0xFFDFE4E0),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

val ExpressiveDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CD5B3),
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF00513A),
    onPrimaryContainer = Color(0xFFA8F2CE),
    secondary = Color(0xFFB2CCBE),
    onSecondary = Color(0xFF1E352B),
    secondaryContainer = Color(0xFF354B41),
    onSecondaryContainer = Color(0xFFCEE9DA),
    tertiary = Color(0xFFA6CCE0),
    onTertiary = Color(0xFF093544),
    tertiaryContainer = Color(0xFF254B5C),
    onTertiaryContainer = Color(0xFFC2E8FC),
    background = Color(0xFF101512),
    onBackground = Color(0xFFDFE4E0),
    surface = Color(0xFF101512),
    onSurface = Color(0xFFDFE4E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C2),
    surfaceContainerLowest = Color(0xFF0B0F0D),
    surfaceContainerLow = Color(0xFF181D1A),
    surfaceContainer = Color(0xFF1C211E),
    surfaceContainerHigh = Color(0xFF262B28),
    surfaceContainerHighest = Color(0xFF313633),
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF404944),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

val ExpressiveOledColorScheme = ExpressiveDarkColorScheme.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0C100E),
    surfaceContainer = Color(0xFF141916),
    surfaceContainerHigh = Color(0xFF1C221F),
    surfaceContainerHighest = Color(0xFF262C29),
)

// Legacy compatibility tokens (retained so untouched modules compile without changes)
val DarkBackground = Color(0xFF101512)
val DarkSurface = Color(0xFF181D1A)
val DarkSurfaceVariant = Color(0xFF1C211E)
val DarkSurfaceElevated = Color(0xFF262B28)
val DarkBorder = Color(0xFF404944)

val OledBackground = Color(0xFF000000)
val OledSurface = Color(0xFF0C100E)
val OledSurfaceVariant = Color(0xFF141916)
val OledSurfaceElevated = Color(0xFF1C221F)
val OledBorder = Color(0xFF262C29)

val LightBackground = Color(0xFFF6FAF6)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAEFEB)
val LightSurfaceElevated = Color(0xFFE4EAE5)
val LightBorder = Color(0xFFBFC9C2)
val LightTextPrimary = Color(0xFF181D1A)
val LightTextSecondary = Color(0xFF404944)

val LightOnSurfaceMuted = Color(0xFF56605A)
val DarkOnSurfaceMuted = Color(0xFFA2ACA6)

val AccentCyan = Color(0xFF8CD5B3)
val AccentCyanDark = Color(0xFF1F5D46)
val AccentCyanLight = Color(0xFFA8F2CE)
val AccentPurple = Color(0xFFA6CCE0)
val AccentGreen = Color(0xFF4CAF50)
val AccentOrange = Color(0xFFFF9800)
val AccentRed = Color(0xFFEF5350)
val AccentYellow = Color(0xFFFFEB3B)

val TextPrimary = Color(0xFFDFE4E0)
val TextSecondary = Color(0xFFBFC9C2)
val TextDisabled = Color(0xFF6F7973)

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

object MonoOpacity {
    const val heading = 1.0f
    const val body = 0.80f
    const val secondary = 0.60f
    const val tertiary = 0.45f
    const val disabled = 0.30f
    const val subtle = 0.15f
    const val whisper = 0.08f
}

object MonoShadows {
    val light = Color(0x1A000000)
    val medium = Color(0x26000000)
    val strong = Color(0x33000000)
    val dark = Color(0x1AFFFFFF)
    val darkMedium = Color(0x26FFFFFF)
}

data class FolioExtendedColors(
    val onSurfaceMuted: Color,
)

val LightExtendedColors = FolioExtendedColors(onSurfaceMuted = LightOnSurfaceMuted)
val DarkExtendedColors = FolioExtendedColors(onSurfaceMuted = DarkOnSurfaceMuted)

val LocalFolioExtendedColors = staticCompositionLocalOf { DarkExtendedColors }
