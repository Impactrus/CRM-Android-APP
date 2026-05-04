package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.databinding.ActivityHandlowcyListBinding
import com.ossadkowski.crm.mobile.ui.sales.HandlowcyListAdapter
import kotlinx.coroutines.launch

class HandlowcyListActivity : BaseActivity() {

    private lateinit var binding: ActivityHandlowcyListBinding
    private lateinit var adapter: HandlowcyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlowcyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupRefresh()
        
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = HandlowcyListAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                android.util.Log.d("Handlowcy", "Pobieranie danych...")
                val apiService = RetrofitClient.apiService // Używamy gotowego lazy property
                val data = apiService.getHandlowcy()
                android.util.Log.d("Handlowcy", "Pobrano ${data.size} handlowców")
                
                if (data.isEmpty()) {
                    android.util.Log.w("Handlowcy", "Lista handlowców jest pusta!")
                }
                
                adapter.updateData(data)
                binding.textCount.text = "Dane z AX (${data.size})"
            } catch (e: Exception) {
                android.util.Log.e("Handlowcy", "Błąd: ${e.message}", e)
                Toast.makeText(this@HandlowcyListActivity, "Błąd pobierania: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
