package cyn.mobile.app.data.repositories.otp.request

import androidx.annotation.Keep

@Keep
data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String
)
