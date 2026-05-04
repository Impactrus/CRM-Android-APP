package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.databinding.ItemNaleznoscBinding
import com.ossadkowski.crm.mobile.data.model.Naleznosc
import java.text.NumberFormat
import java.util.Locale

class NaleznosciAdapter : RecyclerView.Adapter<NaleznosciAdapter.ViewHolder>() {

    private var items: List<Naleznosc> = emptyList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pl", "PL"))

    fun updateData(newItems: List<Naleznosc>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNaleznoscBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemNaleznoscBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Naleznosc) {
            binding.textDokument.text = item.dokument
            binding.textData.text = item.data ?: "-"
            binding.textTermin.text = item.termin ?: "-"
            
            binding.textKwota.text = formatCurrency(item.kwota, item.waluta)
            binding.textPozostalo.text = formatCurrency(item.pozostalo, item.waluta)

            if (item.dniPoTerminie != null && item.dniPoTerminie > 0) {
                binding.textDniPoTerminie.visibility = View.VISIBLE
                binding.textDniPoTerminie.text = "${item.dniPoTerminie} dni po terminie"
            } else {
                binding.textDniPoTerminie.visibility = View.GONE
            }
        }

        private fun formatCurrency(amount: Double?, currency: String?): String {
            if (amount == null) return "0,00 PLN"
            return try {
                val formatted = String.format("%.2f", amount).replace(".", ",")
                "$formatted ${currency ?: "PLN"}"
            } catch (e: Exception) {
                "0,00 PLN"
            }
        }
    }
}
