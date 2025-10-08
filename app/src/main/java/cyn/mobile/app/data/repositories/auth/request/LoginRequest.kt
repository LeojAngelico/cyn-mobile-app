package cyn.mobile.app.data.repositories.auth.request


import androidx.annotation.Keep

@Keep
data class LoginRequest(
    val password: String?,
    val username: String?
)