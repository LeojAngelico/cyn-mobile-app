package cyn.mobile.app.data.repositories.auth.response


import androidx.annotation.Keep
import cyn.mobile.app.data.repositories.base.response.DateBaseResponse

@Keep
data class LoginResponse(
    val `data`: UserData? = null,
    val msg: String? = null,
    val status: Boolean? = null,
    val status_code: String? = null,
    val token: String? = null,
    val token_type: String? = null
)

@Keep
data class UserData(
    val address: String? = null,
    val contact_number: String? = null,
    val date_created: DateBaseResponse? = null,
    val email: String? = null,
    val firstname: String? = null,
    val id: Int? = null,
    val lastname: String? = null,
    val middlename: String? = null,
    val username: String? = null
)