package com.diabdata.core.model

import com.google.gson.annotations.SerializedName

data class BackupMetadata(
    val app: String = "diabdata",

    @SerializedName("app_version")
    val appVersion: String,

    @SerializedName("format_version")
    val formatVersion: Int = 1,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("is_scheduled_backup")
    val isScheduledBackup: Boolean,

    @SerializedName("is_encrypted")
    val isEncrypted: Boolean = false,

    @SerializedName("device_name")
    val deviceName: String,

    @SerializedName("data_summary")
    val dataSummary: DataSummary
)

data class DataSummary(
    @SerializedName("total_entries_count")
    val totalEntriesCount: Int,

    @SerializedName("weight_entries_count")
    val weightEntriesCount: Int,

    @SerializedName("hba1c_entries_count")
    val hba1cEntriesCount: Int,

    @SerializedName("appointment_entries_count")
    val appointmentEntriesCount: Int,

    @SerializedName("treatment_entries_count")
    val treatmentEntriesCount: Int,

    @SerializedName("important_date_entries_count")
    val importantDateEntriesCount: Int,

    @SerializedName("medical_device_entries_count")
    val medicalDeviceEntriesCount: Int,

    @SerializedName("has_user_profile")
    val hasUserProfile: Boolean,

    @SerializedName("has_user_preferences")
    val hasUserPreferences: Boolean,

    @SerializedName("has_profile_photo")
    val hasProfilePhoto: Boolean
)
