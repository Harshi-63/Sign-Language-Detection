@file:Suppress("DEPRECATION")

package com.example.signlanguagedetection_app.backend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


// Global variables for TensorFlow Lite Interpreter and Camera Executor.
var interpreter: Interpreter? = null
val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

// Constants for motion detection and frame processing optimization.
const val ANALYSIS_FRAME_INTERVAL = 1 // Process every frame for faster translation.
const val MOTION_THRESHOLD = 150 // Reduced threshold for better motion detection sensitivity.
const val NUM_CLASSES = 26 // Number of output classes (A-Z).

// Variables to store previous frame and frame counter for motion detection logic.
private var previousBitmap: Bitmap? = null

// Initialize TensorFlow Lite Interpreter with the model file from the assets folder.
fun initializeInterpreter(context: Context) {
    try {
        interpreter = Interpreter(loadModelFile(context, "sign_language_model.tflite"))
    } catch (e: Exception) {
        Log.e("CameraBackend", "Error initializing TFLite interpreter: ${e.message}")
    }
}
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
// Global variable to store the last detected letter
private var lastDetectedLetter: String? = null

fun processFrame(
    imageProxy: ImageProxy,
    context: Context,
    onClassification: (String) -> Unit
) {
    try {
        // Convert ImageProxy to Bitmap and rotate based on camera orientation
        val currentBitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            ?: run {
                Log.e("ProcessFrame", "Failed to convert ImageProxy to Bitmap")
                return@processFrame
            }

        // Detect motion between frames and skip processing if no significant motion detected
        previousBitmap?.let { prev ->
            val diffCount = detectMotion(prev, currentBitmap)
            Log.d("MotionDetection", "Pixel difference count: $diffCount")

            if (diffCount < MOTION_THRESHOLD) {
                imageProxy.close()
                return // Skip processing if no significant motion is detected.
            }
        }

        previousBitmap = currentBitmap

        // Apply preprocessing pipeline before running inference
        val processedBitmap = preprocessPipeline(currentBitmap, context)

        // Prepare input buffer for TensorFlow Lite model inference
        val inputBuffer = preprocessImage(processedBitmap)
        val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

        interpreter?.run(inputBuffer, outputBuffer)

        // Get prediction from model output
        val predictedLabel = getLabel(outputBuffer[0])

        // Check if the detected letter is different from the last detected letter
        if (predictedLabel != lastDetectedLetter) {
            lastDetectedLetter = predictedLabel // Update last detected letter
            onClassification(predictedLabel)   // Append only new letters to the translation box
        } else {
            Log.d("ProcessFrame", "Repeated letter detected: $predictedLabel. Skipping.")
        }

    } catch (e: Exception) {
        Log.e("ProcessFrame", "Error processing frame: ${e.localizedMessage}")
    } finally {
        imageProxy.close()
    }
}


fun applyGaussianBlur(context: Context, bitmap: Bitmap): Bitmap {
    val BITMAP_SCALE = 0.6f // Scale factor for performance optimization
    val width = Math.round(bitmap.width * BITMAP_SCALE)
    val height = Math.round(bitmap.height * BITMAP_SCALE)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

    val outputBitmap = Bitmap.createBitmap(scaledBitmap)

    val rs = RenderScript.create(context)
    val intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

    val inputAllocation = Allocation.createFromBitmap(rs, scaledBitmap)
    val outputAllocation = Allocation.createFromBitmap(rs, outputBitmap)

    intrinsicBlur.setRadius(15f) // Blur radius (between 0 and 25)
    intrinsicBlur.setInput(inputAllocation)
    intrinsicBlur.forEach(outputAllocation)

    outputAllocation.copyTo(outputBitmap)
    rs.destroy()

    return outputBitmap
}
// Apply binary thresholding to convert grayscale image to binary.
fun applyThresholding(bitmap: Bitmap): Bitmap {
    val binaryBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)
            val grayValue = Color.red(pixel) // Since it's grayscale, red channel represents brightness.

            // Apply thresholding (threshold value can be adjusted based on testing).
            val thresholdValue = 128
            if (grayValue > thresholdValue) {
                binaryBitmap.setPixel(x, y, Color.WHITE)
            } else {
                binaryBitmap.setPixel(x, y, Color.BLACK)
            }
        }
    }
    return binaryBitmap
}
// Preprocessing pipeline for image processing tasks.
fun preprocessPipeline(bitmap: Bitmap, context: Context): Bitmap {
    // Step 1: Convert to Grayscale
    val grayscaleBitmap = convertToGrayscale(bitmap)

    // Step 2: Apply Gaussian Blur using RenderScript
    val blurredBitmap = applyGaussianBlur(context, grayscaleBitmap)

    // Step 3: Apply Binary Thresholding (convert to binary image)
    val binaryBitmap = applyThresholding(blurredBitmap)

    // Step 4: Crop to Region of Interest (ROI)
    return cropToROI(binaryBitmap)
}
// Crop the binary image to Region of Interest (ROI).
fun cropToROI(bitmap: Bitmap): Bitmap {
    // Step 1: Detect edges using Sobel or Canny edge detection
    val edgeBitmap = detectEdges(bitmap)

    // Step 2: Find contours to isolate the hand region
    val contours = findContours(edgeBitmap)

    // Step 3: Identify the largest contour (assuming it's the hand)
    val handContour = contours.maxByOrNull { it.area } ?: return bitmap

    // Step 4: Get bounding rectangle around the largest contour
    val roiRect = handContour.boundingBox

    // Step 5: Crop the image to the bounding rectangle (ROI)
    return cropBitmap(bitmap, roiRect)
}

// Detect edges using Sobel or Canny edge detection
private fun detectEdges(bitmap: Bitmap): Bitmap {
    val grayscaleBitmap = convertToGrayscale(bitmap)

    // Apply Sobel or Canny edge detection (placeholder logic for now)
    val edgeBitmap = Bitmap.createBitmap(grayscaleBitmap.width, grayscaleBitmap.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until grayscaleBitmap.width) {
        for (y in 0 until grayscaleBitmap.height) {
            val pixel = grayscaleBitmap.getPixel(x, y)
            val intensity = Color.red(pixel) // Grayscale intensity
            edgeBitmap.setPixel(x, y, if (intensity > 128) Color.WHITE else Color.BLACK) // Thresholding for edges
        }
    }
    return edgeBitmap
}

// Convert image to grayscale
private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
    val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayscaleBitmap)
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return grayscaleBitmap
}

// Find contours in binary image
private fun findContours(bitmap: Bitmap): List<Contour> {
    // Placeholder logic for contour detection. Replace with actual contour-finding algorithm.
    val contours = mutableListOf<Contour>()

    // Example: Add dummy contour for testing purposes
    contours.add(Contour(Rect(50, 50, 200, 200), area = 30000))

    return contours
}

// Crop bitmap to the specified rectangle (ROI)
private fun cropBitmap(bitmap: Bitmap, rect: Rect): Bitmap {
    return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
}

// Contour class to represent detected contours
data class Contour(val boundingBox: Rect, val area: Int)


// Helper function to preprocess the image for TensorFlow Lite model input.
fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val inputSize = 224 // Match your model's expected input size.
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
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
