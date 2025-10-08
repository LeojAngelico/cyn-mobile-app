package cyn.mobile.app.di

import cyn.mobile.app.data.repositories.interceptor.NoAccessTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModuleNoAuth {

    @Provides @Singleton
    fun provideNoAccessTokenInterceptor(): NoAccessTokenInterceptor = NoAccessTokenInterceptor()

    @Provides @Singleton
    fun provideOkHttpClient(noAccessTokenInterceptor: NoAccessTokenInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            // place it early if you want it to run before others
            .addInterceptor(noAccessTokenInterceptor)
            .build()
    }
}
