package com.explorer.fileexplorer.feature.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileColumn
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.model.StorageVolume
import com.explorer.fileexplorer.core.ui.BreadcrumbBar
import com.explorer.fileexplorer.core.ui.FileListItem
import com.explorer.fileexplorer.core.ui.folioDragSource
import com.explorer.fileexplorer.core.ui.folioDropTarget
import com.explorer.fileexplorer.core.ui.folioPayloadOrNull

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DualPaneContent(
    state: BrowserUiState,
    onPrimaryNavigate: (String) -> Unit,
    onPrimaryItemClick: (FileItem) -> Unit,
    onPrimaryItemLongClick: (FileItem) -> Unit,
    onPrimarySwipeLeft: (FileItem) -> Unit,
    onPrimarySwipeRight: (FileItem) -> Unit,
    onSecondaryNavigate: (String) -> Unit,
    onSecondaryItemClick: (FileItem) -> Unit,
    onSecondaryItemLongClick: (FileItem) -> Unit,
    onSecondarySwipeLeft: (FileItem) -> Unit,
    onSecondarySwipeRight: (FileItem) -> Unit,
    onPrimaryNavigateUp: () -> Unit,
    onSecondaryNavigateUp: () -> Unit,
    onPrimaryRefresh: () -> Unit,
    onSecondaryRefresh: () -> Unit,
    onSelectTab: (BrowserPane, Int) -> Unit,
    onCloseTab: (BrowserPane, Int) -> Unit,
    onAddTab: (BrowserPane) -> Unit,
    onReorderTabs: (BrowserPane, Int, Int) -> Unit,
    onRequestDrop: (List<FileItem>, String, BrowserPane, BrowserPane) -> Unit,
    bottomContentPadding: Dp = 124.dp,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isVerticalSplit = maxWidth < 600.dp
        if (isVerticalSplit) {
            Column(modifier = Modifier.fillMaxSize()) {
                PanePanel(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    path = state.currentPath,
                    files = state.files,
                    isLoading = state.isLoading,
                    error = state.error,
                    selectedItems = state.selectedItems,
                    compactDensity = state.compactDensity,
                    visibleColumns = state.visibleColumns,
                    showDirectorySizes = state.showDirectorySizes,
                    directorySizes = state.directorySizes,
                    directorySizesUnavailable = state.directorySizesUnavailable,
                    tabs = state.tabs,
                    selectedTabIndex = state.selectedTabIndex,
                    volumes = state.volumes,
                    onSelectTab = { stateIndex -> onSelectTab(BrowserPane.PRIMARY, stateIndex) },
                    onCloseTab = { stateIndex -> onCloseTab(BrowserPane.PRIMARY, stateIndex) },
                    onAddTab = { onAddTab(BrowserPane.PRIMARY) },
                    onReorderTabs = { from, to -> onReorderTabs(BrowserPane.PRIMARY, from, to) },
                    pane = BrowserPane.PRIMARY,
                    onNavigate = onPrimaryNavigate,
                    onItemClick = onPrimaryItemClick,
                    onItemLongClick = onPrimaryItemLongClick,
                    onSwipeLeft = onPrimarySwipeLeft,
                    onSwipeRight = onPrimarySwipeRight,
                    onNavigateUp = onPrimaryNavigateUp,
                    onRefresh = onPrimaryRefresh,
                    onRequestDrop = onRequestDrop,
                    bottomPadding = 8.dp,
                )

                HorizontalDivider()

                PanePanel(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    path = state.secondaryPath,
                    files = state.secondaryFiles,
                    isLoading = state.secondaryIsLoading,
                    error = state.secondaryError,
                    selectedItems = state.secondarySelectedItems,
                    compactDensity = state.compactDensity,
                    visibleColumns = state.visibleColumns,
                    showDirectorySizes = state.showDirectorySizes,
                    directorySizes = state.secondaryDirectorySizes,
                    directorySizesUnavailable = state.secondaryDirectorySizesUnavailable,
                    tabs = state.secondaryTabs,
                    selectedTabIndex = state.secondarySelectedTabIndex,
                    volumes = state.volumes,
                    onSelectTab = { stateIndex -> onSelectTab(BrowserPane.SECONDARY, stateIndex) },
                    onCloseTab = { stateIndex -> onCloseTab(BrowserPane.SECONDARY, stateIndex) },
                    onAddTab = { onAddTab(BrowserPane.SECONDARY) },
                    onReorderTabs = { from, to -> onReorderTabs(BrowserPane.SECONDARY, from, to) },
                    pane = BrowserPane.SECONDARY,
                    onNavigate = onSecondaryNavigate,
                    onItemClick = onSecondaryItemClick,
                    onItemLongClick = onSecondaryItemLongClick,
                    onSwipeLeft = onSecondarySwipeLeft,
                    onSwipeRight = onSecondarySwipeRight,
                    onNavigateUp = onSecondaryNavigateUp,
                    onRefresh = onSecondaryRefresh,
                    onRequestDrop = onRequestDrop,
                    bottomPadding = bottomContentPadding,
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                PanePanel(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    path = state.currentPath,
                    files = state.files,
                    isLoading = state.isLoading,
                    error = state.error,
                    selectedItems = state.selectedItems,
                    compactDensity = state.compactDensity,
                    visibleColumns = state.visibleColumns,
                    showDirectorySizes = state.showDirectorySizes,
                    directorySizes = state.directorySizes,
                    directorySizesUnavailable = state.directorySizesUnavailable,
                    tabs = state.tabs,
                    selectedTabIndex = state.selectedTabIndex,
                    volumes = state.volumes,
                    onSelectTab = { stateIndex -> onSelectTab(BrowserPane.PRIMARY, stateIndex) },
                    onCloseTab = { stateIndex -> onCloseTab(BrowserPane.PRIMARY, stateIndex) },
                    onAddTab = { onAddTab(BrowserPane.PRIMARY) },
                    onReorderTabs = { from, to -> onReorderTabs(BrowserPane.PRIMARY, from, to) },
                    pane = BrowserPane.PRIMARY,
                    onNavigate = onPrimaryNavigate,
                    onItemClick = onPrimaryItemClick,
                    onItemLongClick = onPrimaryItemLongClick,
                    onSwipeLeft = onPrimarySwipeLeft,
                    onSwipeRight = onPrimarySwipeRight,
                    onNavigateUp = onPrimaryNavigateUp,
                    onRefresh = onPrimaryRefresh,
                    onRequestDrop = onRequestDrop,
                    bottomPadding = bottomContentPadding,
                )

                VerticalDivider(modifier = Modifier.fillMaxHeight())

                PanePanel(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    path = state.secondaryPath,
                    files = state.secondaryFiles,
                    isLoading = state.secondaryIsLoading,
                    error = state.secondaryError,
                    selectedItems = state.secondarySelectedItems,
                    compactDensity = state.compactDensity,
                    visibleColumns = state.visibleColumns,
                    showDirectorySizes = state.showDirectorySizes,
                    directorySizes = state.secondaryDirectorySizes,
                    directorySizesUnavailable = state.secondaryDirectorySizesUnavailable,
                    tabs = state.secondaryTabs,
                    selectedTabIndex = state.secondarySelectedTabIndex,
                    volumes = state.volumes,
                    onSelectTab = { stateIndex -> onSelectTab(BrowserPane.SECONDARY, stateIndex) },
                    onCloseTab = { stateIndex -> onCloseTab(BrowserPane.SECONDARY, stateIndex) },
                    onAddTab = { onAddTab(BrowserPane.SECONDARY) },
                    onReorderTabs = { from, to -> onReorderTabs(BrowserPane.SECONDARY, from, to) },
                    pane = BrowserPane.SECONDARY,
                    onNavigate = onSecondaryNavigate,
                    onItemClick = onSecondaryItemClick,
                    onItemLongClick = onSecondaryItemLongClick,
                    onSwipeLeft = onSecondarySwipeLeft,
                    onSwipeRight = onSecondarySwipeRight,
                    onNavigateUp = onSecondaryNavigateUp,
                    onRefresh = onSecondaryRefresh,
                    onRequestDrop = onRequestDrop,
                    bottomPadding = bottomContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PanePanel(
    modifier: Modifier,
    path: String,
    files: List<FileItem>,
    isLoading: Boolean,
    error: String?,
    selectedItems: Set<String>,
    compactDensity: Boolean,
    visibleColumns: Set<FileColumn>,
    showDirectorySizes: Boolean,
    directorySizes: Map<String, Long>,
    directorySizesUnavailable: Set<String>,
    tabs: List<BrowserTab>,
    selectedTabIndex: Int,
    volumes: List<StorageVolume>,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onAddTab: () -> Unit,
    onReorderTabs: (Int, Int) -> Unit,
    pane: BrowserPane,
    onNavigate: (String) -> Unit,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
    onSwipeLeft: (FileItem) -> Unit,
    onSwipeRight: (FileItem) -> Unit,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onRequestDrop: (List<FileItem>, String, BrowserPane, BrowserPane) -> Unit,
    bottomPadding: Dp = 124.dp,
) {
    var isDropTarget by remember { mutableStateOf(false) }
    val borderColor = if (isDropTarget) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val storageFallback = stringResource(DesignSystemR.string.storage)
    val folderTitle = remember(path, volumes, storageFallback) {
        resolveTabLabel(path, volumes, storageFallback)
    }

    Column(
        modifier = modifier
            .border(if (isDropTarget) 2.dp else 0.5.dp, borderColor)
            .folioDropTarget(
                acceptsExternal = false,
                onHoverChanged = { isDropTarget = it },
                onDrop = { event ->
                    val payload = event.folioPayloadOrNull() ?: return@folioDropTarget false
                    val sourcePane = if (pane == BrowserPane.PRIMARY) BrowserPane.SECONDARY else BrowserPane.PRIMARY
                    onRequestDrop(payload.items, path, sourcePane, pane)
                    true
                },
            ),
    ) {
        // Compact single header row: folder pill, [↑] up, [↻] refresh, [+] new tab
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = folderTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(onClick = onNavigateUp, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = stringResource(DesignSystemR.string.up_one_folder),
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(DesignSystemR.string.refresh_pane),
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onAddTab, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(DesignSystemR.string.add_tab),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Show tabs bar only when tabs.size > 1
        if (tabs.size > 1) {
            BrowserTabsBar(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onSelect = onSelectTab,
                onClose = onCloseTab,
                onAdd = onAddTab,
                onReorder = onReorderTabs,
                volumes = volumes,
            )
        }

        BreadcrumbBar(currentPath = path, onNavigate = onNavigate)

        if (isDropTarget) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(DesignSystemR.string.release_to_transfer_here), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (files.isEmpty() && !isLoading) {
                PaneEmptyState(error)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = files, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            isSelected = item.path in selectedItems,
                            selectionMode = selectedItems.isNotEmpty(),
                            compact = compactDensity,
                            visibleColumns = visibleColumns,
                            showDirectorySizes = showDirectorySizes,
                            directorySize = directorySizes[item.path],
                            directorySizeUnavailable = item.path in directorySizesUnavailable,
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) },
                            onSwipeLeft = { onSwipeLeft(item) },
                            onSwipeRight = { onSwipeRight(item) },
                            modifier = Modifier.folioDragSource(
                                enabled = true,
                                context = LocalContext.current,
                                itemsProvider = {
                                    if (item.path in selectedItems) files.filter { it.path in selectedItems } else listOf(item)
                                },
                                sourcePath = path,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaneEmptyState(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = error ?: stringResource(DesignSystemR.string.empty_directory),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun DropConfirmationDialog(
    request: PendingDrop,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
        title = { Text(stringResource(DesignSystemR.string.transfer_files)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (request.items.size == 1) request.items.first().name
                    else "${request.items.size} selected items",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Destination: ${request.destinationPath}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onCopy) { Text(stringResource(DesignSystemR.string.copy)) }
                Button(onClick = onMove) { Text(stringResource(DesignSystemR.string.move)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}
