package cyn.mobile.app.ui.main.viewmodel

import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.utils.PopupErrorState

sealed class OAuthViewState {
    object Loading : OAuthViewState()
    data class Success(val message: String = "") : OAuthViewState()

    // For result payloads
    data class Initiated(
        val code: String?,
        val message: String
    ) : OAuthViewState()

    data class TokenExchanged(
        val sessionId: String?,
        val accessToken: String?,
        val refreshToken: String?,
        val tokenType: String?,
        val expiresIn: Long?,
        val scope: String?,
        val message: String?
    ) : OAuthViewState()

    data class Verified(
        val phoneNumber: String?,
        val verified: Boolean?,
        val devicePhoneNumberVerified: Boolean?,
        val timestamp: String?
    ) : OAuthViewState()

    // Error states
    data class PopupError(val errorCode: PopupErrorState, val message: String = "") : OAuthViewState()
    data class InputError(val errorData: ErrorsData? = null) : OAuthViewState()
}