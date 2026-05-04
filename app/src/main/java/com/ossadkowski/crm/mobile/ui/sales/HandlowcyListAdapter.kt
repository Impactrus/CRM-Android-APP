package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.Handlowiec
import com.ossadkowski.crm.mobile.databinding.ItemHandlowiecBinding

class HandlowcyListAdapter(private var items: List<Handlowiec>) :
    RecyclerView.Adapter<HandlowcyListAdapter.ViewHolder>() {

    private var filteredItems = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHandlowiecBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }

    override fun getItemCount() = filteredItems.size

    fun updateData(newItems: List<Handlowiec>) {
        items = newItems
        filteredItems = newItems
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter {
                it.pelnaNazwa.contains(query, ignoreCase = true) ||
                it.id.contains(query, ignoreCase = true) ||
                it.stanowisko?.contains(query, ignoreCase = true) == true
            }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemHandlowiecBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Handlowiec) {
            binding.textName.text = item.pelnaNazwa
            binding.textStanowisko.text = item.stanowisko ?: "Brak stanowiska"
            binding.textId.text = "ID: ${item.id}"
            binding.badgeDzial.text = item.dzial ?: "-"
            
            // Kolorowanie statusu
            when (item.status) {
                1 -> {
                    binding.badgeStatus.text = "Aktywny"
                    binding.badgeStatus.setBackgroundResource(R.drawable.bg_badge_success)
                    binding.badgeStatus.setTextColor(android.graphics.Color.parseColor("#03543F"))
                }
                0 -> {
                    binding.badgeStatus.text = "Nieaktywny"
                    binding.badgeStatus.setBackgroundResource(R.drawable.bg_badge_info)
                    binding.badgeStatus.setTextColor(android.graphics.Color.parseColor("#1E429F"))
                }
                else -> {
                    binding.badgeStatus.text = "Status: ${item.status}"
                    binding.badgeStatus.setBackgroundResource(R.drawable.bg_badge_info)
                    binding.badgeStatus.setTextColor(android.graphics.Color.parseColor("#1E429F"))
                }
            }
        }
    }
}
