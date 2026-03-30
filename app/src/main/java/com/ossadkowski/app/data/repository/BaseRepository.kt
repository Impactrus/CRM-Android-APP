package com.ossadkowski.app.data.repository

import com.google.gson.Gson
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.cache.AppDatabase
import com.ossadkowski.app.data.cache.CacheEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Unknown Error")
            }
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
            } catch (e: Exception) {
                // 3. Network failed — try stale cache as offline fallback
                val stale = try { db.getAny(cacheKey) } catch (_: Exception) { null }
                if (stale != null) {
                    val data: T = gson.fromJson(stale.json_data, type)
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(e.message ?: "Unknown Error")
                }
            }
        }
    }
}
