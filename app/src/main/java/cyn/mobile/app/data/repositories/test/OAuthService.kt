package cyn.mobile.app.data.repositories.test

import cyn.mobile.app.data.repositories.test.request.InitiateOAuthRequest
import cyn.mobile.app.data.repositories.test.request.TokenExchangeRequest
import cyn.mobile.app.data.repositories.test.request.VerifyPhoneRequest
import cyn.mobile.app.data.repositories.test.response.InitiateOAuthResponse
import cyn.mobile.app.data.repositories.test.response.TokenExchangeResponse
import cyn.mobile.app.data.repositories.test.response.VerifyPhoneResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OAuthService {

    @POST("api/oauth/initiate")
    suspend fun initiateOAuth(@Body request: InitiateOAuthRequest): Response<InitiateOAuthResponse>

    @POST("api/oauth/token")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): Response<TokenExchangeResponse>

    @POST("api/oauth/verify")
    suspend fun verifyPhone(@Body request: VerifyPhoneRequest): Response<VerifyPhoneResponse>
}
