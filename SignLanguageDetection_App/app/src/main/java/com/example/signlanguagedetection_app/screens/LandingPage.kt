package com.example.signlanguagedetection_app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signlanguagedetection_app.R
import com.example.signlanguagedetection_app.data.SignUpViewModel
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun LandingPage(signUpViewModel: SignUpViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000L)
        isLoading = false

        if (signUpViewModel.isUserNew()) {
            Router.navigateTo(Screen.SignUpScreen)
        } else {
            Router.navigateTo(Screen.LoginScreen)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White // Background color for landing page
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
                )
                Text(
                    text = if (isLoading) "Loading..." else "We hear You",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // Text color for landing page message
                )
            }
        }
    }
}
