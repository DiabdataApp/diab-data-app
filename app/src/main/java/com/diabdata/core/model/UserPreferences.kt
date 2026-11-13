package com.diabdata.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import com.google.gson.annotations.SerializedName
import kotlin.time.Instant

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 0,
    @SerializedName("is_automatic_backup_enabled", alternate = ["automaticBackupEnabled"])
    val automaticBackupEnabled: Boolean = false,
    @SerializedName("backup_frequency", alternate = ["frequency"])
    val frequency: String = BackupFrequency.WEEKLY.key,
    @SerializedName("last_backup_date", alternate = ["lastBackupDate"])
    val lastBackupDate: String? = null,
    @SerializedName("backup_path", alternate = ["backupPath"])
    val backupPath: String? = null,
    @SerializedName("expiration_reminder_enabled", alternate = ["expirationReminder"])
    val expirationReminder: Boolean = false,
    @SerializedName("appointment_reminder_enabled", alternate = ["appointmentReminder"])
    val appointmentReminder: Boolean = false,
    @SerializedName("backup_encryption_enabled", alternate = ["backupEncryptionEnabled"])
    val backupEncryptionEnabled: Boolean = false,
    @SerializedName("updated_at")
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Instant = UNKNOWN_TIME_DEFAULT,
)