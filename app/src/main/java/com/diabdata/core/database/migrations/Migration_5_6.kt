package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS weight_entries_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                value REAL NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO weight_entries_new (id, date, value)
            SELECT id, date, weightKg
            FROM weight_entries
            """.trimIndent()
        )

        db.execSQL("DROP TABLE weight_entries")

        db.execSQL("ALTER TABLE weight_entries_new RENAME TO weight_entries")
    }
}
