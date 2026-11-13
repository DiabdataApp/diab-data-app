package com.diabdata.feature.treatments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.Treatment
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.TreatmentType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import com.diabdata.shared.utils.dateUtils.toRelativeString
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.utils.ui.SvgIcon
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.time.Instant
import com.diabdata.shared.R as shared

data class TreatmentCardData(
    val titleText: String,
    val dateText: String,
    val icon: Int,
    val isExpiringSoon: Boolean,
    val untilDateIcon: Int,
    val type: String,
    val remainingText: String
)

@Composable
fun UpcomingTreatmentExpirationDatesContent(
    treatments: List<Treatment>
) {
    if (treatments.isEmpty()) return

    val context = LocalContext.current
    val today = LocalDate.now()
    val soonThreshold = today.plusDays(30)

    fun getUntilIconIdMap(isExpiringSoon: Boolean): Int =
        if (isExpiringSoon) shared.drawable.warning_icon_vector else shared.drawable.hourglass_icon_vector

    val treatmentCards = treatments.map { treatment ->
        val remainingText = treatment.expirationDate.toRelativeString(context)
        val daysUntil = ChronoUnit.DAYS.between(today, treatment.expirationDate).toInt()
        val isExpiringSoon = daysUntil in 0..29
        val untilDateIcon = getUntilIconIdMap(isExpiringSoon)
        TreatmentCardData(
            titleText = treatment.name.ifBlank { treatment.type.displayName(context) },
            dateText = stringResource(
                shared.string.expires_on_text,
                formatLocalDate(treatment.expirationDate)
            ),
            icon = treatment.type.iconRes,
            isExpiringSoon = treatment.expirationDate.isBefore(soonThreshold),
            type = treatment.type.displayName(context),
            remainingText = remainingText,
            untilDateIcon = untilDateIcon,
        )
    }

    CardsList(
        header = stringResource(shared.string.medications_homscreen_upcoming_treatment_expirations),
        pageSize = 4,
        cards = treatmentCards.map { card ->
            CardItem(
                leadingColoredCircleIcon = ColoredIconCircleProps(
                    iconRes = card.icon,
                    baseColor = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else AddableType.TREATMENT.baseColor,
                    size = 40.dp,
                    iconSize = 26.dp
                ),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.titleText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = card.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = card.dateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SvgIcon(
                                resId = card.untilDateIcon,
                                modifier = Modifier.size(15.dp),
                                color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = card.remainingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun UpcomingTreatmentExpirationDates(viewModel: DataViewModel) {
    val treatments by viewModel.upcomingExpiringTreatmentDates.collectAsState()
    UpcomingTreatmentExpirationDatesContent(treatments)
}

@Preview(showBackground = true)
@Composable
fun PreviewUpcomingTreatmentExpirationDates() {
    val today = LocalDate.now()
    val sampleTreatments = listOf(
        Treatment(
            id = 1,
            expirationDate = LocalDate.now().plusMonths(2),
            name = "Levemir Penfill",
            createdAt = Instant.parse(today.minusYears(1).toString()),
            updatedAt = Instant.parse(today.minusMonths(7).toString()),
            isArchived = false,
            type = TreatmentType.GLUCAGON_SPRAY,
            entryUuid = "preview-1"
        ),
        Treatment(
            id = 1,
            expirationDate = LocalDate.now().plusMonths(2),
            name = "Levemir Penfill",
            createdAt = Instant.parse(today.minusYears(1).toString()),
            updatedAt = Instant.parse(today.minusMonths(7).toString()),
            isArchived = false,
            type = TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE,
            entryUuid = "preview-2"
        ),
        Treatment(
            id = 1,
            expirationDate = LocalDate.now().plusMonths(2),
            name = "Levemir Penfill",
            createdAt = Instant.parse(today.minusYears(1).toString()),
            updatedAt = Instant.parse(today.minusMonths(7).toString()),
            isArchived = false,
            type = TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE,
            entryUuid = "preview-3"
        ),
        Treatment(
            id = 1,
            expirationDate = LocalDate.now().plusMonths(2),
            name = "Levemir Penfill",
            createdAt = Instant.parse(today.minusYears(1).toString()),
            updatedAt = Instant.parse(today.minusMonths(7).toString()),
            isArchived = false,
            type = TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE,
            entryUuid = "preview-4"
        ),
        Treatment(
            id = 2,
            expirationDate = LocalDate.now().plusDays(23),
            name = "Novorapid Penfill",
            createdAt = Instant.parse(today.minusYears(1).toString()),
            updatedAt = Instant.parse(today.minusDays(18).toString()),
            isArchived = false,
            type = TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE,
            entryUuid = "preview-5"
        )
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            UpcomingTreatmentExpirationDatesContent(sampleTreatments)
        }
    }
}
