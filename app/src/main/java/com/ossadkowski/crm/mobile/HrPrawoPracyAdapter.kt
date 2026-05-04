package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.HrPrawoPracyTypDto
import com.ossadkowski.crm.mobile.databinding.ItemHrPrawoPracyCardBinding

class HrPrawoPracyAdapter(
    private val onActionClick: (HrPrawoPracyTypDto, String) -> Unit
) : RecyclerView.Adapter<HrPrawoPracyAdapter.Holder>() {

    private var items = listOf<HrPrawoPracyTypDto>()

    fun submitList(list: List<HrPrawoPracyTypDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHrPrawoPracyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(private val binding: ItemHrPrawoPracyCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HrPrawoPracyTypDto) {
            binding.tvKod.text = item.kod
            binding.tvNazwa.text = item.nazwa
            binding.tvPodstawa.text = item.podstawaPrawna ?: "Brak podstawy"
            
            val limit = item.limitRoczny ?: 0.0
            val limitStr = if (limit % 1.0 == 0.0) limit.toInt().toString() else String.format("%.2f", limit)
            val unitStr = (item.jednostka ?: "DNI").uppercase()
            
            val altLimit = item.limitRocznyAlt
            if (altLimit != null && altLimit > 0) {
                val altLimitStr = if (altLimit % 1.0 == 0.0) altLimit.toInt().toString() else String.format("%.2f", altLimit)
                val altUnitStr = (item.jednostkaAlt ?: "godz").uppercase()
                binding.tvLimit.text = "$limitStr $unitStr / $altLimitStr $altUnitStr"
                binding.tvJednostka.visibility = android.view.View.GONE
            } else {
                binding.tvLimit.text = limitStr
                binding.tvJednostka.text = unitStr
                binding.tvJednostka.visibility = android.view.View.VISIBLE
            }
            
            binding.tvOpis.text = item.opis ?: "Brak opisu szczegółowego."

            binding.btnZmienLimit.setOnClickListener { onActionClick(item, "EDIT") }
            binding.btnHistoria.setOnClickListener { onActionClick(item, "HISTORY") }
            binding.btnDezaktywuj.setOnClickListener { onActionClick(item, "DELETE") }
        }
    }
}
