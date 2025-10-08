package cyn.mobile.app.data.repositories.dashboard

import cyn.mobile.app.data.repositories.dashboard.response.DashboardTotalResponse
import cyn.mobile.app.data.repositories.dashboard.response.LatestTransactionResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val remoteDataSource: DashboardRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getDashboardInfo(): Flow<DashboardTotalResponse> {
        return flow {
            emit(remoteDataSource.getDashboardInfo())
        }.flowOn(ioDispatcher)
    }

    fun getLatestTransaction(): Flow<LatestTransactionResponse> {
        return flow {
            emit(remoteDataSource.getLatestTransaction())
        }.flowOn(ioDispatcher)
    }
}
