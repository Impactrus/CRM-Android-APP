package com.ossadkowski.crm.mobile.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.model.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class NewRequestRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getTypy(): NetworkResult<List<SlownikItemDto>> {
        val result = cachedApiCall(db, "ref_typy_v2", CacheTtl.REFERENCE,
            object : TypeToken<List<SlownikItemDto>>() {}.type
        ) { apiService.getWnioskiTypy() }
        
        return if (result is NetworkResult.Success) {
            // Filtrujemy tylko L4 – Delegacja jest przywrócona
            val filtered = result.data?.filter { it.nazwa != "L4" }
            NetworkResult.Success(filtered ?: emptyList())
        } else result
    }

    suspend fun getRodzajeUrlopu(): NetworkResult<List<SlownikItemDto>> {
        val result = cachedApiCall(db, "ref_rodzaje_urlopu_v2", CacheTtl.REFERENCE,
            object : TypeToken<List<SlownikItemDto>>() {}.type
        ) { apiService.getRodzajeUrlopu() }

        // Rodzaje urlopu pozostają bez filtrowania delegacji
        return result
    }

    suspend fun getUzytkownicy(): NetworkResult<List<SlownikItemDto>> {
        return cachedApiCall(db, "ref_uzytkownicy", CacheTtl.REFERENCE,
            object : TypeToken<List<SlownikItemDto>>() {}.type
        ) { apiService.getWnioskiUzytkownicy() }
    }

    suspend fun createWniosekWithPhotos(
        request: CreateWniosekRequest,
        photoUris: List<Uri>,
        context: Context
    ): NetworkResult<CreateWniosekResponse> {
        val result = safeApiCall { apiService.createWniosek(request) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")

            val wniosekId = result.data?.id ?: return result
            for ((index, uri) in photoUris.withIndex()) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: continue
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val fileName = "zdjecie_${index + 1}.jpg"
                    val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                    apiService.uploadWniosekFile(wniosekId, part)
                } catch (_: Exception) {
                    // photo upload failure is non-fatal
                }
            }
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("create_wniosek", gson.toJson(request))
            return NetworkResult.Success(CreateWniosekResponse(0, "queued_offline"))
        }
        return result
    }
}
