package com.example.signlanguagedetection_app.screens

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
import com.example.signlanguagedetection_app.components.CheckboxComponent
import com.example.signlanguagedetection_app.components.ClickableLoginTextComponent
import com.example.signlanguagedetection_app.components.DividerTextComponent
import com.example.signlanguagedetection_app.components.HeadingTextComponent
import com.example.signlanguagedetection_app.components.MyPwdField
import com.example.signlanguagedetection_app.components.MyTextfield
import com.example.signlanguagedetection_app.components.NormalTextComponent
import com.example.signlanguagedetection_app.data.SignUpViewModel
import com.example.signlanguagedetection_app.data.SignupUIEvents
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.example.signlanguagedetection_app.ui.theme.BackgroundBlue


@Composable
fun SignupScreen(signUpViewModel: SignUpViewModel = viewModel()) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlue)
                .padding(29.dp)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlue)
            )
            {
                Spacer(modifier = Modifier.height(80.dp))
                NormalTextComponent(value = stringResource(id = R.string.hello))
                HeadingTextComponent(value = stringResource(id = R.string.create_account))
                Spacer(modifier = Modifier.height(20.dp))
                MyTextfield(
                    labelValue = stringResource(id = R.string.fName),
                    painterResource = painterResource(id = R.drawable.profiile),
                    onTextSelected = {
                        signUpViewModel.onEvent(SignupUIEvents.FirstNameChanges(it))
                    },
                    errorStatus = signUpViewModel.registrationUIState.value.fNameError
                )
                MyTextfield(
                    labelValue = stringResource(id = R.string.sName),
                    painterResource = painterResource(id = R.drawable.profiile),
                    onTextSelected = {
                        signUpViewModel.onEvent(SignupUIEvents.LastNameChanges(it))
                    },
                    errorStatus = signUpViewModel.registrationUIState.value.lasNameError
                )
                MyTextfield(
                    labelValue = stringResource(id = R.string.mail),
                    painterResource = painterResource(id = R.drawable.mail),
                    onTextSelected = {
                        signUpViewModel.onEvent(SignupUIEvents.EmailChanges(it))
                    },
                    errorStatus = signUpViewModel.registrationUIState.value.emailError
                )
                MyPwdField(
                    labelValue = stringResource(id = R.string.pwd),
                    painterResource = painterResource(id = R.drawable.pwd),
                    onTextSelected = {
                        signUpViewModel.onEvent(SignupUIEvents.PasswordChanges(it))
                    },
                    errorStatus = signUpViewModel.registrationUIState.value.passwordError
                )
                CheckboxComponent(value = stringResource(id = R.string.t_and_c),
                    onTextSelected = {
                        Router.navigateTo(Screen.TandCScreen)
                    },
                    onCheckedChange = {
                        signUpViewModel.onEvent(SignupUIEvents.PrivacyPolicyCheckboxClicked(it))
                    }
                )
                Spacer(modifier = Modifier.height(30.dp))
                ButtonComponent(
                    value = stringResource(id = R.string.register),
                    onButtonClicked = {
                        signUpViewModel.onEvent(SignupUIEvents.RegisterButtonClicked)
                    },
                    isEnabled = signUpViewModel.allValidationPassed.value
                )
                Spacer(modifier = Modifier.height(50.dp))
                DividerTextComponent()


                ClickableLoginTextComponent(tryingToLogin = true, onTextSelected = {
                    Router.navigateTo(Screen.LoginScreen)
                })

            }
        }
       if (signUpViewModel.signupInProgress.value){
           CircularProgressIndicator()
       }
    }

}

@Preview
@Composable
fun DefaultPreviewOfSignUpScreen() {
    SignupScreen()
}