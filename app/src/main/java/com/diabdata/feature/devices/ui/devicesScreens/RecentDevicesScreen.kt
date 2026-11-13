package com.diabdata.feature.devices.ui.devicesScreens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.MedicalDeviceInfoEntity
import com.diabdata.core.model.newEntryUuid
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.devices.ui.components.AddDeviceFab
import com.diabdata.feature.devices.ui.components.CurrentConsumableDevicesList
import com.diabdata.feature.devices.ui.components.CurrentNonConsumableDevicesList
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.feature.dataMatrixScanner.ui.DataMatrixScannerDialog
import com.diabdata.feature.dataMatrixScanner.ui.ScanResult
import com.diabdata.feature.dataMatrixScanner.ui.ScannableTypes
import com.diabdata.core.ui.components.addDataPopup.AddDataPopup
import com.diabdata.core.ui.components.noDataView.IconTypes
import com.diabdata.core.ui.components.noDataView.NoDataView
import com.diabdata.feature.dataMatrixScanner.ScannerViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RecentDevicesScreen(
    dataViewModel: DataViewModel
) {
    val scannerViewModel: ScannerViewModel = hiltViewModel()

    val availability by dataViewModel.dataAvailability.collectAsState()
    var showAddDevicePopup by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    var prefilledDevice by remember { mutableStateOf<MedicalDevice?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .padding(20.dp)
            .padding(top = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(35.dp)
    ) {
        if (availability.hasDevices) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(35.dp)
            ) {
                CurrentNonConsumableDevicesList()
                CurrentConsumableDevicesList()
                Spacer(modifier = Modifier.height(70.dp))
            }
        } else {
            NoDataView(iconType = IconTypes.DEVICES)
        }

    }

    AddDeviceFab(onSelect = { showAddDevicePopup = true }, onScanClick = { showScanner = true })

    if (showAddDevicePopup) {
        AddDataPopup(
            type = AddableType.DEVICE,
            dataViewModel = dataViewModel,
            onDismiss = {
                showAddDevicePopup = false
            },
            prefilledMedicalDevice = prefilledDevice,
        )
    }

    if (showScanner) {
        DataMatrixScannerDialog(
            onDismiss = { showScanner = false }, onResult = { result ->
                scope.launch {
                    when (result) {
                        is ScanResult.Device -> {
                            val info = result.data
                            val entity = scannerViewModel.getMedicalDeviceByCode(info.gtin)
                            Log.d("EXTRACTED-GTIN", "GTIN: ${info.gtin}\n - entity: $entity,\n-device: ${result.data},\n-lot: ${result.data.lot},\n- serial: ${result.data.serialNumber},\n- ref: ${result.data.referenceNumber}")

                            if (entity != null) {
                                val device = generateMedicalDeviceEntry(result, entity)
                                prefilledDevice = device
                                showAddDevicePopup = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Appareil inconnu pour GTIN ${info.gtin}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> Unit
                    }
                    showScanner = false
                }
            }, visible = showScanner, scannedType = ScannableTypes.DEVICE
        )
    }
}

fun generateMedicalDeviceEntry(
    scanResult: ScanResult.Device, medicalDeviceInfo: MedicalDeviceInfoEntity
): MedicalDevice {
    val today = LocalDate.now()
    val now = nowMillis()

    val deviceEntry = MedicalDevice(
        date = today,
        lifeSpanEndDate = today.plusDays(medicalDeviceInfo.daysLifespan.toLong()),
        name = medicalDeviceInfo.fullName,
        batchNumber = scanResult.data.lot,
        serialNumber = scanResult.data.serialNumber,
        manufacturer = medicalDeviceInfo.manufacturer,
        deviceType = medicalDeviceInfo.deviceType,
        createdAt = now,
        isArchived = false,
        lifeSpan = medicalDeviceInfo.daysLifespan,
        isFaulty = false,
        isReported = false,
        isLifeSpanOver = false,
        updatedAt = now,
        referenceNumber = scanResult.data.referenceNumber,
        entryUuid = newEntryUuid()
    )

    return deviceEntry
}