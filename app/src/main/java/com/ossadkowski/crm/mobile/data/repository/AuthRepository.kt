package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.LoginRequest
import com.ossadkowski.crm.mobile.data.model.LoginResponse

class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) : BaseRepository() {
    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        return safeApiCall { apiService.login(LoginRequest(username, password)) }
    }

    suspend fun logout(): NetworkResult<Any> {
        return safeApiCall {
            try {
                apiService.logout()
            } catch (e: Exception) {
                // Ignore logout failures — we clear session anyway
            }
        }
    }
}
