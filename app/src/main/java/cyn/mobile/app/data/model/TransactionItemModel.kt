package cyn.mobile.app.data.model

import androidx.annotation.Keep

@Keep
data class TransactionItem(
    val transactionId: String,
    val sessionId: String,
    val dateTime: String,
    val phone: String,
    val authCode: String,
    val generateToken: String,
    val numberVerification: String,
    val status: String
)
