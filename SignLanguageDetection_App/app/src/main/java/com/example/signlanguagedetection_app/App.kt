package com.example.signlanguagedetection_app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.example.signlanguagedetection_app.screens.HomeScreen
import com.example.signlanguagedetection_app.screens.LoginScreen
import com.example.signlanguagedetection_app.screens.SignupScreen
import com.example.signlanguagedetection_app.screens.TAndCScreen


@Composable
fun SignLanguageDetection(){
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ){
        Crossfade(targetState = Router.currentScreen) { currentState->
            when(currentState.value){
                is Screen.SignUpScreen ->{
                    SignupScreen()
                }
                is Screen.TandCScreen ->{
                    TAndCScreen()
                }
                is Screen.LoginScreen ->{
                    LoginScreen()
                }
                is Screen.HomeScreen ->{
                    HomeScreen()
                }
            }
        }
    }
}