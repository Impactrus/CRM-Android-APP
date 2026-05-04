package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.GrainContractListItem
import com.ossadkowski.crm.mobile.databinding.ItemGrainContractBinding

class GrainContractsListAdapter(private val onClick: (GrainContractListItem) -> Unit) :
    ListAdapter<GrainContractListItem, GrainContractsListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGrainContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemGrainContractBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GrainContractListItem) {
            binding.textNr.text = item.nr ?: "Brak numeru"
            
            // Mapowanie statusów
            when (item.status?.uppercase()) {
                "DRAFT" -> {
                    binding.textStatus.text = "Szkic"
                    binding.textStatus.setBackgroundResource(com.ossadkowski.crm.mobile.R.drawable.bg_status_draft)
                }
                "PENDING" -> {
                    binding.textStatus.text = "Oczekujący"
                    binding.textStatus.setBackgroundResource(com.ossadkowski.crm.mobile.R.drawable.bg_status_pending)
                }
                "APPROVED" -> {
                    binding.textStatus.text = "Zatwierdzony"
                    binding.textStatus.setBackgroundResource(com.ossadkowski.crm.mobile.R.drawable.bg_status_approved)
                }
                "REJECTED" -> {
                    binding.textStatus.text = "Odrzucony"
                    binding.textStatus.setBackgroundResource(com.ossadkowski.crm.mobile.R.drawable.bg_status_rejected)
                }
                else -> {
                    binding.textStatus.text = item.status ?: "Nieznany"
                    binding.textStatus.setBackgroundResource(com.ossadkowski.crm.mobile.R.drawable.bg_status_draft)
                }
            }

            binding.textKontrahent.text = item.kontrahent ?: "Nieznany"
            binding.textRodzaj.text = item.rodzajZboza ?: "-"
            binding.textIlosc.text = "Ilość: ${item.ilosc ?: 0.0} t"
            binding.textCena.text = "${item.price ?: 0.0} PLN/t"
            binding.textData.text = item.data?.split("T")?.firstOrNull() ?: item.data
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GrainContractListItem>() {
        override fun areItemsTheSame(oldItem: GrainContractListItem, newItem: GrainContractListItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GrainContractListItem, newItem: GrainContractListItem) = oldItem == newItem
    }
}
