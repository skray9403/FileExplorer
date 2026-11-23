package com.explorer.fileexplorer.core.designsystem

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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

/**
 * Hoisted state controller bridging active screens (e.g. BrowserScreen) to the single
 * persistent floating [FolioMorphingBottomBar] mounted above the NavHost.
 */
@Stable
class FolioDockController {
    private var activeOwner: String? = null

    var scrollMorphState by mutableStateOf<FolioBottomBarMode?>(null)
    var showScrollToTop by mutableStateOf(false)
    var onScrollToTop by mutableStateOf<() -> Unit>({})

    var selectionMode by mutableStateOf(false)
    var selectionActions by mutableStateOf(FolioSelectionDockActions())

    var canPaste by mutableStateOf(false)
    var onPaste by mutableStateOf<() -> Unit>({})

    var canCreatePrimaryAction by mutableStateOf(true)
    var onPrimaryAction by mutableStateOf<() -> Unit>({})

    val resolvedMode: FolioBottomBarMode
        get() = when {
            selectionMode -> FolioBottomBarMode.SELECTION_ACTIONS
            scrollMorphState != null -> scrollMorphState!!
            else -> FolioBottomBarMode.DOCK_TABS
        }

    fun claimOwnership(owner: String) {
        activeOwner = owner
    }

    fun onFastScrollDown() {
        if (!selectionMode) {
            scrollMorphState = FolioBottomBarMode.SCROLL_ACTION
        }
    }

    fun onFastScrollUp() {
        if (!selectionMode) {
            scrollMorphState = FolioBottomBarMode.SCROLL_SEARCH
        }
    }

    fun resetScrollMorph() {
        scrollMorphState = null
    }

    fun clearScreenBindings(owner: String? = null) {
        if (owner != null && activeOwner != owner) return
        activeOwner = null
        scrollMorphState = null
        showScrollToTop = false
        onScrollToTop = {}
        selectionMode = false
        selectionActions = FolioSelectionDockActions()
        canPaste = false
        onPaste = {}
        canCreatePrimaryAction = false
        onPrimaryAction = {}
    }
}

val LocalFolioDockController = compositionLocalOf<FolioDockController> {
    FolioDockController()
}
