package com.example.signlanguagedetection_app.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var translation by remember { mutableStateOf("Translating...") }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera Preview
                val previewView = remember { androidx.camera.view.PreviewView(context) }
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                ) {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        setupCamera(
                            context,
                            lifecycleOwner,
                            cameraProvider,
                            previewView,
                            lensFacing
                        ) { translatedText ->
                            translation = translatedText
                        }
                    }, ContextCompat.getMainExecutor(context))
                }

                // Back Button
                Button(
                    onClick = { Router.navigateTo(Screen.HomeScreen) },
                    modifier = Modifier
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                        .align(Alignment.TopStart)
                ) {
                    Text("Back", fontSize = 16.sp)
                }

                // Translation Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = translation,
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Camera Switch Button
                FloatingActionButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                        setupCamera(
                            context,
                            lifecycleOwner,
                            cameraProvider,
                            previewView,
                            lensFacing
                        ) { translatedText ->
                            translation = translatedText
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Switch")
                }
            }
        } else {
            PermissionRequestUI(cameraPermissionState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequestUI(permissionState: com.google.accompanist.permissions.PermissionState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera permission required")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text("Grant Permission")
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: androidx.camera.view.PreviewView,
    lensFacing: Int,
    onClassification: (String) -> Unit
) {
    try {
        cameraProvider.unbindAll()

        val preview = Preview.Builder()
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analyzer ->
                analyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processFrame(imageProxy, context, onClassification)
                }
            }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error setting up camera: ${e.localizedMessage}")
    }
}

private fun processFrame(
    imageProxy: ImageProxy,
    context: Context,
    onClassification: (String) -> Unit
) {
    try {
        val bitmap = imageProxy.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees.toFloat())

        bitmap?.let {
            val interpreter = Interpreter(loadModelFile(context, "sign_language_model.tflite"))
            val inputBuffer = preprocessImage(bitmap)
            val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) } // Adjust NUM_CLASSES as per your model

            interpreter.run(inputBuffer, outputBuffer)

            val predictedLabel = getLabel(outputBuffer[0])
            onClassification(predictedLabel)
        }
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error processing frame: ${e.localizedMessage}")
    } finally {
        imageProxy.close()
    }
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun ImageProxy.toBitmap(): Bitmap? {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val inputSize = 224 // Adjust this to match your model's input size
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
    byteBuffer.order(ByteOrder.nativeOrder())

    val intValues = IntArray(inputSize * inputSize)
    scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    var pixel = 0
    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val value = intValues[pixel++]
            byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 127.5f)
        }
    }
    return byteBuffer
}

private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = fileDescriptor.createInputStream()
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

private fun getLabel(output: FloatArray): String {
    val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
    return labels[maxIndex]
}

// Constants
private const val NUM_CLASSES = 26 // Adjust this based on your model's output
