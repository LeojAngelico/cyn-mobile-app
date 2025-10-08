package cyn.mobile.app.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    companion object {
        var instance: BaseApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}