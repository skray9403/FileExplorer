package com.explorer.fileexplorer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.explorer.fileexplorer.core.designsystem.FileExplorerTheme
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import com.explorer.fileexplorer.core.designsystem.ThemeMode
import com.explorer.fileexplorer.core.storage.PermissionHelper
import com.explorer.fileexplorer.feature.security.BiometricHelper
import com.explorer.fileexplorer.feature.security.SecurityRepository
import com.explorer.fileexplorer.feature.security.SecuritySettings
import com.explorer.fileexplorer.feature.settings.SettingsRepository
import com.explorer.fileexplorer.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var permissionHelper: PermissionHelper
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var securityRepository: SecurityRepository
    @Inject lateinit var biometricHelper: BiometricHelper

    private val hasPermissionState = mutableStateOf(false)
    private val limitedStorageState = mutableStateOf(false)
    private val appUnlockedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hasPermissionState.value = permissionHelper.hasFullStorageAccess()

        setContent {
            val themeModeFlow = remember(settingsRepository) {
                settingsRepository.settings.map { it.themeMode }
            }
            val themeMode by themeModeFlow
                .collectAsStateWithLifecycle(initialValue = ThemeMode.DYNAMIC)
            val securitySettings by securityRepository.settings
                .collectAsStateWithLifecycle(initialValue = SecuritySettings())
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.OLED -> true
                ThemeMode.DYNAMIC, ThemeMode.SYSTEM -> systemDark
            }

            SideEffect {
                val barStyle = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle,
                )
            }

            val requiresAuth = securitySettings.appLockEnabled &&
                securityRepository.canUseBiometrics() &&
                !appUnlockedState.value

            val triggerBiometric: () -> Unit = {
                biometricHelper.showBiometricPrompt(
                    activity = this@MainActivity,
                    title = getString(DesignSystemR.string.unlock_app_title),
                    subtitle = getString(DesignSystemR.string.unlock_app_subtitle),
                    onSuccess = { appUnlockedState.value = true },
                    onFailure = { /* retry on demand */ },
                )
            }

            LaunchedEffect(requiresAuth) {
                if (requiresAuth) {
                    triggerBiometric()
                }
            }

            FileExplorerTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when {
                        requiresAuth -> {
                            LockScreen(onUnlock = triggerBiometric)
                        }
                        hasPermissionState.value || limitedStorageState.value -> {
                            AppNavigation(
                                initialPath = if (limitedStorageState.value) {
                                    permissionHelper.scopedStorageRootPath()
                                } else null,
                            )
                        }
                        else -> {
                            PermissionScreen(
                                onGrantPermission = {
                                    startActivity(permissionHelper.getManageStorageIntent())
                                },
                                onContinueWithLimitedAccess = {
                                    limitedStorageState.value = true
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasPermissionState.value = permissionHelper.hasFullStorageAccess()
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(DesignSystemR.string.unlock_app_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(DesignSystemR.string.unlock_app_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(DesignSystemR.string.unlock_app_button))
        }
    }
}

@Composable
private fun PermissionScreen(
    onGrantPermission: () -> Unit,
    onContinueWithLimitedAccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Storage,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(DesignSystemR.string.storage_access_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(DesignSystemR.string.storage_access_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGrantPermission,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(DesignSystemR.string.grant_storage_access))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onContinueWithLimitedAccess,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(DesignSystemR.string.continue_limited_storage))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(DesignSystemR.string.storage_access_limited_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(DesignSystemR.string.storage_access_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
