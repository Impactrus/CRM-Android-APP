package com.ossadkowski.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.TaskCommentDto
import com.ossadkowski.app.data.model.TaskFileDto
import com.ossadkowski.app.data.model.TaskHistoriaDto
import com.ossadkowski.app.databinding.ActivityTaskDetailBinding
import com.ossadkowski.app.ui.tasks.TaskDetailViewModel
import com.ossadkowski.app.util.StatusHelper

class TaskDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private val viewModel: TaskDetailViewModel by viewModels()
    private var taskId = 0
    private var activeTab = "comments"

    private val commentsAdapter = SimpleTextAdapter()
    private val filesAdapter = SimpleTextAdapter()
    private val historyAdapter = SimpleTextAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getIntExtra("id", 0)
        binding.backButton.setOnClickListener { finish() }

        binding.commentsRecycler.layoutManager = LinearLayoutManager(this)
        binding.commentsRecycler.setHasFixedSize(true)
        binding.commentsRecycler.adapter = commentsAdapter

        binding.filesRecycler.layoutManager = LinearLayoutManager(this)
        binding.filesRecycler.setHasFixedSize(true)
        binding.filesRecycler.adapter = filesAdapter

        binding.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.setHasFixedSize(true)
        binding.historyRecycler.adapter = historyAdapter

        setupTabs()
        setupCommentInput()
        observeData()

        viewModel.loadDetail(taskId)
        viewModel.loadComments(taskId)
    }

    private fun setupTabs() {
        val tabs = listOf(
            binding.tabComments to "comments",
            binding.tabFiles to "files",
            binding.tabHistory to "history"
        )

        fun selectTab(tab: String) {
            activeTab = tab
            tabs.forEach { (tv, name) ->
                tv.setBackgroundResource(if (name == tab) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
                tv.setTextColor(android.graphics.Color.parseColor(if (name == tab) "#374151" else "#6B7280"))
            }
            binding.commentsSection.visibility = if (tab == "comments") View.VISIBLE else View.GONE
            binding.filesRecycler.visibility = if (tab == "files") View.VISIBLE else View.GONE
            binding.historyRecycler.visibility = if (tab == "history") View.VISIBLE else View.GONE

            when (tab) {
                "files" -> viewModel.loadFiles(taskId)
                "history" -> viewModel.loadHistoria(taskId)
            }
        }

        binding.tabComments.setOnClickListener { selectTab("comments") }
        binding.tabFiles.setOnClickListener { selectTab("files") }
        binding.tabHistory.setOnClickListener { selectTab("history") }
    }

    private fun setupCommentInput() {
        binding.btnAddComment.setOnClickListener {
            val text = binding.commentInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(taskId, text)
                binding.commentInput.setText("")
            }
        }
    }

    private fun observeData() {
        viewModel.detail.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val t = result.data ?: return@observe
                    binding.taskTitle.text = t.tytul ?: "Zadanie #${t.id}"
                    binding.taskStatus.text = t.status ?: "-"
                    StatusHelper.applyStatusStyle(binding.taskStatus, t.status)
                    binding.taskTyp.text = "Typ: ${t.typ ?: "-"}"
                    binding.taskOpis.text = t.opis ?: ""
                    binding.taskAssigned.text = "Przypisane do: ${t.assignedToName ?: "-"}"
                    binding.taskTermin.text = "Termin: ${t.termin ?: "-"}"
                    binding.taskCreated.text = "Utworzone: ${t.createdAt?.take(10) ?: "-"} przez ${t.createdByName ?: "-"}"
                }
                is NetworkResult.Error -> Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.comments.observe(this) { result ->
            if (result is NetworkResult.Success) {
                commentsAdapter.submitList(
                    result.data?.map { "${it.username ?: ""}: ${it.tresc ?: ""} (${it.createdAt?.take(16) ?: ""})" } ?: emptyList()
                )
            }
        }

        viewModel.files.observe(this) { result ->
            if (result is NetworkResult.Success) {
                filesAdapter.submitList(
                    result.data?.map { "${it.nazwaPliku ?: ""} - ${it.uploadedBy ?: ""} (${it.createdAt?.take(10) ?: ""})" } ?: emptyList()
                )
            }
        }

        viewModel.historia.observe(this) { result ->
            if (result is NetworkResult.Success) {
                historyAdapter.submitList(
                    result.data?.map { "${it.createdAt?.take(16) ?: ""} - ${it.username ?: ""}: ${it.akcja ?: ""}" } ?: emptyList()
                )
            }
        }

        viewModel.commentResult.observe(this) { result ->
            if (result is NetworkResult.Success) {
                viewModel.loadComments(taskId)
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    class SimpleTextAdapter : ListAdapter<String, SimpleTextAdapter.VH>(
        object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    ) {
        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.text.text = getItem(position)
            holder.text.textSize = 13f
        }
    }
}
