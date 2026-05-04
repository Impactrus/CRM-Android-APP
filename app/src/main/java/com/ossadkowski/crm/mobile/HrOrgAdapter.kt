package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.HrOrgItemDto
import com.ossadkowski.crm.mobile.databinding.ItemHrOrgMemberBinding

class HrOrgAdapter(
    private val onItemClick: (HrOrgItemDto) -> Unit
) : RecyclerView.Adapter<HrOrgAdapter.Holder>() {

    private var items = listOf<HrOrgItemDto>()

    fun submitList(list: List<HrOrgItemDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHrOrgMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(private val binding: ItemHrOrgMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HrOrgItemDto) {
            binding.tvName.text = item.displayName
            binding.tvPosition.text = item.workpost
            binding.tvInitials.text = getInitials(item.displayName ?: "")
            
            // If they have children, show chevron
            binding.ivChevron.visibility = if (item.children.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getInitials(name: String): String {
            val parts = name.split(" ")
            if (parts.size >= 2) {
                return (parts[0].take(1) + parts[1].take(1)).uppercase()
            }
            return name.take(2).uppercase()
        }
    }
}
