package cyn.mobile.app.ui.auth.viewmodel

import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.utils.PopupErrorState


sealed class LoginViewState{
    object Loading : LoginViewState()
    data class Success(val message: String = "") : LoginViewState()
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") : LoginViewState()
    data class InputError(val errorData: ErrorsData? = null) : LoginViewState()
}
