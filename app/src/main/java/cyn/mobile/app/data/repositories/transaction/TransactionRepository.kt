package cyn.mobile.app.data.repositories.transaction

import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.data.repositories.transaction.request.StoreTestResultRequest
import cyn.mobile.app.data.repositories.transaction.request.TransactionListRequest
import cyn.mobile.app.data.repositories.transaction.response.TransactionListResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun doStoreTestResult(request: StoreTestResultRequest): Flow<GeneralResponse> {
        return flow {
            emit(remoteDataSource.doStoreTestResult(request))
        }.flowOn(ioDispatcher)
    }

    fun getTransactionList(request: TransactionListRequest): Flow<TransactionListResponse> {
        return flow {
            emit(remoteDataSource.getTransactionList(request))
        }.flowOn(ioDispatcher)
    }
}
