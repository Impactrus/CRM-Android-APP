package com.ossadkowski.crm.mobile.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestDao
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestEntity
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordDao
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordEntity
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventDao
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventEntity

/**
 * Shared Room database for the new feature stack. Distinct from the legacy
 * [com.ossadkowski.crm.mobile.data.cache.AppDatabase] (raw SQLite cache),
 * which we leave untouched.
 *
 * v1: only the offline parts table.
 * v2: adds the Wizyty (GPS visit-detection) outbox (`visit_events`) — see
 * `AppModule.MIGRATION_1_2`.
 * v3: adds locally-attached contractor coordinates (`contractor_coords`) — see
 * `AppModule.MIGRATION_2_3`. Migrations must stay non-destructive: the same DB
 * holds unsynced parts requests and visits.
 */
@Database(
    entities = [PartRequestEntity::class, VisitEventEntity::class, ContractorCoordEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
abstract class CrmDatabase : RoomDatabase() {
    abstract fun partRequestDao(): PartRequestDao
    abstract fun visitEventDao(): VisitEventDao
    abstract fun contractorCoordDao(): ContractorCoordDao
}
