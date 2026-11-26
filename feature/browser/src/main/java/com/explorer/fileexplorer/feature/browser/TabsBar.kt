package com.explorer.fileexplorer.feature.browser

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.explorer.fileexplorer.core.data.UsbPathCodec
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSwitchableActionPopup
import com.explorer.fileexplorer.core.designsystem.MonoSpring
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.StorageVolume
import kotlin.math.abs

internal fun resolveTabLabel(
    path: String,
    volumes: List<StorageVolume>,
    fallbackStorage: String,
): String {
    if (UsbPathCodec.isUsbPath(path)) {
        return UsbPathCodec.name(path)?.ifBlank { fallbackStorage } ?: fallbackStorage
    }
    val cleaned = path.trimEnd('/')
    if (cleaned.isEmpty() || cleaned == "/") return "/"
    volumes.firstOrNull { it.path.trimEnd('/') == cleaned }?.let { return it.name }
    if (cleaned == "/storage/emulated/0") return fallbackStorage
    return cleaned.substringAfterLast('/').ifBlank { fallbackStorage }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BrowserTabsBar(
    tabs: List<BrowserTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onAdd: () -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    volumes: List<StorageVolume> = emptyList(),
    showAddButton: Boolean = true,
    floating: Boolean = false,
    onOpenInSplit: ((String) -> Unit)? = null,
    onCopyTabPath: ((String) -> Unit)? = null,
) {
    val tabBounds = remember { mutableStateMapOf<Long, Rect>() }
    var reorderingId by remember { mutableStateOf<Long?>(null) }
    var menuTabIndex by remember { mutableStateOf<Int?>(null) }
    val currentTabs by rememberUpdatedState(tabs)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnClose by rememberUpdatedState(onClose)
    val currentOnReorder by rememberUpdatedState(onReorder)
    val storageFallback = stringResource(DesignSystemR.string.storage)
    val haptic = LocalHapticFeedback.current
    val containerColor = if (floating) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        color = containerColor,
        tonalElevation = if (floating) 0.dp else 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (floating) 4.dp else 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val tabId = tab.id
                    val latestIndex by rememberUpdatedState(index)
                    val label = resolveTabLabel(tab.path, volumes, storageFallback)
                    Box {
                        TabPillChip(
                            tabId = tabId,
                            label = label,
                            selected = index == selectedIndex,
                            canClose = tabs.size > 1,
                            isReordering = reorderingId == tabId,
                            getStartPosition = { tabBounds[tabId]?.center?.x ?: 0f },
                            modifier = Modifier.onGloballyPositioned { tabBounds[tabId] = it.boundsInRoot() },
                            onSelect = { currentOnSelect(latestIndex) },
                            onClose = { currentOnClose(latestIndex) },
                            onLongPressMenu = {
                                if (currentTabs.size > 1) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuTabIndex = latestIndex
                                }
                            },
                            onReorderStart = {
                                menuTabIndex = null
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                reorderingId = tabId
                            },
                            onReorderDragEnd = { position ->
                                if (reorderingId == tabId) {
                                    val activeTabs = currentTabs
                                    val target = activeTabs.indices.firstOrNull { targetIndex ->
                                        tabBounds[activeTabs[targetIndex].id]?.let { position < it.center.x } == true
                                    } ?: activeTabs.lastIndex.coerceAtLeast(0)
                                    if (latestIndex != target) currentOnReorder(latestIndex, target)
                                    reorderingId = null
                                }
                            },
                            onReorderCancel = {
                                if (reorderingId == tabId) reorderingId = null
                            },
                        )
                        if (menuTabIndex == index) {
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(0, 108),
                                onDismissRequest = { menuTabIndex = null },
                                properties = PopupProperties(focusable = true),
                            ) {
                                val actions = buildList {
                                    if (onOpenInSplit != null) add(
                                        FolioFabMenuAction(
                                            icon = Icons.Rounded.ViewColumn,
                                            labelRes = DesignSystemR.string.open_in_split_tab,
                                            onClick = { onOpenInSplit(tab.path) },
                                        ),
                                    )
                                    if (onCopyTabPath != null) add(
                                        FolioFabMenuAction(
                                            icon = Icons.Rounded.ContentCopy,
                                            labelRes = DesignSystemR.string.copy_path,
                                            onClick = { onCopyTabPath(tab.path) },
                                        ),
                                    )
                                    if (tabs.size > 1) add(
                                        FolioFabMenuAction(
                                            icon = Icons.Rounded.Close,
                                            labelRes = DesignSystemR.string.close_this_tab,
                                            onClick = { currentOnClose(latestIndex) },
                                        ),
                                    )
                                }
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
            if (showAddButton) {
                IconButton(onClick = onAdd, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(DesignSystemR.string.new_tab),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPillChip(
    tabId: Long,
    label: String,
    selected: Boolean,
    canClose: Boolean,
    isReordering: Boolean,
    getStartPosition: () -> Float,
    modifier: Modifier,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onLongPressMenu: () -> Unit,
    onReorderStart: () -> Unit,
    onReorderDragEnd: (Float) -> Unit,
    onReorderCancel: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val latestGetStart by rememberUpdatedState(getStartPosition)
    val latestOnLongPressMenu by rememberUpdatedState(onLongPressMenu)
    val latestOnReorderStart by rememberUpdatedState(onReorderStart)
    val latestOnReorderDragEnd by rememberUpdatedState(onReorderDragEnd)
    val latestOnReorderCancel by rememberUpdatedState(onReorderCancel)
    val bg by animateColorAsState(
        targetValue = if (selected) colorScheme.secondaryContainer else colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
        animationSpec = MonoSpring.noBounceColor(),
        label = "tabPillBg_$tabId",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
        animationSpec = MonoSpring.noBounceColor(),
        label = "tabPillFg_$tabId",
    )

    Surface(
        color = bg,
        contentColor = fg,
        shape = CircleShape,
        shadowElevation = if (selected) 3.dp else 1.dp,
        border = if (!selected) BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)) else null,
        modifier = modifier
            .height(34.dp)
            .widthIn(min = 88.dp, max = 168.dp)
            .alpha(if (isReordering) 0.6f else 1f)
            .clip(CircleShape)
            .clickable(onClick = onSelect)
            .pointerInput(tabId) {
                val touchSlop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                    longPress.consume()
                    var dragging = false
                    var finished = false
                    var accumulatedDx = 0f
                    var position = latestGetStart()
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == longPress.id } ?: break
                            if (change.changedToUpIgnoreConsumed()) {
                                change.consume()
                                finished = true
                                if (dragging) latestOnReorderDragEnd(position) else latestOnLongPressMenu()
                                break
                            }
                            if (!change.pressed) break
                            val dx = change.positionChange().x
                            accumulatedDx += dx
                            position += dx
                            if (!dragging && abs(accumulatedDx) > touchSlop) {
                                dragging = true
                                latestOnReorderStart()
                            }
                            change.consume()
                        }
                    } finally {
                        if (dragging && !finished) latestOnReorderCancel()
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = if (canClose) 6.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (canClose) {
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).clickable(onClick = onClose),
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
}
