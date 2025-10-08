package cyn.mobile.app.security

import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.base.response.DateBaseResponse

interface AuthStorage {
    suspend fun setAccessToken(token: String)
    suspend fun getAccessToken(): String
    suspend fun isLoggedIn(): Boolean
    suspend fun resetToken()

    suspend fun setUserBasicInfo(user: UserData)
    suspend fun getUserBasicInfo(): UserData

    suspend fun setUserDateInfo(dateInfo: DateBaseResponse)
    suspend fun getUserDateInfo(): DateBaseResponse

    suspend fun clearAll()
}
