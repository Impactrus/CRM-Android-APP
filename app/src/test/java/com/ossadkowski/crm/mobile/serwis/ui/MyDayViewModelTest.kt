package com.ossadkowski.crm.mobile.serwis.ui

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderStatus
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMyOrdersUseCase
import com.ossadkowski.crm.mobile.ui.serwis.screens.today.MyDayUiState
import com.ossadkowski.crm.mobile.ui.serwis.screens.today.MyDayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MyDayViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Loading transitions to Success on Result_Success`() = runTest {
        val orders = listOf(
            MyOrder(
                orderRegNum = "MPE-000123",
                custAccount = "ACC-1",
                custName = "Test",
                orderDate = null,
                orderType = null,
                status = OrderStatus.OPEN,
                jobCards = emptyList(),
            ),
        )
        val useCase: GetMyOrdersUseCase = mock {
            onBlocking { invoke() }.thenReturn(Result.Success(orders))
        }
        val vm = MyDayViewModel(useCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MyDayUiState.Success)
        assertEquals(orders, (state as MyDayUiState.Success).orders)
    }

    @Test
    fun `Loading transitions to Error on Result_Error`() = runTest {
        val useCase: GetMyOrdersUseCase = mock {
            onBlocking { invoke() }.thenReturn(Result.Error("API down"))
        }
        val vm = MyDayViewModel(useCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MyDayUiState.Error)
        assertEquals("API down", (state as MyDayUiState.Error).message)
    }
}
