package cyn.mobile.app.data.repositories.transaction

import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.data.repositories.transaction.request.StoreTestResultRequest
import cyn.mobile.app.data.repositories.transaction.request.TransactionListRequest
import cyn.mobile.app.data.repositories.transaction.response.TransactionListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TransactionService {

    @POST("api/auth/transaction/store")
    suspend fun doStoreTestResult(
        @Body request: StoreTestResultRequest
    ): Response<GeneralResponse>

    @POST("api/auth/transaction")
    suspend fun getTransactionList(
        @Body request: TransactionListRequest
    ): Response<TransactionListResponse>
}