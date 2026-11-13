package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.devices.data.MedicalDeviceDao
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import org.junit.Assert.assertEquals
import java.time.ZoneId
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

/**
 * Tests for [MedicalDeviceDao] found at [com.diabdata.feature.devices.data.MedicalDeviceDao]
 */
@RunWith(AndroidJUnit4::class)
class MedicalDevicesDaoTest {
    private lateinit var db: DiabDataDatabase
    private lateinit var medicalDeviceDao: MedicalDeviceDao

    val today: LocalDate = LocalDate.now()
    val now = nowMillis()

    val consumableDevice = MedicalDevice(
        id = 1,
        date = today,
        lifeSpanEndDate = today.plusDays(3),
        name = "Omnipod Pod",
        batchNumber = "B-001",
        serialNumber = null,
        referenceNumber = null,
        manufacturer = "Insulet",
        deviceType = MedicalDeviceInfoType.WIRELESS_PATCH,
        createdAt = now,
        isArchived = false,
        lifeSpan = 3,
        isFaulty = false,
        isReported = false,
        isLifeSpanOver = false,
        updatedAt = now,
        entryUuid = "device-1"
    )

    val consumableDeviceFaulty = consumableDevice.copy(
        id = 2,
        name = "Dexcom G6",
        deviceType = MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR,
        batchNumber = "B-002",
        isFaulty = true,
        entryUuid = "device-2"
    )

    val consumableDeviceFaultyReported = consumableDeviceFaulty.copy(
        id = 3,
        name = "Faulty dexcom",
        batchNumber = "B-002",
        isReported = true,
        entryUuid = "device-3"
    )

    val consumableDeviceExpired = consumableDevice.copy(
        id = 4,
        name = "Expired device",
        deviceType = MedicalDeviceInfoType.WIRELESS_PATCH,
        batchNumber = "B-003",
        lifeSpanEndDate = today.minusDays(3),
        isLifeSpanOver = true,
        entryUuid = "device-4"
    )

    val nonConsumableDevice = MedicalDevice(
        id = 5,
        date = today.minusYears(2),
        lifeSpanEndDate = today.minusYears(2),
        name = "Omnipod Dash",
        batchNumber = "B-004",
        serialNumber = "S-001",
        referenceNumber = "R-001",
        manufacturer = "Insulet",
        deviceType = MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE,
        createdAt = today.minusYears(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
        isArchived = false,
        lifeSpan = 0,
        isFaulty = false,
        isReported = false,
        isLifeSpanOver = false,
        updatedAt = now,
        entryUuid = "device-5"
    )

    val nonConsumableDevice2 = nonConsumableDevice.copy(
        id = 6,
        date = today.minusYears(8),
        lifeSpanEndDate = today.minusYears(8),
        name = "Minimed 720",
        manufacturer = "Medtronic",
        batchNumber = "B-005",
        serialNumber = "S-002",
        referenceNumber = "R-002",
        deviceType = MedicalDeviceInfoType.WIRED_PUMP,
        entryUuid = "device-6"
    )

    val allDevices = listOf(
        consumableDevice,
        consumableDeviceFaulty,
        consumableDeviceFaultyReported,
        consumableDeviceExpired,
        nonConsumableDevice,
        nonConsumableDevice2
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        medicalDeviceDao = db.medicalDevicesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetMedicalDeviceTest() = runBlocking {
        medicalDeviceDao.insert(consumableDevice)
        val medicalDevices = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(1, medicalDevices.size)
        assertEquals(consumableDevice, medicalDevices[0])
    }

    @Test
    @Throws(Exception::class)
    fun insertAllAndGetAllTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(6, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun updateDeviceTest() = runBlocking {
        medicalDeviceDao.insert(consumableDevice)

        val medicalDevicesBefore = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(1, medicalDevicesBefore.size)
        assertEquals(consumableDevice, medicalDevicesBefore[0])

        medicalDeviceDao.update(consumableDevice.copy(name = "updated", referenceNumber = "updated"))
        val medicalDevicesAfter = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(1, medicalDevicesAfter.size)
        assertEquals("updated", medicalDevicesAfter[0].name)
        assertEquals("updated", medicalDevicesAfter[0].referenceNumber)
    }

    @Test
    @Throws(Exception::class)
    fun deleteDeviceTest() = runBlocking {
        medicalDeviceDao.insert(consumableDevice)

        val medicalDevicesBefore = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(1, medicalDevicesBefore.size)
        assertEquals(consumableDevice, medicalDevicesBefore[0])

        medicalDeviceDao.deleteById(consumableDevice.id)
        val medicalDevicesAfter = medicalDeviceDao.getAllMedicalDevices().first()
        assertEquals(0, medicalDevicesAfter.size)
    }

    @Test
    @Throws(Exception::class)
    fun getConsumableDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllConsumableDevices().first()
        assertEquals(4, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun getNonConsumableDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllNonConsumableDevices().first()
        assertEquals(2, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun getAllCurrentConsumableDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllCurrentConsumableMedicalDevices(today).first()
        assertEquals(1, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun getAllFaultyDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllFaultyUnreportedMedicalDevices().first()
        assertEquals(1, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun getAllFaultyReportedMedicalDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val medicalDevices = medicalDeviceDao.getAllFaultyReportedMedicalDevices().first()
        assertEquals(1, medicalDevices.size)
    }

    @Test
    @Throws(Exception::class)
    fun getFaultyBatchNumbersCountsTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val faultyBatchNumbersCounts = medicalDeviceDao.getFaultyBatchNumbersCounts().first()
        assertEquals(1, faultyBatchNumbersCounts.size)
        assertEquals("B-002", faultyBatchNumbersCounts[0].batchNumber)
    }

    @Test
    @Throws(Exception::class)
    fun getSimilarExpiringConsumableDevicesTest() = runBlocking {
        allDevices.forEach { medicalDeviceDao.insert(it) }

        val upcomingExpirationDates = medicalDeviceDao.getSimilarExpiringConsumableDevices(today,
            MedicalDeviceInfoType.WIRELESS_PATCH)
        assertEquals(1, upcomingExpirationDates.size)
        assertEquals("B-003", upcomingExpirationDates[0].batchNumber)
    }
}