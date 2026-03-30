package com.ossadkowski.app.data.api

import android.content.Context
import android.content.Intent
import com.ossadkowski.app.BuildConfig
import com.ossadkowski.app.MainActivity
import com.ossadkowski.app.data.SessionManager
import okhttp3.Authenticator
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.ossadkowski.app.data.cache.AppDatabase
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private lateinit var sessionManager: SessionManager
    private lateinit var appContext: Context

    val cacheDb: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    fun init(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(appContext)
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBuilder = request.newBuilder()
            .addHeader("Content-Type", "application/json")

        // Only send token to our trusted base URL
        val baseHost = android.net.Uri.parse(BuildConfig.API_BASE_URL).host
        if (request.url.host == baseHost) {
            sessionManager.token?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val tokenAuthenticator = Authenticator { _: Route?, response: Response ->
        // Don't try to refresh if this is already a refresh request
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            return@Authenticator null
        }
        // Don't try to refresh for login
        if (response.request.url.encodedPath.contains("/auth/login")) {
            return@Authenticator null
        }

        val currentToken = sessionManager.token ?: return@Authenticator null

        synchronized(this) {
            // Check if token was already refreshed by another thread
            val latestToken = sessionManager.token
            if (latestToken != currentToken && latestToken != null) {
                // Token was refreshed, retry with new token
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
            }

            // Try to refresh
            try {
                val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
                val refreshRequest = Request.Builder()
                    .url("${baseUrl}api/auth/refresh")
                    .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
                    .header("Authorization", "Bearer $currentToken")
                    .build()

                val refreshClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body?.string()
                    val json = JSONObject(body ?: "{}")
                    val newToken = json.optString("token", "")
                    if (newToken.isNotEmpty()) {
                        sessionManager.updateToken(newToken)
                        return@Authenticator response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    }
                }

                // Refresh failed — clear session and redirect to login
                sessionManager.clear()
                val intent = Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                appContext.startActivity(intent)
                return@Authenticator null
            } catch (e: Exception) {
                sessionManager.clear()
                val intent = Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                appContext.startActivity(intent)
                return@Authenticator null
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient by lazy {
        val cacheDir = File(appContext.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 10L * 1024 * 1024) // 10 MB

        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
