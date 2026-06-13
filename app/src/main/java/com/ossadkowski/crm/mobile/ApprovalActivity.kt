package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.databinding.ActivityApprovalBinding
import com.ossadkowski.crm.mobile.ui.approval.ApprovalAdapter
import com.ossadkowski.crm.mobile.ui.approval.ApprovalViewModel
import com.ossadkowski.crm.mobile.util.PaginationHelper
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener

class ApprovalActivity : BaseActivity() {
    private lateinit var binding: ActivityApprovalBinding
    private lateinit var session: SessionManager
    private val viewModel: ApprovalViewModel by viewModels()
    private lateinit var adapter: ApprovalAdapter

    private var targetRole: String = "User"

    private val pagination = PaginationHelper(pageSize = 10) { page ->
        viewModel.page = page
        viewModel.loadApprovals(session.userId, targetRole)
    }

    private val detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadApprovals(session.userId, targetRole)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        targetRole = intent.getStringExtra("target_role") ?: session.approvalRole

        adapter = ApprovalAdapter(
            onApprove = { wniosek ->
                viewModel.approve(wniosek.id, session.userId, targetRole) { success ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, R.string.approved, Toast.LENGTH_SHORT).show()
                            viewModel.loadApprovals(session.userId, targetRole)
                        } else {
                            Toast.makeText(this, R.string.approve_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onReject = { wniosek ->
                viewModel.reject(wniosek.id, session.userId, targetRole) { success ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, R.string.rejected, Toast.LENGTH_SHORT).show()
                            viewModel.loadApprovals(session.userId, targetRole)
                        } else {
                            Toast.makeText(this, R.string.reject_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onItemClick = { wniosek ->
                val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                    putExtra("wniosek_id", wniosek.id)
                    putExtra("wniosek_num", wniosek.id.toString())
                    putExtra("target_role", targetRole)
                }
                detailLauncher.launch(intent)
            }
        )

        binding.approvalsRecycler.layoutManager = LinearLayoutManager(this)
        binding.approvalsRecycler.setHasFixedSize(true)
        binding.approvalsRecycler.adapter = adapter

        binding.backButton.setOnClickListener { finish() }

        binding.btnSyncFirebird.setOnClickListener {
            Toast.makeText(this, "Synchronizacja Firebird...", Toast.LENGTH_SHORT).show()
            // Placeholder: tu w przyszłości ewentualny call do API synchronizującego
            viewModel.loadApprovals(session.userId, targetRole)
        }

        binding.searchInput.addDebouncedTextListener(lifecycleScope) { query ->
            viewModel.search = query.takeIf { it.isNotBlank() }
            viewModel.page = 1
            pagination.reset()
            viewModel.loadApprovals(session.userId, targetRole)
        }

        viewModel.approvals.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    adapter.submitList(data.items)
                    if (data.items.isEmpty()) binding.emptyText.visibility = View.VISIBLE
                    pagination.updateFromResponse(data.totalCount, data.totalPages)
                    binding.btnPrev.isEnabled = pagination.hasPrev()
                    binding.btnNext.isEnabled = pagination.hasNext()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnPrev.setOnClickListener { pagination.prevPage() }
        binding.btnNext.setOnClickListener { pagination.nextPage() }

        viewModel.loadApprovals(session.userId, targetRole)
    }
}
