package com.explorer.fileexplorer.preview

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import kotlinx.coroutines.launch

@Composable
fun PreviewCollectionsScreen(
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dockController = LocalFolioDockController.current
    val listState = rememberLazyListState()

    var selectedCategory by remember { mutableStateOf<PreviewCategory?>(null) }
    var inlineSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val scrollThresholdPx = with(density) { 160.dp.toPx() }
    val showScrollToTop by remember(listState, scrollThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThresholdPx
        }
    }

    BackHandler(enabled = selectedCategory != null || inlineSearchOpen) {
        when {
            inlineSearchOpen -> {
                inlineSearchOpen = false
                searchQuery = ""
            }
            selectedCategory != null -> {
                selectedCategory = null
                searchQuery = ""
            }
        }
    }

    LaunchedEffect(selectedCategory, showScrollToTop, inlineSearchOpen, searchQuery) {
        dockController.claimOwnership("collections")
        dockController.contextResetKey = "collections|${selectedCategory?.id ?: "root"}"
        dockController.isSubfolder = selectedCategory != null
        dockController.onNavigateUp = {
            selectedCategory = null
            inlineSearchOpen = false
            searchQuery = ""
        }
        dockController.onNavigateToRoot = {
            selectedCategory = null
            inlineSearchOpen = false
            searchQuery = ""
        }
        dockController.inlineSearchActive = inlineSearchOpen
        dockController.inlineSearchQuery = searchQuery
        dockController.onInlineSearchOpen = if (selectedCategory != null) {
            { inlineSearchOpen = true }
        } else {
            null
        }
        dockController.onInlineSearchClose = {
            inlineSearchOpen = false
            searchQuery = ""
        }
        dockController.onInlineSearchQueryChange = { searchQuery = it }
        dockController.showScrollToTop = showScrollToTop
        dockController.onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } }
        dockController.sortPrimaryItems = emptyList()
        dockController.sortSecondaryItems = emptyList()
        dockController.fabMenuItems = listOf(
            FolioFabMenuAction(
                icon = Icons.Rounded.Refresh,
                labelRes = DesignSystemR.string.refresh,
                onClick = {
                    Toast.makeText(
                        context,
                        context.getString(DesignSystemR.string.refresh),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            ),
        )
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings("collections") }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val category = selectedCategory
    val displayedFiles = remember(category, searchQuery) {
        val base = category?.files.orEmpty()
        if (searchQuery.isBlank()) base
        else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 12.dp,
                bottom = 128.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "collections_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (category != null) stringResource(category.labelRes) else stringResource(DesignSystemR.string.collections),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(DesignSystemR.string.tools_and_settings),
                        )
                    }
                }
            }

            if (category == null) {
                item(key = "collections_cards") {
                    FolioSegmentedColumn {
                        PreviewMockData.categories.forEachIndexed { idx, cat ->
                            FolioSegmentedCard(
                                index = idx,
                                count = PreviewMockData.categories.size,
                                onClick = { selectedCategory = cat },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(42.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(cat.icon, null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(cat.labelRes),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = pluralStringResource(DesignSystemR.plurals.items_count, cat.count, cat.count),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (displayedFiles.isEmpty()) {
                item(key = "collections_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(DesignSystemR.string.empty_directory),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(displayedFiles, key = { it.path }) { item ->
                    PreviewFileListRow(
                        item = item,
                        selected = false,
                        visibleColumns = setOf(PreviewFileColumn.SIZE, PreviewFileColumn.DATE),
                        onClick = {
                            Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
                        },
                        onLongClick = {},
                    )
                }
            }
        }

        FolioTopScrim(
            height = statusBarTop + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
