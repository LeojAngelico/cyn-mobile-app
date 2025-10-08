package cyn.mobile.app.data.repositories.auth.request


import androidx.annotation.Keep

@Keep
data class RegisterRequest(
    val address: String?,
    val contact_number: String?,
    val email: String?,
    val firstname: String?,
    val lastname: String?,
    val middlename: String?,
    val password: String?,
    val password_confirmation: String?,
    val username: String?
)