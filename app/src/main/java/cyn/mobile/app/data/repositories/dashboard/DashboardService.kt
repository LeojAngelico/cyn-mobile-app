package cyn.mobile.app.data.repositories.dashboard

import cyn.mobile.app.data.repositories.dashboard.response.DashboardTotalResponse
import cyn.mobile.app.data.repositories.dashboard.response.LatestTransactionResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardService {

    @GET("api/auth/dashboard/total")
    suspend fun getDashboardInfo(): Response<DashboardTotalResponse>

    @GET("api/auth/dashboard/latest")
    suspend fun getLatestTransaction(): Response<LatestTransactionResponse>
}