package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private data class FolioNavDestination(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private enum class DockRenderMode {
    INLINE_SEARCH,
    SELECTION,
    NORMAL,
}

/**
 * Native Material 3 Expressive 3-slot floating bottom dock.
 *
 * Slot 1: Primary Toolbar ([HorizontalFloatingToolbar]) - Root tabs or Subfolder [Back | Search | Home].
 * Slot 2: Conditional Task & Progress Pill - Shows stacked clipboard queue and/or live transfer progress.
 * Slot 3: Contextual [FloatingActionButtonMenu] (+) anchored cleanly to BottomEnd.
 */
@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)
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
    val colorScheme = MaterialTheme.colorScheme

    // Always auto-collapse the FAB menu on navigation, folder change, selection, or search
    LaunchedEffect(
        selectedDestination,
        controller.contextResetKey,
        controller.isSubfolder,
        controller.selectionMode,
        controller.inlineSearchActive,
    ) {
        fabMenuExpanded = false
    }

    val destinations = remember {
        listOf(
            FolioNavDestination("browser", R.string.browse, Icons.Rounded.Folder),
            FolioNavDestination("collections", R.string.collections, Icons.Rounded.GridView),
            FolioNavDestination("tools", R.string.tools, Icons.Rounded.Widgets),
        )
    }

    val renderMode = when {
        controller.inlineSearchActive -> DockRenderMode.INLINE_SEARCH
        controller.selectionMode -> DockRenderMode.SELECTION
        else -> DockRenderMode.NORMAL
    }

    val triggerSearch: () -> Unit = {
        fabMenuExpanded = false
        controller.onInlineSearchOpen?.invoke() ?: onSearchClick()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.End,
    ) {
        // Independent scroll-to-top button tucked cleanly above the FAB
        AnimatedVisibility(
            visible = controller.showScrollToTop &&
                !controller.selectionMode &&
                !controller.inlineSearchActive &&
                !fabMenuExpanded,
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
        ) {
            Box(modifier = Modifier.padding(bottom = 10.dp, end = 7.dp)) {
                Surface(
                    onClick = {
                        controller.onScrollToTop()
                        controller.resetScrollMorph()
                    },
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                    contentColor = colorScheme.onSurface,
                    shadowElevation = 4.dp,
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

        AnimatedContent(
            targetState = renderMode,
            transitionSpec = {
                val dur = if (reducedMotion) 80 else 180
                fadeIn(tween(dur)) togetherWith fadeOut(tween(dur / 2))
            },
            label = "folio_dock_mode",
            modifier = Modifier.fillMaxWidth(),
        ) { mode ->
            when (mode) {
                DockRenderMode.INLINE_SEARCH -> {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        runCatching { focusRequester.requestFocus() }
                    }
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = controller.onInlineSearchClose) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.close_search),
                                )
                            }

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
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )

                            if (controller.inlineSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { controller.onInlineSearchQueryChange("") },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.clear_search),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Spacer(Modifier.width(4.dp))

                            // Right-edge scope toggle chip (Folder / All)
                            Surface(
                                onClick = controller.onInlineSearchScopeToggle,
                                shape = CircleShape,
                                color = if (controller.inlineSearchRecursive) {
                                    colorScheme.primaryContainer
                                } else {
                                    colorScheme.surfaceContainerHighest
                                },
                                contentColor = if (controller.inlineSearchRecursive) {
                                    colorScheme.onPrimaryContainer
                                } else {
                                    colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .height(34.dp)
                                    .padding(end = 4.dp),
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
                                    )
                                }
                            }
                        }
                    }
                }

                DockRenderMode.SELECTION -> {
                    val actions = controller.selectionActions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left Selection Pill: Cut | Copy (or Extract inside archive)
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.surfaceContainerHigh,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        ) {
                            if (actions.insideArchive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (actions.canExtract) 1f else 0.4f)
                                        .clickable(enabled = actions.canExtract, onClick = actions.onExtract),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Unarchive, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.extract_all),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .alpha(if (actions.canMove) 1f else 0.4f)
                                            .clickable(enabled = actions.canMove, onClick = actions.onCut),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.ContentCut, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.cut),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .alpha(if (actions.canCopy) 1f else 0.4f)
                                            .clickable(enabled = actions.canCopy, onClick = actions.onCopy),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.copy),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right Selection Pill: Delete | More
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.surfaceContainerHigh,
                            contentColor = colorScheme.onSurface,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val deleteEnabled = actions.canDelete || actions.canTrash
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .combinedClickable(
                                            enabled = deleteEnabled,
                                            onClick = { if (actions.canTrash) actions.onDelete() else actions.onPermanentDelete() },
                                            onLongClick = actions.onPermanentDelete,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = null,
                                            tint = if (deleteEnabled) colorScheme.error else colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.delete),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = if (deleteEnabled) colorScheme.error else colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(onClick = actions.onMore),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.MoreHoriz, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.more),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                DockRenderMode.NORMAL -> {
                    val hasTasks = controller.taskBadgeCount > 0
                    val hasFab = controller.fabMenuItems.isNotEmpty()
                    val compactTabs = hasTasks

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        // Slots 1 & 2 stay strictly clear of the 56dp FAB on the right
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(end = if (hasFab) 64.dp else 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Slot 1: Primary Toolbar
                            HorizontalFloatingToolbar(
                                expanded = true,
                                modifier = Modifier.height(56.dp),
                            ) {
                                if (controller.isSubfolder) {
                                    // Subfolder pill: [Back (long-press root) | Search | Home]
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .combinedClickable(
                                                onClick = controller.onNavigateUp,
                                                onLongClick = controller.onNavigateToRoot,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.back),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    IconButton(onClick = triggerSearch) {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = stringResource(R.string.search),
                                        )
                                    }
                                    IconButton(onClick = controller.onNavigateToRoot) {
                                        Icon(
                                            imageVector = Icons.Rounded.Home,
                                            contentDescription = stringResource(R.string.home),
                                        )
                                    }
                                } else {
                                    destinations.forEach { dest ->
                                        val isSelected = selectedDestination == dest.id
                                        val pillBg by animateColorAsState(
                                            targetValue = if (isSelected) colorScheme.primaryContainer else Color.Transparent,
                                            animationSpec = tween(if (reducedMotion) 60 else 180),
                                            label = "tab_pill_bg_${dest.id}",
                                        )
                                        val pillFg by animateColorAsState(
                                            targetValue = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                            animationSpec = tween(if (reducedMotion) 60 else 180),
                                            label = "tab_pill_fg_${dest.id}",
                                        )

                                        Surface(
                                            onClick = { onDestinationSelected(dest.id) },
                                            shape = CircleShape,
                                            color = pillBg,
                                            contentColor = pillFg,
                                            modifier = Modifier
                                                .height(42.dp)
                                                .semantics { selected = isSelected },
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = if (isSelected && !compactTabs) 14.dp else 11.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(
                                                    imageVector = dest.icon,
                                                    contentDescription = if (!isSelected || compactTabs) stringResource(dest.labelRes) else null,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                if (isSelected && !compactTabs) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = stringResource(dest.labelRes),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    IconButton(onClick = triggerSearch) {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = stringResource(R.string.search),
                                        )
                                    }
                                }
                            }

                            // Slot 2: Unified Task Stack & Live Transfer Progress Bubble
                            if (hasTasks) {
                                val progress = controller.taskProgress
                                val hasConflict = controller.taskHasConflict
                                val progressFillColor = if (hasConflict) {
                                    colorScheme.errorContainer.copy(alpha = 0.55f)
                                } else {
                                    colorScheme.primaryContainer.copy(alpha = 0.72f)
                                }

                                Surface(
                                    onClick = {
                                        fabMenuExpanded = false
                                        controller.onTaskPillClick?.invoke()
                                    },
                                    shape = CircleShape,
                                    color = colorScheme.surfaceContainerHigh,
                                    contentColor = if (hasConflict) colorScheme.error else colorScheme.primary,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .height(56.dp)
                                        .widthIn(min = 60.dp, max = 96.dp)
                                        .clip(CircleShape)
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
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .fillMaxHeight(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapHoriz,
                                            contentDescription = stringResource(R.string.tasks_and_progress),
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        val badgeLabel = if (progress != null && progress > 0f) {
                                            "${(progress * 100f).roundToInt()}%"
                                        } else {
                                            "${controller.taskBadgeCount}"
                                        }
                                        Text(
                                            text = badgeLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }

                        // Slot 3: Contextual FAB Menu (+) anchored cleanly to BottomEnd
                        if (hasFab) {
                            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                FloatingActionButtonMenu(
                                    expanded = fabMenuExpanded,
                                    button = {
                                        ToggleFloatingActionButton(
                                            checked = fabMenuExpanded,
                                            onCheckedChange = { fabMenuExpanded = it },
                                            containerSize = { 56.dp },
                                            containerCornerRadius = { 28.dp },
                                        ) {
                                            val rotation by animateFloatAsState(
                                                targetValue = if (fabMenuExpanded) 45f else 0f,
                                                label = "fab_rotation",
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = stringResource(
                                                    if (fabMenuExpanded) R.string.close else R.string.more_options,
                                                ),
                                                modifier = Modifier.graphicsLayer { rotationZ = rotation },
                                            )
                                        }
                                    },
                                ) {
                                    controller.fabMenuItems
                                        .filter { it.enabled }
                                        .forEach { action ->
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
                    }
                }
            }
        }
    }
}
