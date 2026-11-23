package com.explorer.fileexplorer.core.data

import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.core.model.FileOperation
import com.explorer.fileexplorer.core.model.QueuedClipboardTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide MiXplorer-style stacked clipboard queue shared across tabs and screens.
 */
@Singleton
class ClipboardQueueRepository @Inject constructor() {

    private val nextId = AtomicLong(1L)
    private val _tasks = MutableStateFlow<List<QueuedClipboardTask>>(emptyList())
    val tasks: StateFlow<List<QueuedClipboardTask>> = _tasks.asStateFlow()

    fun enqueue(
        operation: FileOperation,
        items: List<FileItem>,
        sourcePath: String,
        archivePath: String? = null,
        archiveEntries: List<String>? = null,
    ): QueuedClipboardTask {
        require(items.isNotEmpty() || archivePath != null) { "Clipboard task requires items or an archive path" }
        val itemKeys = items.mapTo(linkedSetOf()) { it.path }
        val task = QueuedClipboardTask(
            id = nextId.getAndIncrement(),
            operation = operation,
            items = items,
            sourcePath = sourcePath,
            archivePath = archivePath,
            archiveEntries = archiveEntries,
        )
        _tasks.update { existing ->
            val deduplicated = existing.filterNot { candidate ->
                candidate.operation == operation &&
                    candidate.archivePath == archivePath &&
                    candidate.archiveEntries == archiveEntries &&
                    candidate.items.mapTo(hashSetOf()) { it.path } == itemKeys
            }
            deduplicated + task
        }
        return task
    }

    fun remove(taskId: Long) {
        _tasks.update { list -> list.filterNot { it.id == taskId } }
    }

    fun pop(taskId: Long): QueuedClipboardTask? {
        var popped: QueuedClipboardTask? = null
        _tasks.update { list ->
            popped = list.firstOrNull { it.id == taskId }
            if (popped == null) list else list.filterNot { it.id == taskId }
        }
        return popped
    }

    fun popLatest(): QueuedClipboardTask? {
        var popped: QueuedClipboardTask? = null
        _tasks.update { list ->
            if (list.isEmpty()) list
            else {
                popped = list.last()
                list.dropLast(1)
            }
        }
        return popped
    }

    fun clear() {
        _tasks.value = emptyList()
    }
}
