package cyn.mobile.app.data.repositories.test.response

import androidx.annotation.Keep

@Keep
data class InitiateOAuthResponse(
    val success: Boolean? = null,
    val sessionId: String? = null,
    val authUrl: String? = null,
    val state: String? = null,
    val extractedCode: String? = null,
    val redirectUri: String? = null,
    val message: String? = null
)