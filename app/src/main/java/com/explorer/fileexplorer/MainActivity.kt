package com.explorer.fileexplorer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.explorer.fileexplorer.core.designsystem.FileExplorerTheme
import com.explorer.fileexplorer.core.designsystem.FolioAccentPalette
import com.explorer.fileexplorer.core.designsystem.ThemeMode
import com.explorer.fileexplorer.preview.FolioPreviewRoot
import com.explorer.fileexplorer.preview.PreviewAppearanceState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var appearance by remember { mutableStateOf(PreviewAppearanceState()) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (appearance.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.OLED -> true
                ThemeMode.DYNAMIC, ThemeMode.SYSTEM -> systemDark
            }

            SideEffect {
                val barStyle = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle,
                )
            }

            FileExplorerTheme(
                themeMode = appearance.themeMode,
                useDynamicColor = appearance.useDynamicColor,
                accentPalette = appearance.accentPalette,
                cornerStyle = appearance.cornerStyle,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FolioPreviewRoot(
                        appearance = appearance,
                        onAppearanceChange = { appearance = it },
                    )
                }
            }
        }
    }
}
