package com.ossadkowski.crm.mobile.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.WniosekItem
import com.ossadkowski.crm.mobile.util.StatusHelper

class WnioskiAdapter(
    private val onClick: ((WniosekItem) -> Unit)? = null,
    private val onSend: ((WniosekItem) -> Unit)? = null,
    private val onResubmit: ((WniosekItem) -> Unit)? = null,
    private val onDelete: ((WniosekItem) -> Unit)? = null
) : ListAdapter<WniosekItem, WnioskiAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<WniosekItem>() {
        override fun areItemsTheSame(oldItem: WniosekItem, newItem: WniosekItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WniosekItem, newItem: WniosekItem) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.wniosek_type)
        val dates: TextView = view.findViewById(R.id.wniosek_dates)
        val status: TextView = view.findViewById(R.id.wniosek_status)
        val actionsRow: View = view.findViewById(R.id.actions_row)
        val btnSend: TextView = view.findViewById(R.id.btn_send)
        val btnResubmit: TextView = view.findViewById(R.id.btn_resubmit)
        val btnDelete: TextView = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wniosek, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.type.text = item.typ ?: "-"
        holder.dates.text = item.odDo ?: "-"
        holder.status.text = item.status ?: "-"
        StatusHelper.applyStatusStyle(holder.status, item.status)

        val status = item.status ?: ""
        val canSend = status == "Szkic"
        val canResubmit = status in listOf("Do poprawy", "Do poprawy (HR)")
        val canDelete = status in listOf("Szkic", "Do poprawy", "Do poprawy (HR)")
        val hasActions = canSend || canResubmit || canDelete

        holder.actionsRow.visibility = if (hasActions) View.VISIBLE else View.GONE
        holder.btnSend.visibility = if (canSend) View.VISIBLE else View.GONE
        holder.btnResubmit.visibility = if (canResubmit) View.VISIBLE else View.GONE
        holder.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onClick?.invoke(item) }
        holder.btnSend.setOnClickListener { onSend?.invoke(item) }
        holder.btnResubmit.setOnClickListener { onResubmit?.invoke(item) }
        holder.btnDelete.setOnClickListener { onDelete?.invoke(item) }
    }
}
