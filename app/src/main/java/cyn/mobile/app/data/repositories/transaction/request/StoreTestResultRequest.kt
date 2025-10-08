package cyn.mobile.app.data.repositories.transaction.request

import androidx.annotation.Keep

@Keep
data class StoreTestResultRequest(
    val transaction_id: String,
    val session_id: String,
    val phone_number: String,
    val auth_code: Int,
    val token_status: Int,
    val number_verification: Int,
    val final_status: Int
)