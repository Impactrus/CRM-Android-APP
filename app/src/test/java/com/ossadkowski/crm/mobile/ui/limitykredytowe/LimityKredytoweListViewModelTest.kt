package com.ossadkowski.crm.mobile.ui.limitykredytowe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.LimityKredytoweRepository
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LimityKredytoweListViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: LimityKredytoweRepository

    private lateinit var viewModel: LimityKredytoweListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LimityKredytoweListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load updates items LiveData`() = runTest {
        val item = LimitKredytowyListItem(1, 1, "ACC1", "Firma", 1000.0, 2000.0, "nowy", false, "2026-01-01", "admin")
        val response = GenericPageResponse(listOf(item), 1, 1, 20)
        whenever(repository.getList(1, 20, null, null, null)).thenReturn(NetworkResult.Success(response))

        viewModel.load()
        advanceUntilIdle()

        assertTrue(viewModel.items.value is NetworkResult.Success)
        assertEquals(1, (viewModel.items.value as NetworkResult.Success).data?.data?.size)
    }

    @Test
    fun `load sets loading state`() = runTest {
        whenever(repository.getList(1, 20, null, null, null)).thenReturn(
            NetworkResult.Success(GenericPageResponse(emptyList<LimitKredytowyListItem>(), 0, 1, 20))
        )

        viewModel.load()
        assertTrue(viewModel.items.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `load with status filter`() = runTest {
        viewModel.statusFilter = "zatwierdzony"
        whenever(repository.getList(1, 20, "zatwierdzony", null, null)).thenReturn(
            NetworkResult.Success(GenericPageResponse(emptyList<LimitKredytowyListItem>(), 0, 1, 20))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(1, 20, "zatwierdzony", null, null)
    }

    @Test
    fun `load with search`() = runTest {
        viewModel.search = "firma"
        whenever(repository.getList(1, 20, null, "firma", null)).thenReturn(
            NetworkResult.Success(GenericPageResponse(emptyList<LimitKredytowyListItem>(), 0, 1, 20))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(1, 20, null, "firma", null)
    }

    @Test
    fun `load with tab`() = runTest {
        viewModel.tab = "moje"
        whenever(repository.getList(1, 20, null, null, "moje")).thenReturn(
            NetworkResult.Success(GenericPageResponse(emptyList<LimitKredytowyListItem>(), 0, 1, 20))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(1, 20, null, null, "moje")
    }

    @Test
    fun `load with pagination`() = runTest {
        viewModel.page = 3
        whenever(repository.getList(3, 20, null, null, null)).thenReturn(
            NetworkResult.Success(GenericPageResponse(emptyList<LimitKredytowyListItem>(), 0, 3, 20))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(3, 20, null, null, null)
    }

    @Test
    fun `default state`() {
        assertEquals(1, viewModel.page)
        assertNull(viewModel.search)
        assertNull(viewModel.statusFilter)
        assertNull(viewModel.tab)
    }
}
