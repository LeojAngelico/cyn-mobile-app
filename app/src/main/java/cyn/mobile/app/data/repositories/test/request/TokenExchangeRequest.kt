package cyn.mobile.app.data.repositories.test.request

import androidx.annotation.Keep

@Keep
data class TokenExchangeRequest(
    val sessionId: String,
    val code: String
)
