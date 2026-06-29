package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTestLocationsUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    operator fun invoke(): Flow<List<ContractorLocation>> = repo.observeContractorLocations()
}
