package cyn.mobile.app.data.repositories.profile.response

import androidx.annotation.Keep
import cyn.mobile.app.data.repositories.auth.response.UserData

@Keep
data class ProfileDetailResponse(
    val status: Boolean? = null,
    val status_code: String? = null,
    val msg: String? = null,
    val data: UserData? = null
)