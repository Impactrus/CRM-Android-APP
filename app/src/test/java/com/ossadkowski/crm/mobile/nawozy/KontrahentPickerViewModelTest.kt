package com.ossadkowski.crm.mobile.nawozy

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetLimitStatusUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.SearchKontrahenciUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.StartKoszykUseCase
import com.ossadkowski.crm.mobile.ui.nawozy.screens.kontrahent.KontrahentPickerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class KontrahentPickerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val customer = Kontrahent(accountNum = "ACC-1", nazwa = "Rolnik", isMyClient = true)

    private val search: SearchKontrahenciUseCase = mock {
        onBlocking { invoke(anyOrNull(), any()) }.thenReturn(Result.Success(listOf(customer)))
    }
    private val limit: GetLimitStatusUseCase = mock()
    private val start: StartKoszykUseCase = mock()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `selecting a customer loads its limit status`() = runTest {
        whenever(limit.invoke(any()))
            .thenReturn(Result.Success(LimitStatus(1000.0, 500.0, isFrozen = false, isBlocked = false, frozenReason = null)))
        val vm = KontrahentPickerViewModel(search, limit, start)
        advanceUntilIdle()

        vm.select(customer)
        advanceUntilIdle()

        assertEquals(customer, vm.state.value.selected)
        assertNotNull(vm.state.value.limitStatus)
    }

    @Test
    fun `startOrder signals navigation with the new koszykId`() = runTest {
        whenever(limit.invoke(any()))
            .thenReturn(Result.Success(LimitStatus(1000.0, 500.0, isFrozen = false, isBlocked = false, frozenReason = null)))
        whenever(start.invoke(any())).thenReturn(Result.Success(999L))
        val vm = KontrahentPickerViewModel(search, limit, start)
        advanceUntilIdle()

        vm.select(customer)
        advanceUntilIdle()
        vm.startOrder()
        advanceUntilIdle()

        assertEquals(999L, vm.state.value.startedKoszykId)
    }

    @Test
    fun `startOrder does nothing without a selected customer`() = runTest {
        val vm = KontrahentPickerViewModel(search, limit, start)
        advanceUntilIdle()

        vm.startOrder()
        advanceUntilIdle()

        assertTrue(vm.state.value.startedKoszykId == null)
    }
}
