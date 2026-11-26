package com.explorer.fileexplorer.feature.browser

import com.explorer.fileexplorer.core.data.UsbPathCodec
import com.explorer.fileexplorer.core.ui.externalClipUris
import com.explorer.fileexplorer.core.ui.folioDragSource
import com.explorer.fileexplorer.core.ui.folioDropTarget
import com.explorer.fileexplorer.core.ui.folioPayloadOrNull
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.SavedSearch
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.explorer.fileexplorer.core.data.ArchiveFormat
import com.explorer.fileexplorer.core.data.SecureDeleteCapability
import com.explorer.fileexplorer.core.database.SavedSearchEntity
import com.explorer.fileexplorer.core.database.TagEntity
import com.explorer.fileexplorer.core.designsystem.AccentOrange
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSelectionDockActions
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.rememberScrollDistanceConnection
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileColumn
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.model.RepositoryFeature
import com.explorer.fileexplorer.core.model.RepositoryOperation
import com.explorer.fileexplorer.core.model.SortDirection
import com.explorer.fileexplorer.core.model.SortField
import com.explorer.fileexplorer.core.model.SortOrder
import com.explorer.fileexplorer.core.model.StorageVolume
import com.explorer.fileexplorer.core.model.ViewMode
import com.explorer.fileexplorer.core.storage.RootState
import com.explorer.fileexplorer.core.ui.FileGridItem
import com.explorer.fileexplorer.core.ui.FileListItem
import com.explorer.fileexplorer.feature.security.SecurityEntryPoint
import com.explorer.fileexplorer.feature.settings.SwipeAction
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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
    val haptic = LocalHapticFeedback.current

    val securityEntryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        )
    }

    var showLocationsSheet by remember { mutableStateOf(false) }
    var topTabDropHovered by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showTabsSheet by remember { mutableStateOf(false) }
    var breadcrumbExpanded by remember { mutableStateOf(false) }
    val backPreviewProgress = remember { Animatable(0f) }
    var backPreviewDirection by remember { mutableFloatStateOf(1f) }
    val scrollDistanceConnection = rememberScrollDistanceConnection(
        thresholdDp = 24f,
        onScrollDown = { dockController.onFastScrollDown() },
        onScrollUp = { dockController.onFastScrollUp() },
    )

    LaunchedEffect(state.currentPath, state.inlineSearchActive, state.selectionMode) {
        breadcrumbExpanded = false
        backPreviewProgress.snapTo(0f)
    }

    val clipboardManager = LocalClipboardManager.current
    val onCopyCurrentPath = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        clipboardManager.setText(AnnotatedString(state.currentPath))
        Toast.makeText(
            context,
            context.applicationContext.getString(DesignSystemR.string.path_copied),
            Toast.LENGTH_SHORT,
        ).show()
    }

    val tabListStates = remember { mutableMapOf<Long, LazyListState>() }
    val tabGridStates = remember { mutableMapOf<Long, LazyGridState>() }
    val activeTabId = state.tabs.getOrNull(state.selectedTabIndex)?.id ?: 1L
    val listState = tabListStates.getOrPut(activeTabId) { LazyListState() }
    val gridState = tabGridStates.getOrPut(activeTabId) { LazyGridState() }

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
                is BrowserEvent.RequestEncrypt -> {
                    val activity = context.findActivity() as? FragmentActivity
                    if (activity != null && securityEntryPoint.securityRepository().canUseBiometrics()) {
                        securityEntryPoint.biometricHelper().showBiometricPrompt(
                            activity = activity,
                            title = context.applicationContext.getString(DesignSystemR.string.encrypt_files),
                            subtitle = context.applicationContext.getString(DesignSystemR.string.authenticate_to_encrypt),
                            onSuccess = { viewModel.encryptFiles(event.paths) },
                            onFailure = { reason ->
                                Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.encryption_cancelled, reason), Toast.LENGTH_SHORT).show()
                            },
                        )
                    } else {
                        viewModel.encryptFiles(event.paths)
                    }
                }
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

    val selectedFileObjects = remember(state.files, state.selectedItems) {
        state.files.filter { it.path in state.selectedItems }
    }
    val singleSelectedFolder = selectedFileObjects.singleOrNull()?.takeIf { it.isDirectory }
    val canHexView = selectedFileObjects.size == 1 && !selectedFileObjects.first().isDirectory
    val canQueueExtract = state.insideArchive || selectedFileObjects.any { it.isArchive }

    LaunchedEffect(
        state.currentPath,
        state.selectedTabIndex,
        state.selectionMode,
        state.selectedCount,
        state.insideArchive,
        state.canPaste,
        state.capabilityMatrix,
        showScrollToTop,
        state.viewMode,
        state.isAtVolumeRoot,
        state.inlineSearchActive,
        state.inlineSearchQuery,
        state.inlineSearchRecursive,
        selectedFileObjects,
    ) {
        dockController.claimOwnership("browser")
        dockController.contextResetKey = "${state.currentPath}|${state.selectedTabIndex}|${state.selectionMode}"
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
            onMore = {},
        )
        dockController.sortPrimaryItems = listOf(
            SortField.NAME,
            SortField.SIZE,
            SortField.DATE,
            SortField.TYPE,
        ).map { field ->
            val isSelected = state.sortOrder.field == field
            val directionIcon = if (isSelected) {
                if (state.sortOrder.direction == SortDirection.ASCENDING) Icons.Rounded.ArrowUpward
                else Icons.Rounded.ArrowDownward
            } else null
            val labelRes = when (field) {
                SortField.NAME -> DesignSystemR.string.sort_name
                SortField.SIZE -> DesignSystemR.string.sort_size
                SortField.DATE -> DesignSystemR.string.sort_date
                SortField.TYPE -> DesignSystemR.string.sort_type
            }
            FolioFabMenuAction(
                icon = when (field) {
                    SortField.NAME -> Icons.AutoMirrored.Rounded.Sort
                    SortField.SIZE -> Icons.Rounded.Storage
                    SortField.DATE -> Icons.Rounded.History
                    SortField.TYPE -> Icons.Rounded.Folder
                },
                labelRes = labelRes,
                selected = isSelected,
                trailingIcon = directionIcon,
                onClick = {
                    val newDir = if (isSelected) {
                        if (state.sortOrder.direction == SortDirection.ASCENDING) SortDirection.DESCENDING
                        else SortDirection.ASCENDING
                    } else {
                        SortDirection.ASCENDING
                    }
                    viewModel.setSortOrder(state.sortOrder.copy(field = field, direction = newDir))
                },
            )
        }
        dockController.sortSecondaryItems = listOf(
            FolioFabMenuAction(
                icon = if (state.viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                labelRes = DesignSystemR.string.toggle_view,
                onClick = viewModel::toggleViewMode,
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.ViewColumn,
                labelRes = if (state.dualPaneEnabled) DesignSystemR.string.close_dual_pane else DesignSystemR.string.open_dual_pane,
                onClick = viewModel::toggleDualPane,
            ),
        )
        dockController.selectionPrimaryItems = buildList {
            if (selectedFileObjects.size == 1) {
                add(
                    FolioFabMenuAction(
                        icon = Icons.Rounded.Edit,
                        labelRes = DesignSystemR.string.rename,
                        onClick = { selectedFileObjects.firstOrNull()?.let(viewModel::showRename) },
                    ),
                )
            } else if (selectedFileObjects.size > 1) {
                add(
                    FolioFabMenuAction(
                        icon = Icons.Rounded.EditNote,
                        labelRes = DesignSystemR.string.batch_rename,
                        onClick = viewModel::showBatchRenameDialog,
                    ),
                )
            }
            if (!state.insideArchive) {
                add(
                    FolioFabMenuAction(
                        icon = Icons.Rounded.Share,
                        labelRes = DesignSystemR.string.share,
                        onClick = viewModel::shareSelected,
                    ),
                )
                if (state.capabilityMatrix.isActionEnabled(RepositoryFeature.ARCHIVE_CREATION)) {
                    add(
                        FolioFabMenuAction(
                            icon = Icons.Rounded.FolderZip,
                            labelRes = DesignSystemR.string.compress,
                            onClick = viewModel::showCompressDialog,
                        ),
                    )
                }
                singleSelectedFolder?.let { folder ->
                    add(
                        FolioFabMenuAction(
                            icon = Icons.Rounded.FolderOpen,
                            labelRes = DesignSystemR.string.open_in_new_tab,
                            onClick = {
                                viewModel.clearSelection()
                                viewModel.openTabAt(BrowserPane.PRIMARY, folder.path)
                            },
                        ),
                    )
                    add(
                        FolioFabMenuAction(
                            icon = Icons.Rounded.ViewColumn,
                            labelRes = DesignSystemR.string.open_in_split_tab,
                            onClick = { viewModel.openInSplitTab(folder.path) },
                        ),
                    )
                }
            }
            if (canQueueExtract) {
                add(
                    FolioFabMenuAction(
                        icon = Icons.Rounded.Unarchive,
                        labelRes = DesignSystemR.string.queue_extract,
                        onClick = viewModel::queueExtractSelected,
                    ),
                )
            }
        }
        dockController.selectionSecondaryItems = if (!state.insideArchive) {
            listOf(
                FolioFabMenuAction(
                    icon = Icons.Rounded.Lock,
                    labelRes = DesignSystemR.string.encrypt_files,
                    onClick = viewModel::requestEncryptSelected,
                ),
                FolioFabMenuAction(
                    icon = Icons.Rounded.LockOpen,
                    labelRes = DesignSystemR.string.decrypt_files,
                    onClick = viewModel::requestDecryptSelected,
                ),
                FolioFabMenuAction(
                    icon = Icons.Rounded.EnhancedEncryption,
                    labelRes = DesignSystemR.string.add_to_vault,
                    onClick = viewModel::requestAddToVaultSelected,
                ),
            )
        } else {
            emptyList()
        }
        dockController.canPaste = state.canPaste
        dockController.onPaste = viewModel::paste
        dockController.onExecuteClipboardTask = if (state.insideArchive) null else viewModel::executeClipboardTask
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
        dockController.isSubfolder = !state.isAtVolumeRoot || state.insideArchive
        dockController.onNavigateUp = viewModel::navigateUp
        dockController.onNavigateToRoot = {
            state.volumes.firstOrNull()?.path?.let(viewModel::navigateTo)
                ?: viewModel.navigateTo(android.os.Environment.getExternalStorageDirectory().absolutePath)
        }
        dockController.inlineSearchActive = state.inlineSearchActive
        dockController.inlineSearchQuery = state.inlineSearchQuery
        dockController.inlineSearchRecursive = state.inlineSearchRecursive
        dockController.onInlineSearchOpen = { viewModel.setInlineSearchActive(true) }
        dockController.onInlineSearchQueryChange = viewModel::setInlineSearchQuery
        dockController.onInlineSearchScopeToggle = viewModel::toggleInlineSearchScope
        dockController.onInlineSearchClose = viewModel::closeInlineSearch
        dockController.onOpenFullSearch = onOpenSearch

        dockController.fabMenuItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.CreateNewFolder,
                labelRes = DesignSystemR.string.new_folder,
                enabled = !state.insideArchive && state.capabilityMatrix.isActionEnabled(RepositoryOperation.CREATE_DIRECTORY),
                onClick = viewModel::showNewFolderDialog,
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.EditNote,
                labelRes = DesignSystemR.string.new_file,
                enabled = !state.insideArchive && state.capabilityMatrix.isActionEnabled(RepositoryOperation.CREATE_FILE),
                onClick = viewModel::showNewFileDialog,
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Usb,
                labelRes = DesignSystemR.string.mount_usb,
                onClick = { usbTreePicker.launch(null) },
            ),
        )
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings("browser") }
    }

    val backEnabled = breadcrumbExpanded || state.inlineSearchActive || state.selectionMode || state.insideArchive || !state.isAtVolumeRoot
    PredictiveBackHandler(enabled = backEnabled) { progress ->
        val isFolderBack = !breadcrumbExpanded && !state.inlineSearchActive && !state.selectionMode
        try {
            progress.collect { event ->
                if (isFolderBack) {
                    backPreviewDirection = if (event.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
                    backPreviewProgress.snapTo(event.progress)
                }
            }
            when {
                breadcrumbExpanded -> breadcrumbExpanded = false
                state.inlineSearchActive -> viewModel.closeInlineSearch()
                state.selectionMode -> viewModel.clearSelection()
                else -> viewModel.navigateUp()
            }
        } catch (_: CancellationException) {
            withContext(NonCancellable) {
                backPreviewProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 650f),
                )
            }
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val hasPinnedTabs = state.tabs.size > 1 && !state.dualPaneEnabled
    val topHeaderHeight = statusBarTop + 60.dp + if (hasPinnedTabs) 44.dp else 0.dp
    val storageFallback = stringResource(DesignSystemR.string.storage)
    val activeFolderTitle = remember(state.currentPath, state.volumes, state.isAtVolumeRoot, storageFallback) {
        if (UsbPathCodec.isUsbPath(state.currentPath)) {
            UsbPathCodec.name(state.currentPath)?.ifBlank { storageFallback } ?: storageFallback
        } else {
            val cleaned = state.currentPath.trimEnd('/')
            if (state.isAtVolumeRoot) {
                state.volumes.firstOrNull { it.path.trimEnd('/') == cleaned }?.name
                    ?: cleaned.substringAfterLast('/').ifBlank { storageFallback }
            } else {
                cleaned.substringAfterLast('/').ifBlank { storageFallback }
            }
        }
    }

    val overscrollThresholdPx = with(density) { 88.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollDistanceConnection)
                .graphicsLayer {
                    val p = backPreviewProgress.value.coerceIn(0f, 1f)
                    val scale = 1f - (0.12f * p)
                    scaleX = scale
                    scaleY = scale
                    translationX = backPreviewDirection * 32.dp.toPx() * p
                    shape = RoundedCornerShape(28.dp * p)
                    clip = p > 0f
                },
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
                Box(modifier = Modifier.fillMaxSize().padding(top = statusBarTop + 56.dp)) {
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
                        bottomContentPadding = 124.dp,
                    )
                }
            } else {
                val pagerState = rememberPagerState(
                    initialPage = state.selectedTabIndex.coerceIn(0, (state.tabs.size - 1).coerceAtLeast(0)),
                    pageCount = { state.tabs.size },
                )

                LaunchedEffect(pagerState.settledPage) {
                    if (pagerState.settledPage != state.selectedTabIndex && pagerState.settledPage in state.tabs.indices) {
                        viewModel.selectTab(BrowserPane.PRIMARY, pagerState.settledPage)
                    }
                }

                LaunchedEffect(state.selectedTabIndex, state.tabs.size) {
                    val target = state.selectedTabIndex.coerceIn(0, (state.tabs.size - 1).coerceAtLeast(0))
                    if (pagerState.currentPage != target) {
                        pagerState.animateScrollToPage(target)
                    }
                }

                // Clean overscroll-past-last-tab NestedScrollConnection (never fights normal pager swipes)
                val overscrollNewTabConnection = remember(
                    state.tabs.size,
                    state.selectionMode,
                    state.inlineSearchActive,
                    overscrollThresholdPx,
                ) {
                    object : NestedScrollConnection {
                        private var unconsumedLeftPx = 0f

                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource,
                        ): Offset {
                            if (source == NestedScrollSource.UserInput &&
                                !state.selectionMode &&
                                !state.inlineSearchActive &&
                                pagerState.currentPage == state.tabs.lastIndex &&
                                available.x < 0f
                            ) {
                                unconsumedLeftPx += -available.x
                            } else if (available.x > 0f) {
                                unconsumedLeftPx = 0f
                            }
                            return Offset.Zero
                        }

                        override suspend fun onPreFling(available: Velocity): Velocity {
                            if (unconsumedLeftPx >= overscrollThresholdPx) {
                                unconsumedLeftPx = 0f
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.openTab(BrowserPane.PRIMARY)
                            } else {
                                unconsumedLeftPx = 0f
                            }
                            return Velocity.Zero
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    key = { index -> state.tabs.getOrNull(index)?.id ?: index.toLong() },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(overscrollNewTabConnection),
                ) { page ->
                    val currentTab = state.tabs.getOrNull(page)
                    val pageTabId = currentTab?.id ?: page.toLong()
                    val pageListState = tabListStates.getOrPut(pageTabId) { LazyListState() }
                    val pageGridState = tabGridStates.getOrPut(pageTabId) { LazyGridState() }
                    val pageFiles = if (page == state.selectedTabIndex) {
                        state.displayedFiles
                    } else {
                        currentTab?.let { state.tabFilesCache[it.id] } ?: emptyList()
                    }

                    PullToRefreshBox(
                        isRefreshing = state.isLoading && page == state.selectedTabIndex,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier
                            .fillMaxSize()
                            .folioDropTarget(
                                acceptsExternal = true,
                                shouldStart = { it.externalClipUris().isNotEmpty() },
                                onDrop = { event ->
                                    val uris = event.externalClipUris()
                                    if (uris.isEmpty()) return@folioDropTarget false
                                    val activity = context.findActivity()
                                    val perm = activity?.requestDragAndDropPermissions(event.toAndroidDragEvent())
                                    viewModel.importDroppedUris(context, uris)
                                    perm?.release()
                                    true
                                },
                            ),
                    ) {
                        AnimatedContent(
                            targetState = if (page == state.selectedTabIndex) state.currentPath else "",
                            transitionSpec = {
                                fadeIn(tween(120)).togetherWith(fadeOut(tween(90)))
                            },
                            label = "folder_navigation_animation",
                            modifier = Modifier.fillMaxSize(),
                        ) { _ ->
                        if (state.viewMode == ViewMode.GRID) {
                            LazyVerticalGrid(
                                state = pageGridState,
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                contentPadding = PaddingValues(
                                    top = topHeaderHeight + 8.dp,
                                    bottom = 144.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                ),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if ((state.rootEnabled && state.isRootPath) || state.insideArchive) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        BrowserStatusBanners(state = state)
                                    }
                                }
                                if (pageFiles.isEmpty() && !state.isLoading) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        EmptyState(state.error)
                                    }
                                } else {
                                    gridItems(items = pageFiles, key = { it.path }) { item ->
                                        FileGridItem(
                                            item = item,
                                            isSelected = item.path in state.selectedItems,
                                            selectionMode = state.selectionMode,
                                            visibleColumns = state.visibleColumns,
                                            onClick = { viewModel.onItemClick(item) },
                                            onLongClick = { viewModel.onItemLongClick(item) },
                                            modifier = Modifier
                                                .folioDragSource(
                                                    enabled = item.path in state.selectedItems,
                                                    context = context,
                                                    itemsProvider = {
                                                        if (item.path in state.selectedItems) selectedFileObjects else listOf(item)
                                                    },
                                                    sourcePath = state.currentPath,
                                                )
                                                .then(
                                                    if (item.isDirectory && !state.insideArchive) {
                                                        Modifier.folioDropTarget(
                                                            acceptsExternal = false,
                                                            onDrop = { event ->
                                                                val payload = event.folioPayloadOrNull() ?: return@folioDropTarget false
                                                                viewModel.requestDrop(
                                                                    payload.items,
                                                                    item.path,
                                                                    BrowserPane.PRIMARY,
                                                                    BrowserPane.PRIMARY,
                                                                )
                                                                true
                                                            },
                                                        )
                                                    } else Modifier,
                                                ),
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                state = pageListState,
                                contentPadding = PaddingValues(
                                    top = topHeaderHeight + 8.dp,
                                    bottom = 144.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                ),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if ((state.rootEnabled && state.isRootPath) || state.insideArchive) {
                                    item(key = "browser_banners") {
                                        BrowserStatusBanners(state = state)
                                    }
                                }

                                if (pageFiles.isEmpty() && !state.isLoading) {
                                    item(key = "empty_state") { EmptyState(state.error) }
                                } else {
                                    items(items = pageFiles, key = { it.path }) { item ->
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
                                            onSwipeLeft = if (state.swipeLeftAction != SwipeAction.NONE) {
                                                { viewModel.applySwipe(item, state.swipeLeftAction) }
                                            } else null,
                                            onSwipeRight = if (state.swipeRightAction != SwipeAction.NONE) {
                                                { viewModel.applySwipe(item, state.swipeRightAction) }
                                            } else null,
                                            modifier = Modifier
                                                .folioDragSource(
                                                    enabled = item.path in state.selectedItems,
                                                    context = context,
                                                    itemsProvider = {
                                                        if (item.path in state.selectedItems) selectedFileObjects else listOf(item)
                                                    },
                                                    sourcePath = state.currentPath,
                                                )
                                                .then(
                                                    if (item.isDirectory && !state.insideArchive) {
                                                        Modifier.folioDropTarget(
                                                            acceptsExternal = false,
                                                            onDrop = { event ->
                                                                val payload = event.folioPayloadOrNull() ?: return@folioDropTarget false
                                                                viewModel.requestDrop(
                                                                    payload.items,
                                                                    item.path,
                                                                    BrowserPane.PRIMARY,
                                                                    BrowserPane.PRIMARY,
                                                                )
                                                                true
                                                            },
                                                        )
                                                    } else Modifier,
                                                ),
                                        )
                                        if (item != pageFiles.lastOrNull()) {
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
            }
        }

        // Lightweight Scrim behind floating top bar
        FolioTopScrim(
            height = topHeaderHeight + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Gallery-Style Floating Top Bar + Pinned Multi-Tab Strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        ) {
            FloatingBrowserTopBar(
                selectionMode = state.selectionMode,
                selectedCount = state.selectedCount,
                folderTitle = activeFolderTitle,
                currentPath = state.currentPath,
                breadcrumbExpanded = breadcrumbExpanded,
                rootActive = state.rootEnabled && state.isRootPath,
                tabCount = state.tabs.size,
                viewMode = state.viewMode,
                visibleColumns = state.visibleColumns,
                showHidden = state.showHidden,
                dualPaneEnabled = state.dualPaneEnabled,
                insideArchive = state.insideArchive,
                canExtract = state.capabilityMatrix.isActionEnabled(RepositoryFeature.ARCHIVE_EXTRACTION),
                canCreateFolder = state.capabilityMatrix.isActionEnabled(RepositoryOperation.CREATE_DIRECTORY),
                dropHovered = topTabDropHovered,
                onToggleBreadcrumbs = { breadcrumbExpanded = !breadcrumbExpanded },
                onNavigateBreadcrumb = { path ->
                    breadcrumbExpanded = false
                    viewModel.navigateTo(path)
                },
                onOpenLocations = {
                    breadcrumbExpanded = false
                    showLocationsSheet = true
                },
                onCopyPath = onCopyCurrentPath,
                onOpenTabs = { showTabsSheet = true },
                onAddTab = { viewModel.openTab(BrowserPane.PRIMARY) },
                onClearSelection = viewModel::clearSelection,
                onSelectAll = viewModel::selectAll,
                onToggleHidden = viewModel::toggleHidden,
                onToggleColumn = viewModel::toggleColumn,
                onExtractAll = { viewModel.extractArchive() },
                onOpenSettings = onOpenSettings,
                modifier = Modifier.folioDropTarget(
                    acceptsExternal = false,
                    shouldStart = { it.folioPayloadOrNull()?.containsOnlyFolders == true },
                    onHoverChanged = { topTabDropHovered = it },
                    onDrop = { event ->
                        val folder = event.folioPayloadOrNull()?.items?.firstOrNull { it.isDirectory }
                            ?: return@folioDropTarget false
                        viewModel.clearSelection()
                        viewModel.openTabAt(BrowserPane.PRIMARY, folder.path)
                        true
                    },
                ),
            )

            AnimatedVisibility(
                visible = hasPinnedTabs && !state.selectionMode,
                enter = expandVertically(tween(180)) + fadeIn(tween(150)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(120)),
            ) {
                BrowserTabsBar(
                    tabs = state.tabs,
                    selectedIndex = state.selectedTabIndex,
                    onSelect = { viewModel.selectTab(BrowserPane.PRIMARY, it) },
                    onClose = { viewModel.closeTab(BrowserPane.PRIMARY, it) },
                    onAdd = { viewModel.openTab(BrowserPane.PRIMARY) },
                    onReorder = { from, to -> viewModel.reorderTabs(BrowserPane.PRIMARY, from, to) },
                    volumes = state.volumes,
                    showAddButton = false,
                    floating = true,
                    onOpenInSplit = viewModel::openInSplitTab,
                    onCopyTabPath = { path ->
                        clipboardManager.setText(AnnotatedString(path))
                        Toast.makeText(
                            context,
                            context.applicationContext.getString(DesignSystemR.string.path_copied),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }

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
            onOpenNetwork = {
                showLocationsSheet = false
                onOpenNetwork()
            },
            onOpenSavedSearch = { saved ->
                showLocationsSheet = false
                onOpenSavedSearch(saved)
            },
            onToggleRoot = viewModel::toggleRootMode,
        )
    }



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
    if (state.showNewFileDialog) {
        InputDialog(
            title = stringResource(DesignSystemR.string.new_file_title),
            label = stringResource(DesignSystemR.string.file_name),
            initial = "",
            confirmText = stringResource(DesignSystemR.string.create),
            onConfirm = viewModel::createFile,
            onDismiss = viewModel::dismissNewFileDialog,
        )
    }
    if (showTabsSheet) {
        TabsBottomSheet(
            tabs = state.tabs,
            selectedTabIndex = state.selectedTabIndex,
            onSelectTab = { index ->
                showTabsSheet = false
                viewModel.selectTab(BrowserPane.PRIMARY, index)
            },
            onCloseTab = { index ->
                viewModel.closeTab(BrowserPane.PRIMARY, index)
            },
            onAddTab = {
                showTabsSheet = false
                viewModel.openTab(BrowserPane.PRIMARY)
            },
            onDismiss = { showTabsSheet = false },
            volumes = state.volumes,
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
private fun BrowserStatusBanners(state: BrowserUiState) {
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
    }
}

private data class TopPathSegment(val name: String, val path: String)

private fun parseTopPathSegments(path: String): List<TopPathSegment> {
    val cleaned = path.trimEnd('/')
    if (cleaned.isEmpty() || cleaned == "/") return emptyList()
    val parts = cleaned.split("/").filter { it.isNotEmpty() }
    val segments = mutableListOf<TopPathSegment>()
    var accumulated = ""
    for (part in parts) {
        accumulated = "$accumulated/$part"
        val displayName = if (UsbPathCodec.isUsbPath(path)) {
            UsbPathCodec.name(accumulated)?.ifBlank { part } ?: part
        } else {
            part
        }
        segments.add(TopPathSegment(displayName, accumulated))
    }
    return segments
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FloatingBrowserTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    folderTitle: String,
    currentPath: String,
    breadcrumbExpanded: Boolean,
    rootActive: Boolean,
    tabCount: Int,
    viewMode: ViewMode,
    visibleColumns: Set<FileColumn>,
    showHidden: Boolean,
    dualPaneEnabled: Boolean,
    insideArchive: Boolean,
    canExtract: Boolean,
    canCreateFolder: Boolean,
    dropHovered: Boolean = false,
    onToggleBreadcrumbs: () -> Unit,
    onNavigateBreadcrumb: (String) -> Unit,
    onOpenLocations: () -> Unit,
    onCopyPath: () -> Unit,
    onOpenTabs: () -> Unit,
    onAddTab: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleColumn: (FileColumn) -> Unit,
    onExtractAll: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreExpanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val floatingBubbleColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        AnimatedContent(
            targetState = when {
                selectionMode -> 0
                breadcrumbExpanded -> 1
                else -> 2
            },
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(110)) },
            label = "gallery_top_bar_mode",
            modifier = Modifier.fillMaxWidth(),
        ) { mode ->
            when (mode) {
                // Selection Mode Header: [✕ N selected] on left, [Select All] on right
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Surface(
                            onClick = onClearSelection,
                            shape = CircleShape,
                            color = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                            shadowElevation = 4.dp,
                            modifier = Modifier.height(44.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
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
                            color = floatingBubbleColor,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.SelectAll, stringResource(DesignSystemR.string.select_all), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Expanded In-Place Directory Pill Strip (Single-tap on center folder pill)
                1 -> {
                    val scrollState = rememberScrollState()
                    val segments = remember(currentPath) { parseTopPathSegments(currentPath) }
                    LaunchedEffect(currentPath) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = floatingBubbleColor,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(scrollState)
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                // Chip 0: Storage / Locations Switcher
                                Surface(
                                    onClick = onOpenLocations,
                                    shape = CircleShape,
                                    color = colorScheme.primaryContainer,
                                    contentColor = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.height(32.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Rounded.Storage, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(DesignSystemR.string.storage),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                        )
                                        Icon(Icons.Rounded.KeyboardArrowDown, null, modifier = Modifier.size(15.dp))
                                    }
                                }

                                segments.forEachIndexed { index, seg ->
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    val isLast = index == segments.lastIndex
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isLast) colorScheme.secondaryContainer else colorScheme.surfaceContainer,
                                        contentColor = if (isLast) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .height(32.dp)
                                            .clip(CircleShape)
                                            .folioDragSource(
                                                enabled = true,
                                                context = LocalContext.current,
                                                itemsProvider = {
                                                    listOf(FileItem(name = seg.name, path = seg.path, isDirectory = true))
                                                },
                                                sourcePath = seg.path,
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    if (!isLast) onNavigateBreadcrumb(seg.path) else onToggleBreadcrumbs()
                                                },
                                                onLongClick = onCopyPath,
                                            ),
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 11.dp),
                                        ) {
                                            Text(
                                                text = seg.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            onClick = onToggleBreadcrumbs,
                            shape = CircleShape,
                            color = floatingBubbleColor,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Close, stringResource(DesignSystemR.string.close), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                // Gallery-Style Balanced 3-Element Header: [Left Tab Bubble]  [Centered Folder Pill]  [Right ⋮ Bubble]
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Left circular bubble: 1 tab -> Add icon; >1 tabs -> Badged tab switcher (long-press adds tab)
                        Surface(
                            shape = CircleShape,
                            color = floatingBubbleColor,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(44.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = { if (tabCount > 1) onOpenTabs() else onAddTab() },
                                    onLongClick = onAddTab,
                                ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (tabCount > 1) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .border(
                                                width = 1.8.dp,
                                                color = colorScheme.onSurface,
                                                shape = RoundedCornerShape(6.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (tabCount > 99) "99" else "$tabCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = stringResource(DesignSystemR.string.add_tab),
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        }

                        // Truly Centered Compact Gallery Pill (Single-tap -> expand directory pills, Long-press -> copy path)
                        if (!dualPaneEnabled) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    dropHovered -> colorScheme.primaryContainer
                                    rootActive -> AccentOrange.copy(alpha = 0.24f)
                                    else -> floatingBubbleColor
                                },
                                contentColor = when {
                                    dropHovered -> colorScheme.onPrimaryContainer
                                    rootActive -> AccentOrange
                                    else -> colorScheme.onSurface
                                },
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .padding(horizontal = 56.dp)
                                    .height(44.dp)
                                    .clip(CircleShape)
                                    .combinedClickable(
                                        onClick = onToggleBreadcrumbs,
                                        onLongClick = onCopyPath,
                                    ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = if (dropHovered) stringResource(DesignSystemR.string.drop_to_open_tab) else folderTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        // Right circular ⋮ bubble
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            Surface(
                                onClick = { moreExpanded = true },
                                shape = CircleShape,
                                color = floatingBubbleColor,
                                contentColor = colorScheme.onSurface,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.MoreVert, stringResource(DesignSystemR.string.more_options), modifier = Modifier.size(20.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = moreExpanded,
                                onDismissRequest = { moreExpanded = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = colorScheme.surfaceContainerHigh,
                                modifier = Modifier.widthIn(max = 224.dp),
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
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(DesignSystemR.string.settings)) },
                                    onClick = { onOpenSettings(); moreExpanded = false },
                                    leadingIcon = { Icon(Icons.Rounded.Settings, null) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsBottomSheet(
    tabs: List<BrowserTab>,
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onAddTab: () -> Unit,
    onDismiss: () -> Unit,
    volumes: List<StorageVolume> = emptyList(),
) {
    val storageFallback = stringResource(DesignSystemR.string.storage)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(DesignSystemR.string.tabs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onAddTab) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(DesignSystemR.string.add_tab),
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(tabs, key = { _, tab -> tab.id }) { index, tab ->
                    val isSelected = index == selectedTabIndex
                    val tabTitle = resolveTabLabel(tab.path, volumes, storageFallback)
                    Surface(
                        onClick = { onSelectTab(index) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = tab.path,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (tabs.size > 1) {
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onCloseTab(index) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(DesignSystemR.string.close_tab, tabTitle),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
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
    onOpenNetwork: () -> Unit,
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
                text = stringResource(DesignSystemR.string.places),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 6.dp),
            )

            // Storage Volumes + USB OTG + Network
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FolioSectionLabel(stringResource(DesignSystemR.string.storage))
                val totalStorageRows = state.volumes.size + state.usbRoots.size + 2
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
                    FolioSegmentedCard(index = rowIdx++, count = totalStorageRows, onClick = onOpenUsbPicker) {
                        SheetActionRow(
                            Icons.Rounded.Usb,
                            stringResource(DesignSystemR.string.mount_usb),
                            stringResource(DesignSystemR.string.choose_usb_folder),
                        )
                    }
                    FolioSegmentedCard(index = rowIdx, count = totalStorageRows, onClick = onOpenNetwork) {
                        SheetActionRow(
                            Icons.Rounded.Lan,
                            stringResource(DesignSystemR.string.network_shares),
                            stringResource(DesignSystemR.string.network),
                        )
                    }
                }
            }

            if (state.bookmarks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.bookmarks))
                    FolioSegmentedColumn {
                        state.bookmarks.forEachIndexed { idx, bm ->
                            FolioSegmentedCard(index = idx, count = state.bookmarks.size, onClick = { onNavigate(bm.path) }) {
                                SheetActionRow(Icons.Rounded.Bookmark, bm.name, bm.path)
                            }
                        }
                    }
                }
            }

            if (state.recentLocations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.recent_locations))
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

            if (state.savedSearches.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.search))
                    FolioSegmentedColumn {
                        state.savedSearches.forEachIndexed { idx, saved ->
                            FolioSegmentedCard(index = idx, count = state.savedSearches.size, onClick = { onOpenSavedSearch(saved) }) {
                                SheetActionRow(Icons.Rounded.SavedSearch, saved.query, saved.scopePath)
                            }
                        }
                    }
                }
            }

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
                text = error ?: stringResource(DesignSystemR.string.empty_directory),
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

private fun shareFiles(context: Context, items: List<FileItem>) =
    launchShare(context, items, context.getString(DesignSystemR.string.share))

private fun sendNearbyFiles(context: Context, items: List<FileItem>) =
    launchShare(context, items, context.getString(DesignSystemR.string.send_quick_share))

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


private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
