package cyn.mobile.app.data.repositories.test

import android.util.Log
import com.google.gson.Gson
import cyn.mobile.app.data.repositories.test.request.InitiateOAuthRequest
import cyn.mobile.app.data.repositories.test.request.TokenExchangeRequest
import cyn.mobile.app.data.repositories.test.request.VerifyPhoneRequest
import cyn.mobile.app.data.repositories.test.response.InitiateOAuthResponse
import cyn.mobile.app.data.repositories.test.response.TokenExchangeResponse
import cyn.mobile.app.data.repositories.test.response.VerifyPhoneResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class OAuthRemoteDataSource @Inject constructor(
    private val oAuthService: OAuthService
) {

    // Kotlin
    suspend fun initiateOAuth(phoneNumber: String, clientIdentifier: String): InitiateOAuthResponse {
        val req = InitiateOAuthRequest(
            phoneNumber = phoneNumber,
            client_identifier = clientIdentifier
        )

        val response = oAuthService.initiateOAuth(req)
        val status = response.code()
        Log.d("OAuth", "status=${response.code()} headers=${response.headers()}")

        // 1) Handle redirects ourselves
        if (status in 300..399) {
            val rawLocation = response.headers()["Location"] ?: response.headers()["location"]
            Log.d("OAuth", "rawLocation=$rawLocation")
            if (rawLocation.isNullOrBlank()) {
                return InitiateOAuthResponse(
                    success = false,
                    message = "Redirect ($status) without Location header"
                )
            }

            // Resolve relative -> absolute
            val baseUrl = response.raw().request.url
            val resolved = baseUrl.resolve(rawLocation)?.toString() ?: rawLocation
            Log.d("OAuth", "resolvedLocation=$resolved")

            // If it's a custom scheme, skip network call and parse query params
            if (!resolved.startsWith("http://") && !resolved.startsWith("https://")) {
                return parseInitiateOAuthResponseManual(resolved)
            }

            // 2) Do a GET to the Location URL
            val follow = oAuthService.followLocation(resolved)
            val followStatus = follow.code()
            Log.d("OAuth", "follow status=$followStatus headers=${follow.headers()}")

            // If the redirect chain continues, you can either loop or parse the URL right away.
            if (followStatus in 300..399) {
                // Often code/message already live in the URL. Fallback to manual parsing.
                return parseInitiateOAuthResponseManual(resolved)
            }

            if (follow.isSuccessful) {
                val body = follow.body()?.string().orEmpty()
                // Try JSON first
                val parsed = runCatching {
                    Gson().fromJson(body, InitiateOAuthResponse::class.java)
                }.getOrElse {
                    // If not valid JSON, try manual parsing (e.g., HTML page with embedded query params)
                    parseInitiateOAuthResponseManual(resolved)
                }
                return parsed
            }

            // Non-success follow response -> surface the error
            throw HttpException(follow)
        }

        // 3) Normal non-redirect success
        if (response.isSuccessful) {
            val raw = response.body()?.string().orEmpty()
            return runCatching {
                Gson().fromJson(raw, InitiateOAuthResponse::class.java)
            }.getOrElse {
                InitiateOAuthResponse(success = false, message = "Unable to parse success body: ${it.message}")
            }
        }

        // 4) Non-success and non-redirect -> throw
        throw HttpException(response)
    }

    fun parseInitiateOAuthResponseManual(locationOrJson: String): InitiateOAuthResponse {
        locationOrJson.toHttpUrlOrNull()?.let { url ->
            Log.d("OAuth", "url=$url")
            val code = url.queryParameter("code")
            val message = url.queryParameter("message")
            Log.d("OAuth", "code=$code message=$message")
            return InitiateOAuthResponse(
                success = message?.contains("undefined") == false,
                code = code,
                message = message
            )
        }

        return runCatching {
            val decoded = URLDecoder.decode(locationOrJson, StandardCharsets.UTF_8.name())
            val obj = JSONObject(decoded)
            Log.d("OAuth", "obj=$obj")
            InitiateOAuthResponse(
                success = obj.optString("message", null).contains("undefined").not(),
                code = obj.optString("code", null).takeIf { it.isNotEmpty() },
                message = obj.optString("message", null).takeIf { it.isNotEmpty() }
            )
        }.getOrElse {
            // Returning a value prevents the Flow from ending up in .catch
            InitiateOAuthResponse(
                success = false,
                message = "Unexpected Location format"
            )
        }
    }


    suspend fun exchangeToken(
        code: String
    ): TokenExchangeResponse {
        val response = oAuthService.exchangeToken(
            TokenExchangeRequest(code = code)
        )
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun verifyPhone(
        accessToken: String,
        phoneNumber: String
    ): VerifyPhoneResponse {
        val response = oAuthService.verifyPhone(
            VerifyPhoneRequest(accessToken = accessToken, phoneNumber = phoneNumber)
        )
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }
}
