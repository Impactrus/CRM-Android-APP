package com.ossadkowski.crm.mobile.wizyty

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.SearchAddressUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SearchAddressUseCaseTest {

    @Mock lateinit var repo: VisitRepository
    private lateinit var useCase: SearchAddressUseCase

    @Before
    fun setUp() {
        useCase = SearchAddressUseCase(repo)
    }

    @Test
    fun `query shorter than 3 chars returns empty without hitting the repository`() = runTest {
        val result = useCase("ab")

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
        verifyNoInteractions(repo)
    }

    @Test
    fun `whitespace padding is trimmed before the length check`() = runTest {
        val result = useCase("  ab  ")

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
        verifyNoInteractions(repo)
    }

    @Test
    fun `query of 3 or more chars delegates to the repository with a trimmed query`() = runTest {
        whenever(repo.searchAddress("abc"))
            .thenReturn(Result.Success(listOf(AddressSuggestion("Label", 1.0, 2.0))))

        val result = useCase("  abc ")

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        verify(repo).searchAddress("abc")
    }
}
