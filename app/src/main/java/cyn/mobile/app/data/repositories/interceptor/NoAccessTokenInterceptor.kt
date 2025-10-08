package cyn.mobile.app.data.repositories.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class NoAccessTokenInterceptor : Interceptor {

    companion object {
        private val TAG: String = NoAccessTokenInterceptor::class.java.simpleName
        @Volatile private var interceptorLogId = 0

        @Synchronized
        fun nextId(): Int {
            interceptorLogId++
            return interceptorLogId
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val generatedId = nextId()
        Log.d(TAG, "NoAccessTokenInterceptor id=$generatedId")
        val request = newRequestWithoutAccessToken(chain.request())
        return chain.proceed(request)
    }

    private fun newRequestWithoutAccessToken(request: Request): Request {
        // Intentionally do not add any Authorization header
        return request.newBuilder().build()
    }
}