package com.diabdata

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.database.migrations.ALL_MIGRATIONS
import com.diabdata.core.database.migrations.MIGRATION_20_21
import com.diabdata.core.database.migrations.MIGRATION_21_22
import com.diabdata.core.database.migrations.MIGRATION_22_23
import com.diabdata.core.database.migrations.MIGRATION_23_24
import com.diabdata.core.database.migrations.MIGRATION_24_25
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val diabDataTestDb = "migration-test"

    // Array of all migrations.
    private val migrationsList = ALL_MIGRATIONS

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = DiabDataDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory()
    )

    fun SupportSQLiteDatabase.insertRow(table: String, vararg values: Pair<String, Any?>) {
        val cv = ContentValues()
        values.forEach { (k, v) ->
            when (v) {
                null -> cv.putNull(k)
                is Int -> cv.put(k, v)
                is String -> cv.put(k, v)
                is Long -> cv.put(k, v)
                is Float -> cv.put(k, v)
                is Double -> cv.put(k, v)
                else -> error($$"MIGRATION TEST ERROR - UNSUPPORTED TYPE: ${v::class}")
            }
        }
        insert(
            table = table, conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE, values = cv
        )
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(diabDataTestDb, 19).use {}

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DiabDataDatabase::class.java,
            diabDataTestDb
        ).addMigrations(*migrationsList).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate20To21Test() {
        helper.createDatabase(diabDataTestDb, 20).use { db ->
            db.execSQL(
                """
                    INSERT INTO appointments 
                    (id, date, doctor, type, createdAt, isArchived, notes, updatedAt)
                    VALUES (1, '2026-01-15', 'Dr. Dre', 'APPOINTMENT', '2026-01-15', 0, '', '2026-01-15')
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(diabDataTestDb, 21, true, MIGRATION_20_21)
            .use { updatedDb ->
                updatedDb.query(
                    """
                    SELECT date FROM appointments
                    """.trimIndent()
                ).use { updatedAppointment ->
                    updatedAppointment.moveToFirst()

                    assertEquals(1, updatedAppointment.count)
                    assertEquals("2026-01-15T00:00", updatedAppointment.getString(0))
                }
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate21TO22Test() {
        helper.createDatabase(diabDataTestDb, 21).use { }

        helper.runMigrationsAndValidate(diabDataTestDb, 22, true, MIGRATION_21_22)
            .use { updatedDb ->
                updatedDb.query(
                    """
                        SELECT id, automaticBackupEnabled, frequency FROM user_preferences
                    """.trimIndent()
                ).use { cursor ->
                    assertEquals(1, cursor.count)

                    cursor.moveToFirst()
                    assertEquals(0, cursor.getInt(0)) // ID == 0
                    assertEquals(0, cursor.getInt(1)) // automaticBackupEnabled == false
                    assertEquals("weekly", cursor.getString(2)) // frequency == weekly
                }
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To23Test() {
        helper.createDatabase(diabDataTestDb, 22).use { db ->
            db.execSQL(
                """
                INSERT INTO user_preferences 
                (id, automaticBackupEnabled, frequency, lastBackupDate, backupPath)
                VALUES (1, 0, 'WEEKLY', NULL, NULL)
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(diabDataTestDb, 23, true, MIGRATION_22_23)
            .use { updatedDb ->
                updatedDb.execSQL(
                    """
                    UPDATE user_preferences 
                    SET expirationReminder = 1, appointmentReminder = 0 
                    WHERE id = 1
                    """.trimIndent()
                )
                updatedDb.query(
                    """
                    SELECT expirationReminder, appointmentReminder 
                    FROM user_preferences 
                    WHERE id = 1
                    """.trimIndent()
                ).use { cursor ->
                    cursor.moveToFirst()
                    assertEquals(1, cursor.count)
                    assertEquals(1, cursor.getInt(0))
                    assertEquals(0, cursor.getInt(1))
                }
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate23To24Test() {
        helper.createDatabase(diabDataTestDb, 23).use { db ->
            db.insertRow(
                "user_preferences",
                "id" to 1,
                "automaticBackupEnabled" to 0,
                "frequency" to "WEEKLY",
                "lastBackupDate" to null,
                "backupPath" to null,
                "expirationReminder" to 0,
                "appointmentReminder" to 0,
            )
        }

        helper.runMigrationsAndValidate(diabDataTestDb, 24, true, MIGRATION_23_24)
            .use { updatedDb ->
                updatedDb.query(
                    """
                SELECT id, backupEncryptionEnabled 
                FROM user_preferences
                """.trimIndent()
                ).use { cursor ->
                    assertEquals(1, cursor.count)
                    cursor.moveToFirst()
                    assertEquals(0, cursor.getInt(0))  // id corrigé à 0
                    assertEquals(0, cursor.getInt(1))  // valeur par défaut
                }
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate24To25Test() {
        helper.createDatabase(diabDataTestDb, 24).use { db ->
            db.insertRow(
                "user_preferences",
                "id" to 0,
                "automaticBackupEnabled" to 0,
                "frequency" to "WEEKLY",
                "lastBackupDate" to null,
                "backupPath" to null,
                "expirationReminder" to 0,
                "appointmentReminder" to 0,
                "backupEncryptionEnabled" to 0
            )

            db.insertRow(
                "user_details",
                "id" to 0,
                "firstName" to "John",
                "lastName" to "Doe",
                "profilePhotoPath" to null,
                "birthdate" to null,
                "gender" to null,
                "bloodType" to null,
                "diabetesType" to null,
                "diagnosisDate" to null,
                "endocrinologist" to null,
                "generalPractitioner" to null,
                "ophthalmologist" to null,
                "cardiologist" to null,
                "nephrologist" to null,
                "insulinPumpModel" to null,
                "cgmModel" to null,
                "insulinType" to null,
                "basalInsulinType" to null,
                "targetGlucoseMin" to null,
                "targetGlucoseMax" to null,
                "glucoseUnit" to null,
                "emergencyContactName" to null,
                "emergencyContactPhone" to null
            )

            db.insertRow(
                "weight_entries",
                "id" to 1,
                "date" to "2023-01-01",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "value" to 70.0,
                "updatedAt" to "2026-01-15"
            )

            db.insertRow(
                "weight_entries",
                "id" to 2,
                "date" to "2023-08-09",
                "createdAt" to "",
                "isArchived" to 0,
                "value" to 72.0,
                "updatedAt" to ""
            )

            db.insertRow(
                "appointments",
                "id" to 1,
                "date" to "2023-01-01",
                "doctor" to "Dr. Dre",
                "type" to "APPOINTMENT",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "notes" to null,
                "updatedAt" to "2026-01-15"
            )

            db.insertRow(
                "hba1c_entries",
                "id" to 1,
                "date" to "2023-01-01",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "value" to 7.0,
                "updatedAt" to "2026-01-15"
            )

            db.insertRow(
                "treatments",
                "id" to 1,
                "expirationDate" to "2023-01-01",
                "name" to "Insulin",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "type" to "FAST_ACTING_INSULIN_VIAL",
                "updatedAt" to "2026-01-15"
            )

            db.insertRow(
                "important_date_entries",
                "id" to 1,
                "date" to "2023-01-01",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "importantDate" to "2026-01-15",
                "updatedAt" to "2026-01-15"
            )

            db.insertRow(
                "medical_devices",
                "id" to 1,
                "date" to "2023-01-01",
                "lifeSpanEndDate" to "2026-01-15",
                "name" to "Insulin Pump",
                "batchNumber" to "123456",
                "serialNumber" to null,
                "referenceNumber" to null,
                "manufacturer" to null,
                "deviceType" to "INSULIN_PUMP",
                "createdAt" to "2026-01-15",
                "isArchived" to 0,
                "lifeSpan" to 365,
                "isFaulty" to 0,
                "isReported" to 0,
                "isLifeSpanOver" to 0,
                "updatedAt" to "2026-01-15"
            )
        }

        val expectedCreatedAtMillis = LocalDate.of(2026, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val weightsUuids = mutableListOf<String>()

        helper.runMigrationsAndValidate(diabDataTestDb, 25, true, MIGRATION_24_25)
            .use { updatedDb ->
                updatedDb.query(
                    """
                    SELECT * FROM weight_entries ORDER BY id
                    """.trimIndent()
                ).use { cursor ->
                    assertEquals(2, cursor.count)
                    cursor.moveToFirst()
                    assertEquals(expectedCreatedAtMillis, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
                    weightsUuids.add(cursor.getString(cursor.getColumnIndexOrThrow("entryUuid")))
                    cursor.moveToNext()
                    assertEquals(0L, cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")))
                    weightsUuids.add(cursor.getString(cursor.getColumnIndexOrThrow("entryUuid")))
                }

                assertEquals(36, weightsUuids[0].length)
                assertEquals(36, weightsUuids[1].length)

                val uuid1 = UUID.fromString(weightsUuids[0])
                val uuid2 = UUID.fromString(weightsUuids[1])

                assertEquals(7, uuid1.version())
                assertEquals(7, uuid2.version())

                assertNotEquals(uuid1, uuid2)

                updatedDb.query(
                    """
                    SELECT * FROM appointments
                    """.trimIndent()
                ).use { cursor ->
                    assertEquals(1, cursor.count)
                }

                updatedDb.query(
                    """
                    SELECT * FROM user_preferences
                    """.trimIndent()
                ).use { cursor ->
                    assertEquals(1, cursor.count)
                    cursor.moveToFirst()
                    assertEquals(0L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")))
                }

                updatedDb.query(
                    """
                    SELECT * FROM user_details
                    """.trimIndent()
                ).use { cursor ->
                    assertEquals(1, cursor.count)
                    cursor.moveToFirst()
                    assertEquals(0L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")))
                }
            }
    }
}