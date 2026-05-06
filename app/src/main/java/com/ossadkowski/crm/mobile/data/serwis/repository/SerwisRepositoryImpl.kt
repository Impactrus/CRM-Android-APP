package com.ossadkowski.crm.mobile.data.serwis.repository

import com.google.gson.JsonParser
import com.ossadkowski.crm.mobile.data.serwis.api.ServiceOrderApi
import com.ossadkowski.crm.mobile.data.serwis.mapper.toDomain
import com.ossadkowski.crm.mobile.data.serwis.mapper.toRequest
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Activity
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCard
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderLogEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.Schedule
import com.ossadkowski.crm.mobile.domain.serwis.model.ServiceOrderSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.Technician
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyCheck
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewActivity
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewTimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.repository.OrderFilters
import com.ossadkowski.crm.mobile.domain.serwis.repository.OrderWithCards
import com.ossadkowski.crm.mobile.domain.serwis.repository.PagedOrders
import com.ossadkowski.crm.mobile.domain.serwis.repository.ScheduleFilters
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SerwisRepositoryImpl @Inject constructor(
    private val api: ServiceOrderApi
) : SerwisRepository {

    override suspend fun getMyOrders(): Result<List<MyOrder>> = call {
        api.getMyOrders().map { it.toDomain() }
    }

    override suspend fun listOrders(filters: OrderFilters): Result<PagedOrders> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.listOrders(
                    status = filters.status?.code,
                    custAccount = filters.custAccount,
                    dateFrom = filters.dateFrom?.toString(),
                    dateTo = filters.dateTo?.toString(),
                    missingDeadline = filters.missingDeadline,
                    pageNumber = filters.pageNumber,
                    pageSize = filters.pageSize
                )
                if (!response.isSuccessful) {
                    Result.Error(parseHttpError(HttpException(response)), HttpException(response))
                } else {
                    val items = response.body().orEmpty().map { it.toDomain() }
                    val total = response.headers()["X-Total-Count"]?.toIntOrNull() ?: 0
                    Result.Success(PagedOrders(items, total))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Result.Error(parseHttpError(e), e)
            } catch (e: IOException) {
                Result.Error("Brak połączenia z serwerem.", e)
            } catch (e: Exception) {
                Result.Error("Nieoczekiwany błąd.", e)
            }
        }

    override suspend fun getOrder(orderNum: String): Result<ServiceOrderSummary> = call {
        api.getOrder(orderNum).toDomain()
    }

    override suspend fun getOrderWithCards(orderNum: String): Result<OrderWithCards> =
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val orderD = async { api.getOrder(orderNum) }
                    val cardsD = async { api.getJobCards(orderNum) }
                    val logD = async { api.getOrderLog(orderNum) }
                    Result.Success(
                        OrderWithCards(
                            order = orderD.await().toDomain(),
                            jobCards = cardsD.await().map { it.toDomain() },
                            log = logD.await().map { it.toDomain() }
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Result.Error(parseHttpError(e), e)
            } catch (e: IOException) {
                Result.Error("Brak połączenia z serwerem.", e)
            } catch (e: Exception) {
                Result.Error("Nieoczekiwany błąd.", e)
            }
        }

    override suspend fun getJobCards(orderNum: String): Result<List<JobCard>> = call {
        api.getJobCards(orderNum).map { it.toDomain() }
    }

    override suspend fun getJobCard(orderNum: String, cardNum: String): Result<JobCard> = call {
        api.getJobCard(orderNum, cardNum).toDomain()
    }

    override suspend fun getOrderLog(orderNum: String): Result<List<OrderLogEntry>> = call {
        api.getOrderLog(orderNum).map { it.toDomain() }
    }

    override suspend fun getTimeEntries(cardNum: String): Result<List<TimeEntry>> = call {
        api.getTimeEntries(cardNum).map { it.toDomain() }
    }

    override suspend fun addTimeEntry(cardNum: String, req: NewTimeEntry): Result<TimeEntry> = call {
        api.addTimeEntry(cardNum, req.toRequest()).toDomain()
    }

    override suspend fun getActivities(cardNum: String): Result<List<Activity>> = call {
        api.getActivities(cardNum).map { it.toDomain() }
    }

    override suspend fun addActivity(cardNum: String, req: NewActivity): Result<Activity> = call {
        api.addActivity(cardNum, req.toRequest()).toDomain()
    }

    override suspend fun getOrderTechnicians(orderNum: String): Result<List<Technician>> = call {
        api.getOrderTechnicians(orderNum).map { it.toDomain() }
    }

    override suspend fun getOrderMachine(orderNum: String): Result<Machine> = call {
        api.getOrderMachine(orderNum).toDomain()
    }

    override suspend fun getMachineBySerial(serial: String, accountNum: String?): Result<Machine> = call {
        api.getMachineBySerial(serial, accountNum).toDomain()
    }

    override suspend fun getCustomerMachines(accountNum: String?): Result<List<Machine>> = call {
        api.getCustomerMachines(accountNum).map { it.toDomain() }
    }

    override suspend fun checkWarranty(machineId: Long): Result<WarrantyCheck> = call {
        api.checkWarranty(machineId).toDomain()
    }

    override suspend fun getSchedules(filters: ScheduleFilters): Result<List<Schedule>> = call {
        api.getSchedules(
            accountNum = filters.accountNum,
            status = filters.status,
            monthsAhead = filters.monthsAhead,
            machineId = filters.machineId
        ).map { it.toDomain() }
    }

    override suspend fun getMyTimeSummary(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Result<TimeSummary> = call {
        // Backend scopes the result to the JWT user when technicianId is null,
        // so we always omit the param. Dates serialise as ISO yyyy-MM-dd.
        api.getMyTimeSummary(
            technicianId = null,
            dateFrom = dateFrom.toString(),
            dateTo = dateTo.toString()
        ).toDomain()
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Result.Error(parseHttpError(e), e)
            } catch (e: IOException) {
                Result.Error("Brak połączenia z serwerem.", e)
            } catch (e: Exception) {
                Result.Error("Nieoczekiwany błąd.", e)
            }
        }

    private fun parseHttpError(e: HttpException): String = when (e.code()) {
        403 -> "Brak uprawnień."
        404 -> readBackendMessage(e) ?: "Nie znaleziono zasobu."
        in 400..499 -> readBackendMessage(e) ?: "Błąd serwera (${e.code()})."
        else -> "Błąd serwera (${e.code()})."
    }

    private fun readBackendMessage(e: HttpException): String? {
        val body = try {
            e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: return null
        return try {
            val json = JsonParser.parseString(body)
            if (!json.isJsonObject) return null
            val obj = json.asJsonObject
            when {
                obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
                obj.has("error") && !obj.get("error").isJsonNull -> obj.get("error").asString
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
