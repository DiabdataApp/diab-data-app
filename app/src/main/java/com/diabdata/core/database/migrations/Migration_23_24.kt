package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE user_preferences_new (
                id INTEGER NOT NULL PRIMARY KEY,
                automaticBackupEnabled INTEGER NOT NULL DEFAULT 0,
                frequency TEXT NOT NULL DEFAULT 'weekly',
                lastBackupDate TEXT DEFAULT NULL,
                backupPath TEXT DEFAULT NULL,
                expirationReminder INTEGER NOT NULL DEFAULT 0,
                appointmentReminder INTEGER NOT NULL DEFAULT 0,
                backupEncryptionEnabled INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO user_preferences_new (
                id, automaticBackupEnabled, frequency, lastBackupDate, backupPath, expirationReminder, appointmentReminder, backupEncryptionEnabled
            )
            SELECT 
                0, automaticBackupEnabled, frequency, lastBackupDate, backupPath, expirationReminder, appointmentReminder, 0
            FROM user_preferences
            """.trimIndent()
        )

        db.execSQL("DROP TABLE user_preferences")
        db.execSQL("ALTER TABLE user_preferences_new RENAME TO user_preferences")
    }
}
