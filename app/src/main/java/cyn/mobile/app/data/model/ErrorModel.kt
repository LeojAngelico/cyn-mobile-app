package cyn.mobile.app.data.model

import androidx.annotation.Keep

@Keep
data class ErrorModel(
    val msg: String? = null,
    val status: Boolean? = false,
    val status_code: String? = null,
    val error: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val has_requirements: Boolean? = false,
    var errors: ErrorsData? = null
)

@Keep
data class ErrorsData(
    var email: List<String>? = null,
    var password: List<String>? = null,
    var password_confirmation: List<String>? = null,
    var firstname: List<String>? = null,
    var lastname: List<String>? = null,
    var middlename: List<String>? = null,
    var phone_number: List<String>? = null,
    var username: List<String>? = null,
    var contact_number: List<String>? = null,
    var address: List<String>? = null,
    var image: List<String>? = null,
    var desc: List<String>? = null
)
