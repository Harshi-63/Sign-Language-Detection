package com.example.signlanguagedetection_app.data

sealed class SignupUIEvents {
    data class FirstNameChanges(val firstName: String) : SignupUIEvents()
    data class LastNameChanges(val lastName: String) : SignupUIEvents()
    data class EmailChanges(val email: String) : SignupUIEvents()
    data class PasswordChanges(val password: String) : SignupUIEvents()

    data class PrivacyPolicyCheckboxClicked(val status:Boolean):SignupUIEvents()

    object RegisterButtonClicked :SignupUIEvents()
}