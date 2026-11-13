package com.diabdata.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diabdata.core.model.UNKNOWN_TIME_DEFAULT
import com.diabdata.core.model.newEntryUuid

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE `weight_entries_new` 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `date` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `value` REAL NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )
        
        db.execSQL(
            """
                INSERT INTO `weight_entries_new` (`id`, `date`, `createdAt`, `isArchived`, `value`, `updatedAt`, `entryUuid`)
                SELECT `id`, `date`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `value`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `weight_entries`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `weight_entries`")
        db.execSQL("ALTER TABLE `weight_entries_new` RENAME TO `weight_entries`")

        backfillUuidsAndIndex("weight_entries", db)

        db.execSQL(
            """
                CREATE TABLE `appointments_new` 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `date` TEXT NOT NULL, 
                    `doctor` TEXT NOT NULL, 
                    `type` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `notes` TEXT, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `appointments_new` (`id`, `date`, `doctor`, `type`, `createdAt`, `isArchived`, `notes`, `updatedAt`, `entryUuid`)
                SELECT `id`, `date`, `doctor`, `type`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `notes`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `appointments`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `appointments`")
        db.execSQL("ALTER TABLE `appointments_new` RENAME TO `appointments`")

        backfillUuidsAndIndex("appointments", db)

        db.execSQL(
            """
                CREATE TABLE `hba1c_entries_new` 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `date` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `value` REAL NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `hba1c_entries_new` (`id`, `date`, `createdAt`, `isArchived`, `value`, `updatedAt`, `entryUuid`)
                SELECT `id`, `date`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `value`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `hba1c_entries`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `hba1c_entries`")
        db.execSQL("ALTER TABLE `hba1c_entries_new` RENAME TO `hba1c_entries`")

        backfillUuidsAndIndex("hba1c_entries", db)

        db.execSQL(
            """
                CREATE TABLE `treatments_new`
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `expirationDate` TEXT NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `type` TEXT NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `treatments_new` (`id`, `expirationDate`, `name`, `createdAt`, `isArchived`, `type`, `updatedAt`, `entryUuid`)
                SELECT `id`, `expirationDate`, `name`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `type`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `treatments`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `treatments`")
        db.execSQL("ALTER TABLE `treatments_new` RENAME TO `treatments`")

        backfillUuidsAndIndex("treatments", db)

        db.execSQL(
            """
                CREATE TABLE `important_date_entries_new` 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `date` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `importantDate` TEXT NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `important_date_entries_new` (`id`, `date`, `createdAt`, `isArchived`, `importantDate`, `updatedAt`, `entryUuid`)
                SELECT `id`, `date`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `importantDate`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `important_date_entries`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `important_date_entries`")
        db.execSQL("ALTER TABLE `important_date_entries_new` RENAME TO `important_date_entries`")

        backfillUuidsAndIndex("important_date_entries", db)

        db.execSQL(
            """
                CREATE TABLE `medical_devices_new` 
                (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `date` TEXT NOT NULL, 
                    `lifeSpanEndDate` TEXT NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `batchNumber` TEXT NOT NULL, 
                    `serialNumber` TEXT, 
                    `referenceNumber` TEXT, 
                    `manufacturer` TEXT, 
                    `deviceType` TEXT NOT NULL, 
                    `createdAt` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `lifeSpan` INTEGER NOT NULL, 
                    `isFaulty` INTEGER NOT NULL, 
                    `isReported` INTEGER NOT NULL, 
                    `isLifeSpanOver` INTEGER NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `entryUuid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `medical_devices_new` (`id`, `date`, `lifeSpanEndDate`, `name`, `batchNumber`, `serialNumber`, `referenceNumber`, `manufacturer`, `deviceType`, `createdAt`, `isArchived`, `lifeSpan`, `isFaulty`, `isReported`, `isLifeSpanOver`, `updatedAt`, `entryUuid`)
                SELECT `id`, `date`, `lifeSpanEndDate`, `name`, `batchNumber`, `serialNumber`, `referenceNumber`, `manufacturer`, `deviceType`, COALESCE(CAST(strftime('%s', `createdAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), `isArchived`, `lifeSpan`, `isFaulty`, `isReported`, `isLifeSpanOver`, COALESCE(CAST(strftime('%s', `updatedAt`, 'utc') AS INTEGER)*1000, ${UNKNOWN_TIME_DEFAULT.toEpochMilliseconds()}), CAST(`id` AS TEXT)
                FROM `medical_devices`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `medical_devices`")
        db.execSQL("ALTER TABLE `medical_devices_new` RENAME TO `medical_devices`")

        backfillUuidsAndIndex("medical_devices", db)

        db.execSQL(
            """
                ALTER TABLE `user_preferences`
                ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        db.execSQL(
            """
                ALTER TABLE `user_details`
                ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

/**
 * Helper function that takes a table named [tableName] and updates its entryUuid column with a new UUID.
 * It also creates a unique index on the entryUuid column.
 * @param tableName The name of the table to backfill.
 * @param db The database connection.
 */
private fun backfillUuidsAndIndex(tableName: String, db: SupportSQLiteDatabase) {
    val ids: MutableList<Long> = mutableListOf()

    db.query(
        """
            SELECT `id` FROM `$tableName`
        """.trimIndent()
    ).use { cursor ->
        while (cursor.moveToNext()) {
            ids.add(cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        }
    }

    ids.forEach { entryId ->
        db.execSQL(
            """
                UPDATE `$tableName`
                SET `entryUuid` = ?
                WHERE `id` = ?
            """.trimIndent(),
            arrayOf<Any>(newEntryUuid(), entryId)
        )
    }

    db.execSQL(
        """
            CREATE UNIQUE INDEX `index_${tableName}_entryUuid` ON `$tableName` (`entryUuid`)
        """.trimIndent()
    )
}