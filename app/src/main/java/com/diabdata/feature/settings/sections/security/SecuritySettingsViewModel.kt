package com.diabdata.feature.settings.sections.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.core.backup.encryption.BackupEncryptionKeyManager
import com.diabdata.core.database.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val repository: DataRepository,
    private val backupEncryptionKeyManager: BackupEncryptionKeyManager
) : ViewModel() {
    private val _hasBackupPassword = MutableStateFlow(backupEncryptionKeyManager.hasPassword())
    val hasBackupPassword: StateFlow<Boolean> = _hasBackupPassword.asStateFlow()

    val backupEncryptionEnabled: StateFlow<Boolean> = repository.getUserPreferences()
        .map { it?.backupEncryptionEnabled ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    suspend fun setBackupPassword(password: String): Result<Unit> {
        val result = backupEncryptionKeyManager.setPassword(password)
        if (result.isSuccess) _hasBackupPassword.value = true
        return result
    }

    suspend fun clearBackupPassword(): Result<Unit> {
        val result = backupEncryptionKeyManager.clearPassword()
        if (result.isSuccess) {
            _hasBackupPassword.value = false
            repository.toggleBackupEncryption(false)
        }
        return result
    }

    suspend fun toggleBackupEncryption(enabled: Boolean): Result<Unit> {
        if (!_hasBackupPassword.value) {
            return Result.failure(Exception("No backup password set"))
        }
        repository.toggleBackupEncryption(enabled)
        return Result.success(Unit)
    }
}
