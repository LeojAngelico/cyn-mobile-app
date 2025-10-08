package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.auth.AuthRemoteDataSource
import cyn.mobile.app.data.repositories.auth.AuthRepository
import cyn.mobile.app.data.repositories.auth.AuthService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.security.AuthStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class AuthModule {

    @Provides
    @ViewModelScoped
    fun providesAuthService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): AuthService {
        return AppRetrofitService.Builder()
            .withAuthInterceptor(true)
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_URL, AuthService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesAuthRemoteDataSource(authService: AuthService): AuthRemoteDataSource {
        return AuthRemoteDataSource(authService)
    }

    @Provides
    fun providesAuthRepository(
        authRemoteDataSource: AuthRemoteDataSource,
        authStorage: AuthStorage,
    ): AuthRepository {
        return AuthRepository(authRemoteDataSource, authStorage)
    }

}