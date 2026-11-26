package com.explorer.fileexplorer.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.AccentCyanDark
import com.explorer.fileexplorer.core.designsystem.AccentGreen
import com.explorer.fileexplorer.core.designsystem.AccentOrange
import com.explorer.fileexplorer.core.designsystem.AccentPurple
import com.explorer.fileexplorer.core.designsystem.AccentRed
import com.explorer.fileexplorer.core.designsystem.ColorApk
import com.explorer.fileexplorer.core.designsystem.ColorAudio
import com.explorer.fileexplorer.core.designsystem.ColorDocument
import com.explorer.fileexplorer.core.designsystem.ColorImage
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.model.FileItem

@Composable
fun FileIcon(
    item: FileItem,
    modifier: Modifier = Modifier,
) {
    val (icon, containerColor, contentColor) = getExpressiveFileIconStyle(item)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier.size(38.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(
                    if (item.isDirectory) DesignSystemR.string.folder_icon_description
                    else DesignSystemR.string.file_icon_description,
                    item.extension.ifBlank { item.mimeType },
                ),
                modifier = Modifier.fillMaxSize(0.54f),
            )
        }
    }
}

@Composable
private fun getExpressiveFileIconStyle(item: FileItem): Triple<ImageVector, Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when {
        item.isDirectory -> Triple(Icons.Rounded.Folder, scheme.primaryContainer, scheme.onPrimaryContainer)
        item.isSymlink -> Triple(Icons.Rounded.Link, scheme.tertiaryContainer, scheme.onTertiaryContainer)
        item.isApk -> Triple(Icons.Rounded.Android, ColorApk.copy(alpha = 0.16f), ColorApk)
        item.isImage -> Triple(Icons.Rounded.Image, ColorImage.copy(alpha = 0.15f), ColorImage)
        item.isVideo -> Triple(Icons.Rounded.VideoFile, AccentOrange.copy(alpha = 0.16f), AccentOrange)
        item.isAudio -> Triple(Icons.Rounded.AudioFile, ColorAudio.copy(alpha = 0.16f), ColorAudio)
        item.isArchive -> Triple(Icons.Rounded.FolderZip, AccentOrange.copy(alpha = 0.16f), AccentOrange)
        item.mimeType.contains("pdf") -> Triple(Icons.Rounded.PictureAsPdf, AccentRed.copy(alpha = 0.15f), AccentRed)
        item.mimeType.contains("spreadsheet") || item.extension in setOf("xlsx", "xls", "csv") ->
            Triple(Icons.Rounded.TableChart, AccentGreen.copy(alpha = 0.16f), AccentGreen)
        item.mimeType.contains("presentation") || item.extension in setOf("pptx", "ppt") ->
            Triple(Icons.Rounded.Slideshow, AccentOrange.copy(alpha = 0.16f), AccentOrange)
        item.mimeType.contains("document") || item.extension in setOf("docx", "doc") ->
            Triple(Icons.AutoMirrored.Rounded.Article, ColorDocument.copy(alpha = 0.16f), ColorDocument)
        item.extension in setOf("db", "sqlite", "sqlite3") ->
            Triple(Icons.Rounded.Storage, AccentPurple.copy(alpha = 0.18f), AccentPurple)
        item.extension in setOf("json", "xml", "yaml", "yml", "toml") ->
            Triple(Icons.Rounded.DataObject, AccentCyanDark.copy(alpha = 0.16f), scheme.primary)
        item.isText -> Triple(Icons.Rounded.Description, scheme.secondaryContainer, scheme.onSecondaryContainer)
        else -> Triple(Icons.AutoMirrored.Rounded.InsertDriveFile, scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
    }
}
