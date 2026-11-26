package com.explorer.fileexplorer.preview

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector
import com.explorer.fileexplorer.core.designsystem.FolioAccentPalette
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.designsystem.ThemeMode

enum class PreviewRoute {
    BROWSER,
    COLLECTIONS,
    SETTINGS,
    SECURITY,
    APPS,
    TRASH,
    NETWORK,
    CLOUD,
    STORAGE_ANALYZER,
}

enum class PreviewSortField { NAME, SIZE, DATE, TYPE }
enum class PreviewSortDirection { ASCENDING, DESCENDING }
enum class PreviewViewMode { LIST, GRID }
enum class PreviewFileColumn(val label: String) {
    SIZE("Size"),
    DATE("Date"),
    TYPE("Type"),
}

data class PreviewAppearanceState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val accentPalette: FolioAccentPalette = FolioAccentPalette.SAGE,
    val cornerStyle: String = "ROUNDED",
)

data class PreviewFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val sizeLabel: String = "",
    val dateLabel: String = "Nov 26",
    val extension: String = "",
    val isHidden: Boolean = false,
    val isEncrypted: Boolean = false,
)

data class PreviewTab(
    val id: Long,
    val path: String,
)

data class PreviewCategory(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val count: Int,
    val files: List<PreviewFileItem>,
)

data class PreviewAppItem(
    val name: String,
    val packageName: String,
    val version: String,
    val sizeLabel: String,
    val isSystem: Boolean = false,
)

data class PreviewTrashItem(
    val id: String,
    val name: String,
    val originalPath: String,
    val deletedWhen: String,
    val sizeLabel: String,
)

data class PreviewConnection(
    val id: String,
    val name: String,
    val protocol: String,
    val host: String,
    val port: Int,
    val connected: Boolean,
    val icon: ImageVector,
)

data class PreviewCloudAccount(
    val id: String,
    val serviceName: String,
    val email: String,
    val usedGb: Float,
    val totalGb: Float,
    val icon: ImageVector,
)

data class PreviewStorageSegment(
    val name: String,
    val sizeLabel: String,
    val fraction: Float,
)

object PreviewMockData {
    const val ROOT_PATH = "/storage/emulated/0"

    private val allDirectories: Map<String, List<PreviewFileItem>> = mapOf(
        ROOT_PATH to listOf(
            PreviewFileItem("Documents", "$ROOT_PATH/Documents", true, sizeLabel = "14 items", dateLabel = "Nov 26"),
            PreviewFileItem("Downloads", "$ROOT_PATH/Downloads", true, sizeLabel = "9 items", dateLabel = "Nov 26"),
            PreviewFileItem("DCIM", "$ROOT_PATH/DCIM", true, sizeLabel = "128 items", dateLabel = "Nov 25"),
            PreviewFileItem("Music", "$ROOT_PATH/Music", true, sizeLabel = "34 items", dateLabel = "Nov 24"),
            PreviewFileItem("Projects", "$ROOT_PATH/Projects", true, sizeLabel = "6 items", dateLabel = "Nov 26"),
            PreviewFileItem(".nomedia_cache", "$ROOT_PATH/.nomedia_cache", true, sizeLabel = "2 items", dateLabel = "Nov 20", isHidden = true),
            PreviewFileItem("VaultBackup.encrypted", "$ROOT_PATH/VaultBackup.encrypted", false, 48_200_000, "46.0 MB", "Nov 26", "encrypted", isEncrypted = true),
            PreviewFileItem("Folio_Architecture_2026.pdf", "$ROOT_PATH/Folio_Architecture_2026.pdf", false, 3_420_000, "3.3 MB", "Nov 26", "pdf"),
            PreviewFileItem("Folio-v1.6.2-preview.apk", "$ROOT_PATH/Folio-v1.6.2-preview.apk", false, 18_500_000, "17.6 MB", "Nov 26", "apk"),
            PreviewFileItem("Release_Assets.zip", "$ROOT_PATH/Release_Assets.zip", false, 12_100_000, "11.5 MB", "Nov 25", "zip"),
        ),
        "$ROOT_PATH/Documents" to listOf(
            PreviewFileItem("Tax_2026", "$ROOT_PATH/Documents/Tax_2026", true, sizeLabel = "4 items", dateLabel = "Nov 24"),
            PreviewFileItem("SecretDocs.encrypted", "$ROOT_PATH/Documents/SecretDocs.encrypted", false, 9_400_000, "9.0 MB", "Nov 26", "encrypted", isEncrypted = true),
            PreviewFileItem("Design_Spec_M3.docx", "$ROOT_PATH/Documents/Design_Spec_M3.docx", false, 1_240_000, "1.2 MB", "Nov 26", "docx"),
            PreviewFileItem("Budget_Q4.xlsx", "$ROOT_PATH/Documents/Budget_Q4.xlsx", false, 640_000, "625 KB", "Nov 23", "xlsx"),
            PreviewFileItem("Notes.md", "$ROOT_PATH/Documents/Notes.md", false, 18_400, "18 KB", "Nov 26", "md"),
        ),
        "$ROOT_PATH/Documents/Tax_2026" to listOf(
            PreviewFileItem("W2_Form.pdf", "$ROOT_PATH/Documents/Tax_2026/W2_Form.pdf", false, 412_000, "402 KB", "Nov 20", "pdf"),
            PreviewFileItem("Receipts.zip", "$ROOT_PATH/Documents/Tax_2026/Receipts.zip", false, 5_100_000, "4.9 MB", "Nov 19", "zip"),
        ),
        "$ROOT_PATH/Downloads" to listOf(
            PreviewFileItem("gradle-9.5.0-bin.zip", "$ROOT_PATH/Downloads/gradle-9.5.0-bin.zip", false, 142_000_000, "135 MB", "Nov 26", "zip"),
            PreviewFileItem("pixel_wallpaper_4k.png", "$ROOT_PATH/Downloads/pixel_wallpaper_4k.png", false, 8_200_000, "7.8 MB", "Nov 25", "png"),
            PreviewFileItem("Android_XR_Demo.mp4", "$ROOT_PATH/Downloads/Android_XR_Demo.mp4", false, 64_000_000, "61 MB", "Nov 24", "mp4"),
            PreviewFileItem("Compose_Guide.epub", "$ROOT_PATH/Downloads/Compose_Guide.epub", false, 4_800_000, "4.6 MB", "Nov 22", "epub"),
        ),
        "$ROOT_PATH/DCIM" to listOf(
            PreviewFileItem("Camera", "$ROOT_PATH/DCIM/Camera", true, sizeLabel = "112 items", dateLabel = "Nov 26"),
            PreviewFileItem("Screenshots", "$ROOT_PATH/DCIM/Screenshots", true, sizeLabel = "16 items", dateLabel = "Nov 26"),
        ),
        "$ROOT_PATH/DCIM/Camera" to listOf(
            PreviewFileItem("PXL_20261126_102201.jpg", "$ROOT_PATH/DCIM/Camera/PXL_20261126_102201.jpg", false, 4_500_000, "4.3 MB", "Nov 26", "jpg"),
            PreviewFileItem("PXL_20261125_184012.mp4", "$ROOT_PATH/DCIM/Camera/PXL_20261125_184012.mp4", false, 38_000_000, "36 MB", "Nov 25", "mp4"),
        ),
        "$ROOT_PATH/DCIM/Screenshots" to listOf(
            PreviewFileItem("Screenshot_Folio_Dock.png", "$ROOT_PATH/DCIM/Screenshots/Screenshot_Folio_Dock.png", false, 980_000, "957 KB", "Nov 26", "png"),
        ),
        "$ROOT_PATH/Music" to listOf(
            PreviewFileItem("Midnight_Synth.flac", "$ROOT_PATH/Music/Midnight_Synth.flac", false, 31_200_000, "29.8 MB", "Nov 24", "flac"),
            PreviewFileItem("Focus_Mix.mp3", "$ROOT_PATH/Music/Focus_Mix.mp3", false, 9_800_000, "9.3 MB", "Nov 21", "mp3"),
        ),
        "$ROOT_PATH/Projects" to listOf(
            PreviewFileItem("MainActivity.kt", "$ROOT_PATH/Projects/MainActivity.kt", false, 8_200, "8 KB", "Nov 26", "kt"),
            PreviewFileItem("FolioMorphingBottomBar.kt", "$ROOT_PATH/Projects/FolioMorphingBottomBar.kt", false, 29_400, "28.7 KB", "Nov 26", "kt"),
            PreviewFileItem("libs.versions.toml", "$ROOT_PATH/Projects/libs.versions.toml", false, 3_900, "3.8 KB", "Nov 26", "toml"),
        ),
    )

    fun filesFor(path: String): List<PreviewFileItem> =
        allDirectories[path] ?: allDirectories[ROOT_PATH].orEmpty()

    fun allFilesFlat(): List<PreviewFileItem> =
        allDirectories.values.flatten().distinctBy { it.path }

    val categories: List<PreviewCategory> = listOf(
        PreviewCategory("downloads", DesignSystemR.string.downloads, Icons.Rounded.Download, 4, filesFor("$ROOT_PATH/Downloads")),
        PreviewCategory("images", DesignSystemR.string.images, Icons.Rounded.Image, 3, allFilesFlat().filter { it.extension in setOf("png", "jpg") }),
        PreviewCategory("videos", DesignSystemR.string.videos, Icons.Rounded.Movie, 2, allFilesFlat().filter { it.extension == "mp4" }),
        PreviewCategory("audio", DesignSystemR.string.audio, Icons.Rounded.AudioFile, 2, filesFor("$ROOT_PATH/Music")),
        PreviewCategory("documents", DesignSystemR.string.documents, Icons.Rounded.Description, 5, allFilesFlat().filter { it.extension in setOf("pdf", "docx", "xlsx", "epub", "md") }),
        PreviewCategory("apks", DesignSystemR.string.apks, Icons.Rounded.Android, 1, allFilesFlat().filter { it.extension == "apk" }),
        PreviewCategory("archives", DesignSystemR.string.archives, Icons.Rounded.FolderZip, 3, allFilesFlat().filter { it.extension == "zip" }),
    )

    val apps: List<PreviewAppItem> = listOf(
        PreviewAppItem("Folio File Explorer", "com.explorer.fileexplorer", "1.6.2", "18.5 MB"),
        PreviewAppItem("Ketch Download Manager", "com.ketch.downloader", "2.1.0", "12.4 MB"),
        PreviewAppItem("Pixel Gallery", "com.google.android.apps.photos", "7.12", "142 MB", isSystem = true),
        PreviewAppItem("Termux", "com.termux", "0.118.1", "96.2 MB"),
        PreviewAppItem("Material Catalog", "androidx.compose.material3.catalog", "1.5.0", "24.8 MB"),
        PreviewAppItem("Shizuku", "moe.shizuku.privileged.api", "13.5.5", "4.1 MB"),
    )

    val trashItems: List<PreviewTrashItem> = listOf(
        PreviewTrashItem("t1", "old_draft_notes.txt", "$ROOT_PATH/Documents/old_draft_notes.txt", "2 hours ago", "14 KB"),
        PreviewTrashItem("t2", "IMG_20261120_blur.jpg", "$ROOT_PATH/DCIM/Camera/IMG_20261120_blur.jpg", "1 day ago", "3.8 MB"),
        PreviewTrashItem("t3", "temp_build_logs.zip", "$ROOT_PATH/Downloads/temp_build_logs.zip", "3 days ago", "22.4 MB"),
    )

    val connections: List<PreviewConnection> = listOf(
        PreviewConnection("c1", "Home NAS (Synology)", "smb", "192.168.1.42", 445, true, Icons.Rounded.Dns),
        PreviewConnection("c2", "Workstation SFTP", "sftp", "10.0.0.18", 22, false, Icons.Rounded.Security),
        PreviewConnection("c3", "Media WebDAV", "webdav", "nas.local", 5006, false, Icons.Rounded.Cloud),
        PreviewConnection("c4", "Local FTP", "ftp", "192.168.1.90", 2121, false, Icons.Rounded.Lan),
    )

    val cloudAccounts: List<PreviewCloudAccount> = listOf(
        PreviewCloudAccount("cl1", "Google Drive", "skray9403@gmail.com", 42.6f, 200f, Icons.Rounded.CloudCircle),
        PreviewCloudAccount("cl2", "Dropbox", "work@folio.dev", 8.2f, 50f, Icons.Rounded.CloudUpload),
    )

    val storageSegments: List<PreviewStorageSegment> = listOf(
        PreviewStorageSegment("DCIM & Media", "42.8 GB", 0.44f),
        PreviewStorageSegment("Downloads", "24.1 GB", 0.25f),
        PreviewStorageSegment("Apps & Data", "18.4 GB", 0.19f),
        PreviewStorageSegment("Documents & Archives", "11.7 GB", 0.12f),
    )
}
