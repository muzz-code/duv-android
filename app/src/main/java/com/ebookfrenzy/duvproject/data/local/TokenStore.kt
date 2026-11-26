package com.ebookfrenzy.duvproject.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ebookfrenzy.duvproject.data.remote.dto.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps the login session (tokens + who is logged in), encrypted with a key held in the Android Keystore. */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    val accessToken: String? get() = prefs.getString(KEY_ACCESS, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH, null)
    val isLoggedIn: Boolean get() = refreshToken != null

    /** True when the logged-in account has the Entertainer role (decides which home screen to open). */
    val isEntertainer: Boolean get() = prefs.getBoolean(KEY_ENTERTAINER, false)

    @Synchronized
    fun save(auth: AuthResponse) = prefs.edit {
        putString(KEY_ACCESS, auth.accessToken)
        putString(KEY_REFRESH, auth.refreshToken)
        putBoolean(KEY_ENTERTAINER, ROLE_ENTERTAINER in auth.user.roles)
        putString(KEY_FIRST_NAME, auth.user.firstName)
    }

    @Synchronized
    fun clear() = prefs.edit { clear() }

    private companion object {
        const val FILE_NAME = "duv_session"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_ENTERTAINER = "is_entertainer"
        const val KEY_FIRST_NAME = "first_name"
        const val ROLE_ENTERTAINER = "ENTERTAINER"
    }
}
