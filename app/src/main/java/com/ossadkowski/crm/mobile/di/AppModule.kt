package com.ossadkowski.crm.mobile.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.db.CrmDatabase
import com.ossadkowski.crm.mobile.data.device.DeviceIdProvider
import com.ossadkowski.crm.mobile.data.device.FcmTokenProvider
import com.ossadkowski.crm.mobile.data.serwis.api.ServiceOrderApi
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = RetrofitClient.apiService

    @Provides
    @Singleton
    fun provideServiceOrderApi(): ServiceOrderApi =
        RetrofitClient.retrofit.create(ServiceOrderApi::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx)

    @Provides
    @Singleton
    fun provideActionQueue(db: AppDatabase): ActionQueue = ActionQueue(db)

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext ctx: Context): SessionManager =
        SessionManager(ctx)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDeviceIdProvider(@ApplicationContext ctx: Context): DeviceIdProvider =
        DeviceIdProvider(ctx)

    @Provides
    @Singleton
    fun provideFcmTokenProvider(@ApplicationContext ctx: Context): FcmTokenProvider =
        FcmTokenProvider(ctx)

    // ── Room (new feature DB; legacy AppDatabase remains for cache/queue) ──

    @Provides
    @Singleton
    fun provideCrmDatabase(@ApplicationContext context: Context): CrmDatabase =
        Room.databaseBuilder(context, CrmDatabase::class.java, "crm-mobile.db")
            // No migrations needed at v1; future versions must add explicit migrations.
            .build()

    @Provides
    fun providePartRequestDao(db: CrmDatabase): PartRequestDao = db.partRequestDao()
}
