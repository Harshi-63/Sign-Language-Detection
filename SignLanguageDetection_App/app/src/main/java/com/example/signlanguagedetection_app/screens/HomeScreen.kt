package com.example.signlanguagedetection_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.safe_heaven.ui.theme.BackgroundBlue
import com.example.signlanguagedetection_app.components.HeadingTextComponent

@Composable
fun HomeScreen(){
    Surface(
        modifier = Modifier
            .fillMaxSize()
           .background(BackgroundBlue)
            .padding(29.dp)

    ){
        HeadingTextComponent("Welcome")
    }
}

@Composable
@Preview
fun PreviewHomeScreen(){
    HomeScreen()
}