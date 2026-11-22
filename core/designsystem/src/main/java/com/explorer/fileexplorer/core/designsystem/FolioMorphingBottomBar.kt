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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
private val InterPillGap = 10.dp

enum class FolioBottomBarMode {
    /** Resting Dock: [Browse | Transfers | Network | Collections] [+] */
    DOCK_TABS,

    /** Fast Scroll Down: [🔍] [+ New folder] */
    SCROLL_ACTION,

    /** Fast Scroll Up: [🔍 Search files...] [➕] */
    SCROLL_SEARCH,

    /** Selection Mode: [Cut | Copy] [Delete | More] */
    SELECTION_ACTIONS,
}

private enum class LeftSlotKind { DOCK_TABS, SCROLL_SEARCH_COLLAPSED, SCROLL_SEARCH_EXPANDED, SELECTION_EDIT }
private enum class RightSlotKind { ACTION_FAB, ACTION_EXPANDED, SELECTION_MANAGE }

data class FolioNavDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Signature 2-slot floating bottom bar with continuous M3 Expressive morphing.
 *
 * - The two outer [Surface] containers NEVER unmount; widths animate via [animateDpAsState]
 *   and colors via critically-damped [animateColorAsState].
 * - Intra-screen scroll morph uses M3 Standard easing (200ms).
 * - Cross-mode morph uses M3 Expressive spatial spring (dampingRatio = 0.85f, stiffness = 500f).
 * - Through-zero inner content swap eliminates overlapping ghost frames.
 * - The scroll-to-top button floats independently in a slot 12dp above the pills (Δy = 0 on dock).
 */
@Composable
fun FolioMorphingBottomBar(
    selectedDestination: String,
    onDestinationSelected: (String) -> Unit,
    mode: FolioBottomBarMode = FolioBottomBarMode.DOCK_TABS,
    showScrollToTop: Boolean = false,
    onScrollToTop: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onCutClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isScrollPair = mode == FolioBottomBarMode.SCROLL_ACTION || mode == FolioBottomBarMode.SCROLL_SEARCH

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
        FolioBottomBarMode.DOCK_TABS -> colorScheme.surfaceContainer
        FolioBottomBarMode.SCROLL_ACTION, FolioBottomBarMode.SCROLL_SEARCH -> colorScheme.surfaceContainerHigh
        FolioBottomBarMode.SELECTION_ACTIONS -> colorScheme.surfaceContainerHigh
    }
    val leftContentTarget = colorScheme.onSurface

    val rightContainerTarget = when (mode) {
        FolioBottomBarMode.DOCK_TABS, FolioBottomBarMode.SCROLL_ACTION -> colorScheme.primary
        FolioBottomBarMode.SCROLL_SEARCH -> colorScheme.surfaceContainer
        FolioBottomBarMode.SELECTION_ACTIONS -> colorScheme.surfaceContainerHigh
    }
    val rightContentTarget = when (mode) {
        FolioBottomBarMode.DOCK_TABS, FolioBottomBarMode.SCROLL_ACTION -> colorScheme.onPrimary
        else -> colorScheme.onSurface
    }

    val leftContainerColor by animateColorAsState(leftContainerTarget, effectsColorSpec, label = "leftBg")
    val leftContentColor by animateColorAsState(leftContentTarget, effectsColorSpec, label = "leftFg")
    val rightContainerColor by animateColorAsState(rightContainerTarget, effectsColorSpec, label = "rightBg")
    val rightContentColor by animateColorAsState(rightContentTarget, effectsColorSpec, label = "rightFg")

    val destinations = listOf(
        FolioNavDestination("browser", stringResource(R.string.browse), Icons.Filled.Folder),
        FolioNavDestination("transfers", stringResource(R.string.transfer_queue), Icons.Filled.SwapHoriz),
        FolioNavDestination("network", stringResource(R.string.network), Icons.Filled.Lan),
        FolioNavDestination("collections", stringResource(R.string.collections), Icons.Filled.Collections),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Independent scroll-to-top slot (Δy = 0 on dock pills)
        AnimatedVisibility(
            visible = showScrollToTop && mode != FolioBottomBarMode.SELECTION_ACTIONS,
            enter = fadeIn(tween(120)) +
                scaleIn(tween(120), initialScale = 0.75f) +
                slideInVertically(tween(120)) { it / 3 },
            exit = fadeOut(tween(100)) +
                scaleOut(tween(100), targetScale = 0.75f) +
                slideOutVertically(tween(100)) { it / 3 },
        ) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                Surface(
                    onClick = onScrollToTop,
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHigh,
                    contentColor = colorScheme.onSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = stringResource(R.string.quick_access),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Persistent 2-slot pill cluster
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val usableWidth = (maxWidth - InterPillGap).coerceAtLeast(180.dp)

            val leftTargetWidth = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> (usableWidth - CollapsedPillWidth).coerceAtLeast(200.dp)
                FolioBottomBarMode.SCROLL_ACTION -> CollapsedPillWidth
                FolioBottomBarMode.SCROLL_SEARCH -> (usableWidth - CollapsedPillWidth).coerceAtLeast(180.dp)
                FolioBottomBarMode.SELECTION_ACTIONS -> usableWidth / 2f
            }

            val rightTargetWidth = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> CollapsedPillWidth
                FolioBottomBarMode.SCROLL_ACTION -> (usableWidth - CollapsedPillWidth).coerceAtLeast(140.dp)
                FolioBottomBarMode.SCROLL_SEARCH -> CollapsedPillWidth
                FolioBottomBarMode.SELECTION_ACTIONS -> usableWidth / 2f
            }

            val leftWidth by animateDpAsState(leftTargetWidth, spatialSpec, label = "leftWidth")
            val rightWidth by animateDpAsState(rightTargetWidth, spatialSpec, label = "rightWidth")

            val leftSlotKind = when (mode) {
                FolioBottomBarMode.DOCK_TABS -> LeftSlotKind.DOCK_TABS
                FolioBottomBarMode.SCROLL_ACTION -> LeftSlotKind.SCROLL_SEARCH_COLLAPSED
                FolioBottomBarMode.SCROLL_SEARCH -> LeftSlotKind.SCROLL_SEARCH_EXPANDED
                FolioBottomBarMode.SELECTION_ACTIONS -> LeftSlotKind.SELECTION_EDIT
            }

            val rightSlotKind = when (mode) {
                FolioBottomBarMode.DOCK_TABS, FolioBottomBarMode.SCROLL_SEARCH -> RightSlotKind.ACTION_FAB
                FolioBottomBarMode.SCROLL_ACTION -> RightSlotKind.ACTION_EXPANDED
                FolioBottomBarMode.SELECTION_ACTIONS -> RightSlotKind.SELECTION_MANAGE
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
                    modifier = Modifier
                        .width(leftWidth)
                        .height(PillHeight)
                        .clip(CircleShape),
                ) {
                    AnimatedContent(
                        targetState = leftSlotKind,
                        transitionSpec = { throughZeroContentTransform() },
                        label = "leftSlotContent",
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
                                        val isSelected = dest.id == selectedDestination
                                        val chipBg by animateColorAsState(
                                            targetValue = if (isSelected) colorScheme.surfaceContainerHighest else Color.Transparent,
                                            animationSpec = effectsColorSpec,
                                            label = "destBg_${dest.id}",
                                        )
                                        val contentColor by animateColorAsState(
                                            targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                                            animationSpec = effectsColorSpec,
                                            label = "destFg_${dest.id}",
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(chipBg)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true),
                                                    role = Role.Tab,
                                                    onClick = { onDestinationSelected(dest.id) },
                                                )
                                                .semantics {
                                                    selected = isSelected
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(horizontal = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = dest.icon,
                                                    contentDescription = dest.label,
                                                    tint = contentColor,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                AnimatedVisibility(
                                                    visible = isSelected,
                                                    enter = expandHorizontally(
                                                        animationSpec = tween(180, easing = M3StandardEasing),
                                                    ) + fadeIn(tween(140)),
                                                    exit = shrinkHorizontally(
                                                        animationSpec = tween(140, easing = M3StandardEasing),
                                                    ) + fadeOut(tween(100)),
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = dest.label,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            overflow = TextOverflow.Clip,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = contentColor,
                                                        )
                                                    }
                                                }
                                            }
                                        }
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
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(24.dp),
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
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.search),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            LeftSlotKind.SELECTION_EDIT -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(onClick = onCutClick),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Filled.ContentCut, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.cut), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp)
                                            .background(colorScheme.outlineVariant),
                                    )
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(onClick = onCopyClick),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.copy), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
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
                    modifier = Modifier
                        .width(rightWidth)
                        .height(PillHeight)
                        .clip(CircleShape),
                ) {
                    AnimatedContent(
                        targetState = rightSlotKind,
                        transitionSpec = { throughZeroContentTransform() },
                        label = "rightSlotContent",
                        modifier = Modifier.fillMaxSize(),
                    ) { kind ->
                        when (kind) {
                            RightSlotKind.ACTION_FAB -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = onActionClick,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.new_folder),
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }

                            RightSlotKind.ACTION_EXPANDED -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = onActionClick,
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CreateNewFolder,
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
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(onClick = onDeleteClick),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp)
                                            .background(colorScheme.outlineVariant),
                                    )
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(onClick = onMoreClick),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Filled.MoreHoriz, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.more), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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

private fun <S> AnimatedContentTransitionScope<S>.throughZeroContentTransform() =
    (fadeIn(tween(durationMillis = 170, delayMillis = 90, easing = LinearEasing)) +
        scaleIn(initialScale = 0.92f, animationSpec = tween(170, delayMillis = 90, easing = FastOutSlowInEasing)))
        .togetherWith(
            fadeOut(tween(durationMillis = 80, easing = LinearEasing)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(80, easing = FastOutSlowInEasing)),
        )
        .using(SizeTransform(clip = false))
