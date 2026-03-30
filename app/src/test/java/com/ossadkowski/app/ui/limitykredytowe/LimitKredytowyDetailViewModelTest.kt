package com.ossadkowski.app.ui.limitykredytowe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.LimitKredytowyDetailDto
import com.ossadkowski.app.data.repository.LimityKredytoweRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LimitKredytowyDetailViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: LimityKredytoweRepository

    private lateinit var viewModel: LimitKredytowyDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LimitKredytowyDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDetail updates detail LiveData`() = runTest {
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(LimitKredytowyDetailDto(id = 1, user_id = 1, kontrahent_account_num = "ACC1", kontrahent_nazwa = "Firma", obecny_limit = 10000.0, saldo = null, zamowione = null, pozostaly_kredyt = null, wartosc_zabezpieczen = null, naklady_poprzedni = null, naklady_biezacy = null, przychody_poprzedni = null, przychody_biezacy = null, zadluzenie_przeterminowane = null, wnioskowany_limit = 20000.0, termin_zabezpieczen = null, opis_zabezpieczen = null, nowe_zabezpieczenia = null, dodatkowe_dochody = null, zobowiazania = null, uwagi = null, potwierdzone_przeterminowane = false, rozliczenie_plonami = false, status = "Szkic", approved_by = null, approved_at = null, komentarz_decyzja = null, ax_sync = false, ax_data_sync = null, created_at = "2026-01-01", updated_at = null)))

        viewModel.loadDetail(1)
        advanceUntilIdle()

        assertTrue(viewModel.detail.value is NetworkResult.Success)
    }

    @Test
    fun `loadDetail sets loading state`() = runTest {
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(LimitKredytowyDetailDto(id = 1, user_id = 1, kontrahent_account_num = "ACC1", kontrahent_nazwa = "Firma", obecny_limit = 10000.0, saldo = null, zamowione = null, pozostaly_kredyt = null, wartosc_zabezpieczen = null, naklady_poprzedni = null, naklady_biezacy = null, przychody_poprzedni = null, przychody_biezacy = null, zadluzenie_przeterminowane = null, wnioskowany_limit = 20000.0, termin_zabezpieczen = null, opis_zabezpieczen = null, nowe_zabezpieczenia = null, dodatkowe_dochody = null, zobowiazania = null, uwagi = null, potwierdzone_przeterminowane = false, rozliczenie_plonami = false, status = "Szkic", approved_by = null, approved_at = null, komentarz_decyzja = null, ax_sync = false, ax_data_sync = null, created_at = "2026-01-01", updated_at = null)))

        viewModel.loadDetail(1)
        assertTrue(viewModel.detail.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `loadDetail error`() = runTest {
        whenever(repository.getDetail(999)).thenReturn(NetworkResult.Error("Not found"))

        viewModel.loadDetail(999)
        advanceUntilIdle()

        assertTrue(viewModel.detail.value is NetworkResult.Error)
        assertEquals("Not found", viewModel.detail.value?.message)
    }
}
