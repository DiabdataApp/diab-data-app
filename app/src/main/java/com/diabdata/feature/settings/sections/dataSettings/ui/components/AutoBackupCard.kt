@file:Suppress("UNNECESSARY_SAFE_CALL")

package com.diabdata.feature.settings.sections.dataSettings.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.settings.sections.dataSettings.BackupStatusState
import com.diabdata.shared.R as shared
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import com.diabdata.shared.utils.utils.uriStringToReadablePath

@Composable
fun AutoBackupCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    frequency: BackupFrequency? = null,
    onFrequencyChange: (BackupFrequency) -> Unit,
    backupPath: String?,
    onPathChange: (String) -> Unit,
    onResetButtonClick: () -> Unit,
    backupStatusState: BackupStatusState,
    isBackupEnabled: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val numChildren = 6
    val itemCount = 1 + if (expanded) numChildren else 0

    // File picker launcher
    val context = LocalContext.current
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                onPathChange(it.toString())
            }
        }
    )

    val containerColor = MaterialTheme.colorScheme.surface
    val containerContentColor = MaterialTheme.colorScheme.onSurface

    var isSaving by remember { mutableStateOf(false) }

    val motionScheme = MaterialTheme.motionScheme

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "chevronRotation"
    )

    val toggleScheduledBackupText = if (isBackupEnabled) {
        stringResource(
            shared.string.settings_set_data_backup_scheduler_disable_label
        )
    } else {
        stringResource(shared.string.settings_set_data_backup_scheduler_enable_label)
    }

    Column(
        verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        SegmentedListItem(
            onClick = { expanded = !expanded },
            modifier = Modifier.semantics {
                stateDescription = if (expanded) "Expanded" else "Collapsed"
            },
            shapes = if (expanded) {
                ListItemDefaults.segmentedShapes(
                    index = 0,
                    count = itemCount
                )
            } else {
                ListItemDefaults.segmentedShapes(0, 1).copy(
                    shape = RoundedCornerShape(15.dp)
                )
            },
            leadingContent = {
                SvgIcon(
                    resId = shared.drawable.save_clock_icon_vector,
                    color = containerContentColor,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingContent = {
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    SvgIcon(
                        resId = shared.drawable.arrow_down_icon_vector,
                        color = containerContentColor,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
            },
            content = {
                Text(stringResource(shared.string.settings_set_data_backup_scheduler_label))
            },
            supportingContent = {
                Text(stringResource(shared.string.settings_data_backup_scheduler_supporting_text))
            },
            colors = ListItemDefaults.colors(
                containerColor = containerColor,
                headlineColor = containerContentColor,
                supportingColor = containerContentColor
            )
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(MaterialTheme.motionScheme.slowSpatialSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.slowSpatialSpec())
        ) {
            Column(verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap)) {
                // 2. Schedule frequency choice
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = itemCount),
                    leadingContent = {
                        SvgIcon(
                            resId = shared.drawable.recurring_event_filled_icon_vector,
                            color = containerContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    ),
                    content = {
                        Text(
                            text = stringResource(
                                shared.string.settings_set_data_backup_frequency_label
                            )
                        )
                    },
                    supportingContent = {
                        FlowRow(
                            Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = spacedBy(
                                ButtonGroupDefaults.ConnectedSpaceBetween
                            )
                        ) {
                            BackupFrequency.entries.forEachIndexed { index, freq ->
                                ToggleButton(
                                    checked = frequency == freq,
                                    onCheckedChange = { onFrequencyChange(freq) },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        BackupFrequency.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    modifier = Modifier
                                        .semantics {
                                            role = Role.RadioButton
                                        }
                                        .weight(if (frequency == freq) 1.2f else 0.8f)
                                ) {
                                    Text(
                                        text = stringResource(freq.labelRes),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = GoogleSansFlexFontFamily
                                    )
                                }
                            }
                        }
                    }
                )

                // 3. Backup folder choice
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = 3, count = itemCount),
                    leadingContent = {
                        SvgIcon(
                            resId = shared.drawable.folder_check_icon_vector,
                            color = containerContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    ),
                    content = {
                        Text(
                            text = stringResource(
                                shared.string.settings_set_data_backup_path_label
                            ),
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    supportingContent = {
                        if (!backupPath.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = spacedBy(8.dp)
                            ) {
                                Text(
                                    text = backupPath.uriStringToReadablePath(context),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = GoogleSansFlexFontFamily
                                )
                                IconButton(
                                    onClick = { folderPickerLauncher.launch(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    SvgIcon(
                                        resId = shared.drawable.folder_swap_icon_vector,
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { folderPickerLauncher.launch(null) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = spacedBy(8.dp)
                                    ) {
                                        SvgIcon(
                                            resId = shared.drawable.backup_folder_settings_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            text = stringResource(
                                                shared.string.settings_set_data_backup_path
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = GoogleSansFlexFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                // 4. Last backup info
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = 4, count = itemCount),
                    leadingContent = {
                        SvgIcon(
                            resId = shared.drawable.save_info_icon_vector,
                            color = containerContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    ),
                    content = {
                        Text(
                            text = stringResource(
                                shared.string.settings_scheduled_data_backup_status_label
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    supportingContent = {
                        if (backupStatusState.lastBackupDate != null &&
                            backupStatusState.nextBackupEstimate != null
                        ) {
                            // If we have previous and next scheduled backup dates
                            val dates = listOf(
                                Triple(
                                    backupStatusState.lastBackupDate,
                                    shared.drawable.last_backup_icon_vector,
                                    shared.string.settings_set_data_backup_history_last_label
                                ),
                                Triple(
                                    backupStatusState.nextBackupEstimate,
                                    shared.drawable.next_backup_icon_vector,
                                    shared.string.settings_set_data_backup_history_next_label
                                )
                            )

                            Column(
                                verticalArrangement = spacedBy(4.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                dates.forEach { date ->
                                    Row(
                                        horizontalArrangement = spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(date.third),
                                            fontFamily = GoogleSansFlexFontFamily
                                        )
                                        Text(
                                            text = date.first,
                                            fontFamily = GoogleSansFlexFontFamily
                                        )
                                    }
                                    if (date != dates.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        } else if (backupStatusState.lastBackupDate?.isNotBlank() == true ||
                            backupStatusState.nextBackupEstimate?.isNotBlank() == true
                        ) {
                            // If we only have one of the two
                            Row(
                                horizontalArrangement = spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (backupStatusState.lastBackupDate?.isNotBlank() == true) {
                                    Text(
                                        text = stringResource(
                                            shared.string.settings_set_data_backup_history_last_label
                                        ),
                                        fontFamily = GoogleSansFlexFontFamily
                                    )

                                    Text(backupStatusState.lastBackupDate)
                                } else if (backupStatusState.nextBackupEstimate?.isNotBlank() ==
                                    true
                                ) {
                                    Text(
                                        text = stringResource(
                                            shared.string.settings_set_data_backup_history_next_label
                                        ),
                                        fontFamily = GoogleSansFlexFontFamily
                                    )

                                    Text(
                                        text = backupStatusState.nextBackupEstimate,
                                        fontFamily = GoogleSansFlexFontFamily
                                    )
                                }
                            }
                        } else {
                            // If we don't have any backup dates
                            Text(
                                text = stringResource(shared.string.settings_no_data_backup_file),
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        }
                    }
                )

                // 5. Backup activation switch
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = 5, count = itemCount),
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor,
                        headlineColor = containerContentColor,
                        supportingColor = containerContentColor
                    ),
                    leadingContent = {
                        SvgIcon(
                            resId = shared.drawable.play_arrow_icon_vector,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    content = {
                        Text(
                            text = toggleScheduledBackupText,
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isBackupEnabled,
                            onCheckedChange = onEnabledChange,
                            thumbContent = {
                                val iconRes = if (isBackupEnabled) shared.drawable.play_arrow_icon_vector else shared.drawable.pause_outlined_icon_vector
                                SvgIcon(
                                    resId = iconRes,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    color = if (isBackupEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            }
                        )
                    }
                )

                // 6. Reset button
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = 6, count = itemCount),
                    onClick = {
                        isSaving = true
                        onResetButtonClick()
                        isSaving = false
                    },
                    colors = ListItemDefaults.colors(
                        headlineColor = MaterialTheme.colorScheme.error,
                        supportingColor = MaterialTheme.colorScheme.error
                    ),
                    leadingContent = {
                        SvgIcon(
                            resId = shared.drawable.reset_settings_icon_vector,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    content = {
                        Text(
                            text = stringResource(shared.string.common_reset),
                            fontFamily = GoogleSansFlexFontFamily
                        )
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            SvgIcon(
                                resId = shared.drawable.arrow_right_icon_vector,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

const val locale = "fr"
const val testData = false
const val testBackupPath = "content://com.android.externalstorage.documents/tree/primary%3ADiabdata"
val backupStatusState = BackupStatusState("30 juil. 2026", "31/08/26 16h30")
val emptyBackupStatusState = BackupStatusState(null, null)
val withNextBackupDate = BackupStatusState(null, "30 juil. 2026")

@Preview(
    name = "ON",
    showBackground = true,
    locale = locale,
    device = "id:pixel_8_pro",
    showSystemUi = true
)
@Composable
fun TwoToneInfoRowPreview() {
    var darkTheme by remember { mutableStateOf(false) }

    var frequency by remember { mutableStateOf<BackupFrequency?>(BackupFrequency.DAILY) }
    var backupPath by remember {
        @Suppress("SimplifyBooleanWithConstants")
        mutableStateOf(
            testBackupPath.takeIf { it.isNotBlank() && testData }
                ?: ""
        )
    }
    var enableAutoBackup by remember { mutableStateOf(false) }

    DiabDataTheme(dynamicColor = false, darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(30.dp)
                    .fillMaxSize()
                    .animateContentSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AutoBackupCard(
                    enabled = enableAutoBackup,
                    onEnabledChange = { enableAutoBackup = it },
                    frequency = frequency,
                    backupPath = backupPath,
                    onFrequencyChange = { frequency = it },
                    onPathChange = { backupPath = it },
                    onResetButtonClick = { backupPath = "" },
                    backupStatusState = if (testData) backupStatusState else emptyBackupStatusState
                )

                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                    verticalArrangement = spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dark Theme",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight(800)
                        )
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it },
                            thumbContent = {
                                if (darkTheme) {
                                    SvgIcon(
                                        resId = shared.drawable.tick_icon_vector,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    SvgIcon(
                                        resId = shared.drawable.close_icon_vector,
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
