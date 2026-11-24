package com.explorer.fileexplorer.feature.security

import android.content.Context
import com.explorer.fileexplorer.core.storage.StrongBoxKeyFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

internal object FileEncryptionEngine {
    private val magic = byteArrayOf(0x46, 0x45, 0x4e, 0x43)
    private const val VERSION_V1 = 1
    private const val VERSION_V2_ENVELOPE = 2
    private const val IV_SIZE = 12
    private const val DEK_SIZE = 32
    private const val TAG_BITS = 128
    private const val MAX_NAME_BYTES = 4096
    private const val MAX_WRAPPED_KEY_BYTES = 256

    data class ParsedHeader(
        val originalName: String,
        val iv: ByteArray,
        val encoded: ByteArray,
        val version: Int = VERSION_V1,
        val wrapIv: ByteArray? = null,
        val wrappedDek: ByteArray? = null,
    )

    /** Legacy v1 direct-key encrypt (retained for existing unit tests). */
    fun encrypt(
        input: InputStream,
        output: OutputStream,
        originalName: String,
        key: SecretKey,
    ) {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val header = encodeV1Header(originalName, iv)
        output.write(header)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            updateAAD(header)
        }
        CipherOutputStream(output, cipher).use { cipherOutput ->
            input.copyTo(cipherOutput, BUFFER_SIZE)
        }
    }

    /** v2 StrongBox Envelope Encrypt: hardware KEK wraps random 256-bit DEK; DEK streams payload at CPU speed. */
    fun encryptEnvelope(
        input: InputStream,
        output: OutputStream,
        originalName: String,
        kek: SecretKey,
    ) {
        val random = SecureRandom()
        val dekBytes = ByteArray(DEK_SIZE).also(random::nextBytes)
        val payloadIv = ByteArray(IV_SIZE).also(random::nextBytes)
        try {
            val wrapCipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.ENCRYPT_MODE, kek)
            }
            val wrapIv = wrapCipher.iv
            val wrappedDek = wrapCipher.doFinal(dekBytes)
            val header = encodeV2Header(originalName, wrapIv, wrappedDek, payloadIv)
            output.write(header)

            val dek = SecretKeySpec(dekBytes, "AES")
            val payloadCipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.ENCRYPT_MODE, dek, GCMParameterSpec(TAG_BITS, payloadIv))
                updateAAD(header)
            }
            CipherOutputStream(output, payloadCipher).use { cipherOutput ->
                input.copyTo(cipherOutput, BUFFER_SIZE)
            }
        } finally {
            dekBytes.fill(0)
        }
    }

    /** Legacy v1 decrypt (retained for existing unit tests). */
    fun decrypt(input: InputStream, output: OutputStream, key: SecretKey): String {
        val header = readHeader(input)
        require(header.version == VERSION_V1) { "Use decryptAuto for envelope-encrypted files" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, header.iv))
            updateAAD(header.encoded)
        }
        CipherInputStream(input, cipher).use { cipherInput ->
            cipherInput.copyTo(output, BUFFER_SIZE)
        }
        return header.originalName
    }

    /** Decrypts both v2 StrongBox Envelope files and legacy v1 files seamlessly. */
    fun decryptAuto(
        input: InputStream,
        output: OutputStream,
        v2KekProvider: () -> SecretKey,
        v1LegacyKeyProvider: () -> SecretKey,
    ): String {
        val header = readHeader(input)
        return when (header.version) {
            VERSION_V1 -> {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                    init(Cipher.DECRYPT_MODE, v1LegacyKeyProvider(), GCMParameterSpec(TAG_BITS, header.iv))
                    updateAAD(header.encoded)
                }
                CipherInputStream(input, cipher).use { cipherInput ->
                    cipherInput.copyTo(output, BUFFER_SIZE)
                }
                header.originalName
            }
            VERSION_V2_ENVELOPE -> {
                val wrapIv = requireNotNull(header.wrapIv) { "Missing envelope IV" }
                val wrappedDek = requireNotNull(header.wrappedDek) { "Missing wrapped key" }
                val unwrapCipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                    init(Cipher.DECRYPT_MODE, v2KekProvider(), GCMParameterSpec(TAG_BITS, wrapIv))
                }
                val dekBytes = unwrapCipher.doFinal(wrappedDek)
                try {
                    val dek = SecretKeySpec(dekBytes, "AES")
                    val payloadCipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                        init(Cipher.DECRYPT_MODE, dek, GCMParameterSpec(TAG_BITS, header.iv))
                        updateAAD(header.encoded)
                    }
                    CipherInputStream(input, payloadCipher).use { cipherInput ->
                        cipherInput.copyTo(output, BUFFER_SIZE)
                    }
                } finally {
                    dekBytes.fill(0)
                }
                header.originalName
            }
            else -> throw IOException("Unsupported encrypted file version")
        }
    }

    fun readHeader(input: InputStream): ParsedHeader {
        val encoded = ByteArrayOutputStream()
        val actualMagic = readFully(input, magic.size).also { encoded.write(it) }
        if (!actualMagic.contentEquals(magic)) throw IOException("Unsupported encrypted file")

        val version = readByte(input).also { encoded.write(it) }
        if (version != VERSION_V1 && version != VERSION_V2_ENVELOPE) {
            throw IOException("Unsupported encrypted file version")
        }

        val nameLengthBytes = readFully(input, 2).also { encoded.write(it) }
        val nameLength = (((nameLengthBytes[0].toInt() and 0xff) shl 8) or
            (nameLengthBytes[1].toInt() and 0xff))
        if (nameLength !in 1..MAX_NAME_BYTES) throw IOException("Invalid encrypted filename")

        val nameBytes = readFully(input, nameLength).also { encoded.write(it) }
        val originalName = String(nameBytes, StandardCharsets.UTF_8)
        validateName(originalName)

        return if (version == VERSION_V1) {
            val iv = readFully(input, IV_SIZE).also { encoded.write(it) }
            ParsedHeader(originalName, iv, encoded.toByteArray(), version = VERSION_V1)
        } else {
            val wrapIvLen = readByte(input).also { encoded.write(it) }
            if (wrapIvLen !in 1..32) throw IOException("Invalid envelope IV length")
            val wrapIv = readFully(input, wrapIvLen).also { encoded.write(it) }

            val wrappedLenBytes = readFully(input, 2).also { encoded.write(it) }
            val wrappedLen = (((wrappedLenBytes[0].toInt() and 0xff) shl 8) or
                (wrappedLenBytes[1].toInt() and 0xff))
            if (wrappedLen !in 16..MAX_WRAPPED_KEY_BYTES) throw IOException("Invalid wrapped key length")
            val wrappedDek = readFully(input, wrappedLen).also { encoded.write(it) }

            val payloadIv = readFully(input, IV_SIZE).also { encoded.write(it) }
            ParsedHeader(
                originalName = originalName,
                iv = payloadIv,
                encoded = encoded.toByteArray(),
                version = VERSION_V2_ENVELOPE,
                wrapIv = wrapIv,
                wrappedDek = wrappedDek,
            )
        }
    }

    private fun encodeV1Header(originalName: String, iv: ByteArray): ByteArray {
        validateName(originalName)
        require(iv.size == IV_SIZE) { "Invalid encryption IV" }
        val nameBytes = originalName.toByteArray(StandardCharsets.UTF_8)
        require(nameBytes.size <= MAX_NAME_BYTES) { "Filename is too long" }
        return ByteArrayOutputStream().apply {
            write(magic)
            write(VERSION_V1)
            write((nameBytes.size shr 8) and 0xff)
            write(nameBytes.size and 0xff)
            write(nameBytes)
            write(iv)
        }.toByteArray()
    }

    private fun encodeV2Header(
        originalName: String,
        wrapIv: ByteArray,
        wrappedDek: ByteArray,
        payloadIv: ByteArray,
    ): ByteArray {
        validateName(originalName)
        require(wrapIv.size in 1..32) { "Invalid wrap IV" }
        require(wrappedDek.size in 16..MAX_WRAPPED_KEY_BYTES) { "Invalid wrapped DEK" }
        require(payloadIv.size == IV_SIZE) { "Invalid payload IV" }
        val nameBytes = originalName.toByteArray(StandardCharsets.UTF_8)
        require(nameBytes.size <= MAX_NAME_BYTES) { "Filename is too long" }
        return ByteArrayOutputStream().apply {
            write(magic)
            write(VERSION_V2_ENVELOPE)
            write((nameBytes.size shr 8) and 0xff)
            write(nameBytes.size and 0xff)
            write(nameBytes)
            write(wrapIv.size and 0xff)
            write(wrapIv)
            write((wrappedDek.size shr 8) and 0xff)
            write(wrappedDek.size and 0xff)
            write(wrappedDek)
            write(payloadIv)
        }.toByteArray()
    }

    private fun validateName(name: String) {
        require(name.isNotBlank()) { "Encrypted filename is empty" }
        require(name != "." && name != "..") { "Invalid encrypted filename" }
        require(!name.contains('/') && !name.contains('\\')) { "Invalid encrypted filename" }
        require(name.none(Char::isISOControl)) { "Invalid encrypted filename" }
    }

    private fun readFully(input: InputStream, length: Int): ByteArray {
        val result = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val read = input.read(result, offset, length - offset)
            if (read < 0) throw IOException("Truncated encrypted file")
            if (read == 0) continue
            offset += read
        }
        return result
    }

    private fun readByte(input: InputStream): Int {
        val value = input.read()
        if (value < 0) throw IOException("Truncated encrypted file")
        return value
    }

    private const val BUFFER_SIZE = 64 * 1024
}

data class FileEncryptionBatchResult(
    val succeeded: List<String>,
    val failures: List<String>,
) {
    val succeededCount: Int
        get() = succeeded.size
}

@Singleton
class FileEncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityRepository: SecurityRepository,
) {

    suspend fun encryptFiles(paths: List<String>): FileEncryptionBatchResult = withContext(Dispatchers.IO) {
        val invalidateOnChange = securityRepository.settings.first().invalidateKeysOnBiometricChange
        process(paths) { encryptFile(it, invalidateOnChange) }
    }

    suspend fun decryptFiles(paths: List<String>): FileEncryptionBatchResult = withContext(Dispatchers.IO) {
        val invalidateOnChange = securityRepository.settings.first().invalidateKeysOnBiometricChange
        process(paths) { decryptFile(it, invalidateOnChange) }
    }

    private suspend fun process(
        paths: List<String>,
        operation: suspend (String) -> Result<String>,
    ): FileEncryptionBatchResult {
        val succeeded = mutableListOf<String>()
        val failures = mutableListOf<String>()
        for (path in paths.distinct()) {
            operation(path)
                .onSuccess { succeeded += it }
                .onFailure { error ->
                    val detail = if (StrongBoxKeyFactory.isKeyInvalidated(error)) {
                        "Hardware key invalidated after biometric enrollment change"
                    } else {
                        error.message ?: "operation failed"
                    }
                    failures += "$path: $detail"
                }
        }
        return FileEncryptionBatchResult(succeeded, failures)
    }

    private suspend fun encryptFile(path: String, invalidateOnChange: Boolean): Result<String> = runCatching {
        val source = File(path).canonicalFile
        require(Files.isRegularFile(source.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            "Only regular local files can be encrypted"
        }
        require(!source.name.endsWith(ENCRYPTED_SUFFIX)) { "File is already encrypted" }
        val parent = source.parentFile ?: error("File has no parent folder")
        val destination = File(parent, source.name + ENCRYPTED_SUFFIX)
        require(!Files.exists(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            "Encrypted destination already exists"
        }
        val temporary = File(parent, ".fileexplorer-encrypt-" + UUID.randomUUID() + ".tmp")
        try {
            val kek = getOrCreateStrongBoxKek(invalidateOnChange)
            Files.newOutputStream(temporary.toPath(), java.nio.file.StandardOpenOption.CREATE_NEW).use { output ->
                source.inputStream().buffered().use { input ->
                    FileEncryptionEngine.encryptEnvelope(input, output, source.name, kek)
                }
            }
            moveIntoPlace(temporary, destination)
            destination.setLastModified(source.lastModified())
            require(Files.deleteIfExists(source.toPath())) { "Unable to remove plaintext file" }
            destination.absolutePath
        } catch (error: Exception) {
            runCatching { Files.deleteIfExists(temporary.toPath()) }
            runCatching { Files.deleteIfExists(destination.toPath()) }
            throw error
        }
    }

    private suspend fun decryptFile(path: String, invalidateOnChange: Boolean): Result<String> = runCatching {
        val source = File(path).canonicalFile
        require(source.name.endsWith(ENCRYPTED_SUFFIX)) { "Select a .encrypted file" }
        require(Files.isRegularFile(source.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            "Only regular local files can be decrypted"
        }
        val parent = source.parentFile ?: error("File has no parent folder")
        val header = source.inputStream().buffered().use { input -> FileEncryptionEngine.readHeader(input) }
        val destination = File(parent, header.originalName)
        require(!Files.exists(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            "Decrypted destination already exists"
        }
        val temporary = File(parent, ".fileexplorer-decrypt-" + UUID.randomUUID() + ".tmp")
        try {
            Files.newOutputStream(temporary.toPath(), java.nio.file.StandardOpenOption.CREATE_NEW).use { output ->
                source.inputStream().buffered().use { input ->
                    FileEncryptionEngine.decryptAuto(
                        input = input,
                        output = output,
                        v2KekProvider = { getOrCreateStrongBoxKek(invalidateOnChange) },
                        v1LegacyKeyProvider = { getLegacyV1Key() },
                    )
                }
            }
            moveIntoPlace(temporary, destination)
            destination.setLastModified(source.lastModified())
            require(Files.deleteIfExists(source.toPath())) { "Unable to remove encrypted file" }
            destination.absolutePath
        } catch (error: Exception) {
            runCatching { Files.deleteIfExists(temporary.toPath()) }
            runCatching { Files.deleteIfExists(destination.toPath()) }
            throw error
        }
    }

    private fun moveIntoPlace(source: File, destination: File) {
        try {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), destination.toPath())
        }
    }

    private fun getOrCreateStrongBoxKek(invalidateOnChange: Boolean): SecretKey =
        StrongBoxKeyFactory.getOrCreateAesGcmKey(
            context = context,
            alias = KEYSTORE_ALIAS_V2_STRONGBOX,
            requireUserAuth = true,
            authValiditySeconds = 15,
            invalidateOnNewBiometric = invalidateOnChange,
        )

    private fun getLegacyV1Key(): SecretKey =
        StrongBoxKeyFactory.getOrCreateAesGcmKey(
            context = context,
            alias = KEYSTORE_ALIAS_V1,
            requireUserAuth = false,
        )

    companion object {
        const val ENCRYPTED_SUFFIX = ".encrypted"
        internal const val KEYSTORE_ALIAS_V1 = "file_explorer_file_encryption_v1"
        internal const val KEYSTORE_ALIAS_V2_STRONGBOX = "file_explorer_file_encryption_v2_sb"
    }
}
