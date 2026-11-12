package com.diabdata.feature.appointments.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.core.model.Appointment
import com.diabdata.shared.utils.dataTypes.AppointmentType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import kotlin.time.Instant

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM appointments WHERE entryUuid = :uuid")
    suspend fun getByUuid(uuid: String): Appointment?

    @Query("SELECT * FROM appointments WHERE createdAt = :createdAt AND date = :date AND type = :type LIMIT 1")
    suspend fun getByContent(createdAt: Instant, date: LocalDateTime, type: AppointmentType): Appointment?

    @Update
    suspend fun update(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE (isArchived = 0 OR isArchived = 1) ORDER BY date DESC")
    fun getAllAppointmentsFlow(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date >= :today AND isArchived = 0 ORDER BY date ASC")
    fun getUpcomingAppointmentsFlow(today: LocalDateTime): Flow<List<Appointment>>
}