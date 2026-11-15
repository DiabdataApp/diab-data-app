package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medications (
                cipGtin TEXT NOT NULL PRIMARY KEY,
                insulin TEXT NOT NULL,
                treatmentType TEXT NOT NULL,
                fullName TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
