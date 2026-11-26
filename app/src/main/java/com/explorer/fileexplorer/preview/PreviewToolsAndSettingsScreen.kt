package com.explorer.fileexplorer.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.FolioAccentPalette
import com.explorer.fileexplorer.core.designsystem.FolioBottomScrim
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBottomBar
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.designsystem.ThemeMode
import com.explorer.fileexplorer.core.designsystem.paletteSwatchColor

@Composable
fun PreviewToolsAndSettingsScreen(
    appearance: PreviewAppearanceState,
    onAppearanceChange: (PreviewAppearanceState) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigate: (PreviewRoute) -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()
    var prevIdx by remember { mutableIntStateOf(0) }
    var prevOff by remember { mutableIntStateOf(0) }
    var isScrollingDown by remember { mutableStateOf(false) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val idx = listState.firstVisibleItemIndex
        val off = listState.firstVisibleItemScrollOffset
        isScrollingDown = when {
            idx == 0 && off == 0 -> false
            idx != prevIdx -> idx > prevIdx
            off != prevOff -> off > prevOff
            else -> isScrollingDown
        }
        prevIdx = idx
        prevOff = off
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = 128.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "tools_section") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.tools))
                    val toolRows = listOf(
                        Triple(Icons.Rounded.Security, DesignSystemR.string.security, PreviewRoute.SECURITY),
                        Triple(Icons.Rounded.Apps, DesignSystemR.string.apps, PreviewRoute.APPS),
                        Triple(Icons.Rounded.DeleteOutline, DesignSystemR.string.trash, PreviewRoute.TRASH),
                        Triple(Icons.Rounded.Lan, DesignSystemR.string.network, PreviewRoute.NETWORK),
                        Triple(Icons.Rounded.Cloud, DesignSystemR.string.cloud_storage, PreviewRoute.CLOUD),
                        Triple(Icons.Rounded.Storage, DesignSystemR.string.storage_analyzer, PreviewRoute.STORAGE_ANALYZER),
                    )
                    FolioSegmentedColumn {
                        toolRows.forEachIndexed { idx, (icon, labelRes, route) ->
                            FolioSegmentedCard(
                                index = idx,
                                count = toolRows.size,
                                onClick = { onNavigate(route) },
                            ) {
                                PreviewToolRow(icon = icon, title = stringResource(labelRes))
                            }
                        }
                    }
                }
            }

            item(key = "appearance_section") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.appearance))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(stringResource(DesignSystemR.string.theme), style = MaterialTheme.typography.titleMedium)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val modes = listOf(
                                        ThemeMode.SYSTEM to DesignSystemR.string.theme_system,
                                        ThemeMode.LIGHT to DesignSystemR.string.theme_light,
                                        ThemeMode.DARK to DesignSystemR.string.theme_dark,
                                        ThemeMode.OLED to DesignSystemR.string.theme_oled,
                                    )
                                    modes.forEach { (mode, labelRes) ->
                                        FilterChip(
                                            selected = appearance.themeMode == mode,
                                            onClick = { onAppearanceChange(appearance.copy(themeMode = mode)) },
                                            label = { Text(stringResource(labelRes)) },
                                        )
                                    }
                                }
                            }
                        }

                        FolioSegmentedCard(index = 1, count = 4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Palette, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(DesignSystemR.string.dynamic_color_title), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        stringResource(DesignSystemR.string.dynamic_color_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = appearance.useDynamicColor,
                                    onCheckedChange = { onAppearanceChange(appearance.copy(useDynamicColor = it)) },
                                )
                            }
                        }

                        FolioSegmentedCard(index = 2, count = 4) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(stringResource(DesignSystemR.string.accent_palette), style = MaterialTheme.typography.titleMedium)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val palettes = listOf(
                                        FolioAccentPalette.SAGE to DesignSystemR.string.palette_sage,
                                        FolioAccentPalette.OCEAN to DesignSystemR.string.palette_ocean,
                                        FolioAccentPalette.VIOLET to DesignSystemR.string.palette_violet,
                                        FolioAccentPalette.TERRACOTTA to DesignSystemR.string.palette_terracotta,
                                    )
                                    palettes.forEach { (pal, labelRes) ->
                                        val isSelected = appearance.accentPalette == pal
                                        Surface(
                                            onClick = { onAppearanceChange(appearance.copy(accentPalette = pal)) },
                                            shape = CircleShape,
                                            color = paletteSwatchColor(pal),
                                            border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                                            modifier = Modifier.size(44.dp),
                                        ) {
                                            if (isSelected) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = stringResource(labelRes),
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FolioSegmentedCard(index = 3, count = 4) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(stringResource(DesignSystemR.string.corner_style), style = MaterialTheme.typography.titleMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val styles = listOf(
                                        "SQUARE" to DesignSystemR.string.corner_square,
                                        "ROUNDED" to DesignSystemR.string.corner_rounded,
                                        "EXTRA_ROUNDED" to DesignSystemR.string.corner_extra,
                                    )
                                    styles.forEach { (key, labelRes) ->
                                        FilterChip(
                                            selected = appearance.cornerStyle == key,
                                            onClick = { onAppearanceChange(appearance.copy(cornerStyle = key)) },
                                            label = { Text(stringResource(labelRes)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FolioTopScrim(
            height = statusBarTop + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        FolioBottomScrim(
            height = 112.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.tools_and_settings),
            onNavigateBack = onNavigateBack,
            onFabClick = null,
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PreviewToolRow(
    icon: ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
