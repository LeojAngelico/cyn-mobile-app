package cyn.mobile.app.data.repositories.interceptor

import cyn.mobile.app.security.AuthStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessTokenInterceptor @Inject constructor(
    private val authStorage: AuthStorage
) : Interceptor {

    companion object {
        private val TAG: String = AccessTokenInterceptor::class.java.simpleName
        @Volatile private var interceptorLogId = 0

        @Synchronized
        fun nextId(): Int {
            interceptorLogId++
            return interceptorLogId
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val generatedId = nextId()
        val request = newRequestWithAccessToken(chain.request())
        return chain.proceed(request)
    }

    private fun newRequestWithAccessToken(request: Request): Request {
        if (request.header("Authorization")?.isNotBlank() == true) {
            return request
        }

        val token: String = runBlocking(Dispatchers.IO) {
            authStorage.getAccessToken()
        }

        val builder = request.newBuilder()
        if (token.isNotBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        return builder.build()
    }
}
