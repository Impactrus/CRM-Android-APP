package com.ossadkowski.crm.mobile.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.BoardColumn
import com.ossadkowski.crm.mobile.data.model.BoardTask

class BoardColumnAdapter(private val onTaskClick: (BoardTask) -> Unit) :
    ListAdapter<Pair<String, BoardColumn>, BoardColumnAdapter.ColumnViewHolder>(ColumnDiffCallback()) {

    private val expandedColumns = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_column, parent, false)
        return ColumnViewHolder(view, onTaskClick)
    }

    override fun onBindViewHolder(holder: ColumnViewHolder, position: Int) {
        val item = getItem(position)
        val isExpanded = expandedColumns.contains(item.first)
        holder.bind(item, isExpanded) { expanded ->
            if (expanded) expandedColumns.add(item.first) else expandedColumns.remove(item.first)
        }
    }

    class ColumnViewHolder(view: View, private val onTaskClick: (BoardTask) -> Unit) : RecyclerView.ViewHolder(view) {
        private val header: View = view.findViewById(R.id.columnHeader)
        private val title: TextView = view.findViewById(R.id.columnTitle)
        private val count: TextView = view.findViewById(R.id.columnCount)
        private val arrow: View = view.findViewById(R.id.columnArrow)
        private val recycler: RecyclerView = view.findViewById(R.id.columnRecycler)
        private val taskAdapter = BoardTaskAdapter(onTaskClick)

        init {
            recycler.layoutManager = LinearLayoutManager(view.context)
            recycler.adapter = taskAdapter
        }

        fun bind(item: Pair<String, BoardColumn>, isExpanded: Boolean, onToggle: (Boolean) -> Unit) {
            title.text = item.first
            count.text = item.second.items.size.toString()
            taskAdapter.submitList(item.second.items)

            recycler.visibility = if (isExpanded) View.VISIBLE else View.GONE
            arrow.rotation = if (isExpanded) 180f else 0f

            header.setOnClickListener {
                val nextState = recycler.visibility != View.VISIBLE
                recycler.visibility = if (nextState) View.VISIBLE else View.GONE
                arrow.animate().rotation(if (nextState) 180f else 0f).setDuration(200).start()
                onToggle(nextState)
            }
        }
    }

    class ColumnDiffCallback : DiffUtil.ItemCallback<Pair<String, BoardColumn>>() {
        override fun areItemsTheSame(oldItem: Pair<String, BoardColumn>, newItem: Pair<String, BoardColumn>) = oldItem.first == newItem.first
        override fun areContentsTheSame(oldItem: Pair<String, BoardColumn>, newItem: Pair<String, BoardColumn>) = oldItem.second == newItem.second
    }
}

class BoardTaskAdapter(private val onClick: (BoardTask) -> Unit) :
    ListAdapter<BoardTask, BoardTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_task, parent, false)
        return TaskViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(view: View, private val onClick: (BoardTask) -> Unit) : RecyclerView.ViewHolder(view) {
        private val type: TextView = view.findViewById(R.id.taskType)
        private val title: TextView = view.findViewById(R.id.taskTitle)
        private val client: TextView = view.findViewById(R.id.taskClient)
        private val deadline: TextView = view.findViewById(R.id.taskDeadline)
        private val assigned: TextView = view.findViewById(R.id.taskAssigned)
        private val idText: TextView = view.findViewById(R.id.taskId)
        private val typeIndicator: View = view.findViewById(R.id.typeIndicator)

        fun bind(task: BoardTask) {
            title.text = task.tytul
            client.text = task.kontrahentNazwa ?: ""
            deadline.text = task.termin
            assigned.text = task.assignedToName
            idText.text = "#${task.id}"
            type.text = task.typ?.replaceFirstChar { it.uppercase() }

            // Kolory i tagi zależnie od typu
            when (task.typ?.lowercase()) {
                "marketing" -> {
                    type.setBackgroundResource(R.drawable.bg_tag_pink)
                    type.setTextColor(Color.parseColor("#DB2777")) // Pink 600
                    typeIndicator.setBackgroundColor(Color.parseColor("#DB2777"))
                }
                "wizyta" -> {
                    type.setBackgroundResource(R.drawable.bg_tag_light)
                    type.setTextColor(Color.parseColor("#10B981")) // Emerald 500
                    typeIndicator.setBackgroundColor(Color.parseColor("#10B981"))
                }
                else -> {
                    type.setBackgroundResource(R.drawable.bg_count_badge)
                    type.setTextColor(Color.parseColor("#6B7280")) // Gray 500
                    typeIndicator.setBackgroundColor(Color.parseColor("#6B7280"))
                }
            }

            // Opóźnienie
            if (task.isOverdue) {
                deadline.setTextColor(Color.parseColor("#EF4444")) // Red 500
            } else {
                deadline.setTextColor(Color.parseColor("#6B7280"))
            }

            itemView.setOnClickListener { onClick(task) }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<BoardTask>() {
        override fun areItemsTheSame(oldItem: BoardTask, newItem: BoardTask) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BoardTask, newItem: BoardTask) = oldItem == newItem
    }
}
