package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.ZamrozenieDto

class HrZamrozeniaAdapter : RecyclerView.Adapter<HrZamrozeniaAdapter.Holder>() {

    private var items = listOf<ZamrozenieDto>()

    fun submitList(list: List<ZamrozenieDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hr_zamrozenie, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val datesTv = itemView.findViewById<TextView>(R.id.zamrozenie_dates)
        private val detailsTv = itemView.findViewById<TextView>(R.id.zamrozenie_details)

        fun bind(item: ZamrozenieDto) {
            val od = item.dataOd?.substring(0, 10) ?: ""
            val do_ = item.dataDo?.substring(0, 10) ?: ""
            datesTv.text = "Ograniczenie: $od  -  $do_"
            detailsTv.text = "Dział: ${item.dzial ?: "Wszyscy"} | Powód: ${item.opis ?: "Brak"}"
        }
    }
}
