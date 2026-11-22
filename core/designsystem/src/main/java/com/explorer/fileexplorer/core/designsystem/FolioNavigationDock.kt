package com.explorer.fileexplorer.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Detached Monomail-style navigation dock powered by [FolioMorphingBottomBar].
 * Maintained for high-level top navigation integration across the app.
 */
@Composable
fun FolioNavigationDock(
    selectedDestination: String,
    onBrowse: () -> Unit,
    onTransfers: () -> Unit,
    onNetwork: () -> Unit,
    onCollections: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    mode: FolioBottomBarMode = FolioBottomBarMode.DOCK_TABS,
    showScrollToTop: Boolean = false,
    onScrollToTop: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onCutClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    FolioMorphingBottomBar(
        selectedDestination = selectedDestination,
        onDestinationSelected = { destId ->
            when (destId) {
                "browser" -> onBrowse()
                "transfers" -> onTransfers()
                "network" -> onNetwork()
                "collections" -> onCollections()
            }
        },
        mode = mode,
        showScrollToTop = showScrollToTop,
        onScrollToTop = onScrollToTop,
        onSearchClick = onSearch,
        onActionClick = onActionClick,
        onCutClick = onCutClick,
        onCopyClick = onCopyClick,
        onDeleteClick = onDeleteClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
    )
}
