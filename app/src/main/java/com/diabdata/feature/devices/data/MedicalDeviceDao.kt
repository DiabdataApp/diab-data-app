package com.diabdata.feature.devices.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.core.model.MedicalDevice
import com.diabdata.feature.devices.classes.FaultyBatchCount
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.time.Instant

@Dao
interface MedicalDeviceDao {
    @Insert
    suspend fun insert(device: MedicalDevice)

    @Update
    suspend fun update(device: MedicalDevice)

    @Query("SELECT * FROM medical_devices WHERE entryUuid = :uuid")
    suspend fun getByUuid(uuid: String): MedicalDevice?

    @Query("SELECT * FROM medical_devices WHERE createdAt = :createdAt AND date = :date AND deviceType = :deviceType AND batchNumber = :batchNumber LIMIT 1")
    suspend fun getByContent(createdAt: Instant, date: LocalDate, deviceType: MedicalDeviceInfoType, batchNumber: String): MedicalDevice?

    @Query("DELETE FROM medical_devices WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM medical_devices ORDER BY date DESC")
    fun getAllMedicalDevices(): Flow<List<MedicalDevice>>

    @Query("SELECT * FROM medical_devices WHERE (deviceType != 'WIRELESS_PATCH_REMOTE' AND deviceType != 'WIRED_PUMP') ORDER BY date DESC")
    fun getAllConsumableDevices(): Flow<List<MedicalDevice>>

    @Query("SELECT * FROM medical_devices WHERE (deviceType = 'WIRELESS_PATCH_REMOTE' OR deviceType = 'WIRED_PUMP') ORDER BY date DESC")
    fun getAllNonConsumableDevices(): Flow<List<MedicalDevice>>

    @Query("SELECT * FROM medical_devices WHERE (lifeSpanEndDate >= :today AND isFaulty = 0) AND isLifeSpanOver = 0 AND (deviceType != 'WIRELESS_PATCH_REMOTE' AND deviceType != 'WIRED_PUMP')")
    fun getAllCurrentConsumableMedicalDevices(today: LocalDate): Flow<List<MedicalDevice>>

    @Query("SELECT * FROM medical_devices WHERE isFaulty=1 AND isReported=0")
    fun getAllFaultyUnreportedMedicalDevices(): Flow<List<MedicalDevice>>

    @Query("SELECT * FROM medical_devices WHERE isFaulty=1 AND isReported=1")
    fun getAllFaultyReportedMedicalDevices(): Flow<List<MedicalDevice>>

    @Query(
        """
    SELECT COUNT(*) AS count, batchNumber 
    FROM medical_devices 
    WHERE isFaulty = 1 AND isReported = 1 
    GROUP BY batchNumber 
    ORDER BY count DESC
"""
    )
    fun getFaultyBatchNumbersCounts(): Flow<List<FaultyBatchCount>>

    @Query("SELECT * from medical_devices WHERE deviceType = :deviceType AND lifeSpanEndDate <= :today")
    fun getSimilarExpiringConsumableDevices(
        today: LocalDate,
        deviceType: MedicalDeviceInfoType
    ): List<MedicalDevice>
}