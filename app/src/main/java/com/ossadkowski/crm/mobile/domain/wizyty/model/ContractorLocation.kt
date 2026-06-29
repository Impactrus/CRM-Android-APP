package com.ossadkowski.crm.mobile.domain.wizyty.model

/**
 * A contractor location usable as a geofence target.
 *
 * [isLive] is false today — coordinates are demo / manually-attached, not from the
 * backend — which drives the "dane demonstracyjne" banner in the UI. [key] is the
 * stable identifier (accountNum or name) and the geofence requestId.
 */
data class ContractorLocation(
    val key: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val label: String?,
    val isLive: Boolean,
    val coordSource: CoordSource,
)

enum class CoordSource { PLACEHOLDER, USER_GEOCODED, BACKEND }
