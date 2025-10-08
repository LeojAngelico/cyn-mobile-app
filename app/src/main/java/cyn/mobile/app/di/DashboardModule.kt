package cyn.mobile.app.di

import cyn.mobile.app.BuildConfig
import cyn.mobile.app.data.repositories.AppRetrofitService
import cyn.mobile.app.data.repositories.dashboard.DashboardRemoteDataSource
import cyn.mobile.app.data.repositories.dashboard.DashboardRepository
import cyn.mobile.app.data.repositories.dashboard.DashboardService
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class DashboardModule {

    @Provides
    @ViewModelScoped
    fun providesDashboardService(
        accessTokenInterceptor: AccessTokenInterceptor
    ): DashboardService {
        return AppRetrofitService.Builder()
            .withAuthInterceptor(true)
            .accessTokenInterceptor(accessTokenInterceptor)
            .enableHttpLogging(BuildConfig.DEBUG)
            .build(BuildConfig.BASE_URL, DashboardService::class.java)
    }

    @Provides
    @ViewModelScoped
    fun providesDashboardRemoteDataSource(dashboardService: DashboardService): DashboardRemoteDataSource {
        return DashboardRemoteDataSource(dashboardService)
    }

    @Provides
    fun providesDashboardRepository(
        dashboardRemoteDataSource: DashboardRemoteDataSource,
    ): DashboardRepository {
        return DashboardRepository(dashboardRemoteDataSource)
    }

}