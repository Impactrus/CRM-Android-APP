package com.ossadkowski.app.data.repository

import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.data.model.LoginRequest
import com.ossadkowski.app.data.model.LoginResponse

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
