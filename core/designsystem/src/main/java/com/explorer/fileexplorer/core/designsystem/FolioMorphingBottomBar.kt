package com.explorer.fileexplorer.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val PillHeight = 58.dp
private val FabSize = 56.dp
private val TaskPillWidth = 72.dp
private val InterPillGap = 10.dp

private data class FolioNavDestination(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private enum class LeftPillKind {
    ROOT_TABS,
    SUBFOLDER_NAV,
    INLINE_SEARCH,
    SELECTION_EDIT,
    SELECTION_ARCHIVE,
}

private enum class RightPillKind {
    FAB_MENU,
    SELECTION_MANAGE,
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolioMorphingBottomBar(
    selectedDestination: String,
    onDestinationSelected: (String) -> Unit,
    controller: FolioDockController,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var selectionPopupOpen by remember { mutableStateOf(false) }
    var sortPopupOpen by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme

    LaunchedEffect(
        selectedDestination,
        controller.contextResetKey,
        controller.isSubfolder,
        controller.selectionMode,
        controller.inlineSearchActive,
    ) {
        fabMenuExpanded = false
        selectionPopupOpen = false
        sortPopupOpen = false
    }

    val destinations = remember {
        listOf(
            FolioNavDestination("browser", R.string.browse, Icons.Rounded.Folder),
            FolioNavDestination("collections", R.string.collections, Icons.Rounded.GridView),
        )
    }

    val enabledFabActions = remember(controller.fabMenuItems) {
        controller.fabMenuItems.filter { it.enabled }
    }
    val enabledSelPrimary = remember(controller.selectionPrimaryItems) {
        controller.selectionPrimaryItems.filter { it.enabled }
    }
    val enabledSelSecondary = remember(controller.selectionSecondaryItems) {
        controller.selectionSecondaryItems.filter { it.enabled }
    }
    val enabledSortPrimary = remember(controller.sortPrimaryItems) {
        controller.sortPrimaryItems.filter { it.enabled }
    }
    val enabledSortSecondary = remember(controller.sortSecondaryItems) {
        controller.sortSecondaryItems.filter { it.enabled }
    }

    val hasFab = enabledFabActions.isNotEmpty()
    val hasSort = enabledSortPrimary.isNotEmpty()
    val hasSearch = controller.onInlineSearchOpen != null
    val hasTasks = controller.taskBadgeCount > 0 && !controller.selectionMode && !controller.inlineSearchActive
    val compactTabs = hasTasks

    val leftKind = when {
        controller.inlineSearchActive -> LeftPillKind.INLINE_SEARCH
        controller.selectionMode -> {
            if (controller.selectionActions.insideArchive) LeftPillKind.SELECTION_ARCHIVE else LeftPillKind.SELECTION_EDIT
        }
        controller.isSubfolder -> LeftPillKind.SUBFOLDER_NAV
        else -> LeftPillKind.ROOT_TABS
    }
    val rightKind = if (controller.selectionMode) RightPillKind.SELECTION_MANAGE else RightPillKind.FAB_MENU

    val closeAllPopups = {
        fabMenuExpanded = false
        selectionPopupOpen = false
        sortPopupOpen = false
    }

    val triggerSearch: () -> Unit = {
        closeAllPopups()
        controller.onInlineSearchOpen?.invoke()
    }

    val spatialDpSpec = if (reducedMotion) tween(70) else motionScheme.defaultSpatialSpec<Dp>()
    val fastSpatialSpec = if (reducedMotion) tween(60) else motionScheme.fastSpatialSpec<Float>()
    val effectsColorSpec = if (reducedMotion) tween(60) else motionScheme.fastEffectsSpec<Color>()
    val effectsFloatSpec = if (reducedMotion) tween(60) else motionScheme.fastEffectsSpec<Float>()

    val rightBg by animateColorAsState(
        targetValue = when {
            controller.selectionMode -> colorScheme.surfaceContainerHigh
            fabMenuExpanded -> colorScheme.primary
            else -> colorScheme.primaryContainer
        },
        animationSpec = effectsColorSpec,
        label = "rightPillBg",
    )
    val rightFg by animateColorAsState(
        targetValue = when {
            controller.selectionMode -> colorScheme.onSurface
            fabMenuExpanded -> colorScheme.onPrimary
            else -> colorScheme.onPrimaryContainer
        },
        animationSpec = effectsColorSpec,
        label = "rightPillFg",
    )
    val fabCornerRadius by animateDpAsState(
        targetValue = if (fabMenuExpanded && !controller.selectionMode) 18.dp else 28.dp,
        animationSpec = spatialDpSpec,
        label = "fabCorner",
    )

    val anyOverlayExpanded = fabMenuExpanded || selectionPopupOpen || sortPopupOpen

    Box(modifier = modifier.fillMaxWidth()) {
        FolioBottomScrim(
            height = 112.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (anyOverlayExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = closeAllPopups,
                    ),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Compact Pixel-style Selection Popup
            AnimatedVisibility(
                visible = selectionPopupOpen && controller.selectionMode && enabledSelPrimary.isNotEmpty(),
                enter = fadeIn(tween(110)) +
                    scaleIn(initialScale = 0.92f, animationSpec = fastSpatialSpec),
                exit = fadeOut(tween(80)) +
                    scaleOut(targetScale = 0.94f, animationSpec = fastSpatialSpec),
                modifier = Modifier.align(Alignment.End),
            ) {
                FolioSwitchableActionPopup(
                    primaryTitleRes = R.string.actions_menu,
                    secondaryTitleRes = R.string.security,
                    primaryItems = enabledSelPrimary,
                    secondaryItems = enabledSelSecondary,
                    onItemClick = { action ->
                        selectionPopupOpen = false
                        action.onClick()
                    },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            // Compact Pixel-style Sort / View Popup
            AnimatedVisibility(
                visible = sortPopupOpen && !controller.selectionMode && !controller.inlineSearchActive && hasSort,
                enter = fadeIn(tween(110)) +
                    scaleIn(initialScale = 0.92f, animationSpec = fastSpatialSpec),
                exit = fadeOut(tween(80)) +
                    scaleOut(targetScale = 0.94f, animationSpec = fastSpatialSpec),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                FolioSwitchableActionPopup(
                    primaryTitleRes = R.string.sort,
                    secondaryTitleRes = R.string.view_options,
                    primaryItems = enabledSortPrimary,
                    secondaryItems = enabledSortSecondary,
                    onItemClick = { action ->
                        sortPopupOpen = false
                        action.onClick()
                    },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            // Contextual "+" FAB Menu Fan-Out
            if (!controller.selectionMode && !controller.inlineSearchActive && hasFab) {
                Box(modifier = Modifier.align(Alignment.End)) {
                    FloatingActionButtonMenu(
                        expanded = fabMenuExpanded,
                        button = {},
                        modifier = Modifier.padding(bottom = if (fabMenuExpanded) 6.dp else 0.dp),
                    ) {
                        enabledFabActions.forEach { action ->
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    action.onClick()
                                },
                                text = { Text(stringResource(action.labelRes)) },
                                icon = { Icon(action.icon, null) },
                            )
                        }
                    }
                }
            }

            // Scroll-to-top bubble
            AnimatedVisibility(
                visible = controller.showScrollToTop &&
                    !controller.selectionMode &&
                    !controller.inlineSearchActive &&
                    !anyOverlayExpanded,
                enter = if (reducedMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn(tween(120)) + scaleIn(tween(120), initialScale = 0.78f) + slideInVertically(tween(120)) { it / 3 }
                },
                exit = if (reducedMotion) {
                    fadeOut(tween(80))
                } else {
                    fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.78f) + slideOutVertically(tween(100)) { it / 3 }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Box(modifier = Modifier.padding(bottom = 10.dp, end = 6.dp)) {
                    Surface(
                        onClick = {
                            controller.onScrollToTop()
                            controller.resetScrollMorph()
                        },
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHighest.copy(alpha = 0.96f),
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 4.dp,
                        border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = stringResource(R.string.scroll_to_top),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // ONE SINGLE PERSISTENT MORPHING ROW — Outer containers NEVER unmount on mode change
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val totalW = maxWidth
                val halfW = ((totalW - InterPillGap) / 2f).coerceAtLeast(120.dp)
                val showMiddle = hasTasks
                val showRight = controller.selectionMode || (!controller.inlineSearchActive && hasFab)

                val middleTargetW = if (showMiddle) TaskPillWidth else 0.dp
                val rightTargetW = when {
                    controller.inlineSearchActive -> 0.dp
                    controller.selectionMode -> halfW
                    hasFab -> FabSize
                    else -> 0.dp
                }
                val gap1Target = if (showMiddle) InterPillGap else 0.dp
                val gap2Target = if (showRight) InterPillGap else 0.dp

                val middleWidth by animateDpAsState(middleTargetW, spatialDpSpec, label = "dockMidW")
                val rightWidth by animateDpAsState(rightTargetW, spatialDpSpec, label = "dockRightW")
                val gap1 by animateDpAsState(gap1Target, spatialDpSpec, label = "dockGap1")
                val gap2 by animateDpAsState(gap2Target, spatialDpSpec, label = "dockGap2")
                val middleAlpha by animateFloatAsState(if (showMiddle) 1f else 0f, effectsFloatSpec, label = "dockMidA")
                val rightAlpha by animateFloatAsState(if (showRight) 1f else 0f, effectsFloatSpec, label = "dockRightA")

                val slot1MinW = when (leftKind) {
                    LeftPillKind.INLINE_SEARCH -> totalW
                    LeftPillKind.SELECTION_EDIT, LeftPillKind.SELECTION_ARCHIVE -> halfW
                    LeftPillKind.ROOT_TABS, LeftPillKind.SUBFOLDER_NAV -> 0.dp
                }
                val slot1MaxW = when (leftKind) {
                    LeftPillKind.INLINE_SEARCH -> totalW
                    LeftPillKind.SELECTION_EDIT, LeftPillKind.SELECTION_ARCHIVE -> halfW
                    LeftPillKind.ROOT_TABS, LeftPillKind.SUBFOLDER_NAV ->
                        (totalW - middleTargetW - rightTargetW - gap1Target - gap2Target).coerceAtLeast(120.dp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // SLOT 1: Persistent Primary Pill (Gallery-clustered centered morph)
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .height(PillHeight)
                            .animateContentSize(
                                animationSpec = if (reducedMotion) tween(70) else motionScheme.defaultSpatialSpec(),
                            )
                            .clip(CircleShape),
                    ) {
                        AnimatedContent(
                            targetState = leftKind,
                            transitionSpec = { folioInnerMorphSpec(reducedMotion, fastSpatialSpec) },
                            label = "dockLeftContent",
                            modifier = Modifier
                                .widthIn(min = slot1MinW, max = slot1MaxW)
                                .height(PillHeight),
                        ) { kind ->
                            when (kind) {
                                LeftPillKind.ROOT_TABS -> {
                                    Row(
                                        modifier = Modifier
                                            .height(PillHeight)
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        destinations.forEach { dest ->
                                            FolioMorphTabChip(
                                                destination = dest,
                                                isSelected = selectedDestination == dest.id,
                                                iconOnly = compactTabs,
                                                reducedMotion = reducedMotion,
                                                onClick = { onDestinationSelected(dest.id) },
                                            )
                                        }
                                        if (hasSort) {
                                            FolioIconPillButton(
                                                icon = Icons.AutoMirrored.Rounded.Sort,
                                                contentDescription = stringResource(R.string.sort),
                                                isActive = sortPopupOpen,
                                                onClick = {
                                                    fabMenuExpanded = false
                                                    sortPopupOpen = !sortPopupOpen
                                                },
                                            )
                                        }
                                        if (hasSearch) {
                                            FolioIconPillButton(
                                                icon = Icons.Rounded.Search,
                                                contentDescription = stringResource(R.string.search),
                                                onClick = triggerSearch,
                                            )
                                        }
                                    }
                                }

                                LeftPillKind.SUBFOLDER_NAV -> {
                                    // Gallery-style tightly packed squircle buttons centered beside (+) FAB
                                    Row(
                                        modifier = Modifier
                                            .height(PillHeight)
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        FolioGallerySquircleButton(
                                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.back),
                                            onClick = controller.onNavigateUp,
                                            onLongClick = controller.onNavigateToRoot,
                                        )
                                        if (hasSearch) {
                                            FolioGallerySquircleButton(
                                                icon = Icons.Rounded.Search,
                                                contentDescription = stringResource(R.string.search),
                                                onClick = triggerSearch,
                                            )
                                        }
                                        FolioGallerySquircleButton(
                                            icon = Icons.Rounded.Home,
                                            contentDescription = stringResource(R.string.home),
                                            onClick = controller.onNavigateToRoot,
                                        )
                                    }
                                }

                                LeftPillKind.INLINE_SEARCH -> {
                                    val focusRequester = remember { FocusRequester() }
                                    LaunchedEffect(Unit) {
                                        runCatching { focusRequester.requestFocus() }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        FolioIconPillButton(
                                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.close_search),
                                            onClick = controller.onInlineSearchClose,
                                        )
                                        BasicTextField(
                                            value = controller.inlineSearchQuery,
                                            onValueChange = controller.onInlineSearchQueryChange,
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
                                            cursorBrush = SolidColor(colorScheme.primary),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(onSearch = { controller.onOpenFullSearch() }),
                                            modifier = Modifier
                                                .weight(1f)
                                                .focusRequester(focusRequester)
                                                .padding(horizontal = 6.dp),
                                            decorationBox = { innerTextField ->
                                                Box(contentAlignment = Alignment.CenterStart) {
                                                    if (controller.inlineSearchQuery.isEmpty()) {
                                                        Text(
                                                            text = stringResource(R.string.search_files_hint),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            },
                                        )
                                        if (controller.inlineSearchQuery.isNotEmpty()) {
                                            FolioIconPillButton(
                                                icon = Icons.Rounded.Close,
                                                contentDescription = stringResource(R.string.clear_search),
                                                iconSize = 18.dp,
                                                onClick = { controller.onInlineSearchQueryChange("") },
                                            )
                                        }
                                        if (selectedDestination == "browser") {
                                            Spacer(Modifier.width(4.dp))
                                            val scopeBg by animateColorAsState(
                                                targetValue = if (controller.inlineSearchRecursive) {
                                                    colorScheme.primaryContainer
                                                } else {
                                                    colorScheme.surfaceContainerHighest
                                                },
                                                animationSpec = effectsColorSpec,
                                                label = "searchScopeBg",
                                            )
                                            val scopeFg by animateColorAsState(
                                                targetValue = if (controller.inlineSearchRecursive) {
                                                    colorScheme.onPrimaryContainer
                                                } else {
                                                    colorScheme.onSurfaceVariant
                                                },
                                                animationSpec = effectsColorSpec,
                                                label = "searchScopeFg",
                                            )
                                            Surface(
                                                onClick = controller.onInlineSearchScopeToggle,
                                                shape = CircleShape,
                                                color = scopeBg,
                                                contentColor = scopeFg,
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .padding(end = 2.dp),
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.padding(horizontal = 12.dp),
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            if (controller.inlineSearchRecursive) R.string.all_scope else R.string.folder_scope,
                                                        ),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                LeftPillKind.SELECTION_EDIT -> {
                                    val actions = controller.selectionActions
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .alpha(if (actions.canMove) 1f else 0.38f)
                                                .clickable(enabled = actions.canMove, onClick = actions.onCut),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(Icons.Rounded.ContentCut, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.cut),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(20.dp)
                                                .background(colorScheme.outlineVariant.copy(alpha = 0.55f)),
                                        )
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .alpha(if (actions.canCopy) 1f else 0.38f)
                                                .clickable(enabled = actions.canCopy, onClick = actions.onCopy),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.copy),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }

                                LeftPillKind.SELECTION_ARCHIVE -> {
                                    val actions = controller.selectionActions
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(if (actions.canExtract) 1f else 0.38f)
                                            .clickable(enabled = actions.canExtract, onClick = actions.onExtract),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Rounded.Unarchive, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.extract_all),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (gap1 > 0.dp) Spacer(Modifier.width(gap1))

                    // SLOT 2: Middle Task Stack & Live Transfer Progress Pill
                    if (middleWidth > 1.dp) {
                        val hasConflict = controller.taskHasConflict
                        val progressFillColor = if (hasConflict) {
                            colorScheme.errorContainer.copy(alpha = 0.65f)
                        } else {
                            colorScheme.primaryContainer.copy(alpha = 0.78f)
                        }

                        Surface(
                            onClick = {
                                closeAllPopups()
                                controller.onTaskPillClick?.invoke()
                            },
                            shape = CircleShape,
                            color = colorScheme.surfaceContainerHigh,
                            contentColor = if (hasConflict) colorScheme.error else colorScheme.primary,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .width(middleWidth)
                                .height(PillHeight)
                                .graphicsLayer { alpha = middleAlpha }
                                .clip(CircleShape),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val p = controller.taskProgress
                                        if (p != null && p > 0f) {
                                            drawRect(
                                                color = progressFillColor,
                                                size = Size(
                                                    width = size.width * p.coerceIn(0f, 1f),
                                                    height = size.height,
                                                ),
                                            )
                                        }
                                    }
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = stringResource(R.string.tasks_and_progress),
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                val p = controller.taskProgress
                                val label = if (p != null && p > 0f) {
                                    "${(p * 100f).roundToInt()}%"
                                } else {
                                    "${controller.taskBadgeCount}"
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }

                    if (gap2 > 0.dp) Spacer(Modifier.width(gap2))

                    // SLOT 3: Persistent Right Pill (Circular (+) FAB <-> Selection [Delete | More])
                    if (rightWidth > 1.dp) {
                        val rightShape = RoundedCornerShape(fabCornerRadius)
                        Surface(
                            shape = rightShape,
                            color = rightBg,
                            contentColor = rightFg,
                            shadowElevation = 6.dp,
                            border = if (controller.selectionMode) {
                                BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f))
                            } else {
                                null
                            },
                            modifier = Modifier
                                .width(rightWidth)
                                .height(if (controller.selectionMode) PillHeight else FabSize)
                                .graphicsLayer { alpha = rightAlpha }
                                .clip(rightShape),
                        ) {
                            AnimatedContent(
                                targetState = rightKind,
                                transitionSpec = { folioInnerMorphSpec(reducedMotion, fastSpatialSpec) },
                                label = "dockRightContent",
                                modifier = Modifier.fillMaxSize(),
                            ) { kind ->
                                when (kind) {
                                    RightPillKind.FAB_MENU -> {
                                        val fabInteraction = remember { MutableInteractionSource() }
                                        val pressed by fabInteraction.collectIsPressedAsState()
                                        val pressScale by animateFloatAsState(
                                            targetValue = if (pressed) 0.90f else 1f,
                                            animationSpec = fastSpatialSpec,
                                            label = "fabPressScale",
                                        )
                                        val rotation by animateFloatAsState(
                                            targetValue = if (fabMenuExpanded) 45f else 0f,
                                            animationSpec = fastSpatialSpec,
                                            label = "fabRotation",
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = pressScale, scaleY = pressScale)
                                                .clickable(
                                                    interactionSource = fabInteraction,
                                                    indication = ripple(bounded = true),
                                                    onClick = {
                                                        sortPopupOpen = false
                                                        if (enabledFabActions.size == 1) {
                                                            enabledFabActions.first().onClick()
                                                        } else {
                                                            fabMenuExpanded = !fabMenuExpanded
                                                        }
                                                    },
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = stringResource(
                                                    if (fabMenuExpanded) R.string.close else R.string.more_options,
                                                ),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .graphicsLayer { rotationZ = rotation },
                                            )
                                        }
                                    }

                                    RightPillKind.SELECTION_MANAGE -> {
                                        val actions = controller.selectionActions
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (!actions.insideArchive) {
                                                val deleteEnabled = actions.canDelete || actions.canTrash
                                                Row(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .alpha(if (deleteEnabled) 1f else 0.38f)
                                                        .combinedClickable(
                                                            enabled = deleteEnabled,
                                                            onClick = { if (actions.canTrash) actions.onDelete() else actions.onPermanentDelete() },
                                                            onLongClick = actions.onPermanentDelete,
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.DeleteOutline,
                                                        contentDescription = stringResource(R.string.delete),
                                                        tint = colorScheme.error,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = stringResource(R.string.delete),
                                                        color = colorScheme.error,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Clip,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .width(1.dp)
                                                        .height(20.dp)
                                                        .background(colorScheme.outlineVariant.copy(alpha = 0.55f)),
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable(
                                                        enabled = enabledSelPrimary.isNotEmpty(),
                                                        onClick = { selectionPopupOpen = !selectionPopupOpen },
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                Icon(
                                                    imageVector = if (selectionPopupOpen) Icons.Rounded.Close else Icons.Rounded.MoreHoriz,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(if (selectionPopupOpen) R.string.close else R.string.more),
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Clip,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold,
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
    }
}

/**
 * Compact, snappy Pixel Launcher-style popup:
 * Slim header pill (with optional ↕ page toggle) + single clean unified rounded card.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolioSwitchableActionPopup(
    @StringRes primaryTitleRes: Int,
    @StringRes secondaryTitleRes: Int,
    primaryItems: List<FolioFabMenuAction>,
    secondaryItems: List<FolioFabMenuAction>,
    onItemClick: (FolioFabMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSecondary by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val fastSpatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val hasSecondary = secondaryItems.isNotEmpty()
    val activeItems = if (showSecondary && hasSecondary) secondaryItems else primaryItems

    val chevronRotation by animateFloatAsState(
        targetValue = if (showSecondary) 180f else 0f,
        animationSpec = fastSpatial,
        label = "popupSwitchRot",
    )

    Column(
        modifier = modifier.width(196.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Slim Header Pill
        Surface(
            shape = CircleShape,
            color = colorScheme.surfaceContainerHighest,
            contentColor = colorScheme.onSurface,
            shadowElevation = 6.dp,
            border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(if (showSecondary && hasSecondary) secondaryTitleRes else primaryTitleRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                if (hasSecondary) {
                    Surface(
                        onClick = { showSecondary = !showSecondary },
                        shape = CircleShape,
                        color = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.UnfoldMore,
                                contentDescription = stringResource(R.string.more_options),
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer { rotationZ = chevronRotation },
                            )
                        }
                    }
                }
            }
        }

        // Single Compact Unified Card (matches Pixel Launcher proportions)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface,
            shadowElevation = 6.dp,
            border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = showSecondary && hasSecondary,
                transitionSpec = {
                    (fadeIn(tween(110)) + scaleIn(initialScale = 0.96f, animationSpec = fastSpatial))
                        .togetherWith(fadeOut(tween(60)))
                },
                label = "compactPopupPage",
            ) { _ ->
                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    activeItems.forEach { action ->
                        val bg = if (action.selected) colorScheme.secondaryContainer else Color.Transparent
                        val fg = if (action.selected) colorScheme.onSecondaryContainer else colorScheme.onSurface
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bg)
                                .clickable { onItemClick(action) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = if (action.selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(action.labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (action.selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = fg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            action.trailingIcon?.let { trail ->
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = trail,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolioGallerySquircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "squircleScale",
    )

    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = colorScheme.surfaceContainerHighest,
        contentColor = colorScheme.onSurface,
        border = BorderStroke(0.6.dp, colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .size(46.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolioMorphTabChip(
    destination: FolioNavDestination,
    isSelected: Boolean,
    iconOnly: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val effectsColor = if (reducedMotion) tween(60) else motionScheme.fastEffectsSpec<Color>()
    val fastSpatial = if (reducedMotion) tween(60) else motionScheme.fastSpatialSpec<Float>()

    val bg by animateColorAsState(
        targetValue = if (isSelected) colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = effectsColor,
        label = "tabBg_${destination.id}",
    )
    val fg by animateColorAsState(
        targetValue = if (isSelected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
        animationSpec = effectsColor,
        label = "tabFg_${destination.id}",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = fastSpatial,
        label = "tabScale_${destination.id}",
    )

    Box(
        modifier = Modifier
            .height(46.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { selected = isSelected }
            .padding(horizontal = if (isSelected && !iconOnly) 14.dp else 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.labelRes),
                tint = fg,
                modifier = Modifier.size(20.dp),
            )
            AnimatedVisibility(
                visible = isSelected && !iconOnly,
                enter = expandHorizontally(
                    animationSpec = if (reducedMotion) tween(60) else motionScheme.defaultSpatialSpec(),
                    expandFrom = Alignment.Start,
                ) + fadeIn(tween(110, delayMillis = 30, easing = LinearEasing)),
                exit = shrinkHorizontally(
                    animationSpec = if (reducedMotion) tween(60) else motionScheme.defaultSpatialSpec(),
                    shrinkTowards = Alignment.Start,
                ) + fadeOut(tween(70, easing = LinearEasing)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(destination.labelRes),
                        color = fg,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolioIconPillButton(
    icon: ImageVector,
    contentDescription: String,
    iconSize: Dp = 20.dp,
    isActive: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "iconBtnScale",
    )
    val bg by animateColorAsState(
        targetValue = if (isActive) colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "iconBtnBg",
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(bg)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize),
        )
    }
}

private fun <S> AnimatedContentTransitionScope<S>.folioInnerMorphSpec(
    reducedMotion: Boolean,
    spatialSpec: FiniteAnimationSpec<Float>,
) =
    if (reducedMotion) {
        fadeIn(tween(70)) togetherWith fadeOut(tween(50))
    } else {
        (fadeIn(tween(durationMillis = 150, delayMillis = 55, easing = LinearEasing)) +
            scaleIn(initialScale = 0.94f, animationSpec = spatialSpec))
            .togetherWith(
                fadeOut(tween(durationMillis = 65, easing = LinearEasing)) +
                    scaleOut(targetScale = 0.94f, animationSpec = spatialSpec),
            )
            .using(SizeTransform(clip = false))
    }
