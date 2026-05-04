package com.ossadkowski.crm.mobile

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.model.CreateTaskRequest
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.databinding.ActivityNewDebtTaskBinding
import com.ossadkowski.crm.mobile.ui.tasks.NewDebtTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewDebtTaskActivity : BaseActivity() {
    private lateinit var binding: ActivityNewDebtTaskBinding
    private val viewModel: NewDebtTaskViewModel by viewModels()
    private var selectedKontrahent: KontrahentSearchItem? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDebtTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSession()

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }

        // Date Picker
        binding.etTermin.setOnClickListener { showDatePicker() }

        // Contractor Search
        setupContractorSearch()

        // Save Button
        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun setupContractorSearch() {
        val popup = ListPopupWindow(this).apply {
            anchorView = binding.etKontrahentSearch
            isModal = false
        }

        binding.etKontrahentSearch.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                timer.cancel()
                val query = s.toString()
                if (query.length >= 3) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            runOnUiThread { viewModel.searchKontrahenci(query) }
                        }
                    }, 500)
                }
            }
        })

        viewModel.kontrahenci.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val items = result.data ?: emptyList()
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items.map { it.name ?: "" })
                popup.setAdapter(adapter)
                popup.setOnItemClickListener { _, _, position, _ ->
                    selectedKontrahent = items[position]
                    binding.etKontrahentSearch.setText(selectedKontrahent?.name)
                    popup.dismiss()
                }
                if (items.isNotEmpty()) popup.show() else popup.dismiss()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.etTermin.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndSave() {
        val tytul = binding.etTytul.text.toString().trim()
        val opis = binding.etOpis.text.toString().trim()
        val termin = binding.etTermin.text.toString().trim()

        if (tytul.isEmpty()) {
            Toast.makeText(this, getString(R.string.validation_title_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (termin.isEmpty()) {
            Toast.makeText(this, getString(R.string.validation_date_required), Toast.LENGTH_SHORT).show()
            return
        }

        val myUserId = sessionManager.userId
        val request = CreateTaskRequest(
            tytul = tytul,
            opis = opis,
            termin = termin,
            assignedToIds = listOf(myUserId),
            kontrahentNazwa = selectedKontrahent?.name,
            typ = "windykacja"
        )

        viewModel.createTask(request)
    }

    private fun observeViewModel() {
        viewModel.createResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.btnSave.isEnabled = false
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Utworzono zadanie", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is NetworkResult.Error -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
