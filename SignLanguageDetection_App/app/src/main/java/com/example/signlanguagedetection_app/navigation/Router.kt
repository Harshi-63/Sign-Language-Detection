package com.example.signlanguagedetection_app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object LandingPage : Screen() // Add LandingPage as the initial screen
    object SignUpScreen : Screen()
    object TandCScreen : Screen()
    object LoginScreen : Screen()
    object HomeScreen : Screen()
    object CameraScreen : Screen()
}

object Router {
    // Set LandingPage as the initial screen
    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.LandingPage)

    fun navigateTo(destination: Screen) {
        currentScreen.value = destination
    }
}
