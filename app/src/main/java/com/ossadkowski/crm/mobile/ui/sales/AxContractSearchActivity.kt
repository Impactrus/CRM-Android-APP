package com.ossadkowski.crm.mobile.ui.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.BaseActivity
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityKontrahentSearchBinding // We reuse the layout since it's just a search bar + list

class AxContractSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityKontrahentSearchBinding
    private val viewModel: TransportViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private val adapter = AxContractSearchAdapter { contract ->
        val resultIntent = Intent()
        resultIntent.putExtra("AX_CONTRACT_ID", contract.id)
        resultIntent.putExtra("AX_VENDOR_NAME", contract.vendorName)
        resultIntent.putExtra("AX_ITEM_ID", contract.itemId)
        resultIntent.putExtra("AX_ITEM_NAME", contract.itemName)
        resultIntent.putExtra("AX_QUANTITY", contract.quantity ?: 0.0)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKontrahentSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        
        viewModel.search = null
        viewModel.searchAxContracts(null)
    }

    private fun showHint(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.paginationLayout.visibility = View.GONE
        binding.textHint.text = message
        binding.textHint.visibility = View.VISIBLE
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    viewModel.searchAxContracts(query)
                }
                handler.postDelayed(searchRunnable!!, 400)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.axContracts.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.textHint.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val items = result.data ?: emptyList()

                    if (items.isEmpty()) {
                        showHint("Nie znaleziono kontraktów AX")
                    } else {
                        binding.textHint.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        adapter.submitList(items)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showHint("Błąd: ${result.message}")
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { handler.removeCallbacks(it) }
    }
}
