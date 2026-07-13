package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import javax.inject.Inject

/**
 * Address autocomplete for attaching a location to a manual visit. Mirrors the
 * transport map's debounce: queries shorter than 3 chars return empty without a
 * network round-trip.
 */
class SearchAddressUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    suspend operator fun invoke(query: String): Result<List<AddressSuggestion>> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return Result.Success(emptyList())
        return repo.searchAddress(trimmed)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}
