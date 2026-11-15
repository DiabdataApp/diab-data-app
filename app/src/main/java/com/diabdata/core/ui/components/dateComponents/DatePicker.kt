package com.diabdata.core.ui.components.dateComponents

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.diabdata.shared.R as shared
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun DateSelector(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    isExpiryDate: Boolean = false,
    isStartDate: Boolean = false,
    isEndDate: Boolean = false,
    @StringRes labelRes: Int? = null
) {
    val locale = LocalLocale.current.platformLocale
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    }

    var dateText by remember { mutableStateOf(TextFieldValue(date.format(formatter))) }
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        dateText = TextFieldValue(date.format(formatter))
    }

    val labelText = if (labelRes != null) {
        stringResource(labelRes)
    } else {
        when {
            isExpiryDate -> stringResource(shared.string.popup_placeholder_expiration_date)
            isStartDate -> stringResource(shared.string.popup_placeholder_start_date)
            isEndDate -> stringResource(shared.string.popup_placeholder_end_date)
            else -> stringResource(shared.string.popup_placeholder_date)
        }
    }

    OutlinedTextField(
        value = dateText,
        onValueChange = { newValue ->
            dateText = newValue
            try {
                val parsedDate = LocalDate.parse(newValue.text, formatter)
                onDateSelected(parsedDate)
            } catch (_: DateTimeParseException) {
            }
        },
        label = { Text(labelText) },
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = labelText,
                modifier = Modifier.clickable { showModal = true }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showModal = true },
        shape = MaterialTheme.shapes.small
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    val newDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(newDate)
                }
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(stringResource(shared.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(shared.string.common_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DateTimeSelector(
    dateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    @StringRes labelRes: Int? = null
) {
    val locale = LocalLocale.current.platformLocale
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)
    }

    var dateTimeText by remember { mutableStateOf(TextFieldValue(dateTime.format(formatter))) }
    var showDateModal by remember { mutableStateOf(false) }
    var showTimeModal by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(dateTime.toLocalDate()) }

    LaunchedEffect(dateTime) {
        dateTimeText = TextFieldValue(dateTime.format(formatter))
    }

    val labelText = if (labelRes != null) {
        stringResource(labelRes)
    } else {
        stringResource(shared.string.popup_placeholder_date)
    }

    OutlinedTextField(
        value = dateTimeText,
        onValueChange = { newValue ->
            dateTimeText = newValue
            try {
                val parsed = LocalDateTime.parse(newValue.text, formatter)
                onDateTimeSelected(parsed)
            } catch (_: DateTimeParseException) {
            }
        },
        label = { Text(labelText) },
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = labelText,
                modifier = Modifier.clickable { showDateModal = true }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDateModal = true },
        shape = MaterialTheme.shapes.small
    )

    // Étape 1 : Sélection de la date
    if (showDateModal) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    selectedDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    showTimeModal = true // → passe à l'étape 2
                }
                showDateModal = false
            },
            onDismiss = { showDateModal = false }
        )
    }

    // Étape 2 : Sélection de l'heure
    if (showTimeModal) {
        TimePickerModal(
            initialHour = dateTime.hour,
            initialMinute = dateTime.minute,
            onTimeSelected = { hour, minute ->
                val newDateTime = selectedDate.atTime(hour, minute)
                onDateTimeSelected(newDateTime)
                showTimeModal = false
            },
            onDismiss = { showTimeModal = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
            }) {
                Text(stringResource(shared.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(shared.string.common_cancel))
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
