package cyn.mobile.app.data.repositories.otp

import cyn.mobile.app.data.repositories.otp.request.RequestOtpRequest
import cyn.mobile.app.data.repositories.otp.request.VerifyOtpRequest
import cyn.mobile.app.data.repositories.otp.response.RequestOtpResponse
import cyn.mobile.app.data.repositories.otp.response.VerifyOtpResponse
import retrofit2.HttpException
import javax.inject.Inject

class OtpRemoteDataSource @Inject constructor(
    private val service: OtpService
) {

    suspend fun requestOtp(request: RequestOtpRequest): RequestOtpResponse {
        val response = service.requestOtp(request)
        if (response.isSuccessful) {
            return response.body() ?: throw HttpException(response)
        } else {
            throw HttpException(response)
        }
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): VerifyOtpResponse {
        val response = service.verifyOtp(request)
        if (response.isSuccessful) {
            return response.body() ?: throw HttpException(response)
        } else {
            throw HttpException(response)
        }
    }
}
