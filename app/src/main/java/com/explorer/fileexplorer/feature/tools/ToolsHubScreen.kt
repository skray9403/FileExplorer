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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import kotlinx.coroutines.launch

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
    onOpenNetwork: () -> Unit = {},
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

    LaunchedEffect(showScrollToTop) {
        dockController.claimOwnership("tools")
        dockController.contextResetKey = "tools"
        dockController.showScrollToTop = showScrollToTop
        dockController.onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } }
        dockController.fabMenuItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.Search,
                labelRes = DesignSystemR.string.search,
                onClick = onOpenSearch,
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Settings,
                labelRes = DesignSystemR.string.settings,
                onClick = onOpenSettings,
            ),
        )
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings("tools") }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = statusBarTop + 60.dp
    val floatingBubbleColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = headerHeight + 8.dp,
                bottom = 144.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "storage_activity") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.activity_storage))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4, onClick = onOpenTransfers) {
                            ToolsRow(Icons.Rounded.SwapHoriz, stringResource(DesignSystemR.string.transfer_queue), stringResource(DesignSystemR.string.storage_activity_desc))
                        }
                        FolioSegmentedCard(index = 1, count = 4, onClick = onOpenAnalyzer) {
                            ToolsRow(Icons.Rounded.PieChart, stringResource(DesignSystemR.string.storage_analyzer), stringResource(DesignSystemR.string.storage_analyzer_desc))
                        }
                        FolioSegmentedCard(index = 2, count = 4, onClick = onOpenTrash) {
                            ToolsRow(Icons.Rounded.DeleteOutline, stringResource(DesignSystemR.string.trash), stringResource(DesignSystemR.string.trash_desc))
                        }
                        FolioSegmentedCard(index = 3, count = 4, onClick = onOpenApps) {
                            ToolsRow(Icons.Rounded.Apps, stringResource(DesignSystemR.string.apps), stringResource(DesignSystemR.string.apps_desc))
                        }
                    }
                }
            }

            item(key = "remote_organize") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.remote_organization))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4, onClick = onOpenCloud) {
                            ToolsRow(Icons.Rounded.Cloud, stringResource(DesignSystemR.string.cloud), stringResource(DesignSystemR.string.cloud_desc))
                        }
                        FolioSegmentedCard(index = 1, count = 4, onClick = onOpenServer) {
                            ToolsRow(Icons.Rounded.WifiTethering, stringResource(DesignSystemR.string.share_server), stringResource(DesignSystemR.string.share_server_desc))
                        }
                        FolioSegmentedCard(index = 2, count = 4, onClick = onOpenTags) {
                            ToolsRow(Icons.Rounded.Label, stringResource(DesignSystemR.string.tags), stringResource(DesignSystemR.string.tags_desc))
                        }
                        FolioSegmentedCard(index = 3, count = 4, onClick = onOpenNetwork) {
                            ToolsRow(Icons.Rounded.Lan, stringResource(DesignSystemR.string.network), stringResource(DesignSystemR.string.network_desc))
                        }
                    }
                }
            }

            item(key = "security_system") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.security_system))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 4, onClick = onOpenSecurity) {
                            ToolsRow(Icons.Rounded.Security, stringResource(DesignSystemR.string.security), stringResource(DesignSystemR.string.security_desc))
                        }
                        FolioSegmentedCard(index = 1, count = 4, onClick = onOpenShizuku) {
                            ToolsRow(Icons.Rounded.AdminPanelSettings, stringResource(DesignSystemR.string.shizuku), stringResource(DesignSystemR.string.shizuku_desc))
                        }
                        FolioSegmentedCard(index = 2, count = 4, onClick = onOpenRootModules) {
                            ToolsRow(Icons.Rounded.Extension, stringResource(DesignSystemR.string.root_modules), stringResource(DesignSystemR.string.root_modules_desc))
                        }
                        FolioSegmentedCard(index = 3, count = 4, onClick = onOpenSettings) {
                            ToolsRow(Icons.Rounded.Settings, stringResource(DesignSystemR.string.settings), stringResource(DesignSystemR.string.settings_desc))
                        }
                    }
                }
            }
        }

        FolioTopScrim(
            height = headerHeight + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Centered gallery-style floating pill header matching Browse & Collections
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = floatingBubbleColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.height(44.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    Text(
                        text = stringResource(DesignSystemR.string.tools),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Surface(
                onClick = onOpenSettings,
                shape = CircleShape,
                color = floatingBubbleColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(DesignSystemR.string.settings),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
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
