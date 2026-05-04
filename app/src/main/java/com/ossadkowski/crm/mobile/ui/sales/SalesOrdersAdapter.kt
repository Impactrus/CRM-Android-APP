package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.SalesOrderListItem
import com.ossadkowski.crm.mobile.databinding.ItemSalesOrderBinding
import java.util.Locale

class SalesOrdersAdapter(
    private val onItemClick: (SalesOrderListItem) -> Unit
) : ListAdapter<SalesOrderListItem, SalesOrdersAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSalesOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSalesOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SalesOrderListItem) {
            binding.itemNrZam.text = item.nrZam
            binding.itemKontrahent.text = item.kontrahent ?: "---"
            binding.itemAxId.text = "AX: ${item.nrAx ?: "-"}"
            
            val count = item.iloscTowarow ?: 0
            val rawDate = item.dataUtworzenia ?: "---"
            val date = if (rawDate.contains("T")) rawDate.split("T")[0] else rawDate
            val formattedDate = date.split("-").let { if (it.size == 3) "${it[2]}.${it[1]}.${it[0]}" else date }
            binding.itemDetails.text = "Ilość tow.: $count | Data: $formattedDate"
            
            val value = item.wartoscNetto ?: 0.0
            binding.itemValue.text = String.format(Locale.getDefault(), "%.2f PLN", value)
            
            binding.itemStatus.text = item.status ?: "Brak"
            
            // Status styling
            val statusRes = when (item.status?.lowercase()) {
                "szkic" -> R.drawable.bg_status_szkic
                "zaakceptowany" -> R.drawable.bg_status_zaakceptowany
                "w trakcie" -> R.drawable.bg_status_w_trakcie
                "odrzucony" -> R.drawable.bg_status_odrzucony
                else -> R.drawable.bg_status_badge
            }
            binding.itemStatus.setBackgroundResource(statusRes)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SalesOrderListItem>() {
        override fun areItemsTheSame(oldItem: SalesOrderListItem, newItem: SalesOrderListItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SalesOrderListItem, newItem: SalesOrderListItem) = oldItem == newItem
    }
}
