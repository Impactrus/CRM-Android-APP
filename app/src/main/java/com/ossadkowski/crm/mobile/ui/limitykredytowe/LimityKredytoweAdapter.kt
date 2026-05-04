package com.ossadkowski.crm.mobile.ui.limitykredytowe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.LimitKredytowyListItem
import com.ossadkowski.crm.mobile.util.StatusHelper

class LimityKredytoweAdapter(
    private val onClick: (LimitKredytowyListItem) -> Unit
) : ListAdapter<LimitKredytowyListItem, LimityKredytoweAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<LimitKredytowyListItem>() {
        override fun areItemsTheSame(oldItem: LimitKredytowyListItem, newItem: LimitKredytowyListItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LimitKredytowyListItem, newItem: LimitKredytowyListItem) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemId: TextView = view.findViewById(R.id.item_id)
        val kontrahent: TextView = view.findViewById(R.id.item_kontrahent)
        val accountNum: TextView = view.findViewById(R.id.item_account_num)
        val limitCurrent: TextView = view.findViewById(R.id.item_limit_current)
        val limitRequested: TextView = view.findViewById(R.id.item_limit_requested)
        val axSync: TextView = view.findViewById(R.id.item_ax_sync)
        val status: TextView = view.findViewById(R.id.item_status)
        val date: TextView = view.findViewById(R.id.item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_limit_kredytowy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        
        holder.itemId.text = "#${item.id}"
        holder.kontrahent.text = item.kontrahentNazwa ?: "-"
        holder.accountNum.text = item.kontrahentAccountNum ?: ""
        
        holder.limitCurrent.text = "Obecny: ${String.format("%.2f", item.obecnyLimit ?: 0.0)} PLN"
        holder.limitRequested.text = "Wnioskowany: ${String.format("%.2f", item.wnioskowanyLimit ?: 0.0)} PLN"
        
        holder.status.text = item.status ?: "-"
        holder.date.text = item.createdAt?.take(10) ?: "-"
        
        // AX Sync status
        if (item.axSync == true) {
            holder.axSync.text = context.getString(R.string.ax_sync_ok)
            holder.axSync.setBackgroundResource(R.drawable.bg_status_badge)
            androidx.core.view.ViewCompat.setBackgroundTintList(holder.axSync, android.content.res.ColorStateList.valueOf(context.getColor(R.color.crm_success)))
        } else {
            holder.axSync.text = context.getString(R.string.ax_sync_none)
            holder.axSync.setBackgroundResource(R.drawable.bg_status_badge)
            androidx.core.view.ViewCompat.setBackgroundTintList(holder.axSync, android.content.res.ColorStateList.valueOf(context.getColor(R.color.crm_error)))
        }
        
        StatusHelper.applyStatusStyle(holder.status, item.status)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
