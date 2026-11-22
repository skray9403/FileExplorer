package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val PillHeight = 56.dp
private val CollapsedPillWidth = 56.dp
private val InterPillGap = 8.dp

enum class FolioBottomBarMode {
    /** Resting Dock: [Browse | Collections | Network | Tools] [+ or Paste] */
    DOCK_TABS,

    /** Fast Scroll Down: [🔍] [+ New folder] */
    SCROLL_ACTION,

    /** Fast Scroll Up: [🔍 Search] [+] */
    SCROLL_SEARCH,

    /** File Selection Mode: [Cut | Copy] [Delete | More] (or Archive Extract) */
    SELECTION_ACTIONS,
}

private enum class LeftSlotKind {
    DOCK_TABS,
    SCROLL_SEARCH_COLLAPSED,
    SCROLL_SEARCH_EXPANDED,
    SELECTION_EDIT,
    SELECTION_ARCHIVE,
}

private enum class RightSlotKind {
    ACTION_FAB,
    PASTE_FAB,
    ACTION_EXPANDED,
    SELECTION_MANAGE,
}

data class FolioNavDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Persistent hoisted 2-slot M3 Expressive floating bottom bar for Folio.
 *
 * - Both outer [Surface] containers stay mounted permanently; widths interpolate via
 *   [animateDpAsState] (`200ms` M3 Standard for scroll morph, `spring(0.85f, 500f)` for
 *   selection & mode transforms) and colors via critically-damped `stiffness = 1400f`.
 * - Through-zero inner content swap (`80ms` out, `90ms` delay + `170ms` in) prevents ghosting.
 * - Independent offset-gated `↑` scroll-to-top slot floats `12.dp` above the bar (`Δy = 0`).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolioMorphingBottomBar(
    selectedDestination: String,
    onDestinationSelected: (String) -> Unit,
    controller: FolioDockController,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = controller.resolvedMode
    val selection = controller.selectionActions
    val isScrollPair = mode == FolioBottomBarMode.SCROLL_ACTION || mode == FolioBottomBarMode.SCROLL_SEARCH
    val showPasteFab = controller.canPaste && mode != FolioBottomBarMode.SELECTION_ACTIONS

    val spatialSpec = if (isScrollPair) {
        tween<Dp>(durationMillis = 200, easing = M3StandardEasing)
    } else {
        spring<Dp>(dampingRatio = 0.85f, stiffness = 500f)
    }

    val effectsColorSpec = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 1400f,
    )

    val colorScheme = MaterialTheme.colorScheme

    val leftContainerTarget = when (mode) {
        FolioBottomBarMode.DOCK_TABS -> colorScheme.surfaceContainerHigh
        FolioBottomBarMode.SCROLL_ACTION, FolioBottomBarMode.SCROLL_SEARCH -> colorScheme.surfaceContainerHigh
        FolioBottomBarMode.SELECTION_ACTIONS -> colorScheme.surfaceContainerHigh
    }
    val leftContentTarget = colorScheme.onSurface

    val rightContainerTarget = when {
        mode == FolioBottomBarMode.SELECTION_ACTIONS -> colorScheme.surfaceContainerHigh
        showPasteFab -> colorScheme.primary
        mode == FolioBottomBarMode.DOCK_TABS || mode == FolioBottomBarMode.SCROLL_ACTION -> colorScheme.primaryContainer
        else -> colorScheme.secondaryContainer
    }
    val rightContentTarget = when {
        mode == FolioBottomBarMode.SELECTION_ACTIONS -> colorScheme.onSurface
        showPasteFab -> colorScheme.onPrimary
        mode == FolioBottomBarMode.DOCK_TABS || mode == FolioBottomBarMode.SCROLL_ACTION -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSecondaryContainer
    }

    val leftContainerColor by animateColorAsState(leftContainerTarget, effectsColorSpec, label = "folioLeftBg")
    val leftContentColor by animateColorAsState(leftContentTarget, effectsColorSpec, label = "folioLeftFg")
    val rightContainerColor by animateColorAsState(rightContainerTarget, effectsColorSpec, label = "folioRightBg")
    val rightContentColor by animateColorAsState(rightContentTarget, effectsColorSpec, label = "folioRightFg")

    val destinations = listOf(
        FolioNavDestination("browser", stringResource(R.string.browse), Icons.Rounded.Folder),
        FolioNavDestination("collections", stringResource(R.string.collections), Icons.Rounded.GridView),
        FolioNavDestination("network", stringResource(R.string.network), Icons.Rounded.Lan),
        FolioNavDestination("tools", stringResource(R.string.tools), Icons.Rounded.Widgets),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Independent scroll-to-top button (offset-gated > 160dp, Δy = 0 on bottom pills)
        AnimatedVisibility(
            visible = controller.showScrollToTop && mode != FolioBottomBarMode.SELECTION_ACTIONS,
            enter = fadeIn(tween(120)) +
                scaleIn(tween(120), initialScale = 0.75f) +
                slideInVertically(tween(120)) { it / 3 },
            exit = fadeOut(tween(100)) +
                scaleOut(tween(100), targetScale = 0.75f) +
                slideOutVertically(tween(100)) { it / 3 },
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
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Scroll to top",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Persistent 2-slot morphing container cluster
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val usableWidth = (maxWidth - InterPillGap).coerceAtLeast(180.dp)

            val leftTargetWidth = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> {
                    val rightW = if (showPasteFab) 104.dp else CollapsedPillWidth
                    (usableWidth - rightW).coerceAtLeast(196.dp)
                }
                FolioBottomBarMode.SCROLL_ACTION -> CollapsedPillWidth
                FolioBottomBarMode.SCROLL_SEARCH -> 146.dp.coerceAtMost(usableWidth - CollapsedPillWidth)
                FolioBottomBarMode.SELECTION_ACTIONS -> usableWidth / 2f
            }

            val rightTargetWidth = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> if (showPasteFab) 104.dp else CollapsedPillWidth
                FolioBottomBarMode.SCROLL_ACTION -> 152.dp.coerceAtMost(usableWidth - CollapsedPillWidth)
                FolioBottomBarMode.SCROLL_SEARCH -> CollapsedPillWidth
                FolioBottomBarMode.SELECTION_ACTIONS -> usableWidth / 2f
            }

            val leftWidth by animateDpAsState(leftTargetWidth, spatialSpec, label = "folioLeftWidth")
            val rightWidth by animateDpAsState(rightTargetWidth, spatialSpec, label = "folioRightWidth")

            val leftSlotKind = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> LeftSlotKind.DOCK_TABS
                FolioBottomBarMode.SCROLL_ACTION -> LeftSlotKind.SCROLL_SEARCH_COLLAPSED
                FolioBottomBarMode.SCROLL_SEARCH -> LeftSlotKind.SCROLL_SEARCH_EXPANDED
                FolioBottomBarMode.SELECTION_ACTIONS -> {
                    if (selection.insideArchive) LeftSlotKind.SELECTION_ARCHIVE else LeftSlotKind.SELECTION_EDIT
                }
            }

            val rightSlotKind = when {
                mode == FolioBottomBarMode.SELECTION_ACTIONS -> RightSlotKind.SELECTION_MANAGE
                showPasteFab -> RightSlotKind.PASTE_FAB
                mode == FolioBottomBarMode.SCROLL_ACTION -> RightSlotKind.ACTION_EXPANDED
                else -> RightSlotKind.ACTION_FAB
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(InterPillGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LEFT PERSISTENT PILL
                Surface(
                    shape = CircleShape,
                    color = leftContainerColor,
                    contentColor = leftContentColor,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .width(leftWidth)
                        .height(PillHeight)
                        .clip(CircleShape),
                ) {
                    AnimatedContent(
                        targetState = leftSlotKind,
                        transitionSpec = { folioThroughZeroTransform() },
                        label = "folioLeftSlotContent",
                        modifier = Modifier.fillMaxSize(),
                    ) { kind ->
                        when (kind) {
                            LeftSlotKind.DOCK_TABS -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    destinations.forEach { dest ->
                                        FolioDockChip(
                                            destination = dest,
                                            isSelected = dest.id == selectedDestination,
                                            onClick = { onDestinationSelected(dest.id) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }

                            LeftSlotKind.SCROLL_SEARCH_COLLAPSED -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = onSearchClick,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }

                            LeftSlotKind.SCROLL_SEARCH_EXPANDED -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = onSearchClick,
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.search),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }

                            LeftSlotKind.SELECTION_EDIT -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .alpha(if (selection.canMove) 1f else 0.38f)
                                            .clickable(enabled = selection.canMove, onClick = selection.onCut),
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
                                            .background(colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    )
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .alpha(if (selection.canCopy) 1f else 0.38f)
                                            .clickable(enabled = selection.canCopy, onClick = selection.onCopy),
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

                            LeftSlotKind.SELECTION_ARCHIVE -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (selection.canExtract) 1f else 0.38f)
                                        .clickable(enabled = selection.canExtract, onClick = selection.onExtract),
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

                // RIGHT PERSISTENT PILL
                Surface(
                    shape = CircleShape,
                    color = rightContainerColor,
                    contentColor = rightContentColor,
                    shadowElevation = 6.dp,
                    border = if (mode == FolioBottomBarMode.SELECTION_ACTIONS) {
                        BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f))
                    } else {
                        null
                    },
                    modifier = Modifier
                        .width(rightWidth)
                        .height(PillHeight)
                        .clip(CircleShape),
                ) {
                    AnimatedContent(
                        targetState = rightSlotKind,
                        transitionSpec = { folioThroughZeroTransform() },
                        label = "folioRightSlotContent",
                        modifier = Modifier.fillMaxSize(),
                    ) { kind ->
                        when (kind) {
                            RightSlotKind.ACTION_FAB -> {
                                val actionEnabled = controller.canCreatePrimaryAction
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (actionEnabled) 1f else 0.5f)
                                        .clickable(
                                            enabled = actionEnabled,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = controller.onPrimaryAction,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (selectedDestination == "browser") Icons.Rounded.Add else Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.new_folder),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            RightSlotKind.PASTE_FAB -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = controller.onPaste,
                                        )
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(Icons.Rounded.ContentPaste, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.paste),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            RightSlotKind.ACTION_EXPANDED -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            enabled = controller.canCreatePrimaryAction,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = controller.onPrimaryAction,
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CreateNewFolder,
                                        contentDescription = stringResource(R.string.new_folder),
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.new_folder),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            RightSlotKind.SELECTION_MANAGE -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!selection.insideArchive) {
                                        val deleteEnabled = selection.canTrash || selection.canDelete
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .alpha(if (deleteEnabled) 1f else 0.38f)
                                                .combinedClickable(
                                                    enabled = deleteEnabled,
                                                    onClick = { if (selection.canTrash) selection.onDelete() else selection.onPermanentDelete() },
                                                    onLongClick = selection.onPermanentDelete,
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
                                                .background(colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(onClick = selection.onMore),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Rounded.MoreHoriz, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.more),
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

@Composable
private fun FolioDockChip(
    destination: FolioNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectsSpec = MonoSpring.noBounceColor<Color>()

    val chipBg by animateColorAsState(
        targetValue = if (isSelected) colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = effectsSpec,
        label = "folioChipBg_${destination.id}",
    )
    val chipFg by animateColorAsState(
        targetValue = if (isSelected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
        animationSpec = effectsSpec,
        label = "folioChipFg_${destination.id}",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = MonoSpring.bouncy(),
        label = "folioChipPress_${destination.id}",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer(scaleX = pressScale, scaleY = pressScale)
            .clip(CircleShape)
            .background(chipBg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { selected = isSelected },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = chipFg,
                modifier = Modifier.size(20.dp),
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(
                    animationSpec = tween(200, easing = M3StandardEasing),
                    expandFrom = Alignment.Start,
                    clip = true,
                ) + fadeIn(tween(110, delayMillis = 40, easing = LinearEasing)),
                exit = shrinkHorizontally(
                    animationSpec = tween(200, easing = M3StandardEasing),
                    shrinkTowards = Alignment.Start,
                    clip = true,
                ) + fadeOut(tween(70, easing = LinearEasing)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = destination.label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = chipFg,
                    )
                }
            }
        }
    }
}

private fun <S> AnimatedContentTransitionScope<S>.folioThroughZeroTransform() =
    (fadeIn(tween(durationMillis = 170, delayMillis = 90, easing = LinearEasing)) +
        scaleIn(initialScale = 0.92f, animationSpec = tween(170, delayMillis = 90, easing = FastOutSlowInEasing)))
        .togetherWith(
            fadeOut(tween(durationMillis = 80, easing = LinearEasing)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(80, easing = FastOutSlowInEasing)),
        )
        .using(SizeTransform(clip = false))
