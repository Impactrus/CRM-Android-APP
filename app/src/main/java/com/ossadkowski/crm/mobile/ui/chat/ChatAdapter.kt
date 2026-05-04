package com.ossadkowski.crm.mobile.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.TaskCommentDto
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val currentUserId: Int
) : ListAdapter<TaskCommentDto, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_OTHER = 2

        private val DiffCallback = object : DiffUtil.ItemCallback<TaskCommentDto>() {
            override fun areItemsTheSame(oldItem: TaskCommentDto, newItem: TaskCommentDto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TaskCommentDto, newItem: TaskCommentDto): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val comment = getItem(position)
        return if (comment.userId == currentUserId) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ME) {
            val view = inflater.inflate(R.layout.item_chat_message_me, parent, false)
            MeViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_message_other, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment = getItem(position)
        if (holder is MeViewHolder) {
            holder.bind(comment)
        } else if (holder is OtherViewHolder) {
            holder.bind(comment)
        }
    }

    private fun formatTime(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(isoDate) ?: return ""
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            try {
                // Alternatywny format
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = parser.parse(isoDate) ?: return ""
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.format(date)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    inner class MeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textMessage: TextView = view.findViewById(R.id.text_message)
        private val textTime: TextView = view.findViewById(R.id.text_time)

        fun bind(comment: TaskCommentDto) {
            textMessage.text = comment.tresc
            textTime.text = formatTime(comment.createdAt)
        }
    }

    inner class OtherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textAuthor: TextView = view.findViewById(R.id.text_author)
        private val textMessage: TextView = view.findViewById(R.id.text_message)
        private val textTime: TextView = view.findViewById(R.id.text_time)

        fun bind(comment: TaskCommentDto) {
            textAuthor.text = comment.username ?: "Nieznany"
            textMessage.text = comment.tresc
            textTime.text = formatTime(comment.createdAt)
        }
    }
}
