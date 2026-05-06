package com.ossadkowski.crm.mobile.data.device

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenProvider @Inject constructor(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun update(token: String) {
        prefs.edit().putString(KEY, token).apply()
    }

    fun current(): String? = prefs.getString(KEY, null)

    private companion object {
        const val PREFS = "crm_fcm_prefs"
        const val KEY = "fcm_token"
    }
}
