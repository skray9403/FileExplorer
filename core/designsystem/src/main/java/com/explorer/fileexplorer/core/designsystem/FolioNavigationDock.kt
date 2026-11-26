package com.explorer.fileexplorer.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FolioNavigationDock(
    selectedDestination: String,
    controller: FolioDockController,
    onBrowse: () -> Unit,
    onCollections: () -> Unit,
    onTools: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FolioMorphingBottomBar(
        selectedDestination = selectedDestination,
        onDestinationSelected = { destId ->
            when (destId) {
                "browser" -> onBrowse()
                "collections" -> onCollections()
                "tools" -> onTools()
            }
        },
        controller = controller,
        onSearchClick = onSearch,
        modifier = modifier,
    )
}
