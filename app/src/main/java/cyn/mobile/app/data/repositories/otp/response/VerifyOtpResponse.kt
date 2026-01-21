package cyn.mobile.app.data.repositories.otp.response

import androidx.annotation.Keep

@Keep
data class VerifyOtpResponse(
    val success: Boolean? = null,
    val valid: Boolean? = null,
    val message: String? = null,
    val phoneNumber: String? = null,
    val verified: Boolean? = null,
    val timestamp: String? = null
)
