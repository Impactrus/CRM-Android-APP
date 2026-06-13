package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.NetworkResult
import retrofit2.Response

class VacationRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getMyVacationPlan(year: Int): NetworkResult<VacationPlanDto> =
        safeApiCall { apiService.getMyVacationPlan(year) }

    suspend fun saveVacationPlanBulk(year: Int, addDates: List<String>, removeDates: List<String>): NetworkResult<Unit> =
        safeApiCall {
            val response = apiService.saveVacationPlanBulk(year, BulkSaveVacationRequest(year, addDates, removeDates))
            if (response.isSuccessful) Unit else throw Exception("API Error: ${response.code()}")
        }

    suspend fun submitVacationPlan(year: Int): NetworkResult<Unit> =
        safeApiCall {
            val response = apiService.submitVacationPlan(year)
            if (response.isSuccessful) Unit else throw Exception("API Error: ${response.code()}")
        }

    suspend fun revokeVacationPlan(year: Int): NetworkResult<Unit> =
        safeApiCall {
            val response = apiService.revokeVacationPlan(year)
            if (response.isSuccessful) Unit else throw Exception("API Error: ${response.code()}")
        }

    suspend fun clearVacationPlan(year: Int): NetworkResult<Unit> =
        safeApiCall {
            val response = apiService.clearVacationPlan(year)
            if (response.isSuccessful) Unit else throw Exception("API Error: ${response.code()}")
        }

    suspend fun getVacationSubmission(year: Int): NetworkResult<VacationSubmissionDto> =
        safeApiCall { apiService.getVacationSubmission(year) }

    suspend fun getTeamVacationPlans(year: Int): NetworkResult<List<TeamEmployeePlanDto>> =
        safeApiCall { apiService.getTeamVacationPlans(year) }

    suspend fun getPendingVacationPlans(): NetworkResult<List<TeamEmployeePlanDto>> =
        safeApiCall { apiService.getPendingVacationPlans() }

    suspend fun decideVacationPlan(submissionId: String, approve: Boolean, rejectReason: String?): NetworkResult<Unit> =
        safeApiCall {
            val response = apiService.decideVacationPlan(submissionId, DecideVacationRequest(approve, rejectReason))
            if (response.isSuccessful) Unit else throw Exception("API Error: ${response.code()}")
        }
}
