package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE appointments ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE appointments ADD COLUMN notes TEXT DEFAULT ''")
        db.execSQL("ALTER TABLE appointments ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            "ALTER TABLE diagnosis_date_entries ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''"
        )
        db.execSQL(
            "ALTER TABLE diagnosis_date_entries ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
        )

        db.execSQL("ALTER TABLE hba1c_entries ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE hba1c_entries ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE treatments ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE treatments ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE weight_entries ADD COLUMN createdAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE weight_entries ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
    }
}
