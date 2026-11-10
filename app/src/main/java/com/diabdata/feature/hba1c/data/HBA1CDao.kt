package com.diabdata.feature.hba1c.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.core.model.Hba1c
import com.diabdata.feature.graphs.classes.PlotPoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.time.Instant

@Dao
interface HBA1CDao {
    // Legacy
    @Insert
    suspend fun insert(hba1C: Hba1c)

    @Update
    suspend fun update(hba1C: Hba1c)

    @Query("DELETE FROM hba1c_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM hba1c_entries WHERE entryUuid = :uuid")
    suspend fun getByUuid(uuid: String): Hba1c?

    @Query("SELECT * FROM hba1c_entries WHERE createdAt = :createdAt AND date = :date AND value = :value LIMIT 1")
    suspend fun getByContent(createdAt: Instant, date: LocalDate, value: Float): Hba1c?

    @Query("SELECT value,date FROM hba1c_entries WHERE (date >= :minDate AND date <= :maxDate) AND isArchived = 0 ORDER BY date ASC ")
    fun getHBA1CPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>>

    @Query("SELECT * FROM hba1c_entries ORDER BY date DESC")
    fun getAllHBA1CFlow(): Flow<List<Hba1c>>

    @Query("SELECT * FROM hba1c_entries WHERE date >= :minDate AND isArchived = 0 ORDER BY date DESC")
    fun getHBA1CEntriesSince(minDate: LocalDate): Flow<List<Hba1c>>
}