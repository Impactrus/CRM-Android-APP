package com.ossadkowski.crm.mobile.domain.wizyty.repository

import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation

/**
 * Pluggable source of contractor coordinates for geofence registration.
 *
 * Today the only implementation reads locally-attached (non-live) coordinates. When the
 * backend exposes real `/kontrahenci` lat/lng, a new implementation is a one-line
 * `@Binds` swap and everything downstream keeps consuming [ContractorLocation.isLive].
 */
interface ContractorLocationSource {

    /** All known contractor locations (caller caps to the geofence limit). */
    suspend fun all(): List<ContractorLocation>

    suspend fun forKey(key: String): ContractorLocation?
}
