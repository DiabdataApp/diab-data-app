package com.diabdata.core.database.encryption

import android.content.Context
import android.util.Log
import java.io.File
import net.zetetic.database.sqlcipher.SQLiteDatabase

class SQLCipherUtils {

    enum class State {
        DOES_NOT_EXIST,
        UNENCRYPTED,
        ENCRYPTED
    }

    fun getDatabaseState(context: Context, dbName: String): State =
        getDatabaseState(context.getDatabasePath(dbName))

    private fun getDatabaseState(dbPath: File): State {
        Log.d("SQLCipher", "Checking database state: ${dbPath.absolutePath}")
        if (!dbPath.exists()) {
            Log.d("SQLCipher", "Database file does not exist")
            return State.DOES_NOT_EXIST
        }
        return try {
            SQLiteDatabase.openDatabase(
                dbPath.absolutePath,
                "",
                null,
                SQLiteDatabase.OPEN_READONLY,
                null,
                null
            ).use { db ->
                db.version
                Log.d("SQLCipher", "Database opened without passphrase → UNENCRYPTED")
                State.UNENCRYPTED
            }
        } catch (e: Exception) {
            Log.d("SQLCipher", "Database cannot be opened without passphrase → ENCRYPTED")
            State.ENCRYPTED
        }
    }
}
