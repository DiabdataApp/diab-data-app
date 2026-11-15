package com.diabdata.feature.dataMatrixScanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diabdata.core.model.Medication

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE cipGtin = :code LIMIT 1")
    suspend fun findByCode(code: String): Medication?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun countAll(): Int
}
