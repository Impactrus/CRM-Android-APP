package com.ossadkowski.app.ui.approval

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.ApprovalRepository
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
class ApprovalViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: ApprovalRepository

    private lateinit var viewModel: ApprovalViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ApprovalViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadApprovals updates approvals LiveData`() = runTest {
        val response = PaginatedResponse(
            items = listOf(WniosekItem(1, "user", "Urlop", "01-05", null, "r", "1", "pending")),
            totalCount = 1, totalPages = 1
        )
        whenever(repository.getApprovals(1, 1, 10, null)).thenReturn(NetworkResult.Success(response))

        viewModel.loadApprovals(1)
        advanceUntilIdle()

        assertTrue(viewModel.approvals.value is NetworkResult.Success)
        assertEquals(1, (viewModel.approvals.value as NetworkResult.Success).data?.items?.size)
    }

    @Test
    fun `loadApprovals sets loading state`() = runTest {
        whenever(repository.getApprovals(1, 1, 10, null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<WniosekItem>(), 0, 0))
        )

        viewModel.loadApprovals(1)
        assertTrue(viewModel.approvals.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `loadApprovals uses search filter`() = runTest {
        viewModel.search = "test"
        whenever(repository.getApprovals(1, 1, 10, "test")).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<WniosekItem>(), 0, 0))
        )

        viewModel.loadApprovals(1)
        advanceUntilIdle()

        verify(repository).getApprovals(1, 1, 10, "test")
    }

    @Test
    fun `loadApprovals uses pagination`() = runTest {
        viewModel.page = 3
        whenever(repository.getApprovals(1, 3, 10, null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<WniosekItem>(), 0, 0))
        )

        viewModel.loadApprovals(1)
        advanceUntilIdle()

        verify(repository).getApprovals(1, 3, 10, null)
    }

    @Test
    fun `approve as Manager calls approveManager with true`() = runTest {
        whenever(repository.approveManager(1, 2, true)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.approve(1, 2, "Manager") { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
        verify(repository).approveManager(1, 2, true)
    }

    @Test
    fun `approve as HR calls approveHr with true`() = runTest {
        whenever(repository.approveHr(1, 2, true)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.approve(1, 2, "HR") { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
        verify(repository).approveHr(1, 2, true)
    }

    @Test
    fun `reject as Manager calls approveManager with false`() = runTest {
        whenever(repository.approveManager(1, 2, false)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.reject(1, 2, "Manager") { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
        verify(repository).approveManager(1, 2, false)
    }

    @Test
    fun `reject as HR calls approveHr with false`() = runTest {
        whenever(repository.approveHr(1, 2, false)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.reject(1, 2, "HR") { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
    }

    @Test
    fun `approve failure calls onResult with false`() = runTest {
        whenever(repository.approveManager(1, 2, true)).thenReturn(NetworkResult.Error("fail"))
        var result: Boolean? = null

        viewModel.approve(1, 2, "Manager") { result = it }
        advanceUntilIdle()

        assertTrue(result == false)
    }

    @Test
    fun `default state`() {
        assertEquals(1, viewModel.page)
        assertNull(viewModel.search)
    }
}
