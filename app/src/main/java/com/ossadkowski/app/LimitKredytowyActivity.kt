package com.ossadkowski.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.SessionManager
import com.ossadkowski.app.data.model.CreateLimitKredytowyRequest
import com.ossadkowski.app.databinding.ActivityLimitKredytowyNewBinding
import com.ossadkowski.app.ui.limitykredytowe.LimitKredytowyNewViewModel

class LimitKredytowyActivity : BaseActivity() {
    private lateinit var binding: ActivityLimitKredytowyNewBinding
    private val viewModel: LimitKredytowyNewViewModel by viewModels()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLimitKredytowyNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.backButton.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            val accountNum = binding.inputAccountNum.text.toString().trim()
            val limitStr = binding.inputLimit.text.toString().trim()
            val uwagi = binding.inputUwagi.text.toString().trim()

            if (accountNum.isEmpty()) {
                Toast.makeText(this, R.string.validation_account_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val limit = limitStr.toDoubleOrNull()
            if (limit == null || limit <= 0) {
                Toast.makeText(this, R.string.validation_limit_positive, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.create(CreateLimitKredytowyRequest(
                userId = session.userId,
                kontrahentAccountNum = accountNum,
                wnioskowanyLimit = limit,
                uwagi = uwagi.takeIf { it.isNotBlank() }
            ))
        }

        viewModel.createResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.btnSubmit.isEnabled = false
                is NetworkResult.Success -> {
                    Toast.makeText(this, R.string.wniosek_created, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
