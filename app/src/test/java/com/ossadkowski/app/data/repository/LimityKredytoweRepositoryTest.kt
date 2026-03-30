package com.ossadkowski.app.data.repository

import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LimityKredytoweRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: LimityKredytoweRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = LimityKredytoweRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getList success with filters`() = runTest {
        val item = LimitKredytowyListItem(1, 1, "ACC1", "Firma", 10000.0, 20000.0, "nowy", false, "2026-01-01", "user")
        val response = GenericPageResponse(data = listOf(item), total = 1, page = 1, pageSize = 10)
        whenever(apiService.getLimityKredytowe(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(response)

        val result = repository.getList(1, 10, "nowy", "firma", "moje")

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.data?.size)
    }

    @Test
    fun `getList error`() = runTest {
        whenever(apiService.getLimityKredytowe(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(RuntimeException("Error"))

        val result = repository.getList(1, 10, null, null, null)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getDetail success`() = runTest {
        val detail = LimitKredytowyDetailDto(id = 1, user_id = 1, kontrahent_account_num = "ACC1", kontrahent_nazwa = "Firma", obecny_limit = null, saldo = null, zamowione = null, pozostaly_kredyt = null, wartosc_zabezpieczen = null, naklady_poprzedni = null, naklady_biezacy = null, przychody_poprzedni = null, przychody_biezacy = null, zadluzenie_przeterminowane = null, wnioskowany_limit = 20000.0, termin_zabezpieczen = null, opis_zabezpieczen = null, nowe_zabezpieczenia = null, dodatkowe_dochody = null, zobowiazania = null, uwagi = null, potwierdzone_przeterminowane = false, rozliczenie_plonami = false, status = "Szkic", approved_by = null, approved_at = null, komentarz_decyzja = null, ax_sync = false, ax_data_sync = null, created_at = "2026-01-01", updated_at = null)
        whenever(apiService.getLimitKredytowyDetail(any())).thenReturn(detail)

        val result = repository.getDetail(1)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `getDetail error`() = runTest {
        whenever(apiService.getLimitKredytowyDetail(any())).thenThrow(RuntimeException("Not found"))

        val result = repository.getDetail(99)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `create success`() = runTest {
        whenever(apiService.createLimitKredytowy(any())).thenReturn(Unit)

        val request = CreateLimitKredytowyRequest(userId = 1, kontrahentAccountNum = "ACC1", wnioskowanyLimit = 20000.0)
        val result = repository.create(request)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `create error`() = runTest {
        whenever(apiService.createLimitKredytowy(any())).thenThrow(RuntimeException("Validation"))

        val request = CreateLimitKredytowyRequest(userId = 1, kontrahentAccountNum = "ACC1", wnioskowanyLimit = 20000.0)
        val result = repository.create(request)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `searchKontrahenci success`() = runTest {
        whenever(apiService.searchKontrahenci(anyOrNull(), any(), any())).thenReturn(listOf<Any>())

        val result = repository.searchKontrahenci("firma")

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `searchKontrahenci error`() = runTest {
        whenever(apiService.searchKontrahenci(anyOrNull(), any(), any())).thenThrow(RuntimeException("Error"))

        val result = repository.searchKontrahenci("test")

        assertTrue(result is NetworkResult.Error)
    }
}
