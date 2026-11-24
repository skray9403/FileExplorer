package com.explorer.fileexplorer.feature.browser

import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import com.explorer.fileexplorer.core.designsystem.FolioBottomScrim
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBarDefaults
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBottomBar
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenHeadline
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import kotlin.coroutines.cancellation.CancellationException

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.explorer.fileexplorer.core.data.LocalTrashManager
import com.explorer.fileexplorer.core.data.TrashItem
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.storage.StorageVolumeHelper
import com.explorer.fileexplorer.core.ui.FileIcon
import com.explorer.fileexplorer.feature.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TrashUiState(
    val items: List<TrashItem> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val ttlDays: Int = LocalTrashManager.DEFAULT_TTL_DAYS,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val selectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectedCount: Int get() = selectedIds.size
}

sealed interface TrashEvent {
    data class Toast(val message: String) : TrashEvent
}

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashManager: LocalTrashManager,
    private val storageVolumeHelper: StorageVolumeHelper,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TrashUiState())
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TrashEvent>()
    val events: SharedFlow<TrashEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings
                .map { it.trashTtlDays }
                .distinctUntilChanged()
                .collect { ttlDays ->
                    _state.update { it.copy(ttlDays = ttlDays) }
                    purgeAndLoad(ttlDays)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch { purgeAndLoad(_state.value.ttlDays) }
    }

    fun toggleSelection(id: String) {
        _state.update { state ->
            val selected = state.selectedIds.toMutableSet()
            if (id in selected) selected.remove(id) else selected.add(id)
            state.copy(selectedIds = selected)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun restoreSelected() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val result = trashManager.restore(ids, trashVolumeRoots())
            if (result.isSuccess) {
                _events.emit(TrashEvent.Toast("${result.getOrThrow()} items restored"))
                clearSelection()
                loadTrash()
            } else {
                _events.emit(TrashEvent.Toast("Restore failed: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    fun deleteSelectedPermanently() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val result = trashManager.permanentlyDelete(ids, trashVolumeRoots())
            if (result.isSuccess) {
                _events.emit(TrashEvent.Toast("${result.getOrThrow()} items deleted permanently"))
                clearSelection()
                loadTrash()
            } else {
                _events.emit(TrashEvent.Toast("Delete failed: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val result = trashManager.emptyTrash(trashVolumeRoots())
            if (result.isSuccess) {
                _events.emit(TrashEvent.Toast("${result.getOrThrow()} items deleted permanently"))
                clearSelection()
                loadTrash()
            } else {
                _events.emit(TrashEvent.Toast("Empty Trash failed: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private suspend fun purgeAndLoad(ttlDays: Int) {
        val roots = trashVolumeRoots()
        val result = trashManager.purgeExpired(ttlDays, roots)
        if (result.isSuccess) {
            val count = result.getOrThrow()
            if (count > 0) _events.emit(TrashEvent.Toast("$count expired trash items purged"))
        } else {
            _events.emit(TrashEvent.Toast("Trash purge failed: ${result.exceptionOrNull()?.message}"))
        }
        loadTrash(roots)
    }

    private suspend fun loadTrash(roots: List<String> = trashVolumeRoots()) {
        _state.update { it.copy(isLoading = true, error = null) }
        trashManager.listTrashItems(roots)
            .onSuccess { items ->
                _state.update { state ->
                    state.copy(
                        items = items,
                        selectedIds = state.selectedIds.intersect(items.map { item -> item.id }.toSet()),
                        isLoading = false,
                    )
                }
            }
            .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Unable to load Trash") } }
    }

    private fun trashVolumeRoots(): List<String> {
        return (storageVolumeHelper.getStorageVolumes().map { it.path } +
            Environment.getExternalStorageDirectory().absolutePath)
            .distinct()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrashEvent.Toast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect {}
            if (state.selectionMode) viewModel.clearSelection() else onNavigateBack()
        } catch (_: CancellationException) {}
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = 128.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "trash_headline") {
                FolioSubScreenHeadline(
                    title = if (state.selectionMode) {
                        stringResource(DesignSystemR.string.selected_count, state.selectedCount)
                    } else {
                        stringResource(DesignSystemR.string.trash)
                    }
                )
            }

            item(key = "trash_info") {
                ListItem(
                    headlineContent = { Text(stringResource(DesignSystemR.string.auto_purge_after_days, state.ttlDays)) },
                    supportingContent = { Text(stringResource(DesignSystemR.string.change_retention_settings)) },
                )
            }

            if (state.isLoading) {
                item(key = "trash_loading") {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            when {
                state.items.isEmpty() && !state.isLoading -> item(key = "trash_empty") { EmptyTrashState(state.error) }
                else -> {
                    items(items = state.items, key = { it.id }) { item ->
                        TrashListItem(
                            item = item,
                            selected = item.id in state.selectedIds,
                            selectionMode = state.selectionMode,
                            onClick = { viewModel.toggleSelection(item.id) },
                            onLongClick = { viewModel.toggleSelection(item.id) },
                        )
                    }
                }
            }
        }

        FolioTopScrim(
            height = statusBarTop + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        FolioSubScreenBottomBar(
            title = if (state.selectionMode) {
                stringResource(DesignSystemR.string.selected_count, state.selectedCount)
            } else {
                stringResource(DesignSystemR.string.trash)
            },
            onNavigateBack = if (state.selectionMode) viewModel::clearSelection else onNavigateBack,
            onFabClick = if (state.selectionMode) viewModel::restoreSelected else null,
            fabIcon = Icons.Filled.RestoreFromTrash,
            fabContentDescription = stringResource(DesignSystemR.string.restore),
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        FolioBottomScrim(
            height = FolioSubScreenBarDefaults.BottomBarHeight + 16.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EmptyTrashState(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.FolderOff,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                error ?: stringResource(DesignSystemR.string.trash_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrashListItem(
    item: TrashItem,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val fileItem = FileItem(
        name = item.name,
        path = item.trashedPath,
        size = item.size,
        lastModified = item.deletedAt,
        isDirectory = item.isDirectory,
    )
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface

    Surface(color = background, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                if (selectionMode && selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = stringResource(DesignSystemR.string.selected),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    FileIcon(item = fileItem, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.originalPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatTrashDate(item.deletedAt)} - ${trashItemSize(item)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val trashDateFormatter = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())

private fun formatTrashDate(millis: Long): String {
    return "Deleted ${trashDateFormatter.format(Date(millis))}"
}

private fun trashItemSize(item: TrashItem): String {
    if (item.isDirectory) return "Folder"
    return when {
        item.size < 1024 -> "${item.size} B"
        item.size < 1024 * 1024 -> "%.1f KB".format(item.size / 1024.0)
        item.size < 1024 * 1024 * 1024 -> "%.1f MB".format(item.size / (1024.0 * 1024.0))
        else -> "%.2f GB".format(item.size / (1024.0 * 1024.0 * 1024.0))
    }
}
