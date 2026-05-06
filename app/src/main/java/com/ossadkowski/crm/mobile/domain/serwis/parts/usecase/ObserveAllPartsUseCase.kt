package com.ossadkowski.crm.mobile.domain.serwis.parts.usecase

import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.repository.PartsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllPartsUseCase @Inject constructor(
    private val repo: PartsRepository,
) {
    operator fun invoke(): Flow<List<PartRequest>> = repo.observeAll()
}
