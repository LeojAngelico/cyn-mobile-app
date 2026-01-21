package cyn.mobile.app.data.repositories.otp.request

import androidx.annotation.Keep

@Keep
data class RequestOtpRequest(
    val phoneNumber: String
)