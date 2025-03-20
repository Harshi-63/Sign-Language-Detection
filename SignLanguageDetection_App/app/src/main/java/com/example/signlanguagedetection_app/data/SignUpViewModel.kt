package com.example.signlanguagedetection_app.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


import com.example.signlanguagedetection_app.data.rules.Validator
import com.example.signlanguagedetection_app.navigation.Router
import com.example.signlanguagedetection_app.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel : ViewModel() {
    private val tag = SignUpViewModel::class.simpleName
    var registrationUIState = mutableStateOf(RegistrationUIState())
    var allValidationPassed = mutableStateOf(false)
    var signupInProgress = mutableStateOf(false)

    fun onEvent(event: SignupUIEvents) {
        validateDataWithRules()
        when (event) {
            is SignupUIEvents.FirstNameChanges -> {
                registrationUIState.value = registrationUIState.value.copy(
                    firstName = event.firstName
                )
                printState()
            }

            is SignupUIEvents.LastNameChanges -> {
                registrationUIState.value = registrationUIState.value.copy(
                    lasName = event.lastName
                )
                printState()
            }

            is SignupUIEvents.EmailChanges -> {
                registrationUIState.value = registrationUIState.value.copy(
                    email = event.email
                )
                printState()
            }

            is SignupUIEvents.PasswordChanges -> {
                registrationUIState.value = registrationUIState.value.copy(
                    password = event.password
                )
                printState()
            }

            is SignupUIEvents.RegisterButtonClicked -> {
                signUp()
            }

            is SignupUIEvents.PrivacyPolicyCheckboxClicked -> {
                registrationUIState.value = registrationUIState.value.copy(
                    privacyPolicyAccepted = event.status
                )
            }
        }
    }

    private fun signUp() {
        Log.d(tag, "inside signup func")
        printState()
        createUserInFirebase(
            email = registrationUIState.value.email,
            password = registrationUIState.value.password
        )
    }

    private fun validateDataWithRules() {
        val fNameResult = Validator.validateFirstName(
            fName = registrationUIState.value.firstName
        )

        val sNameResult = Validator.validateLastName(
            sName = registrationUIState.value.lasName
        )
        val emailResult = Validator.validateEmail(
            mail = registrationUIState.value.email
        )
        val pwdResult = Validator.validatePassword(
            password = registrationUIState.value.password
        )
        val privacyPolicyResult = Validator.validatePrivacyPolicyAcceptance(
            statusValue = registrationUIState.value.privacyPolicyAccepted
        )
        Log.d(tag, "FName =$fNameResult")
        Log.d(tag, "LName= $sNameResult")
        Log.d(tag, "Email = $emailResult")
        Log.d(tag, "Password = $pwdResult")
        Log.d(tag, "Privacy Policy=$privacyPolicyResult")

        registrationUIState.value = registrationUIState.value.copy(
            fNameError = fNameResult.status,
            lasNameError = sNameResult.status,
            emailError = emailResult.status,
            passwordError = pwdResult.status,
            privacyPolicyError = privacyPolicyResult.status
        )
        allValidationPassed.value =
            fNameResult.status && sNameResult.status && emailResult.status && pwdResult.status
    }

    private fun printState() {
        Log.d(tag, "Inside printState")
        Log.d(tag, registrationUIState.value.toString())
    }

    private fun createUserInFirebase(email: String, password: String) {
        signupInProgress.value == true
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                Log.d(tag, "Inside_login_success")
                Log.d(tag, "${it.isSuccessful}")
                signupInProgress.value=false

                if (it.isSuccessful) {
                    Router.navigateTo(Screen.HomeScreen)
                }
            }
            .addOnFailureListener {
                Log.d(tag, "Inside_login_failure")
                Log.d(tag, "${it.localizedMessage}")


            }

    }
    fun isUserNew(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            // Compare creationTime and lastSignInTime after parsing them as dates
            val metadata = currentUser.metadata
            val creationTime = metadata?.creationTimestamp
            val lastSignInTime = metadata?.lastSignInTimestamp

            // Check if creation time equals last sign-in time
            creationTime == lastSignInTime
        } else {
            // If no user is signed in, assume it's a new user
            true
        }
    }

}