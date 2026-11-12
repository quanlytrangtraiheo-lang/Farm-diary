package com.example.ui.pig

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

/**
 * Hộp thoại quét mã QR thẻ tai heo (Ear Tag QR Scanner) bằng CameraX và thư viện ZXing.
 * Tích hợp nhận diện thời gian thực qua Camera, mô phỏng thẻ tai chuồng nuôi và nhập thủ công.
 */
@Composable
fun PigEarTagScannerDialog(
    onDismiss: () -> Unit,
    onTagIdentified: (String) -> Unit,
    availableSampleTags: List<String> = listOf("NY-089", "NY-045", "Lô A-12", "F1-018", "NY-102")
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualTagInput by remember { mutableStateOf("") }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("pig_qr_scanner_dialog")
        ) {
            // Live Camera Viewfinder khi đã được cấp quyền Camera
            if (hasCameraPermission) {
                CameraPreviewWithQrAnalyzer(
                    onQrCodeScanned = { rawText ->
                        val sanitized = sanitizeEarTagCode(rawText)
                        if (sanitized.isNotBlank()) {
                            onTagIdentified(sanitized)
                        }
                    },
                    onCameraReady = { camera -> cameraInstance = camera }
                )
            } else {
                // Giao diện khi chưa có quyền Camera
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FarmAmber,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Yêu cầu quyền truy cập Camera",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                            Text(
                                text = "Ứng dụng cần sử dụng Camera để quét mã QR in trên thẻ tai heo nhằm tra cứu thông tin nhanh tại chuồng.",
                                fontSize = 14.sp,
                                color = FarmTextSecondary
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cấp quyền Camera")
                            }
                        }
                    }
                }
            }

            // Scanning Overlay: Khung quét hình vuông màu xanh có tia quét laser
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Khung viền ngắm quét QR (Target Viewfinder)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(3.dp, FarmGreenPrimary, RoundedCornerShape(20.dp))
                        .background(Color.Transparent)
                        .testTag("pig_qr_viewfinder"),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Tia quét laser chuyển động lên xuống
                    val infiniteTransition = rememberInfiniteTransition()
                    val laserOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 220f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .offset(y = laserOffset.dp)
                            .background(FarmGreenPrimary)
                    )
                }
            }

            // Top Bar: Tiêu đề, Đèn flash, Nút đóng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "QUÉT MÃ THẺ TAI HEO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bật / Tắt đèn Flash trợ sáng
                    IconButton(
                        onClick = {
                            isTorchEnabled = !isTorchEnabled
                            cameraInstance?.cameraControl?.enableTorch(isTorchEnabled)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Đèn Flash",
                            tint = if (isTorchEnabled) Color.Yellow else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút đóng
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("pig_qr_scanner_close")
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng quét",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Panel: Hướng dẫn quét, Nhập mã thủ công, Thẻ tai mẫu thử nghiệm
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Hướng camera vào mã QR in trên thẻ tai hoặc chọn thẻ nhanh bên dưới:",
                        fontSize = 12.sp,
                        color = FarmTextSecondary
                    )

                    // Phím chọn nhanh thẻ tai mẫu (rất hữu ích khi chạy máy ảo hoặc thẻ bị bẩn)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableSampleTags) { sampleTag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FarmGreenContainer,
                                modifier = Modifier
                                    .testTag("sample_tag_$sampleTag")
                                    .clickable { onTagIdentified(sampleTag) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = sampleTag,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmGreenPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Ô nhập mã số tai thủ công khi camera không đọc được (bùn đất che lấp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualTagInput,
                            onValueChange = { manualTagInput = it },
                            placeholder = { Text("Hoặc gõ mã tai (ví dụ: NY-089)...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_ear_tag_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FarmGreenPrimary,
                                unfocusedBorderColor = FarmOutline
                            )
                        )

                        Button(
                            onClick = {
                                if (manualTagInput.isNotBlank()) {
                                    onTagIdentified(manualTagInput.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("manual_ear_tag_submit")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Tìm kiếm")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quét luồng khung hình từ CameraX và giải mã QR bằng ZXing Reader.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithQrAnalyzer(
    onQrCodeScanned: (String) -> Unit,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastScannedCode by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val reader = MultiFormatReader().apply {
                    val hints = mapOf<DecodeHintType, Any>(
                        DecodeHintType.POSSIBLE_FORMATS to listOf(
                            BarcodeFormat.QR_CODE,
                            BarcodeFormat.CODE_128,
                            BarcodeFormat.DATA_MATRIX
                        ),
                        DecodeHintType.TRY_HARDER to true
                    )
                    setHints(hints)
                }

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxyWithZxing(imageProxy, reader) { detectedText ->
                        if (detectedText != lastScannedCode) {
                            lastScannedCode = detectedText
                            previewView.post {
                                onQrCodeScanned(detectedText)
                            }
                        }
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    onCameraReady(camera)
                } catch (exc: Exception) {
                    Log.e("PigEarTagScanner", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Trích xuất byte plane Y (Luminance) từ ImageProxy và giải mã qua ZXing.
 */
private fun processImageProxyWithZxing(
    imageProxy: ImageProxy,
    reader: MultiFormatReader,
    onSuccess: (String) -> Unit
) {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)

    val width = imageProxy.width
    val height = imageProxy.height

    val source = PlanarYUVLuminanceSource(
        data,
        width,
        height,
        0,
        0,
        width,
        height,
        false
    )

    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

    try {
        val result = reader.decodeWithState(binaryBitmap)
        reader.reset()
        onSuccess(result.text)
    } catch (_: Exception) {
        // NotFoundException là trạng thái bình thường khi khung hình chưa có mã QR
    } finally {
        imageProxy.close()
    }
}

/**
 * Làm sạch chuỗi mã quét được (loại bỏ tiền tố url hoặc json nếu có).
 */
fun sanitizeEarTagCode(rawText: String): String {
    val text = rawText.trim()
    return when {
        text.startsWith("tag:", ignoreCase = true) -> text.substringAfter(":").trim()
        text.startsWith("pig://", ignoreCase = true) -> text.substringAfter("://").trim()
        text.startsWith("#") -> text.removePrefix("#").trim()
        text.contains("tagCode\":\"", ignoreCase = true) -> {
            text.substringAfter("tagCode\":\"").substringBefore("\"").trim()
        }
        else -> text
    }
}
