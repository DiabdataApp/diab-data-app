package com.diabdata.feature.dataMatrixScanner.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class ZxingAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.DATA_MATRIX),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val yBuffer = mediaImage.planes[0].buffer
            val data = ByteArray(yBuffer.remaining())
            yBuffer.get(data)

            val source = PlanarYUVLuminanceSource(
                data,
                mediaImage.width,
                mediaImage.height,
                0,
                0,
                mediaImage.width,
                mediaImage.height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val invertedBitmap = BinaryBitmap(HybridBinarizer(source.invert()))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                onBarcodeDetected(result.text)
            } catch (e: NotFoundException) {
                try {
                    val result = reader.decodeWithState(invertedBitmap)
                    onBarcodeDetected(result.text)
                } catch (e2: NotFoundException) {
                    Log.d(
                        "ZXING_SCANNER",
                        "No DataMatrix found in this frame (normal or inverted): $e, $e2"
                    )
                } catch (e2: Exception) {
                    Log.e("ZXING_SCANNER", "Error while decoding inverted: ${e2.message}", e2)
                }
            } catch (e: Exception) {
                Log.e("ZXING_SCANNER", "Error while decoding normal: ${e.message}", e)
            } finally {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CameraPreview(modifier: Modifier = Modifier, onBarcodeDetected: (String) -> Unit) {
    LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // --- Preview ---
                val preview = Preview.Builder().build()
                preview.surfaceProvider = previewView.surfaceProvider

                // --- Analyzer ---
                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(ctx.mainExecutor, ZxingAnalyzer(onBarcodeDetected))
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analyzer
                    )

                    // --- Autofocus en continu ---
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(0.5f, 0.5f) // centre
                    val action = FocusMeteringAction.Builder(point).build()
                    camera.cameraControl.startFocusAndMetering(action)

                    // --- Tap to focus ---
                    previewView.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            val tapPoint = factory.createPoint(event.x, event.y)
                            val focusAction = FocusMeteringAction.Builder(tapPoint).build()
                            camera.cameraControl.startFocusAndMetering(focusAction)
                        }
                        true
                    }
                } catch (exc: Exception) {
                    Log.e("ZXING_SCANNER", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
