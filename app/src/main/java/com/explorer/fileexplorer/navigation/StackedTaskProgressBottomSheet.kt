package com.explorer.fileexplorer.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileItem
import com.explorer.fileexplorer.feature.transfer.TransferConflictAction
import com.explorer.fileexplorer.feature.transfer.TransferQueueState
import com.explorer.fileexplorer.feature.transfer.TransferQueueTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackedTaskProgressBottomSheet(
    snapshot: DockTaskSnapshot,
    viewModel: DockStateViewModel,
    onExecuteClipboardTask: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(DesignSystemR.string.tasks_and_progress),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (snapshot.queuedClipboardTasks.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearClipboardQueue() }) {
                            Text(stringResource(DesignSystemR.string.clear_all))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(DesignSystemR.string.clear),
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            )

            if (snapshot.activeTransfers.isEmpty() && snapshot.queuedClipboardTasks.isEmpty()) {
                Text(
                    text = stringResource(DesignSystemR.string.no_active_tasks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                ) {
                    if (snapshot.activeTransfers.isNotEmpty()) {
                        item(key = "transfers_header") {
                            Text(
                                text = stringResource(DesignSystemR.string.active_transfers),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        items(snapshot.activeTransfers, key = { "transfer_${it.id}" }) { task ->
                            TransferTaskItem(
                                task = task,
                                onPause = { viewModel.pauseTransfer(task.id) },
                                onResume = { viewModel.resumeTransfer(task.id) },
                                onCancel = { viewModel.cancelTransfer(task.id) },
                                onRetry = { viewModel.retryTransfer(task.id) },
                                onResolveConflict = { action, applyAll ->
                                    viewModel.resolveConflict(task.id, action, applyAll)
                                },
                            )
                        }
                    }

                    if (snapshot.queuedClipboardTasks.isNotEmpty()) {
                        item(key = "clipboard_header") {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(DesignSystemR.string.queued_clipboard_actions),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        items(snapshot.queuedClipboardTasks, key = { "clipboard_${it.id}" }) { clipTask ->
                            ClipboardTaskItem(
                                task = clipTask,
                                onExecute = { onExecuteClipboardTask?.invoke(clipTask.id) },
                                onDismiss = { viewModel.dismissClipboardTask(clipTask.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferTaskItem(
    task: TransferQueueTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onResolveConflict: (TransferConflictAction, Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = task.sourcePath.trimEnd('/').substringAfterLast('/').ifBlank { task.operation.name },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (task.state) {
                        TransferQueueState.RUNNING -> {
                            IconButton(onClick = onPause, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Pause, stringResource(DesignSystemR.string.pause), modifier = Modifier.size(18.dp))
                            }
                        }
                        TransferQueueState.PAUSED -> {
                            IconButton(onClick = onResume, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.PlayArrow, stringResource(DesignSystemR.string.resume), modifier = Modifier.size(18.dp))
                            }
                        }
                        TransferQueueState.FAILED -> {
                            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Refresh, stringResource(DesignSystemR.string.retry), modifier = Modifier.size(18.dp))
                            }
                        }
                        else -> Unit
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, stringResource(DesignSystemR.string.cancel), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            val progress = if (task.totalBytes > 0) {
                (task.transferredBytes.toFloat() / task.totalBytes).coerceIn(0f, 1f)
            } else 0f

            if (task.state == TransferQueueState.RUNNING || task.state == TransferQueueState.PAUSED) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${FileItem.formatSize(task.transferredBytes)} / ${FileItem.formatSize(task.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (task.state == TransferQueueState.WAITING_CONFLICT) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(DesignSystemR.string.resolve_conflict),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { onResolveConflict(TransferConflictAction.SKIP, false) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(DesignSystemR.string.skip))
                    }
                    FilledTonalButton(
                        onClick = { onResolveConflict(TransferConflictAction.REPLACE, false) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(DesignSystemR.string.overwrite))
                    }
                    FilledTonalButton(
                        onClick = { onResolveConflict(TransferConflictAction.KEEP_BOTH, false) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(DesignSystemR.string.keep_both))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardTaskItem(
    task: com.explorer.fileexplorer.core.model.QueuedClipboardTask,
    onExecute: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (task.isExtract) Icons.Rounded.Unarchive else Icons.Rounded.ContentPaste,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (task.isExtract) {
                        stringResource(DesignSystemR.string.queue_extract)
                    } else {
                        task.operation.name
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${task.items.size} ${task.sourcePath}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onExecute,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Text(
                    text = if (task.isExtract) {
                        stringResource(DesignSystemR.string.extract_here)
                    } else {
                        stringResource(DesignSystemR.string.paste_here)
                    },
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(DesignSystemR.string.dismiss),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
