package com.explorer.fileexplorer.feature.browser

import android.content.Context
import android.content.Intent
import androidx.activity.compose.PredictiveBackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.explorer.fileexplorer.core.designsystem.FolioFabMenuAction
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.ui.FileListItem
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CollectionsScreen(
    onNavigateBack: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val selected = state.selectedCategory
    val dockController = LocalFolioDockController.current
    val listState = rememberLazyListState()

    val scrollThresholdPx = with(density) { 160.dp.toPx() }
    val showScrollToTop by remember(listState, scrollThresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThresholdPx
        }
    }

    LaunchedEffect(selected, showScrollToTop) {
        dockController.claimOwnership("collections")
        dockController.contextResetKey = "collections|${selected?.name ?: "root"}"
        dockController.isSubfolder = selected != null
        dockController.onNavigateUp = { viewModel.closeCategory() }
        dockController.onNavigateToRoot = { viewModel.closeCategory() }
        dockController.showScrollToTop = showScrollToTop
        dockController.onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } }
        dockController.sortPrimaryItems = emptyList()
        dockController.fabMenuItems = emptyList()
    }

    DisposableEffect(Unit) {
        onDispose { dockController.clearScreenBindings("collections") }
    }

    var backPreviewProgress by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler(enabled = selected != null) { progress ->
        try {
            progress.collect { backPreviewProgress = it.progress }
            backPreviewProgress = 0f
            viewModel.closeCategory()
        } catch (_: CancellationException) {
            backPreviewProgress = 0f
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = statusBarTop + 60.dp
    val floatingBubbleColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (selected == null) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = headerHeight + 8.dp,
                    bottom = 144.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.error?.let { message ->
                    item(key = "collections_error") {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
                if (state.isLoading && state.summaries.all { it.itemCount == 0 }) {
                    item(key = "collections_loading") {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
                item(key = "collections_cards") {
                    FolioSegmentedColumn {
                        state.summaries.forEachIndexed { idx, summary ->
                            FolioSegmentedCard(
                                index = idx,
                                count = state.summaries.size,
                                onClick = { viewModel.open(summary.category) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = collectionIcon(summary.category),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.category.title,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = stringResource(
                                                DesignSystemR.string.collection_item_summary,
                                                summary.itemCount,
                                                formatCollectionBytes(summary.totalBytes),
                                            ),
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
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = backPreviewProgress.coerceIn(0f, 1f)
                        val scale = 1f - (0.05f * p)
                        scaleX = scale
                        scaleY = scale
                    },
                contentPadding = PaddingValues(
                    top = headerHeight + 8.dp,
                    bottom = 144.dp,
                    start = 12.dp,
                    end = 12.dp,
                ),
            ) {
                if (state.isLoading && state.files.isEmpty()) {
                    item(key = "cat_loading") {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                } else if (state.files.isEmpty()) {
                    item(key = "cat_empty") {
                        Text(
                            text = stringResource(DesignSystemR.string.no_indexed_files),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                } else {
                    items(state.files, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            onClick = { openCollectionFile(context, item) },
                            onLongClick = {},
                        )
                        if (item != state.files.lastOrNull()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            )
                        }
                    }
                }
            }
        }

        FolioTopScrim(
            height = headerHeight + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Floating Gallery-Style Top Header (no back arrow at top level)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            if (selected != null) {
                Surface(
                    onClick = viewModel::closeCategory,
                    shape = CircleShape,
                    color = floatingBubbleColor,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(DesignSystemR.string.back),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = floatingBubbleColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.height(44.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    Text(
                        text = selected?.title ?: stringResource(DesignSystemR.string.collections),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Surface(
                onClick = viewModel::refresh,
                shape = CircleShape,
                color = floatingBubbleColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(DesignSystemR.string.refresh),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private fun openCollectionFile(context: Context, item: FileItem) {
    runCatching {
        val uri = item.uri ?: FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(item.path),
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
private fun collectionIcon(category: CollectionCategory) = when (category) {
    CollectionCategory.PHOTOS -> Icons.Rounded.Image
    CollectionCategory.VIDEOS -> Icons.Rounded.Movie
    CollectionCategory.MUSIC -> Icons.Rounded.AudioFile
    CollectionCategory.DOCUMENTS -> Icons.Rounded.Description
    CollectionCategory.DOWNLOADS -> Icons.Rounded.Download
    CollectionCategory.APKS -> Icons.Rounded.Android
}

private fun formatCollectionBytes(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}
