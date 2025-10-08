package cyn.mobile.app.data.repositories.base.response


import androidx.annotation.Keep

@Keep
data class GeneralResponse(
    val msg: String?,
    val status: Boolean?,
    val status_code: String?,
    val token: String?,
    val token_type: String?
)