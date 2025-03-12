package com.example.signlanguagedetection_app.data

import java.lang.Error

data class RegistrationUIState(
    var firstName: String = "",
    var lasName: String = "",
    var email: String = "",
    var password: String = "",
    var privacyPolicyAccepted: Boolean = false,

    var fNameError: Boolean = false,
    var lasNameError: Boolean = false,
    var emailError: Boolean = false,
    var passwordError: Boolean = false,
    var privacyPolicyError: Boolean = false,
)