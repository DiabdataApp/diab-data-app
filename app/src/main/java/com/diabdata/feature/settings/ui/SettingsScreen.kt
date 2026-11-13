package com.diabdata.feature.settings.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.BuildConfig
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardListItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.utils.ui.darken
import com.diabdata.feature.settings.SettingsViewModel
import com.diabdata.feature.settings.ui.components.changelog.ChangelogDialog
import com.diabdata.shared.theme.DataIconColor
import com.diabdata.shared.theme.GtinFilesIconColor
import com.diabdata.shared.theme.NotificationIconColor
import com.diabdata.shared.theme.SecurityIconColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.diabdata.shared.R as shared

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingsScreen(
    dataViewModel: DataViewModel,
    onNavigateToDataSettings: () -> Unit,
    onNavigateToSecuritySettings: () -> Unit,
    onNavigateToAboutScreen: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val medicationsGtinFileVersion = BuildConfig.MEDICATION_GTIN_FILE_VERSION
    val medicalDeviceGtinFileVersion = BuildConfig.MEDICAL_DEVICES_GTIN_FILE_VERSION

    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val scope = rememberCoroutineScope()

    var showChangeLogDialog by remember { mutableStateOf(false) }

    val userPreferences by settingsViewModel.preferences.collectAsState()
    val isExpirationEnabled = userPreferences?.expirationReminder ?: false
    val isAppointmentEnabled = userPreferences?.appointmentReminder ?: false

    val toastAppointmentReminderEnabled = stringResource(shared.string.appointments_reminders_enabled_success_toast)
    val toastMedicationReminderEnabled = stringResource(shared.string.medications_reminders_enabled_success_toast)

    val medicationStoreRebuiltText = stringResource(shared.string.medications_medication_store_rebuilt_toast_message)
    val medicalDevicesStoreRebuiltText = stringResource(shared.string.devices_medical_devices_store_rebuilt_toast)

    val nextAppointmentDate by remember {
        dataViewModel.upcomingAppointment
            .map { appointments ->
                appointments.minByOrNull { it.date }?.date
            }
    }.collectAsState(initial = null)

    val nextTreatmentExpirationDate by remember {
        dataViewModel.upcomingExpiringTreatmentDates
            .map { treatments ->
                treatments.minByOrNull { it.expirationDate }?.expirationDate
            }
    }.collectAsState(initial = null)

    val iconCircleProps = ColoredIconCircleProps(
        baseColor = NotificationIconColor,
        iconRes = shared.drawable.notification_filled_icon_vector,
        size = null,
        iconSize = null
    )

    val containerColor = MaterialTheme.colorScheme.surface
    val containerContentColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp
                )
                .background(Color.Transparent)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = spacedBy(32.dp)
        ) {
            val notificationSection: List<CardItem> = listOf(
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps,
                    content = {
                        val displayText = if (nextTreatmentExpirationDate != null) stringResource(
                            shared.string.medications_setting_screen_reminder_details,
                            nextTreatmentExpirationDate!!.format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        ) else ""

                        Column {
                            Text(
                                text = stringResource(shared.string.medications_expiry_notification_title_text),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        }
                    },
                    switchState = isExpirationEnabled,
                    onSwitchChange = { isChecked ->
                        settingsViewModel.onExpirationReminderSwitch(isChecked)
                        if (isChecked) {
                            Toast.makeText(
                                context,
                                toastMedicationReminderEnabled,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    switchColor = NotificationIconColor.darken(0.2f),
                    trailingIcon = shared.drawable.notification_filled_icon_vector,
                    uncheckedTrailingIcon = shared.drawable.notification_off_icon_vector
                ),
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps.copy(
                        iconRes = shared.drawable.event_notification_icon_vector
                    ),
                    content = {
                        val displayText = if (nextAppointmentDate != null) stringResource(
                            shared.string.appointments_setting_screen_reminder_details,
                            nextAppointmentDate!!.format(
                                DateTimeFormatter.ofLocalizedDateTime(
                                    FormatStyle.MEDIUM,
                                    FormatStyle.SHORT
                                )
                            )
                        ) else ""

                        Column {
                            Text(
                                text = stringResource(shared.string.appointments_setting_screen_reminder_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        }
                    },
                    switchState = isAppointmentEnabled,
                    onSwitchChange = { isChecked ->
                        settingsViewModel.onAppointmentSwitch(isChecked)
                        if (isChecked) {
                            Toast.makeText(
                                context,
                                toastAppointmentReminderEnabled,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    switchColor = NotificationIconColor.darken(0.2f),
                    trailingIcon = shared.drawable.notification_filled_icon_vector,
                    uncheckedTrailingIcon = shared.drawable.notification_off_icon_vector
                )
            )

            val aboutApplicationSection: List<CardItem> = listOf(
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps.copy(
                        baseColor = GtinFilesIconColor,
                        iconRes = shared.drawable.medication_info_icon_vector
                    ),
                    content = {
                        Row {
                            Text("Medication information file version $medicationsGtinFileVersion")
                        }
                    },
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            settingsViewModel.forceRebuildMedications()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    medicationStoreRebuiltText,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    trailingIcon = shared.drawable.refresh_icon_vector
                ),
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps.copy(
                        baseColor = GtinFilesIconColor,
                        iconRes = shared.drawable.medical_device_info_version_icon_vector
                    ),
                    content = {
                        Row {
                            Text("Medical devices information file version $medicalDeviceGtinFileVersion")
                        }
                    },
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            settingsViewModel.forceRebuildMedicalDevices()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    medicalDevicesStoreRebuiltText,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    trailingIcon = shared.drawable.refresh_icon_vector,
                )
            )

            // Database section
            CardListItem(
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps.copy(
                        baseColor = DataIconColor,
                        iconRes = shared.drawable.database_icon_vector
                    ),
                    content = {
                        Column {
                            Text(
                                text = stringResource(shared.string.settings_data_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                            Text(
                                text = stringResource(shared.string.settings_data_section_description_text),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        }
                    },
                    onClick = onNavigateToDataSettings,
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                ),
                shape = RoundedCornerShape(15.dp)
            )

            // Security section
            CardListItem(
                CardItem(
                    leadingColoredCircleIcon = iconCircleProps.copy(
                        baseColor = SecurityIconColor,
                        iconRes = shared.drawable.shield_toggle_icon_vector
                    ),
                    content = {
                        Column {
                            Text(
                                text = stringResource(shared.string.settings_security_settings),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                            Text(
                                text = stringResource(shared.string.settings_security_settings_section_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = GoogleSansFlexFontFamily
                            )
                        }
                    },
                    onClick = {
                        onNavigateToSecuritySettings()
                    },
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                ),
                shape = RoundedCornerShape(15.dp)
            )

            // Notification section
            CardsList(
                cards = notificationSection
            )

            // About app section
            CardsList(
                cards = aboutApplicationSection
            )

            SegmentedListItem(
                onClick = { onNavigateToAboutScreen() },
                shapes = ListItemDefaults.segmentedShapes(0, 1).copy(
                    shape = RoundedCornerShape(15.dp)
                ),
                leadingContent = {
                    SvgIcon(
                        resId = shared.drawable.app_version_icon_vector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingContent = {
                    SvgIcon(
                        resId = shared.drawable.arrow_right_icon_vector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                content = {
                    Text(stringResource(shared.string.settings_about_app_label),
                        fontFamily = GoogleSansFlexFontFamily
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = containerColor,
                    headlineColor = containerContentColor,
                    supportingColor = containerContentColor
                ),
            )
        }
    }

    if (showChangeLogDialog) {
        ChangelogDialog(onDismiss = { showChangeLogDialog = false })
    }
}