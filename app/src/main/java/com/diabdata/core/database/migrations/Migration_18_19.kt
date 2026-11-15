package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_details (
                id INTEGER NOT NULL PRIMARY KEY,
                firstName TEXT,
                lastName TEXT,
                profilePhotoPath TEXT,
                birthdate TEXT,
                gender TEXT,
                bloodType TEXT,
                diabetesType TEXT,
                diagnosisDate TEXT,
                endocrinologist TEXT,
                generalPractitioner TEXT,
                ophthalmologist TEXT,
                cardiologist TEXT,
                nephrologist TEXT,
                insulinPumpModel TEXT,
                cgmModel TEXT,
                insulinType TEXT,
                basalInsulinType TEXT,
                targetGlucoseMin REAL,
                targetGlucoseMax REAL,
                glucoseUnit TEXT,
                emergencyContactName TEXT,
                emergencyContactPhone TEXT
            )
            """.trimIndent()
        )
    }
}
