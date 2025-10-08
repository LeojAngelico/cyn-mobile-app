package cyn.mobile.app.ui.main.viewmodel

import androidx.annotation.Keep
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.utils.PopupErrorState

sealed class ProfileViewState {
    @Keep
    object Loading : ProfileViewState()

    // For result payloads
    @Keep
    data class ProfileLoaded(
        val data: UserData?,
        val message: String? = null
    ) : ProfileViewState()

    @Keep
    data class ProfileUpdated(
        val success: Boolean,
        val message: String? = null
    ) : ProfileViewState()

    // Error states
    @Keep
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") : ProfileViewState()

    @Keep
    data class InputError(val errorData: ErrorsData? = null) : ProfileViewState()
}
