package com.ossadkowski.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "crm_session_encrypted",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "userId"
        private const val KEY_ROLE = "role"
        private const val KEY_USERNAME = "username"
        private const val KEY_DZIAL = "dzial"
        private const val KEY_EMPLOYEE_CACHE_ID = "employeeCacheId"
        private const val KEY_CLAIMS = "claims"
        private const val KEY_CLAIMS_VERSION = "claimsVersion"

        // Fallback claims per role (mirrors frontend useAuth.js)
        private val MANAGER_CLAIMS = setOf(
            "nav.pulpit", "nav.pracownicy", "nav.pracownicy.akceptacja",
            "nav.planer", "nav.planer.zadania", "nav.planer.wiadomosci", "nav.planer.kalendarz",
            "nav.transport", "nav.windykacja", "nav.windykacja.wnioski_o_limit", "nav.windykacja.nowy_wniosek",
            "nav.marketing", "nav.marketing.zadania", "nav.szkoleniowa",
            "nav.sprzedaz", "nav.sprzedaz.oferty", "nav.sprzedaz.zamowienia", "nav.sprzedaz.handlowcy",
            "nav.towar"
        )
        private val USER_CLAIMS = setOf(
            "nav.pulpit", "nav.planer", "nav.planer.zadania", "nav.planer.wiadomosci", "nav.planer.kalendarz",
            "nav.pracownicy"
        )
    }

    fun saveSession(
        token: String, userId: Int, role: String, username: String,
        dzial: String? = null, employeeCacheId: Int? = null,
        claims: Array<String>? = null, claimsVersion: Int? = null
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role)
            .putString(KEY_USERNAME, username)
            .putString(KEY_DZIAL, dzial ?: "")
            .putInt(KEY_EMPLOYEE_CACHE_ID, employeeCacheId ?: 0)
            .putStringSet(KEY_CLAIMS, claims?.toSet() ?: emptySet())
            .putInt(KEY_CLAIMS_VERSION, claimsVersion ?: 0)
            .apply()
    }

    fun updateToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    val token: String? get() = prefs.getString(KEY_TOKEN, null)
    val userId: Int get() = prefs.getInt(KEY_USER_ID, 0)
    val role: String get() = prefs.getString(KEY_ROLE, "User") ?: "User"
    val username: String get() = prefs.getString(KEY_USERNAME, "") ?: ""
    val dzial: String get() = prefs.getString(KEY_DZIAL, "") ?: ""
    val employeeCacheId: Int get() = prefs.getInt(KEY_EMPLOYEE_CACHE_ID, 0)

    val claims: Set<String> get() = prefs.getStringSet(KEY_CLAIMS, emptySet()) ?: emptySet()
    val claimsVersion: Int get() = prefs.getInt(KEY_CLAIMS_VERSION, 0)

    val isLoggedIn: Boolean get() = token != null

    fun hasClaim(code: String): Boolean {
        val savedClaims = claims
        if (savedClaims.isNotEmpty()) return savedClaims.contains(code)
        // Fallback by role if no claims saved
        return when (role) {
            "Admin" -> true
            "Manager", "HR" -> MANAGER_CLAIMS.contains(code)
            else -> USER_CLAIMS.contains(code)
        }
    }

    fun updateClaims(newClaims: Array<String>, version: Int) {
        prefs.edit()
            .putStringSet(KEY_CLAIMS, newClaims.toSet())
            .putInt(KEY_CLAIMS_VERSION, version)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
