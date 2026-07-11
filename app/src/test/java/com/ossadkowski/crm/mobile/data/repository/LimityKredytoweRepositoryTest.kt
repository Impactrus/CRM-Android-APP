package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.model.*
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
    @Mock lateinit var db: AppDatabase
    @Mock lateinit var actionQueue: ActionQueue
    private lateinit var repository: LimityKredytoweRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = LimityKredytoweRepository(apiService, db, actionQueue)
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

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals(1, result.data?.data?.size)
    }

    @Test
    fun `getList error`() = runTest {
        whenever(apiService.getLimityKredytowe(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(RuntimeException("Error"))

        val result = repository.getList(1, 10, null, null, null)

        assertTrue(result is NetworkResult.Error<*>)
    }

    @Test
    fun `getDetail success`() = runTest {
        val detail = LimitKredytowyDetailDto(
            id = 1,
            userId = 1,
            kontrahentAccountNum = "ACC1",
            kontrahentNazwa = "Firma",
            obecnyLimit = null,
            saldo = null,
            zamowione = null,
            pozostalyKredyt = null,
            wartoscZabezpieczen = null,
            nakladyPoprzedni = null,
            nakladyBiezacy = null,
            przychodyPoprzedni = null,
            przychodyBiezacy = null,
            zadluzeniePrzeterminowane = null,
            wnioskowanyLimit = 20000.0,
            terminZabezpieczen = null,
            opisZabezpieczen = null,
            noweZabezpieczenia = null,
            dodatkoweDochody = null,
            zobowiazania = null,
            uwagi = null,
            potwierdzonePrzeterminowane = false,
            rozliczeniePlonami = false,
            status = "Szkic",
            approvedBy = null,
            approvedAt = null,
            komentarzDecyzja = null,
            axSync = false,
            axDataSync = null,
            createdAt = "2026-01-01",
            updatedAt = null
        )
        whenever(apiService.getLimitKredytowyDetail(any())).thenReturn(detail)

        val result = repository.getDetail(1)

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `getDetail error`() = runTest {
        whenever(apiService.getLimitKredytowyDetail(any())).thenThrow(RuntimeException("Not found"))

        val result = repository.getDetail(99)

        assertTrue(result is NetworkResult.Error<*>)
    }

    @Test
    fun `create success`() = runTest {
        whenever(apiService.createLimitKredytowy(any())).thenReturn(Unit)

        val request = CreateLimitKredytowyRequest(userId = 1, kontrahentAccountNum = "ACC1", wnioskowanyLimit = 20000.0)
        val result = repository.create(request)

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `create error`() = runTest {
        whenever(apiService.createLimitKredytowy(any())).thenThrow(RuntimeException("Validation"))

        val request = CreateLimitKredytowyRequest(userId = 1, kontrahentAccountNum = "ACC1", wnioskowanyLimit = 20000.0)
        val result = repository.create(request)

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals("queued_offline", (result as NetworkResult.Success).data)
    }

    @Test
    fun `searchKontrahenci success`() = runTest {
        whenever(apiService.searchKontrahenci(any())).thenReturn(listOf(KontrahentSearchItem("ACC1", "Firma", "Adres", "1234567890")))

        val result = repository.searchKontrahenci("firma")

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `searchKontrahenci error`() = runTest {
        whenever(apiService.searchKontrahenci(any())).thenThrow(RuntimeException("Error"))

        val result = repository.searchKontrahenci("test")

        assertTrue(result is NetworkResult.Error<*>)
    }
}
