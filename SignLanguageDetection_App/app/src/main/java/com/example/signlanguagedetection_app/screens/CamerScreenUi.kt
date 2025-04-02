package com.example.signlanguagedetection_app.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.signlanguagedetection_app.backend.cameraExecutor
import com.example.signlanguagedetection_app.backend.initializeInterpreter
import com.example.signlanguagedetection_app.backend.processFrame
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var translation by remember { mutableStateOf("") } // Complete translation text.
    var currentLetter by remember { mutableStateOf("") } // Currently detected letter.
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) } // Lens direction.

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    LaunchedEffect(context) {
        initializeInterpreter(context)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            com.example.signlanguagedetection_app.backend.interpreter?.close()
            com.example.signlanguagedetection_app.backend.interpreter = null
            cameraExecutor.shutdown()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
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
                            onClassification = { detectedLetter ->
                                if (detectedLetter.isNotEmpty() && detectedLetter != currentLetter) {
                                    currentLetter = detectedLetter // Update current letter dynamically.
                                }
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

                // Small Rectangle in the Middle of the Screen for Current Letter Display.
                Box(
                    modifier = Modifier.size(width = 120.dp, height = 60.dp).align(Alignment.Center)
                        .background(Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)), // Rounded corners and translucent white background.
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (currentLetter.isEmpty()) "Detecting..." else currentLetter, // Show placeholder or detected letter.
                        fontSize = 24.sp,
                        color = Color.Black, // Black text for visibility.
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Translation Display Box
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = translation, // Full translation text.
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Add Button
                Button(
                    onClick = {
                        if (currentLetter.isNotEmpty() && !translation.endsWith(currentLetter)) {
                            translation += currentLetter // Append the current letter to translation text.
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                ) {
                    Text("Add")
                }

                // Space Button
                Button(
                    onClick = { translation += " " }, // Add a space.
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text("Space")
                }

                // Delete Button
                Button(
                    onClick = {
                        if (translation.isNotEmpty()) {
                            translation = translation.dropLast(1) // Delete last character.
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Text("Delete")
                }

                // Camera Switch Button
                FloatingActionButton(
                    onClick = {
                        lensFacing =
                            if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

                        val cameraProvider =
                            ProcessCameraProvider.getInstance(context).get()
                        setupCamera(
                            context,
                            lifecycleOwner,
                            cameraProvider,
                            previewView,
                            lensFacing,
                            onClassification = { detectedLetter ->
                                if (detectedLetter.isNotEmpty() && detectedLetter != currentLetter) {
                                    currentLetter = detectedLetter // Update current letter dynamically.
                                }
                            }
                        )
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(80.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Switch Camera")
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Camera permission required")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text("Grant Permission")
        }
    }
}

// Setup CameraX with ImageAnalysis and Preview UseCases.
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
            androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val imageAnalyzerUseCase =
            androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_4_3)
                .build()

        imageAnalyzerUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
            processFrame(imageProxy, context, onClassification)
        }

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, previewUseCase, imageAnalyzerUseCase,
        )
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error setting up camera: ${e.message}")
    }
}
