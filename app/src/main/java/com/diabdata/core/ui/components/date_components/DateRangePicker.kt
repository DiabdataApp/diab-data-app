package com.diabdata.core.ui.components.date_components

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.diabdata.shared.R as shared
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeModal(
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli(),
        initialSelectedEndDateMillis = initialEndDate?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        val startDate = Instant.ofEpochMilli(start)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val endDate = Instant.ofEpochMilli(end)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateRangeSelected(startDate, endDate)
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(shared.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(shared.string.common_cancel))
            }
        }
    ) {
        DateRangePicker(
            state = state,
            showModeToggle = false
        )
    }
}
