package com.ossadkowski.crm.mobile.data.wizyty.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.fcm.NotificationChannelHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts a local notification when a visit is auto-detected at a geofenced location.
 * Uses the app's existing high-importance channel so it's noticeable; tapping it opens
 * the Wizyty screen to confirm/reject the visit.
 *
 * This is an on-device ("local") notification, not a server push — detection happens on
 * the device, so there is no backend round-trip to deliver an FCM message.
 *
 * The tap target comes from [WizytyContentIntentProvider] (implemented in the ui layer)
 * so this data-layer class never imports a concrete Activity.
 */
@Singleton
class WizytyNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentIntentProvider: WizytyContentIntentProvider,
) {
    fun notifyDetectedVisit(contractorName: String?) {
        if (!canPostNotifications()) return
        val name = contractorName?.takeIf { it.isNotBlank() } ?: "kontrahenta"

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntentProvider.contentIntent(),
            flags,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannelHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.crm_primary))
            .setContentTitle("Wykryto wizytę")
            .setContentText("Jesteś u: $name. Potwierdź wizytę w aplikacji.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Jesteś u: $name. Potwierdź lub odrzuć wizytę w aplikacji."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_BASE + (name.hashCode() and 0xFFFF), notification)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val NOTIF_ID_BASE = 5200
    }
}
