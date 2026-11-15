package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medical_devices_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                batchNumber TEXT NOT NULL,
                serialNumber TEXT,
                manufacturer TEXT,
                deviceType TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                isArchived INTEGER NOT NULL,
                updatedAt TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO medical_devices_new (
                id, date, name, batchNumber, serialNumber, manufacturer, deviceType, createdAt, isArchived, updatedAt
            )
            SELECT id, date, '' AS name, batchNumber, serialNumber, manufacturer, type, createdAt, isArchived, updatedAt
            FROM medical_devices
            """.trimIndent()
        )

        db.execSQL("DROP TABLE medical_devices")
        db.execSQL("ALTER TABLE medical_devices_new RENAME TO medical_devices")
    }
}
