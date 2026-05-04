package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityLimityKredytoweListBinding
import com.ossadkowski.crm.mobile.ui.limitykredytowe.LimityKredytoweAdapter
import com.ossadkowski.crm.mobile.ui.limitykredytowe.LimityKredytoweListViewModel
import com.ossadkowski.crm.mobile.util.PaginationHelper
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener

class LimityKredytoweListActivity : BaseActivity() {
    private lateinit var binding: ActivityLimityKredytoweListBinding
    private val viewModel: LimityKredytoweListViewModel by viewModels()
    private lateinit var adapter: LimityKredytoweAdapter

    private val pagination = PaginationHelper(pageSize = 20) { page ->
        viewModel.page = page
        viewModel.load()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLimityKredytoweListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LimityKredytoweAdapter(onClick = { item ->
            val intent = Intent(this, LimitKredytowyDetailActivity::class.java)
            intent.putExtra("id", item.id)
            startActivity(intent)
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter

        binding.backButton.setOnClickListener { finish() }
        binding.btnNew.setOnClickListener {
            startActivity(Intent(this, LimitKredytowyActivity::class.java))
        }

        binding.searchInput.addDebouncedTextListener(lifecycleScope) { query ->
            viewModel.search = query.takeIf { it.isNotBlank() }
            viewModel.page = 1
            pagination.reset()
            viewModel.load()
        }

        binding.btnPrev.setOnClickListener { pagination.prevPage() }
        binding.btnNext.setOnClickListener { pagination.nextPage() }

        viewModel.items.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    adapter.submitList(data.data)
                    if (data.data.isEmpty()) binding.emptyText.visibility = View.VISIBLE
                    pagination.updateFromGenericResponse(data.total, data.pageSize)
                    binding.pageInfo.text = getString(R.string.page_info_format, pagination.currentPage, pagination.totalPages, pagination.totalCount)
                    binding.btnPrev.isEnabled = pagination.hasPrev()
                    binding.btnNext.isEnabled = pagination.hasNext()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.load()
    }

    private var needsRefresh = false

    override fun onPause() {
        super.onPause()
        needsRefresh = true
    }

    override fun onResume() {
        super.onResume()
        if (needsRefresh) {
            viewModel.load()
            needsRefresh = false
        }
    }
}
