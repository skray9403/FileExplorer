package com.explorer.fileexplorer.preview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.AccentAmber
import com.explorer.fileexplorer.core.designsystem.AccentBlue
import com.explorer.fileexplorer.core.designsystem.AccentGreen
import com.explorer.fileexplorer.core.designsystem.AccentPurple
import com.explorer.fileexplorer.core.designsystem.FolioBottomScrim
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBarDefaults
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBottomBar
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR

@Composable
private fun rememberIsScrollingDown(listState: LazyListState): Boolean {
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
    return isScrollingDown
}

@Composable
fun PreviewAppsScreen(
    onNavigateBack: () -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()
    val isScrollingDown = rememberIsScrollingDown(listState)
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) PreviewMockData.apps
        else PreviewMockData.apps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
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
                bottom = FolioSubScreenBarDefaults.BottomBarHeight + 24.dp + navBarBottom,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { idx, app ->
                FolioSegmentedCard(index = idx, count = filteredApps.size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGreen.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Android, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${app.version} • ${app.sizeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        FolioTopScrim(height = statusBarTop + 16.dp, modifier = Modifier.align(Alignment.TopCenter))
        FolioBottomScrim(height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp, modifier = Modifier.align(Alignment.BottomCenter))
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.apps_count, filteredApps.size),
            onNavigateBack = onNavigateBack,
            searchQuery = searchQuery,
            searchHint = stringResource(DesignSystemR.string.search_apps_hint),
            onSearchQueryChange = { searchQuery = it },
            onFabClick = null,
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun PreviewTrashScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()
    val isScrollingDown = rememberIsScrollingDown(listState)
    val items = remember { mutableStateListOf(*PreviewMockData.trashItems.toTypedArray()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarTop, bottom = FolioSubScreenBarDefaults.BottomBarHeight + navBarBottom),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(DesignSystemR.string.trash_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = statusBarTop + 16.dp,
                    bottom = FolioSubScreenBarDefaults.BottomBarHeight + 24.dp + navBarBottom,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(items, key = { _, t -> t.id }) { idx, item ->
                    FolioSegmentedCard(index = idx, count = items.size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${item.sizeLabel} • ${item.deletedWhen}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(
                                onClick = {
                                    items.remove(item)
                                    Toast.makeText(context, context.getString(DesignSystemR.string.restore), Toast.LENGTH_SHORT).show()
                                },
                            ) {
                                Icon(Icons.Rounded.RestoreFromTrash, stringResource(DesignSystemR.string.restore))
                            }
                        }
                    }
                }
            }
        }

        FolioTopScrim(height = statusBarTop + 16.dp, modifier = Modifier.align(Alignment.TopCenter))
        FolioBottomScrim(height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp, modifier = Modifier.align(Alignment.BottomCenter))
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.trash),
            onNavigateBack = onNavigateBack,
            fabIcon = if (items.isNotEmpty()) Icons.Rounded.DeleteSweep else null,
            fabContentDescription = stringResource(DesignSystemR.string.empty_trash),
            onFabClick = if (items.isNotEmpty()) { { items.clear() } } else null,
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun PreviewNetworkScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()
    val isScrollingDown = rememberIsScrollingDown(listState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = FolioSubScreenBarDefaults.BottomBarHeight + 24.dp + navBarBottom,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(PreviewMockData.connections, key = { _, c -> c.id }) { idx, conn ->
                FolioSegmentedCard(index = idx, count = PreviewMockData.connections.size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            conn.icon,
                            null,
                            tint = if (conn.connected) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(conn.name, style = MaterialTheme.typography.titleSmall)
                                if (conn.connected) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(color = AccentGreen.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = stringResource(DesignSystemR.string.connected).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                            Text(
                                text = stringResource(DesignSystemR.string.connection_endpoint, conn.protocol.uppercase(), conn.host, conn.port),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        FolioTopScrim(height = statusBarTop + 16.dp, modifier = Modifier.align(Alignment.TopCenter))
        FolioBottomScrim(height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp, modifier = Modifier.align(Alignment.BottomCenter))
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.network),
            onNavigateBack = onNavigateBack,
            fabIcon = Icons.Rounded.Add,
            fabContentDescription = stringResource(DesignSystemR.string.add_connection),
            onFabClick = {
                Toast.makeText(context, context.getString(DesignSystemR.string.add_connection), Toast.LENGTH_SHORT).show()
            },
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun PreviewCloudScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()
    val isScrollingDown = rememberIsScrollingDown(listState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = FolioSubScreenBarDefaults.BottomBarHeight + 24.dp + navBarBottom,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(PreviewMockData.cloudAccounts, key = { _, a -> a.id }) { idx, acct ->
                FolioSegmentedCard(index = idx, count = PreviewMockData.cloudAccounts.size) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(acct.icon, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(acct.serviceName, style = MaterialTheme.typography.titleSmall)
                                Text(acct.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { (acct.usedGb / acct.totalGb).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        )
                        Text(
                            text = "${acct.usedGb} GB / ${acct.totalGb} GB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        FolioTopScrim(height = statusBarTop + 16.dp, modifier = Modifier.align(Alignment.TopCenter))
        FolioBottomScrim(height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp, modifier = Modifier.align(Alignment.BottomCenter))
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.cloud_storage),
            onNavigateBack = onNavigateBack,
            fabIcon = Icons.Rounded.Add,
            fabContentDescription = stringResource(DesignSystemR.string.add_account),
            onFabClick = {
                Toast.makeText(context, context.getString(DesignSystemR.string.add_account), Toast.LENGTH_SHORT).show()
            },
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun PreviewStorageAnalyzerScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()
    val isScrollingDown = rememberIsScrollingDown(listState)
    var selectedTab by remember { mutableIntStateOf(0) }

    val segmentColors = listOf(AccentBlue, AccentAmber, AccentPurple, AccentGreen)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = FolioSubScreenBarDefaults.BottomBarHeight + 24.dp + navBarBottom,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "analyzer_chips") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text(stringResource(DesignSystemR.string.treemap)) },
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text(stringResource(DesignSystemR.string.duplicates_count, 4)) },
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text(stringResource(DesignSystemR.string.largest_files)) },
                    )
                }
            }

            item(key = "analyzer_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    PreviewMockData.storageSegments.forEachIndexed { idx, seg ->
                        Box(
                            modifier = Modifier
                                .weight(seg.fraction)
                                .fillMaxHeight()
                                .background(segmentColors[idx % segmentColors.size]),
                        )
                    }
                }
            }

            item(key = "analyzer_list") {
                FolioSegmentedColumn {
                    PreviewMockData.storageSegments.forEachIndexed { idx, seg ->
                        FolioSegmentedCard(index = idx, count = PreviewMockData.storageSegments.size) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(segmentColors[idx % segmentColors.size]),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = seg.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = seg.sizeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        FolioTopScrim(height = statusBarTop + 16.dp, modifier = Modifier.align(Alignment.TopCenter))
        FolioBottomScrim(height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp, modifier = Modifier.align(Alignment.BottomCenter))
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.storage_analyzer),
            onNavigateBack = onNavigateBack,
            fabIcon = Icons.Rounded.Refresh,
            fabContentDescription = stringResource(DesignSystemR.string.rescan_storage),
            onFabClick = {
                Toast.makeText(context, context.getString(DesignSystemR.string.rescan_storage), Toast.LENGTH_SHORT).show()
            },
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
