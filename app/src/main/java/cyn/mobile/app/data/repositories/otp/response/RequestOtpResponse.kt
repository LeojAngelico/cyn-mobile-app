package cyn.mobile.app.data.repositories.otp.response

import androidx.annotation.Keep

@Keep
data class RequestOtpResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val phoneNumber: String? = null,
    val expiresIn: Int? = null,
    val timestamp: String? = null
)