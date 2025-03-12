package com.example.signlanguagedetection_app.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.safe_heaven.navigation.Router
import com.example.safe_heaven.navigation.Screen

import com.example.signlanguagedetection_app.data.rules.Validator
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

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                Log.d(tag, "Inside_login_success")
                Log.d(tag, "${authTask.isSuccessful}")

                if (authTask.isSuccessful) {
                    // ✅ Fetching ID Token after successful login
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.getIdToken(true) // 'true' forces token refresh
                        ?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                Log.d(tag, "ID Token: $idToken")

                                // ➔ You can now send this ID Token to your backend if needed
                            } else {
                                Log.e(tag, "Failed to get ID Token", tokenTask.exception)
                            }

                            loginInProgress.value = false
                            Router.navigateTo(Screen.HomeScreen)
                        }
                } else {
                    loginInProgress.value = false
                }
            }
            .addOnFailureListener { exception ->
                Log.d(tag, "Inside_login_failure")
                Log.d(tag, exception.localizedMessage ?: "Unknown Error")
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