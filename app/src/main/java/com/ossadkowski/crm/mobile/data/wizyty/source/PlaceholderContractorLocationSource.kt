package com.ossadkowski.crm.mobile.data.wizyty.source

import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordDao
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordEntity
import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import com.ossadkowski.crm.mobile.domain.wizyty.model.CoordSource
import com.ossadkowski.crm.mobile.domain.wizyty.repository.ContractorLocationSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Non-live contractor-location source: reads coordinates the rep attached locally
 * (via the manual-add address search). Every result is flagged `isLive = false` so the
 * UI keeps showing the demo-data banner. Swap this binding for a backend-backed source
 * once `/kontrahenci` exposes real coordinates.
 */
@Singleton
class PlaceholderContractorLocationSource @Inject constructor(
    private val coordDao: ContractorCoordDao,
) : ContractorLocationSource {

    override suspend fun all(): List<ContractorLocation> =
        coordDao.getAll().map(ContractorCoordEntity::toContractorLocation)

    override suspend fun forKey(key: String): ContractorLocation? =
        coordDao.get(key)?.toContractorLocation()
}

private fun ContractorCoordEntity.toContractorLocation(): ContractorLocation =
    ContractorLocation(
        key = key,
        name = name,
        lat = lat,
        lng = lng,
        label = label,
        isLive = false,
        coordSource = CoordSource.USER_GEOCODED,
    )
