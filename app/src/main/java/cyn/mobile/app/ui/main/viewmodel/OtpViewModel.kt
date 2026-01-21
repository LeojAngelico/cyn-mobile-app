package cyn.mobile.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cyn.mobile.app.data.model.ErrorModel
import cyn.mobile.app.data.repositories.otp.OtpRepository
import cyn.mobile.app.data.repositories.otp.request.RequestOtpRequest
import cyn.mobile.app.data.repositories.otp.request.VerifyOtpRequest
import cyn.mobile.app.utils.PopupErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val otpRepository: OtpRepository
) : ViewModel() {

    private val _state = MutableSharedFlow<OtpViewState>()
    val state: SharedFlow<OtpViewState> = _state.asSharedFlow()

    fun requestOtp(phoneNumber: String) {
        viewModelScope.launch {

            val request = RequestOtpRequest(
                phoneNumber = phoneNumber
            )

            _state.emit(OtpViewState.Loading)
            otpRepository.requestOtp(request)
                .catch { throwable -> onError(throwable) }
                .collect { response ->
                    _state.emit(OtpViewState.Requested(response.message.orEmpty()))
                }
        }
    }

    fun verifyOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            val request = VerifyOtpRequest(
                phoneNumber = phoneNumber,
                otp = otp
            )
            _state.emit(OtpViewState.Loading)
            otpRepository.verifyOtp(request)
                .catch { throwable -> onError(throwable) }
                .collect { response ->
                    _state.emit(OtpViewState.Verified(response.message.orEmpty()))
                }
        }
    }

    private suspend fun onError(exception: Throwable) {
        when (exception) {
            is IOException, is TimeoutException -> {
                _state.emit(OtpViewState.PopupError(PopupErrorState.NetworkError))
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()
                val type = object : TypeToken<ErrorModel>() {}.type
                val errorResponse: ErrorModel? = Gson().fromJson(errorBody?.charStream(), type)
                if (errorResponse?.has_requirements == true) {
                    _state.emit(OtpViewState.InputError(errorResponse.errors))
                } else {
                    _state.emit(
                        OtpViewState.PopupError(
                            PopupErrorState.HttpError,
                            errorResponse?.error_description.orEmpty()
                        )
                    )
                }
            }
            else -> _state.emit(OtpViewState.PopupError(PopupErrorState.UnknownError))
        }
    }

}
