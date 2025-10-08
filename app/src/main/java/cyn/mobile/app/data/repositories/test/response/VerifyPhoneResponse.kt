package cyn.mobile.app.data.repositories.test.response

import androidx.annotation.Keep


@Keep
data class VerifyPhoneResponse(
    val success: Boolean? = null,
    val phoneNumber: String? = null,
    val verified: Boolean? = null,
    val devicePhoneNumberVerified: Boolean? = null,
    val result: VerifyPhoneResultData? = null,
    val timestamp: String? = null
)

@Keep
data class VerifyPhoneResultData(
    val devicePhoneNumberVerified: Boolean? = null
)
