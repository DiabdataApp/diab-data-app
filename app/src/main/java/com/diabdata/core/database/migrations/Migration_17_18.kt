package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE medical_devices_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                lifeSpanEndDate TEXT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                batchNumber TEXT NOT NULL,
                serialNumber TEXT,
                referenceNumber TEXT,
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
                id, date, lifeSpanEndDate, name, batchNumber, serialNumber, referenceNumber, manufacturer, deviceType,
                lifeSpan, isFaulty, isReported, isLifeSpanOver,
                createdAt, isArchived, updatedAt
            )
            SELECT
                id,
                -- forcer la date à un ISO_LOCAL_DATE ou aujourd'hui si vide
                COALESCE(NULLIF(substr(date,1,10), ''), strftime('%Y-%m-%d','now')) AS date,
                -- lifeSpanEndDate = date + lifeSpan, fallback à today si date invalide
                COALESCE(
                    strftime('%Y-%m-%d', julianday(NULLIF(substr(date,1,10), strftime('%Y-%m-%d','')) ) + COALESCE(lifeSpan,0)),
                    strftime('%Y-%m-%d','now')
                ) AS lifeSpanEndDate,
                '' AS name,
                batchNumber,
                serialNumber,
                referenceNumber,
                manufacturer,
                deviceType,
                COALESCE(lifeSpan,0),
                COALESCE(isFaulty,0),
                COALESCE(isReported,0),
                COALESCE(isLifeSpanOver,0),
                COALESCE(NULLIF(substr(createdAt,1,10), ''), strftime('%Y-%m-%d','now')),
                COALESCE(isArchived,0),
                COALESCE(NULLIF(substr(updatedAt,1,10), ''), strftime('%Y-%m-%d','now'))
            FROM medical_devices;
            """.trimIndent()
        )

        db.execSQL("DROP TABLE medical_devices")
        db.execSQL("ALTER TABLE medical_devices_new RENAME TO medical_devices")
    }
}
