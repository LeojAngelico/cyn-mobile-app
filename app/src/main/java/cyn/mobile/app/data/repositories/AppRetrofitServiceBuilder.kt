package cyn.mobile.app.data.repositories

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cyn.mobile.app.BuildConfig
import java.util.concurrent.TimeUnit
import cyn.mobile.app.data.repositories.interceptor.AccessTokenInterceptor
import cyn.mobile.app.data.repositories.interceptor.NoAccessTokenInterceptor
import okhttp3.JavaNetCookieJar
import java.net.CookieManager
import java.net.CookiePolicy

class AppRetrofitService private constructor() {

    data class Builder(
        // Toggle which interceptor to use
        var withAuthInterceptor: Boolean = true,
        // Inject your interceptors (e.g., via Hilt)
        var accessTokenInterceptor: AccessTokenInterceptor? = null,
        var noAccessTokenInterceptor: NoAccessTokenInterceptor? = null,

        // Control logging behavior (default: only on debug builds)
        var enableHttpLogging: Boolean = BuildConfig.DEBUG,
        var httpLoggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,

        // Timeouts
        var connectTimeoutSeconds: Long = 30,
        var readTimeoutSeconds: Long = 30,
        var writeTimeoutSeconds: Long = 30,

        // Extra interceptors
        var applicationInterceptors: List<Interceptor> = emptyList(),
        var networkInterceptors: List<Interceptor> = emptyList(),

        // Optional: override OkHttpClient.Builder if you need very custom config
        var clientCustomizer: (OkHttpClient.Builder.() -> Unit)? = null,

        // Optional: override Moshi if you need custom adapters
        var moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    ) {

        fun withAuthInterceptor(enabled: Boolean) = apply { this.withAuthInterceptor = enabled }

        fun accessTokenInterceptor(interceptor: AccessTokenInterceptor) = apply {
            this.accessTokenInterceptor = interceptor
        }

        fun noAccessTokenInterceptor(interceptor: NoAccessTokenInterceptor) = apply {
            this.noAccessTokenInterceptor = interceptor
        }

        fun enableHttpLogging(enabled: Boolean) = apply { this.enableHttpLogging = enabled }
        fun httpLoggingLevel(level: HttpLoggingInterceptor.Level) = apply { this.httpLoggingLevel = level }

        fun timeouts(
            connect: Long = connectTimeoutSeconds,
            read: Long = readTimeoutSeconds,
            write: Long = writeTimeoutSeconds
        ) = apply {
            connectTimeoutSeconds = connect
            readTimeoutSeconds = read
            writeTimeoutSeconds = write
        }

        fun addInterceptors(vararg interceptors: Interceptor) = apply {
            applicationInterceptors = applicationInterceptors + interceptors
        }

        fun addNetworkInterceptors(vararg interceptors: Interceptor) = apply {
            networkInterceptors = networkInterceptors + interceptors
        }

        fun customizeClient(block: OkHttpClient.Builder.() -> Unit) = apply { clientCustomizer = block }
        fun moshi(moshi: Moshi) = apply { this.moshi = moshi }

        fun <T> build(baseUrl: String, service: Class<T>): T {
            val okHttpClient = createOkHttpClient()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.ensureEndsWithSlash())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()

            return retrofit.create(service)
        }

        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }


        private fun createOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
                .cookieJar(JavaNetCookieJar(cookieManager))
                .followRedirects(false)
                .followSslRedirects(false)

            // Choose which auth interceptor to add (if provided)
            if (withAuthInterceptor) {
                accessTokenInterceptor?.let { builder.addInterceptor(it) }
            } else {
                noAccessTokenInterceptor?.let { builder.addInterceptor(it) }
            }

            // Logging
            if (enableHttpLogging) {
                builder.addInterceptor(HttpLoggingInterceptor().apply { level = httpLoggingLevel })
            }

            // Extra interceptors
            applicationInterceptors.forEach { builder.addInterceptor(it) }
            networkInterceptors.forEach { builder.addNetworkInterceptor(it) }

            // Allow final customization
            clientCustomizer?.let { builder.it() }

            return builder.build()
        }
    }
}

private fun String.ensureEndsWithSlash(): String =
    if (this.endsWith("/")) this else "$this/"
