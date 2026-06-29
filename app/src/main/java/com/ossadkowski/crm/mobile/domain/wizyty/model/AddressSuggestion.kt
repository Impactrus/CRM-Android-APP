package com.ossadkowski.crm.mobile.domain.wizyty.model

/**
 * A geocoded address candidate the rep can pick to attach a location to a visit.
 *
 * Sourced from the existing server-proxied TomTom search (`ApiService.searchGeocode`),
 * mapped into the domain so the use-case/UI layers don't depend on transport DTOs.
 */
data class AddressSuggestion(
    val label: String,
    val lat: Double,
    val lng: Double,
)
