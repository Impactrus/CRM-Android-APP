package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.WniosekHistoryItem
import com.ossadkowski.crm.mobile.databinding.ItemWniosekHistoryBinding

class HrHistoriaAdapter(private val onClick: (WniosekHistoryItem) -> Unit) :
    ListAdapter<WniosekHistoryItem, HrHistoriaAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemWniosekHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WniosekHistoryItem, isFirst: Boolean, isLast: Boolean) {
            // Timeline
            binding.timelineLineTop.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
            binding.timelineLineBottom.visibility = if (isLast) View.INVISIBLE else View.VISIBLE

            binding.tvTyp.text = item.typ ?: "Nieznany typ"
            binding.tvOdDo.text = item.odDo ?: "-"
            
            // Ilość dni
            val dni = item.iloscDni ?: 0
            val dniTekst = when {
                dni == 1 -> "1 dzień"
                dni > 0 -> "$dni dni"
                else -> ""
            }
            binding.tvIloscDni.text = dniTekst
            binding.tvIloscDni.visibility = if (dniTekst.isEmpty()) View.GONE else View.VISIBLE
            binding.tvDotDivider.visibility = if (dniTekst.isEmpty() && (item.godziny ?: 0) == 0) View.GONE else View.VISIBLE

            // Godziny
            if (item.godziny != null && item.godziny > 0) {
                binding.tvGodziny.text = "${item.godziny}h"
                binding.tvGodziny.visibility = View.VISIBLE
            } else {
                binding.tvGodziny.visibility = View.GONE
            }

            // Powód
            if (!item.powod.isNullOrBlank()) {
                binding.tvPowod.text = item.powod
                binding.tvPowod.visibility = View.VISIBLE
            } else {
                binding.tvPowod.visibility = View.GONE
            }

            // Data złożenia
            binding.tvCreatedAt.text = item.createdAt ?: "-"

            // Status i kropka
            binding.tvStatus.text = item.status ?: "Brak"
            
            val statusLower = item.status?.lowercase() ?: ""
            when {
                statusLower.contains("zaakceptowane") || statusLower.contains("zatwierdzone") -> {
                    binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_green)
                    binding.timelineDot.setBackgroundResource(R.drawable.bg_status_dot_green)
                }
                statusLower.contains("w trakcie") -> {
                    binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_orange)
                    binding.timelineDot.setBackgroundResource(R.drawable.bg_status_dot_orange)
                }
                statusLower.contains("odrzucone") || statusLower.contains("anulowane") -> {
                    binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_red)
                    binding.timelineDot.setBackgroundResource(R.drawable.bg_status_dot_red)
                }
                else -> {
                    // Wprowadzono, Cofnięte, Odwołane, Szkic
                    binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_gray)
                    binding.timelineDot.setBackgroundResource(R.drawable.bg_timeline_dot)
                }
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWniosekHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isFirst = position == 0
        val isLast = position == itemCount - 1
        holder.bind(getItem(position), isFirst, isLast)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<WniosekHistoryItem>() {
            override fun areItemsTheSame(oldItem: WniosekHistoryItem, newItem: WniosekHistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: WniosekHistoryItem, newItem: WniosekHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
