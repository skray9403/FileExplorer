package com.explorer.fileexplorer.feature.browser

import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorer.fileexplorer.core.data.ArchiveFormat
import com.explorer.fileexplorer.core.data.ArchiveHelper
import com.explorer.fileexplorer.core.data.BatchRenameEngine
import com.explorer.fileexplorer.core.data.BatchRenameOptions
import com.explorer.fileexplorer.core.data.DeleteCapabilities
import com.explorer.fileexplorer.core.data.FileRepository
import com.explorer.fileexplorer.core.data.FileRepositoryFactory
import com.explorer.fileexplorer.core.data.LocalFileRepository
import com.explorer.fileexplorer.core.data.LocalTrashManager
import com.explorer.fileexplorer.core.data.RootFileRepository
import com.explorer.fileexplorer.core.data.SecureDelete
import com.explorer.fileexplorer.core.data.SecureDeleteCapability
import com.explorer.fileexplorer.core.data.TagRepository
import com.explorer.fileexplorer.core.data.UsbDeviceInfo
import com.explorer.fileexplorer.core.data.UsbPathCodec
import com.explorer.fileexplorer.core.data.UsbStorageManager
import com.explorer.fileexplorer.core.data.UsbStorageRoot
import com.explorer.fileexplorer.feature.security.FileEncryptionManager
import com.explorer.fileexplorer.feature.security.FileEncryptionBatchResult
import com.explorer.fileexplorer.feature.security.VaultManager
import com.explorer.fileexplorer.feature.security.SecurityRepository
import com.explorer.fileexplorer.feature.security.IntegrityRepository
import com.explorer.fileexplorer.feature.transfer.TransferQueueManager
import com.explorer.fileexplorer.core.database.BookmarkDao
import com.explorer.fileexplorer.core.database.BookmarkEntity
import com.explorer.fileexplorer.core.database.DirectoryViewPreferenceCodec
import com.explorer.fileexplorer.core.database.DirectoryViewPreferenceDao
import com.explorer.fileexplorer.core.database.RecentFileDao
import com.explorer.fileexplorer.core.database.RecentFileEntity
import com.explorer.fileexplorer.core.database.RecentLocationDao
import com.explorer.fileexplorer.core.database.RecentLocationEntity
import com.explorer.fileexplorer.core.database.SavedSearchDao
import com.explorer.fileexplorer.core.database.SavedSearchEntity
import com.explorer.fileexplorer.core.database.TagEntity
import com.explorer.fileexplorer.core.model.*
import com.explorer.fileexplorer.core.storage.RootHelper
import com.explorer.fileexplorer.core.storage.RootState
import com.explorer.fileexplorer.core.storage.StorageVolumeHelper
import com.explorer.fileexplorer.feature.settings.SettingsRepository
import com.explorer.fileexplorer.feature.settings.SwipeAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import java.util.UUID
import javax.inject.Inject

data class BrowserTab(
    val id: Long,
    val path: String,
    val pathHistory: List<String> = listOf(path),
    val historyIndex: Int = 0,
    val insideArchive: Boolean = false,
    val archivePath: String? = null,
    val archiveInternalPath: String = "",
)

data class PermanentDeleteConfirmation(
    val paths: List<String>,
    val capabilities: DeleteCapabilities,
    val secureDeleteEnabled: Boolean,
)

data class BrowserUiState(
    val currentPath: String = Environment.getExternalStorageDirectory().absolutePath,
    val capabilityMatrix: RepositoryCapabilityMatrix = RepositoryCapabilityMatrix.unavailable(),
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortOrder: SortOrder = SortOrder(),
    val viewMode: ViewMode = ViewMode.LIST,
    val visibleColumns: Set<FileColumn> = FileColumn.DEFAULT_VISIBLE_COLUMNS,
    val showHidden: Boolean = false,
    val selectedItems: Set<String> = emptySet(),
    val clipboard: ClipboardContent = ClipboardContent(),
    val volumes: List<StorageVolume> = emptyList(),
    val usbDevices: List<UsbDeviceInfo> = emptyList(),
    val usbRoots: List<UsbStorageRoot> = emptyList(),
    val isRefreshingUsb: Boolean = false,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val recentLocations: List<RecentLocationEntity> = emptyList(),
    val savedSearches: List<SavedSearchEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val showTagDialog: Boolean = false,
    val tagDialogSelectedTags: Set<String> = emptySet(),
    val pathHistory: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val propertiesItem: FileItem? = null,
    val renameItem: FileItem? = null,
    val showBatchRenameDialog: Boolean = false,
    val showNewFolderDialog: Boolean = false,
    val rootState: RootState = RootState.UNKNOWN,
    val rootEnabled: Boolean = false,
    val selinuxContext: String? = null,
    val insideArchive: Boolean = false,
    val archivePath: String? = null,
    val archiveInternalPath: String = "",
    val showCompressDialog: Boolean = false,
    val deleteConfirmationItem: FileItem? = null,
    val permanentDeleteConfirmation: PermanentDeleteConfirmation? = null,
    val showExtractDialog: Boolean = false,
    val compactDensity: Boolean = false,
    val showDirectorySizes: Boolean = false,
    val directorySizes: Map<String, Long> = emptyMap(),
    val directorySizesLoading: Set<String> = emptySet(),
    val directorySizesUnavailable: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
    val swipeLeftAction: SwipeAction = SwipeAction.NONE,
    val swipeRightAction: SwipeAction = SwipeAction.NONE,
    val dualPaneEnabled: Boolean = false,
    val secondaryPath: String = Environment.getExternalStorageDirectory().absolutePath,
    val secondaryCapabilityMatrix: RepositoryCapabilityMatrix = RepositoryCapabilityMatrix.unavailable(),
    val secondaryFiles: List<FileItem> = emptyList(),
    val secondaryIsLoading: Boolean = false,
    val secondaryError: String? = null,
    val secondaryDirectorySizes: Map<String, Long> = emptyMap(),
    val secondaryDirectorySizesLoading: Set<String> = emptySet(),
    val secondaryDirectorySizesUnavailable: Set<String> = emptySet(),
    val secondarySelectedItems: Set<String> = emptySet(),
    val pendingDrop: PendingDrop? = null,
    val clipboardQueue: List<QueuedClipboardTask> = emptyList(),
    val tabFilesCache: Map<Long, List<FileItem>> = emptyMap(),
    val inlineSearchActive: Boolean = false,
    val inlineSearchQuery: String = "",
    val inlineSearchRecursive: Boolean = false,
    val inlineSearchResults: List<FileItem> = emptyList(),
    val isInlineSearching: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val tabs: List<BrowserTab> = listOf(
        BrowserTab(1L, Environment.getExternalStorageDirectory().absolutePath),
    ),
    val selectedTabIndex: Int = 0,
    val secondaryTabs: List<BrowserTab> = listOf(
        BrowserTab(2L, Environment.getExternalStorageDirectory().absolutePath),
    ),
    val secondarySelectedTabIndex: Int = 0,
) {
    val selectionMode: Boolean get() = selectedItems.isNotEmpty()
    val selectedCount: Int get() = selectedItems.size
    val canGoBack: Boolean get() = historyIndex > 0 || insideArchive
    val canGoForward: Boolean get() = historyIndex < pathHistory.lastIndex
    val canPaste: Boolean get() = (clipboardQueue.isNotEmpty() || clipboard.items.isNotEmpty()) && !insideArchive
    val isAtVolumeRoot: Boolean get() {
        if (insideArchive) return false
        val normalized = currentPath.trimEnd('/').ifBlank { "/" }
        if (normalized == "/") return true
        if (normalized == Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')) return true
        if (volumes.any { it.path.trimEnd('/') == normalized }) return true
        if (usbRoots.any { it.path.trimEnd('/') == normalized }) return true
        return false
    }
    val isRootPath: Boolean get() = currentPath.let { p ->
        listOf("/data", "/system", "/vendor", "/product", "/efs").any { p.startsWith(it) }
    }
    val displayedFiles: List<FileItem> get() = if (inlineSearchActive && inlineSearchQuery.isNotBlank()) inlineSearchResults else files
}

enum class BrowserPane {
    PRIMARY, SECONDARY,
}

data class PendingDrop(
    val items: List<FileItem>,
    val destinationPath: String,
    val sourcePane: BrowserPane,
    val destinationPane: BrowserPane,
)

sealed interface BrowserEvent {
    data class Toast(val message: String) : BrowserEvent
    data class OpenFile(val item: FileItem) : BrowserEvent
    data class ShareFiles(val items: List<FileItem>) : BrowserEvent
    data class SendNearbyFiles(val items: List<FileItem>) : BrowserEvent
    data class RequestDecrypt(val paths: List<String>) : BrowserEvent
    data class RequestVaultAdd(val paths: List<String>) : BrowserEvent
}

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val repoFactory: FileRepositoryFactory,
    private val rootHelper: RootHelper,
    private val rootRepo: RootFileRepository,
    private val archiveHelper: ArchiveHelper,
    private val storageVolumeHelper: StorageVolumeHelper,
    private val usbStorageManager: UsbStorageManager,
    private val trashManager: LocalTrashManager,
    private val settingsRepository: SettingsRepository,
    private val securityRepository: SecurityRepository,
    private val fileEncryptionManager: FileEncryptionManager,
    private val vaultManager: VaultManager,
    private val integrityRepository: IntegrityRepository,
    private val tagRepository: TagRepository,
    private val transferQueueManager: TransferQueueManager,
    private val clipboardQueueRepository: com.explorer.fileexplorer.core.data.ClipboardQueueRepository,
    private val diagnosticLog: com.explorer.fileexplorer.core.data.DiagnosticLog,
    private val castMediaSender: CastMediaSender,
    private val bookmarkDao: BookmarkDao,
    private val savedSearchDao: SavedSearchDao,
    private val recentFileDao: RecentFileDao,
    private val recentLocationDao: RecentLocationDao,
    private val directoryViewPreferenceDao: DirectoryViewPreferenceDao,
) : ViewModel() {

    private var nextTabId = 3L
    private var primaryLoadJob: Job? = null
    private var secondaryLoadJob: Job? = null
    private var latestSettings: com.explorer.fileexplorer.feature.settings.SettingsState =
        com.explorer.fileexplorer.feature.settings.SettingsState()

    private val _state = MutableStateFlow(BrowserUiState())
    val state: StateFlow<BrowserUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BrowserEvent>()
    val events: SharedFlow<BrowserEvent> = _events.asSharedFlow()

    private val directorySizeCache = DirectorySizeCache()
    private var directorySizeJob: Job? = null
    private var secondaryDirectorySizeJob: Job? = null

    init {
        loadVolumes()
        refreshUsbStorage()
        observeBookmarks()
        observeRecentLocations()
        observeSavedSearches()
        observeTags()
        observeRootState()
        observeSettings()
        observeClipboardQueue()
        observeCompletedTransfers()
        initializeRoot()
        navigateTo(_state.value.currentPath)
    }

    private fun defaultDirectorySnapshot(): com.explorer.fileexplorer.core.database.DirectoryViewPreferenceSnapshot {
        val s = latestSettings
        return com.explorer.fileexplorer.core.database.DirectoryViewPreferenceSnapshot(
            viewMode = ViewMode.entries.firstOrNull { it.name == s.defaultView } ?: ViewMode.LIST,
            sortOrder = SortOrder(
                field = SortField.entries.firstOrNull { it.name == s.sortField } ?: SortField.NAME,
                direction = SortDirection.entries.firstOrNull { it.name == s.sortDirection } ?: SortDirection.ASCENDING,
                foldersFirst = s.foldersFirst,
            ),
            visibleColumns = FileColumn.DEFAULT_VISIBLE_COLUMNS,
        )
    }

    private suspend fun resolveDirectoryPreferences(path: String) =
        directoryViewPreferenceDao.getByPath(path)
            ?.let(DirectoryViewPreferenceCodec::decode)
            ?: defaultDirectorySnapshot()

    private fun observeClipboardQueue() {
        viewModelScope.launch {
            clipboardQueueRepository.tasks.collect { tasks ->
                val latest = tasks.lastOrNull()
                _state.update {
                    it.copy(
                        clipboardQueue = tasks,
                        clipboard = if (latest != null && !latest.isExtract) {
                            ClipboardContent(latest.items, latest.operation, latest.sourcePath)
                        } else {
                            ClipboardContent()
                        },
                    )
                }
            }
        }
    }

    private fun observeCompletedTransfers() {
        viewModelScope.launch {
            transferQueueManager.completedDestinations.collect { completedDir ->
                val normalized = completedDir.trimEnd('/').ifBlank { "/" }
                val current = _state.value
                if (!current.insideArchive && current.currentPath.trimEnd('/').ifBlank { "/" } == normalized) {
                    refresh()
                }
                if (current.dualPaneEnabled && current.secondaryPath.trimEnd('/').ifBlank { "/" } == normalized) {
                    refreshSecondary()
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { s ->
                val previous = _state.value
                latestSettings = s
                _state.update {
                    it.copy(
                        compactDensity = s.compactDensity,
                        showDirectorySizes = s.showDirectorySizes,
                        confirmDelete = s.confirmDelete,
                        swipeLeftAction = s.swipeLeftAction,
                        swipeRightAction = s.swipeRightAction,
                        showHidden = s.showHidden,
                        sortOrder = it.sortOrder.copy(foldersFirst = s.foldersFirst),
                        files = sortFiles(it.files, it.sortOrder.copy(foldersFirst = s.foldersFirst), s.showHidden),
                        secondaryFiles = sortFiles(it.secondaryFiles, it.sortOrder.copy(foldersFirst = s.foldersFirst), s.showHidden),
                    )
                }
                val current = _state.value
                if (!s.showDirectorySizes) {
                    directorySizeJob?.cancel()
                    secondaryDirectorySizeJob?.cancel()
                    _state.update {
                        it.copy(
                            directorySizes = emptyMap(),
                            directorySizesLoading = emptySet(),
                            directorySizesUnavailable = emptySet(),
                            secondaryDirectorySizes = emptyMap(),
                            secondaryDirectorySizesLoading = emptySet(),
                            secondaryDirectorySizesUnavailable = emptySet(),
                        )
                    }
                } else if (!previous.showDirectorySizes) {
                    if (!current.insideArchive && current.files.isNotEmpty()) {
                        scheduleDirectorySizes(
                            path = current.currentPath,
                            files = current.files,
                            repository = repoFactory.getRepository(current.currentPath),
                            secondary = false,
                        )
                    }
                    if (current.dualPaneEnabled && current.secondaryFiles.isNotEmpty()) {
                        scheduleDirectorySizes(
                            path = current.secondaryPath,
                            files = current.secondaryFiles,
                            repository = repoFactory.getRepository(current.secondaryPath),
                            secondary = true,
                        )
                    }
                }
            }
        }
    }

    private fun initializeRoot() {
        viewModelScope.launch { rootHelper.initialize() }
    }

    private fun observeRootState() {
        viewModelScope.launch {
            rootHelper.rootState.collect { state -> _state.update { it.copy(rootState = state) } }
        }
        viewModelScope.launch {
            rootHelper.rootEnabled.collect { enabled -> _state.update { it.copy(rootEnabled = enabled) } }
        }
    }

    private fun scheduleDirectorySizes(
        path: String,
        files: List<FileItem>,
        repository: FileRepository,
        secondary: Boolean,
    ) {
        val directories = files.filter(FileItem::isDirectory).map(FileItem::path)
        val supportsSize = repository.capabilityMatrix(path).isActionEnabled(RepositoryOperation.SIZE)
        val unavailable = if (supportsSize) emptySet() else directories.toSet()
        val loading = if (_state.value.showDirectorySizes && supportsSize) directories.toSet() else emptySet()

        if (secondary) {
            secondaryDirectorySizeJob?.cancel()
            secondaryDirectorySizeJob = null
            _state.update {
                if (it.secondaryPath != path) it else it.copy(
                    secondaryDirectorySizes = emptyMap(),
                    secondaryDirectorySizesLoading = loading,
                    secondaryDirectorySizesUnavailable = unavailable,
                )
            }
        } else {
            directorySizeJob?.cancel()
            directorySizeJob = null
            _state.update {
                if (it.currentPath != path) it else it.copy(
                    directorySizes = emptyMap(),
                    directorySizesLoading = loading,
                    directorySizesUnavailable = unavailable,
                )
            }
        }

        if (!_state.value.showDirectorySizes || directories.isEmpty() || loading.isEmpty()) return

        val cachePrefix = repository::class.qualifiedName ?: repository::class.java.name
        val job = viewModelScope.launch {
            for (directory in directories) {
                ensureActive()
                val cacheKey = "$cachePrefix|$directory"
                val size = directorySizeCache.get(cacheKey) ?: calculateDirectorySize(repository, directory)?.also {
                    directorySizeCache.put(cacheKey, it)
                }
                if (!isCurrentDirectorySizeRequest(path, secondary)) return@launch
                _state.update { state ->
                    if (secondary) {
                        if (size == null) {
                            state.copy(
                                secondaryDirectorySizesLoading = state.secondaryDirectorySizesLoading - directory,
                                secondaryDirectorySizesUnavailable = state.secondaryDirectorySizesUnavailable + directory,
                            )
                        } else {
                            state.copy(
                                secondaryDirectorySizes = state.secondaryDirectorySizes + (directory to size),
                                secondaryDirectorySizesLoading = state.secondaryDirectorySizesLoading - directory,
                            )
                        }
                    } else if (size == null) {
                        state.copy(
                            directorySizesLoading = state.directorySizesLoading - directory,
                            directorySizesUnavailable = state.directorySizesUnavailable + directory,
                        )
                    } else {
                        state.copy(
                            directorySizes = state.directorySizes + (directory to size),
                            directorySizesLoading = state.directorySizesLoading - directory,
                        )
                    }
                }
            }
        }
        if (secondary) secondaryDirectorySizeJob = job else directorySizeJob = job
    }

    private suspend fun calculateDirectorySize(repository: FileRepository, path: String): Long? = try {
        withContext(Dispatchers.IO) { repository.calculateSize(listOf(path)) }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }

    private fun isCurrentDirectorySizeRequest(path: String, secondary: Boolean): Boolean {
        val state = _state.value
        return state.showDirectorySizes && if (secondary) {
            state.secondaryPath == path
        } else {
            state.currentPath == path && !state.insideArchive
        }
    }

    private fun clearDirectorySizeCacheForRefresh() {
        directorySizeCache.clear()
    }

    fun toggleRootMode() {
        if (rootHelper.isRooted) {
            rootHelper.setRootEnabled(!rootHelper.rootEnabled.value)
            refresh()
        } else {
            viewModelScope.launch { _events.emit(BrowserEvent.Toast("Root not available")) }
        }
    }

    fun navigateTo(path: String) {
        closeInlineSearch()
        primaryLoadJob?.cancel()
        directorySizeJob?.cancel()
        _state.update {
            it.copy(
                directorySizes = emptyMap(),
                directorySizesLoading = emptySet(),
                directorySizesUnavailable = emptySet(),
            )
        }
        primaryLoadJob = viewModelScope.launch {
            val preferences = resolveDirectoryPreferences(path)
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    selectedItems = emptySet(),
                    sortOrder = preferences.sortOrder,
                    viewMode = preferences.viewMode,
                    visibleColumns = preferences.visibleColumns,
                    tabs = updateTabState(it.tabs, it.selectedTabIndex, path, false, null, ""),
                )
            }
            val repo = repoFactory.getRepository(path)
            val capabilityMatrix = repo.capabilityMatrix(path)
            diagnosticLog.logCapabilities(capabilityMatrix)
            _state.update { it.copy(capabilityMatrix = capabilityMatrix) }
            try {
                repo.listFiles(path).collect { files ->
                    val sorted = sortFiles(files, preferences.sortOrder, _state.value.showHidden)
                    val history = _state.value.pathHistory.toMutableList()
                    val idx = _state.value.historyIndex
                    if (idx < history.lastIndex) { while (history.size > idx + 1) history.removeAt(history.lastIndex) }
                    history.add(path)
                    val selinux = if (_state.value.rootEnabled && rootHelper.requiresRoot(path)) {
                        rootRepo.getSelinuxContext(path)
                    } else null
                    val currentTabId = _state.value.tabs.getOrNull(_state.value.selectedTabIndex)?.id
                    _state.update {
                        val newCache = if (currentTabId != null) it.tabFilesCache + (currentTabId to sorted) else it.tabFilesCache
                        it.copy(currentPath = path, files = sorted, isLoading = false,
                            tabFilesCache = newCache,
                            pathHistory = history, historyIndex = history.lastIndex,
                            insideArchive = false, archivePath = null, archiveInternalPath = "",
                            selinuxContext = selinux,
                            tabs = updateTabState(it.tabs, it.selectedTabIndex, path, false, null, "", history, history.lastIndex))
                    }
                    recordRecentLocation(path)
                    scheduleDirectorySizes(path, sorted, repo, secondary = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(isLoading = false, error = "Unable to open folder: ${error.message}") }
            }
        }
    }

    fun toggleDualPane() {
        val current = _state.value
        if (current.insideArchive) {
            viewModelScope.launch {
                _events.emit(BrowserEvent.Toast("Close the archive before opening dual-pane view"))
            }
            return
        }

        val enabled = !current.dualPaneEnabled
        _state.update {
            it.copy(
                dualPaneEnabled = enabled,
                secondarySelectedItems = emptySet(),
                pendingDrop = null,
            )
        }
        if (!enabled) {
            secondaryDirectorySizeJob?.cancel()
            _state.update {
                it.copy(
                    secondaryDirectorySizes = emptyMap(),
                    secondaryDirectorySizesLoading = emptySet(),
                    secondaryDirectorySizesUnavailable = emptySet(),
                )
            }
        }
        if (enabled) {
            navigateSecondaryTo(current.secondaryPath.ifBlank { current.currentPath })
        }
    }

    fun openInSplitTab(folderPath: String) {
        val current = _state.value
        if (current.insideArchive) {
            viewModelScope.launch {
                _events.emit(BrowserEvent.Toast("Close the archive before opening dual-pane view"))
            }
            return
        }
        _state.update {
            it.copy(
                dualPaneEnabled = true,
                selectedItems = emptySet(),
                secondarySelectedItems = emptySet(),
                pendingDrop = null,
            )
        }
        navigateSecondaryTo(folderPath)
    }

    fun importDroppedUris(
        context: android.content.Context,
        uris: List<Uri>,
        destinationDir: String = _state.value.currentPath,
    ) {
        if (uris.isEmpty() || _state.value.insideArchive) return
        viewModelScope.launch(Dispatchers.IO) {
            val destRoot = java.io.File(destinationDir)
            if (!destRoot.isDirectory) return@launch
            var imported = 0
            uris.forEach { uri ->
                runCatching {
                    val displayName = context.contentResolver.query(
                        uri,
                        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null,
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }?.substringAfterLast('/')?.ifBlank { null }
                        ?: "dropped_${System.currentTimeMillis()}"

                    var target = java.io.File(destRoot, displayName)
                    var counter = 1
                    val base = target.nameWithoutExtension
                    val ext = target.extension
                    while (target.exists()) {
                        val candidate = if (ext.isNotEmpty()) "$base ($counter).$ext" else "$base ($counter)"
                        target = java.io.File(destRoot, candidate)
                        counter++
                    }
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    imported++
                }
            }
            if (imported > 0) {
                _events.emit(BrowserEvent.Toast("Imported $imported file(s)"))
                withContext(Dispatchers.Main) { refresh() }
            }
        }
    }

    fun navigateSecondaryTo(path: String) {
        if (!_state.value.dualPaneEnabled) return
        secondaryLoadJob?.cancel()
        secondaryDirectorySizeJob?.cancel()
        _state.update {
            it.copy(
                secondaryDirectorySizes = emptyMap(),
                secondaryDirectorySizesLoading = emptySet(),
                secondaryDirectorySizesUnavailable = emptySet(),
            )
        }
        secondaryLoadJob = viewModelScope.launch {
            val preferences = resolveDirectoryPreferences(path)
            _state.update {
                it.copy(
                    secondaryIsLoading = true,
                    secondaryError = null,
                    secondarySelectedItems = emptySet(),
                    secondaryTabs = updateTabState(
                        it.secondaryTabs,
                        it.secondarySelectedTabIndex,
                        path,
                        false,
                        null,
                        "",
                    ),
                )
            }
            try {
                val repository = repoFactory.getRepository(path)
                val capabilityMatrix = repository.capabilityMatrix(path)
                diagnosticLog.logCapabilities(capabilityMatrix)
                _state.update { it.copy(secondaryCapabilityMatrix = capabilityMatrix) }
                repository.listFiles(path).collect { files ->
                    val sorted = sortFiles(files, preferences.sortOrder, _state.value.showHidden)
                    _state.update {
                        it.copy(
                            secondaryPath = path,
                            secondaryFiles = sorted,
                            secondaryIsLoading = false,
                            secondaryError = null,
                        )
                    }
                    recordRecentLocation(path)
                    scheduleDirectorySizes(path, sorted, repository, secondary = true)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(secondaryIsLoading = false, secondaryError = "Unable to open folder: ${e.message}")
                }
            }
        }
    }

    fun navigateSecondaryUp() {
        val path = _state.value.secondaryPath
        if (UsbPathCodec.isUsbPath(path)) {
            UsbPathCodec.parentPath(path)?.let(::navigateSecondaryTo) ?: navigateSecondaryTo("/")
            return
        }
        val parent = path.substringBeforeLast('/', "/")
        if (parent.isNotEmpty() && parent != path) navigateSecondaryTo(parent)
    }

    fun refreshSecondary() {
        clearDirectorySizeCacheForRefresh()
        navigateSecondaryTo(_state.value.secondaryPath)
    }

    fun openTab(pane: BrowserPane) {
        val state = _state.value
        val path = if (pane == BrowserPane.PRIMARY) state.currentPath else state.secondaryPath
        openTabAt(pane, path)
    }

    fun openTabAt(pane: BrowserPane, targetPath: String) {
        val tab = BrowserTab(nextTabId++, targetPath)
        if (pane == BrowserPane.PRIMARY) {
            _state.update { it.copy(tabs = it.tabs + tab, selectedTabIndex = it.tabs.size) }
            navigateTo(targetPath)
        } else {
            _state.update {
                it.copy(
                    secondaryTabs = it.secondaryTabs + tab,
                    secondarySelectedTabIndex = it.secondaryTabs.size,
                )
            }
            navigateSecondaryTo(targetPath)
        }
    }

    fun selectTab(pane: BrowserPane, index: Int) {
        val state = _state.value
        if (pane == BrowserPane.PRIMARY) {
            closeInlineSearch()
            val tab = state.tabs.getOrNull(index) ?: return
            if (index == state.selectedTabIndex) return
            _state.update {
                it.copy(
                    selectedTabIndex = index,
                    pathHistory = tab.pathHistory,
                    historyIndex = tab.historyIndex,
                    insideArchive = tab.insideArchive,
                    archivePath = tab.archivePath,
                    archiveInternalPath = tab.archiveInternalPath,
                )
            }
            if (tab.insideArchive && tab.archivePath != null) {
                navigateInsideArchive(tab.archivePath, tab.archiveInternalPath)
            } else {
                navigateTo(tab.path)
            }
        } else {
            val tab = state.secondaryTabs.getOrNull(index) ?: return
            if (index == state.secondarySelectedTabIndex) return
            _state.update {
                it.copy(
                    secondarySelectedTabIndex = index,
                )
            }
            navigateSecondaryTo(tab.path)
        }
    }

    fun closeTab(pane: BrowserPane, index: Int) {
        val state = _state.value
        if (pane == BrowserPane.PRIMARY) {
            if (state.tabs.size <= 1 || state.tabs.getOrNull(index) == null) return
            val closedTab = state.tabs.getOrNull(index)
            val tabs = state.tabs.toMutableList().apply { removeAt(index) }
            val selected = TabIndexPolicy.selectedIndexAfterClose(state.selectedTabIndex, index, tabs.lastIndex)
            val newCache = if (closedTab != null) state.tabFilesCache - closedTab.id else state.tabFilesCache
            _state.update { it.copy(tabs = tabs, selectedTabIndex = selected, tabFilesCache = newCache) }
            if (index == state.selectedTabIndex) navigateTo(tabs[selected].path)
        } else {
            if (state.secondaryTabs.size <= 1 || state.secondaryTabs.getOrNull(index) == null) return
            val tabs = state.secondaryTabs.toMutableList().apply { removeAt(index) }
            val selected = TabIndexPolicy.selectedIndexAfterClose(
                state.secondarySelectedTabIndex,
                index,
                tabs.lastIndex,
            )
            _state.update { it.copy(secondaryTabs = tabs, secondarySelectedTabIndex = selected) }
            if (index == state.secondarySelectedTabIndex) navigateSecondaryTo(tabs[selected].path)
        }
    }

    fun reorderTabs(pane: BrowserPane, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        _state.update { state ->
            if (pane == BrowserPane.PRIMARY) {
                val tabs = state.tabs.toMutableList()
                if (fromIndex !in tabs.indices || toIndex !in tabs.indices) return@update state
                val tab = tabs.removeAt(fromIndex)
                tabs.add(toIndex, tab)
                state.copy(
                    tabs = tabs,
                    selectedTabIndex = TabIndexPolicy.moveSelectedIndex(
                        state.selectedTabIndex,
                        fromIndex,
                        toIndex,
                    ),
                )
            } else {
                val tabs = state.secondaryTabs.toMutableList()
                if (fromIndex !in tabs.indices || toIndex !in tabs.indices) return@update state
                val tab = tabs.removeAt(fromIndex)
                tabs.add(toIndex, tab)
                state.copy(
                    secondaryTabs = tabs,
                    secondarySelectedTabIndex = TabIndexPolicy.moveSelectedIndex(
                        state.secondarySelectedTabIndex,
                        fromIndex,
                        toIndex,
                    ),
                )
            }
        }
    }

    fun onSecondaryItemClick(item: FileItem) {
        if (_state.value.secondarySelectedItems.isNotEmpty()) {
            toggleSecondarySelection(item.path)
            return
        }
        if (item.isArchive && archiveHelper.isArchive(item.path)) {
            viewModelScope.launch {
                _events.emit(BrowserEvent.Toast("Close dual-pane view to browse an archive"))
            }
        } else if (item.isDirectory) {
            navigateSecondaryTo(item.path)
        } else {
            viewModelScope.launch {
                recentFileDao.upsert(
                    RecentFileEntity(name = item.name, path = item.path, mimeType = item.mimeType, size = item.size),
                )
                _events.emit(BrowserEvent.OpenFile(item))
            }
        }
    }

    fun onSecondaryItemLongClick(item: FileItem) {
        toggleSecondarySelection(item.path)
    }

    fun toggleSecondarySelection(path: String) {
        _state.update { state ->
            val selected = state.secondarySelectedItems.toMutableSet()
            if (!selected.add(path)) selected.remove(path)
            state.copy(secondarySelectedItems = selected)
        }
    }

    fun requestDrop(
        items: List<FileItem>,
        destinationPath: String,
        sourcePane: BrowserPane,
        destinationPane: BrowserPane,
    ) {
        val validItems = items.filter { DropPathPolicy.canDrop(it.path, destinationPath) }
        if (validItems.isEmpty()) {
            viewModelScope.launch {
                _events.emit(BrowserEvent.Toast("Nothing can be dropped into this folder"))
            }
            return
        }
        _state.update {
            it.copy(
                pendingDrop = PendingDrop(
                    items = validItems,
                    destinationPath = destinationPath,
                    sourcePane = sourcePane,
                    destinationPane = destinationPane,
                ),
            )
        }
    }

    fun dismissDrop() {
        _state.update { it.copy(pendingDrop = null) }
    }

    fun confirmDrop(move: Boolean) {
        val request = _state.value.pendingDrop ?: return
        val operation = if (move) RepositoryOperation.MOVE else RepositoryOperation.COPY
        val matrix = repoFactory.getRepository(request.destinationPath).capabilityMatrix(request.destinationPath)
        diagnosticLog.logCapabilities(matrix)
        if (!matrix.isActionEnabled(operation)) {
            viewModelScope.launch { _events.emit(BrowserEvent.Toast(matrix.explanation(operation))) }
            _state.update { it.copy(pendingDrop = null) }
            return
        }
        _state.update { it.copy(pendingDrop = null) }
        transferQueueManager.enqueue(
            operation = if (move) FileOperation.MOVE else FileOperation.COPY,
            sourcePaths = request.items.map { it.path },
            destination = request.destinationPath,
        )
        viewModelScope.launch {
            _events.emit(BrowserEvent.Toast("${request.items.size} item(s) queued for ${if (move) "move" else "copy"}"))
            clearPaneSelection(request.sourcePane)
        }
    }

    fun navigateUp() {
        if (_state.value.insideArchive) {
            val ip = _state.value.archiveInternalPath
            if (ip.contains('/')) {
                navigateInsideArchive(_state.value.archivePath!!, ip.substringBeforeLast('/'))
            } else { navigateTo(_state.value.archivePath!!.substringBeforeLast('/')) }
            return
        }
        if (UsbPathCodec.isUsbPath(_state.value.currentPath)) {
            UsbPathCodec.parentPath(_state.value.currentPath)?.let(::navigateTo) ?: navigateTo("/")
            return
        }
        val parent = _state.value.currentPath.substringBeforeLast('/', "/")
        if (parent.isNotEmpty() && parent != _state.value.currentPath) navigateTo(parent)
    }

    fun goBack() {
        if (_state.value.insideArchive) { navigateUp(); return }
        val s = _state.value
        if (s.canGoBack) loadPath(s.pathHistory[s.historyIndex - 1], s.historyIndex - 1)
    }

    fun goForward() {
        val s = _state.value
        if (s.canGoForward) loadPath(s.pathHistory[s.historyIndex + 1], s.historyIndex + 1)
    }

    private fun loadPath(path: String, historyIndex: Int) {
        primaryLoadJob?.cancel()
        directorySizeJob?.cancel()
        _state.update {
            it.copy(
                directorySizes = emptyMap(),
                directorySizesLoading = emptySet(),
                directorySizesUnavailable = emptySet(),
            )
        }
        primaryLoadJob = viewModelScope.launch {
            val preferences = resolveDirectoryPreferences(path)
            _state.update { it.copy(isLoading = true, selectedItems = emptySet()) }
            val repository = repoFactory.getRepository(path)
            val capabilityMatrix = repository.capabilityMatrix(path)
            diagnosticLog.logCapabilities(capabilityMatrix)
            _state.update { it.copy(capabilityMatrix = capabilityMatrix) }
            try {
                repository.listFiles(path).collect { files ->
                    val sorted = sortFiles(files, preferences.sortOrder, _state.value.showHidden)
                    _state.update {
                        it.copy(
                            currentPath = path,
                            files = sorted,
                            sortOrder = preferences.sortOrder,
                            viewMode = preferences.viewMode,
                            visibleColumns = preferences.visibleColumns,
                            isLoading = false,
                            historyIndex = historyIndex,
                            tabs = updateTabState(it.tabs, it.selectedTabIndex, path, false, null, "", it.pathHistory, historyIndex),
                        )
                    }
                    recordRecentLocation(path)
                    scheduleDirectorySizes(path, sorted, repository, secondary = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(isLoading = false, error = "Unable to open folder: ${error.message}") }
            }
        }
    }

    private fun navigateInsideArchive(archivePath: String, internalPath: String) {
        primaryLoadJob?.cancel()
        directorySizeJob?.cancel()
        primaryLoadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val entries = archiveHelper.listArchive(archivePath, internalPath)
                val capabilityMatrix = repoFactory.getRepository(archivePath).capabilityMatrix(archivePath)
                diagnosticLog.logCapabilities(capabilityMatrix)
                _state.update { it.copy(files = entries, isLoading = false, insideArchive = true,
                    archivePath = archivePath, archiveInternalPath = internalPath,
                    currentPath = "$archivePath/$internalPath".trimEnd('/'),
                    capabilityMatrix = capabilityMatrix,
                    directorySizes = emptyMap(),
                    directorySizesLoading = emptySet(),
                    directorySizesUnavailable = emptySet(),
                    tabs = updateTabState(it.tabs, it.selectedTabIndex, "$archivePath/$internalPath".trimEnd('/'), true, archivePath, internalPath, it.pathHistory, it.historyIndex)) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Archive error: ${e.message}") }
            }
        }
    }

    fun onItemClick(item: FileItem) {
        if (_state.value.selectionMode) { toggleSelection(item.path); return }
        if (_state.value.insideArchive) {
            if (item.isDirectory) navigateInsideArchive(_state.value.archivePath!!, item.path)
            return
        }
        if (item.isArchive && archiveHelper.isArchive(item.path)) { navigateInsideArchive(item.path, ""); return }
        if (item.isDirectory) { navigateTo(item.path) } else {
            viewModelScope.launch {
                recentFileDao.upsert(RecentFileEntity(name = item.name, path = item.path, mimeType = item.mimeType, size = item.size))
                _events.emit(BrowserEvent.OpenFile(item))
            }
        }
    }

    fun onItemLongClick(item: FileItem) { toggleSelection(item.path) }

    fun applySwipe(item: FileItem, action: SwipeAction) {
        if (action == SwipeAction.NONE) return
        if (_state.value.insideArchive) {
            viewModelScope.launch { _events.emit(BrowserEvent.Toast("Gestures are unavailable inside archives")) }
            return
        }
        when (action) {
            SwipeAction.DELETE -> {
                if (!canUseFeature(RepositoryFeature.TRASH)) return
                if (_state.value.confirmDelete) {
                    _state.update { it.copy(deleteConfirmationItem = item) }
                } else {
                    _state.update { it.copy(selectedItems = setOf(item.path)) }
                    deleteSelected()
                }
            }
            SwipeAction.SHARE -> viewModelScope.launch {
                _events.emit(BrowserEvent.ShareFiles(listOf(item)))
            }
            SwipeAction.COMPRESS -> {
                _state.update { it.copy(selectedItems = setOf(item.path), showCompressDialog = true) }
            }
            SwipeAction.MOVE -> {
                _state.update {
                    it.copy(
                        clipboard = ClipboardContent(listOf(item), FileOperation.MOVE, it.currentPath),
                        selectedItems = emptySet(),
                    )
                }
                viewModelScope.launch { _events.emit(BrowserEvent.Toast("Move ready; navigate to a folder and tap Paste")) }
            }
            SwipeAction.NONE -> Unit
        }
    }

    fun dismissDeleteConfirmation() { _state.update { it.copy(deleteConfirmationItem = null) } }

    fun confirmDeleteGesture() {
        val item = _state.value.deleteConfirmationItem ?: return
        _state.update { it.copy(deleteConfirmationItem = null, selectedItems = setOf(item.path)) }
        deleteSelected()
    }

    fun watchSelectedIntegrity() {
        val paths = getSelectedFileItems().map { it.path }
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val matrix = repoFactory.getRepository(paths.first()).capabilityMatrix(paths.first(), paths)
            diagnosticLog.logCapabilities(matrix)
            if (!matrix.isActionEnabled(RepositoryFeature.CHECKSUMS)) {
                _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryFeature.CHECKSUMS)))
                return@launch
            }
            var watched = 0
            var failed = 0
            paths.forEach { path ->
                integrityRepository.addPath(path)
                    .onSuccess { watched++ }
                    .onFailure { failed++ }
            }
            _events.emit(
                BrowserEvent.Toast(
                    if (failed == 0) "Watching $watched path(s) for changes"
                    else "Watching $watched path(s); $failed could not be added",
                ),
            )
            clearSelection()
        }
    }

    fun showTagDialog() {
        val paths = getSelectedFileItems().map { it.path }
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val commonTags = tagRepository.commonTagsForPaths(paths)
            _state.update {
                it.copy(showTagDialog = true, tagDialogSelectedTags = commonTags)
            }
        }
    }

    fun dismissTagDialog() {
        _state.update { it.copy(showTagDialog = false, tagDialogSelectedTags = emptySet()) }
    }

    fun toggleTagInDialog(tagName: String) {
        _state.update { state ->
            val selected = state.tagDialogSelectedTags.toMutableSet()
            if (!selected.add(tagName)) selected.remove(tagName)
            state.copy(tagDialogSelectedTags = selected)
        }
    }

    fun createTagForDialog(value: String) {
        viewModelScope.launch {
            tagRepository.createTag(value)
                .onSuccess { tag ->
                    _state.update { it.copy(tagDialogSelectedTags = it.tagDialogSelectedTags + tag.name) }
                }
                .onFailure { error -> _events.emit(BrowserEvent.Toast(error.message ?: "Unable to create tag")) }
        }
    }

    fun applyTags() {
        val paths = getSelectedFileItems().map { it.path }
        val selectedTags = _state.value.tagDialogSelectedTags
        if (paths.isEmpty()) {
            dismissTagDialog()
            return
        }
        viewModelScope.launch {
            tagRepository.replaceTags(paths, selectedTags)
            _events.emit(BrowserEvent.Toast("Applied ${selectedTags.size} tag(s) to ${paths.size} item(s)"))
            dismissTagDialog()
            clearSelection()
        }
    }

    fun showCompressDialog() { _state.update { it.copy(showCompressDialog = true) } }
    fun dismissCompressDialog() { _state.update { it.copy(showCompressDialog = false) } }

    fun toggleSelection(path: String) {
        _state.update { s -> val n = s.selectedItems.toMutableSet(); if (path in n) n.remove(path) else n.add(path); s.copy(selectedItems = n) }
    }
    fun selectAll() { _state.update { s -> s.copy(selectedItems = s.files.map { it.path }.toSet()) } }
    fun clearSelection() { _state.update { it.copy(selectedItems = emptySet()) } }

    fun copySelected() {
        val items = getSelectedFileItems()
        if (items.isEmpty() || !canUseOperation(RepositoryOperation.COPY)) return
        clipboardQueueRepository.enqueue(FileOperation.COPY, items, _state.value.currentPath)
        _state.update { it.copy(selectedItems = emptySet()) }
        viewModelScope.launch { _events.emit(BrowserEvent.Toast("${items.size} item(s) added to action queue")) }
    }

    fun cutSelected() {
        val items = getSelectedFileItems()
        if (items.isEmpty() || !canUseOperation(RepositoryOperation.MOVE)) return
        clipboardQueueRepository.enqueue(FileOperation.MOVE, items, _state.value.currentPath)
        _state.update { it.copy(selectedItems = emptySet()) }
        viewModelScope.launch { _events.emit(BrowserEvent.Toast("${items.size} item(s) queued to move")) }
    }

    fun queueExtractSelected() {
        val current = _state.value
        if (current.insideArchive) {
            val archive = current.archivePath ?: return
            val entries = current.selectedItems.toList().takeIf { it.isNotEmpty() }
            clipboardQueueRepository.enqueue(
                operation = FileOperation.EXTRACT,
                items = getSelectedFileItems(),
                sourcePath = current.currentPath,
                archivePath = archive,
                archiveEntries = entries,
            )
            clearSelection()
            viewModelScope.launch { _events.emit(BrowserEvent.Toast("Extract queued; open destination folder to run")) }
            return
        }
        val archives = getSelectedFileItems().filter { it.isArchive && archiveHelper.isArchive(it.path) }
        if (archives.isEmpty()) return
        archives.forEach { archive ->
            clipboardQueueRepository.enqueue(
                operation = FileOperation.EXTRACT,
                items = listOf(archive),
                sourcePath = current.currentPath,
                archivePath = archive.path,
            )
        }
        clearSelection()
        viewModelScope.launch { _events.emit(BrowserEvent.Toast("${archives.size} archive(s) queued for extraction")) }
    }

    fun executeClipboardTask(taskId: Long) {
        val destination = _state.value.currentPath
        if (_state.value.insideArchive) return
        val task = clipboardQueueRepository.tasks.value.firstOrNull { it.id == taskId } ?: return
        when (task.operation) {
            FileOperation.EXTRACT -> {
                val archive = task.archivePath ?: task.items.firstOrNull()?.path ?: return
                if (!canUseFeature(RepositoryFeature.ARCHIVE_EXTRACTION, archive)) return
                clipboardQueueRepository.remove(taskId)
                transferQueueManager.enqueueExtract(
                    archivePath = archive,
                    destination = destination,
                    entriesToExtract = task.archiveEntries,
                )
                viewModelScope.launch { _events.emit(BrowserEvent.Toast("Extraction queued")) }
            }
            FileOperation.MOVE, FileOperation.COPY -> {
                val op = if (task.isCut) RepositoryOperation.MOVE else RepositoryOperation.COPY
                if (!canUseOperation(op, destination)) return
                clipboardQueueRepository.remove(taskId)
                transferQueueManager.enqueue(
                    operation = task.operation,
                    sourcePaths = task.items.map { it.path },
                    destination = destination,
                )
                viewModelScope.launch {
                    _events.emit(BrowserEvent.Toast("${task.items.size} item(s) queued for ${if (task.isCut) "move" else "copy"}"))
                }
            }
            else -> Unit
        }
    }

    fun dismissClipboardTask(taskId: Long) = clipboardQueueRepository.remove(taskId)
    fun clearClipboardQueue() = clipboardQueueRepository.clear()

    fun paste() {
        val latest = clipboardQueueRepository.tasks.value.lastOrNull() ?: return
        executeClipboardTask(latest.id)
    }

    fun extractArchive(archivePath: String? = null, destination: String? = null) {
        val archive = archivePath ?: _state.value.archivePath ?: return
        val dest = destination ?: if (_state.value.insideArchive) {
            archive.substringBeforeLast('/')
        } else {
            _state.value.currentPath
        }
        if (!canUseFeature(RepositoryFeature.ARCHIVE_EXTRACTION, archive)) return
        transferQueueManager.enqueueExtract(archivePath = archive, destination = dest)
        viewModelScope.launch { _events.emit(BrowserEvent.Toast("Extraction queued")) }
    }

    fun compressSelected(outputName: String, format: ArchiveFormat = ArchiveFormat.ZIP, password: CharArray? = null) {
        val items = getSelectedFileItems()
        if (items.isEmpty()) {
            password?.fill('\u0000')
            return
        }
        val outputPath = "${_state.value.currentPath}/$outputName.${format.extension}"
        if (!canUseFeature(RepositoryFeature.ARCHIVE_CREATION, outputPath)) {
            password?.fill('\u0000')
            return
        }
        transferQueueManager.enqueueCompress(
            sourcePaths = items.map { it.path },
            outputPath = outputPath,
            format = format,
            password = password,
        )
        clearSelection()
        viewModelScope.launch { _events.emit(BrowserEvent.Toast("Compression queued")) }
    }

    fun deleteSelected() {
        deleteSelected(useTrash = true)
    }

    fun permanentlyDeleteSelected() {
        val paths = _state.value.selectedItems.toList()
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val repo = repoFactory.getRepository(paths.first())
            val matrix = repo.capabilityMatrix(paths.first(), paths)
            diagnosticLog.logCapabilities(matrix)
            if (!matrix.isActionEnabled(RepositoryOperation.DELETE)) {
                _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryOperation.DELETE)))
                return@launch
            }
            val capabilities = repo.deleteCapabilities(paths)
            if (!capabilities.supportsPermanentDelete) {
                _events.emit(BrowserEvent.Toast("Permanent deletion is not supported by this provider"))
                return@launch
            }
            val secureDeleteEnabled = securityRepository.settings.first().secureDeleteEnabled
            _state.update {
                it.copy(
                    permanentDeleteConfirmation = PermanentDeleteConfirmation(
                        paths = paths,
                        capabilities = capabilities,
                        secureDeleteEnabled = secureDeleteEnabled,
                    ),
                )
            }
        }
    }

    fun dismissPermanentDeleteConfirmation() {
        _state.update { it.copy(permanentDeleteConfirmation = null) }
    }

    fun confirmPermanentDelete() {
        val confirmation = _state.value.permanentDeleteConfirmation ?: return
        _state.update { it.copy(permanentDeleteConfirmation = null) }
        deleteSelected(
            useTrash = false,
            confirmedPaths = confirmation.paths,
            confirmedCapabilities = confirmation.capabilities,
            confirmedSecureDelete = confirmation.secureDeleteEnabled,
        )
    }

    private fun deleteSelected(
        useTrash: Boolean,
        confirmedPaths: List<String>? = null,
        confirmedCapabilities: DeleteCapabilities? = null,
        confirmedSecureDelete: Boolean? = null,
    ) {
        viewModelScope.launch {
            val paths = confirmedPaths ?: _state.value.selectedItems.toList()
            if (paths.isEmpty()) return@launch

            if (useTrash) {
                val matrix = repoFactory.getRepository(paths.first()).capabilityMatrix(paths.first(), paths)
                diagnosticLog.logCapabilities(matrix)
                if (!matrix.isActionEnabled(RepositoryFeature.TRASH)) {
                    _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryFeature.TRASH)))
                    return@launch
                }
                if (repoFactory.getRepository(paths.first()) !is LocalFileRepository) {
                    _events.emit(BrowserEvent.Toast("Trash is only available for local storage"))
                    return@launch
                }
                val roots = trashVolumeRoots()
                val ttlDays = settingsRepository.settings.first().trashTtlDays
                trashManager.purgeExpired(ttlDays, roots)
                    .onFailure { e -> _events.emit(BrowserEvent.Toast("Trash purge failed: ${e.message}")) }
                trashManager.moveToTrash(paths, roots)
                    .onSuccess { c -> _events.emit(BrowserEvent.Toast("$c items moved to Trash")); clearSelection(); refresh() }
                    .onFailure { e -> _events.emit(BrowserEvent.Toast("Trash failed: ${e.message}")) }
            } else {
                val repo = repoFactory.getRepository(paths.first())
                val capabilities = confirmedCapabilities ?: repo.deleteCapabilities(paths)
                if (!capabilities.supportsPermanentDelete) {
                    _events.emit(BrowserEvent.Toast("Permanent deletion is not supported by this provider"))
                    return@launch
                }
                val secureEnabled = confirmedSecureDelete ?: securityRepository.settings.first().secureDeleteEnabled
                if (secureEnabled && capabilities.secureDelete == SecureDeleteCapability.BEST_EFFORT) {
                    var deleted = 0
                    var failed = 0
                    val failedPaths = mutableSetOf<String>()
                    for (p in paths) {
                        SecureDelete.secureDelete(p).onFailure { e ->
                            _events.emit(BrowserEvent.Toast("Secure delete failed: ${e.message}"))
                            failed++
                            failedPaths += p
                        }.onSuccess { deleted++ }
                    }
                    if (failed == 0) {
                        _events.emit(BrowserEvent.Toast("$deleted items deleted with best-effort overwrite"))
                    } else {
                        _events.emit(BrowserEvent.Toast("$deleted items deleted; $failed failed"))
                    }
                    if (failed == 0) clearSelection()
                    else _state.update { it.copy(selectedItems = failedPaths) }
                    if (deleted > 0 || failed == 0) refresh()
                } else {
                    if (secureEnabled && capabilities.secureDelete == SecureDeleteCapability.UNSUPPORTED) {
                        _events.emit(BrowserEvent.Toast("Secure overwrite unavailable: ${capabilities.secureDeleteDescription}"))
                    }
                    repo.deleteFiles(paths)
                        .onSuccess { c ->
                            val message = if (c == paths.size) {
                                "$c items deleted permanently"
                            } else {
                                "$c of ${paths.size} items deleted; partial failure"
                            }
                            _events.emit(BrowserEvent.Toast(message))
                            if (c == paths.size) clearSelection()
                            refresh()
                        }
                        .onFailure { e -> _events.emit(BrowserEvent.Toast("Delete failed: ${e.message}")) }
                }
            }
        }
    }

    fun rename(path: String, newName: String) {
        viewModelScope.launch {
            val repository = repoFactory.getRepository(path)
            val matrix = repository.capabilityMatrix(path)
            diagnosticLog.logCapabilities(matrix)
            if (!matrix.isActionEnabled(RepositoryOperation.RENAME)) {
                _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryOperation.RENAME)))
                return@launch
            }
            repository.rename(path, newName)
                .onSuccess { _state.update { it.copy(renameItem = null) }; refresh() }
                .onFailure { e -> _events.emit(BrowserEvent.Toast("Rename failed: ${e.message}")) }
        }
    }

    fun showBatchRenameDialog() {
        if (_state.value.selectedItems.isNotEmpty()) {
            _state.update { it.copy(showBatchRenameDialog = true) }
        }
    }

    fun dismissBatchRenameDialog() {
        _state.update { it.copy(showBatchRenameDialog = false) }
    }

    fun batchRename(options: BatchRenameOptions) {
        val snapshot = _state.value
        val items = snapshot.files.filter { it.path in snapshot.selectedItems }
        val preview = BatchRenameEngine.preview(items, options)
        if (!preview.isValid) {
            viewModelScope.launch {
                _events.emit(
                    BrowserEvent.Toast(
                        preview.errors.firstOrNull() ?: "Choose a template that changes at least one name",
                    ),
                )
            }
            return
        }

        _state.update { it.copy(showBatchRenameDialog = false) }
        viewModelScope.launch {
            val staged = mutableListOf<StagedRename>()
            try {
                val repository = repoFactory.getRepository(snapshot.currentPath)
                val matrix = repository.capabilityMatrix(snapshot.currentPath)
                diagnosticLog.logCapabilities(matrix)
                if (!matrix.isActionEnabled(RepositoryOperation.RENAME)) {
                    _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryOperation.RENAME)))
                    return@launch
                }
                val changedSources = preview.changedItems.map { it.item.path }.toSet()
                val conflict = preview.changedItems.firstOrNull { operation ->
                    operation.targetPath !in changedSources && repository.exists(operation.targetPath)
                }
                if (conflict != null) {
                    _events.emit(BrowserEvent.Toast("Target already exists: ${conflict.newName}"))
                    return@launch
                }

                val operationId = UUID.randomUUID().toString().replace("-", "")
                preview.changedItems.forEachIndexed { index, operation ->
                    val temporaryName = ".fileexplorer-rename-$operationId-$index"
                    val temporaryPath = BatchRenameEngine.siblingPath(operation.item.path, temporaryName)
                    if (repository.exists(temporaryPath)) {
                        error("Temporary rename target already exists")
                    }
                    repository.rename(operation.item.path, temporaryName).getOrThrow()
                    staged += StagedRename(operation, temporaryPath)
                }

                staged.forEach { operation ->
                    repository.rename(operation.temporaryPath, operation.preview.newName).getOrThrow()
                    operation.finalized = true
                }
                _events.emit(BrowserEvent.Toast("${staged.size} items renamed"))
                clearSelection()
                refresh()
            } catch (error: Exception) {
                if (staged.isNotEmpty()) {
                    val repository = repoFactory.getRepository(snapshot.currentPath)
                    staged.asReversed().forEach { operation ->
                        val currentPath = if (operation.finalized) {
                            operation.preview.targetPath
                        } else {
                            operation.temporaryPath
                        }
                        repository.rename(currentPath, operation.preview.item.name)
                    }
                }
                _events.emit(BrowserEvent.Toast("Batch rename failed: ${error.message}"))
                refresh()
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val path = if (UsbPathCodec.isUsbPath(_state.value.currentPath)) {
                UsbPathCodec.childPath(_state.value.currentPath, name)
            } else {
                "${_state.value.currentPath}/$name"
            }
            val repository = repoFactory.getRepository(path)
            val matrix = repository.capabilityMatrix(path)
            diagnosticLog.logCapabilities(matrix)
            if (!matrix.isActionEnabled(RepositoryOperation.CREATE_DIRECTORY)) {
                _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryOperation.CREATE_DIRECTORY)))
                return@launch
            }
            repository.createDirectory(path)
                .onSuccess { _state.update { it.copy(showNewFolderDialog = false) }; refresh() }
                .onFailure { e -> _events.emit(BrowserEvent.Toast("Create failed: ${e.message}")) }
        }
    }

    private fun canUseOperation(
        operation: RepositoryOperation,
        path: String = _state.value.currentPath,
    ): Boolean {
        val matrix = repoFactory.getRepository(path).capabilityMatrix(path)
        diagnosticLog.logCapabilities(matrix)
        if (matrix.isActionEnabled(operation)) return true
        viewModelScope.launch { _events.emit(BrowserEvent.Toast(matrix.explanation(operation))) }
        return false
    }

    private fun canUseFeature(
        feature: RepositoryFeature,
        path: String = _state.value.currentPath,
    ): Boolean {
        val matrix = repoFactory.getRepository(path).capabilityMatrix(path)
        diagnosticLog.logCapabilities(matrix)
        if (matrix.isActionEnabled(feature)) return true
        viewModelScope.launch { _events.emit(BrowserEvent.Toast(matrix.explanation(feature))) }
        return false
    }

    fun showNewFolderDialog() { _state.update { it.copy(showNewFolderDialog = true) } }
    fun dismissNewFolderDialog() { _state.update { it.copy(showNewFolderDialog = false) } }

    fun showNewFileDialog() { _state.update { it.copy(showNewFileDialog = true) } }
    fun dismissNewFileDialog() { _state.update { it.copy(showNewFileDialog = false) } }

    fun createFile(name: String) {
        viewModelScope.launch {
            val path = if (UsbPathCodec.isUsbPath(_state.value.currentPath)) {
                UsbPathCodec.childPath(_state.value.currentPath, name)
            } else {
                "${_state.value.currentPath}/$name"
            }
            val repository = repoFactory.getRepository(path)
            val matrix = repository.capabilityMatrix(path)
            diagnosticLog.logCapabilities(matrix)
            if (!matrix.isActionEnabled(RepositoryOperation.CREATE_FILE)) {
                _events.emit(BrowserEvent.Toast(matrix.explanation(RepositoryOperation.CREATE_FILE)))
                return@launch
            }
            repository.createFile(path)
                .onSuccess { _state.update { it.copy(showNewFileDialog = false) }; refresh() }
                .onFailure { e -> _events.emit(BrowserEvent.Toast("Create file failed: ${e.message}")) }
        }
    }

    private var inlineSearchJob: Job? = null

    fun setInlineSearchActive(active: Boolean) {
        if (!active) {
            closeInlineSearch()
            return
        }
        _state.update {
            it.copy(
                inlineSearchActive = true,
                inlineSearchQuery = "",
                inlineSearchResults = emptyList(),
                isInlineSearching = false,
            )
        }
    }

    fun setInlineSearchQuery(query: String) {
        _state.update { it.copy(inlineSearchQuery = query) }
        executeInlineSearch(query, _state.value.inlineSearchRecursive)
    }

    fun toggleInlineSearchScope() {
        val newRecursive = !_state.value.inlineSearchRecursive
        _state.update { it.copy(inlineSearchRecursive = newRecursive) }
        executeInlineSearch(_state.value.inlineSearchQuery, newRecursive)
    }

    fun closeInlineSearch() {
        inlineSearchJob?.cancel()
        inlineSearchJob = null
        _state.update {
            it.copy(
                inlineSearchActive = false,
                inlineSearchQuery = "",
                inlineSearchResults = emptyList(),
                isInlineSearching = false,
            )
        }
    }

    private fun executeInlineSearch(query: String, recursive: Boolean) {
        inlineSearchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _state.update { it.copy(inlineSearchResults = emptyList(), isInlineSearching = false) }
            return
        }
        val current = _state.value
        if (!recursive) {
            inlineSearchJob = viewModelScope.launch(Dispatchers.Default) {
                val filtered = current.files.filter { file ->
                    file.name.contains(trimmed, ignoreCase = true)
                }
                _state.update { it.copy(inlineSearchResults = filtered, isInlineSearching = false) }
            }
        } else {
            inlineSearchJob = viewModelScope.launch {
                delay(220L)
                _state.update { it.copy(isInlineSearching = true, inlineSearchResults = emptyList()) }
                try {
                    val repo = repoFactory.getRepository(current.currentPath)
                    val results = mutableListOf<FileItem>()
                    var lastEmitNanos = 0L
                    repo.search(
                        rootPath = current.currentPath,
                        query = trimmed,
                        regex = false,
                        includeHidden = current.showHidden,
                    ).take(500).collect { item ->
                        results.add(item)
                        val now = System.nanoTime()
                        if (now - lastEmitNanos >= 80_000_000L || results.size <= 10) {
                            lastEmitNanos = now
                            _state.update { it.copy(inlineSearchResults = results.toList()) }
                        }
                    }
                    _state.update { it.copy(inlineSearchResults = results.toList()) }
                } catch (c: CancellationException) {
                    throw c
                } catch (_: Exception) {
                } finally {
                    _state.update { it.copy(isInlineSearching = false) }
                }
            }
        }
    }
    fun showProperties(item: FileItem) { _state.update { it.copy(propertiesItem = item) } }
    fun dismissProperties() { _state.update { it.copy(propertiesItem = null) } }
    fun showRename(item: FileItem) { _state.update { it.copy(renameItem = item) } }
    fun dismissRename() { _state.update { it.copy(renameItem = null) } }
    fun shareSelected() { viewModelScope.launch { _events.emit(BrowserEvent.ShareFiles(getSelectedFileItems())) } }
    fun sendNearbySelected() {
        viewModelScope.launch { _events.emit(BrowserEvent.SendNearbyFiles(getSelectedFileItems())) }
    }

    fun castSelected() {
        val items = getSelectedFileItems()
        if (items.size != 1) {
            viewModelScope.launch { _events.emit(BrowserEvent.Toast("Select one photo, video, or audio file to cast")) }
            return
        }
        viewModelScope.launch {
            castMediaSender.cast(items.first())
                .onSuccess { _events.emit(BrowserEvent.Toast("Casting ${items.first().name}")) }
                .onFailure { error -> _events.emit(BrowserEvent.Toast("Cast failed: ${error.message}")) }
        }
    }

    fun encryptSelected() {
        val paths = _state.value.selectedItems.toList()
        if (paths.isEmpty()) return
        viewModelScope.launch {
            _events.emit(BrowserEvent.Toast("Encrypting..."))
            val result = fileEncryptionManager.encryptFiles(paths)
            _events.emit(BrowserEvent.Toast(encryptionSummary("Encrypted", result)))
            if (result.succeeded.isNotEmpty()) {
                clearSelection()
                refresh()
            }
        }
    }

    fun requestDecryptSelected() {
        val paths = _state.value.selectedItems.toList()
        if (paths.isNotEmpty()) {
            viewModelScope.launch { _events.emit(BrowserEvent.RequestDecrypt(paths)) }
        }
    }

    fun requestAddToVaultSelected() {
        val paths = _state.value.selectedItems.toList()
        if (paths.isEmpty()) return
        viewModelScope.launch {
            if (!securityRepository.settings.first().vaultEnabled) {
                _events.emit(BrowserEvent.Toast("Enable Vault in Security before adding files"))
            } else {
                _events.emit(BrowserEvent.RequestVaultAdd(paths))
            }
        }
    }

    /** Call only from a UI callback after biometric/device-credential success. */
    fun addToVault(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            if (!securityRepository.settings.first().vaultEnabled) {
                _events.emit(BrowserEvent.Toast("Vault is disabled"))
                return@launch
            }
            val session = vaultManager.unlock().getOrElse { error ->
                _events.emit(BrowserEvent.Toast("Vault unlock failed: ${error.message}"))
                return@launch
            }
            val succeeded = mutableListOf<String>()
            val failures = mutableListOf<String>()
            try {
                paths.distinct().forEach { path ->
                    vaultManager.addToVault(path, session)
                        .onSuccess { succeeded += path }
                        .onFailure { error -> failures += "$path: ${error.message ?: "operation failed"}" }
                }
            } finally {
                vaultManager.lock(session)
            }
            val summary = when {
                failures.isEmpty() -> "Added ${succeeded.size} file(s) to vault"
                succeeded.isEmpty() -> "Vault add failed: ${failures.first()}"
                else -> "Added ${succeeded.size} file(s) to vault; ${failures.size} failed"
            }
            _events.emit(BrowserEvent.Toast(summary))
            if (succeeded.isNotEmpty()) {
                _state.update { current -> current.copy(selectedItems = current.selectedItems - succeeded.toSet()) }
                refresh()
            }
        }
    }

    fun decryptFiles(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            _events.emit(BrowserEvent.Toast("Decrypting..."))
            val result = fileEncryptionManager.decryptFiles(paths)
            _events.emit(BrowserEvent.Toast(encryptionSummary("Decrypted", result)))
            if (result.succeeded.isNotEmpty()) {
                clearSelection()
                refresh()
            }
        }
    }

    private fun encryptionSummary(action: String, result: FileEncryptionBatchResult): String {
        return when {
            result.failures.isEmpty() -> "$action ${result.succeeded.size} file(s)"
            result.succeeded.isEmpty() -> "$action failed: ${result.failures.first()}"
            else -> "$action ${result.succeeded.size} file(s); ${result.failures.size} failed"
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        val current = _state.value
        val updated = current.copy(
            sortOrder = sortOrder,
            files = sortFiles(current.files, sortOrder, current.showHidden),
            secondaryFiles = sortFiles(current.secondaryFiles, sortOrder, current.showHidden),
        )
        _state.value = updated
        persistDirectoryPreferences(updated)
    }

    fun toggleViewMode() {
        val current = _state.value
        val updated = current.copy(
            viewMode = if (current.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST,
        )
        _state.value = updated
        persistDirectoryPreferences(updated)
    }

    fun toggleColumn(column: FileColumn) {
        val current = _state.value
        val columns = current.visibleColumns.toMutableSet()
        if (!columns.add(column)) columns.remove(column)
        val updated = current.copy(visibleColumns = columns)
        _state.value = updated
        persistDirectoryPreferences(updated)
    }

    private fun persistDirectoryPreferences(state: BrowserUiState) {
        if (state.currentPath.isBlank() || state.insideArchive) return
        viewModelScope.launch {
            directoryViewPreferenceDao.upsert(
                DirectoryViewPreferenceCodec.encode(
                    path = state.currentPath,
                    viewMode = state.viewMode,
                    sortOrder = state.sortOrder,
                    visibleColumns = state.visibleColumns,
                ),
            )
        }
    }
    fun toggleHidden() {
        _state.update { state ->
            val showHidden = !state.showHidden
            state.copy(
                showHidden = showHidden,
                files = sortFiles(state.files, state.sortOrder, showHidden),
                secondaryFiles = sortFiles(state.secondaryFiles, state.sortOrder, showHidden),
            )
        }
        refresh()
        if (_state.value.dualPaneEnabled) refreshSecondary()
    }

    fun toggleBookmark(path: String, name: String) {
        viewModelScope.launch {
            if (bookmarkDao.exists(path)) { bookmarkDao.deleteByPath(path); _events.emit(BrowserEvent.Toast("Bookmark removed")) }
            else { bookmarkDao.insert(BookmarkEntity(name = name, path = path)); _events.emit(BrowserEvent.Toast("Bookmarked")) }
        }
    }

    fun refresh() {
        clearDirectorySizeCacheForRefresh()
        if (_state.value.insideArchive) navigateInsideArchive(_state.value.archivePath!!, _state.value.archiveInternalPath)
        else navigateTo(_state.value.currentPath)
    }

    private fun refreshPane(pane: BrowserPane) {
        if (pane == BrowserPane.PRIMARY) refresh() else refreshSecondary()
    }

    private fun clearPaneSelection(pane: BrowserPane) {
        _state.update {
            if (pane == BrowserPane.PRIMARY) it.copy(selectedItems = emptySet())
            else it.copy(secondarySelectedItems = emptySet())
        }
    }

    private fun updateTabPath(tabs: List<BrowserTab>, selectedIndex: Int, path: String): List<BrowserTab> {
        val tab = tabs.getOrNull(selectedIndex) ?: return tabs
        return tabs.toMutableList().apply { set(selectedIndex, tab.copy(path = path)) }
    }

    private fun updateTabState(
        tabs: List<BrowserTab>,
        selectedIndex: Int,
        path: String,
        insideArchive: Boolean = false,
        archivePath: String? = null,
        archiveInternalPath: String = "",
        pathHistory: List<String>? = null,
        historyIndex: Int? = null,
    ): List<BrowserTab> {
        val tab = tabs.getOrNull(selectedIndex) ?: return tabs
        val updated = tab.copy(
            path = path,
            insideArchive = insideArchive,
            archivePath = archivePath,
            archiveInternalPath = archiveInternalPath,
            pathHistory = pathHistory ?: tab.pathHistory,
            historyIndex = historyIndex ?: tab.historyIndex,
        )
        return tabs.toMutableList().apply { set(selectedIndex, updated) }
    }


    private fun loadVolumes() { _state.update { it.copy(volumes = storageVolumeHelper.getStorageVolumes()) } }
    fun refreshUsbStorage() {
        if (_state.value.isRefreshingUsb) return
        viewModelScope.launch {
            _state.update { it.copy(isRefreshingUsb = true) }
            val devices = usbStorageManager.connectedDevices()
            val roots = usbStorageManager.savedRoots()
            _state.update { it.copy(usbDevices = devices, usbRoots = roots, isRefreshingUsb = false) }
        }
    }

    fun addUsbTree(uri: Uri) {
        viewModelScope.launch {
            usbStorageManager.saveTree(uri)
                .onSuccess { root ->
                    _events.emit(BrowserEvent.Toast("USB storage connected: ${root.name}"))
                    _state.update { it.copy(usbRoots = (it.usbRoots + root).distinctBy(UsbStorageRoot::path)) }
                    navigateTo(root.path)
                }
                .onFailure { error -> _events.emit(BrowserEvent.Toast("USB access failed: ${error.message}")) }
        }
    }
    private fun trashVolumeRoots(): List<String> = storageVolumeHelper.getStorageVolumes().map { it.path }.distinct()
    private fun observeBookmarks() { viewModelScope.launch { bookmarkDao.getAllFlow().collect { b -> _state.update { it.copy(bookmarks = b) } } } }
    private fun observeRecentLocations() {
        viewModelScope.launch {
            recentLocationDao.getRecentFlow().collect { locations ->
                _state.update { it.copy(recentLocations = locations) }
            }
        }
    }
    private fun observeSavedSearches() {
        viewModelScope.launch {
            savedSearchDao.getAllFlow().collect { searches -> _state.update { it.copy(savedSearches = searches) } }
        }
    }
    private suspend fun recordRecentLocation(path: String) {
        val normalizedPath = path.trimEnd('/').ifBlank { "/" }
        recentLocationDao.upsert(
            RecentLocationEntity(
                path = normalizedPath,
                name = normalizedPath.substringAfterLast('/').ifBlank { normalizedPath },
            ),
        )
    }
    private fun observeTags() { viewModelScope.launch { tagRepository.tags.collect { tags -> _state.update { it.copy(tags = tags) } } } }
    private fun getSelectedFileItems(): List<FileItem> { val s = _state.value.selectedItems; return _state.value.files.filter { it.path in s } }

    private data class StagedRename(
        val preview: com.explorer.fileexplorer.core.data.BatchRenamePreviewItem,
        val temporaryPath: String,
        var finalized: Boolean = false,
    )

    private fun sortFiles(files: List<FileItem>, order: SortOrder, showHidden: Boolean): List<FileItem> {
        val filtered = if (showHidden) files else files.filter { !it.isHidden }
        val cmp = compareBy<FileItem> { if (order.foldersFirst) if (it.isDirectory) 0 else 1 else 0 }
            .thenBy(when (order.field) {
                SortField.NAME -> compareBy<FileItem> { it.name.lowercase() }
                SortField.SIZE -> compareBy<FileItem> { it.size }
                SortField.DATE -> compareBy<FileItem> { it.lastModified }
                SortField.TYPE -> compareBy<FileItem> { it.extension.lowercase() }
            }.let { c -> if (order.direction == SortDirection.DESCENDING) c.reversed() else c }) { it }
        return filtered.sortedWith(cmp)
    }
}

private fun <T> Comparator<T>.thenBy(other: Comparator<T>, selector: (T) -> T): Comparator<T> {
    return Comparator { a, b -> val r = this.compare(a, b); if (r != 0) r else other.compare(selector(a), selector(b)) }
}
