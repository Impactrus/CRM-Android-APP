package com.ossadkowski.app.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.LoginResponse
import com.ossadkowski.app.data.repository.AuthRepository
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
class LoginViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: AuthRepository

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates loginResult with token`() = runTest {
        val response = LoginResponse(
            token = "abc123", userId = 1, role = "admin", username = "test",
            success = true, message = null, dzial = null, employeeCacheId = null,
            claims = null, claimsVersion = null
        )
        whenever(repository.login("user", "pass")).thenReturn(NetworkResult.Success(response))

        viewModel.login("user", "pass")
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result is NetworkResult.Success)
        assertEquals("abc123", (result as NetworkResult.Success).data?.token)
        assertEquals(1, result.data?.userId)
    }

    @Test
    fun `login failure updates loginResult with error`() = runTest {
        whenever(repository.login("user", "wrong")).thenReturn(NetworkResult.Error("Invalid credentials"))

        viewModel.login("user", "wrong")
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result is NetworkResult.Error)
        assertEquals("Invalid credentials", result?.message)
    }

    @Test
    fun `login sets loading state before result`() = runTest {
        whenever(repository.login("user", "pass")).thenReturn(NetworkResult.Success(
            LoginResponse("t", 1, "a", "u", true, null, null, null, null, null)
        ))

        viewModel.login("user", "pass")

        // Before advancing, the value should be Loading (set synchronously)
        val loadingResult = viewModel.loginResult.value
        assertTrue(loadingResult is NetworkResult.Loading)

        advanceUntilIdle()
        assertTrue(viewModel.loginResult.value is NetworkResult.Success)
    }

    @Test
    fun `login network error is handled`() = runTest {
        whenever(repository.login("user", "pass")).thenReturn(NetworkResult.Error("Network error"))

        viewModel.login("user", "pass")
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result is NetworkResult.Error)
        assertEquals("Network error", result?.message)
    }

    @Test
    fun `login with empty credentials still calls repository`() = runTest {
        whenever(repository.login("", "")).thenReturn(NetworkResult.Error("Username required"))

        viewModel.login("", "")
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `loginResult is initially null`() {
        assertNull(viewModel.loginResult.value)
    }
}
