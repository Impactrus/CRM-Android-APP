package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.HrHomeOfficeLimitDto
import com.ossadkowski.crm.mobile.databinding.ItemHrHomeOfficeCardBinding

class HrHomeOfficeAdapter : RecyclerView.Adapter<HrHomeOfficeAdapter.Holder>() {

    private var items = listOf<HrHomeOfficeLimitDto>()

    fun submitList(list: List<HrHomeOfficeLimitDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHrHomeOfficeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(private val binding: ItemHrHomeOfficeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HrHomeOfficeLimitDto) {
            val isGlobal = item.typLimitu == "G"
            
            if (isGlobal) {
                binding.tvName.text = "GLOBALNY LIMIT HO"
                binding.tvTypeLabel.text = "Dotyczy wszystkich pracowników"
                binding.ivTypeIcon.setImageResource(R.drawable.ic_home) // assume home icon for global
                binding.ivTypeIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            } else {
                binding.tvName.text = "${item.fname ?: ""} ${item.name ?: ""}".trim()
                binding.tvTypeLabel.text = "Limit indywidualny"
                binding.ivTypeIcon.setImageResource(R.drawable.ic_user)
            }
            
            val limit = item.limitDni ?: 0.0
            binding.tvLimitValue.text = if (limit % 1.0 == 0.0) limit.toInt().toString() else String.format("%.1f", limit)
            
            val utilized = item.wykorzystane ?: 0.0
            binding.tvUtilized.text = "${if (utilized % 1.0 == 0.0) utilized.toInt().toString() else String.format("%.1f", utilized)} dni"
            
            // Hide divider and utilized if it's a simple global header in some views, but here we show it.
        }
    }
}
