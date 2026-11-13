package com.diabdata.feature.appointments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.Appointment
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dateUtils.formatLocalDateTime
import com.diabdata.shared.utils.dateUtils.toRelativeString
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.utils.ui.SvgIcon
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Instant
import com.diabdata.shared.R as shared

@Composable
fun UpcomingAppointmentsListContent(
    upcomingAppointments: List<Appointment>
) {
    if (upcomingAppointments.isEmpty()) return

    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    CardsList(
        header = stringResource(shared.string.appointments_homescreen_upcoming_appointments),
        pageSize = 3,
        cards = upcomingAppointments.map { appointment ->
            val remainingText = appointment.date.toRelativeString(context)

            CardItem(
                leadingColoredCircleIcon = ColoredIconCircleProps(
                    iconRes = appointment.type.iconRes,
                    baseColor = AddableType.APPOINTMENT.baseColor,
                    size = 40.dp,
                    iconSize = 25.dp
                ),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${appointment.doctor} - (${
                                    appointment.type.displayName(
                                        context
                                    )
                                })",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!appointment.notes.isNullOrBlank()) {
                                    var showNotesDialog by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = { showNotesDialog = true },
                                        modifier = Modifier.size(22.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        SvgIcon(
                                            resId = shared.drawable.note_icon_vector,
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (showNotesDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showNotesDialog = false },
                                            icon = {
                                                SvgIcon(
                                                    resId = shared.drawable.note_icon_vector,
                                                    modifier = Modifier.size(48.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            title = {
                                                Text(
                                                    text = stringResource(shared.string.common_notes_label),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            },
                                            text = {
                                                Text(
                                                    text = appointment.notes,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    showNotesDialog = false
                                                }) {
                                                    Text(text = stringResource(shared.string.common_confirm))
                                                }
                                            }
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(
                                        shared.string.scheduled_on_date_text,
                                        formatLocalDateTime(appointment.date)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SvgIcon(
                                resId = shared.drawable.hourglass_icon_vector,
                                modifier = Modifier.size(15.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = remainingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun UpcomingAppointmentsList(viewModel: DataViewModel) {
    val upcomingAppointments by viewModel.upcomingAppointment.collectAsState()
    UpcomingAppointmentsListContent(upcomingAppointments)
}

@Preview(showBackground = true)
@Composable
fun PreviewUpcomingAppointmentsList() {
    val sampleAppointments = listOf(
        Appointment(
            doctor = "Dr. Dupont",
            type = AppointmentType.APPOINTMENT,
            date = LocalDateTime.now().plusDays(5),
            notes = "Rappel vaccin",
            id = 1,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-1"
        ),
        Appointment(
            doctor = "Dr. Martin",
            type = AppointmentType.ANNUAL_CHECKUP,
            date = LocalDateTime.now().plusMonths(2),
            notes = null,
            id = 2,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-2"
        )
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            UpcomingAppointmentsListContent(upcomingAppointments = sampleAppointments)
        }
    }
}