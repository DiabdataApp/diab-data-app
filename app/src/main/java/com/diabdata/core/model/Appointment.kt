package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import kotlin.time.Instant

@Entity(
    tableName = "appointments",
    indices = [Index(value = ["entryUuid"], unique = true)]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName(value = "appointment_date", alternate = ["date"])
    val date: LocalDateTime,
    @SerializedName("doctor_name", alternate = ["doctor"])
    val doctor: String,
    @SerializedName("appointment_type", alternate = ["type"])
    val type: AppointmentType,
    @SerializedName("created_at", alternate = ["createdAt"])
    val createdAt: Instant,
    @SerializedName("is_archived", alternate = ["isArchived"])
    val isArchived: Boolean,
    val notes: String?,
    @SerializedName("updated_at", alternate = ["updatedAt"])
    val updatedAt: Instant,
    @SerializedName("entry_uuid")
    val entryUuid: String
)