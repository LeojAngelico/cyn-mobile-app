package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.auth.AuthService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.data.repositories.test.OAuthRemoteDataSource
import cyn.mobile.app.data.repositories.test.OAuthRepository
import cyn.mobile.app.data.repositories.test.OAuthService
import cyn.mobile.app.security.AuthStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class `OAuthModule` {

    @Provides
    @ViewModelScoped
    fun providesOAuthService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): OAuthService {
        return AppRetrofitService.Builder()
            .withAuthInterceptor(true)
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_TEST_URL, OAuthService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesOAuthRemoteDataSource(authService: OAuthService): OAuthRemoteDataSource {
        return OAuthRemoteDataSource(authService)
    }

    @Provides
    fun providesOAuthRepository(
        authRemoteDataSource: OAuthRemoteDataSource,
        authStorage: AuthStorage,
    ): OAuthRepository {
        return OAuthRepository(authRemoteDataSource)
    }

}