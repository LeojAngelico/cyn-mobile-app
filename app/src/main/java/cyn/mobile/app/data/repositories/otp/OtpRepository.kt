package cyn.mobile.app.data.repositories.otp

import cyn.mobile.app.data.repositories.otp.request.RequestOtpRequest
import cyn.mobile.app.data.repositories.otp.request.VerifyOtpRequest
import cyn.mobile.app.data.repositories.otp.response.RequestOtpResponse
import cyn.mobile.app.data.repositories.otp.response.VerifyOtpResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class OtpRepository @Inject constructor(
    private val remoteDataSource: OtpRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun requestOtp(request: RequestOtpRequest): Flow<RequestOtpResponse> = flow {
        emit(remoteDataSource.requestOtp(request))
    }.flowOn(ioDispatcher)

    fun verifyOtp(request: VerifyOtpRequest): Flow<VerifyOtpResponse> = flow {
        emit(remoteDataSource.verifyOtp(request))
    }.flowOn(ioDispatcher)
}
