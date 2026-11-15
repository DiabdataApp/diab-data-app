package com.diabdata.feature.devices.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diabdata.core.model.MedicalDeviceInfoEntity

@Dao
interface MedicalDevicesInfoDao {
    @Query("SELECT * FROM medical_devices_infos WHERE cipGtin = :code LIMIT 1")
    suspend fun findByCode(code: String): MedicalDeviceInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<MedicalDeviceInfoEntity>)

    @Query("SELECT COUNT(*) FROM medical_devices_infos")
    suspend fun countAll(): Int
}
