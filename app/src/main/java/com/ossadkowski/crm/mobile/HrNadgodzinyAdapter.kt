package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.databinding.ItemHrNadgodzinyCardBinding

class HrNadgodzinyAdapter : RecyclerView.Adapter<HrNadgodzinyAdapter.Holder>() {

    private var items = listOf<HrNadgodzinyGrouped>()

    fun submitList(list: List<HrNadgodzinyGrouped>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHrNadgodzinyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(private val binding: ItemHrNadgodzinyCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HrNadgodzinyGrouped) {
            binding.tvName.text = "${item.fname} ${item.name}".trim()
            binding.tvDepart.text = item.depart
            
            binding.tvQ1.text = String.format("%.2f", item.q1)
            binding.tvQ2.text = String.format("%.2f", item.q2)
            binding.tvQ3.text = String.format("%.2f", item.q3)
            binding.tvQ4.text = String.format("%.2f", item.q4)
            
            binding.tvTotalSaldo.text = String.format("%.2f", item.totalSaldo)
        }
    }
}
