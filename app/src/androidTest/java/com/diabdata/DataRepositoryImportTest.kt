package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DataRepository
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.model.Weight
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class DataRepositoryImportTest {
    private lateinit var db: DiabDataDatabase
    private lateinit var repository: DataRepository

    private val older = Instant.fromEpochMilliseconds(1_000)
    private val newer = Instant.fromEpochMilliseconds(2_000)

    private fun weight(
        entryUuid: String,
        id: Int = 0,
        value: Float = 70f,
        date: LocalDate = LocalDate.of(2026, 1, 15),
        createdAt: Instant = older,
        updatedAt: Instant = older,
    ): Weight {
        return Weight(
            entryUuid = entryUuid,
            id = id,
            value = value,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isArchived = false,
        )
    }

    private fun device(
        entryUuid: String,
        id: Int = 0,
        date: LocalDate = LocalDate.of(2026, 1, 15),
        lifeSpan: Int = 3,
        deviceType: MedicalDeviceInfoType = MedicalDeviceInfoType.WIRELESS_PATCH,
        name: String = "Omnipod 1",
        isFaulty: Boolean = false,
        isReported: Boolean = false,
        isArchived: Boolean = false,
        isLifeSpanOver: Boolean = false,
        createdAt: Instant = older,
        updatedAt: Instant = older,
    ): MedicalDevice {
        return MedicalDevice(
            id = id,
            date = date,
            lifeSpanEndDate = date.plusDays(lifeSpan.toLong()),
            name = name,
            batchNumber = "12345678",
            serialNumber = "12345678",
            referenceNumber = "12345678",
            manufacturer = "Insulet",
            deviceType = deviceType,
            createdAt = createdAt,
            isArchived = isArchived,
            lifeSpan = lifeSpan,
            isFaulty = isFaulty,
            isReported = isReported,
            isLifeSpanOver = isLifeSpanOver,
            updatedAt = updatedAt,
            entryUuid = entryUuid
        )
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        repository = DataRepository(
            weightDao = db.weightDao(),
            hba1cDao = db.hba1cDao(),
            importantDateDao = db.importantDateDao(),
            appointmentDao = db.appointmentDao(),
            treatmentDao = db.treatmentDao(),
            medicationDao = db.medicationDao(),
            medicalDevicesDao = db.medicalDevicesDao(),
            medicalDeviceInfo = db.medicalDevicesInfoDao(),
            userDetailsDao = db.userDetailsDao(),
            userPreferencesDao = db.userPreferencesDao(),
            database = db,
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun reimportingSameWeightsBackupTwiceDoesNotDuplicate() = runBlocking {
        val weightsBackup = listOf(
            weight(entryUuid = "weight-1", id = 1),
            weight(entryUuid = "weight-2", id = 2, value = 76f),
        )

        repository.importWeights(weightsBackup)
        repository.importWeights(weightsBackup)

        val allWeights = db.weightDao().getAllWeightsFlow().first()

        assertEquals(2, allWeights.size)
    }

    @Test
    @Throws(Exception::class)
    fun reimportingSameDevicesBackupTwiceDoesNotDuplicate() = runBlocking {
        val devicesBackup = listOf(
            device(entryUuid = "device-1", id = 1),
            device(entryUuid = "device-2", id = 2, name = "Omnipod 2"),
        )

        repository.importMedicalDevices(devicesBackup)
        repository.importMedicalDevices(devicesBackup)

        val allDevices = db.medicalDevicesDao().getAllMedicalDevices().first()

        assertEquals(2, allDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun importedPreferencesWithLegacyIdAreReadable() = runBlocking {
        repository.importUserPreferences(UserPreferences(id = 1, frequency = "monthly"))

        val prefs = repository.getUserPreferences().first()

        assertEquals("monthly", prefs?.frequency)
    }

    @Test
    @Throws(Exception::class)
    fun importedUserDetailsWithLegacyIdAreReadable() = runBlocking {
        repository.importUserDetails(UserDetails(id = 1, firstName = "ImportTest"))

        val details = repository.getUserDetails().first()

        assertEquals("ImportTest", details?.firstName)
    }

    @Test
    @Throws(Exception::class)
    fun keepMoreRecentLocalEntry() = runBlocking {
        db.weightDao().insert(weight(entryUuid = "weight-a", id = 1, value = 70f, updatedAt = newer))
        repository.importWeights(listOf(weight(entryUuid = "weight-a", id = 1, value = 65f, updatedAt = older)))

        val allWeights = db.weightDao().getAllWeightsFlow().first()

        assertEquals(70f, allWeights.single().value)
    }

    @Test
    @Throws(Exception::class)
    fun keepNewerBackupWithPreservedLocalId() = runBlocking {
        db.weightDao().insert(weight(entryUuid = "weight-a", id = 1, value = 70f, updatedAt = older))
        repository.importWeights(listOf(weight(entryUuid = "weight-a", id = 42, value = 65f, updatedAt = newer)))

        val allWeights = db.weightDao().getAllWeightsFlow().first()
        val result = allWeights.single()
        assertEquals(1, result.id)
        assertEquals(65f, result.value)
    }

    @Test
    @Throws(Exception::class)
    fun keepLocalAndBackupWithDifferentUuids() = runBlocking {
        db.weightDao().insert(weight(entryUuid = "weight-a", id = 1, value = 70f))
        repository.importWeights(listOf(weight(entryUuid = "weight-b", id = 1, value = 80f)))

        val allWeights = db.weightDao().getAllWeightsFlow().first()
        val weightsList = allWeights.map {it.value}.sorted()

        assertEquals(2, allWeights.size)
        assertEquals(listOf(70f, 80f), weightsList)
    }

    @Test
    @Throws(Exception::class)
    fun failedImportRollsBackEverything() = runBlocking {
        try {
            repository.inTransaction {
                repository.importWeights(listOf(weight(entryUuid = "weight-a")))
                throw IllegalStateException("simulated failure")
            }
        } catch (e: IllegalStateException) {
            // expected: simulated failure
        }

        val allWeights = db.weightDao().getAllWeightsFlow().first()

        assertEquals(0, allWeights.size)
    }
}