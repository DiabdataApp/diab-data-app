package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
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
                lifeSpan INTEGER NOT NULL DEFAULT 0,
                isFaulty INTEGER NOT NULL DEFAULT 0,
                isReported INTEGER NOT NULL DEFAULT 0,
                isLifeSpanOver INTEGER NOT NULL DEFAULT 0,
                createdAt TEXT NOT NULL,
                isArchived INTEGER NOT NULL,
                updatedAt TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO medical_devices_new (
                id, date, name, batchNumber, serialNumber, manufacturer, deviceType,
                lifeSpan, isFaulty, isReported, isLifeSpanOver,
                createdAt, isArchived, updatedAt
            )
            SELECT id, date, '' AS name, batchNumber, serialNumber, manufacturer, deviceType,
                   0 AS lifeSpan, 0 AS isFaulty, 0 AS isReported, 0 AS isLifeSpanOver,
                   createdAt, isArchived, updatedAt
            FROM medical_devices
            """.trimIndent()
        )

        db.execSQL("DROP TABLE medical_devices")

        db.execSQL("ALTER TABLE medical_devices_new RENAME TO medical_devices")
    }
}
