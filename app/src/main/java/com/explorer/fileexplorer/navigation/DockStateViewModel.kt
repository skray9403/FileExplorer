package com.explorer.fileexplorer.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explorer.fileexplorer.core.data.ClipboardQueueRepository
import com.explorer.fileexplorer.core.model.QueuedClipboardTask
import com.explorer.fileexplorer.feature.transfer.TransferConflictAction
import com.explorer.fileexplorer.feature.transfer.TransferQueueManager
import com.explorer.fileexplorer.feature.transfer.TransferQueueState
import com.explorer.fileexplorer.feature.transfer.TransferQueueTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DockTaskSnapshot(
    val activeTransfers: List<TransferQueueTask> = emptyList(),
    val queuedClipboardTasks: List<QueuedClipboardTask> = emptyList(),
    val aggregateProgress: Float? = null,
    val hasConflict: Boolean = false,
    val totalBadgeCount: Int = 0,
)

@HiltViewModel
class DockStateViewModel @Inject constructor(
    private val transferQueueManager: TransferQueueManager,
    private val clipboardQueueRepository: ClipboardQueueRepository,
) : ViewModel() {

    val snapshot: StateFlow<DockTaskSnapshot> = combine(
        transferQueueManager.tasks,
        clipboardQueueRepository.tasks,
    ) { transfers, clipboardTasks ->
        val active = transfers.filterNot { it.isTerminal }
        val hasConflict = active.any { it.state == TransferQueueState.WAITING_CONFLICT }
        val runningTransfers = active.filter { it.state == TransferQueueState.RUNNING }

        val totalBytes = runningTransfers.sumOf { it.totalBytes }
        val transferredBytes = runningTransfers.sumOf { it.transferredBytes }

        val aggregateProgress = if (runningTransfers.isNotEmpty() && totalBytes > 0L) {
            (transferredBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
        } else if (runningTransfers.isNotEmpty()) {
            0f
        } else {
            null
        }

        DockTaskSnapshot(
            activeTransfers = active,
            queuedClipboardTasks = clipboardTasks,
            aggregateProgress = aggregateProgress,
            hasConflict = hasConflict,
            totalBadgeCount = active.size + clipboardTasks.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DockTaskSnapshot(),
    )

    fun pauseTransfer(id: Long) = transferQueueManager.pause(id)
    fun resumeTransfer(id: Long) = transferQueueManager.resume(id)
    fun cancelTransfer(id: Long) = transferQueueManager.cancel(id)
    fun retryTransfer(id: Long) = transferQueueManager.retry(id)
    fun resolveConflict(id: Long, action: TransferConflictAction, applyToAll: Boolean) =
        transferQueueManager.resolveConflict(id, action, applyToAll)

    fun dismissClipboardTask(id: Long) = clipboardQueueRepository.remove(id)
    fun clearClipboardQueue() = clipboardQueueRepository.clear()
}
