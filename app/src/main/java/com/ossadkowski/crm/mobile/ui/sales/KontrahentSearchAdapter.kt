package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.databinding.ItemKontrahentSearchBinding

class KontrahentSearchAdapter(private val onItemSelected: (KontrahentSearchItem) -> Unit) :
    ListAdapter<KontrahentSearchItem, KontrahentSearchAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKontrahentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemKontrahentSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KontrahentSearchItem) {
            binding.textName.text = item.name ?: "Brak nazwy"
            binding.textAccountNum.text = "KOD: ${item.accountNum ?: "-"}"
            binding.textNip.text = "NIP: ${item.nip ?: "-"}"
            binding.textAddress.text = item.address ?: "-"
            
            binding.root.setOnClickListener { onItemSelected(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<KontrahentSearchItem>() {
        override fun areItemsTheSame(oldItem: KontrahentSearchItem, newItem: KontrahentSearchItem) = 
            oldItem.accountNum == newItem.accountNum
        override fun areContentsTheSame(oldItem: KontrahentSearchItem, newItem: KontrahentSearchItem) = 
            oldItem == newItem
    }
}
