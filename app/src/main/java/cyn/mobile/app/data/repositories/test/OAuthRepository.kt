package cyn.mobile.app.data.repositories.test

import cyn.mobile.app.data.repositories.test.response.InitiateOAuthResponse
import cyn.mobile.app.data.repositories.test.response.TokenExchangeResponse
import cyn.mobile.app.data.repositories.test.response.VerifyPhoneResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class OAuthRepository @Inject constructor(
    private val remoteDataSource: OAuthRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun initiateOAuth(
        phoneNumber: String,
        clientIdentifier: String
    ): Flow<InitiateOAuthResponse> {
        return flow {
            emit(remoteDataSource.initiateOAuth(phoneNumber, clientIdentifier))
        }.flowOn(ioDispatcher)
    }

    fun exchangeToken(
        code: String
    ): Flow<TokenExchangeResponse> {
        return flow {
            emit(remoteDataSource.exchangeToken(code))
        }.flowOn(ioDispatcher)
    }

    fun verifyPhone(
        accessToken: String,
        phoneNumber: String
    ): Flow<VerifyPhoneResponse> {
        return flow {
            emit(remoteDataSource.verifyPhone(accessToken, phoneNumber))
        }.flowOn(ioDispatcher)
    }
}
