package com.explorer.fileexplorer.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Monomail-inspired detached navigation dock.
 *
 * The navigation surface and primary search action remain visually separate.
 * File browsing is the persistent home destination; secondary destinations
 * keep their existing navigation callbacks and state owners.
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
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier.height(72.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavigationBarItem(
                    selected = selectedDestination == "browser",
                    onClick = onBrowse,
                    icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                    label = { Text(stringResource(R.string.browse)) },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
                NavigationBarItem(
                    selected = selectedDestination == "transfers",
                    onClick = onTransfers,
                    icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
                    label = { Text(stringResource(R.string.transfer_queue)) },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = selectedDestination == "network",
                    onClick = onNetwork,
                    icon = { Icon(Icons.Filled.Lan, contentDescription = null) },
                    label = { Text(stringResource(R.string.network)) },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = selectedDestination == "collections",
                    onClick = onCollections,
                    icon = { Icon(Icons.Filled.Collections, contentDescription = null) },
                    label = { Text(stringResource(R.string.collections)) },
                    alwaysShowLabel = false,
                )
            }
        }

        FilledIconButton(
            onClick = onSearch,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search),
            )
        }
    }
}
