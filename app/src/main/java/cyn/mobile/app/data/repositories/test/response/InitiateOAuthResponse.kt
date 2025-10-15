package cyn.mobile.app.data.repositories.test.response

import androidx.annotation.Keep

@Keep
data class InitiateOAuthResponse(
    val success: Boolean? = false,
    val code: String? = null,
    val message: String? = null
)