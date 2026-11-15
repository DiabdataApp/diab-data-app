package com.diabdata.feature.settings.sections.security.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.settings.sections.security.SecuritySettingsViewModel
import com.diabdata.shared.R.drawable as icons
import com.diabdata.shared.R.string as strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SecuritySettingsScreen() {
    val viewModel: SecuritySettingsViewModel = hiltViewModel()

    val containerColor = MaterialTheme.colorScheme.surface
    val containerContentColor = MaterialTheme.colorScheme.onSurface

    val backupEncryptionEnabled by viewModel.backupEncryptionEnabled.collectAsState()
    val hasBackupPassword = viewModel.hasBackupPassword.collectAsState().value

    var passwordValue: String by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val setBackupPasswordSuccess =
        stringResource(strings.settings_security_set_backup_password_success)
    val setBackupPasswordError =
        stringResource(strings.settings_security_set_backup_password_error)
    val unsetPasswordErrorMessage =
        stringResource(strings.settings_security_unset_backup_password_error)

    val passwordButtonIcon =
        if (hasBackupPassword) {
            icons.save_as_icon_vector
        } else {
            icons.save_icon_vector
        }

    val isBackupEncryptionEnabledIcon =
        if (backupEncryptionEnabled) {
            icons.shield_lock_filled_icon_vector
        } else {
            icons.shield_lock_icon_vector
        }
    val lockUnlockIcon =
        if (backupEncryptionEnabled) {
            icons.lock_icon_vector
        } else {
            icons.lock_open_icon_vector
        }
    val backupToggleSupportingText =
        if (backupEncryptionEnabled) {
            strings.settings_security_disable_backup_encryption
        } else {
            strings.settings_security_enable_backup_encryption
        }
    val backupEncryptionSectionChildCount = if (hasBackupPassword) {
        3
    } else {
        2
    }

    fun submitPassword() {
        scope.launch(Dispatchers.IO) {
            val setPassword = viewModel.setBackupPassword(passwordValue)
            withContext(Dispatchers.Main) {
                setPassword.onSuccess {
                    Toast.makeText(
                        context,
                        setBackupPasswordSuccess,
                        Toast.LENGTH_SHORT
                    ).show()
                    passwordValue = ""
                }
                setPassword.onFailure { e ->
                    Toast.makeText(
                        context,
                        "$setBackupPasswordError : ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    strings.settings_security_settings_section_backup_password_label
                ),
                fontFamily = GoogleSansFlexFontFamily
            )

            Column(
                verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                SegmentedListItem(
                    modifier = Modifier,
                    shapes = ListItemDefaults.segmentedShapes(
                        0,
                        backupEncryptionSectionChildCount
                    ),
                    leadingContent = {
                        SvgIcon(
                            resId = icons.password_icon_vector,
                            color = containerContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    content = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        stringResource(
                                            strings.settings_security_set_backup_password_label
                                        ),
                                        fontFamily = GoogleSansFlexFontFamily
                                    )
                                },
                                value = passwordValue,
                                onValueChange = { passwordValue = it },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { submitPassword() }
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    FilledTonalIconButton(
                                        onClick = { submitPassword() },
                                        enabled = passwordValue.isNotEmpty(),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .size(36.dp)
                                    ) {
                                        SvgIcon(
                                            resId = passwordButtonIcon,
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    )
                )

                AnimatedVisibility(
                    visible = hasBackupPassword,
                    enter = expandVertically(
                        MaterialTheme.motionScheme.slowSpatialSpec()
                    ),
                    exit = shrinkVertically(
                        MaterialTheme.motionScheme.slowSpatialSpec()
                    )
                ) {
                    SegmentedListItem(
                        modifier = Modifier,
                        shapes = ListItemDefaults.segmentedShapes(
                            if (hasBackupPassword) {
                                1
                            } else {
                                0
                            },
                            backupEncryptionSectionChildCount
                        ),
                        leadingContent = {
                            SvgIcon(
                                resId = icons.backspace_icon_vector,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        content = {
                            Text(
                                text = stringResource(
                                    strings.settings_security_unset_backup_password_label
                                ),
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    strings.settings_security_unset_backup_password_description
                                ),
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val result = viewModel.clearBackupPassword()
                                        withContext(Dispatchers.Main) {
                                            result.onSuccess {
                                                Toast.makeText(
                                                    context,
                                                    setBackupPasswordSuccess,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            result.onFailure { e ->
                                                Toast.makeText(
                                                    context,
                                                    "$unsetPasswordErrorMessage : ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            ) {
                                SvgIcon(
                                    resId = icons.arrow_right_icon_vector,
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = containerColor,
                            headlineColor = MaterialTheme.colorScheme.error,
                            supportingColor = containerContentColor
                        )
                    )
                }

                SegmentedListItem(
                    modifier = Modifier,
                    shapes = ListItemDefaults.segmentedShapes(
                        if (hasBackupPassword) {
                            2
                        } else {
                            1
                        },
                        backupEncryptionSectionChildCount
                    ),
                    leadingContent = {
                        SvgIcon(
                            resId = isBackupEncryptionEnabledIcon,
                            color = if (backupEncryptionEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                containerContentColor
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingContent = {
                        Switch(checked = backupEncryptionEnabled, onCheckedChange = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.toggleBackupEncryption(it)
                            }
                        }, enabled = hasBackupPassword, thumbContent = {
                            SvgIcon(
                                resId = lockUnlockIcon,
                                color = containerContentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        })
                    },
                    content = {
                        Text(
                            text = stringResource(
                                strings.settings_security_backup_encryption_label
                            ),
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(backupToggleSupportingText),
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SvgIcon(
                    resId = icons.lightbulb_icon_vector,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = stringResource(strings.settings_security_backup_encryption_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = GoogleSansFlexFontFamily
                )
            }
        }
    }
}
