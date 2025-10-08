package cyn.mobile.app.data.repositories.profile.request

import androidx.annotation.Keep

@Keep
data class UpdateProfileRequest(
    val firstname: String,
    val middlename: String,
    val lastname: String,
    val contact_number: String,
    val address: String,
    val email: String,
    val username: String
)