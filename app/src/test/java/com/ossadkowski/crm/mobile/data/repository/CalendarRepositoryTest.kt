package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.model.ZamrozenieDto
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
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CalendarRepositoryTest {

    @Mock lateinit var apiService: ApiService
    @Mock lateinit var db: AppDatabase
    private lateinit var repository: CalendarRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Pass a mock cache DB so the constructor doesn't resolve the default
        // RetrofitClient.cacheDb (which needs an initialized Android appContext).
        // With no cache entries stubbed, getValid/getAny return null → cache miss →
        // the network branch is exercised exactly as before.
        repository = CalendarRepository(apiService, db)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getZamrozeniaMiesiac success`() = runTest {
        val zamrozenia = listOf(ZamrozenieDto(1, "IT", "2026-03-01", "2026-03-15", "Sprint"))
        whenever(apiService.getZamrozeniaMiesiac(any(), any())).thenReturn(zamrozenia)

        val result = repository.getZamrozeniaMiesiac(2026, 3)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
    }

    @Test
    fun `getZamrozeniaMiesiac error`() = runTest {
        whenever(apiService.getZamrozeniaMiesiac(any(), any())).thenThrow(RuntimeException("Error"))

        val result = repository.getZamrozeniaMiesiac(2026, 3)

        assertTrue(result is NetworkResult.Error)
    }
}
