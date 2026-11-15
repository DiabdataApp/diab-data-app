package com.diabdata.feature.dataMatrixScanner.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.dataMatrixScanner.utils.MedicalDeviceInfo
import com.diabdata.feature.dataMatrixScanner.utils.MedicationInfo
import com.diabdata.feature.dataMatrixScanner.utils.parseMedicalDevice
import com.diabdata.feature.dataMatrixScanner.utils.parseMedication
import com.diabdata.shared.R as shared

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun DataMatrixScannerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onResult: (ScanResult) -> Unit,
    scannedType: ScannableTypes
) {
    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.95f))
            .clickable { onDismiss() } // ferme si on clique en dehors
    ) {
        val scanSize = 280.dp

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(scanSize)
                .clip(RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            CameraPreview(
                modifier = Modifier.matchParentSize(),
                onBarcodeDetected = { rawValue ->
                    when (scannedType) {
                        ScannableTypes.MEDICATION -> {
                            val parsed = parseMedication(rawValue)
                            onResult(ScanResult.Medication(parsed))
                            onDismiss()
                        }

                        ScannableTypes.DEVICE -> {
                            val parsed = parseMedicalDevice(rawValue)
                            onResult(ScanResult.Device(parsed))
                            onDismiss()
                        }
                    }
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = scanSize / 2 + 16.dp) // sous le carré
        ) {
            SvgIcon(
                resId = shared.drawable.lightbulb_icon_vector,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(shared.string.scanner_viewfinder_hint_text),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

enum class ScannableTypes {
    MEDICATION,
    DEVICE
}

sealed class ScanResult {
    data class Medication(val data: MedicationInfo) : ScanResult()
    data class Device(val data: MedicalDeviceInfo) : ScanResult()
}

fun ScanResult.toEntity(): Any = when (this) {
    is ScanResult.Medication -> MedicationInfo(
        gtin = data.gtin,
        lot = data.lot,
        expiration = data.expiration,
        serial = data.serial
    )

    is ScanResult.Device -> MedicalDeviceInfo(
        gtin = data.gtin,
        lot = data.lot,
        expiration = data.expiration,
        serialNumber = data.serialNumber,
        referenceNumber = data.referenceNumber
    )
}
