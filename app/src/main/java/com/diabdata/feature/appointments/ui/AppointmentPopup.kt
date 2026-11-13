package com.diabdata.feature.appointments.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.database.converters.toEntity
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.newEntryUuid
import com.diabdata.core.model.nowMillis
import com.diabdata.core.ui.components.actionInput.EnumDropdown
import com.diabdata.core.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.core.ui.components.date_components.DateTimeSelector
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.AppointmentType
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import com.diabdata.shared.R as shared

@Composable
fun AppointmentPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    toUpdate: DataViewModel.MixedDbEntry.AppointmentEntry? = null
) {
    var doctor by remember { mutableStateOf(toUpdate?.doctor ?: "") }
    var notes by remember { mutableStateOf(toUpdate?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(toUpdate?.date ?: LocalDateTime.now()) }
    var selectedAppointmentType by remember {
        mutableStateOf(
            toUpdate?.type ?: AppointmentType.APPOINTMENT
        )
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BasePopupLayout(
        title = stringResource(
            if (toUpdate == null) shared.string.popup_title_add else shared.string.popup_title_update,
            AddableType.APPOINTMENT.getDisplayName(context)
        ),
        icon = AddableType.APPOINTMENT.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val now = nowMillis()
            val appointmentEntry = DataViewModel.MixedDbEntry.AppointmentEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate,
                doctor = doctor,
                type = selectedAppointmentType,
                createdAt = toUpdate?.createdAt ?: now,
                isArchived = toUpdate?.isArchived ?: false,
                notes = notes,
                updatedAt = now,
                icon = AddableType.APPOINTMENT.iconRes,
                entryUuid = toUpdate?.entryUuid ?: newEntryUuid()
            )

            if (toUpdate == null) {
                dataViewModel.addAppointment(appointmentEntry.toEntity() as Appointment)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(appointmentEntry)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = doctor.isNotBlank()
    ) {
        DateTimeSelector(dateTime = selectedDate, onDateTimeSelected = { selectedDate = it })

        EnumDropdown(
            label = stringResource(shared.string.popup_placeholder_appointment_type),
            options = AppointmentType.entries,
            selected = selectedAppointmentType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedAppointmentType = it },
            iconRes = { it.iconRes }
        )

        OutlinedTextField(
            value = doctor,
            onValueChange = { doctor = it },
            label = { Text(stringResource(shared.string.popup_placeholder_doctor)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(shared.string.common_notes_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}