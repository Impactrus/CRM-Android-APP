package com.ossadkowski.crm.mobile.ui.approval

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.WniosekItem
import com.ossadkowski.crm.mobile.util.StatusHelper

class ApprovalAdapter(
    private val onApprove: (WniosekItem) -> Unit,
    private val onReject: (WniosekItem) -> Unit,
    private val onItemClick: (WniosekItem) -> Unit = {}
) : ListAdapter<WniosekItem, ApprovalAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<WniosekItem>() {
        override fun areItemsTheSame(oldItem: WniosekItem, newItem: WniosekItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WniosekItem, newItem: WniosekItem) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val employee: TextView = view.findViewById(R.id.approval_employee)
        val type: TextView = view.findViewById(R.id.approval_type)
        val dates: TextView = view.findViewById(R.id.approval_dates)
        val hours: TextView = view.findViewById(R.id.approval_hours)
        val reason: TextView = view.findViewById(R.id.approval_reason)
        val days: TextView = view.findViewById(R.id.approval_days)
        val status: TextView = view.findViewById(R.id.approval_status)
        val btnApprove: Button = view.findViewById(R.id.btn_approve)
        val btnReject: Button = view.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_approval, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.employee.text = item.username ?: "-"
        holder.type.text = item.typ ?: "-"
        holder.dates.text = item.odDo ?: "-"
        holder.hours.text = item.godziny?.toString() ?: "-"
        holder.reason.text = item.powod ?: "-"
        holder.days.text = item.iloscDni?.toString() ?: "-"
        holder.status.text = item.status ?: "-"
        StatusHelper.applyStatusStyle(holder.status, item.status)

        // Kliknięcie w kartę → otwiera widok szczegółów
        holder.itemView.setOnClickListener { onItemClick(item) }

        // Szybkie przyciski bezpośrednio na liście
        holder.btnApprove.setOnClickListener { onApprove(item) }
        holder.btnReject.setOnClickListener { onReject(item) }
    }
}
