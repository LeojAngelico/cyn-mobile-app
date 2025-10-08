package cyn.mobile.app.data.repositories.dashboard.response

import androidx.annotation.Keep
import cyn.mobile.app.data.repositories.base.response.DateBaseResponse

@Keep
data class LatestTransactionResponse(
    val msg: String? = null,
    val status: Boolean? = null,
    val status_code: String? = null,
    val data: LatestTransactionData? = null
)

@Keep
data class LatestTransactionData(
    val id: Int? = null,
    val session_id: String? = null,
    val transaction_id: String? = null,
    val phone_number: String? = null,
    val auth_code: String? = null,
    val token_status: String? = null,
    val number_verification: String? = null,
    val final_status: String? = null,
    val transaction_date: DateBaseResponse? = null
)
