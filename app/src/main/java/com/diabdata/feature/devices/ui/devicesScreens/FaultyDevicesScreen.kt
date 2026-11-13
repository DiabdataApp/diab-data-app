package com.diabdata.feature.devices.ui.devicesScreens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.MedicalDevice
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dateUtils.shortenedFormatLocalDate
import com.diabdata.core.utils.ui.ColoredIconCircle
import com.diabdata.feature.devices.ui.components.MedicalDeviceCardData
import com.diabdata.core.ui.components.actionInput.ButtonType
import com.diabdata.core.ui.components.tables.DataTable
import com.diabdata.core.ui.components.tables.DataTableDecoration
import com.diabdata.core.ui.components.actionInput.FaultyToggleButton
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.ui.components.noDataView.IconTypes
import com.diabdata.core.ui.components.noDataView.NoDataView
import com.diabdata.core.utils.ui.darken
import com.diabdata.core.utils.ui.getItemShape
import com.diabdata.feature.devices.DevicesViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.time.Instant
import com.diabdata.shared.R as shared

@Suppress("unused")
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FaultyDevices(
    dataViewModel: DataViewModel
) {
    val devicesViewModel: DevicesViewModel = hiltViewModel()
    val faultyDevices by devicesViewModel.faultyDevices.collectAsState(initial = emptyList())
    val faultyCountsByBatches by devicesViewModel.faultyBatchNumbersTableData.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    FaultyDevicesScreen(
        faultyDevices = faultyDevices,
        faultyCountsByBatchNumbers = faultyCountsByBatches,
        onMarkAsReported = { device ->
            coroutineScope.launch {
                devicesViewModel.updateDevice(device.copy(isReported = !device.isReported))
            }
        },
        onMarkAsFaulty = { device ->
            coroutineScope.launch {
                devicesViewModel.updateDevice(device.copy(isFaulty = !device.isFaulty))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FaultyDevicesScreen(
    faultyDevices: List<MedicalDevice>,
    faultyCountsByBatchNumbers: List<List<String>>,
    onMarkAsReported: (MedicalDevice) -> Unit,
    onMarkAsFaulty: (MedicalDevice) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            if (faultyDevices.isEmpty() && faultyCountsByBatchNumbers.isEmpty()) {
                NoDataView(iconType = IconTypes.DEVICES)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 6.dp)
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(35.dp)
                ) {
                    if (faultyDevices.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(shared.string.devices_device_screen_faulty_heading),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                            DisplayCard(
                                items = faultyDevices,
                                onMarkFaulty = onMarkAsFaulty,
                                onMarkAsReported = onMarkAsReported
                            )
                        }
                    }

                    if (faultyCountsByBatchNumbers.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(shared.string.devices_device_screen_reported_heading),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                            DataTable(
                                headerColor = MaterialTheme.colorScheme.primary,
                                headers = listOf(
                                    stringResource(shared.string.devices_device_batch_number_text),
                                    stringResource(shared.string.devices_device_total_count_text)
                                ),
                                data = faultyCountsByBatchNumbers,
                                decoration = DataTableDecoration.build {
                                    alternateRowBackground(true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FaultyDevicesScreenPreview() {
    val sampleDevices = listOf(
        MedicalDevice(
            date = LocalDate.now(),
            lifeSpanEndDate = LocalDate.now().plusDays(10),
            name = "Pompe à insuline",
            batchNumber = "1234A",
            serialNumber = "SN001",
            manufacturer = "Acme",
            deviceType = MedicalDeviceInfoType.WIRED_PUMP,
            createdAt = Instant.parse(LocalDate.now().toString()),
            isArchived = false,
            lifeSpan = 365,
            isFaulty = true,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = Instant.parse(LocalDate.now().toString()),
            referenceNumber = "REF001",
            entryUuid = "preview-1"
        ),
        MedicalDevice(
            date = LocalDate.now(),
            lifeSpanEndDate = LocalDate.now().plusDays(5),
            name = "Quickserter",
            batchNumber = "5678B",
            serialNumber = "SN002",
            manufacturer = "Acme",
            deviceType = MedicalDeviceInfoType.WIRED_PATCH,
            createdAt = Instant.parse(LocalDate.now().toString()),
            isArchived = false,
            lifeSpan = 365,
            isFaulty = false,
            isReported = true,
            isLifeSpanOver = false,
            updatedAt = Instant.parse(LocalDate.now().toString()),
            referenceNumber = "REF002",
            entryUuid = "preview-2"
        )
    )
    val faultyCounts = listOf(
        listOf("1234A", "2"),
        listOf("5678B", "1")
    )

    FaultyDevicesScreen(
        faultyDevices = sampleDevices,
        faultyCountsByBatchNumbers = faultyCounts,
        onMarkAsReported = {},
        onMarkAsFaulty = {}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DisplayCard(
    items: List<MedicalDevice>,
    onMarkFaulty: (MedicalDevice) -> Unit = {},
    onMarkAsReported: (MedicalDevice) -> Unit = {}
) {
    val cards = items.map { device ->
        MedicalDeviceCardData(
            device = device,
            titleText = device.name.ifEmpty { device.deviceType.displayName(LocalContext.current) },
            textColor = if (device.isLifeSpanOver) MaterialTheme.colorScheme.error else device.deviceType.baseColor,
            addableType = AddableType.DEVICE,
            isLifeSpanOver = device.isLifeSpanOver,
            iconRes = device.deviceType.iconRes,
            date = device.date,
            lifeSpanEndDate = device.lifeSpanEndDate,
            lifeSpan = device.lifeSpan,
            batchNumber = device.batchNumber
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        cards.forEachIndexed { index, card ->
            val animatedContainerColor by animateColorAsState(
                targetValue = if (card.device.isFaulty) {
                    card.textColor.darken()
                } else {
                    card.textColor.copy(alpha = 0.2f)
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = EaseInOut
                ),
                label = "iconContainerColor"
            )

            val animatedIconColor by animateColorAsState(
                targetValue = if (card.device.isFaulty) {
                    MaterialTheme.colorScheme.onError
                } else {
                    card.textColor.darken()
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = EaseInOut
                ),
                label = "iconContentColor"
            )

            Surface(
                shape = getItemShape(index, cards.size),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColoredIconCircle(
                        iconRes = card.iconRes,
                        baseColor = if (card.textColor != primaryColor) card.textColor else card.addableType.baseColor,
                        size = 40.dp,
                        iconSize = 25.dp
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = card.textColor.darken(),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                val space = 4.dp

                                if (card.batchNumber.isNotBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = shared.drawable.lot_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.batchNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!card.device.referenceNumber.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = shared.drawable.ref_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.device.referenceNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!card.device.serialNumber.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = shared.drawable.sn_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.device.serialNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                FaultyToggleButton(
                                    isFaulty = card.device.isFaulty,
                                    isReported = card.device.isReported,
                                    onClick = { onMarkFaulty(card.device) },
                                    animatedContainerColor = animatedContainerColor,
                                    animatedIconColor = animatedIconColor,
                                )

                                FaultyToggleButton(
                                    isFaulty = card.device.isFaulty,
                                    isReported = card.device.isReported,
                                    type = ButtonType.REPORT,
                                    onClick = { onMarkAsReported(card.device) },
                                    animatedContainerColor = animatedContainerColor,
                                    animatedIconColor = animatedIconColor,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = shortenedFormatLocalDate(card.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}