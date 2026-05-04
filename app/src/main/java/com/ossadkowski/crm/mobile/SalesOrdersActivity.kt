package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivitySalesOrdersBinding
import com.ossadkowski.crm.mobile.ui.sales.SalesOrdersAdapter
import com.ossadkowski.crm.mobile.ui.sales.SalesOrdersViewModel
import com.ossadkowski.crm.mobile.util.PaginationHelper

class SalesOrdersActivity : BaseActivity() {

    private lateinit var binding: ActivitySalesOrdersBinding
    private val viewModel: SalesOrdersViewModel by viewModels()
    private val adapter = SalesOrdersAdapter { order ->
        val intent = android.content.Intent(this, SalesOrderDetailActivity::class.java)
        intent.putExtra("ORDER_ID", order.id)
        startActivity(intent)
    }

    private val pagination = PaginationHelper(pageSize = 15) { page ->
        viewModel.page = page
        viewModel.loadOrders()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        
        viewModel.loadOrders()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        
        binding.btnNew.setOnClickListener {
            val intent = android.content.Intent(this, SalesOrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", 0) // 0 means new order
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnPrev.setOnClickListener { pagination.prevPage() }
        binding.btnNext.setOnClickListener { pagination.nextPage() }
        
        binding.searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.search = s?.toString()
                viewModel.page = 1
                viewModel.loadOrders()
            }
        })
    }

    private fun observeViewModel() {
        viewModel.orders.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    adapter.submitList(data.data)
                    pagination.updateFromGenericResponse(data.total, data.pageSize)
                    binding.btnPrev.isEnabled = pagination.hasPrev()
                    binding.btnNext.isEnabled = pagination.hasNext()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Internal adapter name might be different, let's check SalesOrdersAdapter.kt
