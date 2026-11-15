package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE user_preferences
            ADD COLUMN expirationReminder INTEGER NOT NULL DEFAULT 0;
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE user_preferences
            ADD COLUMN appointmentReminder INTEGER NOT NULL DEFAULT 0;
            """.trimIndent()
        )
    }
}
