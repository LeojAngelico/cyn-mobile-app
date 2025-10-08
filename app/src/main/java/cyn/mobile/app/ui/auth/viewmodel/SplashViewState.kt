package cyn.mobile.app.ui.auth.viewmodel

import cyn.mobile.app.utils.PopupErrorState


sealed class SplashViewState {
    object Idle : SplashViewState()
    object Loading : SplashViewState()
    data class SuccessRefreshToken(val status: Boolean = false) : SplashViewState()
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") :
        SplashViewState()
}