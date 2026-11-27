package com.explorer.fileexplorer.preview

import android.widget.Toast
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.explorer.fileexplorer.core.designsystem.AccentAmber
import com.explorer.fileexplorer.core.designsystem.AccentBlue
import com.explorer.fileexplorer.core.designsystem.AccentGreen
import com.explorer.fileexplorer.core.designsystem.AccentOrange
import com.explorer.fileexplorer.core.designsystem.AccentPurple
import com.explorer.fileexplorer.core.designsystem.AccentRed
import com.explorer.fileexplorer.core.designsystem.AccentTeal
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSelectionDockActions
import com.explorer.fileexplorer.core.designsystem.FolioSwitchableActionPopup
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.MonoSpring
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PreviewBrowserScreen(
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dockController = LocalFolioDockController.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val tabs = remember {
        mutableStateListOf(
            PreviewTab(1L, PreviewMockData.ROOT_PATH),
            PreviewTab(2L, "${PreviewMockData.ROOT_PATH}/Documents"),
        )
    }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var nextTabId by remember { mutableIntStateOf(3) }

    val currentPath = tabs.getOrNull(selectedTabIndex)?.path ?: PreviewMockData.ROOT_PATH
    val isAtRoot = currentPath == PreviewMockData.ROOT_PATH

    var sortField by remember { mutableStateOf(PreviewSortField.NAME) }
    var sortDir by remember { mutableStateOf(PreviewSortDirection.ASCENDING) }
    var viewMode by remember { mutableStateOf(PreviewViewMode.LIST) }
    var showHidden by remember { mutableStateOf(false) }
    var visibleColumns by remember { mutableStateOf(setOf(PreviewFileColumn.SIZE, PreviewFileColumn.DATE)) }

    var selectedPaths by remember { mutableStateOf(emptySet<String>()) }
    val selectionMode = selectedPaths.isNotEmpty()

    var inlineSearchActive by remember { mutableStateOf(false) }
    var inlineSearchQuery by remember { mutableStateOf("") }
    var inlineSearchRecursive by remember { mutableStateOf(false) }

    var breadcrumbExpanded by remember { mutableStateOf(false) }
    var showLocationsSheet by remember { mutableStateOf(false) }

    val backPreviewAnim = remember { Animatable(0f) }
    var backSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    var lastPathDepth by remember { mutableIntStateOf(currentPath.count { it == '/' }) }
    val goingUp = currentPath.count { it == '/' } < lastPathDepth

    LaunchedEffect(currentPath, inlineSearchActive, selectionMode) {
        breadcrumbExpanded = false
        backPreviewAnim.snapTo(0f)
        lastPathDepth = currentPath.count { it == '/' }
    }

    val navigateTo: (String) -> Unit = { newPath ->
        val idx = selectedTabIndex.coerceIn(0, tabs.lastIndex)
        tabs[idx] = tabs[idx].copy(path = newPath)
        selectedPaths = emptySet()
    }

    val navigateUp: () -> Unit = {
        if (!isAtRoot) {
            val parent = currentPath.substringBeforeLast('/', PreviewMockData.ROOT_PATH)
                .ifBlank { PreviewMockData.ROOT_PATH }
            navigateTo(if (parent.length < PreviewMockData.ROOT_PATH.length) PreviewMockData.ROOT_PATH else parent)
        }
    }

    val openNewTab: (String) -> Unit = { path ->
        val newId = nextTabId++.toLong()
        tabs.add(PreviewTab(newId, path))
        selectedTabIndex = tabs.lastIndex
        selectedPaths = emptySet()
    }

    val tabListStates = remember { mutableMapOf<Long, LazyListState>() }
    val tabGridStates = remember { mutableMapOf<Long, LazyGridState>() }
    val activeTabId = tabs.getOrNull(selectedTabIndex)?.id ?: 1L
    val listState = tabListStates.getOrPut(activeTabId) { LazyListState() }
    val gridState = tabGridStates.getOrPut(activeTabId) { LazyGridState() }

    LaunchedEffect(listState, gridState, viewMode) {
        var previousOffset = 0
        snapshotFlow {
            if (viewMode == PreviewViewMode.GRID) {
                gridState.firstVisibleItemIndex * 10_000 + gridState.firstVisibleItemScrollOffset
            } else {
                listState.firstVisibleItemIndex * 10_000 + listState.firstVisibleItemScrollOffset
            }
        }.collect { offset ->
            when {
                offset > previousOffset + 2_000 -> dockController.onFastScrollDown()
                offset < previousOffset - 2_000 || offset == 0 -> dockController.onFastScrollUp()
            }
            previousOffset = offset
        }
    }

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
        pageCount = { tabs.size.coerceAtLeast(1) },
    )
    LaunchedEffect(selectedTabIndex, tabs.size) {
        val target = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != selectedTabIndex && pagerState.settledPage in tabs.indices) {
            selectedTabIndex = pagerState.settledPage
            selectedPaths = emptySet()
        }
    }

    val scrollThresholdPx = with(density) { 160.dp.toPx() }
    val showScrollToTop by remember(listState, gridState, viewMode, scrollThresholdPx) {
        derivedStateOf {
            if (viewMode == PreviewViewMode.GRID) {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > scrollThresholdPx
            } else {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThresholdPx
            }
        }
    }

    val toastMsg: (Int) -> Unit = { resId ->
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(
        currentPath,
        isAtRoot,
        selectionMode,
        selectedPaths,
        inlineSearchActive,
        inlineSearchQuery,
        inlineSearchRecursive,
        sortField,
        sortDir,
        viewMode,
        showScrollToTop,
    ) {
        dockController.claimOwnership("browser")
        dockController.contextResetKey = "browser|$currentPath"
        dockController.isSubfolder = !isAtRoot
        dockController.selectionMode = selectionMode
        dockController.inlineSearchActive = inlineSearchActive
        dockController.inlineSearchQuery = inlineSearchQuery
        dockController.inlineSearchRecursive = inlineSearchRecursive
        dockController.showScrollToTop = showScrollToTop
        dockController.onNavigateUp = navigateUp
        dockController.onNavigateToRoot = { navigateTo(PreviewMockData.ROOT_PATH) }
        dockController.onInlineSearchOpen = { inlineSearchActive = true }
        dockController.onInlineSearchClose = {
            inlineSearchActive = false
            inlineSearchQuery = ""
        }
        dockController.onInlineSearchQueryChange = { inlineSearchQuery = it }
        dockController.onInlineSearchScopeToggle = { inlineSearchRecursive = !inlineSearchRecursive }
        dockController.onOpenFullSearch = { toastMsg(DesignSystemR.string.search) }
        dockController.onScrollToTop = {
            scope.launch {
                if (viewMode == PreviewViewMode.GRID) gridState.animateScrollToItem(0)
                else listState.animateScrollToItem(0)
            }
        }

        dockController.selectionActions = FolioSelectionDockActions(
            selectedCount = selectedPaths.size,
            onCut = {
                selectedPaths = emptySet()
                toastMsg(DesignSystemR.string.cut)
            },
            onCopy = {
                selectedPaths = emptySet()
                toastMsg(DesignSystemR.string.copy)
            },
            onDelete = {
                selectedPaths = emptySet()
                toastMsg(DesignSystemR.string.move_to_trash)
            },
            onPermanentDelete = {
                selectedPaths = emptySet()
                toastMsg(DesignSystemR.string.delete)
            },
            onMore = {},
        )

        dockController.selectionPrimaryItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.Edit,
                labelRes = DesignSystemR.string.rename,
                enabled = selectedPaths.size == 1,
                onClick = { toastMsg(DesignSystemR.string.rename) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Share,
                labelRes = DesignSystemR.string.share,
                onClick = { toastMsg(DesignSystemR.string.share) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.FolderZip,
                labelRes = DesignSystemR.string.compress,
                onClick = { toastMsg(DesignSystemR.string.compress) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Info,
                labelRes = DesignSystemR.string.details,
                enabled = selectedPaths.size == 1,
                onClick = { toastMsg(DesignSystemR.string.details) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.SelectAll,
                labelRes = DesignSystemR.string.select_all,
                onClick = {
                    selectedPaths = PreviewMockData.filesFor(currentPath).map { it.path }.toSet()
                },
            ),
        )
        dockController.selectionSecondaryItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.Add,
                labelRes = DesignSystemR.string.open_in_new_tab,
                onClick = {
                    selectedPaths.firstOrNull()?.let { selected ->
                        openNewTab(selected.substringBeforeLast('/', PreviewMockData.ROOT_PATH))
                    }
                },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.ViewColumn,
                labelRes = DesignSystemR.string.open_in_split_tab,
                onClick = {
                    selectedPaths.firstOrNull()?.let { selected ->
                        openNewTab(selected.substringBeforeLast('/', PreviewMockData.ROOT_PATH))
                    }
                    toastMsg(DesignSystemR.string.open_in_split_tab)
                },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Lock,
                labelRes = DesignSystemR.string.encrypt_files,
                onClick = {
                    selectedPaths = emptySet()
                    toastMsg(DesignSystemR.string.encrypt_files)
                },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.LockOpen,
                labelRes = DesignSystemR.string.decrypt_files,
                onClick = {
                    selectedPaths = emptySet()
                    toastMsg(DesignSystemR.string.decrypt_files)
                },
            ),
        )

        dockController.sortPrimaryItems = listOf(
            PreviewSortField.NAME,
            PreviewSortField.SIZE,
            PreviewSortField.DATE,
            PreviewSortField.TYPE,
        ).map { field ->
            val isSelected = sortField == field
            val directionIcon = if (isSelected) {
                if (sortDir == PreviewSortDirection.ASCENDING) Icons.Rounded.ArrowUpward
                else Icons.Rounded.ArrowDownward
            } else {
                null
            }
            val labelRes = when (field) {
                PreviewSortField.NAME -> DesignSystemR.string.sort_name
                PreviewSortField.SIZE -> DesignSystemR.string.sort_size
                PreviewSortField.DATE -> DesignSystemR.string.sort_date
                PreviewSortField.TYPE -> DesignSystemR.string.sort_type
            }
            val leadingIcon = when (field) {
                PreviewSortField.NAME -> Icons.Rounded.SortByAlpha
                PreviewSortField.SIZE -> Icons.Rounded.DataUsage
                PreviewSortField.DATE -> Icons.Rounded.Schedule
                PreviewSortField.TYPE -> Icons.Rounded.Category
            }
            FolioFabMenuAction(
                icon = leadingIcon,
                labelRes = labelRes,
                selected = isSelected,
                trailingIcon = directionIcon,
                onClick = {
                    if (isSelected) {
                        sortDir = if (sortDir == PreviewSortDirection.ASCENDING) {
                            PreviewSortDirection.DESCENDING
                        } else {
                            PreviewSortDirection.ASCENDING
                        }
                    } else {
                        sortField = field
                        sortDir = PreviewSortDirection.ASCENDING
                    }
                },
            )
        }
        dockController.sortSecondaryItems = listOf(
            FolioFabMenuAction(
                icon = if (viewMode == PreviewViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                labelRes = DesignSystemR.string.toggle_view,
                onClick = {
                    viewMode = if (viewMode == PreviewViewMode.LIST) PreviewViewMode.GRID else PreviewViewMode.LIST
                },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Visibility,
                labelRes = if (showHidden) DesignSystemR.string.hide_hidden_files else DesignSystemR.string.show_hidden_files_menu,
                selected = showHidden,
                onClick = { showHidden = !showHidden },
            ),
        )
        dockController.fabMenuItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.CreateNewFolder,
                labelRes = DesignSystemR.string.new_folder,
                onClick = { toastMsg(DesignSystemR.string.new_folder) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.NoteAdd,
                labelRes = DesignSystemR.string.new_file,
                onClick = { toastMsg(DesignSystemR.string.new_file) },
            ),
            FolioFabMenuAction(
                icon = Icons.Rounded.Add,
                labelRes = DesignSystemR.string.new_tab,
                onClick = { openNewTab(currentPath) },
            ),
        )
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings("browser") }
    }

    val backEnabled = breadcrumbExpanded || inlineSearchActive || selectionMode || !isAtRoot
    val backSpring = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    PredictiveBackHandler(enabled = backEnabled) { progress ->
        val isFolderBack = !breadcrumbExpanded && !inlineSearchActive && !selectionMode
        try {
            progress.collect { event ->
                if (isFolderBack) {
                    backSwipeEdge = event.swipeEdge
                    backPreviewAnim.snapTo(event.progress)
                    dockController.predictiveBackProgress = event.progress
                    dockController.predictiveBackEdgeSign =
                        if (event.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
                }
            }
            when {
                breadcrumbExpanded -> breadcrumbExpanded = false
                inlineSearchActive -> {
                    inlineSearchActive = false
                    inlineSearchQuery = ""
                }
                selectionMode -> selectedPaths = emptySet()
                else -> navigateUp()
            }
            backPreviewAnim.animateTo(0f, backSpring) {
                dockController.predictiveBackProgress = value
            }
        } catch (_: CancellationException) {
            backPreviewAnim.animateTo(0f, backSpring) {
                dockController.predictiveBackProgress = value
            }
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val hasPinnedTabs = tabs.size > 1
    val topHeaderHeight = statusBarTop + 60.dp + if (hasPinnedTabs) 44.dp else 0.dp
    val storageFallback = stringResource(DesignSystemR.string.storage)
    val activeTitle = remember(currentPath, storageFallback) {
        val cleaned = currentPath.trimEnd('/')
        if (cleaned == PreviewMockData.ROOT_PATH) storageFallback
        else cleaned.substringAfterLast('/').ifBlank { storageFallback }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = backPreviewAnim.value.coerceIn(0f, 1f)
                    val dir = if (backSwipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
                    val scale = 1f - (0.055f * p)
                    scaleX = scale
                    scaleY = scale
                    translationX = dir * 24.dp.toPx() * p
                    shape = RoundedCornerShape(28.dp * p)
                    clip = p > 0f
                },
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = tabs.size > 1 && !selectionMode && !inlineSearchActive,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val tabPath = tabs.getOrNull(page)?.path ?: PreviewMockData.ROOT_PATH
                val pageListState = tabListStates.getOrPut(tabs.getOrNull(page)?.id ?: page.toLong()) { LazyListState() }
                val pageGridState = tabGridStates.getOrPut(tabs.getOrNull(page)?.id ?: page.toLong()) { LazyGridState() }

                val rawFiles = remember(tabPath, inlineSearchActive, inlineSearchQuery, inlineSearchRecursive, showHidden, sortField, sortDir) {
                    val base = if (inlineSearchActive && inlineSearchQuery.isNotBlank() && inlineSearchRecursive) {
                        PreviewMockData.allFilesFlat()
                    } else {
                        PreviewMockData.filesFor(tabPath)
                    }
                    base.filter { item ->
                        (showHidden || !item.isHidden) &&
                            (!inlineSearchActive || inlineSearchQuery.isBlank() || item.name.contains(inlineSearchQuery, ignoreCase = true))
                    }.sortedWith(
                        compareBy<PreviewFileItem> { !it.isDirectory }.then(
                            when (sortField) {
                                PreviewSortField.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                                PreviewSortField.SIZE -> compareBy { it.sizeBytes }
                                PreviewSortField.DATE -> compareBy { it.dateLabel }
                                PreviewSortField.TYPE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.extension }
                            },
                        ).let { cmp -> if (sortDir == PreviewSortDirection.DESCENDING) cmp.reversed() else cmp },
                    )
                }

                AnimatedContent(
                    targetState = if (page == selectedTabIndex) currentPath else tabPath,
                    transitionSpec = {
                        (fadeIn(tween(140)) + scaleIn(initialScale = if (goingUp) 1.03f else 0.97f, animationSpec = tween(160)))
                            .togetherWith(fadeOut(tween(80)))
                    },
                    label = "previewFolderAnim",
                    modifier = Modifier.fillMaxSize(),
                ) { _ ->
                    if (viewMode == PreviewViewMode.GRID) {
                        LazyVerticalGrid(
                            state = pageGridState,
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(
                                top = topHeaderHeight + 8.dp,
                                bottom = 144.dp,
                                start = 12.dp,
                                end = 12.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            gridItems(rawFiles, key = { it.path }) { item ->
                                val isSel = item.path in selectedPaths
                                PreviewFileGridCard(
                                    item = item,
                                    selected = isSel,
                                    onClick = {
                                        if (selectionMode) {
                                            selectedPaths = if (isSel) selectedPaths - item.path else selectedPaths + item.path
                                        } else if (item.isDirectory) {
                                            navigateTo(item.path)
                                        } else {
                                            Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPaths = if (isSel) selectedPaths - item.path else selectedPaths + item.path
                                    },
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = pageListState,
                            contentPadding = PaddingValues(
                                top = topHeaderHeight + 8.dp,
                                bottom = 144.dp,
                                start = 16.dp,
                                end = 16.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(rawFiles, key = { it.path }) { item ->
                                val isSel = item.path in selectedPaths
                                PreviewFileListRow(
                                    item = item,
                                    selected = isSel,
                                    visibleColumns = visibleColumns,
                                    onClick = {
                                        if (selectionMode) {
                                            selectedPaths = if (isSel) selectedPaths - item.path else selectedPaths + item.path
                                        } else if (item.isDirectory) {
                                            navigateTo(item.path)
                                        } else {
                                            Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPaths = if (isSel) selectedPaths - item.path else selectedPaths + item.path
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        FolioTopScrim(
            height = topHeaderHeight + 18.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = statusBarTop + 6.dp),
        ) {
            PreviewTopBar(
                title = activeTitle,
                currentPath = currentPath,
                selectionMode = selectionMode,
                selectedCount = selectedPaths.size,
                breadcrumbExpanded = breadcrumbExpanded,
                showHidden = showHidden,
                visibleColumns = visibleColumns,
                onToggleBreadcrumb = { breadcrumbExpanded = !breadcrumbExpanded },
                onBreadcrumbNavigate = { path ->
                    breadcrumbExpanded = false
                    navigateTo(path)
                },
                onOpenLocations = { showLocationsSheet = true },
                onOpenNewTab = { openNewTab(currentPath) },
                onClearSelection = { selectedPaths = emptySet() },
                onToggleHidden = { showHidden = !showHidden },
                onToggleColumn = { col ->
                    visibleColumns = if (col in visibleColumns) visibleColumns - col else visibleColumns + col
                },
                onOpenSettings = onOpenSettings,
            )

            AnimatedVisibility(
                visible = hasPinnedTabs && !selectionMode,
                enter = expandVertically(tween(180)) + fadeIn(tween(150)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(120)),
            ) {
                PreviewTabsBar(
                    tabs = tabs,
                    selectedIndex = selectedTabIndex,
                    onSelect = { selectedTabIndex = it },
                    onClose = { idx ->
                        if (tabs.size > 1) {
                            tabs.removeAt(idx)
                            selectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)
                        }
                    },
                    onOpenInSplit = { p -> openNewTab(p) },
                    onCopyPath = { p ->
                        clipboardManager.setText(AnnotatedString(p))
                        toastMsg(DesignSystemR.string.path_copied)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }

    if (showLocationsSheet) {
        ModalBottomSheet(onDismissRequest = { showLocationsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(DesignSystemR.string.locations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                val quickPaths = listOf(
                    stringResource(DesignSystemR.string.storage) to PreviewMockData.ROOT_PATH,
                    stringResource(DesignSystemR.string.documents) to "${PreviewMockData.ROOT_PATH}/Documents",
                    stringResource(DesignSystemR.string.downloads) to "${PreviewMockData.ROOT_PATH}/Downloads",
                    "DCIM" to "${PreviewMockData.ROOT_PATH}/DCIM",
                )
                FolioSegmentedColumn {
                    quickPaths.forEachIndexed { idx, (label, path) ->
                        FolioSegmentedCard(
                            index = idx,
                            count = quickPaths.size,
                            onClick = {
                                showLocationsSheet = false
                                navigateTo(path)
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Storage, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(text = label, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PreviewTopBar(
    title: String,
    currentPath: String,
    selectionMode: Boolean,
    selectedCount: Int,
    breadcrumbExpanded: Boolean,
    showHidden: Boolean,
    visibleColumns: Set<PreviewFileColumn>,
    onToggleBreadcrumb: () -> Unit,
    onBreadcrumbNavigate: (String) -> Unit,
    onOpenLocations: () -> Unit,
    onOpenNewTab: () -> Unit,
    onClearSelection: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleColumn: (PreviewFileColumn) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var moreExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selectionMode) {
            Surface(
                shape = CircleShape,
                color = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier.height(44.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(DesignSystemR.string.close),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onClearSelection),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = pluralStringResource(DesignSystemR.plurals.items_count, selectedCount, selectedCount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        } else {
            // Left new-tab pill
            Surface(
                onClick = onOpenNewTab,
                shape = CircleShape,
                color = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                contentColor = colorScheme.onSurface,
                shadowElevation = 4.dp,
                border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(DesignSystemR.string.new_tab),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Center Breadcrumb Pill
            Surface(
                onClick = onToggleBreadcrumb,
                shape = CircleShape,
                color = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                contentColor = colorScheme.onSurface,
                shadowElevation = 4.dp,
                border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .padding(horizontal = 54.dp)
                    .height(44.dp),
            ) {
                if (breadcrumbExpanded) {
                    val relative = currentPath.removePrefix(PreviewMockData.ROOT_PATH).trim('/')
                    val segments = if (relative.isEmpty()) emptyList() else relative.split('/')
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(DesignSystemR.string.storage),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (segments.isEmpty()) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onBreadcrumbNavigate(PreviewMockData.ROOT_PATH) },
                        )
                        var acc = PreviewMockData.ROOT_PATH
                        segments.forEachIndexed { idx, seg ->
                            acc = "$acc/$seg"
                            val segPath = acc
                            Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                            Text(
                                text = seg,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (idx == segments.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (idx == segments.lastIndex) colorScheme.primary else colorScheme.onSurface,
                                modifier = Modifier.clickable { onBreadcrumbNavigate(segPath) },
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 168.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Right More Bubble + Compact Pixel Popup
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                Surface(
                    onClick = { moreExpanded = true },
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                    contentColor = colorScheme.onSurface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(DesignSystemR.string.more_options),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (moreExpanded) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, 128),
                        onDismissRequest = { moreExpanded = false },
                        properties = PopupProperties(focusable = true),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = colorScheme.surfaceContainerHigh,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 6.dp,
                            border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.width(208.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (showHidden) colorScheme.secondaryContainer else Color.Transparent)
                                        .clickable { onToggleHidden(); moreExpanded = false }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Visibility,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (showHidden) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = stringResource(if (showHidden) DesignSystemR.string.hide_hidden_files else DesignSystemR.string.show_hidden_files_menu),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (showHidden) colorScheme.onSecondaryContainer else colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    PreviewFileColumn.entries.forEach { column ->
                                        val active = column in visibleColumns
                                        Surface(
                                            onClick = { onToggleColumn(column) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (active) colorScheme.secondaryContainer else colorScheme.surfaceContainerHighest,
                                            contentColor = if (active) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                                                Text(
                                                    text = column.label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onOpenSettings(); moreExpanded = false }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(Icons.Rounded.Settings, null, modifier = Modifier.size(18.dp), tint = colorScheme.onSurfaceVariant)
                                    Text(
                                        text = stringResource(DesignSystemR.string.tools_and_settings),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewTabsBar(
    tabs: List<PreviewTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onOpenInSplit: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val storageFallback = stringResource(DesignSystemR.string.storage)
    val haptic = LocalHapticFeedback.current
    var menuTabIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            val label = if (tab.path == PreviewMockData.ROOT_PATH) storageFallback
            else tab.path.substringAfterLast('/').ifBlank { storageFallback }
            val selected = index == selectedIndex
            val colorScheme = MaterialTheme.colorScheme
            val bg by animateColorAsState(
                targetValue = if (selected) colorScheme.secondaryContainer else colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                animationSpec = MonoSpring.noBounceColor(),
                label = "previewTabBg_${tab.id}",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                animationSpec = MonoSpring.noBounceColor(),
                label = "previewTabFg_${tab.id}",
            )

            Box {
                Surface(
                    color = bg,
                    contentColor = fg,
                    shape = CircleShape,
                    shadowElevation = if (selected) 3.dp else 1.dp,
                    border = if (!selected) BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)) else null,
                    modifier = Modifier
                        .height(34.dp)
                        .widthIn(min = 88.dp, max = 168.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = { onSelect(index) },
                            onLongClick = {
                                if (tabs.size > 1) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuTabIndex = index
                                }
                            },
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = if (tabs.size > 1) 6.dp else 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Folder, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (tabs.size > 1) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .clickable { onClose(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(DesignSystemR.string.close_tab, label),
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                    }
                }

                if (menuTabIndex == index) {
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(0, 108),
                        onDismissRequest = { menuTabIndex = null },
                        properties = PopupProperties(focusable = true),
                    ) {
                        val actions = listOf(
                            FolioFabMenuAction(
                                icon = Icons.Rounded.ContentCopy,
                                labelRes = DesignSystemR.string.open_in_new_tab,
                                onClick = { onOpenInSplit(tab.path) },
                            ),
                            FolioFabMenuAction(
                                icon = Icons.Rounded.ContentCopy,
                                labelRes = DesignSystemR.string.copy_path,
                                onClick = { onCopyPath(tab.path) },
                            ),
                            FolioFabMenuAction(
                                icon = Icons.Rounded.Close,
                                labelRes = DesignSystemR.string.close_this_tab,
                                onClick = { onClose(index) },
                            ),
                        )
                        FolioSwitchableActionPopup(
                            primaryTitleRes = DesignSystemR.string.tab_actions,
                            secondaryTitleRes = DesignSystemR.string.tab_actions,
                            primaryItems = actions,
                            secondaryItems = emptyList(),
                            onItemClick = { action ->
                                menuTabIndex = null
                                action.onClick()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewFileIcon(
    item: PreviewFileItem,
    size: Dp = 38.dp,
) {
    val (icon, tint) = when {
        item.isDirectory -> Icons.Rounded.Folder to AccentAmber
        item.isEncrypted -> Icons.Rounded.EnhancedEncryption to AccentPurple
        item.extension in setOf("jpg", "png", "webp", "gif") -> Icons.Rounded.Image to AccentGreen
        item.extension in setOf("mp4", "mkv") -> Icons.Rounded.Movie to AccentRed
        item.extension in setOf("mp3", "flac") -> Icons.Rounded.AudioFile to AccentPurple
        item.extension in setOf("zip", "7z", "tar") -> Icons.Rounded.FolderZip to AccentOrange
        item.extension == "apk" -> Icons.Rounded.Android to AccentGreen
        item.extension in setOf("kt", "toml", "md", "json") -> Icons.Rounded.Code to AccentTeal
        item.extension in setOf("pdf", "docx", "xlsx", "epub") -> Icons.Rounded.Description to AccentBlue
        else -> Icons.Rounded.InsertDriveFile to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (item.isDirectory) {
                stringResource(DesignSystemR.string.folder_icon_description)
            } else {
                stringResource(DesignSystemR.string.file_icon_description, item.name)
            },
            tint = tint,
            modifier = Modifier.fillMaxSize(0.54f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewFileListRow(
    item: PreviewFileItem,
    selected: Boolean,
    visibleColumns: Set<PreviewFileColumn>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colorScheme.secondaryContainer.copy(alpha = 0.65f) else colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.45f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreviewFileIcon(item = item, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val metaParts = buildList {
                    if (PreviewFileColumn.SIZE in visibleColumns && item.sizeLabel.isNotBlank()) add(item.sizeLabel)
                    if (PreviewFileColumn.DATE in visibleColumns && item.dateLabel.isNotBlank()) add(item.dateLabel)
                    if (PreviewFileColumn.TYPE in visibleColumns && item.extension.isNotBlank()) add(item.extension.uppercase())
                }
                if (metaParts.isNotEmpty()) {
                    Text(
                        text = metaParts.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(DesignSystemR.string.selected),
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewFileGridCard(
    item: PreviewFileItem,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colorScheme.secondaryContainer.copy(alpha = 0.65f) else colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.45f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                PreviewFileIcon(item = item, size = 44.dp)
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(DesignSystemR.string.selected),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.sizeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
