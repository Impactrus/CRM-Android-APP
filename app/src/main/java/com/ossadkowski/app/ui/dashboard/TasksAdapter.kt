package com.ossadkowski.app.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.app.R
import com.ossadkowski.app.data.model.TaskItem
import com.ossadkowski.app.util.StatusHelper

class TasksAdapter : ListAdapter<TaskItem, TasksAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem) = oldItem == newItem
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
        holder.title.text = item.title ?: "Zadanie #${item.id}"
        holder.date.text = item.dueDate ?: item.createdAt ?: "-"
        holder.status.text = item.status ?: "-"
        StatusHelper.applyStatusStyle(holder.status, item.status)
    }
}
