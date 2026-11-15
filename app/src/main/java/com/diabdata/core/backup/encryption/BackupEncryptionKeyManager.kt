package com.diabdata.core.backup.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class BackupEncryptionKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val backupPrefs = context.getSharedPreferences(
        "backup_encryption_prefs",
        Context.MODE_PRIVATE
    )
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keystoreAlias = "backup_encryption_keystore_key"
    private val mutex = Mutex()

    private fun generateKeystoreKeyIfNeeded() {
        if (!keyStore.containsAlias(keystoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val spec = KeyGenParameterSpec.Builder(
                keystoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    fun hasPassword(): Boolean = backupPrefs.contains("encrypted_password")

    suspend fun setPassword(password: String): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                generateKeystoreKeyIfNeeded()
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
                val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
                backupPrefs.edit {
                    putString(
                        "encrypted_password",
                        Base64.encodeToString(encrypted, Base64.NO_WRAP)
                    )
                    putString("password_iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPassword(): Result<String?> = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (!hasPassword()) return@withContext Result.success(null)
                val encrypted = Base64.decode(
                    backupPrefs.getString("encrypted_password", null),
                    Base64.NO_WRAP
                )
                val iv = Base64.decode(backupPrefs.getString("password_iv", null), Base64.NO_WRAP)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
                Result.success(String(cipher.doFinal(encrypted), Charsets.UTF_8))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun clearPassword(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                backupPrefs.edit {
                    remove("encrypted_password")
                    remove("password_iv")
                }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getSecretKey(): SecretKey =
        (keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry).secretKey
}
