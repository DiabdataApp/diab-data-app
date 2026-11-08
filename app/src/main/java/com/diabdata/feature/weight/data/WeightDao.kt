package com.diabdata.feature.weight.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.core.model.Weight
import com.diabdata.feature.graphs.classes.PlotPoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.time.Instant

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weight: Weight)

    @Update
    suspend fun update(weight: Weight)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM weight_entries WHERE entryUuid = :uuid")
    suspend fun getByUuid(uuid: String): Weight?

    @Query("SELECT * FROM weight_entries WHERE createdAt = :createdAt AND date = :date AND value = :value LIMIT 1")
    suspend fun getByContent(createdAt: Instant, date: LocalDate, value: Float): Weight?

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightsFlow(): Flow<List<Weight>>

    @Query("SELECT value,date FROM weight_entries WHERE (date >= :minDate AND date <= :maxDate) AND isArchived = 0 ORDER BY date ASC ")
    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>>

    @Query("SELECT * FROM weight_entries WHERE date >= :minDate AND isArchived = 0 ORDER BY date DESC")
    fun getWeightsSince(minDate: LocalDate): Flow<List<Weight>>
}