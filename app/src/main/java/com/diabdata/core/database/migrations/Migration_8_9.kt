package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medical_devices (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                batchNumber TEXT NOT NULL,
                serialNumber TEXT,
                manufacturer TEXT,
                type INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                isArchived INTEGER NOT NULL,
                updatedAt TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
