package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE diagnosis_date_entries
            RENAME TO important_date_entries;
        """
        )

        db.execSQL(
            """
            ALTER TABLE important_date_entries
            RENAME COLUMN diagnosis TO importantDate;
        """
        )

        db.execSQL(
            """
            ALTER TABLE important_date_entries
            ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '2025-01-01';
        """
        )

        db.execSQL(
            """
            ALTER TABLE appointments
            ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '2025-01-01';
        """
        )

        db.execSQL(
            """
            ALTER TABLE hba1c_entries
            ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '2025-01-01';
        """
        )

        db.execSQL(
            """
            ALTER TABLE treatments
            ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '2025-01-01';
        """
        )

        db.execSQL(
            """
            ALTER TABLE weight_entries
            ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '2025-01-01';
        """
        )
    }
}
