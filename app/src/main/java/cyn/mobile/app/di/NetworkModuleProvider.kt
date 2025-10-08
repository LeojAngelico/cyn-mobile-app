package cyn.mobile.app.di

import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(accessTokenInterceptor: AccessTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(accessTokenInterceptor)
            .build()
    }
}
