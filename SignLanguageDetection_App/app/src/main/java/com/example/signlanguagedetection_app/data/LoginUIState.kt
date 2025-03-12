package com.example.signlanguagedetection_app.data

import java.lang.Error

data class LoginUIState(
    var email: String = "",
    var password: String = "",
    var privacyPolicyAccepted: Boolean = false,

    var emailError: Boolean = false,
    var passwordError: Boolean = false,
)