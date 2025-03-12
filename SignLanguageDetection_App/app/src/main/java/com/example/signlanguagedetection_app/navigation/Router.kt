package com.example.signlanguagedetection_app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object SignUpScreen : Screen()
    object TandCScreen : Screen()
    object LoginScreen : Screen()
    object HomeScreen : Screen()
}

object Router {

    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignUpScreen)

    fun navigateTo(destination: Screen) {
        currentScreen.value = destination
    }


}