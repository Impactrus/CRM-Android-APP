package com.ossadkowski.crm.callhistory

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object CrmApiSync {

    private const val BASE_URL = "https://crm.oc-serwer.pl"
    private val client = OkHttpClient.Builder().build()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun syncAddressBook(context: Context, searchQuery: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val sharedPrefs = context.getSharedPreferences("callhistory_prefs", Context.MODE_PRIVATE)
        var token = sharedPrefs.getString("token", null)

        // If local token not present, try to read from main CRM app as fallback
        if (token.isNullOrBlank()) {
            token = try {
                val mainAppContext = context.createPackageContext("com.ossadkowski.crm.mobile", 0)
                val mainPrefs = mainAppContext.getSharedPreferences("crm_session_prefs", Context.MODE_PRIVATE)
                mainPrefs.getString("token", null)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (token.isNullOrBlank()) {
            // Trigger login dialog on UI thread
            Handler(Looper.getMainLooper()).post {
                showLoginDialog(context) { successToken ->
                    // Save token locally
                    sharedPrefs.edit().putString("token", successToken).apply()
                    // Retry sync
                    syncAddressBook(context, searchQuery, onSuccess, onError)
                }
            }
            onError("Brak tokenu. Zaloguj się w oknie dialogowym.")
            return
        }

        fetchAddressBook(context, token, searchQuery, onSuccess) { err ->
            if (err.contains("401") || err.contains("Sesja wygasła")) {
                // Clear token and ask for login
                sharedPrefs.edit().remove("token").apply()
                Handler(Looper.getMainLooper()).post {
                    showLoginDialog(context) { successToken ->
                        sharedPrefs.edit().putString("token", successToken).apply()
                        syncAddressBook(context, searchQuery, onSuccess, onError)
                    }
                }
                onError("Sesja wygasła. Zaloguj się ponownie.")
            } else {
                onError(err)
            }
        }
    }

    private fun fetchAddressBook(context: Context, token: String, searchQuery: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/api/kontrahenci?search=${Uri.encode(searchQuery)}")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Referer", "$BASE_URL/dashboard/handlowcy")
            .header("X-Requested-With", "XMLHttpRequest")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("CrmApiSync", "fetchAddressBook fail: ${e.message}", e)
                onError("Błąd połączenia: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""
                android.util.Log.d("CrmApiSync", "fetchAddressBook response code: ${response.code}, body length: ${bodyStr.length}")
                if (!response.isSuccessful) {
                    android.util.Log.e("CrmApiSync", "fetchAddressBook server error: ${response.code}, body: $bodyStr")
                    if (response.code == 401) {
                        onError("Sesja wygasła. Kod 401.")
                    } else {
                        onError("Błąd API: kod ${response.code}")
                    }
                    return
                }

                onSuccess(bodyStr)
            }
        })
    }

    private fun showLoginDialog(context: Context, onLoginSuccess: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Logowanie do CRM")

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_login, null)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        builder.setView(view)
        builder.setPositiveButton("Zaloguj") { dialog, _ ->
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Wprowadź dane logowania", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            performLogin(context, username, password, onLoginSuccess)
            dialog.dismiss()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun performLogin(context: Context, username: String, password: String, onLoginSuccess: (String) -> Unit) {
        val payload = mapOf(
            "username" to username,
            "password" to password,
            "deviceId" to "callhistory_app",
            "deviceLabel" to "Android Emulator CallHistory",
            "devicePlatform" to "Android"
        )
        val body = Gson().toJson(payload).toRequestBody(JSON)

        val request = Request.Builder()
            .url("$BASE_URL/api/auth/login")
            .post(body)
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Referer", "$BASE_URL/dashboard/handlowcy")
            .header("X-Requested-With", "XMLHttpRequest")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Błąd logowania: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Logowanie nieudane (kod ${response.code})", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                try {
                    val map = Gson().fromJson(bodyStr, Map::class.java)
                    val token = map["token"] as? String
                    if (!token.isNullOrBlank()) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(token)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Brak tokenu w odpowiedzi serwera", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Błąd parsowania odpowiedzi", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
