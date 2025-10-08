// File: TransactionRemoteDataSource.kt
package cyn.mobile.app.data.repositories.transaction

import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.data.repositories.transaction.request.StoreTestResultRequest
import cyn.mobile.app.data.repositories.transaction.request.TransactionListRequest
import cyn.mobile.app.data.repositories.transaction.response.TransactionListResponse
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class TransactionRemoteDataSource @Inject constructor(
    private val transactionService: TransactionService
) {

    suspend fun doStoreTestResult(request: StoreTestResultRequest): GeneralResponse {
        val response = transactionService.doStoreTestResult(request)
        if (response.code() != HttpURLConnection.HTTP_CREATED) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun getTransactionList(request: TransactionListRequest): TransactionListResponse {
        val response = transactionService.getTransactionList(request)
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }
}
