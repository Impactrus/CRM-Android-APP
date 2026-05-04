package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.model.CreateLimitKredytowyRequest
import com.ossadkowski.crm.mobile.databinding.ActivityLimitKredytowyNewBinding
import com.ossadkowski.crm.mobile.ui.limitykredytowe.LimitKredytowyNewViewModel

import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.lifecycle.lifecycleScope
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener

class LimitKredytowyActivity : BaseActivity() {
    private lateinit var binding: ActivityLimitKredytowyNewBinding
    private val viewModel: LimitKredytowyNewViewModel by viewModels()
    private lateinit var session: SessionManager
    private var selectedKontrahent: KontrahentSearchItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLimitKredytowyNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.backButton.setOnClickListener { finish() }

        setupKontrahentSearch()

        binding.btnSubmit.setOnClickListener {
            submitForm()
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

    private fun setupKontrahentSearch() {
        val popup = ListPopupWindow(this).apply {
            anchorView = binding.inputNazwa
            isModal = false
        }

        binding.inputNazwa.addDebouncedTextListener(lifecycleScope) { query ->
            if (query.length >= 3) {
                binding.searchProgress.visibility = View.VISIBLE
                viewModel.searchKontrahenci(query)
            } else {
                popup.dismiss()
            }
        }

        viewModel.kontrahenci.observe(this) { result ->
            binding.searchProgress.visibility = View.GONE
            if (result is NetworkResult.Success) {
                val items = result.data ?: emptyList()
                if (items.isNotEmpty()) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, items.map { it.name })
                    popup.setAdapter(adapter)
                    popup.setOnItemClickListener { _, _, position, _ ->
                        val item = items[position]
                        applyKontrahent(item)
                        popup.dismiss()
                    }
                    if (!isFinishing) popup.show()
                } else {
                    popup.dismiss()
                }
            }
        }
    }

    private fun applyKontrahent(item: KontrahentSearchItem) {
        selectedKontrahent = item
        binding.inputNazwa.setText(item.name)
        binding.inputAxNum.setText(item.accountNum)
        binding.inputAdres.setText(item.address)
        binding.inputNip.setText(item.nip ?: "")
    }

    private fun submitForm() {
        val nazwa = binding.inputNazwa.text.toString().trim()
        val accountNum = binding.inputAxNum.text.toString().trim()
        val limitStr = binding.inputLimit.text.toString().trim()
        val terminStr = binding.inputTerminZabezpieczen.text.toString().trim()
        val opisZab = binding.inputOpisZabezpieczen.text.toString().trim()
        val noweZab = binding.inputNoweZabezpieczenia.text.toString().trim()
        val dochody = binding.inputDodatkoweDochody.text.toString().trim()
        val zobowiazania = binding.inputZobowiazania.text.toString().trim()
        val uwagi = binding.inputUwagi.text.toString().trim()
        
        val potwPrzeterm = binding.checkPotwierdzonePrzeterminowane.isChecked
        val rozlPlonami = binding.checkRozliczeniePlonami.isChecked

        if (nazwa.isEmpty() || accountNum.isEmpty() || limitStr.isEmpty() || terminStr.isEmpty() || 
            opisZab.isEmpty() || noweZab.isEmpty() || dochody.isEmpty() || zobowiazania.isEmpty()) {
            Toast.makeText(this, R.string.validation_fields_required, Toast.LENGTH_SHORT).show()
            return
        }

        val limitVal = limitStr.toDoubleOrNull() ?: 0.0
        if (limitVal <= 0) {
            Toast.makeText(this, R.string.validation_limit_positive, Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateLimitKredytowyRequest(
            userId = session.userId,
            kontrahentAccountNum = accountNum,
            wnioskowanyLimit = limitVal,
            terminZabezpieczen = terminStr,
            opisZabezpieczen = opisZab,
            noweZabezpieczenia = noweZab,
            dodatkoweDochody = dochody,
            zobowiazania = zobowiazania,
            potwierdzonePrzeterminowane = potwPrzeterm,
            rozliczeniePlonami = rozlPlonami,
            uwagi = uwagi.takeIf { it.isNotBlank() }
        )

        viewModel.create(request)
    }
}
