package com.ossadkowski.crm.mobile.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.TaskListItemDto
import com.ossadkowski.crm.mobile.databinding.ItemTaskDetailSimpleBinding

class CalendarTasksAdapter(
    private val onTaskClick: (TaskListItemDto) -> Unit
) : ListAdapter<TaskListItemDto, CalendarTasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskDetailSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskDetailSimpleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskListItemDto) {
            binding.tvTitle.text = task.tytul ?: "-"
            
            // Pokaż kontrahenta lub przypisanego pracownika
            val kontrahent = task.kontrahentNazwa?.takeIf { it.isNotBlank() && it != "-" }
            val assignedTo = task.assignedToName?.takeIf { it.isNotBlank() && it != "-" }
            binding.tvKontrahent.text = when {
                kontrahent != null -> kontrahent
                assignedTo != null -> "Przypisano: $assignedTo"
                else -> "Brak kontrahenta"
            }
            
            binding.tvStatus.text = task.status?.takeIf { it.isNotBlank() && it != "-" } ?: ""
            
            binding.root.setOnClickListener { onTaskClick(task) }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TaskListItemDto>() {
        override fun areItemsTheSame(oldItem: TaskListItemDto, newItem: TaskListItemDto): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: TaskListItemDto, newItem: TaskListItemDto): Boolean {
            return oldItem == newItem
        }
    }
}
