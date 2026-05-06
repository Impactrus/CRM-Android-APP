package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.Gson
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.device.DeviceIdProvider
import com.ossadkowski.crm.mobile.data.device.FcmTokenProvider
import com.ossadkowski.crm.mobile.data.model.LoginRequest
import com.ossadkowski.crm.mobile.data.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val deviceIdProvider: DeviceIdProvider,
    private val fcmTokenProvider: FcmTokenProvider,
    private val apiService: ApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> =
        withContext(Dispatchers.IO) {
            val req = LoginRequest(
                username = username,
                password = password,
                deviceId = deviceIdProvider.deviceId(),
                deviceLabel = deviceIdProvider.label(),
                devicePlatform = deviceIdProvider.platform(),
                fcmToken = fcmTokenProvider.current()
            )
            try {
                NetworkResult.Success(apiService.login(req))
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val raw = e.response()?.errorBody()?.string().orEmpty()
                val parsed = runCatching { Gson().fromJson(raw, LoginResponse::class.java) }.getOrNull()
                NetworkResult.HttpError(
                    code = e.code(),
                    message = parsed?.message ?: "HTTP ${e.code()}",
                    deviceTrusted = parsed?.deviceTrusted,
                    deviceIsNew = parsed?.deviceIsNew
                )
            } catch (e: IOException) {
                NetworkResult.Error("Brak połączenia z serwerem")
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Nieznany błąd")
            }
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
