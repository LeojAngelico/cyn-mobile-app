package cyn.mobile.app.di

import android.content.Context
import cyn.mobile.app.security.AuthStorage
import cyn.mobile.app.security.AuthStorageDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrefsImpl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataStoreImpl

@Module
@InstallIn(SingletonComponent::class)
object AuthStorageProviders {
    @Provides @Singleton @PrefsImpl
    fun providePrefs(@ApplicationContext context: Context): AuthStorage = AuthStorageDataStore(context)

    @Provides @Singleton @DataStoreImpl
    fun provideDataStore(@ApplicationContext context: Context): AuthStorage = AuthStorageDataStore(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStorageBinding {
    // Choose which one is the default AuthStorage for injection:
    // Option B (DataStore) as default:
    @Binds @Singleton
    abstract fun bindAuthStorage(@DataStoreImpl impl: AuthStorage): AuthStorage

    // If you want to switch to Prefs as default, replace the line above with:
    // @Binds @Singleton
    // abstract fun bindAuthStorage(@PrefsImpl impl: AuthStorage): AuthStorage
}
