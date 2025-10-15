package cyn.mobile.app.data.repositories.test.request

import androidx.annotation.Keep

@Keep
data class InitiateOAuthRequest(
    val phoneNumber: String,
    val client_identifier: String
)