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

        if (!response.isSuccessful) {
            val errorBody = runCatching { response.errorBody()?.string().orEmpty() }.getOrDefault("")
            if (errorBody.isNotBlank()) {
                runCatching { Gson().fromJson(errorBody, InitiateOAuthResponse::class.java) }
                    .getOrNull()
                    ?.let { return it.copy(success = it.success ?: false) }
            }
            throw HttpException(response)
        }

        // The body is a URL to call
        val linkRaw = runCatching { response.body()?.string().orEmpty() }.getOrDefault("").trim()

        if (linkRaw.isBlank()) {
            return InitiateOAuthResponse(success = false, message = "Empty link in initiate response")
        }

        // Remove optional surrounding quotes
        val link = linkRaw.removeSurrounding("\"").trim()
        val initialUrl = link.toHttpUrlOrNull()?.toString()
            ?: run {
                // If not a plain URL, try to pull "url" or "location" from a JSON payload
                val guessed = runCatching {
                    val obj = JSONObject(link)
                    obj.optString("url").ifBlank { obj.optString("location") }
                }.getOrDefault("")
                guessed.toHttpUrlOrNull()?.toString()
            }

        if (initialUrl.isNullOrBlank()) {
            return InitiateOAuthResponse(success = false, message = "Initiate returned non-URL payload")
        }

        // Follow the URL, handle up to 5 redirects manually if present
        var currentUrl = initialUrl
        var attempts = 0
        while (attempts < 5) {
            val follow = oAuthService.followLocation(currentUrl.orEmpty())
            val followStatus = follow.code()

            if (followStatus in 300..399) {
                val rawLocation = follow.headers()["Location"] ?: follow.headers()["location"]
                if (rawLocation.isNullOrBlank()) {
                    // Some providers put code/message directly in the redirect URL; try to parse it
                    return parseInitiateOAuthResponseManual(currentUrl.orEmpty())
                }

                val baseUrl = follow.raw().request.url
                val resolved = baseUrl.resolve(rawLocation)?.toString() ?: rawLocation
                currentUrl = resolved
                attempts++
                continue
            }

            if (follow.isSuccessful) {
                val json = follow.body()?.string().orEmpty()
                if (json.isBlank()) {
                    return InitiateOAuthResponse(success = false, message = "Empty JSON from auth URL")
                }
                return runCatching { Gson().fromJson(json, InitiateOAuthResponse::class.java) }
                    .getOrElse { e ->
                        // If server returned a non-standard payload, try manual fallback once
                        parseInitiateOAuthResponseManual(json).takeIf { it.message != null || it.code != null }
                            ?: InitiateOAuthResponse(success = false, message = "Invalid JSON: ${e.message}")
                    }
            } else {
                val errorJson = follow.errorBody()?.string().orEmpty()
                if (errorJson.isNotBlank()) {
                    runCatching { Gson().fromJson(errorJson, InitiateOAuthResponse::class.java) }
                        .getOrNull()
                        ?.let { return it.copy(success = false) }
                }
                throw HttpException(follow)
            }
        }

        return InitiateOAuthResponse(success = false, message = "Too many redirects")
    }

    fun parseInitiateOAuthResponseManual(locationOrJson: String): InitiateOAuthResponse {
        locationOrJson.toHttpUrlOrNull()?.let { url ->
            Log.d("OAuth", "url=$url")
            val code = url.queryParameter("code")
            val message = url.queryParameter("message")
            val status = url.queryParameter("status")?: false
            Log.d("OAuth", "code=$code message=$message status=$status")
            return InitiateOAuthResponse(
                success = status as Boolean?,
                code = code,
                message = message
            )
        }

        return runCatching {
            val decoded = URLDecoder.decode(locationOrJson, StandardCharsets.UTF_8.name())
            val obj = JSONObject(decoded)
            Log.d("OAuth", "obj=$obj")
            InitiateOAuthResponse(
                success = obj.optBoolean("status", false),
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