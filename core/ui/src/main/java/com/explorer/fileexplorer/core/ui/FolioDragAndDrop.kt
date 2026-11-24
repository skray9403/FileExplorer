package com.explorer.fileexplorer.core.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.core.content.FileProvider
import com.explorer.fileexplorer.core.model.FileItem
import java.io.File

data class FolioDragPayload(
    val items: List<FileItem>,
    val sourcePath: String,
) {
    val containsOnlyFolders: Boolean get() = items.isNotEmpty() && items.all { it.isDirectory }
    val containsOnlyLocalFiles: Boolean
        get() = items.isNotEmpty() && items.all { !it.isDirectory && it.path.startsWith("/") }
}

fun buildFolioDragTransferData(
    context: Context,
    items: List<FileItem>,
    sourcePath: String,
): DragAndDropTransferData? {
    if (items.isEmpty()) return null
    val payload = FolioDragPayload(items = items, sourcePath = sourcePath)
    if (payload.containsOnlyLocalFiles) {
        val uris = items.mapNotNull { item ->
            runCatching {
                item.uri ?: FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(item.path),
                )
            }.getOrNull()
        }
        if (uris.isNotEmpty()) {
            val clip = ClipData.newUri(context.contentResolver, items.first().name, uris.first()).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
            return DragAndDropTransferData(
                clipData = clip,
                flags = View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ,
                localState = payload,
            )
        }
    }
    val clip = ClipData.newPlainText(
        "folio_items",
        items.joinToString("\n") { it.path },
    )
    return DragAndDropTransferData(
        clipData = clip,
        flags = 0,
        localState = payload,
    )
}

fun DragAndDropEvent.folioPayloadOrNull(): FolioDragPayload? =
    toAndroidDragEvent().localState as? FolioDragPayload

fun DragAndDropEvent.externalClipUris(): List<Uri> {
    val dragEvent = toAndroidDragEvent()
    if (dragEvent.localState is FolioDragPayload) return emptyList()
    val clip = dragEvent.clipData ?: return emptyList()
    return buildList {
        for (i in 0 until clip.itemCount) {
            clip.getItemAt(i)?.uri?.let(::add)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.folioDragSource(
    enabled: Boolean,
    context: Context,
    itemsProvider: () -> List<FileItem>,
    sourcePath: String,
    onDragStarted: () -> Unit = {},
): Modifier {
    if (!enabled) return this
    return this.dragAndDropSource(
        transferData = { _ ->
            val items = itemsProvider()
            buildFolioDragTransferData(context, items, sourcePath)?.also {
                onDragStarted()
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.folioDropTarget(
    acceptsExternal: Boolean = false,
    shouldStart: (DragAndDropEvent) -> Boolean = { event ->
        event.folioPayloadOrNull() != null ||
            (acceptsExternal && event.mimeTypes().any { it != ClipDescription.MIMETYPE_TEXT_PLAIN })
    },
    onHoverChanged: (Boolean) -> Unit = {},
    onDrop: (DragAndDropEvent) -> Boolean,
): Modifier = this.dragAndDropTarget(
    shouldStartDragAndDrop = shouldStart,
    target = object : DragAndDropTarget {
        override fun onEntered(event: DragAndDropEvent) = onHoverChanged(true)
        override fun onExited(event: DragAndDropEvent) = onHoverChanged(false)
        override fun onEnded(event: DragAndDropEvent) = onHoverChanged(false)
        override fun onDrop(event: DragAndDropEvent): Boolean {
            onHoverChanged(false)
            return onDrop(event)
        }
    },
)

