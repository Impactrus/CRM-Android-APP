package com.ossadkowski.crm.mobile.serwis

import com.ossadkowski.crm.mobile.data.serwis.dto.JobCardDto
import com.ossadkowski.crm.mobile.data.serwis.dto.JobCardLiteDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MachineDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MachineHistoryEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MyOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ServiceOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeEntryDto
import com.ossadkowski.crm.mobile.data.serwis.mapper.toDomain
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderStatus
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SerwisMappersTest {

    @Test
    fun `MyOrderDto null jobCards maps to empty list`() {
        val dto = MyOrderDto(
            orderRegNum = "MPE-000123",
            custAccount = "ACC-1",
            custName = "Test Customer",
            orderDate = "2026-04-30",
            orderType = 1,
            mpeOrderStatus = 0,
            jobCards = null
        )

        val domain = dto.toDomain()

        assertEquals("MPE-000123", domain.orderRegNum)
        assertEquals(LocalDate.of(2026, 4, 30), domain.orderDate)
        assertEquals(OrderStatus.OPEN, domain.status)
        assertTrue("jobCards null should map to empty list", domain.jobCards.isEmpty())
    }

    @Test
    fun `MyOrderDto with jobCards preserves technican typo field`() {
        val dto = MyOrderDto(
            orderRegNum = "MPE-000123",
            custAccount = null,
            custName = null,
            orderDate = null,
            orderType = null,
            mpeOrderStatus = 1,
            jobCards = listOf(JobCardLiteDto("MPE-000123/1", "JKOWALSKI"))
        )

        val domain = dto.toDomain()

        assertEquals(OrderStatus.IN_PROGRESS, domain.status)
        assertEquals(1, domain.jobCards.size)
        // CRITICAL: backend's `technican` typo flows through to domain field name `technican`.
        assertEquals("JKOWALSKI", domain.jobCards[0].technican)
        assertEquals("MPE-000123/1", domain.jobCards[0].mpeOrderJobCardNum)
    }

    @Test
    fun `JobCardDto closed=1 maps to isClosed=true and groups fuel levels`() {
        val dto = JobCardDto(
            mpeOrderJobCardNum = "MPE-000123/1",
            orderRegNum = "MPE-000123",
            cardNo = 1,
            technican = "JKOWALSKI",
            machineType = "Wózek widłowy",
            closed = 1,
            serviceType = "naprawa",
            reportedSymptoms = "Hałas",
            arrangements = "Wymienić łożysko",
            fixLocation = "U klienta",
            fuel0 = 0,
            fuel14 = 1,
            fuel12 = 0,
            fuel34 = 0,
            fuel44 = 0,
            remarks = "OK"
        )

        val domain = dto.toDomain()

        assertTrue(domain.isClosed)
        assertEquals(0, domain.fuel.zero)
        assertEquals(1, domain.fuel.q14)
        assertEquals(0, domain.fuel.q12)
        assertEquals(0, domain.fuel.q34)
        assertEquals(0, domain.fuel.full)
    }

    @Test
    fun `JobCardDto closed=0 maps to isClosed=false`() {
        val dto = JobCardDto(
            mpeOrderJobCardNum = "MPE-000123/1",
            orderRegNum = "MPE-000123",
            cardNo = 1,
            technican = null, machineType = null,
            closed = 0,
            serviceType = null, reportedSymptoms = null, arrangements = null,
            fixLocation = null,
            fuel0 = null, fuel14 = null, fuel12 = null, fuel34 = null, fuel44 = null,
            remarks = null
        )

        assertFalse(dto.toDomain().isClosed)
    }

    @Test
    fun `TimeEntryDto parses transDate to LocalDate and times to LocalTime`() {
        val dto = TimeEntryDto(
            id = 42L,
            mpeOrderJobCardNum = "MPE-000123/1",
            technican = "JKOWALSKI",
            transDate = "2026-04-30",
            timeBegin = "08:00",
            timeEnd = "16:30",
            kilometers = 124.5,
            travelToMinutes = 30,
            travelFromMinutes = 25
        )

        val domain = dto.toDomain()

        assertEquals(42L, domain.id)
        assertEquals(LocalDate.of(2026, 4, 30), domain.transDate)
        assertEquals(LocalTime.of(8, 0), domain.timeBegin)
        assertEquals(LocalTime.of(16, 30), domain.timeEnd)
        assertEquals(124.5, domain.kilometers!!, 0.0001)
        // Field name preservation check.
        assertEquals("JKOWALSKI", domain.technican)
    }

    @Test
    fun `TimeEntryDto handles HH-mm-ss seconds suffix`() {
        val dto = TimeEntryDto(
            id = null,
            mpeOrderJobCardNum = "MPE-000123/1",
            technican = null,
            transDate = "2026-04-30",
            timeBegin = "08:00:00",
            timeEnd = "16:30:00",
            kilometers = null,
            travelToMinutes = null,
            travelFromMinutes = null
        )

        val domain = dto.toDomain()

        assertEquals(LocalTime.of(8, 0), domain.timeBegin)
        assertEquals(LocalTime.of(16, 30), domain.timeEnd)
    }

    @Test
    fun `MachineDto warranty status=expired maps to WarrantyStatus EXPIRED`() {
        val dto = MachineDto(
            id = 7L,
            accountNum = "ACC-1",
            marka = "Toyota",
            model = "8FBE15",
            numerSeryjny = "SN-12345",
            typMaszyny = "Wózek widłowy",
            rokProdukcji = 2018,
            gwarancjaOd = "2018-01-01",
            gwarancjaDo = "2020-01-01",
            dataSprzedazy = "2018-01-15",
            nrRejestracyjny = null,
            itemId = null,
            itemName = null,
            zrodlo = null,
            uwagi = null,
            warrantyStatus = "expired",
            totalOrders = 3,
            openOrders = 0,
            serviceOrders = null
        )

        val domain = dto.toDomain()

        assertEquals(WarrantyStatus.EXPIRED, domain.warrantyStatus)
        assertEquals(LocalDate.of(2018, 1, 1), domain.gwarancjaOd)
        assertEquals(LocalDate.of(2020, 1, 1), domain.gwarancjaDo)
        assertTrue("null serviceOrders should map to empty list", domain.history.isEmpty())
    }

    @Test
    fun `MachineDto warranty status active and expiring_soon map correctly`() {
        val active = baseMachineDto().copy(warrantyStatus = "active")
        val soon = baseMachineDto().copy(warrantyStatus = "expiring_soon")
        val unknown = baseMachineDto().copy(warrantyStatus = null)

        assertEquals(WarrantyStatus.ACTIVE, active.toDomain().warrantyStatus)
        assertEquals(WarrantyStatus.EXPIRING_SOON, soon.toDomain().warrantyStatus)
        assertEquals(WarrantyStatus.UNKNOWN, unknown.toDomain().warrantyStatus)
    }

    @Test
    fun `MachineDto with serviceOrders maps history entries with status`() {
        val dto = baseMachineDto().copy(
            serviceOrders = listOf(
                MachineHistoryEntryDto(
                    orderRegNum = "MPE-000111",
                    orderDate = "2025-12-15",
                    orderType = 1,
                    mpeOrderStatus = 2,
                    reportedSymptoms = "Hałas",
                    serviceType = "naprawa",
                    isWarranty = false
                )
            )
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.history.size)
        assertEquals("MPE-000111", domain.history[0].orderRegNum)
        assertEquals(OrderStatus.CLOSED, domain.history[0].status)
        assertEquals(LocalDate.of(2025, 12, 15), domain.history[0].orderDate)
    }

    @Test
    fun `ServiceOrderDto parses scheduled ISO instant`() {
        val dto = ServiceOrderDto(
            orderRegNum = "MPE-000123",
            custAccount = null, custName = null, orderDate = null, orderType = null,
            mpeOrderStatus = 0, estimatedHours = 4.5, deadline = "2026-05-10",
            machineId = 99L, numerSeryjny = "SN-1", isWarranty = true,
            scheduledStart = "2026-05-02T08:00:00+02:00",
            scheduledEnd = "2026-05-02T16:00:00+02:00",
            jobCards = null
        )

        val domain = dto.toDomain()

        assertNotNull(domain.scheduledStart)
        assertNotNull(domain.scheduledEnd)
        assertEquals(LocalDate.of(2026, 5, 10), domain.deadline)
    }

    @Test
    fun `LocalDate parsing returns null for blank or malformed date`() {
        val dto = baseMachineDto().copy(gwarancjaOd = "", gwarancjaDo = "not-a-date")
        val domain = dto.toDomain()
        assertNull(domain.gwarancjaOd)
        assertNull(domain.gwarancjaDo)
    }

    @Test
    fun `OrderStatus fromCode unknown returns UNKNOWN`() {
        assertEquals(OrderStatus.UNKNOWN, OrderStatus.fromCode(null))
        assertEquals(OrderStatus.UNKNOWN, OrderStatus.fromCode(99))
        assertEquals(OrderStatus.OPEN, OrderStatus.fromCode(0))
        assertEquals(OrderStatus.IN_PROGRESS, OrderStatus.fromCode(1))
        assertEquals(OrderStatus.CLOSED, OrderStatus.fromCode(2))
    }

    private fun baseMachineDto() = MachineDto(
        id = 1L,
        accountNum = null, marka = null, model = null, numerSeryjny = null,
        typMaszyny = null, rokProdukcji = null,
        gwarancjaOd = null, gwarancjaDo = null, dataSprzedazy = null,
        nrRejestracyjny = null, itemId = null, itemName = null, zrodlo = null, uwagi = null,
        warrantyStatus = null, totalOrders = null, openOrders = null, serviceOrders = null
    )
}
