package com.ossadkowski.crm.mobile.nawozy

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PagedZamowienia
import com.ossadkowski.crm.mobile.domain.nawozy.repository.ZamowieniaFilters
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.ListZamowieniaUseCase
import com.ossadkowski.crm.mobile.ui.nawozy.screens.lista.ZamowieniaListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ZamowieniaListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `init loads orders into success state`() = runTest {
        val useCase: ListZamowieniaUseCase = mock {
            onBlocking { invoke(any()) }.thenReturn(Result.Success(PagedZamowienia(emptyList(), 0)))
        }
        val vm = ZamowieniaListViewModel(useCase)
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.loading)
        assertNull(state.error)
    }

    @Test
    fun `setStatusFilter reloads with the chosen status`() = runTest {
        val useCase: ListZamowieniaUseCase = mock {
            onBlocking { invoke(any()) }.thenReturn(Result.Success(PagedZamowienia(emptyList(), 0)))
        }
        val vm = ZamowieniaListViewModel(useCase)
        advanceUntilIdle()

        vm.setStatusFilter(ZamowienieStatus.WYSLANY)
        advanceUntilIdle()

        assertEquals(ZamowienieStatus.WYSLANY, vm.state.value.statusFilter)
        verify(useCase).invoke(eq(ZamowieniaFilters(status = ZamowienieStatus.WYSLANY)))
    }

    @Test
    fun `error result surfaces message`() = runTest {
        val useCase: ListZamowieniaUseCase = mock {
            onBlocking { invoke(any()) }.thenReturn(Result.Error("Serwer padł"))
        }
        val vm = ZamowieniaListViewModel(useCase)
        advanceUntilIdle()

        assertEquals("Serwer padł", vm.state.value.error)
    }
}
