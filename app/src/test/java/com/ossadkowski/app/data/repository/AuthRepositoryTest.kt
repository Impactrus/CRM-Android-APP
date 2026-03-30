package com.ossadkowski.app.data.repository

import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.model.LoginRequest
import com.ossadkowski.app.data.model.LoginResponse
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
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = AuthRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success returns Success with LoginResponse`() = runTest {
        val response = LoginResponse(
            token = "jwt-token", userId = 1, role = "admin", username = "testuser",
            success = true, message = null, dzial = "IT", employeeCacheId = 10,
            claims = arrayOf("read"), claimsVersion = 1
        )
        whenever(apiService.login(any())).thenReturn(response)

        val result = repository.login("testuser", "pass123")

        assertTrue(result is NetworkResult.Success)
        assertEquals("jwt-token", result.data?.token)
        assertEquals(1, result.data?.userId)
    }

    @Test
    fun `login failure returns Error`() = runTest {
        whenever(apiService.login(any())).thenThrow(RuntimeException("401 Unauthorized"))

        val result = repository.login("bad", "creds")

        assertTrue(result is NetworkResult.Error)
        assertEquals("401 Unauthorized", result.message)
    }

    @Test
    fun `login network error returns Error`() = runTest {
        whenever(apiService.login(any())).thenThrow(RuntimeException("Network error"))

        val result = repository.login("user", "pass")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Network error", result.message)
    }

    @Test
    fun `logout success returns Success`() = runTest {
        whenever(apiService.logout()).thenReturn(Response.success(null))

        val result = repository.logout()

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `logout network error still returns Success gracefully`() = runTest {
        whenever(apiService.logout()).thenThrow(RuntimeException("Network error"))

        val result = repository.logout()

        // logout catches exceptions internally, so safeApiCall wraps the Unit result
        assertTrue(result is NetworkResult.Success)
    }
}
