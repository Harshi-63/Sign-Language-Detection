package com.example.signlanguagedetection_app.screens

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signlanguagedetection_app.components.CameraPreview
import com.example.signlanguagedetection_app.data.CameraViewModel
import com.example.signlanguagedetection_app.utils.PermissionsUtils

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val permissionGranted = remember { PermissionsUtils.checkPermission(context, Manifest.permission.CAMERA) }

    var translation by remember { mutableStateOf("Translating...") }

    if (permissionGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            val previewView = remember { PreviewView(context) }
            CameraPreview(context, previewView)

            viewModel.setupCamera(context, previewView) { detectedGesture ->
                translation = detectedGesture
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = translation, color = Color.Black, modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        Text("Camera permission required. Please enable it.")
    }
}
