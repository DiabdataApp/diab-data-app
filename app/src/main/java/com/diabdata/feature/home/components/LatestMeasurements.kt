package com.diabdata.feature.home.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diabdata.core.database.DataViewModel
import com.diabdata.feature.appointments.ui.UpcomingAppointmentsList
import com.diabdata.feature.importantDates.ui.ImportantDatesList
import com.diabdata.feature.treatments.ui.UpcomingTreatmentExpirationDates

@SuppressLint("DefaultLocale")
@Composable
fun LatestMeasurements(viewModel: DataViewModel) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .padding(bottom = 70.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(35.dp)
    ) {
        val availability by viewModel.dataAvailability.collectAsState()

        if (availability.hasImportantDates) {
            ImportantDatesList(viewModel)
        }
        if (availability.hasWeights || availability.hasHba1c) {
            LatestMeasures(viewModel)
        }
        if (availability.hasAppointments) {
            UpcomingAppointmentsList(viewModel)
        }
        if (availability.hasTreatments) {
            UpcomingTreatmentExpirationDates(viewModel)
        }
    }
}
