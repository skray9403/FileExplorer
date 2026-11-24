package com.explorer.fileexplorer.core.storage

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

enum class KeyHardwareLevel {
    STRONGBOX,
    TEE,
    SOFTWARE,
}

object StrongBoxKeyFactory {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun hasStrongBox(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE) ?: true
    }

    fun isDeviceSecure(context: Context?): Boolean {
        if (context == null) return false
        val km = context.getSystemService(KeyguardManager::class.java) ?: return false
        return km.isDeviceSecure
    }

    fun getExistingKey(alias: String): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(alias, null) as? SecretKey
    }

    fun deleteKey(alias: String) {
        runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
        }
    }

    /**
     * Loads or generates an AES-256-GCM SecretKey backed by StrongBox when available,
     * transparently falling back to TEE Keystore on devices without a discrete secure element.
     */
    fun getOrCreateAesGcmKey(
        context: Context? = null,
        alias: String,
        requireUserAuth: Boolean = false,
        authValiditySeconds: Int = 15,
        invalidateOnNewBiometric: Boolean = false,
    ): SecretKey {
        getExistingKey(alias)?.let { return it }

        val effectiveAuth = requireUserAuth && isDeviceSecure(context)
        val tryStrongBox = hasStrongBox(context)

        return if (tryStrongBox) {
            try {
                generateKey(
                    alias = alias,
                    strongBox = true,
                    requireUserAuth = effectiveAuth,
                    authValiditySeconds = authValiditySeconds,
                    invalidateOnNewBiometric = invalidateOnNewBiometric,
                )
            } catch (_: ProviderException) {
                generateKey(
                    alias = alias,
                    strongBox = false,
                    requireUserAuth = effectiveAuth,
                    authValiditySeconds = authValiditySeconds,
                    invalidateOnNewBiometric = invalidateOnNewBiometric,
                )
            }
        } else {
            generateKey(
                alias = alias,
                strongBox = false,
                requireUserAuth = effectiveAuth,
                authValiditySeconds = authValiditySeconds,
                invalidateOnNewBiometric = invalidateOnNewBiometric,
            )
        }
    }

    /**
     * Inspects the actual Keystore hardware level of an existing key (or device capability if no key exists yet).
     */
    fun inspectHardwareLevel(context: Context, alias: String? = null): KeyHardwareLevel {
        val key = alias?.let { runCatching { getExistingKey(it) }.getOrNull() }
        if (key != null) {
            val info = runCatching {
                val factory = SecretKeyFactory.getInstance(key.algorithm, ANDROID_KEYSTORE)
                factory.getKeySpec(key, KeyInfo::class.java) as? KeyInfo
            }.getOrNull()
            if (info != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return when (info.securityLevel) {
                        KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeyHardwareLevel.STRONGBOX
                        KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeyHardwareLevel.TEE
                        else -> KeyHardwareLevel.SOFTWARE
                    }
                }
                @Suppress("DEPRECATION")
                return if (info.isInsideSecureHardware) KeyHardwareLevel.TEE else KeyHardwareLevel.SOFTWARE
            }
        }
        return if (hasStrongBox(context)) KeyHardwareLevel.STRONGBOX else KeyHardwareLevel.TEE
    }

    fun isKeyInvalidated(error: Throwable): Boolean {
        var cursor: Throwable? = error
        while (cursor != null) {
            if (cursor is KeyPermanentlyInvalidatedException) return true
            cursor = cursor.cause
        }
        return false
    }

    private fun generateKey(
        alias: String,
        strongBox: Boolean,
        requireUserAuth: Boolean,
        authValiditySeconds: Int,
        invalidateOnNewBiometric: Boolean,
    ): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && strongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        if (requireUserAuth) {
            builder.setUserAuthenticationRequired(true)
            builder.setInvalidatedByBiometricEnrollment(invalidateOnNewBiometric)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    authValiditySeconds.coerceAtLeast(1),
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                )
            } else {
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(authValiditySeconds.coerceAtLeast(1))
            }
        }

        generator.init(builder.build())
        return generator.generateKey()
    }
}
