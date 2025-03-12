package com.example.signlanguagedetection_app.data
sealed class LoginUIEvents {
    data class EmailChanges(val email: String) : LoginUIEvents()
    data class PasswordChanges(val password: String) : LoginUIEvents()

    object LoginButtonClicked :LoginUIEvents()
}