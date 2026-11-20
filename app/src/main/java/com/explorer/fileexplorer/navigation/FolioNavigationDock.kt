package com.explorer.fileexplorer.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR

private data class FolioDockItem(val label: String, val icon: ImageVector)

/** Persistent, animated floating navigation dock based on Monomail's dock pattern. */
@Composable
fun FolioNavigationDock(
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        FolioDockItem(stringResource(DesignSystemR.string.browse), Icons.Filled.FolderOpen),
        FolioDockItem(stringResource(DesignSystemR.string.network), Icons.Filled.Lan),
        FolioDockItem(stringResource(DesignSystemR.string.collections), Icons.Filled.Bookmarks),
        FolioDockItem(stringResource(DesignSystemR.string.transfer_queue), Icons.Filled.SyncAlt),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .graphicsLayer {
                shape = CircleShape
                clip = false
                shadowElevation = 8.dp.toPx()
                ambientShadowColor = Color.Black.copy(alpha = 0.09f)
                spotShadowColor = Color.Black.copy(alpha = 0.15f)
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 7.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    label = "folioDockContainer$index",
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    label = "folioDockContent$index",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(containerColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(bounded = true),
                            role = Role.Tab,
                            onClick = { onDestinationSelected(index) },
                        )
                        .semantics { selected = isSelected }
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(item.icon, contentDescription = item.label, tint = contentColor, modifier = Modifier.size(21.dp))
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(7.dp))
                                Text(item.label, style = MaterialTheme.typography.labelLarge, color = contentColor, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
