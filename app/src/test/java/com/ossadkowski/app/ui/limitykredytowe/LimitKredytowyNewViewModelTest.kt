package com.ossadkowski.app.ui.limitykredytowe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
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
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LimitKredytowyNewViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: LimityKredytoweRepository

    private lateinit var viewModel: LimitKredytowyNewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LimitKredytowyNewViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchKontrahenci updates kontrahenci LiveData`() = runTest {
        whenever(repository.searchKontrahenci("firma")).thenReturn(NetworkResult.Success(listOf("result")))

        viewModel.searchKontrahenci("firma")
        advanceUntilIdle()

        assertTrue(viewModel.kontrahenci.value is NetworkResult.Success)
    }

    @Test
    fun `searchKontrahenci error`() = runTest {
        whenever(repository.searchKontrahenci("x")).thenReturn(NetworkResult.Error("Error"))

        viewModel.searchKontrahenci("x")
        advanceUntilIdle()

        assertTrue(viewModel.kontrahenci.value is NetworkResult.Error)
    }

    @Test
    fun `create success updates createResult`() = runTest {
        val request = CreateLimitKredytowyRequest(1, "ACC1", 5000.0)
        whenever(repository.create(any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.create(request)
        advanceUntilIdle()

        assertTrue(viewModel.createResult.value is NetworkResult.Success)
    }

    @Test
    fun `create sets loading state`() = runTest {
        val request = CreateLimitKredytowyRequest(1, "ACC1", 5000.0)
        whenever(repository.create(any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.create(request)
        assertTrue(viewModel.createResult.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `create failure`() = runTest {
        val request = CreateLimitKredytowyRequest(1, "ACC1", 5000.0)
        whenever(repository.create(any())).thenReturn(NetworkResult.Error("Validation error"))

        viewModel.create(request)
        advanceUntilIdle()

        assertTrue(viewModel.createResult.value is NetworkResult.Error)
    }
}
