package com.ossadkowski.crm.mobile.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestDao
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestEntity

/**
 * Shared Room database for the new feature stack. Distinct from the legacy
 * [com.ossadkowski.crm.mobile.data.cache.AppDatabase] (raw SQLite cache),
 * which we leave untouched.
 *
 * v1: only the offline parts table. Future features (work cards, time entries,
 * etc.) will add their own entities and bump the version with migrations.
 */
@Database(entities = [PartRequestEntity::class], version = 1, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class CrmDatabase : RoomDatabase() {
    abstract fun partRequestDao(): PartRequestDao
}
