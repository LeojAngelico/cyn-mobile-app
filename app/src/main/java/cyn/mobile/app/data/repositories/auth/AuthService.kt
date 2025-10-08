package cyn.mobile.app.data.repositories.auth

import cyn.mobile.app.data.repositories.auth.request.LoginRequest
import cyn.mobile.app.data.repositories.auth.request.RegisterRequest
import cyn.mobile.app.data.repositories.auth.response.LoginResponse
import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun doLogin(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/refresh-token")
    suspend fun doRefreshToken(): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun doLogout(): Response<GeneralResponse>

    @POST("api/auth/register")
    suspend fun doRegister(@Body request: RegisterRequest): Response<GeneralResponse>

}