package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.databinding.ItemTowarSearchBinding

class TowarSearchAdapter(private val onItemSelected: (TowarListItem) -> Unit) :
    ListAdapter<TowarListItem, TowarSearchAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTowarSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemTowarSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TowarListItem) {
            binding.textNazwa.text = if (!item.nazwa.isNullOrBlank()) item.nazwa else item.kod ?: "Nieznany towar"
            binding.textKod.text = "Kod: ${item.kod ?: "-"}"
            binding.textBranza.text = "Branża: ${item.branza ?: "-"}"
            binding.textCena.text = String.format("%.2f PLN", item.cena ?: 0.0)
            binding.textMagazyn.text = "Magazyn: ${item.magazyn ?: "-"}"
            binding.textDostepne.text = "Dostępne: ${item.dostepne ?: 0.0}"

            binding.root.setOnClickListener { onItemSelected(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TowarListItem>() {
        override fun areItemsTheSame(oldItem: TowarListItem, newItem: TowarListItem) = oldItem.kod == newItem.kod
        override fun areContentsTheSame(oldItem: TowarListItem, newItem: TowarListItem) = oldItem == newItem
    }
}
