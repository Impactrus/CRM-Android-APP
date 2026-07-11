package com.ossadkowski.crm.mobile.data.wizyty.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.ossadkowski.crm.mobile.R
import dagger.hilt.android.EntryPointAccessors

/**
 * Foreground service for an active work session. Runs ONLY while the rep has a session
 * on, so there is zero background cost otherwise. Shows a persistent (transparency)
 * notification and keeps a balanced, batched location stream warm for precise dwell
 * confirmation. Auto-detection itself runs via geofences, which work independently.
 */
class VisitDetectionForegroundService : Service() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // Session location kept warm; precise dwell confirmation lands in a later pass.
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
        requestLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission") // runtime-checked below
    private fun requestLocationUpdates() {
        if (!LocationPermissions.hasForegroundLocation(this)) return
        val request = LocationRequest.Builder(
            DetectionTuning.SESSION_LOCATION_PRIORITY,
            DetectionTuning.SESSION_LOCATION_INTERVAL_MS,
        ).setMaxUpdateDelayMillis(DetectionTuning.SESSION_LOCATION_MAX_DELAY_MS).build()
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sesja wizyt",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Powiadomienie aktywnej sesji wykrywania wizyt" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val provider = EntryPointAccessors.fromApplication(
            applicationContext,
            WizytyEngineEntryPoint::class.java,
        ).wizytyContentIntentProvider()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            provider.contentIntent(),
            flags,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Wizyty: sesja aktywna")
            .setContentText("Rejestrujemy odwiedziny u kontrahentów. Dotknij, aby zarządzać.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "crm_visits"
        const val NOTIF_ID = 4201
    }
}
