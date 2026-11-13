package com.diabdata.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diabdata.core.database.converters.DateConverters
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.MedicalDeviceInfoEntity
import com.diabdata.core.model.Medication
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.model.Weight
import com.diabdata.feature.appointments.data.AppointmentDao
import com.diabdata.feature.dataMatrixScanner.data.MedicationDao
import com.diabdata.feature.devices.data.MedicalDeviceDao
import com.diabdata.feature.devices.data.MedicalDevicesInfoDao
import com.diabdata.feature.hba1c.data.HBA1CDao
import com.diabdata.feature.importantDates.data.ImportantDateDao
import com.diabdata.feature.settings.data.UserPreferencesDao
import com.diabdata.feature.treatments.data.TreatmentDao
import com.diabdata.feature.userProfile.data.UserDetailsDao
import com.diabdata.feature.weight.data.WeightDao

@Database(
    entities = [
        Weight::class,
        Hba1c::class,
        Appointment::class,
        Treatment::class,
        ImportantDate::class,
        MedicalDevice::class,
        Medication::class,
        MedicalDeviceInfoEntity::class,
        UserDetails::class,
        UserPreferences::class
    ],
    version = 25,
    exportSchema = true
)

@TypeConverters(DateConverters::class)
abstract class DiabDataDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun hba1cDao(): HBA1CDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicalDevicesDao(): MedicalDeviceDao
    abstract fun medicalDevicesInfoDao(): MedicalDevicesInfoDao
    abstract fun userDetailsDao(): UserDetailsDao

    abstract fun userPreferencesDao(): UserPreferencesDao


    fun getAllTableNames(): List<String> {
        val db = openHelper.readableDatabase
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'room_master_table' AND name != 'android_metadata'"
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            tables.add(name)
        }
        cursor.close()
        return tables
    }

    fun clearAllDataAndReset() {
        runInTransaction {
            val exclusionList = listOf("medications", "medical_devices_infos", "user_preferences")
            val tables = getAllTableNames().filter { it !in exclusionList }
            tables.forEach { table ->
                openHelper.writableDatabase.execSQL("DELETE FROM $table")
                openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name='$table'")
            }
        }
        openHelper.writableDatabase.execSQL("VACUUM")
    }
}
