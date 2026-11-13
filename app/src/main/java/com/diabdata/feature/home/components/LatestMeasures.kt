package com.diabdata.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.Weight
import com.diabdata.core.ui.components.cardsList.CardItem
import com.diabdata.core.ui.components.cardsList.CardsList
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import java.time.LocalDate
import kotlin.time.Instant
import com.diabdata.shared.R as shared

data class MeasureCardData(
    val textColor: Color,
    val titleText: String,
    val dateText: String,
    val addableType: AddableType,
    val trendIcon: Int? = null
)

@Composable
fun LatestMeasuresContent(
    weightEntries: List<Weight>,
    hba1cEntries: List<Hba1c>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val oneYearAgo = today.minusYears(1)

    if (weightEntries.isEmpty() && hba1cEntries.isEmpty()) return

    val measures = buildList {
        weightEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    textColor = primaryColor,
                    titleText = String.format(LocalLocale.current.platformLocale, "%.2f kg", latest.value),
                    dateText = stringResource(
                        shared.string.weight_on_date_text,
                        formatLocalDate(latest.date)
                    ),
                    addableType = AddableType.WEIGHT,
                    trendIcon = computeTrendIcon(
                        entries = weightEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value }
                    )
                )
            )
        }
        hba1cEntries.maxByOrNull { it.date }?.let { latest ->
            add(
                MeasureCardData(
                    textColor = when {
                        latest.value in 7.5..8.5 -> colorResource(R.color.archived_primary)
                        latest.value >= 8.6 -> MaterialTheme.colorScheme.error
                        else -> primaryColor
                    },
                    titleText = String.format(LocalLocale.current.platformLocale, "%.1f%%", latest.value),
                    dateText = stringResource(
                        shared.string.hba1c_on_date_text,
                        formatLocalDate(latest.date)
                    ),
                    addableType = AddableType.HBA1C,
                    trendIcon = computeTrendIcon(
                        entries = hba1cEntries,
                        oneYearAgo = oneYearAgo,
                        valueExtractor = { it.value }
                    )
                )
            )
        }
    }

    CardsList(
        header = stringResource(shared.string.home_section_latest_measures),
        cards = measures.map { card ->
            CardItem(
                leadingColoredCircleIcon = ColoredIconCircleProps(
                    iconRes = card.addableType.iconRes,
                    baseColor = if (card.textColor != primaryColor) card.textColor else card.addableType.baseColor,
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
                                text = card.titleText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = card.textColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = card.dateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        card.trendIcon?.let { iconRes ->
                            SvgIcon(
                                resId = iconRes,
                                modifier = Modifier.size(15.dp),
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
fun LatestMeasures(viewModel: DataViewModel) {
    val weightEntries by viewModel.recentWeights.collectAsState()
    val hba1cEntries by viewModel.recentHba1c.collectAsState()
    LatestMeasuresContent(weightEntries, hba1cEntries)
}

@Preview(showBackground = true)
@Composable
fun PreviewLatestMeasures() {
    val sampleWeights = listOf(
        Weight(
            date = LocalDate.now().minusDays(3),
            value = 88.4f,
            id = 1,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-1"
        ),
        Weight(
            date = LocalDate.now().minusDays(10),
            value = 83.7f,
            id = 2,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-2"
        )
    )
    val sampleHba1c = listOf(
        Hba1c(
            date = LocalDate.now().minusMonths(2),
            value = 6.8f,
            id = 3,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-3"
        ),
        Hba1c(
            date = LocalDate.now().minusMonths(6),
            value = 7.2f,
            id = 4,
            createdAt = Instant.parse(LocalDate.of(2015, 3, 1).toString()),
            isArchived = false,
            updatedAt = Instant.parse(LocalDate.of(2015, 4, 2).toString()),
            entryUuid = "preview-4"
        )
    )
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            LatestMeasuresContent(
                weightEntries = sampleWeights,
                hba1cEntries = sampleHba1c
            )
        }
    }
}

private fun <T> computeTrendIcon(
    entries: List<T>,
    oneYearAgo: LocalDate,
    valueExtractor: (T) -> Number,
    dateExtractor: (T) -> LocalDate = {
        when (it) {
            is Weight -> it.date
            is Hba1c -> it.date
            else -> LocalDate.MIN
        }
    }
): Int? {
    val lastYearEntries = entries.filter { dateExtractor(it).isAfter(oneYearAgo) }
    if (lastYearEntries.size < 2) return null

    val sorted = lastYearEntries.sortedBy { dateExtractor(it) }
    val firstValue = valueExtractor(sorted.first()).toFloat()
    val lastValue = valueExtractor(sorted.last()).toFloat()

    return when {
        lastValue > firstValue -> shared.drawable.trending_up_icon_vector
        lastValue < firstValue -> shared.drawable.trending_down_icon_vector
        else -> shared.drawable.trending_flat_icon_vector
    }
}
