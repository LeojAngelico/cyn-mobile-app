package cyn.mobile.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cyn.mobile.app.data.model.ErrorModel
import cyn.mobile.app.data.repositories.transaction.TransactionRepository
import cyn.mobile.app.data.repositories.transaction.request.StoreTestResultRequest
import cyn.mobile.app.data.repositories.transaction.request.TransactionListRequest
import cyn.mobile.app.utils.PopupErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableSharedFlow<TransactionViewState>()
    val state: SharedFlow<TransactionViewState> = _state.asSharedFlow()

    fun doStoreTestResult(request: StoreTestResultRequest) {
        viewModelScope.launch {
            transactionRepository.doStoreTestResult(request)
                .onStart { _state.emit(TransactionViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(TransactionViewState.TestResultStored(success = true, message = resp.msg))
                    } else {
                        _state.emit(TransactionViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    fun getTransactionList(
        perPage: String = "",
        keyword: String = "",
        startDate: String = "",
        endDate: String = ""
    ) {
        viewModelScope.launch {
            val request = TransactionListRequest(
                per_page = perPage,
                keyword = keyword,
                start_date = startDate,
                end_date = endDate
            )
            transactionRepository.getTransactionList(request)
                .onStart { _state.emit(TransactionViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(
                            TransactionViewState.TransactionListLoaded(
                                items = resp.data,
                                total = resp.total,
                                totalPage = resp.total_page,
                                hasMorePage = resp.has_morepage,
                                message = resp.msg
                            )
                        )
                    } else {
                        _state.emit(TransactionViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    private suspend fun onError(exception: Throwable) {
        when (exception) {
            is IOException, is TimeoutException -> {
                _state.emit(TransactionViewState.PopupError(PopupErrorState.NetworkError))
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()
                val type = object : TypeToken<ErrorModel>() {}.type
                val errorResponse: ErrorModel? = Gson().fromJson(errorBody?.charStream(), type)
                if (errorResponse?.has_requirements == true) {
                    _state.emit(TransactionViewState.InputError(errorResponse.errors))
                } else {
                    _state.emit(
                        TransactionViewState.PopupError(
                            PopupErrorState.HttpError,
                            errorResponse?.msg.orEmpty()
                        )
                    )
                }
            }
            else -> _state.emit(TransactionViewState.PopupError(PopupErrorState.UnknownError))
        }
    }
}
