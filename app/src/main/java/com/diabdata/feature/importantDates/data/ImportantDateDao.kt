package com.diabdata.feature.importantDates.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.core.model.ImportantDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.time.Instant

@Dao
interface ImportantDateDao {
    @Insert
    suspend fun insert(importantDate: ImportantDate)

    @Update
    suspend fun update(importantDate: ImportantDate)

    @Query("SELECT * FROM important_date_entries WHERE entryUuid = :uuid")
    suspend fun getByUuid(uuid: String): ImportantDate?

    @Query("SELECT * FROM important_date_entries WHERE createdAt = :createdAt AND date = :date AND importantDate = :importantDate LIMIT 1")
    suspend fun getByContent(createdAt: Instant, date: LocalDate, importantDate: String): ImportantDate?

    @Query("DELETE FROM important_date_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM important_date_entries ORDER BY date DESC")
    fun getAllImportantDates(): Flow<List<ImportantDate>>
}