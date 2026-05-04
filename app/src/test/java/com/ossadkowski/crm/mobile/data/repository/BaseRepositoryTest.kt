package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
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
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class BaseRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    // Concrete subclass for testing
    private class TestRepository : BaseRepository() {
        suspend fun <T> callApi(block: suspend () -> T): NetworkResult<T> {
            return safeApiCall(block)
        }
    }

    private lateinit var repository: TestRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = TestRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `safeApiCall returns Success on successful call`() = runTest {
        val result = repository.callApi { "hello" }
        assertTrue(result is NetworkResult.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun `safeApiCall returns Error on exception`() = runTest {
        val result = repository.callApi<String> { throw RuntimeException("network error") }
        assertTrue(result is NetworkResult.Error)
        assertEquals("network error", result.message)
    }

    @Test
    fun `safeApiCall returns Error with Unknown Error for null message`() = runTest {
        val result = repository.callApi<String> { throw RuntimeException() }
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unknown Error", result.message)
    }

    @Test(expected = CancellationException::class)
    fun `safeApiCall rethrows CancellationException`() = runTest {
        repository.callApi<String> { throw CancellationException("cancelled") }
    }

    @Test
    fun `safeApiCall returns Success with complex object`() = runTest {
        data class TestData(val id: Int, val name: String)
        val result = repository.callApi { TestData(1, "test") }
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data!!.id)
        assertEquals("test", result.data!!.name)
    }

    @Test
    fun `safeApiCall returns Success with null value`() = runTest {
        val result = repository.callApi<String?> { null }
        assertTrue(result is NetworkResult.Success)
        // Success wraps null data — this is how the sealed class works
    }

    @Test
    fun `safeApiCall handles IOException`() = runTest {
        val result = repository.callApi<String> {
            throw java.io.IOException("timeout")
        }
        assertTrue(result is NetworkResult.Error)
        assertEquals("timeout", result.message)
    }
}
