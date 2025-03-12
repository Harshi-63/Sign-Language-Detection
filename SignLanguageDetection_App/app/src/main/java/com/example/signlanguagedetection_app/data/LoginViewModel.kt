package com.example.signlanguagedetection_app.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.signlanguagedetection_app.data.rules.Validator
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val tag = LoginViewModel::class.simpleName
    var loginUIState = mutableStateOf(LoginUIState())
    var allValidationPassed = mutableStateOf(false)
    var loginInProgress = mutableStateOf(false)

    fun onEvent(event: LoginUIEvents) {
        when (event) {
            is LoginUIEvents.EmailChanges -> {
                loginUIState.value = loginUIState.value.copy(
                    email = event.email
                )
                printState()
            }

            is LoginUIEvents.PasswordChanges -> {
                loginUIState.value = loginUIState.value.copy(
                    password = event.password
                )
                printState()
            }

            is LoginUIEvents.LoginButtonClicked -> {
                login()
            }
        }
        validateLoginDataWithRules()
    }

    private fun login() {
        loginInProgress.value = true
        val email = loginUIState.value.email
        val password = loginUIState.value.password
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                Log.d(tag, "Inside_login_success")
                Log.d(tag, "${it.isSuccessful}")

                if (it.isSuccessful) {
                    loginInProgress.value = false
                    Router.navigateTo(Screen.HomeScreen)
                }

            }
            .addOnFailureListener {
                Log.d(tag, "Inside_login_failure")
                Log.d(tag, it.localizedMessage)

                loginInProgress.value = false
            }
    }

    private fun validateLoginDataWithRules() {
        val emailResult = Validator.validateEmail(
            mail = loginUIState.value.email
        )
        val pwdResult = Validator.validatePassword(
            password = loginUIState.value.password
        )

        Log.d(tag, "Email = $emailResult")
        Log.d(tag, "Password = $pwdResult")

        loginUIState.value = loginUIState.value.copy(
            emailError = emailResult.status,
            passwordError = pwdResult.status,
        )
        allValidationPassed.value =
            emailResult.status && pwdResult.status
    }

    private fun printState() {
        Log.d(tag, "Inside printState")
        Log.d(tag, loginUIState.value.toString())
    }

}