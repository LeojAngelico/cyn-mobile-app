package cyn.mobile.app.data.repositories.test.request

import androidx.annotation.Keep

@Keep
data class VerifyPhoneRequest(
    val accessToken: String,
    val phoneNumber: String
)
