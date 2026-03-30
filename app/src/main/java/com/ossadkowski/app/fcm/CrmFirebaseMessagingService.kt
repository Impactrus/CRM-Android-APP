package com.ossadkowski.app.fcm

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ossadkowski.app.R
import com.ossadkowski.app.TaskDetailActivity
import com.ossadkowski.app.data.SessionManager
import com.ossadkowski.app.data.api.RetrofitClient
import kotlinx.coroutines.*

class CrmFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        val session = SessionManager(this)
        if (session.token != null) {
            scope.launch {
                try {
                    RetrofitClient.apiService.registerDeviceToken(DeviceTokenRequest(token))
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to register token", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "CRM Powiadomienie"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val entityType = message.data["entityType"]
        val entityId = message.data["entityId"]?.toIntOrNull()

        NotificationChannelHelper.createChannel(this)

        val intent = if (entityType == "task" && entityId != null) {
            Intent(this, TaskDetailActivity::class.java).apply {
                putExtra("TASK_ID", entityId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            packageManager.getLaunchIntentForPackage(packageName)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, entityId ?: 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannelHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(
                entityId ?: System.currentTimeMillis().toInt(),
                notification
            )
        } catch (e: SecurityException) {
            Log.w("FCM", "Notification permission not granted", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

data class DeviceTokenRequest(val token: String)
