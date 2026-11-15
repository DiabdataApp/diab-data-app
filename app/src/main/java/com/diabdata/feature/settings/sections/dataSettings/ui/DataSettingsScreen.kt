package com.diabdata.feature.settings.sections.dataSettings.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.notifications.showNotification
import com.diabdata.core.ui.LocalSnackbarHostState
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.settings.imEx.ImExViewModel
import com.diabdata.feature.settings.imEx.ImportUiState
import com.diabdata.feature.settings.sections.dataSettings.BackupStatusState
import com.diabdata.feature.settings.sections.dataSettings.BackupViewModel
import com.diabdata.feature.settings.sections.dataSettings.ui.components.AutoBackupCard
import com.diabdata.feature.userProfile.UserProfileViewModel
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import com.diabdata.shared.utils.utils.uriStringToReadablePath
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("Unused", "UnusedVariable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(dataViewModel: DataViewModel) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", LocalLocale.current.platformLocale)
    val currentDate = dateFormat.format(Date())
    val fileName = "diabdata_export_$currentDate.zip"
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val imExViewModel: ImExViewModel = hiltViewModel()
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()

    var showConfirmDialog by remember { mutableStateOf(false) }

    val notifChannelName = stringResource(shared.string.settings_notifications_data_channel_name)
    val dataExportSuccess =
        stringResource(shared.string.settings_toasts_data_export_success_message)
    val dataImportSuccess =
        stringResource(shared.string.settings_toasts_data_import_success_message)
    val dataExportError = stringResource(shared.string.settings_toasts_data_export_error_message)
    val dataImportError = stringResource(shared.string.settings_toasts_data_import_error_message)
    val emptyImportFileError =
        stringResource(shared.string.settings_toasts_data_empty_file_error_message)

    val backupViewModel: BackupViewModel = hiltViewModel()
    val backupPrefs by backupViewModel.preferences.collectAsState()
    val backupStatus by backupViewModel.backupStatus.collectAsState()

    val resetMessage = stringResource(shared.string.settings_backup_policy_reset_snackbar)
    val undoLabel = stringResource(shared.string.common_undo)

    val snackbarHostState = LocalSnackbarHostState.current

    // ── Export launcher ──
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    val result =
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            imExViewModel.exportData(outputStream)
                        } ?: Result.failure(Exception("Unable to open output stream"))

                    withContext(Dispatchers.Main) {
                        result.onSuccess {
                            Toast.makeText(context, dataExportSuccess, Toast.LENGTH_SHORT).show()
                            context.showNotification(
                                title = dataExportSuccess,
                                content = uri.lastPathSegment.orEmpty().uriStringToReadablePath(
                                    context
                                ),
                                channelName = notifChannelName
                            )
                        }.onFailure { e ->
                            Log.e("Export", "Export failed", e)
                            Toast.makeText(
                                context,
                                "$dataExportError : ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            context.showNotification(
                                title = "$dataExportError : ${e.message}",
                                content = uri.lastPathSegment.orEmpty(),
                                channelName = notifChannelName
                            )
                        }
                    }
                }
            }
        }
    )

    // ── Import launcher ──
    val importState by imExViewModel.uiState.collectAsState()

    LaunchedEffect(importState) {
        when (importState) {
            is ImportUiState.Success -> {
                Toast.makeText(context, dataImportSuccess, Toast.LENGTH_SHORT).show()
                // notification si vous voulez la garder
            }
            is ImportUiState.Error -> {
                val message = (importState as ImportUiState.Error).message
                Toast.makeText(context, "$dataImportError : $message", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    var passwordInput by remember { mutableStateOf("") }

    if (importState is ImportUiState.PasswordRequired) {
        AlertDialog(
            onDismissRequest = {
                imExViewModel.onPasswordDialogDismissed()
                passwordInput = ""
            },
            icon = {
                SvgIcon(
                    resId = shared.drawable.key_icon_vector,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                )
            },
            title = {
                Text(stringResource(shared.string.settings_data_import_password_required_title))
            },
            text = {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = {
                        Text(stringResource(shared.string.settings_data_import_password_label))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    imExViewModel.onPasswordSubmitted(passwordInput.toCharArray())
                    passwordInput = ""
                }) {
                    Text(stringResource(shared.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    imExViewModel.onPasswordDialogDismissed()
                    passwordInput = ""
                }) {
                    Text(stringResource(shared.string.common_cancel))
                }
            }
        )
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(uri)?.let { inputStream ->
                    imExViewModel.onFileSelected(inputStream)
                }
            }
        }
    )

    // ── UI ──
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = spacedBy(32.dp)
    ) {
        val dataBaseSection: List<CardItem> = listOf(
            CardItem(
                leadingIcon = shared.drawable.database_download_icon_vector,
                content = {
                    Row { Text(stringResource(shared.string.settings_data_export_label)) }
                },
                onClick = { createFileLauncher.launch(fileName) },
                trailingIcon = shared.drawable.arrow_right_icon_vector
            ),
            CardItem(
                leadingIcon = shared.drawable.database_upload_icon_vector,
                content = {
                    Row { Text(stringResource(shared.string.settings_data_import_label)) }
                },
                onClick = {
                    importFileLauncher.launch(
                        arrayOf("application/json", "application/zip")
                    )
                },
                trailingIcon = shared.drawable.arrow_right_icon_vector
            ),
            CardItem(
                leadingIcon = shared.drawable.database_off_icon_vector,
                leadingIconColor = MaterialTheme.colorScheme.error,
                isDestructive = true,
                content = {
                    Row { Text(stringResource(shared.string.settings_data_database_purge_label)) }
                },
                onClick = { showConfirmDialog = true },
                trailingIcon = shared.drawable.arrow_right_icon_vector
            )
        )

        AutoBackupCard(
            enabled = backupPrefs?.automaticBackupEnabled ?: false,
            onEnabledChange = { backupViewModel.setAutoBackupEnabled(it) },
            frequency = BackupFrequency.fromKey(backupPrefs?.frequency ?: "weekly"),
            onFrequencyChange = { backupViewModel.setAutoBackupFrequency(it) },
            backupPath = backupPrefs?.backupPath,
            onPathChange = { backupViewModel.setAutoBackupPath(it) },
            onResetButtonClick = {
                Log.d("BackupReset", "1. Click - backupPrefs: $backupPrefs")
                scope.launch {
                    val backup = backupPrefs ?: run {
                        Log.d("BackupReset", "2. backupPrefs is NULL, aborting")
                        return@launch
                    }
                    Log.d("BackupReset", "3. Backup saved: $backup")
                    backupViewModel.resetPreferences()
                    Log.d("BackupReset", "4. Reset called, showing snackbar...")
                    try {
                        val result = snackbarHostState.showSnackbar(
                            message = resetMessage,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Short
                        )
                        Log.d("BackupReset", "5. snackbar result: $result")
                        if (result == SnackbarResult.ActionPerformed) {
                            backupViewModel.restorePreferences(backup)
                            Log.d("BackupReset", "6. Preferences restored")
                        }
                    } catch (e: Exception) {
                        Log.e("BackupReset", "snackbar error", e)
                    }
                }
            },
            isBackupEnabled = backupPrefs?.automaticBackupEnabled ?: false,
            backupStatusState = backupStatus ?: BackupStatusState(null, null)
        )

        CardsList(
            cards = dataBaseSection
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                SvgIcon(
                    resId = shared.drawable.database_off_icon_vector,
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(shared.string.settings_data_purge_dialog_title)) },
            text = { Text(stringResource(shared.string.settings_data_purge_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataViewModel.clearDatabase(context)
                        showConfirmDialog = false
                    }
                ) {
                    Text(
                        stringResource(shared.string.common_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(shared.string.common_cancel))
                }
            }
        )
    }
}
