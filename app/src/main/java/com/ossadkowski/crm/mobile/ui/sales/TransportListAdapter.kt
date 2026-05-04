package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.TransportCenyItem
import com.ossadkowski.crm.mobile.databinding.ItemTransportBinding

class TransportListAdapter(private val onClick: (TransportCenyItem) -> Unit) :
    ListAdapter<TransportCenyItem, TransportListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTransportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransportCenyItem) {
            binding.textKontrahent.text = item.kontrahentNazwa ?: "Nieznany"
            binding.textStatus.text = item.status ?: "Nowy"
            binding.textTowar.text = item.towar
            binding.textDetails.text = "${item.ilosc} t | AX: ${item.kontraktAx ?: "-"}"
            binding.textDate.text = item.createdAt
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransportCenyItem>() {
        override fun areItemsTheSame(oldItem: TransportCenyItem, newItem: TransportCenyItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TransportCenyItem, newItem: TransportCenyItem) = oldItem == newItem
    }
}
