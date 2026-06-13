package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseRepository {

    companion object {
        private val gson = Gson()
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(apiCall())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val serverMsg = readBackendMessage(e)
                val fallback = when (e.code()) {
                    400 -> "Nieprawidłowe dane (${e.code()})."
                    403 -> "Brak uprawnień."
                    404 -> "Nie znaleziono zasobu."
                    else -> "Błąd serwera (${e.code()})."
                }
                NetworkResult.Error(serverMsg ?: fallback)
            } catch (e: IOException) {
                NetworkResult.Error("Brak połączenia z serwerem.")
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Nieoczekiwany błąd.")
            }
        }
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
                obj.has("error") && !obj.get("error").isJsonNull -> obj.get("error").asString
                obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun <T> cachedApiCall(
        db: AppDatabase,
        cacheKey: String,
        ttlMs: Long,
        type: Type,
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            // 1. Try cache first
            val cached = try { db.getValid(cacheKey) } catch (_: Exception) { null }

            if (cached != null) {
                val data: T = gson.fromJson(cached.json_data, type)
                // Background refresh (fire-and-forget)
                launch {
                    try {
                        val fresh = apiCall()
                        db.put(CacheEntry(cacheKey, gson.toJson(fresh), System.currentTimeMillis(), ttlMs))
                    } catch (_: Exception) { /* silent refresh */ }
                }
                return@withContext NetworkResult.Success(data)
            }

            // 2. No cache — network call
            try {
                val result = apiCall()
                try {
                    db.put(CacheEntry(cacheKey, gson.toJson(result), System.currentTimeMillis(), ttlMs))
                } catch (_: Exception) { /* cache write failure is non-fatal */ }
                NetworkResult.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                // 3. Network failed — try stale cache as offline fallback
                val stale = try { db.getAny(cacheKey) } catch (_: Exception) { null }
                if (stale != null) {
                    val data: T = gson.fromJson(stale.json_data, type)
                    NetworkResult.Success(data)
                } else {
                    val serverMsg = readBackendMessage(e)
                    val fallback = "Błąd serwera (${e.code()})."
                    NetworkResult.Error(serverMsg ?: fallback)
                }
            } catch (e: IOException) {
                val stale = try { db.getAny(cacheKey) } catch (_: Exception) { null }
                if (stale != null) {
                    val data: T = gson.fromJson(stale.json_data, type)
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error("Brak połączenia z serwerem.")
                }
            } catch (e: Exception) {
                // 3. Network failed — try stale cache as offline fallback
                val stale = try { db.getAny(cacheKey) } catch (_: Exception) { null }
                if (stale != null) {
                    val data: T = gson.fromJson(stale.json_data, type)
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(e.message ?: "Nieoczekiwany błąd.")
                }
            }
        }
    }
}
