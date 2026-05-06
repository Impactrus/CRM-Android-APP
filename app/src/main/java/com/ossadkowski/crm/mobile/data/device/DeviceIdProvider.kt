package com.ossadkowski.crm.mobile.data.device

import android.content.Context
import android.os.Build
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun deviceId(): String {
        prefs.getString(KEY, null)?.let { return it }
        val fresh = UUID.randomUUID().toString()
        prefs.edit().putString(KEY, fresh).apply()
        return fresh
    }

    fun label(): String =
        listOf(Build.MANUFACTURER.orEmpty(), Build.MODEL.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Android Device" }

    fun platform(): String = "android"

    private companion object {
        const val PREFS = "crm_device_prefs"
        const val KEY = "device_id"
    }
}
