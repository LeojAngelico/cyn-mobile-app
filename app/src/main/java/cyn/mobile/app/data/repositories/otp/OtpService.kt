package cyn.mobile.app.data.repositories.otp

import cyn.mobile.app.data.repositories.otp.request.RequestOtpRequest
import cyn.mobile.app.data.repositories.otp.request.VerifyOtpRequest
import cyn.mobile.app.data.repositories.otp.response.RequestOtpResponse
import cyn.mobile.app.data.repositories.otp.response.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OtpService {

    @POST("api/otp/send")
    suspend fun requestOtp(
        @Body body: RequestOtpRequest
    ): Response<RequestOtpResponse>

    @POST("api/otp/verify")
    suspend fun verifyOtp(
        @Body body: VerifyOtpRequest
    ): Response<VerifyOtpResponse>
}
