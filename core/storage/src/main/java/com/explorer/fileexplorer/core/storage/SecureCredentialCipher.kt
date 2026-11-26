package com.explorer.fileexplorer.core.storage

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureCredentialCipher @Inject constructor() : CredentialCipher {

    override fun isEncrypted(value: String): Boolean =
        value.startsWith(PREFIX_V2) || value.startsWith(PREFIX_V1)

    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty() || isEncrypted(plainText)) return plainText
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateStrongBoxKey())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return "$PREFIX_V2${encode(cipher.iv)}:${encode(encrypted)}"
    }

    override fun decrypt(storedText: String): String {
        if (storedText.isEmpty() || !isEncrypted(storedText)) return storedText
        val (prefix, key) = if (storedText.startsWith(PREFIX_V2)) {
            PREFIX_V2 to getOrCreateStrongBoxKey()
        } else {
            PREFIX_V1 to getLegacyV1Key()
        }
        val parts = storedText.removePrefix(prefix).split(":", limit = 2)
        require(parts.size == 2) { "Invalid encrypted credential payload" }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(GCM_TAG_BITS, decode(parts[0])),
        )
        return String(cipher.doFinal(decode(parts[1])), Charsets.UTF_8)
    }

    private fun getOrCreateStrongBoxKey(): SecretKey =
        StrongBoxKeyFactory.getOrCreateAesGcmKey(
            context = null,
            alias = KEY_ALIAS_V2_STRONGBOX,
            requireUserAuth = false,
        )

    private fun getLegacyV1Key(): SecretKey =
        StrongBoxKeyFactory.getOrCreateAesGcmKey(
            context = null,
            alias = KEY_ALIAS_V1,
            requireUserAuth = false,
        )

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val KEY_ALIAS_V1 = "fileexplorer_secure_credentials_v1"
        const val KEY_ALIAS_V2_STRONGBOX = "fileexplorer_secure_credentials_v2_sb"
        const val PREFIX_V1 = "keystore-aes-gcm:v1:"
        const val PREFIX_V2 = "keystore-aes-gcm:v2:"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
