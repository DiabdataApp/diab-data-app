package com.diabdata.feature.casting.relay.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diabdata.feature.casting.relay.ConnectionState
import com.diabdata.feature.casting.relay.RelayViewModel
import com.diabdata.feature.casting.relay.ShareMode
import com.diabdata.shared.R as shared
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ShareDialog(mode: ShareMode, onDismiss: () -> Unit) {
    val viewModel: RelayViewModel = viewModel()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startSharing(mode)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSharing()
        }
    }

    Dialog(onDismissRequest = {
        viewModel.stopSharing()
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (mode == ShareMode.COMPANION) {
                        stringResource(shared.string.share_dialog_title_compagnon_mode)
                    } else {
                        stringResource(shared.string.share_dialog_title_medical_mode)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Status
                when (connectionState) {
                    is ConnectionState.Connecting -> {
                        CircularProgressIndicator()
                        Text(stringResource(shared.string.share_dialog_connecting_to_relay_message))
                    }

                    is ConnectionState.Connected -> {
                        val token = viewModel.token ?: return@Column

                        val qrBitmap = remember(token) {
                            generateQrCode("https://app.diabdata.fr/link_device?token=$token")
                        }
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(200.dp)
                            )
                        }

                        Text(
                            text = token,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (mode == ShareMode.COMPANION) {
                                stringResource(
                                    shared.string.share_dialog_compagnon_mode_instruction_message
                                )
                            } else {
                                stringResource(
                                    shared.string.share_dialog_medical_mode_instruction_message
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.stopSharing()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(shared.string.share_dialog_stop_sharing_button_label)
                            )
                        }
                    }

                    is ConnectionState.Error -> {
                        Text(
                            text = "❌ ${(connectionState as ConnectionState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            viewModel.startSharing(mode)
                        }) {
                            Text(stringResource(shared.string.common_retry))
                        }
                    }

                    is ConnectionState.Disconnected -> {}
                }
            }
        }
    }
}

private fun generateQrCode(content: String, size: Int = 512): Bitmap? = try {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap[x, y] =
                if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    bitmap
} catch (e: Exception) {
    null
}
