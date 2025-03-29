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
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


// TensorFlow Lite Interpreter instance (global)
private var interpreter: Interpreter? = null

private fun initializeInterpreter(context: Context) {
    val options = Interpreter.Options().apply {
        setNumThreads(4) // Use multiple threads for inference
        addDelegate(org.tensorflow.lite.nnapi.NnApiDelegate()) // Use NNAPI if available
    }

    try {
        interpreter = Interpreter(loadModelFile(context, "sign_language_model.tflite"), options)
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
                        setupCameraWithMotionDetection(
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
                        setupCameraWithMotionDetection(
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
                setupCameraWithMotionDetection(
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

// Add this at the top level of your file (outside composables)
private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()


private fun setupCameraWithMotionDetection(
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

        val imageAnalyzerUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(android.util.Size(640, 480)) // Lower resolution for faster processing
            .build()


        imageAnalyzerUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
            try {
                if (isMotionDetected(imageProxy)) { // Only process if motion is detected
                    processFrame(imageProxy, context, onClassification)
                }
            } catch (e: Exception) {
                Log.e("ImageAnalyzer", "Error analyzing image: ${e.localizedMessage}")
            } finally {
                imageProxy.close() // Ensure resources are released after processing
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

private var previousBitmap: Bitmap? = null
private const val MOTION_THRESHOLD = 300

// Main motion detection function
private fun isMotionDetected(imageProxy: ImageProxy): Boolean {
    // Convert ImageProxy to Bitmap with rotation handling
    val currentBitmap = imageProxy.toBitmap()?.apply {
        rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
    } ?: run {
        Log.e("MotionDetection", "Bitmap conversion failed")
        imageProxy.close()
        return false
    }

    return previousBitmap?.let { prevBitmap ->
        val diffCount = detectMotion(prevBitmap, currentBitmap)
        previousBitmap = currentBitmap
        diffCount > MOTION_THRESHOLD
    } ?: run {
        previousBitmap = currentBitmap
        true // First frame always triggers motion detection
    }
}

private const val FRAME_INTERVAL = 2 // Process every 2nd frame
private var frameCounter = 0

private fun processFrame(
    imageProxy: ImageProxy,
    context: Context,
    onClassification: (String) -> Unit
) {
    try {
        frameCounter++

        if (frameCounter % FRAME_INTERVAL != 0 || !isMotionDetected(imageProxy)) {
            imageProxy.close()
            return // Skip this frame if no motion is detected or it's not the target interval
        }

        val currentBitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        if (currentBitmap == null) return

        val inputBuffer = preprocessImage(currentBitmap)
        val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter?.run(inputBuffer, outputBuffer)

        val predictedLabel = getLabel(outputBuffer[0])
        onClassification(predictedLabel)

    } catch (e: Exception) {
        Log.e("ProcessFrame", "Error processing frame: ${e.localizedMessage}")
    } finally {
        imageProxy.close()
    }
}


private fun detectMotion(prev: Bitmap, curr: Bitmap): Int {
    val resizedPrev = Bitmap.createScaledBitmap(prev, 64, 64, true)
    val resizedCurr = Bitmap.createScaledBitmap(curr, 64, 64, true)

    var diffCount = 0
    for (x in 0 until resizedCurr.width step 5) { // Step size reduces computation
        for (y in 0 until resizedCurr.height step 5) {
            if (resizedCurr.getPixel(x, y) != resizedPrev.getPixel(x, y)) {
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
    val yBuffer = planes[0].buffer // Y plane
    val uBuffer = planes[1].buffer // U plane
    val vBuffer = planes[2].buffer // V plane

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
    val inputSize = 224 // Match your model's expected input size
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply { order(ByteOrder.nativeOrder()) }

    val intValues = IntArray(inputSize * inputSize)
    scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    var pixelIndex = 0
    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val value = intValues[pixelIndex++]
            byteBuffer.putFloat((value shr 16 and 0xFF) / 255.0f) // Normalize Red channel
            byteBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)  // Normalize Green channel
            byteBuffer.putFloat((value and 0xFF) / 255.0f)        // Normalize Blue channel
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
