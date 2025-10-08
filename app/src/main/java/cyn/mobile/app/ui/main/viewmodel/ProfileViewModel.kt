package cyn.mobile.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cyn.mobile.app.data.model.ErrorModel
import cyn.mobile.app.data.repositories.profile.ProfileRepository
import cyn.mobile.app.data.repositories.profile.request.UpdateProfileRequest
import cyn.mobile.app.utils.PopupErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableSharedFlow<ProfileViewState>()
    val state: SharedFlow<ProfileViewState> = _state.asSharedFlow()

    fun getProfileInfo() {
        viewModelScope.launch {
            profileRepository.getProfileInfo()
                .onStart { _state.emit(ProfileViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(ProfileViewState.ProfileLoaded(data = resp.data, message = resp.msg))
                    } else {
                        _state.emit(ProfileViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    fun doUpdateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            profileRepository.doUpdateProfile(request)
                .onStart { _state.emit(ProfileViewState.Loading) }
                .catch { onError(it) }
                .collect { resp ->
                    if (resp.status == true) {
                        _state.emit(ProfileViewState.ProfileUpdated(success = true, message = resp.msg))
                    } else {
                        _state.emit(ProfileViewState.PopupError(PopupErrorState.HttpError, resp.msg.orEmpty()))
                    }
                }
        }
    }

    private suspend fun onError(exception: Throwable) {
        when (exception) {
            is IOException, is TimeoutException -> {
                _state.emit(ProfileViewState.PopupError(PopupErrorState.NetworkError))
            }
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()
                val type = object : TypeToken<ErrorModel>() {}.type
                val errorResponse: ErrorModel? = Gson().fromJson(errorBody?.charStream(), type)
                if (errorResponse?.has_requirements == true) {
                    _state.emit(ProfileViewState.InputError(errorResponse.errors))
                } else {
                    _state.emit(
                        ProfileViewState.PopupError(
                            PopupErrorState.HttpError,
                            errorResponse?.msg.orEmpty()
                        )
                    )
                }
            }
            else -> _state.emit(ProfileViewState.PopupError(PopupErrorState.UnknownError))
        }
    }
}
