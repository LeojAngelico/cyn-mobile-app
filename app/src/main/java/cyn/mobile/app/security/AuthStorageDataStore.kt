package cyn.mobile.app.security

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.base.response.DateBaseResponse
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val DATASTORE_NAME = "auth_secure_ds"
private const val TINK_PREFS = "tink_prefs"
private const val TINK_KEYSET_ALIAS = "tink_keyset"
private const val TINK_MASTER_KEY_URI = "android-keystore://tink_master_key"

private val Context.authSecureDataStore by preferencesDataStore(name = DATASTORE_NAME)

class AuthStorageDataStore(context: Context) : AuthStorage {

    private val appContext = context.applicationContext
    private val dataStore = appContext.authSecureDataStore

    private val aead: Aead by lazy {
        AeadConfig.register()
        val handle = AndroidKeysetManager.Builder()
            .withSharedPref(appContext, TINK_KEYSET_ALIAS, TINK_PREFS)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(TINK_MASTER_KEY_URI)
            .build()
            .keysetHandle
        handle.getPrimitive(Aead::class.java)
    }

    private fun encryptToB64(text: String): String {
        val ct = aead.encrypt(text.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(ct, Base64.NO_WRAP)
    }

    private fun decryptFromB64(b64: String): String {
        val ct = Base64.decode(b64, Base64.NO_WRAP)
        val pt = aead.decrypt(ct, null)
        return pt.toString(Charsets.UTF_8)
    }

    private object K {
        val ACCESS_TOKEN = stringPreferencesKey("ACCESS_TOKEN")

        val USER_ID = stringPreferencesKey("USER_INFO_ID")
        val USER_FIRST_NAME = stringPreferencesKey("USER_FIRST_NAME")
        val USER_MIDDLE_NAME = stringPreferencesKey("USER_MIDDLE_NAME")
        val USER_LAST_NAME = stringPreferencesKey("USER_LAST_NAME")
        val USER_EMAIL = stringPreferencesKey("USER_EMAIL")
        val USER_USERNAME = stringPreferencesKey("USER_USERNAME")
        val USER_CONTACT_NUMBER = stringPreferencesKey("USER_CONTACT_NUMBER")
        val USER_ADDRESS = stringPreferencesKey("USER_ADDRESS")

        val USER_DATE_DB = stringPreferencesKey("USER_DATE_DB")
        val USER_DATE_MONTH_YEAR = stringPreferencesKey("USER_DATE_MONTH_YEAR")
        val USER_DATE_TIME_PASSED = stringPreferencesKey("USER_DATE_TIME_PASSED")
        val USER_DATE_TIMESTAMP = stringPreferencesKey("USER_DATE_TIMESTAMP")
        val USER_DATE_TIME_PH = stringPreferencesKey("USER_DATE_TIME_PH")
        val USER_DATE_TIME_ONLY = stringPreferencesKey("USER_DATE_TIME_ONLY")
    }

    override suspend fun setAccessToken(token: String) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                if (token.isEmpty()) {
                    prefs.remove(K.ACCESS_TOKEN)
                } else {
                    prefs[K.ACCESS_TOKEN] = encryptToB64(token)
                }
            }
        }
    }

    override suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val b64 = dataStore.data.map { it[K.ACCESS_TOKEN] }.first()
        b64?.let(::decryptFromB64).orEmpty()
    }

    override suspend fun isLoggedIn(): Boolean = getAccessToken().isNotEmpty()

    override suspend fun resetToken() {
        setAccessToken("")
    }

    override suspend fun setUserBasicInfo(user: UserData) {
        withContext(Dispatchers.IO) {
            user.date_created?.let { setUserDateInfo(it) }
            dataStore.edit { prefs ->
                prefs[K.USER_ID] = encryptToB64((user.id ?: 0).toString())
                setOrRemove(prefs, K.USER_FIRST_NAME, user.firstname)
                setOrRemove(prefs, K.USER_MIDDLE_NAME, user.middlename)
                setOrRemove(prefs, K.USER_LAST_NAME, user.lastname)
                setOrRemove(prefs, K.USER_EMAIL, user.email)
                setOrRemove(prefs, K.USER_USERNAME, user.username)
                setOrRemove(prefs, K.USER_CONTACT_NUMBER, user.contact_number)
                setOrRemove(prefs, K.USER_ADDRESS, user.address)
            }
        }
    }

    override suspend fun getUserBasicInfo(): UserData = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        val idStr = prefs[K.USER_ID]?.let(::decryptFromB64)
        UserData(
            id = idStr?.toIntOrNull() ?: 0,
            firstname = prefs[K.USER_FIRST_NAME]?.let(::decryptFromB64),
            middlename = prefs[K.USER_MIDDLE_NAME]?.let(::decryptFromB64),
            lastname = prefs[K.USER_LAST_NAME]?.let(::decryptFromB64),
            email = prefs[K.USER_EMAIL]?.let(::decryptFromB64),
            username = prefs[K.USER_USERNAME]?.let(::decryptFromB64),
            contact_number = prefs[K.USER_CONTACT_NUMBER]?.let(::decryptFromB64),
            address = prefs[K.USER_ADDRESS]?.let(::decryptFromB64),
            date_created = getUserDateInfo()
        )
    }

    override suspend fun setUserDateInfo(dateInfo: DateBaseResponse) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                setOrRemove(prefs, K.USER_DATE_DB, dateInfo.date_db)
                setOrRemove(prefs, K.USER_DATE_MONTH_YEAR, dateInfo.month_year)
                setOrRemove(prefs, K.USER_DATE_TIME_PASSED, dateInfo.time_passed)
                setOrRemove(prefs, K.USER_DATE_TIMESTAMP, dateInfo.timestamp)
                setOrRemove(prefs, K.USER_DATE_TIME_PH, dateInfo.datetime_ph)
                setOrRemove(prefs, K.USER_DATE_TIME_ONLY, dateInfo.date_only_ph)
            }
        }
    }

    override suspend fun getUserDateInfo(): DateBaseResponse = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        DateBaseResponse(
            date_db = prefs[K.USER_DATE_DB]?.let(::decryptFromB64),
            month_year = prefs[K.USER_DATE_MONTH_YEAR]?.let(::decryptFromB64),
            time_passed = prefs[K.USER_DATE_TIME_PASSED]?.let(::decryptFromB64),
            timestamp = prefs[K.USER_DATE_TIMESTAMP]?.let(::decryptFromB64),
            datetime_ph = prefs[K.USER_DATE_TIME_PH]?.let(::decryptFromB64),
            date_only_ph = prefs[K.USER_DATE_TIME_ONLY]?.let(::decryptFromB64)
        )
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }

    private fun setOrRemove(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<String>,
        value: String?
    ) {
        if (value.isNullOrEmpty()) {
            prefs.remove(key)
        } else {
            prefs[key] = encryptToB64(value)
        }
    }
}
