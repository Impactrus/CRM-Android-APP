package com.ossadkowski.crm.mobile.serwis

import com.ossadkowski.crm.mobile.data.serwis.api.ServiceOrderApi
import com.ossadkowski.crm.mobile.data.serwis.dto.MyOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ServiceOrderDto
import com.ossadkowski.crm.mobile.data.serwis.repository.SerwisRepositoryImpl
import com.ossadkowski.crm.mobile.domain.common.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SerwisRepositoryImplTest {

    @Mock lateinit var api: ServiceOrderApi
    private lateinit var repo: SerwisRepositoryImpl

    @Before
    fun setUp() {
        repo = SerwisRepositoryImpl(api)
    }

    @Test
    fun `getMyOrders success returns Success with mapped list`() = runTest {
        whenever(api.getMyOrders()).thenReturn(
            listOf(
                MyOrderDto(
                    orderRegNum = "MPE-000123",
                    custAccount = "ACC-1",
                    custName = "Customer",
                    orderDate = "2026-04-30",
                    orderType = 1,
                    mpeOrderStatus = 0,
                    jobCards = null
                )
            )
        )

        val result = repo.getMyOrders()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("MPE-000123", data[0].orderRegNum)
    }

    @Test
    fun `403 Forbidden returns 'Brak uprawnień' error`() = runTest {
        val errorResponse = Response.error<Any>(
            403,
            "{}".toResponseBody("application/json".toMediaType())
        )
        whenever(api.getMyOrders()).thenAnswer { throw HttpException(errorResponse) }

        val result = repo.getMyOrders()

        assertTrue(result is Result.Error)
        assertEquals("Brak uprawnień.", (result as Result.Error).message)
    }

    @Test
    fun `404 with message body surfaces backend message`() = runTest {
        val errorResponse = Response.error<Any>(
            404,
            "{\"message\":\"Zlecenie nie istnieje.\"}".toResponseBody("application/json".toMediaType())
        )
        whenever(api.getOrder(eq("MPE-999"))).thenAnswer { throw HttpException(errorResponse) }

        val result = repo.getOrder("MPE-999")

        assertTrue(result is Result.Error)
        assertEquals("Zlecenie nie istnieje.", (result as Result.Error).message)
    }

    @Test
    fun `404 with error key in body surfaces backend error`() = runTest {
        val errorResponse = Response.error<Any>(
            404,
            "{\"error\":\"Karta nie istnieje.\"}".toResponseBody("application/json".toMediaType())
        )
        whenever(api.getOrder(any())).thenAnswer { throw HttpException(errorResponse) }

        val result = repo.getOrder("MPE-X")

        assertTrue(result is Result.Error)
        assertEquals("Karta nie istnieje.", (result as Result.Error).message)
    }

    @Test
    fun `IOException maps to network connection error message`() = runTest {
        whenever(api.getMyOrders()).thenAnswer { throw IOException("offline") }

        val result = repo.getMyOrders()

        assertTrue(result is Result.Error)
        assertEquals("Brak połączenia z serwerem.", (result as Result.Error).message)
    }

    @Test
    fun `unexpected exception falls back to generic error`() = runTest {
        whenever(api.getMyOrders()).thenAnswer { throw RuntimeException("oops") }

        val result = repo.getMyOrders()

        assertTrue(result is Result.Error)
        assertEquals("Nieoczekiwany błąd.", (result as Result.Error).message)
    }

    @Test
    fun `listOrders reads X-Total-Count header into PagedOrders totalCount`() = runTest {
        val items = listOf(
            ServiceOrderDto(
                orderRegNum = "MPE-1",
                custAccount = null, custName = null, orderDate = null, orderType = null,
                mpeOrderStatus = 0, estimatedHours = null, deadline = null,
                machineId = null, numerSeryjny = null, isWarranty = null,
                scheduledStart = null, scheduledEnd = null, jobCards = null
            )
        )
        val response = Response.success(
            items,
            okhttp3.Headers.headersOf("X-Total-Count", "142")
        )
        whenever(
            api.listOrders(
                status = isNull(),
                custAccount = isNull(),
                dateFrom = isNull(),
                dateTo = isNull(),
                missingDeadline = isNull(),
                pageNumber = eq(1),
                pageSize = eq(50)
            )
        ).thenReturn(response)

        val result = repo.listOrders()

        assertTrue(result is Result.Success)
        val paged = (result as Result.Success).data
        assertEquals(142, paged.totalCount)
        assertEquals(1, paged.items.size)
    }

    @Test
    fun `listOrders missing X-Total-Count defaults to 0`() = runTest {
        val response = Response.success<List<ServiceOrderDto>>(emptyList())
        whenever(
            api.listOrders(
                status = isNull(),
                custAccount = isNull(),
                dateFrom = isNull(),
                dateTo = isNull(),
                missingDeadline = isNull(),
                pageNumber = eq(1),
                pageSize = eq(50)
            )
        ).thenReturn(response)

        val result = repo.listOrders()

        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.totalCount)
    }
}
