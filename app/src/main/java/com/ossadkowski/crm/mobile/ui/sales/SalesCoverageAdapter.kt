package com.ossadkowski.crm.mobile.ui.sales

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.SalesCoverageListItem
import java.util.Locale

class SalesCoverageAdapter(
    private val onClick: (SalesCoverageListItem) -> Unit
) : ListAdapter<SalesCoverageListItem, SalesCoverageAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<SalesCoverageListItem>() {
        override fun areItemsTheSame(oldItem: SalesCoverageListItem, newItem: SalesCoverageListItem) = oldItem.contractNumber == newItem.contractNumber
        override fun areContentsTheSame(oldItem: SalesCoverageListItem, newItem: SalesCoverageListItem) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textContractNumber: TextView = view.findViewById(R.id.textContractNumber)
        val textStatus: TextView = view.findViewById(R.id.textStatus)
        val textClientName: TextView = view.findViewById(R.id.textClientName)
        val textItemName: TextView = view.findViewById(R.id.textItemName)
        val progressCoverage: ProgressBar = view.findViewById(R.id.progressCoverage)
        val textCoveragePercent: TextView = view.findViewById(R.id.textCoveragePercent)
        val progressDelivery: ProgressBar = view.findViewById(R.id.progressDelivery)
        val textDeliveryPercent: TextView = view.findViewById(R.id.textDeliveryPercent)
        val textSalesQty: TextView = view.findViewById(R.id.textSalesQty)
        val textCoverageQty: TextView = view.findViewById(R.id.textCoverageQty)
        val textGapQty: TextView = view.findViewById(R.id.textGapQty)
        val textPriceSpread: TextView = view.findViewById(R.id.textPriceSpread)
        val textDkz: TextView = view.findViewById(R.id.textDkz)
        val textDueDate: TextView = view.findViewById(R.id.textDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sales_coverage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        holder.textContractNumber.text = item.contractNumber ?: "Brak numeru"
        holder.textClientName.text = item.clientName ?: "-"
        holder.textItemName.text = item.itemName ?: "-"

        // Progress bars
        val covPercent = (item.coveragePercent ?: 0.0).toInt()
        val delPercent = (item.deliveryPercent ?: 0.0).toInt()
        holder.progressCoverage.progress = covPercent
        holder.textCoveragePercent.text = "$covPercent%"
        holder.progressDelivery.progress = delPercent
        holder.textDeliveryPercent.text = "$delPercent%"

        // Quantities
        holder.textSalesQty.text = String.format(Locale.getDefault(), "%,.2f t", item.salesQty ?: 0.0)
        holder.textCoverageQty.text = String.format(Locale.getDefault(), "%,.2f t", item.coverageQty ?: 0.0)
        
        val gap = item.gapQty ?: 0.0
        holder.textGapQty.text = String.format(Locale.getDefault(), "%,.2f t", gap)
        if (gap > 0) {
            holder.textGapQty.setTextColor(Color.parseColor("#E53E3E"))
        } else {
            holder.textGapQty.setTextColor(Color.parseColor("#4A5568"))
        }

        // Price, spread, dkz, date
        val priceStr = String.format(Locale.getDefault(), "%,.2f", item.price ?: 0.0)
        val spreadStr = String.format(Locale.getDefault(), "%+,.2f", item.spread ?: 0.0)
        holder.textPriceSpread.text = "$priceStr / $spreadStr"
        holder.textDkz.text = item.dkz ?: "-"
        holder.textDueDate.text = item.dueDate?.take(10) ?: "-"

        // Status Badge Style
        holder.textStatus.text = item.status ?: "Nieznany"
        holder.textStatus.setBackgroundResource(R.drawable.bg_status_badge)
        
        val statusLower = (item.status ?: "").lowercase(Locale.getDefault())
        val badgeColor = if (statusLower.contains("pełne") || statusLower.contains("ok")) {
            Color.parseColor("#10B981") // Emerald Green
        } else if (statusLower.contains("luka") || gap > 0) {
            Color.parseColor("#F97316") // Orange
        } else {
            Color.parseColor("#6B7280") // Grey
        }
        ViewCompat.setBackgroundTintList(holder.textStatus, ColorStateList.valueOf(badgeColor))

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
