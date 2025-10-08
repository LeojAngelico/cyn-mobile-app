package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.data.repositories.transaction.TransactionRemoteDataSource
import cyn.mobile.app.data.repositories.transaction.TransactionRepository
import cyn.mobile.app.data.repositories.transaction.TransactionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class TransactionModule {

    @Provides
    @ViewModelScoped
    fun providesTransactionService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): TransactionService {
        return AppRetrofitService.Builder()
            .withAuthInterceptor(true)
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_URL, TransactionService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesTransactionRemoteDataSource(transactionService: TransactionService): TransactionRemoteDataSource {
        return TransactionRemoteDataSource(transactionService)
    }

    @Provides
    fun providesTransactionRepository(
        transactionRemoteDataSource: TransactionRemoteDataSource,
    ): TransactionRepository {
        return TransactionRepository(transactionRemoteDataSource)
    }

}