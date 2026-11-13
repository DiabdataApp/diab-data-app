package com.diabdata.feature.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dateUtils.formatLocalDate
import com.diabdata.shared.utils.dateUtils.toCountString
import com.diabdata.core.ui.components.date_components.DateRangeModal
import com.diabdata.feature.graphs.components.LineGraph
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.ui.components.noDataView.NoDataView
import com.diabdata.core.utils.ScreenSize
import com.diabdata.core.utils.getScreenSize
import java.time.LocalDate
import java.time.Period
import com.diabdata.shared.R as shared

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphViewer(
    viewModel: DataViewModel = viewModel(),
) {
    val scrollState = rememberScrollState()
    val options = listOf(
        stringResource(shared.string.time_range_month),
        stringResource(shared.string.time_range_year),
        stringResource(shared.string.time_range_custom)
    )
    var selectedIndex by remember { mutableIntStateOf(2) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var customDateRange by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    val maxDate = LocalDate.now()
    val minDate = when (selectedIndex) {
        0 -> maxDate.minusMonths(1)
        1 -> maxDate.minusYears(1)
        2 -> customDateRange?.first ?: maxDate.minusYears(2)
        else -> maxDate.minusYears(5)
    }
    val maxDateAdjusted = when (selectedIndex) {
        2 -> customDateRange?.second ?: maxDate
        else -> maxDate
    }
    val period: Period = Period.between(minDate, maxDateAdjusted)
    val weightPoints by viewModel
        .getWeightPlotData(minDate, maxDateAdjusted)
        .collectAsState(initial = emptyList())
    val hba1cPoints by viewModel
        .getHba1cPlotData(minDate, maxDateAdjusted)
        .collectAsState(initial = emptyList())
    var showRegressionLine by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ── Window size ──
    val windowSize = getScreenSize()
    val isLargeScreen = windowSize == ScreenSize.LARGE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween
            ),
        ) {
            options.forEachIndexed { index, label ->
                ToggleButton(
                    checked = selectedIndex == index,
                    colors = ToggleButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    onCheckedChange = { selectedIndex = index },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(0.8f)
                        .height(40.dp)
                        .semantics { role = Role.RadioButton },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    if (selectedIndex == index) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(label)
                }
            }
            if (selectedIndex == 2) {
                IconButton(
                    onClick = { showDateRangePicker = true },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .aspectRatio(1f)
                ) {
                    SvgIcon(
                        resId = shared.drawable.date_range_icon_vector,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            IconButton(
                onClick = { showRegressionLine = !showRegressionLine },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (showRegressionLine)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .aspectRatio(1f)
            ) {
                SvgIcon(
                    resId = shared.drawable.trending_up_icon_vector,
                    color = if (showRegressionLine)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (selectedIndex == 2) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${
                            stringResource(
                                shared.string.database_time_range_indicator,
                                formatLocalDate(minDate),
                                formatLocalDate(maxDateAdjusted)
                            )
                        } (${period.toCountString(context)})",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (weightPoints.isNotEmpty() && hba1cPoints.isNotEmpty()) {
            if (isLargeScreen) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    LineGraph(
                        points = weightPoints,
                        label = stringResource(AddableType.WEIGHT.displayNameRes),
                        primaryColor = AddableType.WEIGHT.baseColor.toArgb(),
                        showTrendLine = showRegressionLine,
                        modifier = Modifier
                            .weight(1f)
                            .height(340.dp),
                    )
                    LineGraph(
                        points = hba1cPoints,
                        label = stringResource(AddableType.HBA1C.displayNameRes),
                        primaryColor = AddableType.HBA1C.baseColor.toArgb(),
                        showTrendLine = showRegressionLine,
                        modifier = Modifier
                            .weight(1f)
                            .height(340.dp),
                    )
                }
            } else {
                LineGraph(
                    points = weightPoints,
                    label = stringResource(AddableType.WEIGHT.displayNameRes),
                    primaryColor = AddableType.WEIGHT.baseColor.toArgb(),
                    showTrendLine = showRegressionLine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                )
                LineGraph(
                    points = hba1cPoints,
                    label = stringResource(AddableType.HBA1C.displayNameRes),
                    primaryColor = AddableType.HBA1C.baseColor.toArgb(),
                    showTrendLine = showRegressionLine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                )
            }
        } else {
            NoDataView()
        }
    }

    if (showDateRangePicker) {
        DateRangeModal(
            initialStartDate = minDate,
            initialEndDate = maxDateAdjusted,
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                customDateRange = start to end
                showDateRangePicker = false
            }
        )
    }
}