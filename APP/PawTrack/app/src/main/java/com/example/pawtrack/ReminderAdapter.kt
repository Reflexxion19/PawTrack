package com.example.pawtrack

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReminderAdapter(private var reminderList: List<SavedAlarm>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val currentItem = reminderList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = reminderList.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val checkBoxRepeat: CheckBox = itemView.findViewById(R.id.checkBoxRepeat)

        fun bind(item: SavedAlarm) {
            textViewMessage.text = item.message
            textViewTime.text = item.time
            checkBoxRepeat.isChecked = item.repeat
        }


    }

    fun updateData(newReminderList: List<SavedAlarm>) {
        reminderList = newReminderList
        notifyDataSetChanged()
    }
}
