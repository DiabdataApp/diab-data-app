package com.diabdata.feature.devices.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.database.converters.toEntity
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.newEntryUuid
import com.diabdata.core.model.nowMillis
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.core.ui.components.actionInput.EnumDropdown
import com.diabdata.core.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.core.ui.components.date_components.DateSelector
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.utils.ui.darken
import com.diabdata.feature.devices.DevicesViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun MedicalDevicePopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    prefilled: MedicalDevice? = null,
    toUpdate: DataViewModel.MixedDbEntry.DeviceEntry? = null
) {
    val devicesViewModel: DevicesViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    var name by remember { mutableStateOf(prefilled?.name ?: toUpdate?.name ?: "") }
    var manufacturer by remember {
        mutableStateOf(
            prefilled?.manufacturer ?: toUpdate?.manufacturer ?: ""
        )
    }
    var serialNumber by remember {
        mutableStateOf(
            prefilled?.serialNumber ?: toUpdate?.serialNumber ?: ""
        )
    }
    var batchNumber by remember {
        mutableStateOf(
            prefilled?.batchNumber ?: toUpdate?.batchNumber ?: ""
        )
    }
    var referenceNumber by remember {
        mutableStateOf(
            prefilled?.referenceNumber ?: toUpdate?.referenceNumber ?: ""
        )
    }
    var selectedDate by remember {
        mutableStateOf(
            prefilled?.date ?: toUpdate?.date?.toLocalDate() ?: today
        )
    }

    var selectedDeviceType by remember {
        mutableStateOf(
            toUpdate?.deviceType ?: prefilled?.deviceType ?: MedicalDeviceInfoType.WIRED_PATCH
        )
    }
    var lifeSpan by remember { mutableIntStateOf(prefilled?.lifeSpan ?: toUpdate?.lifeSpan ?: 3) }
    var lifeSpanEndDate by remember {
        mutableStateOf(
            prefilled?.lifeSpanEndDate ?: toUpdate?.lifeSpanEndDate
            ?: today.plusDays(lifeSpan.toLong())
        )
    }
    var isFaulty by remember { mutableStateOf(prefilled?.isFaulty ?: toUpdate?.isFaulty ?: false) }
    var isReported by remember {
        mutableStateOf(
            prefilled?.isReported ?: toUpdate?.isReported ?: false
        )
    }
    var isLifeSpanOver by remember {
        mutableStateOf(
            prefilled?.isLifeSpanOver ?: toUpdate?.isLifeSpanOver ?: false
        )
    }

    var setSimilarDevicesToExpired by remember { mutableStateOf(false) }

    val isValid = batchNumber.isNotBlank() && name.isNotBlank()

    BasePopupLayout(
        title = stringResource(
            if (toUpdate == null) shared.string.popup_title_add else shared.string.popup_title_update,
            AddableType.DEVICE.getDisplayName(context)
        ),
        icon = AddableType.DEVICE.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val now = nowMillis()
            val deviceEntry = DataViewModel.MixedDbEntry.DeviceEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate.atStartOfDay(),
                lifeSpanEndDate = lifeSpanEndDate,
                addableType = AddableType.DEVICE,
                name = name,
                deviceType = selectedDeviceType,
                icon = AddableType.DEVICE.iconRes,
                isArchived = toUpdate?.isArchived ?: false,
                createdAt = toUpdate?.createdAt ?: now,
                updatedAt = now,
                batchNumber = batchNumber,
                serialNumber = serialNumber,
                referenceNumber = referenceNumber,
                manufacturer = manufacturer,
                lifeSpan = lifeSpan,
                isFaulty = isFaulty,
                isReported = isReported,
                isLifeSpanOver = isLifeSpanOver,
                entryUuid = toUpdate?.entryUuid ?: newEntryUuid()
            )
            if (toUpdate == null) {
                devicesViewModel.insertDevice(deviceEntry.toEntity() as MedicalDevice)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(deviceEntry)
                }
            }

            if (setSimilarDevicesToExpired) {
                val devicesToUpdate = devicesViewModel.getAllFaultyDevicesExpiredToday()
                scope.launch {
                    devicesViewModel.setDevicesLifespanOver(devicesToUpdate, true)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = isValid
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                DateSelector(
                    date = selectedDate,
                    onDateSelected = {
                        selectedDate = it
                        lifeSpanEndDate = it.plusDays(lifeSpan.toLong())
                    },
                    isStartDate = true
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                DateSelector(
                    date = lifeSpanEndDate,
                    onDateSelected = { lifeSpanEndDate = it },
                    isEndDate = true
                )
            }
        }

        EnumDropdown(
            label = stringResource(shared.string.popup_placeholder_device_type),
            options = MedicalDeviceInfoType.entries,
            selected = selectedDeviceType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedDeviceType = it },
            iconRes = { it.iconRes }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(shared.string.devices_device_popup_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = batchNumber,
            onValueChange = { batchNumber = it },
            label = { Text(stringResource(shared.string.devices_device_popup_batch_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            label = { Text(stringResource(shared.string.devices_device_popup_serial_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text(stringResource(shared.string.devices_device_popup_manufacturer_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = lifeSpan.toString(),
            onValueChange = {
                lifeSpan = it.toIntOrNull() ?: 0
                lifeSpanEndDate = selectedDate.plusDays(lifeSpan.toLong())
            },
            label = { Text(stringResource(shared.string.devices_device_popup_lifespan_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        CreateToggle(
            text = stringResource(shared.string.devices_device_popup_faulty_label),
            displayText = stringResource(shared.string.devices_device_popup_faulty_description),
            checked = isFaulty,
            onCheckedChange = { isFaulty = it },
            icon = shared.drawable.faulty_medical_device_icon_vector,
            state = ToggleState.DESTRUCTIVE
        )

        CreateToggle(
            text = stringResource(shared.string.devices_device_popup_reported_label),
            displayText = stringResource(shared.string.devices_device_popup_reported_description),
            checked = isReported,
            onCheckedChange = { isReported = it },
            icon = shared.drawable.report_icon_vector,
        )

        if (toUpdate != null) {
            CreateToggle(
                text = stringResource(shared.string.devices_device_popup_device_lifespan_over_label),
                displayText = stringResource(shared.string.devices_device_popup_lifespan_over_description),
                checked = isLifeSpanOver,
                onCheckedChange = { isLifeSpanOver = it },
                icon = shared.drawable.recycle_icon_vector,
                state = ToggleState.SUCCESS
            )
        }

        if (toUpdate == null) {
            CreateToggle(
                text = stringResource(shared.string.devices_device_popup_update_expired_devices_label),
                displayText = stringResource(shared.string.devices_device_popup_update_expired_devices_description),
                checked = setSimilarDevicesToExpired,
                onCheckedChange = { setSimilarDevicesToExpired = it },
                icon = shared.drawable.select_all_icon_vector
            )
        }
    }
}

@Composable
fun CreateToggle(
    text: String,
    displayText: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: Int? = null,
    state: ToggleState = ToggleState.DEFAULT
) {
    val (thumbColor, trackColor) = state.getColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
            if (displayText.isNotBlank()) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                if (checked && icon != null) {
                    SvgIcon(
                        resId = icon,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                        color = when (state) {
                            ToggleState.DESTRUCTIVE -> MaterialTheme.colorScheme.onError
                            ToggleState.SUCCESS -> Color(0xFF28a745).darken(0.7f)
                            else -> MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = thumbColor,
                checkedTrackColor = trackColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

enum class ToggleState {
    DEFAULT,
    DESTRUCTIVE,
    SUCCESS
}

@Composable
fun ToggleState.getColors(): Pair<Color, Color> {
    return when (this) {
        ToggleState.DEFAULT -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )

        ToggleState.DESTRUCTIVE -> Pair(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer
        )

        ToggleState.SUCCESS -> Pair(
            Color(0xFF28a745),
            Color(0xFF28a745).darken()
        )
    }
}