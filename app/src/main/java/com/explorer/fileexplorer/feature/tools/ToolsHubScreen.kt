package com.explorer.fileexplorer.feature.tools

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.designsystem.rememberScrollVelocityConnection
import com.explorer.fileexplorer.core.designsystem.topEdgeFeatherFade
import kotlinx.coroutines.launch

/**
 * Clean M3 Expressive Hub replacing the legacy hamburger navigation drawer.
 * Houses every advanced tool (Transfers, Analyzer, Trash, Apps, Cloud, Share Server,
 * Tags, Security & Vault, Shizuku, Root Modules, Settings) in Pixel-style segmented stacks.
 */
@Composable
fun ToolsHubScreen(
    onOpenTransfers: () -> Unit,
    onOpenAnalyzer: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenCloud: () -> Unit,
    onOpenServer: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenShizuku: () -> Unit,
    onOpenRootModules: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val dockController = LocalFolioDockController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val scrollToTopThresholdPx = with(density) { 160.dp.toPx() }
    val showScrollToTop by remember(listState, scrollToTopThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollToTopThresholdPx
        }
    }

    val scrollConnection = rememberScrollVelocityConnection(
        velocityThresholdDpPerSec = 600f,
        onFastScrollDown = { dockController.onFastScrollDown() },
        onFastScrollUp = { dockController.onFastScrollUp() },
    )

    LaunchedEffect(showScrollToTop) {
        dockController.showScrollToTop = showScrollToTop
        dockController.onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } }
        dockController.canCreatePrimaryAction = true
        dockController.onPrimaryAction = onOpenSearch
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 15) {
            dockController.resetScrollMorph()
        }
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings() }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = statusBarTop + 56.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollConnection),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = headerHeight + 12.dp,
                bottom = 140.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .topEdgeFeatherFade(opaqueStartDp = headerHeight - 8.dp, fadeHeightDp = 28.dp),
        ) {
            item(key = "storage_activity") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel("Activity & Storage")
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4, onClick = onOpenTransfers) {
                            ToolsRow(Icons.Rounded.SwapHoriz, stringResource(DesignSystemR.string.transfer_queue), "Active copy, move, and network jobs")
                        }
                        FolioSegmentedCard(index = 1, count = 4, onClick = onOpenAnalyzer) {
                            ToolsRow(Icons.Rounded.PieChart, "Storage Analyzer", "Inspect folder sizes and large files")
                        }
                        FolioSegmentedCard(index = 2, count = 4, onClick = onOpenTrash) {
                            ToolsRow(Icons.Rounded.DeleteOutline, "Trash", "Restore or empty deleted files")
                        }
                        FolioSegmentedCard(index = 3, count = 4, onClick = onOpenApps) {
                            ToolsRow(Icons.Rounded.Apps, "Apps", "Installed packages and APK export")
                        }
                    }
                }
            }

            item(key = "remote_organize") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel("Remote & Organization")
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 3, onClick = onOpenCloud) {
                            ToolsRow(Icons.Rounded.Cloud, "Cloud", "Connected cloud drives")
                        }
                        FolioSegmentedCard(index = 1, count = 3, onClick = onOpenServer) {
                            ToolsRow(Icons.Rounded.WifiTethering, "Share Server", "Local HTTP / FTP file sharing")
                        }
                        FolioSegmentedCard(index = 2, count = 3, onClick = onOpenTags) {
                            ToolsRow(Icons.Rounded.Label, "Tags", "Manage custom file labels")
                        }
                    }
                }
            }

            item(key = "security_system") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel("Security & System")
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4, onClick = onOpenSecurity) {
                            ToolsRow(Icons.Rounded.Security, stringResource(DesignSystemR.string.security), "Encrypted vault, biometric lock, integrity watch")
                        }
                        FolioSegmentedCard(index = 1, count = 4, onClick = onOpenShizuku) {
                            ToolsRow(Icons.Rounded.AdminPanelSettings, "Shizuku", "Elevated Android/data & OBB access")
                        }
                        FolioSegmentedCard(index = 2, count = 4, onClick = onOpenRootModules) {
                            ToolsRow(Icons.Rounded.Extension, "Root Modules", "Magisk / KernelSU module browser")
                        }
                        FolioSegmentedCard(index = 3, count = 4, onClick = onOpenSettings) {
                            ToolsRow(Icons.Rounded.Settings, "Settings", "Theme, gestures, columns, and preferences")
                        }
                    }
                }
            }
        }

        // Transparent Floating Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tools",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ToolsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
