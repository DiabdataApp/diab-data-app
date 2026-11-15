package com.diabdata.feature.databaseView

import androidx.compose.runtime.Composable
import com.diabdata.core.database.DataViewModel
import com.diabdata.feature.appointments.ui.AppointmentPopup
import com.diabdata.feature.devices.ui.MedicalDevicePopup
import com.diabdata.feature.hba1c.ui.Hba1cPopup
import com.diabdata.feature.importantDates.components.ImportantDatePopup
import com.diabdata.feature.treatments.ui.TreatmentPopup
import com.diabdata.feature.weight.ui.WeightPopup

@Composable
fun EditEntryDialog(
    entry: DataViewModel.MixedDbEntry,
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    when (entry) {
        is DataViewModel.MixedDbEntry.AppointmentEntry -> {
            AppointmentPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.TreatmentEntry -> {
            TreatmentPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.WeightEntry -> {
            WeightPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.Hba1cEntry -> {
            Hba1cPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.ImportantDateEntry -> {
            ImportantDatePopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.DeviceEntry -> {
            MedicalDevicePopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }
    }
}
