package com.ossadkowski.crm.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityTowarSearchBinding
import com.ossadkowski.crm.mobile.ui.sales.TowarSearchAdapter
import com.ossadkowski.crm.mobile.ui.sales.TowaryViewModel
import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.util.PaginationHelper

class TowarSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityTowarSearchBinding
    private val viewModel: TowaryViewModel by viewModels()
    private val adapter = TowarSearchAdapter { towar ->
        showQuantityDialog(towar)
    }

    private val pagination = PaginationHelper(pageSize = 15) { page ->
        viewModel.page = page
        viewModel.loadTowary()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTowarSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        
        viewModel.loadTowary()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnPrev.setOnClickListener { pagination.prevPage() }
        binding.btnNext.setOnClickListener { pagination.nextPage() }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.search = s?.toString()
                viewModel.page = 1
                viewModel.loadTowary()
            }
        })
    }

    private fun observeViewModel() {
        viewModel.towary.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data
                    adapter.submitList(data?.items ?: emptyList())
                    
                    // Update pagination UI
                    if (data != null) {
                        pagination.updateFromGenericResponse(data.total, data.pageSize)
                        binding.btnPrev.isEnabled = pagination.hasPrev()
                        binding.btnNext.isEnabled = pagination.hasNext()
                        binding.pageInfo.text = "${viewModel.page}"
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showQuantityDialog(towar: TowarListItem) {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText("1.0")

        AlertDialog.Builder(this)
            .setTitle("Dodaj pozycję")
            .setMessage("Wprowadź ilość dla: ${towar.nazwa}")
            .setView(input)
            .setPositiveButton("Dodaj") { _, _ ->
                val qty = input.text.toString().toDoubleOrNull() ?: 1.0
                val resultIntent = Intent()
                resultIntent.putExtra("TOWAR_KOD", towar.kod ?: "")
                resultIntent.putExtra("TOWAR_NAZWA", towar.nazwa ?: "")
                resultIntent.putExtra("TOWAR_CENA", towar.cena ?: 0.0)
                resultIntent.putExtra("ILOSC", qty)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}
