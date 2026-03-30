package com.ossadkowski.app.ui.limitykredytowe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.app.R
import com.ossadkowski.app.data.model.LimitKredytowyListItem
import com.ossadkowski.app.util.StatusHelper

class LimityKredytoweAdapter(
    private val onClick: (LimitKredytowyListItem) -> Unit
) : ListAdapter<LimitKredytowyListItem, LimityKredytoweAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<LimitKredytowyListItem>() {
        override fun areItemsTheSame(oldItem: LimitKredytowyListItem, newItem: LimitKredytowyListItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LimitKredytowyListItem, newItem: LimitKredytowyListItem) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kontrahent: TextView = view.findViewById(R.id.item_kontrahent)
        val accountNum: TextView = view.findViewById(R.id.item_account_num)
        val limit: TextView = view.findViewById(R.id.item_limit)
        val status: TextView = view.findViewById(R.id.item_status)
        val date: TextView = view.findViewById(R.id.item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_limit_kredytowy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.kontrahent.text = item.kontrahentNazwa ?: "-"
        holder.accountNum.text = item.kontrahentAccountNum ?: ""
        holder.limit.text = "Wnioskowany: ${item.wnioskowanyLimit ?: 0} PLN"
        holder.status.text = item.status ?: "-"
        holder.date.text = item.createdAt?.take(10) ?: "-"
        StatusHelper.applyStatusStyle(holder.status, item.status)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
