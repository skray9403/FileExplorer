package com.explorer.fileexplorer.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, OLED, DYNAMIC;

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.name == key } ?: DYNAMIC
    }
}

/**
 * Clean Material 3 Expressive typography scale with proper line heights for calm file legibility.
 */
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

object FolioTheme {
    val extendedColors: FolioExtendedColors
        @Composable get() = LocalFolioExtendedColors.current
}

@Composable
fun FileExplorerTheme(
    themeMode: ThemeMode = ThemeMode.DYNAMIC,
    cornerStyle: String = "ROUNDED",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.OLED -> true
        ThemeMode.DYNAMIC, ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> if (supportsDynamic) dynamicLightColorScheme(context) else ExpressiveLightColorScheme
        ThemeMode.DARK -> if (supportsDynamic) dynamicDarkColorScheme(context) else ExpressiveDarkColorScheme
        ThemeMode.OLED -> {
            if (supportsDynamic) {
                dynamicDarkColorScheme(context).copy(
                    background = OledBackground,
                    surface = OledBackground,
                    surfaceContainerLowest = OledBackground,
                )
            } else {
                ExpressiveOledColorScheme
            }
        }
        ThemeMode.DYNAMIC, ThemeMode.SYSTEM -> {
            if (supportsDynamic) {
                if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemDark) ExpressiveDarkColorScheme else ExpressiveLightColorScheme
            }
        }
    }

    val extendedColors = if (isDark) DarkExtendedColors else LightExtendedColors
    val shapes = getFolioShapes(cornerStyle)

    CompositionLocalProvider(
        LocalFolioExtendedColors provides extendedColors,
        LocalCornerStyle provides cornerStyle,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = shapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
