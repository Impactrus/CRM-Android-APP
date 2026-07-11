package com.ossadkowski.crm.mobile.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.db.CrmDatabase
import com.ossadkowski.crm.mobile.data.device.DeviceIdProvider
import com.ossadkowski.crm.mobile.data.device.FcmTokenProvider
import com.ossadkowski.crm.mobile.data.nawozy.api.NawozyApi
import com.ossadkowski.crm.mobile.data.serwis.api.ServiceOrderApi
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestDao
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordDao
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventDao
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
    fun provideNawozyApi(): NawozyApi =
        RetrofitClient.retrofit.create(NawozyApi::class.java)

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

    /**
     * v1 → v2: add the Wizyty outbox (`visit_events`). Non-destructive so the
     * existing parts outbox survives. DDL must match Room's generated schema.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `visit_events` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`contractorAccountNum` TEXT, " +
                    "`contractorName` TEXT, " +
                    "`addressLabel` TEXT, " +
                    "`lat` REAL, " +
                    "`lng` REAL, " +
                    "`eventType` TEXT NOT NULL, " +
                    "`source` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, " +
                    "`syncStatus` TEXT NOT NULL, " +
                    "`occurredAt` INTEGER NOT NULL, " +
                    "`dwellSeconds` INTEGER, " +
                    "`idempotencyKey` TEXT NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_visit_events_idempotencyKey` " +
                    "ON `visit_events` (`idempotencyKey`)"
            )
        }
    }

    /** v2 → v3: add locally-attached contractor coordinates. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `contractor_coords` (" +
                    "`key` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`lat` REAL NOT NULL, " +
                    "`lng` REAL NOT NULL, " +
                    "`label` TEXT, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`key`))"
            )
        }
    }

    @Provides
    @Singleton
    fun provideCrmDatabase(@ApplicationContext context: Context): CrmDatabase =
        Room.databaseBuilder(context, CrmDatabase::class.java, "crm-mobile.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun providePartRequestDao(db: CrmDatabase): PartRequestDao = db.partRequestDao()

    @Provides
    fun provideVisitEventDao(db: CrmDatabase): VisitEventDao = db.visitEventDao()

    @Provides
    fun provideContractorCoordDao(db: CrmDatabase): ContractorCoordDao = db.contractorCoordDao()
}
