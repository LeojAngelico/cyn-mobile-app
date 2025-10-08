package cyn.mobile.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.base.response.DateBaseResponse

/**
 * Secure storage for authentication and basic user info.
 * Uses EncryptedSharedPreferences with AES256 key encryption and value encryption.
 *
 * Notes:
 * - Avoids keeping a static Context reference.
 * - Keeps a small in-memory cache for frequently used values.
 * - Uses apply() for asynchronous persistence and better performance.
 */
class AuthSecureStorage(context: Context) {

    // MasterKey with recommended AES256_GCM scheme
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Encrypted SharedPreferences using AES256_SIV for keys and AES256_GCM for values
    @VisibleForTesting
    internal val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // In-memory cache
    @Volatile private var cachedAccessToken: String? = null
    @Volatile private var cachedServerUrl: String? = null
    @Volatile private var cachedUser: UserData? = null
    @Volatile private var cachedDateCreated: DateBaseResponse? = null

    // -------------------------------
    // Access Token
    // -------------------------------
    fun setAccessToken(token: String) {
        cachedAccessToken = token
        sharedPreferences.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    fun getAccessToken(): String {
        val cached = cachedAccessToken
        if (cached != null) return cached
        return (sharedPreferences.getString(KEY_ACCESS_TOKEN, "") ?: "").also {
            cachedAccessToken = it
        }
    }

    fun isLoggedIn(): Boolean = getAccessToken().isNotEmpty()

    fun resetToken() {
        setAccessToken("")
    }

    // -------------------------------
    // User Basic Info
    // -------------------------------
    fun setUserBasicInfo(user: UserData) {
        cachedUser = user
        // Save date info if present
        user.date_created?.let { setUserDateInfo(it) }

        sharedPreferences.edit {
            putInt(KEY_USER_ID, user.id?:0)
            putString(KEY_USER_FIRST_NAME, user.firstname.orEmpty())
            putString(KEY_USER_MIDDLE_NAME, user.middlename.orEmpty())
            putString(KEY_USER_LAST_NAME, user.lastname.orEmpty())
            putString(KEY_USER_EMAIL, user.email.orEmpty())
            putString(KEY_USER_ADDRESS, user.address.orEmpty())
            putString(KEY_USER_CONTACT_NUMBER, user.contact_number.orEmpty())
            putString(KEY_USER_USERNAME, user.username.orEmpty())
        }
    }

    fun getUserBasicInfo(): UserData {
        val cached = cachedUser
        if (cached != null) return cached

        val user = UserData(
            id = sharedPreferences.getInt(KEY_USER_ID, 0),
            firstname = sharedPreferences.getString(KEY_USER_FIRST_NAME, "")?.ifEmpty { null },
            middlename = sharedPreferences.getString(KEY_USER_FIRST_NAME, "")?.ifEmpty { null },
            lastname = sharedPreferences.getString(KEY_USER_FIRST_NAME, "")?.ifEmpty { null },
            email = sharedPreferences.getString(KEY_USER_EMAIL, "")?.ifEmpty { null },
            username = sharedPreferences.getString(KEY_USER_USERNAME, "")?.ifEmpty { null },
            date_created = getUserDateInfo()
        )
        cachedUser = user
        return user
    }

    // -------------------------------
    // User Date Created Info
    // -------------------------------
    fun setUserDateInfo(dateInfo: DateBaseResponse) {
        cachedDateCreated = dateInfo
        sharedPreferences.edit {
            putString(KEY_USER_DATE_DB, dateInfo.date_db.orEmpty())
            putString(KEY_USER_DATE_MONTH_YEAR, dateInfo.month_year.orEmpty())
            putString(KEY_USER_DATE_TIME_PASSED, dateInfo.time_passed.orEmpty())
            putString(KEY_USER_DATE_TIMESTAMP, dateInfo.timestamp.orEmpty())
            putString(KEY_USER_DATE_TIME_PH, dateInfo.datetime_ph.orEmpty())
            putString(KEY_USER_DATE_TIME_ONLY, dateInfo.date_only_ph.orEmpty())
        }
    }

    fun getUserDateInfo(): DateBaseResponse {
        val cached = cachedDateCreated
        if (cached != null) return cached

        val info = DateBaseResponse(
            date_db = sharedPreferences.getString(KEY_USER_DATE_DB, "")?.ifEmpty { null },
            month_year = sharedPreferences.getString(KEY_USER_DATE_MONTH_YEAR, "")?.ifEmpty { null },
            time_passed = sharedPreferences.getString(KEY_USER_DATE_TIME_PASSED, "")?.ifEmpty { null },
            timestamp = sharedPreferences.getString(KEY_USER_DATE_TIMESTAMP, "")?.ifEmpty { null },
            datetime_ph = sharedPreferences.getString(KEY_USER_DATE_TIME_PH, "")?.ifEmpty { null },
            date_only_ph = sharedPreferences.getString(KEY_USER_DATE_TIME_ONLY, "")?.ifEmpty { null },
        )
        cachedDateCreated = info
        return info
    }

    // -------------------------------
    // Clearing
    // -------------------------------
    fun clearAll() {
        // Wipe cache first
        cachedAccessToken = null
        cachedServerUrl = null
        cachedUser = null
        cachedDateCreated = null

        // Wipe persisted data
        sharedPreferences.edit {
            clear()
        }
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "auth_secure_prefs"

        private const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"

        private const val KEY_USER_ID = "USER_INFO_ID"
        private const val KEY_USER_FIRST_NAME = "USER_FIRST_NAME"
        private const val KEY_USER_MIDDLE_NAME = "USER_MIDDLE_NAME"
        private const val KEY_USER_LAST_NAME = "USER_LAST_NAME"
        private const val KEY_USER_EMAIL = "USER_EMAIL"
        private const val KEY_USER_USERNAME = "USER_USERNAME"
        private const val KEY_USER_CONTACT_NUMBER = "USER_CONTACT_NUMBER"
        private const val KEY_USER_ADDRESS = "USER_ADDRESS"

        private const val KEY_USER_DATE_DB = "USER_DATE_DB"
        private const val KEY_USER_DATE_MONTH_YEAR = "USER_DATE_MONTH_YEAR"
        private const val KEY_USER_DATE_TIME_PASSED = "USER_DATE_TIME_PASSED"
        private const val KEY_USER_DATE_TIMESTAMP = "USER_DATE_TIMESTAMP"
        private const val KEY_USER_DATE_TIME_PH = "USER_DATE_TIME_PH"
        private const val KEY_USER_DATE_TIME_ONLY= "USER_DATE_TIME_ONLY"
    }
}
