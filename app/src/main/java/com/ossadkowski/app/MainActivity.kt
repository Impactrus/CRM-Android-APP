package com.ossadkowski.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.SessionManager
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.databinding.ActivityMainBinding
import com.ossadkowski.app.fcm.DeviceTokenRequest
import com.ossadkowski.app.fcm.NotificationChannelHelper
import com.ossadkowski.app.ui.login.LoginViewModel
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private val viewModel: LoginViewModel by viewModels()
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrofitClient.init(this)
        session = SessionManager(this)

        NotificationChannelHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        if (session.isLoggedIn) {
            navigateToDashboard()
            return
        }

        // Password visibility toggle
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.ic_eye)
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
        }

        binding.loginButton.setOnClickListener {
            binding.loginErrorText.visibility = View.GONE
            binding.passwordContainer.setBackgroundResource(R.drawable.bg_input_figma)

            val username = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showLoginError(getString(R.string.login_enter_credentials))
                return@setOnClickListener
            }

            viewModel.login(username, password)
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.loginButton.isEnabled = false
                    binding.loginButton.text = getString(R.string.login_loading)
                    binding.loginErrorText.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    session.saveSession(
                        data.token ?: "",
                        data.userId ?: 0,
                        data.role ?: "User",
                        data.username ?: "",
                        data.dzial,
                        data.employeeCacheId,
                        data.claims,
                        data.claimsVersion
                    )
                    registerFcmToken()
                    navigateToDashboard()
                }
                is NetworkResult.Error -> {
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = getString(R.string.login_button)
                    showLoginError(getString(R.string.login_error_inline))
                }
            }
        }
    }

    private fun showLoginError(message: String) {
        binding.loginErrorText.text = message
        binding.loginErrorText.visibility = View.VISIBLE
        binding.passwordContainer.setBackgroundResource(R.drawable.bg_input_error)
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.registerDeviceToken(DeviceTokenRequest(token))
                    Log.d("FCM", "Token registered with backend")
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to register FCM token", e)
                }
            }
        }
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
