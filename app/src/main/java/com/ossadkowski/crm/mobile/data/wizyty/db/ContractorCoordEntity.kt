package com.ossadkowski.crm.mobile.data.wizyty.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Locally-remembered coordinates for a contractor, used to register geofences.
 *
 * Contractor lat/lng is NOT live backend data yet — these rows are populated when a
 * rep attaches an address (via the TomTom geocode) to a manual visit. When the backend
 * exposes real `/kontrahenci` coordinates, [ContractorLocationSource] can swap its
 * source and this table becomes a cache/override.
 *
 * [key] is the contractor's `accountNum` when known, otherwise the contractor name —
 * it is also the geofence requestId.
 */
@Entity(tableName = "contractor_coords")
data class ContractorCoordEntity(
    @PrimaryKey val key: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val label: String?,
    val updatedAt: Instant,
)
