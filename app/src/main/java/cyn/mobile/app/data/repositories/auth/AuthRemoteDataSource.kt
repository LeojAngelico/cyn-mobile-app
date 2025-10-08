package cyn.mobile.app.data.repositories.auth

import cyn.mobile.app.data.repositories.auth.request.LoginRequest
import cyn.mobile.app.data.repositories.auth.request.RegisterRequest
import cyn.mobile.app.data.repositories.auth.response.LoginResponse
import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(private val authService: AuthService)  {

    suspend fun doLogin(email: String, password: String): LoginResponse {
        val request = LoginRequest(password, email)
        val response = authService.doLogin(request)

        if (response.code() != HttpURLConnection.HTTP_OK) {
            throw HttpException(response)
        }

        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun doRefreshToken(): LoginResponse{
        val response = authService.doRefreshToken()

        if (response.code() != HttpURLConnection.HTTP_OK) {
            throw HttpException(response)
        }

        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun doLogout(): GeneralResponse {
        val response = authService.doLogout()
        if (response.code() != HttpURLConnection.HTTP_OK) {
            throw HttpException(response)
        }
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun doRegister(request: RegisterRequest): GeneralResponse {
        val response = authService.doRegister(request)
        if (response.code() != HttpURLConnection.HTTP_CREATED) {
            throw HttpException(response)
        }
        return response.body() ?: throw NullPointerException("Response data is empty")
    }



}