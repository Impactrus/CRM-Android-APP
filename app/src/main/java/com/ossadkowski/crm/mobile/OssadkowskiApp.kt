package com.ossadkowski.crm.mobile

import android.app.Application
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.fcm.NotificationChannelHelper

class OssadkowskiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize core services at startup to avoid UninitializedPropertyAccessException
        RetrofitClient.init(this)
        NotificationChannelHelper.createChannel(this)
    }
}
