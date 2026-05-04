package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.TransportAxContract
import com.ossadkowski.crm.mobile.databinding.ItemAxContractBinding

class AxContractSearchAdapter(private val onClick: (TransportAxContract) -> Unit) :
    ListAdapter<TransportAxContract, AxContractSearchAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAxContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAxContractBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransportAxContract) {
            binding.textContractId.text = item.id
            binding.textVendor.text = item.vendorName ?: "Brak dostawcy"
            binding.textItem.text = "${item.itemName ?: ""} (${item.itemId ?: ""})"
            binding.textQuantity.text = "Ilość: ${item.quantity ?: 0.0} t"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransportAxContract>() {
        override fun areItemsTheSame(oldItem: TransportAxContract, newItem: TransportAxContract) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TransportAxContract, newItem: TransportAxContract) = oldItem == newItem
    }
}
