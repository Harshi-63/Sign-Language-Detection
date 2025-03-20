package com.example.signlanguagedetection_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signlanguagedetection_app.components.HeadingTextComponent
import com.example.signlanguagedetection_app.ui.theme.BackgroundBlue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding

@Composable
fun HomeScreen() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .safeDrawingPadding() // Ensures content avoids system bars
            .padding(29.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HeadingTextComponent("Welcome")
        }
    }
}

@Composable
@Preview
fun PreviewHomeScreen() {
    HomeScreen()
}
