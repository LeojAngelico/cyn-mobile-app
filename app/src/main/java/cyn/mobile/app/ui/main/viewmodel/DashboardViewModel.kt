package cyn.mobile.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cyn.mobile.app.data.model.ErrorModel
import cyn.mobile.app.data.repositories.dashboard.DashboardRepository
import cyn.mobile.app.utils.PopupErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableSharedFlow<DashboardViewState>()
    val state: SharedFlow<DashboardViewState> = _state.asSharedFlow()

    fun getDashboardInfo() {
        viewModelScope.launch {
            dashboardRepository.getDashboardInfo()
                .onStart { _state.emit(DashboardViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(
                            DashboardViewState.DashboardLoaded(
                                data = resp.data,
                                message = resp.msg
                            )
                        )
                    } else {
                        _state.emit(DashboardViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    fun getLatestTransaction() {
        viewModelScope.launch {
            dashboardRepository.getLatestTransaction()
                .onStart { _state.emit(DashboardViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(
                            DashboardViewState.LatestTransactionLoaded(
                                data = resp.data,
                                message = resp.msg
                            )
                        )
                    } else {
                        _state.emit(DashboardViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    private suspend fun onError(exception: Throwable) {
        when (exception) {
            is IOException, is TimeoutException -> {
                _state.emit(DashboardViewState.PopupError(PopupErrorState.NetworkError))
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()
                val type = object : TypeToken<ErrorModel>() {}.type
                val errorResponse: ErrorModel? = Gson().fromJson(errorBody?.charStream(), type)
                if (errorResponse?.has_requirements == true) {
                    _state.emit(DashboardViewState.InputError(errorResponse.errors))
                } else {
                    _state.emit(
                        DashboardViewState.PopupError(
                            PopupErrorState.HttpError,
                            errorResponse?.msg.orEmpty()
                        )
                    )
                }
            }
            else -> _state.emit(DashboardViewState.PopupError(PopupErrorState.UnknownError))
        }
    }
}
