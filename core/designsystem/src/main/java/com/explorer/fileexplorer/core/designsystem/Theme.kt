package com.explorer.fileexplorer.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
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
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

object FolioTheme {
    val extendedColors: FolioExtendedColors
        @Composable get() = LocalFolioExtendedColors.current
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileExplorerTheme(
    themeMode: ThemeMode = ThemeMode.DYNAMIC,
    useDynamicColor: Boolean = true,
    accentPalette: FolioAccentPalette = FolioAccentPalette.SAGE,
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
    val isOled = themeMode == ThemeMode.OLED
    val applyDynamic = supportsDynamic && (useDynamicColor || themeMode == ThemeMode.DYNAMIC)

    val colorScheme = if (applyDynamic) {
        val dynamicBase = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (isOled) {
            dynamicBase.copy(
                background = OledBackground,
                surface = OledBackground,
                surfaceContainerLowest = OledBackground,
                surfaceContainerLow = Color(0xFF0D0F0E),
                surfaceContainer = Color(0xFF141716),
                surfaceContainerHigh = Color(0xFF1D211F),
                surfaceContainerHighest = Color(0xFF272B29),
            )
        } else {
            dynamicBase
        }
    } else {
        resolveStaticColorScheme(palette = accentPalette, isDark = isDark, oled = isOled)
    }

    val extendedColors = if (isDark) DarkExtendedColors else LightExtendedColors
    val shapes = getFolioShapes(cornerStyle)

    CompositionLocalProvider(
        LocalFolioExtendedColors provides extendedColors,
        LocalCornerStyle provides cornerStyle,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = shapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
