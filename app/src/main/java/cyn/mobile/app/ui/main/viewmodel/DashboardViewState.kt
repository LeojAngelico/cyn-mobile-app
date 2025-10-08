package cyn.mobile.app.ui.main.viewmodel

import androidx.annotation.Keep
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.dashboard.response.DashboardTotalData
import cyn.mobile.app.data.repositories.dashboard.response.LatestTransactionData
import cyn.mobile.app.utils.PopupErrorState

sealed class DashboardViewState {
    @Keep
    object Loading : DashboardViewState()

    // Success payloads
    @Keep
    data class DashboardLoaded(
        val data: DashboardTotalData?,
        val message: String? = null
    ) : DashboardViewState()

    @Keep
    data class LatestTransactionLoaded(
        val data: LatestTransactionData?,
        val message: String? = null
    ) : DashboardViewState()

    // Error states
    @Keep
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") : DashboardViewState()

    @Keep
    data class InputError(val errorData: ErrorsData? = null) : DashboardViewState()
}
