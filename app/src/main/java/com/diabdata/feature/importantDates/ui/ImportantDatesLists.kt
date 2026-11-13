package com.diabdata.feature.importantDates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.UNKNOWN_TIME_DEFAULT
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.settings.sections.dataSettings.ui.components.locale
import com.diabdata.feature.userProfile.UserProfileViewModel
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.diabdata.shared.R as shared

@Composable
fun ImportantDatesList(viewModel: DataViewModel) {
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()
    val availability by viewModel.dataAvailability.collectAsState()
    val userDetails by userProfileViewModel.userDetails.collectAsState()
    val showSection = availability.hasImportantDates || userDetails?.diagnosisDate != null
    val importantDates by viewModel.importantDates.collectAsState(initial = emptyList())

    if (showSection) {
        ImportantDatesListContent(
            importantDates = importantDates,
            diagnosisDate = userDetails?.diagnosisDate,
            diabetesType = userDetails?.diabetesType
        )
    }
}

@Composable
fun ImportantDatesListContent(
    importantDates: List<ImportantDate>,
    diagnosisDate: LocalDate? = null,
    diabetesType: DiabetesType? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val diagnosisLabel = if (diabetesType != null) {
        stringResource(
            shared.string.specific_diabetes_discovery_label,
            stringResource(diabetesType.displayNameRes)
        )
    } else {
        stringResource(shared.string.generic_diabetes_discovery_label)
    }

    val allDates = buildList {
        if (diagnosisDate != null) {
            add(
                ImportantDate(
                    id = -1,
                    date = diagnosisDate,
                    importantDate = diagnosisLabel,
                    isArchived = false,
                    createdAt = UNKNOWN_TIME_DEFAULT,
                    updatedAt = UNKNOWN_TIME_DEFAULT,
                    entryUuid = "diagnosis-date"
                )
            )
        }
        addAll(importantDates)
    }

    CardsList(
        header = stringResource(shared.string.home_section_important_dates),
        cards = allDates.map { diagnosis ->
            val years = ChronoUnit.YEARS.between(diagnosis.date, today)
            val monthsTotal = ChronoUnit.MONTHS.between(diagnosis.date, today)
            val remainingMonths = monthsTotal - years * 12
            val elapsedText = when {
                years == 0L && remainingMonths == 0L -> stringResource(shared.string.date_today)
                years == 0L -> pluralStringResource(
                    shared.plurals.plurals_months,
                    remainingMonths.toInt(),
                    remainingMonths
                )

                remainingMonths == 0L -> pluralStringResource(
                    shared.plurals.plurals_years,
                    years.toInt(),
                    years
                )

                else -> pluralStringResource(
                    shared.plurals.years_and_months,
                    years.toInt(),
                    years,
                    remainingMonths
                )
            }
            CardItem(
                leadingColoredCircleIcon = ColoredIconCircleProps(
                    iconRes = AddableType.IMPORTANT_DATE.iconRes,
                    baseColor = AddableType.IMPORTANT_DATE.baseColor,
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
                                text = diagnosis.importantDate,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = stringResource(
                                    shared.string.important_date_on_text,
                                    formatLocalDate(diagnosis.date)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SvgIcon(
                                resId = shared.drawable.event_icon_vector,
                                modifier = Modifier.size(15.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = elapsedText,
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

const val locale = "fr"

@Preview(name = "Interactive", showBackground = true, locale = locale)
@Composable
fun ImportantDatesListPreview() {
    var darkTheme by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    val firstDate = LocalDate.of(
        today.minusYears(5).year,
        today.minusMonths(5).month,
        today.minusDays(23).dayOfMonth
    )
    val secondDate = LocalDate.of(
        today.minusYears(4).year,
        today.minusMonths(4).month,
        today.minusDays(23).dayOfMonth
    )

    val fakeData = listOf(
        ImportantDate(
            importantDate = "Diagnostic du diabète",
            date = firstDate,
            id = 1,
            createdAt = UNKNOWN_TIME_DEFAULT,
            isArchived = false,
            updatedAt = UNKNOWN_TIME_DEFAULT,
            entryUuid = "preview-1"
        ),
        ImportantDate(
            importantDate = "Pose de pompe à insuline",
            date = secondDate,
            id = 2,
            createdAt = UNKNOWN_TIME_DEFAULT,
            isArchived = false,
            updatedAt = UNKNOWN_TIME_DEFAULT,
            entryUuid = "preview-2"
        )
    )

    DiabDataTheme(dynamicColor = false, darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp)
            ) {
                Column {
                    ImportantDatesListContent(fakeData)
                }
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
                            fontWeight = FontWeight(800),
                        )
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it }
                        )
                    }
                }
            }
        }
    }
}
