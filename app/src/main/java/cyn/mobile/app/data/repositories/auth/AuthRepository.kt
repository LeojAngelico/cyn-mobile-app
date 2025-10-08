package cyn.mobile.app.data.repositories.auth

import cyn.mobile.app.data.repositories.auth.request.RegisterRequest
import cyn.mobile.app.data.repositories.auth.response.LoginResponse
import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.security.AuthStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.text.orEmpty

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authStorage: AuthStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun doLogin(email : String, password : String) : Flow<LoginResponse> {
        return flow{
            val response = authRemoteDataSource.doLogin(email, password)
            val userInfo = response.data?: UserData()
            authStorage.setAccessToken(response.token.orEmpty())
            authStorage.setUserBasicInfo(userInfo)
            val token = response.token.orEmpty()

            emit(response)
        }.flowOn(ioDispatcher)
    }

    fun doRefreshToken() : Flow<LoginResponse> {
        return flow{
            val response = authRemoteDataSource.doRefreshToken()
            val token = response.token.orEmpty()
            authStorage.setAccessToken(token)
            emit(response)
        }.flowOn(ioDispatcher)
    }

    fun doLogout(): Flow<GeneralResponse> {
        return flow {
            val response = authRemoteDataSource.doLogout()
            authStorage.clearAll()
            emit(response)
        }.flowOn(ioDispatcher)
    }

    fun doRegister(request: RegisterRequest): Flow<GeneralResponse> {
        return flow {
            val response = authRemoteDataSource.doRegister(request)
            emit(response)
        }.flowOn(ioDispatcher)
    }

}