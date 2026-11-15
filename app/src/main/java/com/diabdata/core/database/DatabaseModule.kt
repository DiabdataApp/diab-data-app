package com.diabdata.core.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diabdata.core.database.encryption.SqlCipherKeyManager
import com.diabdata.core.database.migrations.ALL_MIGRATIONS
import com.diabdata.feature.appointments.data.AppointmentDao
import com.diabdata.feature.dataMatrixScanner.data.MedicationDao
import com.diabdata.feature.dataMatrixScanner.utils.MedicalDevicesInitializer
import com.diabdata.feature.dataMatrixScanner.utils.MedicationInitializer
import com.diabdata.feature.devices.data.MedicalDeviceDao
import com.diabdata.feature.devices.data.MedicalDevicesInfoDao
import com.diabdata.feature.hba1c.data.HBA1CDao
import com.diabdata.feature.importantDates.data.ImportantDateDao
import com.diabdata.feature.settings.data.UserPreferencesDao
import com.diabdata.feature.treatments.data.TreatmentDao
import com.diabdata.feature.userProfile.data.UserDetailsDao
import com.diabdata.feature.weight.data.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDiabDataDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<DiabDataDatabase>
    ): DiabDataDatabase {
        System.loadLibrary("sqlcipher")

        val keyManager = SqlCipherKeyManager(context)

        val encrypted = keyManager.isEncrypted()

        if (!encrypted) {
            keyManager.migrateToEncrypted()
        }

        return Room.databaseBuilder(
            context.applicationContext,
            DiabDataDatabase::class.java,
            "diabdata_database"
        )
            .openHelperFactory(keyManager.getSupportFactory())
            .fallbackToDestructiveMigration(false)
            .addMigrations(*ALL_MIGRATIONS)
            .addCallback(object : Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)

                    CoroutineScope(Dispatchers.IO).launch {
                        val database = databaseProvider.get()
                        val countMedicationInfo = database.medicationDao().countAll()
                        val countDeviceInfo = database.medicalDevicesInfoDao().countAll()

                        if (countMedicationInfo == 0) {
                            MedicationInitializer(context, database).initialize()
                        }

                        if (countDeviceInfo == 0) {
                            MedicalDevicesInitializer(context, database).initialize()
                        }
                    }
                }
            })
            .build()
            .also { Log.d("SQLCipher", "=== Database ready ===") }
    }

    @Provides
    fun weightDao(db: DiabDataDatabase): WeightDao = db.weightDao()

    @Provides
    fun hba1cDao(db: DiabDataDatabase): HBA1CDao = db.hba1cDao()

    @Provides
    fun appointmentDao(db: DiabDataDatabase): AppointmentDao = db.appointmentDao()

    @Provides
    fun treatmentDao(db: DiabDataDatabase): TreatmentDao = db.treatmentDao()

    @Provides
    fun importantDateDao(db: DiabDataDatabase): ImportantDateDao = db.importantDateDao()

    @Provides
    fun medicationDao(db: DiabDataDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun medicalDevicesDao(db: DiabDataDatabase): MedicalDeviceDao = db.medicalDevicesDao()

    @Provides
    fun medicalDevicesInfoDao(db: DiabDataDatabase): MedicalDevicesInfoDao =
        db.medicalDevicesInfoDao()

    @Provides
    fun userDetailsDao(db: DiabDataDatabase): UserDetailsDao = db.userDetailsDao()

    @Provides
    fun userPreferencesDao(db: DiabDataDatabase): UserPreferencesDao = db.userPreferencesDao()

    @Provides
    @Singleton
    fun dataRepository(
        weightDao: WeightDao,
        hba1cDao: HBA1CDao,
        appointmentDao: AppointmentDao,
        treatmentDao: TreatmentDao,
        importantDateDao: ImportantDateDao,
        medicationDao: MedicationDao,
        medicalDevicesDao: MedicalDeviceDao,
        medicalDeviceInfo: MedicalDevicesInfoDao,
        userDetailsDao: UserDetailsDao,
        userPreferencesDao: UserPreferencesDao,
        database: DiabDataDatabase
    ): DataRepository {
        val repository = DataRepository(
            weightDao = weightDao,
            hba1cDao = hba1cDao,
            appointmentDao = appointmentDao,
            treatmentDao = treatmentDao,
            importantDateDao = importantDateDao,
            medicationDao = medicationDao,
            medicalDevicesDao = medicalDevicesDao,
            medicalDeviceInfo = medicalDeviceInfo,
            userDetailsDao = userDetailsDao,
            userPreferencesDao = userPreferencesDao,
            database = database
        )
        return repository
    }
}
