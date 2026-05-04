package com.ossadkowski.crm.mobile.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.ConversationItem
import com.ossadkowski.crm.mobile.databinding.ItemConversationBinding

class ConversationsAdapter(
    private val onClick: (ConversationItem) -> Unit
) : ListAdapter<ConversationItem, ConversationsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConversationItem) {
            binding.convTitle.text = item.tytul ?: "Brak tytułu"
            binding.convType.text = item.typ ?: ""
            binding.convStatus.text = item.status ?: ""
            binding.convClient.text = item.kontrahentNazwa ?: ""
            
            binding.convLastAuthor.text = if (item.lastCommentAuthor != null) "${item.lastCommentAuthor}:" else ""
            binding.convLastText.text = item.lastCommentText ?: ""
            binding.convLastDate.text = item.lastCommentAt ?: ""

            binding.convUnreadBadge.visibility = if (item.unreadCount > 0) View.VISIBLE else View.GONE
            binding.convUnreadBadge.text = item.unreadCount.toString()

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ConversationItem>() {
        override fun areItemsTheSame(oldItem: ConversationItem, newItem: ConversationItem) = oldItem.instanceId == newItem.instanceId
        override fun areContentsTheSame(oldItem: ConversationItem, newItem: ConversationItem) = oldItem == newItem
    }
}
