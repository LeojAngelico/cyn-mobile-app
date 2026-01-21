package cyn.mobile.app.ui.main.viewmodel

import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.otp.response.RequestOtpResponse
import cyn.mobile.app.data.repositories.otp.response.VerifyOtpResponse
import cyn.mobile.app.utils.PopupErrorState

sealed class OtpViewState {
    object Loading : OtpViewState()
    data class Success(val message: String = "") : OtpViewState()

    // Payload states
    data class Requested(val message: String = "") : OtpViewState()
    data class Verified(val message: String = "") : OtpViewState()

    // Error states
    data class PopupError(val errorCode: PopupErrorState, val message: String = "Something went wrong") : OtpViewState()
    data class InputError(val errorData: ErrorsData? = null) : OtpViewState()
}
