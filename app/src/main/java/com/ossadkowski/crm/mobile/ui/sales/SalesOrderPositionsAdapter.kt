package com.ossadkowski.crm.mobile.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.SalesOrderPositionDto
import com.ossadkowski.crm.mobile.databinding.ItemSalesOrderPositionBinding
import java.util.Locale

class SalesOrderPositionsAdapter : ListAdapter<SalesOrderPositionDto, SalesOrderPositionsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSalesOrderPositionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSalesOrderPositionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SalesOrderPositionDto) {
            binding.posTowar.text = item.towar ?: "Nieznany towar"
            
            val ilosc = item.ilosc ?: 0.0
            binding.posIlosc.text = String.format(Locale.getDefault(), "%.2f", ilosc)
            
            val cena = item.cena ?: (item.cenaBaz ?: 0.0)
            binding.posCena.text = String.format(Locale.getDefault(), "%.2f PLN", cena)
            
            val netto = item.netto ?: 0.0
            binding.posNetto.text = String.format(Locale.getDefault(), "%.2f PLN", netto)
            
            binding.posMagazyn.text = item.magazyn ?: "Brak mag."
            
            val rabatProc = item.rabatProcent ?: 0.0
            val rabatPln = item.rabatPln ?: 0.0
            
            if (rabatProc > 0) {
                binding.posRabat.text = "Rabat: ${String.format(Locale.getDefault(), "%.1f", rabatProc)}%"
            } else if (rabatPln > 0) {
                binding.posRabat.text = "Rabat: ${String.format(Locale.getDefault(), "%.2f", rabatPln)} PLN"
            } else {
                binding.posRabat.text = "Brak rabatu"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SalesOrderPositionDto>() {
        override fun areItemsTheSame(oldItem: SalesOrderPositionDto, newItem: SalesOrderPositionDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SalesOrderPositionDto, newItem: SalesOrderPositionDto) = oldItem == newItem
    }
}
