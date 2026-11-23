package com.explorer.fileexplorer.feature.browser

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.SavedSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.mediarouter.app.MediaRouteButton
import com.explorer.fileexplorer.core.data.ArchiveFormat
import com.explorer.fileexplorer.core.data.SecureDeleteCapability
import com.explorer.fileexplorer.core.database.RecentLocationEntity
import com.explorer.fileexplorer.core.database.SavedSearchEntity
import com.explorer.fileexplorer.core.database.TagEntity
import com.explorer.fileexplorer.core.designsystem.AccentOrange
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSelectionDockActions
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.designsystem.rememberScrollVelocityConnection
import com.explorer.fileexplorer.core.designsystem.topEdgeFeatherFade
import com.explorer.fileexplorer.core.model.FileColumn
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.model.RepositoryFeature
import com.explorer.fileexplorer.core.model.RepositoryOperation
import com.explorer.fileexplorer.core.model.SortDirection
import com.explorer.fileexplorer.core.model.SortField
import com.explorer.fileexplorer.core.model.SortOrder
import com.explorer.fileexplorer.core.model.ViewMode
import com.explorer.fileexplorer.core.storage.RootState
import com.explorer.fileexplorer.core.ui.BreadcrumbBar
import com.explorer.fileexplorer.core.ui.FileGridItem
import com.explorer.fileexplorer.core.ui.FileListItem
import com.explorer.fileexplorer.feature.security.SecurityEntryPoint
import com.google.android.gms.cast.framework.CastButtonFactory
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = hiltViewModel(),
    initialPath: String? = null,
    onOpenSearch: () -> Unit = {},
    onOpenSavedSearch: (SavedSearchEntity) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenNetwork: () -> Unit = {},
    onOpenShizuku: () -> Unit = {},
    onOpenServer: () -> Unit = {},
    onOpenCloud: () -> Unit = {},
    onOpenCollections: () -> Unit = {},
    onOpenTags: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenApps: () -> Unit = {},
    onOpenRootModules: () -> Unit = {},
    onOpenTrash: () -> Unit = {},
    onOpenAnalyzer: () -> Unit = {},
    onOpenTransfers: () -> Unit = {},
    onOpenEditor: (String) -> Unit = {},
    onOpenHexEditor: (String) -> Unit = {},
    onOpenPreview: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dockController = LocalFolioDockController.current

    val securityEntryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        )
    }

    var showLocationsSheet by remember { mutableStateOf(false) }
    var showSelectionMoreSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val usbTreePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let(viewModel::addUsbTree)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                viewModel.refreshUsbStorage()
            }
        }
        val filter = IntentFilter().apply {
            addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
            addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(initialPath) {
        initialPath?.takeIf { it.isNotBlank() }?.let(viewModel::navigateTo)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BrowserEvent.Toast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is BrowserEvent.OpenFile -> {
                    val extension = event.item.extension.lowercase()
                    if (event.item.path.startsWith("/") && extension in setOf("pdf", "docx", "xlsx", "epub")) {
                        onOpenPreview(event.item.path)
                    } else if (event.item.path.startsWith("/") &&
                        (event.item.isText || extension in setOf("kt", "java", "py", "js", "ts", "json", "xml", "html", "css", "sh", "ps1", "md", "txt", "yml", "yaml", "toml", "cfg", "ini", "conf", "log", "csv", "sql", "c", "cpp", "h", "rs", "go", "rb", "php", "swift", "gradle", "properties"))
                    ) {
                        onOpenEditor(event.item.path)
                    } else {
                        openFile(context, event.item)
                    }
                }
                is BrowserEvent.ShareFiles -> shareFiles(context, event.items)
                is BrowserEvent.SendNearbyFiles -> sendNearbyFiles(context, event.items)
                is BrowserEvent.RequestDecrypt -> {
                    val activity = context as? FragmentActivity
                    if (activity == null) {
                        Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.biometrics_unavailable), Toast.LENGTH_SHORT).show()
                    } else {
                        securityEntryPoint.biometricHelper().showBiometricPrompt(
                            activity = activity,
                            title = context.applicationContext.getString(DesignSystemR.string.decrypt_files),
                            subtitle = context.applicationContext.getString(DesignSystemR.string.authenticate_to_decrypt),
                            onSuccess = { viewModel.decryptFiles(event.paths) },
                            onFailure = { reason ->
                                Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.decryption_cancelled, reason), Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
                is BrowserEvent.RequestVaultAdd -> {
                    val activity = context as? FragmentActivity
                    if (activity == null) {
                        Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.biometrics_unavailable), Toast.LENGTH_SHORT).show()
                    } else {
                        securityEntryPoint.biometricHelper().showBiometricPrompt(
                            activity = activity,
                            title = context.applicationContext.getString(DesignSystemR.string.add_to_vault),
                            subtitle = context.applicationContext.getString(DesignSystemR.string.authenticate_to_protect_selected),
                            onSuccess = { viewModel.addToVault(event.paths) },
                            onFailure = { reason ->
                                Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.vault_authentication_cancelled, reason), Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }

    // Bind scroll velocity, scroll-to-top, clipboard paste, and selection morph to the hoisted dock
    val scrollToTopThresholdPx = with(density) { 160.dp.toPx() }
    val showScrollToTop by remember(state.viewMode, listState, gridState, scrollToTopThresholdPx) {
        derivedStateOf {
            if (state.viewMode == ViewMode.GRID) {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > scrollToTopThresholdPx
            } else {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollToTopThresholdPx
            }
        }
    }

    val scrollVelocityConnection = rememberScrollVelocityConnection(
        velocityThresholdDpPerSec = 600f,
        onFastScrollDown = { dockController.onFastScrollDown() },
        onFastScrollUp = { dockController.onFastScrollUp() },
    )

    LaunchedEffect(
        state.selectionMode,
        state.selectedCount,
        state.insideArchive,
        state.canPaste,
        state.capabilityMatrix,
        showScrollToTop,
        state.viewMode,
    ) {
        dockController.selectionMode = state.selectionMode
        dockController.selectionActions = FolioSelectionDockActions(
            selectedCount = state.selectedCount,
            insideArchive = state.insideArchive,
            canCopy = state.capabilityMatrix.isActionEnabled(RepositoryOperation.COPY),
            canMove = state.capabilityMatrix.isActionEnabled(RepositoryOperation.MOVE),
            canTrash = state.capabilityMatrix.isActionEnabled(RepositoryFeature.TRASH),
            canDelete = state.capabilityMatrix.isActionEnabled(RepositoryOperation.DELETE),
            canExtract = state.capabilityMatrix.isActionEnabled(RepositoryFeature.ARCHIVE_EXTRACTION),
            onCut = viewModel::cutSelected,
            onCopy = viewModel::copySelected,
            onDelete = viewModel::deleteSelected,
            onPermanentDelete = viewModel::permanentlyDeleteSelected,
            onExtract = { viewModel.extractArchive() },
            onMore = { showSelectionMoreSheet = true },
        )
        dockController.canPaste = state.canPaste
        dockController.onPaste = viewModel::paste
        dockController.canCreatePrimaryAction = !state.insideArchive &&
            state.capabilityMatrix.isActionEnabled(RepositoryOperation.CREATE_DIRECTORY)
        dockController.onPrimaryAction = viewModel::showNewFolderDialog
        dockController.showScrollToTop = showScrollToTop
        dockController.onScrollToTop = {
            scope.launch {
                if (state.viewMode == ViewMode.GRID) {
                    gridState.animateScrollToItem(0)
                } else {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (state.viewMode != ViewMode.GRID && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 15) {
            dockController.resetScrollMorph()
        }
    }

    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        if (state.viewMode == ViewMode.GRID && gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset < 15) {
            dockController.resetScrollMorph()
        }
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings() }
    }

    BackHandler(enabled = state.selectionMode || state.insideArchive || state.currentPath != "/") {
        when {
            state.selectionMode -> viewModel.clearSelection()
            else -> viewModel.navigateUp()
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topHeaderHeight = statusBarTop + 60.dp
    val activeLocationTitle = remember(state.currentPath, state.volumes) {
        state.volumes.firstOrNull { state.currentPath.startsWith(it.path) }?.name
            ?: state.currentPath.trimEnd('/').substringAfterLast('/').ifBlank { "Storage" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollVelocityConnection),
    ) {
        // Full-bleed browser content with smooth top-edge alpha feather fade
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .topEdgeFeatherFade(opaqueStartDp = topHeaderHeight - 10.dp, fadeHeightDp = 30.dp),
        ) {
            if (!state.dualPaneEnabled && LargeScreenLayoutPolicy.useThreePane(maxWidth.value.toInt())) {
                Box(modifier = Modifier.fillMaxSize().padding(top = topHeaderHeight, bottom = 96.dp)) {
                    LargeScreenBrowserContent(
                        state = state,
                        onNavigate = viewModel::navigateTo,
                        onOpenItem = viewModel::onItemClick,
                        onLongClick = viewModel::onItemLongClick,
                        onNavigateUp = viewModel::navigateUp,
                        onRefresh = viewModel::refresh,
                        onSelectAll = viewModel::selectAll,
                        onClearSelection = viewModel::clearSelection,
                        onDeleteSelected = viewModel::deleteSelected,
                        onShowProperties = viewModel::showProperties,
                        onSwipeLeft = { item -> viewModel.applySwipe(item, state.swipeLeftAction) },
                        onSwipeRight = { item -> viewModel.applySwipe(item, state.swipeRightAction) },
                        onSelectTab = { viewModel.selectTab(BrowserPane.PRIMARY, it) },
                        onCloseTab = { viewModel.closeTab(BrowserPane.PRIMARY, it) },
                        onAddTab = { viewModel.openTab(BrowserPane.PRIMARY) },
                        onReorderTabs = { from, to -> viewModel.reorderTabs(BrowserPane.PRIMARY, from, to) },
                    )
                }
            } else if (state.dualPaneEnabled) {
                Box(modifier = Modifier.fillMaxSize().padding(top = topHeaderHeight, bottom = 96.dp)) {
                    DualPaneContent(
                        state = state,
                        onPrimaryNavigate = viewModel::navigateTo,
                        onPrimaryItemClick = viewModel::onItemClick,
                        onPrimaryItemLongClick = viewModel::onItemLongClick,
                        onPrimarySwipeLeft = { item -> viewModel.applySwipe(item, state.swipeLeftAction) },
                        onPrimarySwipeRight = { item -> viewModel.applySwipe(item, state.swipeRightAction) },
                        onSecondaryNavigate = viewModel::navigateSecondaryTo,
                        onSecondaryItemClick = viewModel::onSecondaryItemClick,
                        onSecondaryItemLongClick = viewModel::onSecondaryItemLongClick,
                        onSecondarySwipeLeft = { item -> viewModel.applySwipe(item, state.swipeLeftAction) },
                        onSecondarySwipeRight = { item -> viewModel.applySwipe(item, state.swipeRightAction) },
                        onPrimaryNavigateUp = viewModel::navigateUp,
                        onSecondaryNavigateUp = viewModel::navigateSecondaryUp,
                        onPrimaryRefresh = viewModel::refresh,
                        onSecondaryRefresh = viewModel::refreshSecondary,
                        onSelectTab = viewModel::selectTab,
                        onCloseTab = viewModel::closeTab,
                        onAddTab = viewModel::openTab,
                        onReorderTabs = viewModel::reorderTabs,
                        onRequestDrop = viewModel::requestDrop,
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(
                                top = topHeaderHeight + 8.dp,
                                bottom = 144.dp,
                                start = 12.dp,
                                end = 12.dp,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                BrowserHeaderAccessories(
                                    state = state,
                                    onNavigate = viewModel::navigateTo,
                                    onSelectTab = { viewModel.selectTab(BrowserPane.PRIMARY, it) },
                                    onCloseTab = { viewModel.closeTab(BrowserPane.PRIMARY, it) },
                                    onAddTab = { viewModel.openTab(BrowserPane.PRIMARY) },
                                    onReorderTabs = { from, to -> viewModel.reorderTabs(BrowserPane.PRIMARY, from, to) },
                                )
                            }
                            if (state.files.isEmpty() && !state.isLoading) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    EmptyState(state.error)
                                }
                            } else {
                                gridItems(items = state.files, key = { it.path }) { item ->
                                    FileGridItem(
                                        item = item,
                                        isSelected = item.path in state.selectedItems,
                                        selectionMode = state.selectionMode,
                                        visibleColumns = state.visibleColumns,
                                        onClick = { viewModel.onItemClick(item) },
                                        onLongClick = { viewModel.onItemLongClick(item) },
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = topHeaderHeight + 8.dp,
                                bottom = 144.dp,
                                start = 12.dp,
                                end = 12.dp,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item(key = "browser_accessories") {
                                BrowserHeaderAccessories(
                                    state = state,
                                    onNavigate = viewModel::navigateTo,
                                    onSelectTab = { viewModel.selectTab(BrowserPane.PRIMARY, it) },
                                    onCloseTab = { viewModel.closeTab(BrowserPane.PRIMARY, it) },
                                    onAddTab = { viewModel.openTab(BrowserPane.PRIMARY) },
                                    onReorderTabs = { from, to -> viewModel.reorderTabs(BrowserPane.PRIMARY, from, to) },
                                )
                            }

                            if (state.files.isEmpty() && !state.isLoading) {
                                item(key = "empty_state") { EmptyState(state.error) }
                            } else {
                                items(items = state.files, key = { it.path }) { item ->
                                    FileListItem(
                                        item = item,
                                        isSelected = item.path in state.selectedItems,
                                        selectionMode = state.selectionMode,
                                        compact = state.compactDensity,
                                        visibleColumns = state.visibleColumns,
                                        showDirectorySizes = state.showDirectorySizes,
                                        directorySize = state.directorySizes[item.path],
                                        directorySizeUnavailable = item.path in state.directorySizesUnavailable,
                                        onClick = { viewModel.onItemClick(item) },
                                        onLongClick = { viewModel.onItemLongClick(item) },
                                        onSwipeLeft = { viewModel.applySwipe(item, state.swipeLeftAction) },
                                        onSwipeRight = { viewModel.applySwipe(item, state.swipeRightAction) },
                                    )
                                    if (item != state.files.lastOrNull()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Minimal Transparent Floating Top Bar (No Hamburger Menu)
        FloatingBrowserTopBar(
            selectionMode = state.selectionMode,
            selectedCount = state.selectedCount,
            locationTitle = activeLocationTitle,
            rootActive = state.rootEnabled && state.isRootPath,
            viewMode = state.viewMode,
            visibleColumns = state.visibleColumns,
            showHidden = state.showHidden,
            dualPaneEnabled = state.dualPaneEnabled,
            insideArchive = state.insideArchive,
            canExtract = state.capabilityMatrix.isActionEnabled(RepositoryFeature.ARCHIVE_EXTRACTION),
            canCreateFolder = state.capabilityMatrix.isActionEnabled(RepositoryOperation.CREATE_DIRECTORY),
            showSortMenu = showSortMenu,
            sortOrder = state.sortOrder,
            onDismissSortMenu = { showSortMenu = false },
            onSelectSortField = { field ->
                val newDir = if (state.sortOrder.field == field) {
                    if (state.sortOrder.direction == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                } else {
                    SortDirection.ASCENDING
                }
                viewModel.setSortOrder(state.sortOrder.copy(field = field, direction = newDir))
                showSortMenu = false
            },
            onOpenLocations = { showLocationsSheet = true },
            onClearSelection = viewModel::clearSelection,
            onSelectAll = viewModel::selectAll,
            onSearchClick = onOpenSearch,
            onOpenSortMenu = { showSortMenu = true },
            onViewToggle = viewModel::toggleViewMode,
            onToggleHidden = viewModel::toggleHidden,
            onToggleColumn = viewModel::toggleColumn,
            onToggleDualPane = viewModel::toggleDualPane,
            onNewFolder = viewModel::showNewFolderDialog,
            onExtractAll = { viewModel.extractArchive() },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    // M3 Expressive Locations & Storage Sheet (Replaces Hamburger Drawer Storage/Bookmarks/Recent/Root)
    if (showLocationsSheet) {
        LocationsBottomSheet(
            state = state,
            onDismiss = { showLocationsSheet = false },
            onNavigate = { path ->
                showLocationsSheet = false
                viewModel.navigateTo(path)
            },
            onOpenUsbPicker = {
                showLocationsSheet = false
                usbTreePicker.launch(null)
            },
            onOpenSavedSearch = { saved ->
                showLocationsSheet = false
                onOpenSavedSearch(saved)
            },
            onToggleRoot = viewModel::toggleRootMode,
        )
    }

    // M3 Expressive Selection Overflow Sheet (Opened via Morphed Bottom Bar -> "More")
    if (showSelectionMoreSheet && state.selectionMode) {
        SelectionMoreBottomSheet(
            selectedCount = state.selectedCount,
            insideArchive = state.insideArchive,
            canDelete = state.capabilityMatrix.isActionEnabled(RepositoryOperation.DELETE),
            canCompress = state.capabilityMatrix.isActionEnabled(RepositoryFeature.ARCHIVE_CREATION),
            canWatchIntegrity = state.capabilityMatrix.isActionEnabled(RepositoryFeature.CHECKSUMS),
            onDismiss = { showSelectionMoreSheet = false },
            onShare = { showSelectionMoreSheet = false; viewModel.shareSelected() },
            onSendNearby = { showSelectionMoreSheet = false; viewModel.sendNearbySelected() },
            onCast = { showSelectionMoreSheet = false; viewModel.castSelected() },
            onRename = {
                showSelectionMoreSheet = false
                state.files.firstOrNull { it.path in state.selectedItems }?.let { viewModel.showRename(it) }
            },
            onBatchRename = { showSelectionMoreSheet = false; viewModel.showBatchRenameDialog() },
            onCompress = { showSelectionMoreSheet = false; viewModel.showCompressDialog() },
            onTag = { showSelectionMoreSheet = false; viewModel.showTagDialog() },
            onEncrypt = { showSelectionMoreSheet = false; viewModel.encryptSelected() },
            onDecrypt = { showSelectionMoreSheet = false; viewModel.requestDecryptSelected() },
            onAddToVault = { showSelectionMoreSheet = false; viewModel.requestAddToVaultSelected() },
            onWatchIntegrity = { showSelectionMoreSheet = false; viewModel.watchSelectedIntegrity() },
            onHexView = {
                showSelectionMoreSheet = false
                state.files.firstOrNull { it.path in state.selectedItems }
                    ?.takeIf { !it.isDirectory }
                    ?.let { onOpenHexEditor(it.path) }
            },
            onProperties = {
                showSelectionMoreSheet = false
                state.files.firstOrNull { it.path in state.selectedItems }?.let { viewModel.showProperties(it) }
            },
            onPermanentDelete = { showSelectionMoreSheet = false; viewModel.permanentlyDeleteSelected() },
        )
    }

    // Existing Functional Dialogs (Preserved 100%)
    if (state.showNewFolderDialog) {
        InputDialog(
            title = stringResource(DesignSystemR.string.new_folder),
            label = stringResource(DesignSystemR.string.folder_name),
            initial = "",
            confirmText = stringResource(DesignSystemR.string.create),
            onConfirm = viewModel::createFolder,
            onDismiss = viewModel::dismissNewFolderDialog,
        )
    }
    state.renameItem?.let { item ->
        InputDialog(
            title = stringResource(DesignSystemR.string.rename),
            label = stringResource(DesignSystemR.string.new_name),
            initial = item.name,
            confirmText = stringResource(DesignSystemR.string.rename),
            onConfirm = { newName -> viewModel.rename(item.path, newName) },
            onDismiss = viewModel::dismissRename,
        )
    }
    if (state.showBatchRenameDialog) {
        BatchRenameDialog(
            items = state.files.filter { it.path in state.selectedItems },
            onConfirm = viewModel::batchRename,
            onDismiss = viewModel::dismissBatchRenameDialog,
        )
    }
    state.propertiesItem?.let { item ->
        PropertiesSheet(
            item = item,
            selinuxContext = state.selinuxContext,
            onDismiss = viewModel::dismissProperties,
            onBookmark = { viewModel.toggleBookmark(item.path, item.name) },
        )
    }
    if (state.showCompressDialog) {
        CompressDialog(
            onConfirm = { name, format, password -> viewModel.compressSelected(name, format, password) },
            onDismiss = viewModel::dismissCompressDialog,
        )
    }
    if (state.showTagDialog) {
        TagAssignmentDialog(
            tags = state.tags,
            selectedTags = state.tagDialogSelectedTags,
            onToggle = viewModel::toggleTagInDialog,
            onCreate = viewModel::createTagForDialog,
            onConfirm = viewModel::applyTags,
            onDismiss = viewModel::dismissTagDialog,
        )
    }
    state.deleteConfirmationItem?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(if (item.isDirectory) DesignSystemR.string.delete_folder_title else DesignSystemR.string.delete_file_title)) },
            text = { Text(stringResource(DesignSystemR.string.move_to_trash, item.name)) },
            confirmButton = { TextButton(onClick = viewModel::confirmDeleteGesture) { Text(stringResource(DesignSystemR.string.delete)) } },
            dismissButton = { TextButton(onClick = viewModel::dismissDeleteConfirmation) { Text(stringResource(DesignSystemR.string.cancel)) } },
        )
    }
    state.permanentDeleteConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPermanentDeleteConfirmation,
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(DesignSystemR.string.delete_permanently)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(DesignSystemR.string.permanent_delete_warning, confirmation.paths.size))
                    if (confirmation.secureDeleteEnabled) {
                        Text(
                            stringResource(
                                if (confirmation.capabilities.secureDelete == SecureDeleteCapability.BEST_EFFORT) {
                                    DesignSystemR.string.permanent_delete_best_effort_warning
                                } else {
                                    DesignSystemR.string.permanent_delete_unsupported_warning
                                },
                            ),
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmPermanentDelete) { Text(stringResource(DesignSystemR.string.delete)) } },
            dismissButton = { TextButton(onClick = viewModel::dismissPermanentDeleteConfirmation) { Text(stringResource(DesignSystemR.string.cancel)) } },
        )
    }
    state.pendingDrop?.let { request ->
        DropConfirmationDialog(
            request = request,
            onCopy = { viewModel.confirmDrop(move = false) },
            onMove = { viewModel.confirmDrop(move = true) },
            onDismiss = viewModel::dismissDrop,
        )
    }
}

@Composable
private fun BrowserHeaderAccessories(
    state: BrowserUiState,
    onNavigate: (String) -> Unit,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onAddTab: () -> Unit,
    onReorderTabs: (Int, Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.rootEnabled && state.isRootPath) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AccentOrange.copy(alpha = 0.14f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Warning, null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(DesignSystemR.string.root_mode_active), style = MaterialTheme.typography.labelMedium, color = AccentOrange)
                    state.selinuxContext?.let { ctx ->
                        Spacer(Modifier.weight(1f))
                        Text(ctx, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }

        if (state.insideArchive) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.FolderZip, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(DesignSystemR.string.browsing_archive_read_only),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        if (state.tabs.size > 1) {
            BrowserTabsBar(
                tabs = state.tabs,
                selectedIndex = state.selectedTabIndex,
                onSelect = onSelectTab,
                onClose = onCloseTab,
                onAdd = onAddTab,
                onReorder = onReorderTabs,
            )
        }

        BreadcrumbBar(
            currentPath = state.currentPath,
            onNavigate = onNavigate,
        )
    }
}

@Composable
private fun FloatingBrowserTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    locationTitle: String,
    rootActive: Boolean,
    viewMode: ViewMode,
    visibleColumns: Set<FileColumn>,
    showHidden: Boolean,
    dualPaneEnabled: Boolean,
    insideArchive: Boolean,
    canExtract: Boolean,
    canCreateFolder: Boolean,
    showSortMenu: Boolean,
    sortOrder: SortOrder,
    onDismissSortMenu: () -> Unit,
    onSelectSortField: (SortField) -> Unit,
    onOpenLocations: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onSearchClick: () -> Unit,
    onOpenSortMenu: () -> Unit,
    onViewToggle: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleColumn: (FileColumn) -> Unit,
    onToggleDualPane: () -> Unit,
    onNewFolder: () -> Unit,
    onExtractAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreExpanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (selectionMode) {
            // Minimal Selection Pill (Actions live in the morphed bottom dock)
            Surface(
                onClick = onClearSelection,
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Close, stringResource(DesignSystemR.string.clear), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(DesignSystemR.string.selected_count, selectedCount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Surface(
                onClick = onSelectAll,
                shape = CircleShape,
                color = colorScheme.surfaceContainerHigh,
                contentColor = colorScheme.onSurface,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.SelectAll, stringResource(DesignSystemR.string.select_all), modifier = Modifier.size(20.dp))
                }
            }
        } else {
            // Location Switcher Pill (Replaces Hamburger Menu)
            Surface(
                onClick = onOpenLocations,
                shape = CircleShape,
                color = if (rootActive) AccentOrange.copy(alpha = 0.2f) else colorScheme.surfaceContainerHigh,
                contentColor = if (rootActive) AccentOrange else colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = locationTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            // Minimal Trailing Circular Pill Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = onSearchClick,
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Search, stringResource(DesignSystemR.string.search), modifier = Modifier.size(20.dp))
                    }
                }

                CastRouteButton()

                Box {
                    Surface(
                        onClick = { moreExpanded = true },
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.MoreVert, stringResource(DesignSystemR.string.more), modifier = Modifier.size(20.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = moreExpanded,
                        onDismissRequest = { moreExpanded = false },
                        shape = RoundedCornerShape(22.dp),
                        containerColor = colorScheme.surfaceContainerHigh,
                    ) {
                        if (insideArchive) {
                            DropdownMenuItem(
                                text = { Text(stringResource(DesignSystemR.string.extract_all)) },
                                enabled = canExtract,
                                onClick = { onExtractAll(); moreExpanded = false },
                                leadingIcon = { Icon(Icons.Rounded.Unarchive, null) },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(DesignSystemR.string.sort)) },
                            onClick = { moreExpanded = false; onOpenSortMenu() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Sort, null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(DesignSystemR.string.toggle_view)) },
                            onClick = { onViewToggle(); moreExpanded = false },
                            leadingIcon = {
                                Icon(if (viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList, null)
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(if (dualPaneEnabled) DesignSystemR.string.close_dual_pane else DesignSystemR.string.open_dual_pane))
                            },
                            onClick = { onToggleDualPane(); moreExpanded = false },
                            leadingIcon = { Icon(Icons.Rounded.ViewColumn, null) },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(if (showHidden) DesignSystemR.string.hide_hidden_files else DesignSystemR.string.show_hidden_files_menu))
                            },
                            onClick = { onToggleHidden(); moreExpanded = false },
                            leadingIcon = { Icon(Icons.Rounded.Visibility, null) },
                        )
                        HorizontalDivider()
                        FileColumn.entries.forEach { column ->
                            DropdownMenuItem(
                                text = { Text(column.label) },
                                onClick = { onToggleColumn(column) },
                                leadingIcon = { Icon(Icons.Rounded.ViewColumn, null) },
                                trailingIcon = { Checkbox(checked = column in visibleColumns, onCheckedChange = null) },
                            )
                        }
                        if (!insideArchive) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(DesignSystemR.string.new_folder)) },
                                enabled = canCreateFolder,
                                onClick = { onNewFolder(); moreExpanded = false },
                                leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, null) },
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = onDismissSortMenu,
                        shape = RoundedCornerShape(22.dp),
                        containerColor = colorScheme.surfaceContainerHigh,
                    ) {
                        SortField.entries.forEach { field ->
                            DropdownMenuItem(
                                text = { Text(field.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { onSelectSortField(field) },
                                trailingIcon = {
                                    if (sortOrder.field == field) {
                                        Icon(
                                            if (sortOrder.direction == SortDirection.ASCENDING) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsBottomSheet(
    state: BrowserUiState,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenUsbPicker: () -> Unit,
    onOpenSavedSearch: (SavedSearchEntity) -> Unit,
    onToggleRoot: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 6.dp),
            )

            // Storage Volumes + USB OTG
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FolioSectionLabel("Storage")
                val totalStorageRows = state.volumes.size + state.usbRoots.size + 1
                var rowIdx = 0
                FolioSegmentedColumn {
                    state.volumes.forEach { vol ->
                        FolioSegmentedCard(index = rowIdx++, count = totalStorageRows, onClick = { onNavigate(vol.path) }) {
                            SheetActionRow(Icons.Rounded.Storage, vol.name, vol.path)
                        }
                    }
                    state.usbRoots.forEach { usb ->
                        FolioSegmentedCard(index = rowIdx++, count = totalStorageRows, onClick = { onNavigate(usb.path) }) {
                            SheetActionRow(Icons.Rounded.Usb, usb.name, usb.path)
                        }
                    }
                    FolioSegmentedCard(index = rowIdx, count = totalStorageRows, onClick = onOpenUsbPicker) {
                        SheetActionRow(Icons.Rounded.Usb, "Mount USB storage", "Select external drive folder")
                    }
                }
            }

            // Bookmarks
            if (state.bookmarks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel("Bookmarks")
                    FolioSegmentedColumn {
                        state.bookmarks.forEachIndexed { idx, bm ->
                            FolioSegmentedCard(index = idx, count = state.bookmarks.size, onClick = { onNavigate(bm.path) }) {
                                SheetActionRow(Icons.Rounded.Bookmark, bm.name, bm.path)
                            }
                        }
                    }
                }
            }

            // Recent Locations
            if (state.recentLocations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel("Recent")
                    val recents = state.recentLocations.take(5)
                    FolioSegmentedColumn {
                        recents.forEachIndexed { idx, loc ->
                            FolioSegmentedCard(index = idx, count = recents.size, onClick = { onNavigate(loc.path) }) {
                                SheetActionRow(Icons.Rounded.History, loc.name, loc.path)
                            }
                        }
                    }
                }
            }

            // Saved Searches
            if (state.savedSearches.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel("Saved Searches")
                    FolioSegmentedColumn {
                        state.savedSearches.forEachIndexed { idx, saved ->
                            FolioSegmentedCard(index = idx, count = state.savedSearches.size, onClick = { onOpenSavedSearch(saved) }) {
                                SheetActionRow(Icons.Rounded.SavedSearch, saved.query, saved.scopePath)
                            }
                        }
                    }
                }
            }

            // Root Mode Toggle (when available)
            if (state.rootState != RootState.DENIED || state.rootEnabled) {
                FolioSegmentedCard(index = 0, count = 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(DesignSystemR.string.root_mode), style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(DesignSystemR.string.browse_system_partitions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = state.rootEnabled, onCheckedChange = { onToggleRoot() })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionMoreBottomSheet(
    selectedCount: Int,
    insideArchive: Boolean,
    canDelete: Boolean,
    canCompress: Boolean,
    canWatchIntegrity: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSendNearby: () -> Unit,
    onCast: () -> Unit,
    onRename: () -> Unit,
    onBatchRename: () -> Unit,
    onCompress: () -> Unit,
    onTag: () -> Unit,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
    onAddToVault: () -> Unit,
    onWatchIntegrity: () -> Unit,
    onHexView: () -> Unit,
    onProperties: () -> Unit,
    onPermanentDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(DesignSystemR.string.selected_count, selectedCount),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 6.dp),
            )

            if (!insideArchive) {
                FolioSegmentedColumn {
                    FolioSegmentedCard(index = 0, count = 3, onClick = onShare) {
                        SheetActionRow(Icons.Rounded.Share, stringResource(DesignSystemR.string.share), null)
                    }
                    FolioSegmentedCard(index = 1, count = 3, onClick = onSendNearby) {
                        SheetActionRow(Icons.Rounded.NearMe, stringResource(DesignSystemR.string.send_quick_share), null)
                    }
                    FolioSegmentedCard(index = 2, count = 3, onClick = onCast) {
                        SheetActionRow(Icons.Rounded.Cast, stringResource(DesignSystemR.string.cast_media), null)
                    }
                }

                FolioSegmentedColumn {
                    val editRows = buildList<Triple<ImageVector, String, () -> Unit>> {
                        if (selectedCount == 1) add(Triple(Icons.Rounded.Edit, stringResource(DesignSystemR.string.rename), onRename))
                        add(Triple(Icons.Rounded.EditNote, stringResource(DesignSystemR.string.batch_rename), onBatchRename))
                        if (canCompress) add(Triple(Icons.Rounded.FolderZip, stringResource(DesignSystemR.string.compress), onCompress))
                        add(Triple(Icons.Rounded.Label, stringResource(DesignSystemR.string.set_tags), onTag))
                        if (selectedCount == 1) add(Triple(Icons.Rounded.Code, stringResource(DesignSystemR.string.hex_view), onHexView))
                    }
                    editRows.forEachIndexed { idx, (icon, label, action) ->
                        FolioSegmentedCard(index = idx, count = editRows.size, onClick = action) {
                            SheetActionRow(icon, label, null)
                        }
                    }
                }

                FolioSegmentedColumn {
                    val secRows = buildList<Triple<ImageVector, String, () -> Unit>> {
                        add(Triple(Icons.Rounded.Lock, stringResource(DesignSystemR.string.encrypt_files), onEncrypt))
                        add(Triple(Icons.Rounded.LockOpen, stringResource(DesignSystemR.string.decrypt_files), onDecrypt))
                        add(Triple(Icons.Rounded.EnhancedEncryption, stringResource(DesignSystemR.string.add_to_vault), onAddToVault))
                        if (canWatchIntegrity) add(Triple(Icons.Rounded.VerifiedUser, stringResource(DesignSystemR.string.watch_for_changes), onWatchIntegrity))
                    }
                    secRows.forEachIndexed { idx, (icon, label, action) ->
                        FolioSegmentedCard(index = idx, count = secRows.size, onClick = action) {
                            SheetActionRow(icon, label, null)
                        }
                    }
                }
            }

            FolioSegmentedColumn {
                val tailCount = if (!insideArchive && canDelete) 2 else 1
                FolioSegmentedCard(index = 0, count = tailCount, onClick = onProperties) {
                    SheetActionRow(Icons.Rounded.Info, stringResource(DesignSystemR.string.properties), null)
                }
                if (!insideArchive && canDelete) {
                    FolioSegmentedCard(index = 1, count = tailCount, onClick = onPermanentDelete) {
                        SheetActionRow(
                            icon = Icons.Rounded.DeleteForever,
                            title = stringResource(DesignSystemR.string.delete_permanently),
                            subtitle = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (tint == MaterialTheme.colorScheme.error) tint else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CastRouteButton() {
    AndroidView(
        factory = { viewContext ->
            runCatching {
                MediaRouteButton(viewContext).also { button ->
                    CastButtonFactory.setUpMediaRouteButton(viewContext.applicationContext, button)
                }
            }.getOrElse { View(viewContext) }
        },
        modifier = Modifier.size(40.dp),
    )
}

@Composable
private fun EmptyState(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.FolderOpen,
                null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = error ?: "Empty folder",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InputDialog(
    title: String,
    label: String,
    initial: String,
    confirmText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}

@Composable
private fun TagAssignmentDialog(
    tags: List<TagEntity>,
    selectedTags: Set<String>,
    onToggle: (String) -> Unit,
    onCreate: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var newTag by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(DesignSystemR.string.set_tags)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(DesignSystemR.string.tag_replace_description), style = MaterialTheme.typography.bodySmall)
                if (tags.isEmpty()) {
                    Text(stringResource(DesignSystemR.string.create_tag_prompt), style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(tags, key = { it.name }) { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggle(tag.name) },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = tag.name in selectedTags, onCheckedChange = { onToggle(tag.name) })
                                Text("#${tag.name}")
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text(stringResource(DesignSystemR.string.new_tag)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            if (newTag.isNotBlank()) {
                                onCreate(newTag)
                                newTag = ""
                            }
                        },
                    ) { Text(stringResource(DesignSystemR.string.add)) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(DesignSystemR.string.apply)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}

@Composable
private fun CompressDialog(
    onConfirm: (String, ArchiveFormat, CharArray?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("archive") }
    var format by remember { mutableStateOf(ArchiveFormat.ZIP) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(DesignSystemR.string.compress)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(DesignSystemR.string.archive_name)) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArchiveFormat.entries.forEach { f ->
                        FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f.extension.uppercase()) })
                    }
                }
                if (format == ArchiveFormat.ZIP) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(DesignSystemR.string.password_optional)) },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name.trim(), format, password.ifEmpty { null }?.toCharArray())
            }) { Text(stringResource(DesignSystemR.string.compress)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertiesSheet(item: FileItem, selinuxContext: String?, onDismiss: () -> Unit, onBookmark: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(item.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            PropRow(stringResource(DesignSystemR.string.file_path), item.path)
            PropRow(stringResource(DesignSystemR.string.file_size), item.displaySize)
            PropRow(stringResource(DesignSystemR.string.file_type), item.mimeType)
            if (item.lastModified > 0) {
                PropRow(stringResource(DesignSystemR.string.modified), java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(item.lastModified)))
            }
            if (item.permissions.isNotEmpty()) PropRow(stringResource(DesignSystemR.string.permissions), item.permissions.joinToString(""))
            item.ownerName?.let { PropRow(stringResource(DesignSystemR.string.owner), it) }
            item.groupName?.let { PropRow(stringResource(DesignSystemR.string.group), it) }
            item.symlinkTarget?.let { PropRow(stringResource(DesignSystemR.string.link_target), it) }
            selinuxContext?.let { PropRow(stringResource(DesignSystemR.string.selinux), it) }
            Spacer(Modifier.height(16.dp))
            if (item.isDirectory) {
                OutlinedButton(onClick = onBookmark, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.BookmarkAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(DesignSystemR.string.toggle_bookmark))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PropRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun openFile(context: Context, item: FileItem) {
    try {
        val uri = item.uri ?: FileProvider.getUriForFile(context, "${context.packageName}.provider", File(item.path))
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, item.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                context.getString(DesignSystemR.string.open_with),
            ),
        )
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(DesignSystemR.string.no_app_found), Toast.LENGTH_SHORT).show()
    }
}

private fun shareFiles(context: Context, items: List<FileItem>) = launchShare(context, items, "Share")
private fun sendNearbyFiles(context: Context, items: List<FileItem>) = launchShare(context, items, "Send with Quick Share")

private fun launchShare(context: Context, items: List<FileItem>, chooserTitle: String) {
    try {
        val shareItems = items.mapNotNull { item ->
            try {
                (item.uri ?: FileProvider.getUriForFile(context, "${context.packageName}.provider", File(item.path)))
                    .let { uri -> item to uri }
            } catch (_: Exception) {
                null
            }
        }
        if (shareItems.isEmpty()) return
        val uris = shareItems.map { it.second }
        val intent = if (shareItems.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uris.first())
                type = shareItems.first().first.mimeType
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                type = "*/*"
            }
        }
        intent.clipData = ClipData.newUri(context.contentResolver, shareItems.first().first.name, uris.first()).apply {
            uris.drop(1).forEach { addItem(ClipData.Item(it)) }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_TITLE, chooserTitle)
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(DesignSystemR.string.share_failed), Toast.LENGTH_SHORT).show()
    }
}
