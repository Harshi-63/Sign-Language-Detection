package com.example.signlanguagedetection_app.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signlanguagedetection_app.R
import com.example.signlanguagedetection_app.components.ButtonComponent
import com.example.signlanguagedetection_app.components.ClickableLoginTextComponent
import com.example.signlanguagedetection_app.components.DividerTextComponent
import com.example.signlanguagedetection_app.components.HeadingTextComponent
import com.example.signlanguagedetection_app.components.MyPwdField
import com.example.signlanguagedetection_app.components.MyTextfield
import com.example.signlanguagedetection_app.components.NormalTextComponent
import com.example.signlanguagedetection_app.components.UnderlinedTextComponent
import com.example.signlanguagedetection_app.data.LoginUIEvents
import com.example.signlanguagedetection_app.data.LoginViewModel
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.example.signlanguagedetection_app.ui.theme.BackgroundBlue

@Composable
fun LoginScreen(loginViewModel: LoginViewModel =viewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlue)
                .padding(29.dp)
        )
        {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlue)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                NormalTextComponent(value = stringResource(R.string.hello))
                HeadingTextComponent(value = stringResource(id = R.string.welcome_back))
                Spacer(modifier = Modifier.height(20.dp))
                MyTextfield(
                    labelValue = stringResource(id = R.string.mail),
                    painterResource = painterResource(id = R.drawable.mail),
                    onTextSelected = {
                        loginViewModel.onEvent(LoginUIEvents.EmailChanges(it))
                    },
                    errorStatus = loginViewModel.loginUIState.value.emailError
                )
                MyPwdField(
                    labelValue = stringResource(id = R.string.pwd),
                    painterResource = painterResource(id = R.drawable.pwd),
                    onTextSelected = {
                        loginViewModel.onEvent(LoginUIEvents.PasswordChanges(it))
                    },
                    errorStatus = loginViewModel.loginUIState.value.passwordError
                )
                Spacer(modifier = Modifier.height(10.dp))
                UnderlinedTextComponent(value = stringResource(R.string.forgot_pwd))
                Spacer(modifier = Modifier.height(40.dp))
                ButtonComponent(
                    value = stringResource(id = R.string.login),
                    onButtonClicked = { loginViewModel.onEvent(LoginUIEvents.LoginButtonClicked)
                                      },
                    isEnabled = loginViewModel.allValidationPassed.value
                )
                Spacer(modifier = Modifier.height(50.dp))
                DividerTextComponent()
                ClickableLoginTextComponent(tryingToLogin = false, onTextSelected = {
                    Router.navigateTo(Screen.SignUpScreen)
                })
            }
        }
        if (loginViewModel.loginInProgress.value){
            CircularProgressIndicator()
        }
        BackHandler {
            Router.navigateTo(Screen.SignUpScreen)
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    LoginScreen()
}