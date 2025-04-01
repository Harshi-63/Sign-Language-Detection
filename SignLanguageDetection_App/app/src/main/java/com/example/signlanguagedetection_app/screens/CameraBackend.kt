package com.example.signlanguagedetection_app.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.graphics.scale

// Global variables for TensorFlow Lite Interpreter and Camera Executor
var interpreter: Interpreter? = null
val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

// Constants for motion detection and frame processing optimization
const val ANALYSIS_FRAME_INTERVAL = 2 // Process every 2nd frame for better responsiveness.
const val MOTION_THRESHOLD = 300 // Minimum pixel difference count to detect motion.
const val NUM_CLASSES = 26 // Number of output classes (A-Z).

// Variables to store previous frame and frame counter for motion detection logic
private var previousBitmap: Bitmap? = null
private var frameCounter = 0

// Initialize TensorFlow Lite Interpreter with the model file from the assets folder.
fun initializeInterpreter(context: Context) {
    try {
        interpreter = Interpreter(loadModelFile(context, "sign_language_model.tflite"))
    } catch (e: Exception) {
        Log.e("CameraBackend", "Error initializing TFLite interpreter: ${e.message}")
    }
}


fun isMotionDetected(imageProxy: ImageProxy): Boolean {
    // Convert ImageProxy to Bitmap
    val currentBitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
    if (currentBitmap == null) {
        Log.e("MotionDetection", "Failed to convert ImageProxy to Bitmap")
        return false
    }

    return previousBitmap?.let { prevBitmap ->
        val diffCount = detectMotion(prevBitmap, currentBitmap)
        Log.d("MotionDetection", "Pixel difference count: $diffCount")
        previousBitmap = currentBitmap
        diffCount > MOTION_THRESHOLD // Return true if motion is detected based on the threshold
    } ?: run {
        previousBitmap = currentBitmap
        true // Assume motion detected for the first frame
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}


// Frame processing logic with motion detection and translation updates.
fun processFrame(
    imageProxy: ImageProxy,
    context: Context,
    onClassification: (String) -> Unit
) {
    try {
        frameCounter++

        // Skip frames based on interval or if no motion is detected.
        if (frameCounter % ANALYSIS_FRAME_INTERVAL != 0 || !isMotionDetected(imageProxy)) {
            imageProxy.close()
            return // Skip this frame.
        }

        val currentBitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            ?: run {
                Log.e("ProcessFrame", "Failed to convert ImageProxy to Bitmap")
                return@processFrame
            }

        previousBitmap?.let { prev ->
            val diffCount = detectMotion(prev, currentBitmap)
            if (diffCount < MOTION_THRESHOLD) return@processFrame // Skip processing if no motion detected.
        }

        previousBitmap = currentBitmap

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

// Helper function to preprocess the image for TensorFlow Lite model input.
fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val inputSize: Int = 224 // Match your model's expected input size.
    val scaledBitmap = bitmap.scale(inputSize, inputSize)
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply { order(ByteOrder.nativeOrder()) }

    val pixels = IntArray(inputSize * inputSize)
    scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    for (pixelValue in pixels) {
        byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f) // Normalize Red channel.
        byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)  // Normalize Green channel.
        byteBuffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)        // Normalize Blue channel.
    }

    return byteBuffer
}

// Motion detection logic based on pixel differences between frames.
fun detectMotion(prev: Bitmap, curr: Bitmap): Int {
    if (prev.width != curr.width || prev.height != curr.height) return 0

    var diffCount = 0
    val stepSize = 10 // Check every 10th pixel for performance optimization.

    for (x in 0 until curr.width step stepSize) {
        for (y in 0 until curr.height step stepSize) {
            if (curr.getPixel(x, y) != prev.getPixel(x, y)) {
                diffCount++
            }
        }
    }
    return diffCount
}

// Convert ImageProxy to Bitmap using YUV format conversion.
fun ImageProxy.toBitmap(): Bitmap? {
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

    return try {
        YuvImage(nv21BytesArray, ImageFormat.NV21, width, height, null).run {
            val outputStream = ByteArrayOutputStream().apply { compressToJpeg(Rect(0, 0, width, height), 80, this) }
            BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
        }
    } catch (e: Exception) {
        Log.e("ImageConversion", "Error converting ImageProxy to Bitmap: ${e.message}")
        null
    }
}


// Load TensorFlow Lite model file from assets folder.
fun loadModelFile(context: Context, modelName: String): ByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = fileDescriptor.createInputStream()
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

// Get the label corresponding to the highest probability from model output.
fun getLabel(output: FloatArray): String {
    val labels = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z"
    )
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
    return labels[maxIndex]
}
