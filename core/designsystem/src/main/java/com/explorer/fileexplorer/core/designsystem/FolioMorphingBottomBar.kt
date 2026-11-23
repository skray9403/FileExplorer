package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.rounded.ContentPaste
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.unit.dp

private data class FolioNavDestination(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector,
)

/**
 * Native Material 3 Expressive 3-slot floating bottom dock.
 *
 * Slot 1: Primary Toolbar ([HorizontalFloatingToolbar]) - Subfolder back/search/home or root tabs.
 * Slot 2: Task & Progress Pill - Displays badge count & aggregate progress fill when background tasks exist.
 * Slot 3: Action FAB / Menu - Paste pill or [FloatingActionButtonMenu] + [ToggleFloatingActionButton].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    val destinations = remember {
        listOf(
            FolioNavDestination("browser", R.string.browse, Icons.Rounded.Folder),
            FolioNavDestination("collections", R.string.collections, Icons.Rounded.GridView),
            FolioNavDestination("tools", R.string.tools, Icons.Rounded.Widgets),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Independent scroll-to-top button
        AnimatedVisibility(
            visible = controller.showScrollToTop && !controller.selectionMode && !controller.inlineSearchActive,
            enter = if (reducedMotion) fadeIn(tween(80)) else (fadeIn(tween(120)) + scaleIn(tween(120), initialScale = 0.75f) + slideInVertically(tween(120)) { it / 3 }),
            exit = if (reducedMotion) fadeOut(tween(80)) else (fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.75f) + slideOutVertically(tween(100)) { it / 3 }),
        ) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                Surface(
                    onClick = {
                        controller.onScrollToTop()
                        controller.resetScrollMorph()
                    },
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHigh,
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

        when {
            // Mode 1: Inline Search Active
            controller.inlineSearchActive -> {
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
                                .padding(horizontal = 8.dp),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (controller.inlineSearchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_files_hint),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        // Scope Toggle Chip (Folder / All)
                        Surface(
                            onClick = controller.onInlineSearchScopeToggle,
                            shape = CircleShape,
                            color = if (controller.inlineSearchRecursive) colorScheme.primaryContainer else colorScheme.surfaceContainerHighest,
                            contentColor = if (controller.inlineSearchRecursive) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(32.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp),
                            ) {
                                Text(
                                    text = stringResource(if (controller.inlineSearchRecursive) R.string.all_scope else R.string.folder_scope),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        if (controller.inlineSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { controller.onInlineSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Mode 2: Multi-Item Selection Mode
            controller.selectionMode -> {
                val actions = controller.selectionActions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left Selection Pill: Cut | Copy or Extract
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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        enabled = actions.canDelete || actions.canTrash,
                                        onClick = actions.onDelete,
                                        onLongClick = actions.onPermanentDelete,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = null,
                                        tint = if (actions.canDelete || actions.canTrash) colorScheme.error else colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.delete),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = if (actions.canDelete || actions.canTrash) colorScheme.error else colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
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

            // Mode 3: Normal Resting 3-Slot Dock
            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    // Slots 1 & 2: Toolbar & Task Progress Pill
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Slot 1: Primary Toolbar
                    HorizontalFloatingToolbar(
                        expanded = true,
                        modifier = Modifier.height(56.dp),
                    ) {
                        if (controller.isSubfolder) {
                            // Subfolder Navigation Toolbar: [← Back | 🔍 Search | ⌂ Home]
                            IconButton(onClick = controller.onNavigateUp) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                )
                            }
                            IconButton(onClick = { controller.inlineSearchActive = true }) {
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
                            // Root Dock Navigation: [Browse | Collections | Tools | Search]
                            destinations.forEach { dest ->
                                val isSelected = selectedDestination == dest.id
                                val pillBg by animateColorAsState(
                                    targetValue = if (isSelected) colorScheme.primaryContainer else Color.Transparent,
                                    animationSpec = tween(180),
                                    label = "tab_pill_bg",
                                )
                                val pillFg by animateColorAsState(
                                    targetValue = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                                    animationSpec = tween(180),
                                    label = "tab_pill_fg",
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
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        if (isSelected) {
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

                            IconButton(onClick = onSearchClick) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(R.string.search),
                                )
                            }
                        }
                    }

                    // Slot 2: Task & Progress Pill (Visible when active jobs exist)
                    if (controller.taskBadgeCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        val progress = controller.taskProgress
                        val hasConflict = controller.taskHasConflict
                        val progressFillColor = if (hasConflict) {
                            colorScheme.errorContainer.copy(alpha = 0.5f)
                        } else {
                            colorScheme.primary.copy(alpha = 0.22f)
                        }

                        Surface(
                            onClick = { controller.onTaskPillClick?.invoke() },
                            shape = CircleShape,
                            color = colorScheme.surfaceContainerHigh,
                            contentColor = if (hasConflict) colorScheme.error else colorScheme.primary,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .height(56.dp)
                                .clip(CircleShape)
                                .drawBehind {
                                    if (progress != null && progress > 0f) {
                                        drawRect(
                                            color = progressFillColor,
                                            size = androidx.compose.ui.geometry.Size(
                                                width = size.width * progress.coerceIn(0f, 1f),
                                                height = size.height,
                                            ),
                                        )
                                    }
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = stringResource(R.string.tasks_and_progress),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${controller.taskBadgeCount}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // Slot 3: Action FAB / FAB Menu (Anchored to BottomEnd)
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd),
                ) {
                        if (controller.canPaste) {
                            Surface(
                                onClick = controller.onPaste,
                                shape = CircleShape,
                                color = colorScheme.primary,
                                contentColor = colorScheme.onPrimary,
                                shadowElevation = 6.dp,
                                modifier = Modifier.height(56.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentPaste,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.paste_here),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        } else if (controller.fabMenuItems.isNotEmpty()) {
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
                                            contentDescription = stringResource(if (fabMenuExpanded) R.string.close else R.string.more_options),
                                            modifier = Modifier.graphicsLayer { rotationZ = rotation },
                                        )
                                    }
                                },
                            ) {
                                controller.fabMenuItems.forEach { action ->
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
