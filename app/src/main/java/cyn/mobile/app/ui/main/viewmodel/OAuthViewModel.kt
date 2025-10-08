package cyn.mobile.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cyn.mobile.app.data.model.ErrorModel
import cyn.mobile.app.data.repositories.test.OAuthRepository
import cyn.mobile.app.security.AuthStorage
import cyn.mobile.app.utils.PopupErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val oAuthRepository: OAuthRepository,
    private val authStorage: AuthStorage
) : ViewModel() {

    private val _state = MutableSharedFlow<OAuthViewState>()
    val state: SharedFlow<OAuthViewState> = _state.asSharedFlow()

    fun initiateOAuth(clientIdentifier: String) {
        viewModelScope.launch {
            oAuthRepository.initiateOAuth(
                authStorage.getUserBasicInfo().contact_number.orEmpty(),
                "dpv:FraudPreventionAndDetection number-verification openid",
                clientIdentifier
            )
                .onStart { _state.emit(OAuthViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.success == true) {
                        _state.emit(
                            OAuthViewState.Initiated(
                                sessionId = resp.sessionId.orEmpty(),
                                authUrl = resp.authUrl,
                                state = resp.state,
                                extractedCode = resp.extractedCode,
                                redirectUri = resp.redirectUri,
                                message = resp.message
                            )
                        )
                    } else {
                        _state.emit(OAuthViewState.PopupError(PopupErrorState.HttpError, resp.message.orEmpty()))
                    }
                }
        }
    }

    fun exchangeToken(sessionId: String, code: String) {
        viewModelScope.launch {
            oAuthRepository.exchangeToken(sessionId, code)
                .onStart { _state.emit(OAuthViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.success == true) {
                        val data = resp.data
                        _state.emit(
                            OAuthViewState.TokenExchanged(
                                sessionId = resp.sessionId,
                                accessToken = data?.access_token,
                                refreshToken = data?.refresh_token,
                                tokenType = data?.token_type,
                                expiresIn = data?.expires_in,
                                scope = data?.scope,
                                message = resp.message
                            )
                        )
                    } else {
                        _state.emit(OAuthViewState.PopupError(PopupErrorState.HttpError, resp.message.orEmpty()))
                    }
                }
        }
    }

    fun verifyPhone(accessToken: String) {
        viewModelScope.launch {
            oAuthRepository.verifyPhone(accessToken,
                authStorage.getUserBasicInfo().contact_number.orEmpty())
                .onStart { _state.emit(OAuthViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.success == true) {
                        _state.emit(
                            OAuthViewState.Verified(
                                phoneNumber = resp.phoneNumber,
                                verified = resp.verified,
                                devicePhoneNumberVerified = resp.devicePhoneNumberVerified ?: resp.result?.devicePhoneNumberVerified,
                                timestamp = resp.timestamp
                            )
                        )
                    } else {
                        _state.emit(OAuthViewState.PopupError(PopupErrorState.HttpError))
                    }
                }
        }
    }

    private suspend fun onError(exception: Throwable) {
        when (exception) {
            is IOException, is TimeoutException -> {
                _state.emit(OAuthViewState.PopupError(PopupErrorState.NetworkError))
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()
                val type = object : TypeToken<ErrorModel>() {}.type
                val errorResponse: ErrorModel? = Gson().fromJson(errorBody?.charStream(), type)
                if (errorResponse?.has_requirements == true) {
                    _state.emit(OAuthViewState.InputError(errorResponse.errors))
                } else {
                    _state.emit(
                        OAuthViewState.PopupError(
                            PopupErrorState.HttpError,
                            errorResponse?.message.orEmpty()
                        )
                    )
                }
            }
            else -> _state.emit(OAuthViewState.PopupError(PopupErrorState.UnknownError))
        }
    }
}
