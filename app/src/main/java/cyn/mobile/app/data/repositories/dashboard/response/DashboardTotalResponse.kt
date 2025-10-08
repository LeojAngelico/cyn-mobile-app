package cyn.mobile.app.data.repositories.dashboard.response

import androidx.annotation.Keep

@Keep
data class DashboardTotalResponse(
    val msg: String? = null,
    val status: Boolean? = null,
    val status_code: String? = null,
    val data: DashboardTotalData? = null
)

@Keep
data class DashboardTotalData(
    val sucess_transactions: String? = null, // keeping server spelling as-is
    val failed_transactions: String? = null
)
