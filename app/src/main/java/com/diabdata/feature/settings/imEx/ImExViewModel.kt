package com.diabdata.feature.settings.imEx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.core.backup.BackupArchiveManager
import com.diabdata.core.backup.BackupImportException
import com.diabdata.core.database.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles backup export and import, bridging [BackupArchiveManager] with the import/export UI.
 *
 * Import is a two-step flow when the selected backup is encrypted: [onFileSelected] first attempts
 * a silent decryption via the stored Keystore key. If that fails, [uiState] exposes
 * [ImportUiState.PasswordRequired] and the caller is expected to collect a password from the user
 * and call [onPasswordSubmitted], or [onPasswordDialogDismissed] to cancel.
 */
@HiltViewModel
class ImExViewModel @Inject constructor(
    val repository: DataRepository,
    private val backupArchiveManager: BackupArchiveManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)

    /** Current state of the ongoing (or last) import operation. */
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    /**
     * The buffered content of the backup currently being imported, retained only while
     * [uiState] is [ImportUiState.PasswordRequired], so [onPasswordSubmitted] can retry decryption
     * without asking the user to re-select the file. Cleared by [clearPendingBackupBytes].
     */
    private var pendingBackupBytes: ByteArray? = null

    /**
     * Exports all user data as a backup archive, written to [output].
     *
     * @param output The [OutputStream] the archive is written to.
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     */
    suspend fun exportData(output: OutputStream): Result<Unit> {
        val encrypted = repository.isBackupEncryptionEnabled()
        return backupArchiveManager.writeBackup(
            output,
            isScheduledBackup = false,
            isEncrypted = encrypted
        )
    }

    /**
     * Buffers the content of a backup file selected by the user and attempts to import it,
     * without a password. Updates [uiState] with the outcome, notably
     * [ImportUiState.PasswordRequired] if the backup is encrypted and the stored Keystore key
     * cannot silently decrypt it.
     *
     * @param input The [InputStream] to read the selected backup file from.
     */
    fun onFileSelected(input: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ImportUiState.Loading
            try {
                val fileBuffer = input.use { it.readBytes() }
                if (fileBuffer.isEmpty()) {
                    _uiState.value = ImportUiState.Error("Empty file")
                    return@launch
                }
                pendingBackupBytes = fileBuffer
                val result = ByteArrayInputStream(fileBuffer).use { importData(it) }
                handleImportResult(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Retries importing the backup buffered by [onFileSelected] using [password], after
     * [uiState] reported [ImportUiState.PasswordRequired]. Only attempted once: any failure is
     * reported as a final [ImportUiState.Error], with no further retry.
     *
     * [password] is zeroed out once this call completes, regardless of outcome.
     *
     * @param password The password entered by the user to decrypt the buffered backup.
     */
    fun onPasswordSubmitted(password: CharArray) {
        val bytes = pendingBackupBytes ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ImportUiState.Loading
            try {
                val result = ByteArrayInputStream(bytes).use { importData(it, password) }
                handleImportResult(result)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    /**
     * Cancels an in-progress [ImportUiState.PasswordRequired] prompt, discarding the buffered
     * backup content and resetting [uiState] to [ImportUiState.Idle].
     */
    fun onPasswordDialogDismissed() {
        clearPendingBackupBytes()
        _uiState.value = ImportUiState.Idle
    }

    /**
     * Imports a backup from [input], delegating to [BackupArchiveManager.readBackup].
     *
     * @param input The [InputStream] to read the backup from.
     * @param password The password to decrypt the backup with, if it was encrypted. Defaults to
     * `null`.
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     */
    suspend fun importData(input: InputStream, password: CharArray? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            backupArchiveManager.readBackup(input, password)
        }

    /** Maps the outcome of an import attempt to [uiState], clearing [pendingBackupBytes] unless
     * a password still needs to be collected from the user. */
    private fun handleImportResult(result: Result<Unit>) {
        val newState = result.fold(
            onSuccess = {
                clearPendingBackupBytes()
                ImportUiState.Success
            },
            onFailure = { exception ->
                when (exception) {
                    is BackupImportException.PasswordRequired -> ImportUiState.PasswordRequired
                    else -> {
                        clearPendingBackupBytes()
                        ImportUiState.Error(exception.message ?: "Unknown error")
                    }
                }
            }
        )
        _uiState.value = newState
    }

    /** Zeroes out and discards [pendingBackupBytes]. */
    private fun clearPendingBackupBytes() {
        pendingBackupBytes?.fill(0)
        pendingBackupBytes = null
    }
}
