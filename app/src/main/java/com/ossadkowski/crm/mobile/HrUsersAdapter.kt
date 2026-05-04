package com.ossadkowski.crm.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.data.model.SlownikItemDto

class HrUsersAdapter(
    private val onItemClick: (SlownikItemDto) -> Unit
) : RecyclerView.Adapter<HrUsersAdapter.UserViewHolder>() {

    private var users = listOf<SlownikItemDto>()

    fun submitList(list: List<SlownikItemDto>) {
        users = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(users[adapterPosition])
                }
            }
        }

        fun bind(user: SlownikItemDto) {
            textView.text = user.nazwa
            textView.setPadding(32, 32, 32, 32)
            textView.setTextColor(android.graphics.Color.parseColor("#374151"))
            textView.setBackgroundResource(R.drawable.bg_input_field) // Use simple rounded background if wanted
        }
    }
}
