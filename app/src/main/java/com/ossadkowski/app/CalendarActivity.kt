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
import com.ossadkowski.app.data.model.ZamrozenieDto
import com.ossadkowski.app.databinding.ActivityCalendarBinding
import com.ossadkowski.app.ui.calendar.CalendarViewModel

class CalendarActivity : BaseActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private val viewModel: CalendarViewModel by viewModels()
    private val zamrozeniaAdapter = ZamrozeniaAdapter()

    private val monthNames = arrayOf("", "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = zamrozeniaAdapter

        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth(); updateLabel() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth(); updateLabel() }

        viewModel.zamrozenia.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val items = result.data ?: emptyList()
                    if (items.isEmpty()) binding.emptyText.visibility = View.VISIBLE
                    zamrozeniaAdapter.submitList(items.map { z ->
                        "${z.dzial ?: "-"}: ${z.dataOd ?: ""} - ${z.dataDo ?: ""} ${if (z.opis.isNullOrBlank()) "" else "(${z.opis})"}"
                    })
                }
                is NetworkResult.Error -> Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
            }
        }

        updateLabel()
        viewModel.loadMonth()
    }

    private fun updateLabel() {
        binding.monthLabel.text = "${monthNames[viewModel.currentMonth]} ${viewModel.currentYear}"
    }

    class ZamrozeniaAdapter : ListAdapter<String, ZamrozeniaAdapter.VH>(
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
            holder.text.textSize = 14f
        }
    }
}
