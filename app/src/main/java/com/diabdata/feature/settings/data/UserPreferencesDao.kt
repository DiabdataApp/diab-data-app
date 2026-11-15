package com.diabdata.feature.settings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diabdata.core.model.UserPreferences
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getUserPreferences(): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: UserPreferences)

    @Query("UPDATE user_preferences SET automaticBackupEnabled = :enabled WHERE id = 0")
    suspend fun setAutoBackupEnabled(enabled: Boolean)

    @Query("UPDATE user_preferences SET frequency = :frequency WHERE id = 0")
    suspend fun setFrequency(frequency: String)

    @Query("UPDATE user_preferences SET backupPath = :path WHERE id = 0")
    suspend fun setBackupPath(path: String)

    @Query("UPDATE user_preferences SET lastBackupDate = :date WHERE id = 0")
    suspend fun setLastBackupDate(date: String)

    @Query(
        "UPDATE user_preferences SET backupPath = NULL, lastBackupDate = NULL, frequency = :defaultFrequency, automaticBackupEnabled = 0 WHERE id = 0"
    )
    suspend fun resetBackupPreferences(defaultFrequency: String = BackupFrequency.WEEKLY.key)

    @Query("UPDATE user_preferences SET expirationReminder = :enabled WHERE id = 0")
    suspend fun enableExpirationReminder(enabled: Boolean)

    @Query("SELECT expirationReminder FROM user_preferences WHERE id = 0")
    suspend fun isExpirationReminderEnabled(): Boolean

    @Query("UPDATE user_preferences SET appointmentReminder = :enabled WHERE id = 0")
    suspend fun enableAppointmentReminder(enabled: Boolean)

    @Query("SELECT appointmentReminder FROM user_preferences WHERE id = 0")
    suspend fun isAppointmentReminderEnabled(): Boolean

    @Query("UPDATE user_preferences SET backupEncryptionEnabled = :enabled WHERE id = 0")
    suspend fun toggleBackupEncryptionEnabled(enabled: Boolean)

    @Query("SELECT backupEncryptionEnabled FROM user_preferences WHERE id = 0")
    suspend fun isBackupEncryptionEnabled(): Boolean
}
