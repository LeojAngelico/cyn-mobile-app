package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.data.repositories.otp.OtpRemoteDataSource
import cyn.mobile.app.data.repositories.otp.OtpRepository
import cyn.mobile.app.data.repositories.otp.OtpService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class OtpModule {

    @Provides
    @ViewModelScoped
    fun providesOtpService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): OtpService {
        return AppRetrofitService.Builder()
            // OTP typically doesn't require auth; set to false.
            .withAuthInterceptor(false)
            // Safe to pass in; will be ignored if auth interceptor is disabled.
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_TEST_URL, OtpService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesOtpRemoteDataSource(
        service: OtpService
    ): OtpRemoteDataSource = OtpRemoteDataSource(service)

    @Provides
    @ViewModelScoped
    fun providesOtpRepository(
        remoteDataSource: OtpRemoteDataSource
    ): OtpRepository = OtpRepository(remoteDataSource)
}
