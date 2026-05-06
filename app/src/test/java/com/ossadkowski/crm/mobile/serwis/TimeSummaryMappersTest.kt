package com.ossadkowski.crm.mobile.serwis

import com.google.gson.Gson
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeSummaryDto
import com.ossadkowski.crm.mobile.data.serwis.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimeSummaryMappersTest {

    @Test
    fun `TimeSummaryDto round-trips from JSON to domain`() {
        val json = """
            {
              "technicianId": "j_kowalski",
              "totalHours": 32.5,
              "totalTravelHours": 4.0,
              "totalKilometers": 220.0,
              "entries": [
                {"date": "2026-04-30", "hours": 4.5, "travelHours": 1.0, "kilometers": 45.0},
                {"date": "2026-05-01", "hours": 8.0, "travelHours": 2.0, "kilometers": 90.5}
              ]
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, TimeSummaryDto::class.java)
        val domain = dto.toDomain()

        assertEquals("j_kowalski", domain.technicianId)
        assertEquals(32.5, domain.totalHours, 0.0001)
        assertEquals(4.0, domain.totalTravelHours, 0.0001)
        assertEquals(220.0, domain.totalKilometers, 0.0001)
        assertEquals(2, domain.entries.size)

        val first = domain.entries[0]
        assertEquals(LocalDate.of(2026, 4, 30), first.date)
        assertEquals(4.5, first.hours, 0.0001)
        assertEquals(1.0, first.travelHours, 0.0001)
        assertEquals(45.0, first.kilometers, 0.0001)

        val second = domain.entries[1]
        assertEquals(LocalDate.of(2026, 5, 1), second.date)
        assertEquals(90.5, second.kilometers, 0.0001)
    }

    @Test
    fun `entries with null or blank date are dropped`() {
        val json = """
            {
              "technicianId": "j_kowalski",
              "totalHours": 8.0,
              "totalTravelHours": 1.0,
              "totalKilometers": 30.0,
              "entries": [
                {"date": null, "hours": 4.0, "travelHours": 0.5, "kilometers": 10.0},
                {"date": "", "hours": 2.0, "travelHours": 0.5, "kilometers": 10.0},
                {"date": "2026-05-02", "hours": 2.0, "travelHours": 0.0, "kilometers": 10.0}
              ]
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, TimeSummaryDto::class.java)
        val domain = dto.toDomain()

        assertEquals(1, domain.entries.size)
        assertEquals(LocalDate.of(2026, 5, 2), domain.entries[0].date)
    }

    @Test
    fun `null doubles default to 0_0 and null entries to empty list`() {
        val json = """
            {
              "technicianId": null,
              "totalHours": null,
              "totalTravelHours": null,
              "totalKilometers": null,
              "entries": null
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, TimeSummaryDto::class.java)
        val domain = dto.toDomain()

        assertEquals(0.0, domain.totalHours, 0.0001)
        assertEquals(0.0, domain.totalTravelHours, 0.0001)
        assertEquals(0.0, domain.totalKilometers, 0.0001)
        assertTrue("null entries should map to empty list", domain.entries.isEmpty())
    }

    @Test
    fun `entry with null hours fields defaults each to 0_0 but date is preserved`() {
        val json = """
            {
              "technicianId": "tech",
              "totalHours": 0.0,
              "totalTravelHours": 0.0,
              "totalKilometers": 0.0,
              "entries": [
                {"date": "2026-05-03", "hours": null, "travelHours": null, "kilometers": null}
              ]
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, TimeSummaryDto::class.java)
        val domain = dto.toDomain()

        assertEquals(1, domain.entries.size)
        val entry = domain.entries[0]
        assertEquals(LocalDate.of(2026, 5, 3), entry.date)
        assertEquals(0.0, entry.hours, 0.0001)
        assertEquals(0.0, entry.travelHours, 0.0001)
        assertEquals(0.0, entry.kilometers, 0.0001)
    }
}
