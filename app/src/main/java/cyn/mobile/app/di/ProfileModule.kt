package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.data.repositories.profile.ProfileRemoteDataSource
import cyn.mobile.app.data.repositories.profile.ProfileRepository
import cyn.mobile.app.data.repositories.profile.ProfileService
import cyn.mobile.app.security.AuthStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class ProfileModule {

    @Provides
    @ViewModelScoped
    fun providesProfileService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): ProfileService {
        return AppRetrofitService.Builder()
            .withAuthInterceptor(true)
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_URL, ProfileService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesProfileRemoteDataSource(profileService: ProfileService): ProfileRemoteDataSource {
        return ProfileRemoteDataSource(profileService)
    }

    @Provides
    fun providesProfileRepository(
        profileRemoteDataSource: ProfileRemoteDataSource,
        authStorage: AuthStorage,
    ): ProfileRepository {
        return ProfileRepository(profileRemoteDataSource, authStorage = authStorage)
    }

}