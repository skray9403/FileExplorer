package com.explorer.fileexplorer.feature.security

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileEncryptionEngineTest {
    private val key = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")

    @Test
    fun `encrypted payload round trips with original filename`() {
        val plaintext = ByteArray(128 * 1024 + 17) { (it * 31).toByte() }
        val encrypted = encrypt(plaintext, "résumé.txt")
        val decrypted = ByteArrayOutputStream()

        val originalName = FileEncryptionEngine.decrypt(
            ByteArrayInputStream(encrypted),
            decrypted,
            key,
        )

        assertEquals("résumé.txt", originalName)
        assertContentEquals(plaintext, decrypted.toByteArray())
    }

    @Test
    fun `tampered ciphertext fails authentication`() {
        val encrypted = encrypt("secret contents".toByteArray(), "secret.txt")
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 0x01).toByte()

        assertFailsWith<Exception> {
            FileEncryptionEngine.decrypt(
                ByteArrayInputStream(encrypted),
                ByteArrayOutputStream(),
                key,
            )
        }
    }

    @Test
    fun `invalid header names are rejected before decryption`() {
        val encrypted = encrypt("contents".toByteArray(), "safe.txt")
        val nameStart = 4 + 1 + 2
        encrypted[nameStart] = '/'.code.toByte()

        assertFailsWith<IllegalArgumentException> {
            FileEncryptionEngine.readHeader(ByteArrayInputStream(encrypted))
        }
    }

    private fun encrypt(plaintext: ByteArray, name: String): ByteArray {
        val encrypted = ByteArrayOutputStream()
        FileEncryptionEngine.encrypt(
            ByteArrayInputStream(plaintext),
            encrypted,
            name,
            key,
        )
        return encrypted.toByteArray()
    }


    @Test
    fun `encryptEnvelope and decryptAuto round trip with 256-bit KEK`() {
        val plaintext = "Envelope encrypted test payload with secret data 12345".toByteArray()
        val originalName = "envelope_secret.pdf"
        val outEncrypted = ByteArrayOutputStream()

        FileEncryptionEngine.encryptEnvelope(
            input = ByteArrayInputStream(plaintext),
            output = outEncrypted,
            originalName = originalName,
            kek = key,
        )

        val encryptedBytes = outEncrypted.toByteArray()
        val outDecrypted = ByteArrayOutputStream()

        val restoredName = FileEncryptionEngine.decryptAuto(
            input = ByteArrayInputStream(encryptedBytes),
            output = outDecrypted,
            v2KekProvider = { key },
            v1KeyProvider = { key },
        )

        assertEquals(originalName, restoredName)
        assertContentEquals(plaintext, outDecrypted.toByteArray())
    }

    @Test
    fun `tampered envelope ciphertext fails authentication`() {
        val plaintext = "Tamper check payload".toByteArray()
        val outEncrypted = ByteArrayOutputStream()

        FileEncryptionEngine.encryptEnvelope(
            input = ByteArrayInputStream(plaintext),
            output = outEncrypted,
            originalName = "tamper.txt",
            kek = key,
        )

        val encrypted = outEncrypted.toByteArray()
        // Flip one bit in the payload ciphertext
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 0x01).toByte()

        assertFailsWith<Exception> {
            FileEncryptionEngine.decryptAuto(
                input = ByteArrayInputStream(encrypted),
                output = ByteArrayOutputStream(),
                v2KekProvider = { key },
                v1KeyProvider = { key },
            )
        }
    }

    @Test
    fun `legacy v1 payload decrypts via decryptAuto`() {
        val plaintext = "Legacy v1 payload to verify backward compatibility".toByteArray()
        val originalName = "legacy_doc.docx"
        val v1Encrypted = encrypt(plaintext, originalName)
        val outDecrypted = ByteArrayOutputStream()

        val restoredName = FileEncryptionEngine.decryptAuto(
            input = ByteArrayInputStream(v1Encrypted),
            output = outDecrypted,
            v2KekProvider = { key },
            v1KeyProvider = { key },
        )

        assertEquals(originalName, restoredName)
        assertContentEquals(plaintext, outDecrypted.toByteArray())
    }
}
