package cyn.mobile.app.data.repositories.test.response

import androidx.annotation.Keep

@Keep
data class TokenExchangeResponse(
    val success: Boolean? = null,
    val sessionId: String? = null,
    val message: String? = null,
    val data: TokenExchangePayloadData? = null
)

@Keep
data class TokenExchangePayloadData(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val scope: String? = null
)
