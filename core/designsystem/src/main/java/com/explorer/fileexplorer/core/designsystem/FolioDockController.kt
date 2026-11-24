package com.explorer.fileexplorer.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

data class FolioFabMenuAction(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val trailingIcon: ImageVector? = null,
    val onClick: () -> Unit,
)

data class FolioSelectionDockActions(
    val selectedCount: Int = 0,
    val insideArchive: Boolean = false,
    val canCopy: Boolean = true,
    val canMove: Boolean = true,
    val canTrash: Boolean = true,
    val canDelete: Boolean = true,
    val canExtract: Boolean = false,
    val onCut: () -> Unit = {},
    val onCopy: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onPermanentDelete: () -> Unit = {},
    val onExtract: () -> Unit = {},
    val onMore: () -> Unit = {},
)

@Stable
class FolioDockController {
    private var activeOwner: String? = null

    var contextResetKey by mutableStateOf("")

    var showScrollToTop by mutableStateOf(false)
    var onScrollToTop by mutableStateOf<() -> Unit>({})

    var selectionMode by mutableStateOf(false)
    var selectionActions by mutableStateOf(FolioSelectionDockActions())
    var selectionPrimaryItems by mutableStateOf<List<FolioFabMenuAction>>(emptyList())
    var selectionSecondaryItems by mutableStateOf<List<FolioFabMenuAction>>(emptyList())

    // Contextual Sort / View switchable popup in bottom bar (replaces Tools tab)
    var sortPrimaryItems by mutableStateOf<List<FolioFabMenuAction>>(emptyList())
    var sortSecondaryItems by mutableStateOf<List<FolioFabMenuAction>>(emptyList())

    var isSubfolder by mutableStateOf(false)
    var onNavigateUp by mutableStateOf<() -> Unit>({})
    var onNavigateToRoot by mutableStateOf<() -> Unit>({})

    var inlineSearchActive by mutableStateOf(false)
    var inlineSearchQuery by mutableStateOf("")
    var inlineSearchRecursive by mutableStateOf(false)
    var onInlineSearchOpen by mutableStateOf<(() -> Unit)?>(null)
    var onInlineSearchQueryChange by mutableStateOf<(String) -> Unit>({})
    var onInlineSearchScopeToggle by mutableStateOf<() -> Unit>({})
    var onInlineSearchClose by mutableStateOf<() -> Unit>({})
    var onOpenFullSearch by mutableStateOf<() -> Unit>({})

    var canPaste by mutableStateOf(false)
    var onPaste by mutableStateOf<() -> Unit>({})
    var canCreatePrimaryAction by mutableStateOf(true)
    var onPrimaryAction by mutableStateOf<() -> Unit>({})

    var taskBadgeCount by mutableStateOf(0)
    var taskProgress by mutableStateOf<Float?>(null)
    var taskHasConflict by mutableStateOf(false)
    var onTaskPillClick by mutableStateOf<(() -> Unit)?>(null)

    var onExecuteClipboardTask by mutableStateOf<((Long) -> Unit)?>(null)
    var fabMenuItems by mutableStateOf<List<FolioFabMenuAction>>(emptyList())
    var isDockMinimizedByScroll by mutableStateOf(false)

    fun onFastScrollDown() { isDockMinimizedByScroll = true }
    fun onFastScrollUp() { isDockMinimizedByScroll = false }
    fun resetScrollMorph() { isDockMinimizedByScroll = false }

    fun claimOwnership(owner: String) {
        activeOwner = owner
    }

    fun clearScreenBindings(owner: String? = null) {
        if (owner != null && activeOwner != owner) return
        activeOwner = null
        showScrollToTop = false
        onScrollToTop = {}
        selectionMode = false
        selectionActions = FolioSelectionDockActions()
        selectionPrimaryItems = emptyList()
        selectionSecondaryItems = emptyList()
        sortPrimaryItems = emptyList()
        sortSecondaryItems = emptyList()
        canPaste = false
        onPaste = {}
        canCreatePrimaryAction = true
        onPrimaryAction = {}
        isSubfolder = false
        onNavigateUp = {}
        onNavigateToRoot = {}
        inlineSearchActive = false
        inlineSearchQuery = ""
        inlineSearchRecursive = false
        onInlineSearchOpen = null
        onInlineSearchQueryChange = {}
        onInlineSearchScopeToggle = {}
        onInlineSearchClose = {}
        onOpenFullSearch = {}
        onExecuteClipboardTask = null
        fabMenuItems = emptyList()
        isDockMinimizedByScroll = false
    }
}

val LocalFolioDockController = compositionLocalOf<FolioDockController> {
    FolioDockController()
}
