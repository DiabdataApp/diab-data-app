package com.diabdata.feature.devices.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.ui.components.actionInput.FaultyToggleButton
import com.diabdata.core.utils.ui.ColoredIconCircle
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.core.utils.ui.darken
import com.diabdata.core.utils.ui.getItemShape
import com.diabdata.feature.devices.DevicesViewModel
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dateUtils.shortenedFormatLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.time.Instant
import com.diabdata.shared.R as shared

@Composable
fun CurrentNonConsumableDevicesList() {
    val devicesViewModel: DevicesViewModel = hiltViewModel()
    val currentDevices by devicesViewModel.nonConsumableDevices.collectAsState(initial = emptyList())
    val showSection = currentDevices.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    if (showSection) {
        CurrentNonConsumableDevicesCards(
            currentDevices,
            onMarkFaulty = { device ->
                coroutineScope.launch {
                    devicesViewModel.updateDevice(
                        device.copy(
                            isFaulty = !device.isFaulty
                        )
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CurrentNonConsumableDevicesCards(
    devices: List<MedicalDevice>,
    onMarkFaulty: (MedicalDevice) -> Unit = {},
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    if (devices.isEmpty()) return

    val cards = devices.map { device ->
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

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(shared.string.devices_home_section_current_non_consumable_devices_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

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

                            FaultyToggleButton(
                                isFaulty = card.device.isFaulty,
                                isReported = card.device.isReported,
                                onClick = { onMarkFaulty(card.device) },
                                animatedContainerColor = animatedContainerColor,
                                animatedIconColor = animatedIconColor,
                            )
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
            if (index != cards.size - 1) Spacer(Modifier.height(3.dp))
        }
    }
}

@Preview(
    showBackground = true, locale = "en", showSystemUi = false,
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun NonConsumableDevicesListPreview() {
    val fakeData = listOf(
        MedicalDevice(
            id = 1,
            date = LocalDate.of(2025, 9, 21),
            lifeSpanEndDate = LocalDate.of(2025, 9, 21).plusDays(3),
            name = "Omnipod 5",
            batchNumber = "567GGU",
            serialNumber = "UTYFYZ38",
            referenceNumber = "1R5TFG",
            manufacturer = "Insulet",
            deviceType = MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE,
            createdAt = Instant.parse(LocalDate.of(2025, 9, 25).toString()),
            isArchived = false,
            lifeSpan = 0,
            isFaulty = false,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = Instant.parse(LocalDate.of(2025, 9, 1).toString()),
            entryUuid = "preview-1"
        )
    )

    MaterialTheme {
        Column {
            CurrentNonConsumableDevicesCards(fakeData)
        }
    }
}