package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_preferences (
                id INTEGER NOT NULL PRIMARY KEY,
                automaticBackupEnabled INTEGER NOT NULL DEFAULT 0,
                frequency TEXT NOT NULL DEFAULT 'weekly',
                lastBackupDate TEXT DEFAULT NULL,
                backupPath TEXT DEFAULT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO user_preferences (id, automaticBackupEnabled, frequency)
            VALUES (0, 0, 'weekly')
            """.trimIndent()
        )
    }
}