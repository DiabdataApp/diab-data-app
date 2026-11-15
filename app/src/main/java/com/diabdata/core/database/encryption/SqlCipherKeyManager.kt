package com.diabdata.core.database.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

private const val DB_NAME = "diabdata_database"

class SqlCipherKeyManager(private val context: Context) {
    val sqlCipherPrefs = context.getSharedPreferences("sqlcipher_prefs", Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        initialize()
    }

    private fun initialize() {
        generateKeystoreKeyIfNeeded()
        if (!sqlCipherPrefs.contains("encrypted_key")) {
            generateAndEncryptSqlCipherKey()
        }
    }

    private fun generateKeystoreKeyIfNeeded() {
        if (!keyStore.containsAlias("sqlcipher_keystore_key")) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                "sqlcipher_keystore_key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        } else {
        }
    }

    fun isEncrypted(): Boolean {
        if (sqlCipherPrefs.getBoolean("is_encrypted", false)) {
            return true
        }
        val state = SQLCipherUtils().getDatabaseState(context, DB_NAME)
        return state == SQLCipherUtils.State.ENCRYPTED
    }

    private fun generateAndEncryptSqlCipherKey() {
        val secretKey = getSecretKey("sqlcipher_keystore_key")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val sqlCipherKey = ByteArray(32)
        SecureRandom().nextBytes(sqlCipherKey)

        val encryptedKey = cipher.doFinal(sqlCipherKey)
        val iv = cipher.iv

        sqlCipherPrefs.edit {
            putString("encrypted_key", Base64.encodeToString(encryptedKey, Base64.NO_WRAP))
            putString("encryption_iv", Base64.encodeToString(iv, Base64.NO_WRAP))
        }
    }

    private fun getDecryptedSqlCipherKey(keyAlias: String, key: String, iv: String): ByteArray {
        val encryptedKey = Base64.decode(key, Base64.NO_WRAP)
        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)

        val secretKey = getSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))

        return cipher.doFinal(encryptedKey)
    }

    private fun getSecretKey(keyAlias: String): SecretKey =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey

    fun getSupportFactory(): SupportOpenHelperFactory {
        val encryptedKey = sqlCipherPrefs.getString("encrypted_key", null).orEmpty()
        val iv = sqlCipherPrefs.getString("encryption_iv", null).orEmpty()
        val decryptedKey = getDecryptedSqlCipherKey("sqlcipher_keystore_key", encryptedKey, iv)
        val hexPassphrase = decryptedKey.joinToString("") { "%02x".format(it) }
        return SupportOpenHelperFactory(hexPassphrase.toByteArray())
    }

    fun migrateToEncrypted() {
        val dbPath = context.getDatabasePath(DB_NAME)
        val dbTemp = context.getDatabasePath("${DB_NAME}_encrypted")
        if (dbTemp.exists()) dbTemp.delete()

        val encryptedKey = sqlCipherPrefs.getString("encrypted_key", null).orEmpty()
        val iv = sqlCipherPrefs.getString("encryption_iv", null).orEmpty()
        val decryptedKeyBytes = getDecryptedSqlCipherKey("sqlcipher_keystore_key", encryptedKey, iv)
        val hexPassphrase = decryptedKeyBytes.joinToString("") { "%02x".format(it) }

        SQLiteDatabase.openOrCreateDatabase(
            dbTemp.absolutePath,
            hexPassphrase,
            null,
            null,
            null
        ).use { db ->
            db.execSQL("ATTACH DATABASE '${dbPath.absolutePath}' AS plaintext KEY ''")
            db.rawQuery("SELECT sqlcipher_export('main', 'plaintext')", null)?.use { cursor ->
                cursor.moveToFirst()
            }
            db.execSQL("DETACH DATABASE plaintext")
        }

        dbPath.delete()
        File(dbPath.absolutePath + "-shm").delete()
        File(dbPath.absolutePath + "-wal").delete()
        dbTemp.renameTo(dbPath)
        sqlCipherPrefs.edit { putBoolean("is_encrypted", true) }
    }
}
