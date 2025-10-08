package cyn.mobile.app.ui.main.viewmodel

import androidx.annotation.Keep
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.transaction.response.TransactionListItem
import cyn.mobile.app.utils.PopupErrorState

sealed class TransactionViewState {
    @Keep
    object Loading : TransactionViewState()

    // Success payloads
    @Keep
    data class TestResultStored(
        val success: Boolean,
        val message: String? = null
    ) : TransactionViewState()

    @Keep
    data class TransactionListLoaded(
        val items: List<TransactionListItem>?,
        val total: Int? = null,
        val totalPage: Int? = null,
        val hasMorePage: Boolean? = null,
        val message: String? = null
    ) : TransactionViewState()

    // Error states
    @Keep
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") : TransactionViewState()

    @Keep
    data class InputError(val errorData: ErrorsData? = null) : TransactionViewState()
}
