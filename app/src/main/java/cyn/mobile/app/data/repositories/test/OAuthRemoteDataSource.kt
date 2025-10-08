package cyn.mobile.app.data.repositories.test

import cyn.mobile.app.data.repositories.test.request.InitiateOAuthRequest
import cyn.mobile.app.data.repositories.test.request.TokenExchangeRequest
import cyn.mobile.app.data.repositories.test.request.VerifyPhoneRequest
import cyn.mobile.app.data.repositories.test.response.InitiateOAuthResponse
import cyn.mobile.app.data.repositories.test.response.TokenExchangeResponse
import cyn.mobile.app.data.repositories.test.response.VerifyPhoneResponse
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class OAuthRemoteDataSource @Inject constructor(
    private val oAuthService: OAuthService
) {

    suspend fun initiateOAuth(
        phoneNumber: String,
        scope: String,
        clientIdentifier: String
    ): InitiateOAuthResponse {
        val response = oAuthService.initiateOAuth(
            InitiateOAuthRequest(
                phoneNumber = phoneNumber,
                scope = scope,
                client_identifier = clientIdentifier
            )
        )
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun exchangeToken(
        sessionId: String,
        code: String
    ): TokenExchangeResponse {
        val response = oAuthService.exchangeToken(
            TokenExchangeRequest(sessionId = sessionId, code = code)
        )
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun verifyPhone(
        accessToken: String,
        phoneNumber: String
    ): VerifyPhoneResponse {
        val response = oAuthService.verifyPhone(
            VerifyPhoneRequest(accessToken = accessToken, phoneNumber = phoneNumber)
        )
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }
}
