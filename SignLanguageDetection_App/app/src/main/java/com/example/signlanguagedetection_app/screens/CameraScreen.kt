package com.example.signlanguagedetection_app.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.Executors

// TensorFlow Lite Interpreter instance (global)
private var interpreter: Interpreter? = null

// Initialize TensorFlow Lite Interpreter with the model file from the assets folder.
private fun initializeInterpreter(context: Context) {
    try {
        interpreter = Interpreter(loadModelFile(context, "sign_language_model.tflite"))
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error initializing TFLite interpreter: ${e.message}")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var translation by remember { mutableStateOf("Translating...") }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    // Initialize the interpreter
    LaunchedEffect(context) {
        initializeInterpreter(context)
    }

    // Properly dispose of the interpreter to prevent memory leaks
    DisposableEffect(lifecycleOwner) {
        onDispose {
            interpreter?.close()
            interpreter = null
        }
    }

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
                            lensFacing,
                            onClassification = { translatedText ->
                                translation = translatedText
                            }
                        )
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


@Composable
private fun CameraView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    lensFacing: Int,
    onTranslationUpdate: (String) -> Unit,
    onLensFacingChange: (Int) -> Unit
) {
    var currentTranslation by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        val previewView = remember { PreviewView(context) }
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
                    lensFacing,
                    onClassification = { newTranslation ->
                        currentTranslation = newTranslation // Update translation state.
                        onTranslationUpdate(newTranslation)
                    }
                )
            }, ContextCompat.getMainExecutor(context))
        }

        CameraControls(
            onBackClick = { Router.navigateTo(Screen.HomeScreen) },
            onSwitchCamera = {
                val newLensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                onLensFacingChange(newLensFacing)
            },
            translation = currentTranslation // Pass updated translation state.
        )
    }
}

@Composable
private fun CameraControls(
    onBackClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    translation: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues())
                .align(Alignment.TopStart)
        ) {
            Text("Back", fontSize = 16.sp)
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp).align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = translation, // Display the updated translation text.
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }

        FloatingActionButton(
            onClick = onSwitchCamera,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Text("Switch")
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
    previewView: PreviewView,
    lensFacing: Int,
    onClassification: (String) -> Unit
) {
    try {
        cameraProvider.unbindAll()

        val previewUseCase =
            Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val imageAnalyzerUseCase =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetResolution(Size(640, 480)) // ✅ Lower resolution for better performance
                .build()
        imageAnalyzerUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
            if (isMotionDetected(imageProxy)) {  // Only process if motion is detected
                processFrame(imageProxy, context, onClassification)
            }
        }



        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            previewUseCase,
            imageAnalyzerUseCase
        )
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error setting up camera: ${e.localizedMessage}")
    }
}
private var previousBitmap: Bitmap? =null
private fun processFrame(
    imageProxy: ImageProxy,
    context: Context,
    onClassification: (String) -> Unit
) {
    try {
        val bitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        if (bitmap == null) return

        // Compare current frame with previous frame
        previousBitmap?.let { prev ->
            val motionScore = detectMotion(prev, bitmap)
            Log.d("MotionDetection", "Motion score: $motionScore")

            if (motionScore > 1000) { // Adjust threshold
                Log.d("MotionDetection", "Motion detected!")
                onClassification("Motion Detected!")
            }
        }

        val inputBuffer = preprocessImage(bitmap)
        val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter?.run(inputBuffer, outputBuffer)

        // Log raw output probabilities for debugging
        Log.d("ProcessFrame", "Raw model output: ${outputBuffer[0].contentToString()}")

        val predictedLabel = getLabel(outputBuffer[0])
        Log.d("ProcessFrame", "Predicted label: $predictedLabel")
        onClassification(predictedLabel)

    } catch (e: Exception) {
        Log.e("CameraScreen", "Error processing frame: ${e.localizedMessage}")
    } finally {
        imageProxy.close()
    }
}
// Motion detection using pixel difference
private fun detectMotion(prev: Bitmap, curr: Bitmap): Int {
    var diffCount = 0
    for (x in 0 until curr.width step 10) {
        for (y in 0 until curr.height step 10) {
            if (curr.getPixel(x, y) != prev.getPixel(x, y)) {
                diffCount++
            }
        }
    }
    return diffCount
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

    val nv21BytesArray =
        ByteArray(ySize + uSize + vSize).apply {
            yBuffer.get(this, 0, ySize)
            vBuffer.get(this, ySize, vSize)
            uBuffer.get(this, ySize + vSize, uSize)
        }

    return YuvImage(nv21BytesArray, ImageFormat.NV21, width, height, null).run {
        val outStreamDataHolderStreamBytesArrayDataOutputStream =
            ByteArrayOutputStream().apply { compressToJpeg(Rect(0, 0, width, height), 100, this) }

        BitmapFactory.decodeByteArray(outStreamDataHolderStreamBytesArrayDataOutputStream.toByteArray(), 0, outStreamDataHolderStreamBytesArrayDataOutputStream.size())
    }
}

private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val inputSize = 224 // Adjust this to match your model's input size.
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply { order(ByteOrder.nativeOrder()) }

    val intValues = IntArray(inputSize * inputSize)
    scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    var pixelIndex = 0
    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val value = intValues[pixelIndex++]
            byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 127.5f) // Normalize Red channel
            byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 127.5f)  // Normalize Green channel
            byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 127.5f)        // Normalize Blue channel
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
    val labels = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z"
    )
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
    return labels[maxIndex]
}

private const val NUM_CLASSES = 26 // Adjust this based on your model's output
