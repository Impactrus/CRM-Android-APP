package com.ossadkowski.crm.mobile.ui.calendar

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.model.CalendarDay
import com.ossadkowski.crm.mobile.databinding.ItemCalendarDayBinding

class CalendarGridAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarGridAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: CalendarDay) {
            binding.dayText.text = if (day.dayOfMonth > 0) day.dayOfMonth.toString() else ""
            
            val context = binding.root.context

            // Reset defaults
            binding.dayContainer.alpha = 1.0f
            binding.dayText.setTypeface(null, Typeface.NORMAL)

            // 1. Month/Frozen status background
            if (!day.isCurrentMonth) {
                binding.dayText.setTextColor(Color.parseColor("#D1D5DB"))
                binding.dayContainer.setBackgroundColor(Color.parseColor("#F9FAFB"))
            } else if (day.hasZamrozenia) {
                // Zamrożenie day styling (HR Calendar)
                binding.dayContainer.setBackgroundResource(R.drawable.bg_tag_red_soft)
                binding.dayText.setTextColor(Color.parseColor("#B91C1C"))
                binding.dayText.setTypeface(null, Typeface.BOLD)
            } else {
                // Normal day or Task day (no red background)
                binding.dayContainer.setBackgroundColor(Color.WHITE)
                binding.dayText.setTextColor(ContextCompat.getColor(context, R.color.crm_heading))
                if (day.hasTasks) {
                    binding.dayText.setTypeface(null, Typeface.BOLD)
                }
            }

            // 2. Today highlight (overrides background)
            if (day.isToday) {
                binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.crm_primary))
                binding.dayContainer.setBackgroundColor(Color.TRANSPARENT)
                binding.dayText.setTextColor(Color.WHITE)
                binding.dayText.setTypeface(null, Typeface.BOLD)
            } else {
                binding.dayCard.setCardBackgroundColor(Color.TRANSPARENT)
            }

            // 3. Selection
            binding.dayContainer.isSelected = day.isSelected

            // 4. Indicators
            binding.indicatorZamrozenie.visibility = if (day.hasZamrozenia) ViewGroup.VISIBLE else ViewGroup.GONE
            binding.indicatorTask.visibility = if (day.hasTasks) ViewGroup.VISIBLE else ViewGroup.GONE

            binding.root.setOnClickListener { onDayClick(day) }
        }
    }

    class DayDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }
        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
