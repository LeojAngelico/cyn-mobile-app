package cyn.mobile.app.data.repositories.dashboard

import cyn.mobile.app.data.repositories.dashboard.response.DashboardTotalResponse
import cyn.mobile.app.data.repositories.dashboard.response.LatestTransactionResponse
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class DashboardRemoteDataSource @Inject constructor(
    private val dashboardService: DashboardService
) {

    suspend fun getDashboardInfo(): DashboardTotalResponse {
        val response = dashboardService.getDashboardInfo()
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun getLatestTransaction(): LatestTransactionResponse {
        val response = dashboardService.getLatestTransaction()
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }
}
