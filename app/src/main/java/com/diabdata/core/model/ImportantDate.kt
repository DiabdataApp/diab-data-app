package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import kotlin.time.Instant

@Entity(
    tableName = "important_date_entries",
    indices = [Index(value = ["entryUuid"], unique = true)]
)
data class ImportantDate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName(value = "important_date_date", alternate = ["date"])
    val date: LocalDate,
    @SerializedName(value = "created_at", alternate = ["createdAt"])
    val createdAt: Instant,
    @SerializedName(value = "is_archived", alternate = ["isArchived"])
    val isArchived: Boolean,
    @SerializedName(value = "important_date_value", alternate = ["importantDate"])
    val importantDate: String,
    @SerializedName(value = "updated_at", alternate = ["updatedAt"])
    val updatedAt: Instant,
    @SerializedName("entry_uuid")
    val entryUuid: String
)