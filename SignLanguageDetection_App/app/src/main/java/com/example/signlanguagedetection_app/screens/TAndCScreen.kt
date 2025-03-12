package com.example.signlanguagedetection_app.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signlanguagedetection_app.R
import com.example.signlanguagedetection_app.components.HeadingTextComponent
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen


@Composable
fun TAndCScreen(){
    Surface(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)
        .padding(16.dp)) {
        Spacer(modifier = Modifier.height(50.dp))
        HeadingTextComponent(value = stringResource(id = R.string.terms_of_use))
    }
    BackHandler {
        Router.navigateTo(Screen.SignUpScreen)
    }
}
@Preview
@Composable
fun DefaultPreviewOfTAndCScreen(){
    TAndCScreen()
}