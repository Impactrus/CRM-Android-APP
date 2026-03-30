package com.ossadkowski.app.ui.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.app.R
import com.ossadkowski.app.data.model.TaskListItemDto
import com.ossadkowski.app.util.StatusHelper

class TasksV2Adapter(
    private val onClick: (TaskListItemDto) -> Unit
) : ListAdapter<TaskListItemDto, TasksV2Adapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<TaskListItemDto>() {
        override fun areItemsTheSame(oldItem: TaskListItemDto, newItem: TaskListItemDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TaskListItemDto, newItem: TaskListItemDto) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.req_type)
        val date: TextView = view.findViewById(R.id.req_date)
        val status: TextView = view.findViewById(R.id.req_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.title.text = item.tytul ?: "Zadanie #${item.id}"
        holder.date.text = item.termin ?: item.createdAt?.take(10) ?: "-"
        holder.status.text = item.status ?: "-"
        StatusHelper.applyStatusStyle(holder.status, item.status)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
