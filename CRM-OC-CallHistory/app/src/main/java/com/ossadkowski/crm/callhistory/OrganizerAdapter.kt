package com.ossadkowski.crm.callhistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.callhistory.databinding.ItemOrganizerBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrganizerAdapter(
    private val onSimulateClick: (OrganizerItem) -> Unit,
    private val onAddNoteClick: (OrganizerItem) -> Unit
) : RecyclerView.Adapter<OrganizerAdapter.ViewHolder>() {

    private var items = listOf<OrganizerItem>()

    fun submitList(newList: List<OrganizerItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrganizerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemOrganizerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrganizerItem) {
            binding.clientName.text = item.name
            binding.clientAddress.text = item.address
            binding.clientCoords.text = String.format(Locale.US, "GPS: %.6f, %.6f", item.latitude, item.longitude)

            if (item.lastVisitNote.isNotBlank()) {
                binding.noteContainer.visibility = View.VISIBLE
                val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val dateStr = df.format(Date(item.visitedAt))
                binding.clientNote.text = "${item.lastVisitNote} (${dateStr})"
            } else {
                binding.noteContainer.visibility = View.GONE
            }

            binding.btnSimulate.setOnClickListener { onSimulateClick(item) }
            binding.btnAddNote.setOnClickListener { onAddNoteClick(item) }
        }
    }
}
