package cyn.mobile.app.data.repositories.transaction.request

import androidx.annotation.Keep

@Keep
data class TransactionListRequest(
    val per_page: String = "",
    val keyword: String = "",
    val start_date: String = "",
    val end_date: String = ""
)