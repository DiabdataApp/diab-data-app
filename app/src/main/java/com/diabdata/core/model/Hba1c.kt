package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import kotlin.time.Instant

@Entity(
    tableName = "hba1c_entries",
    indices = [Index(value = ["entryUuid"], unique = true)]
)
data class Hba1c(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName(value = "hba1c_measure_date", alternate = ["date"])
    val date: LocalDate,
    @SerializedName("created_at", alternate = ["createdAt"])
    val createdAt: Instant,
    @SerializedName("is_archived", alternate = ["isArchived"])
    val isArchived: Boolean,
    @SerializedName("hba1c_value", alternate = ["value"])
    val value: Float,
    @SerializedName("updated_at", alternate = ["updatedAt"])
    val updatedAt: Instant,
    @SerializedName("entry_uuid")
    val entryUuid: String
)