package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DataRepository
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.model.Weight
import com.diabdata.core.utils.data.GsonFactory
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private val gson = GsonFactory.create()

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

    private fun legacyWeight(updatedAt: String = "2026-01-15", isArchived: Boolean = false): Weight {
        val json = """
            {
                "id": 1,
                "date": "2026-01-15",
                "is_archived": $isArchived,
                "value": 67.0,
                "created_at": "2026-01-15",
                "updated_at": "$updatedAt"
            }
        """.trimIndent()

        return gson.fromJson(json, Weight::class.java)
    }

    private fun legacyHba1c(updatedAt: String = "2026-01-15", isArchived: Boolean = false): Hba1c {
        val json = """
            {
                "id": 1,
                "date": "2026-01-15",
                "is_archived": $isArchived,
                "value": 6.0,
                "created_at": "2026-01-15",
                "updated_at": "$updatedAt"
            }
        """.trimIndent()

        return gson.fromJson(json, Hba1c::class.java)
    }

    private fun legacyImportantDate(updatedAt: String = "2026-01-15", isArchived: Boolean = false): ImportantDate {
        val json = """
            {
                "id": 1,
                "date": "2026-01-15",
                "important_date_value": "A random important date label",
                "is_archived": $isArchived,
                "created_at": "2026-01-15",
                "updated_at": "$updatedAt"
            }
        """.trimIndent()

        return gson.fromJson(json, ImportantDate::class.java)
    }

    private fun legacyAppointment(updatedAt: String = "2026-01-15", isArchived: Boolean = false): Appointment {
        val json = """
            {
                "id": 1,
                "date": "2026-01-15T10:30:00",
                "doctor": "Pr. John Doe",
                "notes": "",
                "type": "appointment",
                "is_archived": $isArchived,
                "created_at": "2026-01-15",
                "updated_at": "$updatedAt"
            }
        """.trimIndent()

        return gson.fromJson(json, Appointment::class.java)
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

    @Test
    @Throws(Exception::class)
    fun importingLegacyWeightWithoutUuidDoesNotCrash() = runBlocking {
        val json = """
            {
                "id": 0,
                "date": "2026-01-15",
                "is_archived": false,
                "value": 67.0,
                "created_at": "2026-01-15",
                "updated_at": "2026-01-15"
            }
        """.trimIndent()

        val legacyWeight = GsonFactory.create().fromJson(json, Weight::class.java)

        repository.importWeights(listOf(legacyWeight))

        val allWeights = db.weightDao().getAllWeightsFlow().first()
        val result = allWeights.single()

        assertEquals(67f, result.value)
        assertFalse(result.entryUuid.isBlank())
    }

    @Test
    @Throws(Exception::class)
    fun legacyBackupReimportedTwiceDoesNotDuplicate() = runBlocking {
        val backup = listOf(legacyWeight())

        repository.importWeights(backup)
        repository.importWeights(backup)

        val allWeights = db.weightDao().getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
    }

    @Test
    @Throws(Exception::class)
    fun legacyHba1cBackupReimportedTwiceDoesNotDuplicate() = runBlocking {
        val backup = listOf(legacyHba1c())

        repository.importHba1c(backup)
        repository.importHba1c(backup)

        val allHba1c = db.hba1cDao().getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
    }

    @Test
    @Throws(Exception::class)
    fun newerLegacyBackupUpdatesLocalEntryAndKeepsItsUuid() = runBlocking {
        val backup = listOf(legacyWeight())

        repository.importWeights(backup)

        val uuid = db.weightDao().getAllWeightsFlow().first().single().entryUuid

        repository.importWeights(listOf(legacyWeight(updatedAt = "2026-02-01", isArchived = true)))

        val result = db.weightDao().getAllWeightsFlow().first().single()

        assertEquals(true, result.isArchived)
        assertEquals(uuid, result.entryUuid)
    }

    @Test
    @Throws(Exception::class)
    fun legacyImportantDateBackupReimportedTwiceDoesNotDuplicate() = runBlocking {
        val backup = listOf(legacyImportantDate())

        repository.importImportantDates(backup)
        repository.importImportantDates(backup)

        val allImportantDates = db.importantDateDao().getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
    }

    @Test
    @Throws(Exception::class)
    fun legacyAppointmentBackupReimportedTwiceDoesNotDuplicate() = runBlocking {
        val backup = listOf(legacyAppointment())

        repository.importAppointments(backup)
        repository.importAppointments(backup)

        val allApppointments = db.appointmentDao().getAllAppointmentsFlow().first()

        assertEquals(1, allApppointments.size)
    }
}