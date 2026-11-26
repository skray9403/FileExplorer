package com.explorer.fileexplorer.feature.security

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.explorer.fileexplorer.core.data.EncryptedVolumeFormat
import com.explorer.fileexplorer.core.data.EncryptedVolumeManager
import com.explorer.fileexplorer.core.data.EncryptedVolumeMount
import com.explorer.fileexplorer.core.data.EncryptedVolumeRequest
import com.explorer.fileexplorer.core.database.IntegrityEntryEntity
import com.explorer.fileexplorer.core.designsystem.R as DesignSystemR
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Eject
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.explorer.fileexplorer.core.designsystem.FolioBottomScrim
import com.explorer.fileexplorer.core.designsystem.FolioSectionLabel
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedCard
import com.explorer.fileexplorer.core.designsystem.FolioSegmentedColumn
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenBottomBar
import com.explorer.fileexplorer.core.designsystem.FolioSubScreenHeadline
import com.explorer.fileexplorer.core.designsystem.FolioTopScrim
import com.explorer.fileexplorer.core.designsystem.topEdgeFeatherFade
import com.explorer.fileexplorer.core.storage.KeyHardwareLevel
import com.explorer.fileexplorer.core.storage.StrongBoxKeyFactory
import androidx.compose.material.icons.rounded.LockReset
import androidx.activity.compose.PredictiveBackHandler
import kotlin.coroutines.cancellation.CancellationException

private val Context.securityDataStore by preferencesDataStore("security_prefs")

// -- Security Repository --

@Singleton
class SecurityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TIMEOUT_SECONDS = intPreferencesKey("app_lock_timeout_seconds")
        val VAULT_ENABLED = booleanPreferencesKey("vault_enabled")
        val SECURE_DELETE_ENABLED = booleanPreferencesKey("secure_delete_enabled")
        val INVALIDATE_ON_BIOMETRIC_CHANGE = booleanPreferencesKey("invalidate_on_biometric_change")
    }

    val settings: Flow<SecuritySettings> = context.securityDataStore.data.map { prefs ->
        SecuritySettings(
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            appLockTimeoutSeconds = prefs[Keys.APP_LOCK_TIMEOUT_SECONDS] ?: 0,
            vaultEnabled = prefs[Keys.VAULT_ENABLED] ?: false,
            secureDeleteEnabled = prefs[Keys.SECURE_DELETE_ENABLED] ?: false,
            invalidateKeysOnBiometricChange = prefs[Keys.INVALIDATE_ON_BIOMETRIC_CHANGE] ?: false,
        )
    }

    suspend fun setAppLock(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockTimeout(seconds: Int) {
        require(seconds in setOf(0, 30, 60, 300, 900))
        context.securityDataStore.edit { it[Keys.APP_LOCK_TIMEOUT_SECONDS] = seconds }
    }

    suspend fun setVaultEnabled(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.VAULT_ENABLED] = enabled }
    }

    suspend fun setSecureDelete(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.SECURE_DELETE_ENABLED] = enabled }
    }

    suspend fun setInvalidateOnBiometricChange(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.INVALIDATE_ON_BIOMETRIC_CHANGE] = enabled }
    }

    /** Check if device supports biometric authentication. */
    fun canUseBiometrics(): Boolean {
        val mgr = BiometricManager.from(context)
        return mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }
}

data class SecuritySettings(
    val appLockEnabled: Boolean = false,
    val appLockTimeoutSeconds: Int = 0,
    val vaultEnabled: Boolean = false,
    val secureDeleteEnabled: Boolean = false,
    val invalidateKeysOnBiometricChange: Boolean = false,
)

// -- Biometric Helper --

@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use biometrics to unlock",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onFailure(errString.toString()) }
            override fun onAuthenticationFailed() { onFailure("Authentication failed") }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }
}

// SecureDelete is in core:data — com.explorer.fileexplorer.core.data.SecureDelete

// -- Checksum Verification --

object ChecksumUtil {
    /** Compute checksum of a file. Algorithms: MD5, SHA-1, SHA-256, SHA-512 */
    suspend fun computeChecksum(path: String, algorithm: String = "SHA-256"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance(algorithm)
            File(path).inputStream().buffered().use { input ->
                val buf = ByteArray(65536)
                var len: Int
                while (input.read(buf).also { len = it } != -1) {
                    digest.update(buf, 0, len)
                }
            }
            val hex = digest.digest().joinToString("") { "%02x".format(it) }
            Result.success(hex)
        } catch (e: Exception) { Result.failure(e) }
    }

    /** Verify a file against an expected checksum. */
    suspend fun verify(path: String, expected: String, algorithm: String = "SHA-256"): Boolean {
        val computed = computeChecksum(path, algorithm).getOrNull() ?: return false
        return computed.equals(expected, ignoreCase = true)
    }
}

// -- Encrypted Vault --

@Singleton
class VaultManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityRepository: SecurityRepository,
) {
    private val vaultDir: File get() = File(context.filesDir, ".vault")
    private val indexFile: File get() = File(vaultDir, INDEX_NAME)
    private val masterKeyFile: File get() = File(vaultDir, MASTER_KEY_NAME)

    private companion object {
        const val KEYSTORE_ALIAS_V2 = "file_explorer_vault_v2"
        const val KEYSTORE_ALIAS_V3_STRONGBOX = "file_explorer_vault_kek_v3_sb"
        const val MASTER_KEY_NAME = "master.key.v3"
        const val INDEX_NAME = "index.v1"
        const val TEMP_PREFIX = ".fileexplorer-vault-"
    }

    private fun getLegacyV2Key(): javax.crypto.SecretKey {
        val ks = java.security.KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        ks.getKey(KEYSTORE_ALIAS_V2, null)?.let { return it as javax.crypto.SecretKey }
        val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS_V2,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val keygen = javax.crypto.KeyGenerator.getInstance(
            android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keygen.init(spec)
        return keygen.generateKey()
    }

    private fun writeMasterKey(dekBytes: ByteArray, kek: javax.crypto.SecretKey) {
        val wrapCipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(javax.crypto.Cipher.ENCRYPT_MODE, kek)
        }
        val wrapIv = wrapCipher.iv
        val wrappedBytes = wrapCipher.doFinal(dekBytes)
        val temporary = File(vaultDir, TEMP_PREFIX + UUID.randomUUID() + ".master.tmp")
        try {
            FileOutputStream(temporary, false).use { out ->
                out.write(wrapIv.size and 0xff)
                out.write(wrapIv)
                out.write(wrappedBytes)
                out.fd.sync()
            }
            moveIntoPlace(temporary, masterKeyFile)
        } finally {
            runCatching { Files.deleteIfExists(temporary.toPath()) }
        }
    }

    private fun unwrapMasterKey(bytes: ByteArray, kek: javax.crypto.SecretKey): ByteArray {
        require(bytes.isNotEmpty()) { "Master key file is empty" }
        val wrapIvLen = bytes[0].toInt() and 0xff
        require(wrapIvLen in 1..32 && bytes.size > 1 + wrapIvLen) { "Invalid master key file header" }
        val wrapIv = bytes.copyOfRange(1, 1 + wrapIvLen)
        val wrappedBytes = bytes.copyOfRange(1 + wrapIvLen, bytes.size)
        val unwrapCipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(javax.crypto.Cipher.DECRYPT_MODE, kek, javax.crypto.spec.GCMParameterSpec(128, wrapIv))
        }
        return unwrapCipher.doFinal(wrappedBytes)
    }

    /**
     * Opens a vault session. The caller must invoke this only after the configured
     * biometric/device-credential gate has completed successfully.
     */
    suspend fun unlock(): Result<VaultSession> = withContext(Dispatchers.IO) {
        runCatching {
            ensureVaultDirectory()
            val hasMasterKey = Files.exists(masterKeyFile.toPath(), LinkOption.NOFOLLOW_LINKS)
            val hasLegacyIndex = Files.exists(indexFile.toPath(), LinkOption.NOFOLLOW_LINKS)
            if (!hasMasterKey && hasLegacyIndex) {
                val legacyKey = getLegacyV2Key()
                readRecords(legacyKey)
                return@runCatching VaultSession(legacyKey)
            }
            val invalidateOnChange = securityRepository.settings.first().invalidateKeysOnBiometricChange
            val kek = StrongBoxKeyFactory.getOrCreateAesGcmKey(
                context = context,
                alias = KEYSTORE_ALIAS_V3_STRONGBOX,
                requireUserAuth = true,
                authValiditySeconds = 15,
                invalidateOnNewBiometric = invalidateOnChange,
            )
            val dekBytes = if (hasMasterKey) {
                unwrapMasterKey(masterKeyFile.readBytes(), kek)
            } else {
                ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }.also { freshDek ->
                    writeMasterKey(freshDek, kek)
                }
            }
            val dek = javax.crypto.spec.SecretKeySpec(dekBytes, "AES")
            try {
                readRecords(dek)
                VaultSession(dek, dekBytes)
            } catch (error: Throwable) {
                dekBytes.fill(0)
                throw error
            }
        }
    }

    suspend fun listVaultEntries(session: VaultSession): Result<List<VaultEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            session.requireUnlocked()
            readRecords(session.key).map { record ->
                VaultEntry(record.id, record.originalName, record.size, record.modifiedAt)
            }
        }
    }

    suspend fun addToVault(sourcePath: String, session: VaultSession): Result<VaultEntry> = withContext(Dispatchers.IO) {
        runCatching {
            session.requireUnlocked()
            ensureVaultDirectory()
            val source = File(sourcePath)
            require(Files.isRegularFile(source.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Only regular local files can enter the vault"
            }
            VaultFilePolicy.validateName(source.name)
            val records = readRecords(session.key)
            val record = VaultIndexRecord(
                id = UUID.randomUUID().toString(),
                originalName = source.name,
                size = source.length(),
                modifiedAt = source.lastModified().coerceAtLeast(0),
            )
            val temporary = createTemporaryFile()
            val destination = payloadFile(record.id)
            var moved = false
            try {
                VaultPayloadFormat.encrypt(source, temporary, session.key)
                moveIntoPlace(temporary, destination)
                moved = true
                writeRecords(records + record, session.key)
                try {
                    require(Files.deleteIfExists(source.toPath())) { "Unable to remove plaintext source" }
                } catch (error: Exception) {
                    runCatching { writeRecords(records, session.key) }
                    runCatching { Files.deleteIfExists(destination.toPath()) }
                    throw error
                }
                VaultEntry(record.id, record.originalName, record.size, record.modifiedAt)
            } catch (error: Exception) {
                if (moved) runCatching { Files.deleteIfExists(destination.toPath()) }
                throw error
            } finally {
                runCatching { Files.deleteIfExists(temporary.toPath()) }
            }
        }
    }

    suspend fun restoreFile(
        entryId: String,
        destinationDir: String,
        session: VaultSession,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            session.requireUnlocked()
            val records = readRecords(session.key)
            val record = records.firstOrNull { it.id == entryId }
                ?: error("Vault entry not found")
            val directory = File(destinationDir).canonicalFile
            require(Files.isDirectory(directory.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Restore destination is not a directory"
            }
            val destination = File(directory, record.originalName)
            require(destination.parentFile?.canonicalFile == directory) { "Invalid restore destination" }
            require(!Files.exists(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Restore destination already exists"
            }
            val payload = payloadFile(record.id)
            require(Files.isRegularFile(payload.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Vault payload is missing"
            }
            val temporary = File(directory, TEMP_PREFIX + UUID.randomUUID() + ".tmp")
            Files.createFile(temporary.toPath())
            var moved = false
            try {
                VaultPayloadFormat.decrypt(payload, temporary, session.key)
                writeRecords(records - record, session.key)
                try {
                    moveIntoPlace(temporary, destination)
                    moved = true
                    require(Files.deleteIfExists(payload.toPath())) { "Unable to remove restored vault payload" }
                } catch (error: Exception) {
                    if (moved) runCatching { Files.deleteIfExists(destination.toPath()) }
                    runCatching { writeRecords(records, session.key) }
                    throw error
                }
                destination.absolutePath
            } finally {
                runCatching { Files.deleteIfExists(temporary.toPath()) }
            }
        }
    }

    suspend fun deleteFromVault(entryId: String, session: VaultSession): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            session.requireUnlocked()
            val records = readRecords(session.key)
            val record = records.firstOrNull { it.id == entryId }
                ?: error("Vault entry not found")
            val payload = payloadFile(record.id)
            require(Files.isRegularFile(payload.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Vault payload is missing"
            }
            writeRecords(records - record, session.key)
            try {
                require(Files.deleteIfExists(payload.toPath())) { "Unable to delete vault payload" }
            } catch (error: Exception) {
                runCatching { writeRecords(records, session.key) }
                throw error
            }
        }
    }

    fun lock(session: VaultSession?) {
        session?.lock()
    }

    private fun ensureVaultDirectory() {
        if (Files.exists(vaultDir.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            require(Files.isDirectory(vaultDir.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Vault storage is not a directory"
            }
        } else {
            Files.createDirectories(vaultDir.toPath())
        }
    }

    private fun readRecords(key: javax.crypto.SecretKey): List<VaultIndexRecord> {
        ensureVaultDirectory()
        val files = vaultDir.listFiles().orEmpty()
        val unexpected = files.filter { file ->
            file.name != INDEX_NAME &&
                file.name != MASTER_KEY_NAME &&
                !file.name.startsWith(TEMP_PREFIX) &&
                !file.name.endsWith(VaultFilePolicy.PAYLOAD_SUFFIX)
        }
        require(unexpected.isEmpty()) { "Unrecognized vault storage entry" }
        if (!Files.exists(indexFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            require(files.none { it.name.endsWith(VaultFilePolicy.PAYLOAD_SUFFIX) }) {
                "Vault index is missing"
            }
            return emptyList()
        }
        require(Files.isRegularFile(indexFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            "Vault index is not a regular file"
        }
        val records = VaultIndexCodec.decode(indexFile.readBytes(), key)
        require(records.map { it.id }.toSet().size == records.size) { "Duplicate vault entry id" }
        val payloadNames = records.mapTo(hashSetOf()) { VaultFilePolicy.payloadName(it.id) }
        val actualPayloads = files.filter { it.name.endsWith(VaultFilePolicy.PAYLOAD_SUFFIX) }
        require(actualPayloads.all { it.name in payloadNames }) { "Untracked vault payload" }
        require(payloadNames.all { expected -> actualPayloads.any { it.name == expected } }) {
            "Vault payload is missing"
        }
        actualPayloads.forEach { payload ->
            require(Files.isRegularFile(payload.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                "Vault payload is not a regular file"
            }
        }
        return records
    }

    private fun writeRecords(records: List<VaultIndexRecord>, key: javax.crypto.SecretKey) {
        val temporary = File(vaultDir, TEMP_PREFIX + UUID.randomUUID() + ".index.tmp")
        try {
            FileOutputStream(temporary, false).use { output ->
                output.write(VaultIndexCodec.encode(records, key))
                output.fd.sync()
            }
            moveIntoPlace(temporary, indexFile)
        } finally {
            runCatching { Files.deleteIfExists(temporary.toPath()) }
        }
    }

    private fun payloadFile(id: String): File {
        val file = File(vaultDir, VaultFilePolicy.payloadName(id))
        require(file.parentFile?.canonicalFile == vaultDir.canonicalFile) { "Invalid vault payload path" }
        return file
    }

    private fun createTemporaryFile(): File {
        val file = File(vaultDir, TEMP_PREFIX + UUID.randomUUID() + ".tmp")
        Files.createFile(file.toPath())
        return file
    }

    private fun moveIntoPlace(source: File, destination: File) {
        try {
            Files.move(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

// -- ViewModel --

data class SecurityUiState(
    val settings: SecuritySettings = SecuritySettings(),
    val canUseBiometrics: Boolean = false,
    val hardwareLevel: KeyHardwareLevel = KeyHardwareLevel.TEE,
    val vaultEntries: List<VaultEntry> = emptyList(),
    val vaultUnlocked: Boolean = false,
    val isVaultBusy: Boolean = false,
    val checksumResult: String? = null,
    val isComputing: Boolean = false,
    val integrityEntries: List<IntegrityEntryEntity> = emptyList(),
    val isScanningIntegrity: Boolean = false,
    val encryptedFormats: List<EncryptedVolumeFormat> = emptyList(),
    val encryptedMounts: List<EncryptedVolumeMount> = emptyList(),
    val isLoadingEncryptedVolumes: Boolean = false,
    val isMountingEncryptedVolume: Boolean = false,
    val showEncryptedVolumeDialog: Boolean = false,
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityRepo: SecurityRepository,
    private val vaultManager: VaultManager,
    private val integrityRepository: IntegrityRepository,
    private val encryptedVolumeManager: EncryptedVolumeManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityUiState())
    val state: StateFlow<SecurityUiState> = _state.asStateFlow()

    private val _toasts = MutableSharedFlow<String>()
    val toasts: SharedFlow<String> = _toasts.asSharedFlow()

    private var vaultSession: VaultSession? = null

    init {
        val hwLevel = StrongBoxKeyFactory.inspectHardwareLevel(context, "file_explorer_vault_kek_v3_sb")
        _state.update { it.copy(canUseBiometrics = securityRepo.canUseBiometrics(), hardwareLevel = hwLevel) }
        viewModelScope.launch {
            securityRepo.settings.collect { settings ->
                _state.update { it.copy(settings = settings) }
                if (!settings.vaultEnabled) lockVault()
            }
        }
        viewModelScope.launch {
            integrityRepository.entries.collect { entries ->
                _state.update { it.copy(integrityEntries = entries) }
            }
        }
        refreshEncryptedVolumes()
    }

    fun toggleAppLock() { viewModelScope.launch { securityRepo.setAppLock(!_state.value.settings.appLockEnabled) } }
    fun setAppLockTimeout(seconds: Int) {
        viewModelScope.launch { securityRepo.setAppLockTimeout(seconds) }
    }
    fun toggleInvalidateOnBiometricChange() {
        viewModelScope.launch {
            securityRepo.setInvalidateOnBiometricChange(!_state.value.settings.invalidateKeysOnBiometricChange)
        }
    }
    fun toggleVault() {
        val enabled = !_state.value.settings.vaultEnabled
        if (enabled && !_state.value.canUseBiometrics) {
            viewModelScope.launch { _toasts.emit("Vault requires biometric or device-credential authentication") }
            return
        }
        viewModelScope.launch {
            if (!enabled) lockVault()
            securityRepo.setVaultEnabled(enabled)
        }
    }
    fun toggleSecureDelete() { viewModelScope.launch { securityRepo.setSecureDelete(!_state.value.settings.secureDeleteEnabled) } }

    /** Call only from a UI callback after biometric/device-credential success. */
    fun unlockVault() {
        if (_state.value.isVaultBusy || !_state.value.settings.vaultEnabled) return
        viewModelScope.launch {
            _state.update { it.copy(isVaultBusy = true) }
            vaultManager.unlock()
                .onSuccess { session ->
                    vaultManager.listVaultEntries(session)
                        .onSuccess { entries ->
                            vaultManager.lock(vaultSession)
                            vaultSession = session
                            _state.update {
                                it.copy(
                                    vaultEntries = entries,
                                    vaultUnlocked = true,
                                    isVaultBusy = false,
                                )
                            }
                        }
                        .onFailure { error ->
                            vaultManager.lock(session)
                            _state.update { it.copy(isVaultBusy = false) }
                            val msg = if (StrongBoxKeyFactory.isKeyInvalidated(error)) {
                                context.getString(DesignSystemR.string.key_invalidated_error)
                            } else {
                                "Vault unlock failed: ${error.message}"
                            }
                            _toasts.emit(msg)
                        }
                }
                .onFailure { error ->
                    _state.update { it.copy(isVaultBusy = false) }
                    val msg = if (StrongBoxKeyFactory.isKeyInvalidated(error)) {
                        context.getString(DesignSystemR.string.key_invalidated_error)
                    } else {
                        "Vault unlock failed: ${error.message}"
                    }
                    _toasts.emit(msg)
                }
        }
    }

    fun lockVault() {
        vaultManager.lock(vaultSession)
        vaultSession = null
        _state.update { it.copy(vaultEntries = emptyList(), vaultUnlocked = false, isVaultBusy = false) }
    }

    fun restoreVaultEntry(entry: VaultEntry) {
        val session = vaultSession ?: run {
            viewModelScope.launch { _toasts.emit("Unlock the vault first") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isVaultBusy = true) }
            @Suppress("DEPRECATION")
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            vaultManager.restoreFile(entry.id, downloads.absolutePath, session)
                .onSuccess { restoredPath ->
                    _toasts.emit("Restored to Downloads: ${File(restoredPath).name}")
                    refreshVaultEntries(session)
                }
                .onFailure { error -> _toasts.emit("Restore failed: ${error.message}") }
            _state.update { it.copy(isVaultBusy = false) }
        }
    }

    fun deleteVaultEntry(entry: VaultEntry) {
        val session = vaultSession ?: run {
            viewModelScope.launch { _toasts.emit("Unlock the vault first") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isVaultBusy = true) }
            vaultManager.deleteFromVault(entry.id, session)
                .onSuccess { _toasts.emit("Deleted ${entry.displayName} from vault") }
                .onFailure { error -> _toasts.emit("Vault deletion failed: ${error.message}") }
            refreshVaultEntries(session)
            _state.update { it.copy(isVaultBusy = false) }
        }
    }

    private suspend fun refreshVaultEntries(session: VaultSession) {
        vaultManager.listVaultEntries(session)
            .onSuccess { entries -> _state.update { it.copy(vaultEntries = entries) } }
            .onFailure { error ->
                vaultManager.lock(session)
                if (vaultSession === session) vaultSession = null
                _state.update { it.copy(vaultEntries = emptyList(), vaultUnlocked = false) }
                _toasts.emit("Vault refresh failed: ${error.message}")
            }
    }

    fun computeChecksum(path: String, algorithm: String = "SHA-256") {
        viewModelScope.launch {
            _state.update { it.copy(isComputing = true, checksumResult = null) }
            ChecksumUtil.computeChecksum(path, algorithm)
                .onSuccess { hash -> _state.update { it.copy(checksumResult = hash, isComputing = false) } }
                .onFailure { e -> _toasts.emit("Checksum failed: ${e.message}"); _state.update { it.copy(isComputing = false) } }
        }
    }

    fun addIntegrityPath(path: String) {
        viewModelScope.launch {
            integrityRepository.addPath(path)
                .onSuccess { _toasts.emit("Watching ${it.path}") }
                .onFailure { error -> _toasts.emit("Watch failed: ${error.message}") }
        }
    }

    fun removeIntegrityPath(path: String) {
        viewModelScope.launch {
            integrityRepository.removePath(path)
            _toasts.emit("Removed integrity watch")
        }
    }

    fun scanIntegrityNow() {
        if (_state.value.isScanningIntegrity) return
        viewModelScope.launch {
            _state.update { it.copy(isScanningIntegrity = true) }
            integrityRepository.scanNow()
                .onSuccess { summary ->
                    _toasts.emit(
                        "Integrity scan: ${summary.checked} checked, " +
                            "${summary.changed} changed, ${summary.missing} missing",
                    )
                }
                .onFailure { error -> _toasts.emit("Integrity scan failed: ${error.message}") }
            _state.update { it.copy(isScanningIntegrity = false) }
        }
    }

    fun refreshEncryptedVolumes() {
        if (_state.value.isLoadingEncryptedVolumes) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEncryptedVolumes = true) }
            runCatching {
                encryptedVolumeManager.detectFormats() to encryptedVolumeManager.listMounted()
            }.onSuccess { (formats, mounts) ->
                _state.update {
                    it.copy(
                        encryptedFormats = formats,
                        encryptedMounts = mounts,
                        isLoadingEncryptedVolumes = false,
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoadingEncryptedVolumes = false) }
                _toasts.emit("Encrypted volume refresh failed: ${error.message}")
            }
        }
    }

    fun showEncryptedVolumeDialog() {
        _state.update { it.copy(showEncryptedVolumeDialog = true) }
    }

    fun dismissEncryptedVolumeDialog() {
        _state.update { it.copy(showEncryptedVolumeDialog = false) }
    }

    fun mountEncryptedVolume(
        format: EncryptedVolumeFormat,
        cipherPath: String,
        mountPath: String,
        readOnly: Boolean,
        passphrase: CharArray,
    ) {
        if (_state.value.isMountingEncryptedVolume) {
            passphrase.fill('\u0000')
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showEncryptedVolumeDialog = false,
                    isMountingEncryptedVolume = true,
                )
            }
            encryptedVolumeManager.mount(
                EncryptedVolumeRequest(format, cipherPath, mountPath, readOnly),
                passphrase,
            ).onSuccess {
                _toasts.emit("${format.label} volume mounted")
            }.onFailure { error ->
                _toasts.emit("Encrypted volume mount failed: ${error.message}")
            }
            _state.update { it.copy(isMountingEncryptedVolume = false) }
            refreshEncryptedVolumes()
        }
    }

    fun unmountEncryptedVolume(mountPath: String) {
        viewModelScope.launch {
            encryptedVolumeManager.unmount(mountPath)
                .onSuccess { _toasts.emit("Encrypted volume unmounted") }
                .onFailure { error -> _toasts.emit("Encrypted volume unmount failed: ${error.message}") }
            refreshEncryptedVolumes()
        }
    }

    override fun onCleared() {
        vaultManager.lock(vaultSession)
        vaultSession = null
        super.onCleared()
    }
}

// -- Screen UI (Material 3 Expressive) --


/**
 * Drop-in M3 Expressive UI replacement for the composable section of SecurityScreen.kt.
 * Keep SecurityRepository, BiometricHelper, ChecksumUtil, VaultManager, and SecurityViewModel
 * untouched in SecurityScreen.kt and replace only the `@Composable fun SecurityScreen(...)`
 * and its private UI helpers below.
 */
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val securityEntryPoint = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SecurityEntryPoint::class.java,
        )
    }
    var showIntegrityPathDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.toasts.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    DisposableEffect(Unit) { onDispose { viewModel.lockVault() } }

    fun authenticateVault(title: String, subtitle: String, onSuccess: () -> Unit) {
        val activity = context as? FragmentActivity
        if (activity == null) {
            Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.biometrics_unavailable), Toast.LENGTH_SHORT).show()
        } else {
            securityEntryPoint.biometricHelper().showBiometricPrompt(
                activity = activity,
                title = title,
                subtitle = subtitle,
                onSuccess = onSuccess,
                onFailure = { reason ->
                    Toast.makeText(context, context.applicationContext.getString(DesignSystemR.string.vault_authentication_cancelled, reason), Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    if (showIntegrityPathDialog) {
        ExpressiveIntegrityPathDialog(
            onConfirm = { path ->
                showIntegrityPathDialog = false
                viewModel.addIntegrityPath(path)
            },
            onDismiss = { showIntegrityPathDialog = false },
        )
    }

    if (state.showEncryptedVolumeDialog) {
        ExpressiveEncryptedVolumeDialog(
            formats = state.encryptedFormats,
            isMounting = state.isMountingEncryptedVolume,
            onConfirm = { format, cipherPath, mountPath, readOnly, passphrase ->
                viewModel.mountEncryptedVolume(format, cipherPath, mountPath, readOnly, passphrase)
            },
            onDismiss = viewModel::dismissEncryptedVolumeDialog,
        )
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
            item(key = "sec_headline") {
                FolioSubScreenHeadline(title = stringResource(DesignSystemR.string.security))
            }

            // App Lock
            item(key = "sec_app_lock") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.app_lock))
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = 2) {
                            SecuritySwitchRow(
                                icon = Icons.Rounded.Fingerprint,
                                title = stringResource(DesignSystemR.string.biometric_lock),
                                subtitle = if (state.canUseBiometrics) {
                                    stringResource(DesignSystemR.string.require_biometric)
                                } else {
                                    stringResource(DesignSystemR.string.biometrics_unavailable)
                                },
                                checked = state.settings.appLockEnabled,
                                enabled = state.canUseBiometrics,
                                onCheckedChange = { viewModel.toggleAppLock() },
                            )
                        }
                        FolioSegmentedCard(index = 1, count = 2) {
                            SecuritySwitchRow(
                                icon = Icons.Rounded.LockReset,
                                title = stringResource(DesignSystemR.string.invalidate_on_biometric_change),
                                subtitle = stringResource(DesignSystemR.string.invalidate_on_biometric_change_desc),
                                checked = state.settings.invalidateKeysOnBiometricChange,
                                enabled = state.canUseBiometrics,
                                onCheckedChange = { viewModel.toggleInvalidateOnBiometricChange() },
                            )
                        }
                    }
                }
            }

            item(key = "sec_app_lock_timeout") {
                var timeoutExpanded by remember { mutableStateOf(false) }
                val seconds = state.settings.appLockTimeoutSeconds
                val label = when (seconds) {
                    0 -> stringResource(DesignSystemR.string.app_lock_timeout_immediately)
                    30 -> stringResource(DesignSystemR.string.app_lock_timeout_30_seconds)
                    60 -> stringResource(DesignSystemR.string.app_lock_timeout_1_minute)
                    300 -> stringResource(DesignSystemR.string.app_lock_timeout_5_minutes)
                    900 -> stringResource(DesignSystemR.string.app_lock_timeout_15_minutes)
                    else -> stringResource(DesignSystemR.string.app_lock_timeout_immediately)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.lock_after_leaving_app))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { timeoutExpanded = true },
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(DesignSystemR.string.app_lock_timeout_title), style = MaterialTheme.typography.titleSmall)
                                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Filled.ExpandMore, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = timeoutExpanded,
                            onDismissRequest = { timeoutExpanded = false },
                        ) {
                            listOf(0, 30, 60, 300, 900).forEach { option ->
                                val optionLabel = when (option) {
                                    0 -> stringResource(DesignSystemR.string.app_lock_timeout_immediately)
                                    30 -> stringResource(DesignSystemR.string.app_lock_timeout_30_seconds)
                                    60 -> stringResource(DesignSystemR.string.app_lock_timeout_1_minute)
                                    300 -> stringResource(DesignSystemR.string.app_lock_timeout_5_minutes)
                                    else -> stringResource(DesignSystemR.string.app_lock_timeout_15_minutes)
                                }
                                DropdownMenuItem(
                                    text = { Text(optionLabel) },
                                    onClick = {
                                        viewModel.setAppLockTimeout(option)
                                        timeoutExpanded = false
                                    },
                                    trailingIcon = {
                                        if (seconds == option) Icon(Icons.Filled.Check, contentDescription = null)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Encrypted Vault
            item(key = "sec_vault") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.encrypted_vault))
                    val totalRows = 1 + (if (state.settings.vaultEnabled) 1 else 0) +
                        (if (state.settings.vaultEnabled && state.vaultUnlocked) state.vaultEntries.size.coerceAtLeast(1) else 0)
                    var idx = 0
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = idx++, count = totalRows) {
                            SecuritySwitchRow(
                                icon = Icons.Rounded.Lock,
                                title = stringResource(DesignSystemR.string.enable_vault),
                                subtitle = stringResource(DesignSystemR.string.vault_description),
                                checked = state.settings.vaultEnabled,
                                enabled = state.canUseBiometrics,
                                onCheckedChange = { viewModel.toggleVault() },
                            )
                        }
                        if (state.settings.vaultEnabled) {
                            FolioSegmentedCard(index = idx++, count = totalRows) {
                                Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    if (state.vaultUnlocked) {
                                        OutlinedButton(
                                            onClick = viewModel::lockVault,
                                            enabled = !state.isVaultBusy,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(Icons.Rounded.Lock, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(DesignSystemR.string.vault_lock))
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                authenticateVault(
                                                    title = context.applicationContext.getString(DesignSystemR.string.vault_unlock),
                                                    subtitle = context.applicationContext.getString(DesignSystemR.string.authenticate_to_view_protected_files),
                                                    onSuccess = viewModel::unlockVault,
                                                )
                                            },
                                            enabled = !state.isVaultBusy && state.canUseBiometrics,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(Icons.Rounded.LockOpen, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(DesignSystemR.string.vault_unlock))
                                        }
                                    }
                                }
                            }
                            if (state.vaultUnlocked) {
                                if (state.vaultEntries.isEmpty()) {
                                    FolioSegmentedCard(index = idx, count = totalRows) {
                                        Text(
                                            text = stringResource(DesignSystemR.string.vault_empty),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    }
                                } else {
                                    state.vaultEntries.forEach { entry ->
                                        FolioSegmentedCard(index = idx++, count = totalRows) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(entry.displayName, style = MaterialTheme.typography.titleSmall)
                                                    Text(stringResource(DesignSystemR.string.vault_entry_size, entry.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                TextButton(onClick = { viewModel.restoreVaultEntry(entry) }, enabled = !state.isVaultBusy) {
                                                    Text(stringResource(DesignSystemR.string.vault_restore))
                                                }
                                                IconButton(onClick = { viewModel.deleteVaultEntry(entry) }, enabled = !state.isVaultBusy) {
                                                    Icon(Icons.Rounded.DeleteForever, stringResource(DesignSystemR.string.vault_delete))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // File Security
            item(key = "sec_file_security") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.file_security))
                    FolioSegmentedColumn {
                        val fileSecCount = if (state.checksumResult != null) 4 else 3
                        FolioSegmentedCard(index = 0, count = fileSecCount) {
                            SecuritySwitchRow(
                                icon = Icons.Rounded.DeleteForever,
                                title = stringResource(DesignSystemR.string.secure_delete),
                                subtitle = stringResource(DesignSystemR.string.secure_delete_description),
                                checked = state.settings.secureDeleteEnabled,
                                enabled = true,
                                onCheckedChange = { viewModel.toggleSecureDelete() },
                            )
                        }
                        FolioSegmentedCard(index = 1, count = fileSecCount) {
                            SecurityStaticRow(Icons.Rounded.Verified, stringResource(DesignSystemR.string.checksum_verification), stringResource(DesignSystemR.string.checksum_description))
                        }
                        FolioSegmentedCard(index = 2, count = if (state.checksumResult != null) 4 else 3) {
                            SecurityStaticRow(Icons.Rounded.EnhancedEncryption, stringResource(DesignSystemR.string.file_encryption), stringResource(DesignSystemR.string.file_encryption_description))
                        }
                        state.checksumResult?.let { hash ->
                            FolioSegmentedCard(index = 3, count = 4) {
                                Text(
                                    text = hash,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Integrity Watch
            item(key = "sec_integrity") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.integrity_watch))
                    val rows = 1 + state.integrityEntries.size.coerceAtLeast(1)
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = rows) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(DesignSystemR.string.integrity_watch_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { showIntegrityPathDialog = true }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(DesignSystemR.string.add_path))
                                    }
                                    Button(
                                        onClick = viewModel::scanIntegrityNow,
                                        enabled = state.integrityEntries.isNotEmpty() && !state.isScanningIntegrity,
                                        modifier = Modifier.weight(1f),
                                    ) { Text(if (state.isScanningIntegrity) stringResource(DesignSystemR.string.scanning) else stringResource(DesignSystemR.string.scan_now)) }
                                }
                            }
                        }
                        if (state.integrityEntries.isEmpty()) {
                            FolioSegmentedCard(index = 1, count = rows) {
                                Text(
                                    text = stringResource(DesignSystemR.string.no_watched_paths),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            state.integrityEntries.forEachIndexed { idx, entry ->
                                FolioSegmentedCard(index = idx + 1, count = rows) {
                                    ExpressiveIntegrityRow(entry = entry, onRemove = { viewModel.removeIntegrityPath(entry.path) })
                                }
                            }
                        }
                    }
                }
            }

            // Encrypted Volumes
            item(key = "sec_volumes") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.encrypted_volumes))
                    val rows = 1 + state.encryptedMounts.size.coerceAtLeast(1)
                    FolioSegmentedColumn {
                        FolioSegmentedCard(index = 0, count = rows) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.EnhancedEncryption, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(DesignSystemR.string.encrypted_volumes_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = viewModel::refreshEncryptedVolumes,
                                        enabled = !state.isLoadingEncryptedVolumes && !state.isMountingEncryptedVolume,
                                    ) { Icon(Icons.Rounded.Refresh, stringResource(DesignSystemR.string.refresh)) }
                                }
                                OutlinedButton(
                                    onClick = viewModel::showEncryptedVolumeDialog,
                                    enabled = state.encryptedFormats.isNotEmpty() && !state.isMountingEncryptedVolume,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(DesignSystemR.string.mount_volume)) }
                            }
                        }
                        if (state.encryptedMounts.isEmpty()) {
                            FolioSegmentedCard(index = 1, count = rows) {
                                Text(
                                    text = when {
                                        state.isLoadingEncryptedVolumes -> "Checking root environment..."
                                        state.encryptedFormats.isEmpty() -> stringResource(DesignSystemR.string.encrypted_volumes_requirements)
                                        else -> stringResource(DesignSystemR.string.no_encrypted_volumes_mounted)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            state.encryptedMounts.forEachIndexed { idx, mount ->
                                FolioSegmentedCard(index = idx + 1, count = rows) {
                                    ExpressiveMountRow(mount = mount, onUnmount = { viewModel.unmountEncryptedVolume(mount.mountPath) })
                                }
                            }
                        }
                    }
                }
            }

            // Security Model Info
            item(key = "sec_about") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FolioSectionLabel(stringResource(DesignSystemR.string.about))
                    val hwStatus = when (state.hardwareLevel) {
                        KeyHardwareLevel.STRONGBOX -> stringResource(DesignSystemR.string.hardware_security_strongbox)
                        KeyHardwareLevel.TEE -> stringResource(DesignSystemR.string.hardware_security_tee)
                        KeyHardwareLevel.SOFTWARE -> stringResource(DesignSystemR.string.hardware_security_software)
                    }
                    FolioSegmentedCard(index = 0, count = 1) {
                        SecurityStaticRow(
                            icon = Icons.Rounded.Info,
                            title = stringResource(DesignSystemR.string.security_model),
                            subtitle = "$hwStatus\n\n" + stringResource(DesignSystemR.string.security_model_description) +
                                " Secure delete uses DoD 5220.22-M 3-pass overwrite. Checksums computed using java.security.MessageDigest.",
                        )
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
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SecuritySwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SecurityStaticRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExpressiveIntegrityRow(entry: IntegrityEntryEntity, onRemove: () -> Unit) {
    val statusColor = when (entry.status) {
        IntegrityStatuses.OK -> MaterialTheme.colorScheme.primary
        IntegrityStatuses.CHANGED, IntegrityStatuses.MISSING -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Fingerprint, null, tint = statusColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.path, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                text = buildString {
                    append(entry.status)
                    entry.lastCheckedAt?.let { append(" • checked ${java.util.Date(it)}") }
                    entry.lastError?.let { append(" • $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                maxLines = 2,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Rounded.Close, stringResource(DesignSystemR.string.remove_watch))
        }
    }
}

@Composable
private fun ExpressiveMountRow(mount: EncryptedVolumeMount, onUnmount: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(DesignSystemR.string.volume_title, mount.format.label, mount.mountPath), style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                text = stringResource(
                    DesignSystemR.string.volume_details,
                    mount.cipherPath,
                    if (mount.readOnly) stringResource(DesignSystemR.string.read_only) else stringResource(DesignSystemR.string.read_write),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        IconButton(onClick = onUnmount) {
            Icon(Icons.Rounded.Eject, stringResource(DesignSystemR.string.unmount_volume))
        }
    }
}

@Composable
private fun ExpressiveIntegrityPathDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var path by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(DesignSystemR.string.watch_path)) },
        text = {
            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text(stringResource(DesignSystemR.string.absolute_file_or_directory_path)) },
                singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { if (path.isNotBlank()) onConfirm(path.trim()) }) { Text(stringResource(DesignSystemR.string.watch)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}

@Composable
private fun ExpressiveEncryptedVolumeDialog(
    formats: List<EncryptedVolumeFormat>,
    isMounting: Boolean,
    onConfirm: (EncryptedVolumeFormat, String, String, Boolean, CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedFormat by remember(formats) { mutableStateOf(formats.firstOrNull()) }
    var cipherPath by remember { mutableStateOf("") }
    var mountPath by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var readOnly by remember { mutableStateOf(false) }
    val canSubmit = selectedFormat != null && cipherPath.isNotBlank() &&
        mountPath.isNotBlank() && passphrase.isNotEmpty() && !isMounting

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(DesignSystemR.string.mount_encrypted_volume)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(DesignSystemR.string.format), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    formats.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = cipherPath,
                    onValueChange = { cipherPath = it },
                    label = { Text(stringResource(DesignSystemR.string.cipher_directory)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = mountPath,
                    onValueChange = { mountPath = it },
                    label = { Text(stringResource(DesignSystemR.string.empty_mount_directory)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(DesignSystemR.string.passphrase)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(DesignSystemR.string.read_only_mount))
                    Switch(checked = readOnly, onCheckedChange = { readOnly = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val format = selectedFormat ?: return@TextButton
                    val secret = passphrase.toCharArray()
                    passphrase = ""
                    onConfirm(format, cipherPath.trim(), mountPath.trim(), readOnly, secret)
                },
                enabled = canSubmit,
            ) { Text(if (isMounting) stringResource(DesignSystemR.string.mounting) else stringResource(DesignSystemR.string.mount_volume)) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isMounting) { Text(stringResource(DesignSystemR.string.cancel)) } },
    )
}
