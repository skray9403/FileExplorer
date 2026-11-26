package com.explorer.fileexplorer.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

enum class FolioAccentPalette {
    SAGE, OCEAN, VIOLET, TERRACOTTA;

    companion object {
        fun fromKey(key: String?): FolioAccentPalette =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: SAGE
    }
}

// 1. Sage (Default M3 Expressive Fallback)
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

// 2. Ocean
private val OceanLightScheme = lightColorScheme(
    primary = Color(0xFF1D5D8C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF51606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E4F7),
    onSecondaryContainer = Color(0xFF0E1D2A),
    tertiary = Color(0xFF67587A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDDCFF),
    onTertiaryContainer = Color(0xFF221533),
    background = Color(0xFFF7F9FD),
    onBackground = Color(0xFF181C20),
    surface = Color(0xFFF7F9FD),
    onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4F9),
    surfaceContainer = Color(0xFFEBEEF3),
    surfaceContainerHigh = Color(0xFFE5E8ED),
    surfaceContainerHighest = Color(0xFFDFE3E8),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CF),
)

private val OceanDarkScheme = darkColorScheme(
    primary = Color(0xFF9ACBFB),
    onPrimary = Color(0xFF003354),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFCFE5FF),
    secondary = Color(0xFFB9C8DA),
    onSecondary = Color(0xFF233240),
    secondaryContainer = Color(0xFF3A4857),
    onSecondaryContainer = Color(0xFFD5E4F7),
    tertiary = Color(0xFFD2BFE7),
    onTertiary = Color(0xFF382A49),
    tertiaryContainer = Color(0xFF4F4061),
    onTertiaryContainer = Color(0xFFEDDCFF),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE0E2E8),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE0E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    surfaceContainerLowest = Color(0xFF0B0E12),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF262A2F),
    surfaceContainerHighest = Color(0xFF31353A),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),
)

// 3. Violet
private val VioletLightScheme = lightColorScheme(
    primary = Color(0xFF645096),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF200B4E),
    secondary = Color(0xFF615B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5262),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E4),
    onTertiaryContainer = Color(0xFF31101E),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFAF8FF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFE6E0EC),
    onSurfaceVariant = Color(0xFF48454F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F1FA),
    surfaceContainer = Color(0xFFEEEBF4),
    surfaceContainerHigh = Color(0xFFE8E5EE),
    surfaceContainerHighest = Color(0xFFE2DFE8),
    outline = Color(0xFF79757F),
    outlineVariant = Color(0xFFCAC4D0),
)

private val VioletDarkScheme = darkColorScheme(
    primary = Color(0xFFCEBDFF),
    onPrimary = Color(0xFF352164),
    primaryContainer = Color(0xFF4C387C),
    onPrimaryContainer = Color(0xFFE8DDFF),
    secondary = Color(0xFFCBC3DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF494458),
    onSecondaryContainer = Color(0xFFE7DEF8),
    tertiary = Color(0xFFEFB8CA),
    onTertiary = Color(0xFF492534),
    tertiaryContainer = Color(0xFF633B4A),
    onTertiaryContainer = Color(0xFFFFD9E4),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF48454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF0E0D12),
    surfaceContainerLow = Color(0xFF1C1B20),
    surfaceContainer = Color(0xFF201F25),
    surfaceContainerHigh = Color(0xFF2A292F),
    surfaceContainerHighest = Color(0xFF35343A),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF48454F),
)

// 4. Terracotta
private val TerracottaLightScheme = lightColorScheme(
    primary = Color(0xFF934B32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF390D00),
    secondary = Color(0xFF77574D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF2C160E),
    tertiary = Color(0xFF6A5E2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E2A7),
    onTertiaryContainer = Color(0xFF221B00),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231916),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231916),
    surfaceVariant = Color(0xFFF4DDD6),
    onSurfaceVariant = Color(0xFF53433F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1ED),
    surfaceContainer = Color(0xFFFCEAE5),
    surfaceContainerHigh = Color(0xFFF6E4DF),
    surfaceContainerHighest = Color(0xFFF0DFDA),
    outline = Color(0xFF85736E),
    outlineVariant = Color(0xFFD8C2BC),
)

private val TerracottaDarkScheme = darkColorScheme(
    primary = Color(0xFFFFB59C),
    onPrimary = Color(0xFF581E09),
    primaryContainer = Color(0xFF75341D),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE7BDB0),
    onSecondary = Color(0xFF442A21),
    secondaryContainer = Color(0xFF5D4036),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFD7C68D),
    onTertiary = Color(0xFF393005),
    tertiaryContainer = Color(0xFF51461A),
    onTertiaryContainer = Color(0xFFF3E2A7),
    background = Color(0xFF1A110F),
    onBackground = Color(0xFFF0DFDA),
    surface = Color(0xFF1A110F),
    onSurface = Color(0xFFF0DFDA),
    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = Color(0xFFD8C2BC),
    surfaceContainerLowest = Color(0xFF140C0A),
    surfaceContainerLow = Color(0xFF231916),
    surfaceContainer = Color(0xFF271D1A),
    surfaceContainerHigh = Color(0xFF322824),
    surfaceContainerHighest = Color(0xFF3D322F),
    outline = Color(0xFFA08C87),
    outlineVariant = Color(0xFF53433F),
)

fun paletteSwatchColor(palette: FolioAccentPalette): Color = when (palette) {
    FolioAccentPalette.SAGE -> Color(0xFF2E7D5E)
    FolioAccentPalette.OCEAN -> Color(0xFF1D5D8C)
    FolioAccentPalette.VIOLET -> Color(0xFF645096)
    FolioAccentPalette.TERRACOTTA -> Color(0xFF934B32)
}

fun resolveStaticColorScheme(
    palette: FolioAccentPalette,
    isDark: Boolean,
    oled: Boolean = false,
): ColorScheme {
    val base = when (palette) {
        FolioAccentPalette.SAGE -> if (isDark) ExpressiveDarkColorScheme else ExpressiveLightColorScheme
        FolioAccentPalette.OCEAN -> if (isDark) OceanDarkScheme else OceanLightScheme
        FolioAccentPalette.VIOLET -> if (isDark) VioletDarkScheme else VioletLightScheme
        FolioAccentPalette.TERRACOTTA -> if (isDark) TerracottaDarkScheme else TerracottaLightScheme
    }
    return if (isDark && oled) {
        base.copy(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF0D0F0E),
            surfaceContainer = Color(0xFF141716),
            surfaceContainerHigh = Color(0xFF1D211F),
            surfaceContainerHighest = Color(0xFF272B29),
        )
    } else {
        base
    }
}

val ExpressiveOledColorScheme = resolveStaticColorScheme(FolioAccentPalette.SAGE, isDark = true, oled = true)

// Legacy compatibility tokens
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
