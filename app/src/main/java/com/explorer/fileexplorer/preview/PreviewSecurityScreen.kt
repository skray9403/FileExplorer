package com.explorer.fileexplorer.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.explorer.fileexplorer.core.designsystem.FolioBottomScrim
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBottomBar
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR

@Composable
fun PreviewSecurityScreen(
    onNavigateBack: () -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()
    var prevIdx by remember { mutableIntStateOf(0) }
    var prevOff by remember { mutableIntStateOf(0) }
    var isScrollingDown by remember { mutableStateOf(false) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val idx = listState.firstVisibleItemIndex
        val off = listState.firstVisibleItemScrollOffset
        isScrollingDown = when {
            idx == 0 && off == 0 -> false
            idx != prevIdx -> idx > prevIdx
            off != prevOff -> off > prevOff
            else -> isScrollingDown
        }
        prevIdx = idx
        prevOff = off
    }

    var appLockEnabled by remember { mutableStateOf(true) }
    var appLockTimeoutSeconds by remember { mutableIntStateOf(60) }
    var invalidateOnBioChange by remember { mutableStateOf(false) }
    var vaultEnabled by remember { mutableStateOf(true) }
    var vaultUnlocked by remember { mutableStateOf(false) }
    var secureDeleteEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarTop + 16.dp,
                bottom = 128.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "sec_app_lock") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.app_lock))
                    val lockRows = if (appLockEnabled) 3 else 2
                    var lockIdx = 0
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = lockIdx++, count = lockRows) {
                            SecurityPreviewSwitchRow(
                                icon = Icons.Rounded.Fingerprint,
                                title = stringResource(DesignSystemR.string.biometric_lock),
                                subtitle = stringResource(DesignSystemR.string.require_biometric),
                                checked = appLockEnabled,
                                onCheckedChange = { appLockEnabled = it },
                            )
                        }
                        if (appLockEnabled) {
                            FolioSegmentedCard(index = lockIdx++, count = lockRows) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = stringResource(DesignSystemR.string.app_lock_timeout),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        val options = listOf(
                                            0 to DesignSystemR.string.timeout_immediate,
                                            30 to DesignSystemR.string.timeout_30s,
                                            60 to DesignSystemR.string.timeout_1m,
                                            300 to DesignSystemR.string.timeout_5m,
                                            900 to DesignSystemR.string.timeout_15m,
                                        )
                                        options.forEach { (secs, labelRes) ->
                                            FilterChip(
                                                selected = appLockTimeoutSeconds == secs,
                                                onClick = { appLockTimeoutSeconds = secs },
                                                label = { Text(stringResource(labelRes)) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        FolioSegmentedCard(index = lockIdx, count = lockRows) {
                            SecurityPreviewSwitchRow(
                                icon = Icons.Rounded.LockReset,
                                title = stringResource(DesignSystemR.string.invalidate_on_biometric_change),
                                subtitle = stringResource(DesignSystemR.string.invalidate_on_biometric_change_desc),
                                checked = invalidateOnBioChange,
                                onCheckedChange = { invalidateOnBioChange = it },
                            )
                        }
                    }
                }
            }

            item(key = "sec_vault") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.encrypted_vault))
                    val rows = if (vaultEnabled) 2 else 1
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = rows) {
                            SecurityPreviewSwitchRow(
                                icon = Icons.Rounded.Lock,
                                title = stringResource(DesignSystemR.string.enable_vault),
                                subtitle = stringResource(DesignSystemR.string.vault_description),
                                checked = vaultEnabled,
                                onCheckedChange = { vaultEnabled = it },
                            )
                        }
                        if (vaultEnabled) {
                            FolioSegmentedCard(index = 1, count = rows) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (vaultUnlocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = stringResource(if (vaultUnlocked) DesignSystemR.string.vault_lock else DesignSystemR.string.vault_unlock),
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    }
                                    Button(onClick = { vaultUnlocked = !vaultUnlocked }) {
                                        Text(stringResource(if (vaultUnlocked) DesignSystemR.string.vault_lock else DesignSystemR.string.vault_unlock))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "sec_file_security") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.file_security))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 3) {
                            SecurityPreviewSwitchRow(
                                icon = Icons.Rounded.DeleteForever,
                                title = stringResource(DesignSystemR.string.secure_delete),
                                subtitle = stringResource(DesignSystemR.string.secure_delete_description),
                                checked = secureDeleteEnabled,
                                onCheckedChange = { secureDeleteEnabled = it },
                            )
                        }
                        FolioSegmentedCard(index = 1, count = 3) {
                            SecurityPreviewStaticRow(
                                icon = Icons.Rounded.EnhancedEncryption,
                                title = stringResource(DesignSystemR.string.file_encryption),
                                subtitle = stringResource(DesignSystemR.string.file_encryption_description),
                            )
                        }
                        FolioSegmentedCard(index = 2, count = 3) {
                            SecurityPreviewStaticRow(
                                icon = Icons.Rounded.VerifiedUser,
                                title = stringResource(DesignSystemR.string.checksum_verification),
                                subtitle = stringResource(DesignSystemR.string.checksum_description),
                            )
                        }
                    }
                }
            }

            item(key = "sec_model") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.security_model))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 1) {
                            SecurityPreviewStaticRow(
                                icon = Icons.Rounded.Info,
                                title = stringResource(DesignSystemR.string.hardware_security_strongbox),
                                subtitle = stringResource(DesignSystemR.string.security_model_description),
                            )
                        }
                    }
                }
            }
        }

        FolioTopScrim(
            height = statusBarTop + 16.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        FolioBottomScrim(
            height = 112.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        FolioSubScreenBottomBar(
            title = stringResource(DesignSystemR.string.security),
            onNavigateBack = onNavigateBack,
            onFabClick = null,
            scrollCompact = isScrollingDown,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SecurityPreviewSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SecurityPreviewStaticRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
