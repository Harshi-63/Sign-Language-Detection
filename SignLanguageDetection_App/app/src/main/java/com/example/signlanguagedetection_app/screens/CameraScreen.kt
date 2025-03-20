package com.example.signlanguagedetection_app.screens

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.signlanguagedetection_app.ui.theme.BackgroundBlue
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition.getClient

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current // Ensure proper lifecycle binding

    var translation by remember { mutableStateOf("Translation will appear here...") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlue // Background color matching theme
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heading Section
            Text(
                text = "Convert Video to Sign",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Feed Box (Using PreviewView)
            AndroidView(
                factory = { ctx ->
                    android.widget.FrameLayout(ctx).apply {
                        val previewView = androidx.camera.view.PreviewView(ctx)
                        addView(previewView)

                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            bindCameraUseCases(
                                ctx,
                                lifecycleOwner,
                                cameraProvider,
                                previewView,
                                onTextDetected = { detectedText ->
                                    translation = detectedText // Update translation state with detected text
                                }
                            )
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Gray) // Placeholder background for camera feed
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Translation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = translation,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
fun bindCameraUseCases(
    context: android.content.Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: androidx.camera.view.PreviewView,
    onTextDetected: (String) -> Unit
) {
    try {
        val previewUseCase =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val imageAnalyzerUseCase =
            ImageAnalysis.Builder().build().also { analysisUseCase ->
                analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processImage(imageProxy, onTextDetected)
                }
            }

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, // Pass correct LifecycleOwner here
            cameraSelector,
            previewUseCase,
            imageAnalyzerUseCase
        )
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error binding use cases", e)
    }
}

@ExperimentalGetImage
fun processImage(imageProxy: ImageProxy, onTextDetected: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close() // Close the proxy to avoid memory leaks
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
        com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
    )


    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            onTextDetected(visionText.text) // Pass detected text to callback function
        }
        .addOnFailureListener { e ->
            Log.e("CameraScreen", "Text recognition failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close() // Close image proxy after processing to avoid memory leaks.
        }
}

private fun Unit.getClient() {
    TODO("Not yet implemented")
}

