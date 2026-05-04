package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityConversationsBinding
import com.ossadkowski.crm.mobile.ui.dashboard.ConversationsAdapter
import com.ossadkowski.crm.mobile.ui.dashboard.DashboardViewModel

class ConversationsActivity : BaseActivity() {

    private lateinit var binding: ActivityConversationsBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: ConversationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSession()
        setupUI()
        binding.backButton.setOnClickListener { finish() }
        observeData()

        viewModel.loadMessages()
    }

    private fun setupUI() {
        adapter = ConversationsAdapter { item ->
            val intent = Intent(this, com.ossadkowski.crm.mobile.ui.chat.ChatActivity::class.java)
            intent.putExtra("ID", item.instanceId)
            intent.putExtra("TITLE", item.tytul)
            startActivity(intent)
        }

        binding.conversationsRecycler.apply {
            layoutManager = LinearLayoutManager(this@ConversationsActivity)
            adapter = this@ConversationsActivity.adapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadMessages()
        }
    }

    private fun observeData() {
        viewModel.conversations.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = if (binding.swipeRefresh.isRefreshing) View.GONE else View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    val items = result.data?.items ?: emptyList()
                    adapter.submitList(items)
                    binding.emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }
}
