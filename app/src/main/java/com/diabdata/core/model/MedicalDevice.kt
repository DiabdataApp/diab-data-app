package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import kotlin.time.Instant

@Entity(
    tableName = "medical_devices",
    indices = [Index(value = ["entryUuid"], unique = true)]
)
data class MedicalDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName(value = "device_startup_date", alternate = ["date"])
    val date: LocalDate,
    @SerializedName("device_life_span_end_date", alternate = ["lifeSpanEndDate"])
    val lifeSpanEndDate: LocalDate,
    @SerializedName("device_name", alternate = ["name"])
    val name: String,
    @SerializedName("device_batch_number", alternate = ["batchNumber"])
    val batchNumber: String,
    @SerializedName("device_serial_number", alternate = ["serialNumber"])
    val serialNumber: String?,
    @SerializedName("device_reference_number", alternate = ["referenceNumber"])
    val referenceNumber: String?,
    @SerializedName("device_manufacturer", alternate = ["manufacturer"])
    val manufacturer: String?,
    @SerializedName("device_type", alternate = ["deviceType"])
    val deviceType: MedicalDeviceInfoType,
    @SerializedName("created_at", alternate = ["createdAt"])
    val createdAt: Instant,
    @SerializedName("is_archived", alternate = ["isArchived"])
    val isArchived: Boolean,
    @SerializedName("device_life_span", alternate = ["lifeSpan"])
    val lifeSpan: Int,
    @SerializedName("is_faulty", alternate = ["isFaulty"])
    val isFaulty: Boolean,
    @SerializedName("is_reported", alternate = ["isReported"])
    val isReported: Boolean,
    @SerializedName("is_life_span_over", alternate = ["isLifeSpanOver"])
    val isLifeSpanOver: Boolean,
    @SerializedName("updated_at", alternate = ["updatedAt"])
    val updatedAt: Instant,
    @SerializedName("entry_uuid")
    val entryUuid: String
)