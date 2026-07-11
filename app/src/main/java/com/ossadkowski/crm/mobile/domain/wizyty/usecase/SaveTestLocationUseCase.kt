package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import javax.inject.Inject

/**
 * Saves a test location (name + a geocoded address) to be geofenced. Once a session is
 * (re)started the engine watches it, and arriving there auto-detects a visit + posts a
 * local notification.
 */
class SaveTestLocationUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    suspend operator fun invoke(name: String, address: AddressSuggestion): Result<Unit> =
        repo.saveContractorLocation(
            name = name,
            lat = address.lat,
            lng = address.lng,
            label = address.label,
        )
}
