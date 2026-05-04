package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityGenericListBinding
import com.ossadkowski.crm.mobile.ui.tasks.TasksListViewModel
import com.ossadkowski.crm.mobile.ui.tasks.TasksV2Adapter
import com.ossadkowski.crm.mobile.util.PaginationHelper
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener

class DebtTasksListActivity : BaseActivity() {
    private lateinit var binding: ActivityGenericListBinding
    private val viewModel: TasksListViewModel by viewModels()
    private lateinit var adapter: TasksV2Adapter

    private val pagination = PaginationHelper(pageSize = 10) { page ->
        viewModel.page = page
        viewModel.load()
    }

    private val startNewTask = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.page = 1
            pagination.reset()
            viewModel.load()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenericListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleText.text = getString(R.string.debt_tasks_title)

        // Show FAB for adding new tasks
        binding.fabAdd.visibility = View.VISIBLE
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, NewDebtTaskActivity::class.java)
            startNewTask.launch(intent)
        }

        // Set the filter for debt collection tasks (consistent with web API)
        viewModel.typFilter = "windykacja"

        adapter = TasksV2Adapter(onClick = { task ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("id", task.id)
            startActivity(intent)
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
        binding.backButton.setOnClickListener { finish() }

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
                    adapter.submitList(data.items)
                    if (data.items.isEmpty()) binding.emptyText.visibility = View.VISIBLE
                    pagination.updateFromResponse(data.totalCount, data.totalPages)
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
}
