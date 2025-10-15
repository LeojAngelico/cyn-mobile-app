package cyn.mobile.app.data.repositories.test

import cyn.mobile.app.data.repositories.test.request.InitiateOAuthRequest
import cyn.mobile.app.data.repositories.test.request.TokenExchangeRequest
import cyn.mobile.app.data.repositories.test.request.VerifyPhoneRequest
import cyn.mobile.app.data.repositories.test.response.InitiateOAuthResponse
import cyn.mobile.app.data.repositories.test.response.TokenExchangeResponse
import cyn.mobile.app.data.repositories.test.response.VerifyPhoneResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface OAuthService {

    @POST("api/oauth/initiate")
    suspend fun initiateOAuth(@Body request: InitiateOAuthRequest): Response<ResponseBody>

    @GET
    suspend fun followLocation(@Url absoluteUrl: String): Response<ResponseBody>


    @POST("api/oauth/token")
    suspend fun exchangeToken(@Body request: TokenExchangeRequest): Response<TokenExchangeResponse>

    @POST("api/oauth/verify")
    suspend fun verifyPhone(@Body request: VerifyPhoneRequest): Response<VerifyPhoneResponse>
}
